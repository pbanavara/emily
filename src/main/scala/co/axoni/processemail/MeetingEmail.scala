package co.axoni.processemail

import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.JavaConverters._

class MeetingEmail {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  var emailHeaders: String = ""
  var emailFrom: String = ""
  var emailTo: String = ""
  var emailCC: String = ""
  var emailSubject: String = ""
  var emailText: String = ""
  var emailDatestamp: String = ""
  var emailTimestamp: String = ""
  var emailTimeZoneOffset: String = ""
  var emailMessageID: String = ""
  var emailInReplyTo: String = ""
  var emailReferences: List[String] = List()

  def setHeaders(headers: String) = {
    this.emailHeaders = headers
  }

  def getHeaders(): String = {
    this.emailHeaders
  }

  def setFrom(from: String) = {
    this.emailFrom = from
  }

  def getFrom(): String = {
    this.emailFrom
  }

  def getFromEmailID(): String = {
    if (this.getFrom().contains("<") && this.getFrom().contains(">")) {
      this.getFrom().split("<")(1).split(">")(0).replaceAll("\\s+", "")
    }
    else {
      this.getFrom()
    }
  }

  def setTo(to: String) = {
    this.emailTo = to
  }

  def getTo(): String = {
    this.emailTo
  }

  def setCC(cc: String) = {
    this.emailCC = cc
  }

  def getCC(): String = {
    this.emailCC
  }

  def setSubject(subject: String) = {
    this.emailSubject = subject
  }

  def getSubject(): String = {
    this.emailSubject
  }

  def setText(text: String) = {
    this.emailText = text
  }

  def getText(): String = {
    this.emailText
  }

  def setDateAndTimestamp(timestamp: String) = {
    this.emailDatestamp = timestamp.split("T")(0)
    // Ignore the time part of the timestamp and retain only the date
    // Keeping the time part messes up SU Time relative days: tomorrow/today/day after etc.
    this.emailTimestamp = timestamp
  }

  def getDatestamp(): String = {
    this.emailDatestamp
  }

  def getTimestamp(): String = {
    this.emailTimestamp
  }

  def setTimeZoneOffset(timezoneOffset: String) = {
    this.emailTimeZoneOffset = timezoneOffset
  }

  def getTimeZoneOffset(): String = {
    this.emailTimeZoneOffset
  }

  def getEmailParticipants(): Set[String] = {
    val toList = this.getTo()
    val ccList = this.getCC()
    var toParticipants: List[String] = List()
    var ccParticipants: List[String] = List()

    if (toList != "") {
      toParticipants = toList.split(",").toList
      toParticipants.map(entry =>
        if (entry.contains("<") && entry.contains(">")) {
          entry.split("<")(1).split(">")(0).replaceAll("\\s+", "")
        } else {
          entry.replaceAll("\\s+", "")
        }
      )
      toParticipants.foreach(logger.debug(_))
    }

    if (ccList != "") {
      ccParticipants = ccList.split(",").toList
      ccParticipants.map(entry =>
        if (entry.contains("<") && entry.contains(">")) {
          entry.split("<")(1).split(">")(0).replaceAll("\\s+", "")
        } else {
          entry.replaceAll("\\s+", "")
        }
      )
      ccParticipants.foreach(logger.debug(_))
    }

    var fromParticipant: String = ""

    fromParticipant = this.getFrom()

    var participants = toParticipants ++ ccParticipants :+ fromParticipant
    // logger.debug("Before filtering")
    // participants.foreach(logger.debug(_))
    participants = participants.filter(_ != "emily@axoni.co").toList
    // logger.debug("After filtering")
    // participants.foreach(logger.debug(_))
    participants.toSet
  }

  def hasProposedAlternativeTimes(supervisorTrainingContent: Map[String, String]): Boolean = {
    logger.debug("Checking if attendee has proposed alternative times.")
    val meetingTimes: mutable.Buffer[MeetingTime] = Emily.NLP.getTimes(EmailHandler.stripThreadAndFullStopsFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",this.getText())), this.getDatestamp(), Emily.EMILY_TRIGGER_NO).asScala
    !meetingTimes.isEmpty
  }

  def this(DBMeetingEmail: BasicDBObject) = {
    this
    this.emailHeaders = DBMeetingEmail.get("emailHeaders").asInstanceOf[String]
    this.emailFrom = DBMeetingEmail.get("emailFrom").asInstanceOf[String]
    this.emailTo = DBMeetingEmail.get("emailTo").asInstanceOf[String]
    this.emailCC = DBMeetingEmail.get("emailCC").asInstanceOf[String]
    this.emailSubject = DBMeetingEmail.get("emailSubject").asInstanceOf[String]
    this.emailText = DBMeetingEmail.get("emailText").asInstanceOf[String]
    this.emailDatestamp = DBMeetingEmail.get("emailDatestamp").asInstanceOf[String]
    this.emailTimestamp = DBMeetingEmail.get("emailTimestamp").asInstanceOf[String]
    this.emailTimeZoneOffset = DBMeetingEmail.get("emailTimeZoneOffset").asInstanceOf[String]
    this.emailMessageID = DBMeetingEmail.get("emailMessageID").asInstanceOf[String]
    this.emailInReplyTo = DBMeetingEmail.get("emailInReplyTo").asInstanceOf[String]
    this.emailReferences = DBMeetingEmail.get("emailReferences").asInstanceOf[BasicDBList].toList.asInstanceOf[List[String]]
  }
}
