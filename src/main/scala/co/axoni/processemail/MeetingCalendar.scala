package co.axoni.processemail

import java.io.{BufferedWriter, IOException, FileWriter, File}
import java.time.{LocalDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import co.axoni.database.UserCredentialsDB
import co.axoni.webservice._
import com.typesafe.scalalogging.Logger
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import spray.http.{HttpResponse, HttpRequest}
import spray.routing.HttpService

import scala.concurrent.Future
import spray.json._
import spray.client.pipelining._

import spray.client.pipelining._
import co.axoni.webservice.EmailJsonSupport._

/**
  * Created by rajeevgopalakrishna on 12/14/15.
  */


object MeetingCalendar {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  private val version = "VERSION:2.0\r\n"
  private val prodid = "PRODID://emily// EN\r\n"
  private val calBegin = "BEGIN:VCALENDAR\r\n"
  private val calEnd = "END:VCALENDAR\r\n"
  private val eventBegin = "BEGIN:VEVENT\r\n"
  private val eventEnd = "END:VEVENT\r\n"
  private val methodRequest = "METHOD:REQUEST\r\n"
  private val statusConfirmed = "STATUS:CONFIRMED\r\n"

  def addCalendarEntryForOrganizer(meetingStartTime: String, meetingEndTime: String, meetingTitle: String,
                                   meetingRequest:MeetingRequest) = {
    logger.debug("Adding calendar entry ")
    val organizerEmail = meetingRequest.meetingAttendees.head.emailID
    val accessToken = UserCredentialsDB.getAccessToken(organizerEmail)
    //val api_key = "AIzaSyBA0_mFYQy89Usya6xLIDsd1xzjxt2FAUo"
    implicit val system = ActorSystem()
    import system.dispatcher
    val availPipeline: HttpRequest => Future[HttpResponse] = sendReceive
    //val attendess : List[GoogleCalendarAttendee] = List()
    //val newAttendees = attendess :+ GoogleCalendarAttendee("pradeep_bs@yahoo.com")
    val availResponse: Future[HttpResponse] = availPipeline(Post(
      "https://www.googleapis.com/calendar/v3/calendars/" +
        organizerEmail+
        "/events?sendNotifications=true&supportsAttachments=true&fields=attendees%2Csummary&access_token=" + accessToken,
      GoogleCalendarEntry(GoogleCalendarTime(meetingStartTime), GoogleCalendarTime(meetingEndTime), meetingTitle,
        meetingRequest.meetingAttendees.map(b => GoogleCalendarAttendee(b.emailID)))))
    availResponse map {
      case HttpResponse(e, f, g, h) =>
        logger.debug(f.data.asString)
    }
  }

  def getGoogleCalendarAvailability(aToken: String, email: String, startTime: String, endTime: String, timeZone: String): Future[String] = {
    logger.debug("Checking Google Calendar availability for: " + email + " from: " + startTime + " to: " + endTime)
    implicit val system = ActorSystem()
    import system.dispatcher
    val availPipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val eId: EmailIds = EmailIds(email)
    var emails: List[EmailIds] = List(eId)
    val availResponse: Future[HttpResponse] = availPipeline(Post("https://www.googleapis.com/calendar/v3/freeBusy?access_token=" + aToken,
      GoogleCalendarAvailability(startTime, endTime, timeZone, emails)))
    availResponse map {
      case HttpResponse(e, f, g, h) =>
        logger.debug(f.data.asString)
        f.data.asString.parseJson.asJsObject.getFields("calendars").head.asJsObject.getFields(email).head.asJsObject.getFields("busy").head.toString()
    }
  }

  def createICSFileRequest(meetingRequest: MeetingRequest, createTime: String, meetingTitle: String, meetingStartTimeUTC: String, meetingEndTimeUTC: String, meetingLocation: String): String = {
    val organizer = meetingRequest.meetingAttendees.head
    var builder: StringBuilder = new StringBuilder()
    builder.append(meetingTitle + " Invite.ics")

    val meetingDetails: String =
      "UID:emily@axoni.co" + "\n" +
        "DTSTAMP:" + createTime + "\n" +
        "ORGANIZER;CN=" + organizer.name + ":MAILTO:" + organizer.emailID + "\n" +
        "DTSTART:" + meetingStartTimeUTC + "\n" +
        "DTEND:" + meetingEndTimeUTC + "\n" +
        "LOCATION:" + meetingLocation + "\n"

    var attendeeDetails: String = ""
    meetingRequest.meetingAttendees.foreach(attendee =>
      attendeeDetails = attendeeDetails.concat("ATTENDEE;ROLE=REQ-PARTICIPANT;CN="
        + attendee.name.replaceAll("\"","") // Attendees where name = emailID seem to have quotes in names which messes up iCal format
        + ";PARTSTAT=NEEDS-ACTION;RSVP=TRUE"
        + ":MAILTO:"
        + attendee.emailID
      + "\n")
    )

    try {
      val file: File = new File(builder.toString())
      // if file doesnt exists, then create it
      if (!file.exists()) {
        file.createNewFile()
      }
      var fw: FileWriter = null
      try {
        fw = new FileWriter(file.getAbsoluteFile())
      } catch {
        case ioe: Throwable => ioe.printStackTrace()
      }
      val bw: BufferedWriter = new BufferedWriter(fw)
      bw.write(calBegin)
      bw.write(version)
      bw.write(prodid)
      bw.write(methodRequest)
      bw.write(statusConfirmed)
      bw.write(eventBegin)
      bw.write(meetingDetails)
      bw.write(attendeeDetails)
      bw.write("SUMMARY:" + meetingTitle + " : " + meetingRequest.getAttendeeNames().mkString(" and ") + "\n")
      bw.write(eventEnd)
      bw.write(calEnd)
      bw.close()
      return file.getAbsolutePath()
    } catch {
      case ioe: Throwable => ioe.printStackTrace()
    }
    return null
  }
}
