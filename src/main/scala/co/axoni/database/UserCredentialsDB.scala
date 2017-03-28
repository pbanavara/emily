package co.axoni.database

import co.axoni.webservice.{UserEmail, UserToken, Schema}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{Session, SessionFactory}

/**
  * Created by rajeevgopalakrishna on 1/30/16.
  */
object UserCredentialsDB {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  val databaseUsername = "dbuser"
  val databasePassword = "dbpassword"
  val databaseConnection = "jdbc:mysql://localhost:3306/emily"
  Class.forName("com.mysql.jdbc.Driver")
  SessionFactory.concreteFactory = Some(() => Session.create(
    java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
    new MySQLAdapter))

  def storeAccessToken(emailId: String, accessToken: String, refreshToken: String): Unit = {
    transaction {
      val user: UserToken = new UserToken(emailId, accessToken, refreshToken)
      try {
        val queriedUser = Schema.users.where(user => user.emailId === emailId)
        if (queriedUser.size < 1) {
          Schema.users.insert(user)
          logger.debug("Inserted user")
        }
      } catch {
        case e: Exception => logger.debug(e.getMessage)
      }
    }
  }

  def getAccessToken(emailId: String): String = {
    transaction {
      val queriedUser: Option[UserToken] = Schema.users.where(user => user.emailId === emailId).headOption
      queriedUser match {
        case Some(user) =>
          user.accessToken
        case None =>
          "None"
      }
    }
  }

  def storeEmail(emailId: String): Unit = {
    transaction {
      val user1: UserEmail = new UserEmail(emailId)
      try {
        val queriedUser = Schema.users.where(user => user.emailId === emailId)
        if (queriedUser.size < 1) {
          Schema.usersWithOnlyEmails.insert(user1)
          logger.debug("Inserted user1")
        }
      } catch {
        case e: Exception => {
          logger.debug(e.getMessage)
        }

      }
    }
  }

}
