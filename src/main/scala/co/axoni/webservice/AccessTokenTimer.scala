package co.axoni.webservice

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{Session, SessionFactory}
import spray.client.pipelining._
import spray.http.{HttpResponse, HttpRequest}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.{Failure, Success}
import spray.json._
import java.util.concurrent._

/**
  * Created by pbanavara on 11/01/16.
  */
object AccessTokenTimer {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  def setTimerForUpdatingAccessTokens() = {
    val ex = new ScheduledThreadPoolExecutor(1)
    val task = new Runnable {
      def run() = updateAccessTokens()
    }
    val f = ex.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS)
  }

  def updateAccessTokens(): Unit = {
    val databaseUsername = "dbuser"
    val databasePassword = "dbpassword"
    val databaseConnection = "jdbc:mysql://localhost:3306/emily"
    Class.forName("com.mysql.jdbc.Driver")
    SessionFactory.concreteFactory = Some(() => Session.create(
      java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
      new MySQLAdapter))
    transaction {
      //val queriedUser:UserToken = Schema.users.where(user => user.emailId === emailId).single
      val emails = from(Schema.users)(s => select(s))
      emails.foreach(e => {
        val accessTokenFuture = getAccessTokenUsingRefreshToken(e.refreshToken)
        accessTokenFuture onComplete {
          case Success(mesg) => {
            e.accessToken = mesg.toString.parseJson.asJsObject.getFields("access_token").head.toString().stripPrefix(""""""").stripSuffix(""""""").trim
            SessionFactory.concreteFactory = Some(() => Session.create(
              java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
              new MySQLAdapter))
            transaction {
              Schema.users.update(e)
              logger.debug("Access token for user :" + e.emailId + " updated")
            }

          }
          case Failure(e) => {
             logger.debug(e.toString)
          }
        }
        })
    }
  }

  def getAccessTokenUsingRefreshToken(refreshToken: String): Future[String] = {
    implicit val system = ActorSystem("simple-spray-client")
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val response: Future[HttpResponse] = pipeline(Post("https://www.googleapis.com/oauth2/v4/token?grant_type=refresh_token" +
      "&client_id=643564087741-h7tlsge34q3skhp6vr70lolrcf42du6d.apps.googleusercontent.com&client_secret=f-LzX6NLSM7dh4WEKCaL5-c0&refresh_token=" + refreshToken))
    response map  {
      case HttpResponse(e, f, g, h) => {
        f.data.asString
      }
    }
  }
}
