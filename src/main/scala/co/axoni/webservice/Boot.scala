package co.axoni.webservice

import java.util.logging.Level

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import co.axoni.database.MongoOperations
import co.axoni.processemail.{Emily, MeetingRequest}
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  LoggerFactory.getLogger("org.mongodb.driver.cluster").asInstanceOf[
    ch.qos.logback.classic.Logger].setLevel(ch.qos.logback.classic.Level.INFO)
  LoggerFactory.getLogger("org.mongodb.driver.connection").asInstanceOf[
    ch.qos.logback.classic.Logger].setLevel(ch.qos.logback.classic.Level.INFO)
  val localRun = System.getProperty("localrun") != null
  if (localRun) {
    logger.debug("Running locally")
  } else {
    logger.debug("Running on server")
  }

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[EmilyHttpServiceActor], "demo-service")

  implicit val timeout = Timeout(60.seconds)

  // Init Loading of models before server start
  co.axoni.processemail.Emily.NLP
  // start a new HTTP server on port 8080 with our service actor as the handler

  if (localRun) {
    // NOTE: For local testing, use below
    IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
  }
  else {
    IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
  }

  // Initialize from DB
  Emily.meetingRequestID = MongoOperations.getMeetingRequestID()
  Emily.userToMeetingRequestsMapping = MongoOperations.getUserToMeetingRequestsMapping()
  Emily.supervisorTransactionID = MongoOperations.getSupervisorTransactionID()
}
