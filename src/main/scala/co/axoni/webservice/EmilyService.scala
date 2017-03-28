package co.axoni.webservice

import akka.actor.{Actor, ActorSystem}
import co.axoni.database.{MongoOperations, UserCredentialsDB}
import co.axoni.processemail.{MeetingTime, _}
import co.axoni.database.{UserCredentialsDB, MongoOperations}
import co.axoni.processemail.Emily._
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON._
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{KeyedEntity, Session, SessionFactory, _}
import spray.http._
import spray.json._
import spray.routing._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}
import MongoOperations._

class EmilyHttpServiceActor extends Actor with EmilyHttpService {
  // Create the database connection
  val databaseUsername = "dbuser"
  val databasePassword = "dbpassword"
  val databaseConnection = "jdbc:mysql://localhost:3306/emily"
  Class.forName("com.mysql.jdbc.Driver")
  SessionFactory.concreteFactory = Some(() => Session.create(
    java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
    new MySQLAdapter))

  def actorRefFactory = context

  co.axoni.webservice.AccessTokenTimer.setTimerForUpdatingAccessTokens()

  def receive = runRoute(myRoute)
}

class BaseEntity extends KeyedEntity[Long] {
  val id: Long = 0
}

class UserToken(var emailId: String, var accessToken: String, var refreshToken: String) extends BaseEntity {
  def this() = this("", "", "")
}

class UserEmail(var emailId: String) extends BaseEntity {
  def this() = this("")
}

object Schema extends Schema {
  val users = table[UserToken]
  on(users)(user => declare(
    user.id is (autoIncremented),
    user.emailId is (unique)
  ))

  val usersWithOnlyEmails = table[UserEmail]
  on(usersWithOnlyEmails)(user => declare(
    user.id is (autoIncremented),
    user.emailId is (unique)
  ))

}

/*
  This trait definition is for obtaining the results of email classification to - either confirm or non-confirm emails.
 */

trait EmilyHttpService extends HttpService {

  import co.axoni.webservice.EmailJsonSupport._
  import spray.client.pipelining._

  val logger = Logger(LoggerFactory.getLogger("EmilyHttpService"))

  def convertToTestOutput(listDB: BasicDBList): List[TestOutput] = {
    var returnList: List[TestOutput] = List()
    listDB.foreach(elm =>
      returnList = returnList :+
        TestOutput(
          elm.asInstanceOf[BasicDBObject].get("headers").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("to").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("from").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("cc").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("subject").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("text").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("headerReferences").asInstanceOf[String],
          elm.asInstanceOf[BasicDBObject].get("attachment").asInstanceOf[String]
        )
    )
    returnList
  }

  def convertToJson(jVal: scala.collection.mutable.MutableList[Map[String, String]]): Unit = {
    logger.debug(jVal.toList.toJson.toString())
  }

  val myRoute = {
    implicit val system = ActorSystem("simple-spray-client")
    import spray.json._
    import system.dispatcher

    path("classify" / "email") {
      post {
        entity(as[EmailInput]) { ed =>
          logger.debug(ed.body)
          val result = ClassifyEmail.classifyEmails(ed.body)
          result match {
            case true =>
              complete(EmailData(ed.body, true))
            case false =>
              complete(EmailData(ed.body, false))
          }
        }
      }
    } ~
      pathSingleSlash {
        get {
          getFromResource("index.html")
        }
      } ~
      path("signin") {
        get {
          logger.debug("Signup called")
          if (!Boot.localRun) {
            redirect("https://accounts.google.com/o/oauth2/auth?client_id=643564087741-h7tlsge34q3skhp6vr70lolrcf42du6d.apps.googleusercontent.com&redirect_uri=http://axoni.co/Callback&response_type=code&scope=https://www.googleapis.com/auth/calendar%20https://www.googleapis.com/auth/userinfo.email&access_type=offline&approval_prompt=force", StatusCodes.PermanentRedirect)
          }
          else {
            // NOTE: For local testing, use below
            redirect("https://accounts.google.com/o/oauth2/auth?client_id=643564087741-h7tlsge34q3skhp6vr70lolrcf42du6d.apps.googleusercontent.com&redirect_uri=http://localhost:8080/Callback&response_type=code&scope=https://www.googleapis.com/auth/calendar%20https://www.googleapis.com/auth/userinfo.email&access_type=offline&approval_prompt=force", StatusCodes.PermanentRedirect)
          }
        }
      } ~
      path("ngsignin") {
        get {
          parameters('email) { email =>
            logger.debug(email)
            UserCredentialsDB.storeEmail(email)
            complete("done")
          }
        }
      } ~
      path("Callback") {
        get {
          parameters('code) { code =>
            logger.debug(code)
            val pipeline: HttpRequest => Future[GoogleCalendarResult] = sendReceive ~> unmarshal[GoogleCalendarResult]
            var codeString: String = ""

            if (!Boot.localRun) {
              codeString = "&client_id=643564087741-h7tlsge34q3skhp6vr70lolrcf42du6d.apps.googleusercontent.com&client_secret=f-LzX6NLSM7dh4WEKCaL5-c0&redirect_uri=http://axoni.co/Callback"
            }
            else {
              // NOTE: For local testing, use below
              codeString = "&client_id=643564087741-h7tlsge34q3skhp6vr70lolrcf42du6d.apps.googleusercontent.com&client_secret=f-LzX6NLSM7dh4WEKCaL5-c0&redirect_uri=http://localhost:8080/Callback"
            }
            val response: Future[GoogleCalendarResult] = pipeline(Post("https://www.googleapis.com/oauth2/v3/token/?grant_type=authorization_code&code=" + code + codeString))
            response onComplete {
              case Success(hResponse) => {
                val emailPipeline: HttpRequest => Future[HttpResponse] = sendReceive
                val emailResponse = emailPipeline(Get("https://www.googleapis.com/plus/v1/people/me?access_token=" + hResponse.access_token))
                emailResponse onComplete {
                  case Success(eResponse) => {
                    val emailString: String = eResponse.entity.data.asString
                    val jsonEmail = emailString.parseJson
                    val googleEmail = jsonEmail.asJsObject.getFields("emails")
                    googleEmail.foreach(e => {
                      (e.toString().parseJson.asInstanceOf[JsArray]).elements.foreach(v => {
                        val emailVal = v.asJsObject.getFields("value").head.toString().stripPrefix( """"""").stripSuffix( """"""").trim
                        logger.debug(emailVal)
                        co.axoni.database.UserCredentialsDB.storeAccessToken(emailVal, hResponse.access_token.stripPrefix( """"""").stripSuffix( """"""").trim, hResponse.refresh_token.stripPrefix( """"""").stripSuffix( """"""").trim)
                        // AccessTokenTimer.setTimerForAccessToken(emailVal)
                      })
                    })
                  }
                  case Failure(ex) => logger.debug(ex.toString)
                }
              }
              case Failure(ex) => logger.debug(ex.toString)
            }
            respondWithMediaType(MediaTypes.`application/json`) {
              complete(Result("Done", "ok"))
            }
          }
        }
      } ~
      path("availability") {
        get {
          parameters('email, 'startTime, 'endTime, 'timeZone) { (email, startTime, endTime, timeZone) =>
            val aToken = co.axoni.database.UserCredentialsDB.getAccessToken(email)
            if (aToken != "None") {
              onSuccess(co.axoni.processemail.MeetingCalendar.getGoogleCalendarAvailability(aToken, email, startTime, endTime, timeZone)) {
                mesg => complete(Result("ok", mesg.toString))
              }
            }
            else {
              complete("Error: User has not given Calendar access.")
            }
          }
        }
      } ~
      path("sendgrid") {
        post {
          entity(as[MultipartContent]) { formData =>
            logger.debug("formData:" + formData)
            var emailContent: Map[String, String] = Map()
            formData.parts map {
              e => {
                e.name match {
                  case Some(name) => {
                    emailContent = emailContent + (name.toString -> e.entity.data.asString)
                    //logger.debug("emailContent.size: " + emailContent.size.toString)
                    //logger.debug("formData.parts.size: " + formData.parts.size)
                    if (emailContent.size == formData.parts.size) {
                      logger.debug("Calling processReceivedEmail")
                      co.axoni.processemail.Emily.TEST_MODE = true
                      co.axoni.processemail.Emily.SUPERVISOR_MODE = true
                      Emily.debugAndExceptionMessages = ""
                      co.axoni.processemail.EmailHandler.processReceivedEmail(emailContent, Map())
                    }
                  }
                  case None => {
                    // Do nothing
                  }
                }
              }
            }
            complete("Done")
          }
        }
      } ~
      path("sendgrid-test") {
        post {
          var emailContent: Map[String, String] = Map()
          entity(as[MultipartContent]) { formData =>
            logger.debug("formData:" + formData)
            complete("Done")
          }
        }
      } ~
      path("test") {
        post {
          var emailContent: Map[String, String] = Map()
          val returnValue: List[TestOutput] = List()
          entity(as[TestInput]) { testData =>
            logger.debug("testData:" + testData)
            emailContent = emailContent + ("headers" -> testData.headers)
            emailContent = emailContent + ("from" -> testData.from)
            emailContent = emailContent + ("to" -> testData.to)
            emailContent = emailContent + ("cc" -> testData.cc)
            emailContent = emailContent + ("subject" -> testData.subject)
            emailContent = emailContent + ("text" -> testData.text)
            co.axoni.processemail.Emily.TEST_MODE = true
            co.axoni.processemail.Emily.SUPERVISOR_MODE = false
            complete(returnValue ++ co.axoni.processemail.EmailHandler.processReceivedEmail(emailContent,Map()))
          }
        }
      } ~
      path("cleanServerState") {
        post {
          entity(as[CleanServerState]) { cleanServerState =>
            logger.debug("CleanServerState Command: " + cleanServerState.command)
            co.axoni.processemail.Emily.cleanServerState()
            complete("Cleaned Server State")
          }
        }
      } ~
      path("testSupervisor") {
        post {
          var emailContent: Map[String, String] = Map()
          val returnValue: List[TestOutput] = List()
          entity(as[TestInput]) { testData =>
            logger.debug("testData:" + testData)
            emailContent = emailContent + ("headers" -> testData.headers)
            emailContent = emailContent + ("from" -> testData.from)
            emailContent = emailContent + ("to" -> testData.to)
            emailContent = emailContent + ("cc" -> testData.cc)
            emailContent = emailContent + ("subject" -> testData.subject)
            emailContent = emailContent + ("text" -> testData.text)
            co.axoni.processemail.Emily.TEST_MODE = true
            co.axoni.processemail.Emily.SUPERVISOR_MODE = true
            Emily.debugAndExceptionMessages = ""
            complete(returnValue ++ co.axoni.processemail.EmailHandler.processReceivedEmail(emailContent, Map()))
          }
        }
      } ~
      path("trainingBySupervisor") {
        get {
          parameters('supervisorTransactionID) { supervisorTransactionID =>
            var query = MongoDBObject()
            if (supervisorTransactionID != "") {
              query = MongoDBObject("_id" -> supervisorTransactionID)
            }
            // Return only ONE at a time for now
            // TODO: Add logic to sort transactions based on criteria (time of meeting, organizer, critical meetings, etc.) so that those will be verified first
            val DBEntriesStagedForSupervisorChecking = collectionDataStagedForSupervisorChecking.findOne(query).toList
            complete(
              DBEntriesStagedForSupervisorChecking.map(DBEntryStagedForSupervisorChecking => {
                val incomingMeetingEmail = new MeetingEmail(DBEntryStagedForSupervisorChecking("incomingMeetingEmail").asInstanceOf[BasicDBObject])
                val associatedMeetingRequest = new MeetingRequest(DBEntryStagedForSupervisorChecking("associatedMeetingRequest").asInstanceOf[BasicDBObject])
                val outgoingMeetingEmails: List[TestOutput] = this.convertToTestOutput(DBEntryStagedForSupervisorChecking("outgoingMeetingEmails").asInstanceOf[BasicDBList])
                val debugAndExceptionMessages: String = DBEntryStagedForSupervisorChecking("debugAndExceptionMessages").asInstanceOf[String]
                val supervisorTransactionID: String = DBEntryStagedForSupervisorChecking("_id").asInstanceOf[String]
                val supervisorOverrideEmailText: String = DBEntryStagedForSupervisorChecking("supervisorOverrideEmailText").asInstanceOf[String]
                DataStagedForSupervisorTraining(
                  JsonMeetingEmail(
                    incomingMeetingEmail.getHeaders(),
                    incomingMeetingEmail.getFrom,
                    incomingMeetingEmail.getTo(),
                    incomingMeetingEmail.getCC,
                    incomingMeetingEmail.getSubject(),
                    incomingMeetingEmail.getText(),
                    incomingMeetingEmail.getDatestamp(),
                    incomingMeetingEmail.getTimestamp(),
                    incomingMeetingEmail.getTimeZoneOffset(),
                    incomingMeetingEmail.emailMessageID,
                    incomingMeetingEmail.emailInReplyTo,
                    incomingMeetingEmail.emailReferences),
                  JsonMeetingRequest(associatedMeetingRequest.ID,
                    associatedMeetingRequest.confirmed,
                    associatedMeetingRequest.happened,
                    associatedMeetingRequest.meetingSubjectLine,
                    associatedMeetingRequest.meetingLocation,
                    associatedMeetingRequest.SU_Times,
                    associatedMeetingRequest.SU_Durations,
                    associatedMeetingRequest.meetingDuration,
                    associatedMeetingRequest.meetingAttendees.map(
                      meetingAttendee => JsonMeetingAttendee(
                        meetingAttendee.organizer, meetingAttendee.name,
                        meetingAttendee.emailID, meetingAttendee.meetingTimesThatWorkGroup1, meetingAttendee.meetingTimesThatWorkGroup2, meetingAttendee.confirmed,
                        meetingAttendee.waitingToHearBack, meetingAttendee.sentEmail, meetingAttendee.timeZone, meetingAttendee.timeZoneOffset, meetingAttendee.indexOfMeetingTimesThatWorkGroup1,
                        meetingAttendee.indexOfMeetingTimesThatWorkGroup2,
                        meetingAttendee.meetingEmailsSentTo.map(ms =>
                          JsonMeetingEmail(ms.getHeaders(), ms.getFrom, ms.getTo, ms.getCC, "", "", "", "", "", "", "", ms.emailReferences)),
                        meetingAttendee.meetingEmailsReceivedFrom.map(mer =>
                          JsonMeetingEmail(mer.getHeaders(), mer.getFrom, mer.getTo, mer.getCC, "", "", "", "", "", "", "", mer.emailReferences)),
                        meetingAttendee.numberOfEmailsReceivedFrom, meetingAttendee.numberOfEmailsSentTo)),
                    associatedMeetingRequest.numberOfAttendees,
                    associatedMeetingRequest.numberOfAttendeesConfirmed,
                    if (associatedMeetingRequest.mostRecentEmail != null)
                      JsonMeetingEmail(
                        associatedMeetingRequest.mostRecentEmail.getHeaders(),
                        associatedMeetingRequest.mostRecentEmail.getFrom,
                        associatedMeetingRequest.mostRecentEmail.getTo,
                        associatedMeetingRequest.mostRecentEmail.getCC,
                        associatedMeetingRequest.mostRecentEmail.getSubject(),
                        associatedMeetingRequest.mostRecentEmail.getText(),
                        associatedMeetingRequest.mostRecentEmail.getDatestamp(),
                        associatedMeetingRequest.mostRecentEmail.getTimestamp(),
                        associatedMeetingRequest.mostRecentEmail.getTimeZoneOffset(),
                        associatedMeetingRequest.mostRecentEmail.emailMessageID,
                        associatedMeetingRequest.mostRecentEmail.emailInReplyTo,
                        associatedMeetingRequest.mostRecentEmail.emailReferences)
                    else
                      JsonMeetingEmail(
                        "", "", "", "", "", "", "", "", "", "", "", List())
                    ,
                    associatedMeetingRequest.numberOfEmailsReceivedForThisRequest,
                    associatedMeetingRequest.numberOfEmailsSentForThisRequest),
                  outgoingMeetingEmails.map { outgoingMeetingEmail =>
                    TestOutput(
                      outgoingMeetingEmail.headers,
                      outgoingMeetingEmail.to,
                      outgoingMeetingEmail.from,
                      outgoingMeetingEmail.cc,
                      outgoingMeetingEmail.subject,
                      outgoingMeetingEmail.text,
                      outgoingMeetingEmail.headerReferences,
                      outgoingMeetingEmail.attachment
                    )
                  },
                  debugAndExceptionMessages,
                  supervisorTransactionID,
                  supervisorOverrideEmailText
                )
              }))
          }
        }
      } ~
      post {
        val returnValue: List[TestOutput] = List()
        entity(as[DataAfterSupervisorTraining]) { dataAfterSupervisorTraining =>
          var emailContent: Map[String, String] = Map()
          var supervisorTrainingContent: Map[String, String] = Map()
          val returnValue: List[TestOutput] = List()
          logger.debug("incomingMeetingEmail -> " + dataAfterSupervisorTraining.incomingMeetingEmail)
          logger.debug("associatedMeetingRequest -> " + dataAfterSupervisorTraining.associatedMeetingRequest)
          logger.debug("outgoingEmails -> " + dataAfterSupervisorTraining.outgoingMeetingEmails)
          emailContent = emailContent + ("headers" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailHeaders)
          emailContent = emailContent + ("from" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailFrom)
          emailContent = emailContent + ("to" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailTo)
          emailContent = emailContent + ("cc" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailCC)
          emailContent = emailContent + ("subject" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailSubject)
          emailContent = emailContent + ("text" -> dataAfterSupervisorTraining.incomingMeetingEmail.emailText)
          if (dataAfterSupervisorTraining.supervisorVerified == "false") {
            co.axoni.processemail.Emily.TEST_MODE = true
            co.axoni.processemail.Emily.SUPERVISOR_MODE = true
            Emily.debugAndExceptionMessages = ""
          }
          else {
            //Set to true if we do not want to use SendGrid while testing
            // co.axoni.processemail.Emily.TEST_MODE = true
            co.axoni.processemail.Emily.TEST_MODE = false
            co.axoni.processemail.Emily.SUPERVISOR_MODE = false
          }
          supervisorTrainingContent = supervisorTrainingContent + ("supervisorTransactionID" -> dataAfterSupervisorTraining.supervisorTransactionID)
          supervisorTrainingContent = supervisorTrainingContent + ("supervisorOverrideEmailText" -> dataAfterSupervisorTraining.supervisorOverrideEmailText)
          complete(returnValue ++ co.axoni.processemail.EmailHandler.processReceivedEmail(emailContent, supervisorTrainingContent))
        }
      } ~
      pathPrefix("css") {
        get {
          getFromResourceDirectory("css")
        }
      } ~
      pathPrefix("images") {
        get {
          getFromResourceDirectory("images")
        }
      } ~
      pathPrefix("js") {
        get {
          getFromResourceDirectory("js")
        }
      } ~
      path("testSUTime") {
        post {
          var returnValue: List[TestSUTimeOutput] = List()
          entity(as[TestSUTimeInput]) { testData =>
            logger.debug("\n\n **** In testSUTime **** \n\n")
            logger.debug("testData:" + testData)
            co.axoni.processemail.Emily.TEST_MODE = true
            //val emailTimestamp: String = testData.headers.substring(testData.headers.indexOf("Date:") + 6)
            val dateRegexp = "\\s+Date: .*\\n".r
            val emailTimestamp: String = (dateRegexp findAllIn testData.headers).mkString.trim.substring(6)
            logger.debug("Header Date & Time Sent:" + emailTimestamp)
            val referenceTimestamp: mutable.Set[String] = co.axoni.processemail.Emily.NLP.parseTime(emailTimestamp).asScala
            logger.debug("Reference Timestamp:")
            referenceTimestamp.foreach(entry => {
              logger.debug(entry)
            })
            var MeetingInfo: mutable.Buffer[MeetingTime] = Emily.NLP.getTimes(EmailHandler.stripThreadAndFullStopsFromEmailText(testData.text), referenceTimestamp.head.split("T")(0), Emily.EMILY_TRIGGER_NO).asScala
            logger.debug("Meeting Request from Organizer:")
            returnValue = List()
            MeetingInfo.foreach(entry => {
              // logger.debug(entry.time + entry.token + entry.tokenCount + entry.sentenceCount)
              // returnValue = returnValue :+ TestSUTimeOutput(entry.time + entry.token + entry.tokenCount + entry.sentenceCount)
              if (entry.isExactTime()) {
                returnValue = returnValue :+ TestSUTimeOutput(entry.time)
              }
              if (entry.isRangeTime() || entry.isUnspecifiedTime()) {
                returnValue = returnValue :+ TestSUTimeOutput("START: " + co.axoni.processemail.MeetingTimeHelperFunctions.getMeetingTimeRange(entry.time).startTime + " END: " + co.axoni.processemail.MeetingTimeHelperFunctions.getMeetingTimeRange(entry.time).endTime)
              }
            })
            complete(returnValue.distinct)
          }
        }
      } ~
      path("emails") {
        get {
          getFromResource("hill.html")
        }
      }
  }
}
