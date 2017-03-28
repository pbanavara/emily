package co.axoni.processemail

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal
import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import co.axoni.webservice._
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import spray.http.{HttpRequest, HttpResponse}
import spray.routing.HttpService
import spray.json._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import spray.http._
import spray.client.pipelining._
import akka.util.Timeout

import co.axoni.processemail.Emily._
import co.axoni.database._

class MeetingRequest {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  // Unique ID
  var ID: Int = -1
  // Once the meeting is scheduled with all attendees agreeing on time/place then it is confirmed and calendar invite(s) are sent out.
  // This can go from true -> false if someone wants to reschedule it
  var confirmed: Boolean = false

  // Once a scheduled meeting has taken place (past the time i.e. elapsed) then we mark this as happened
  var happened: Boolean = false

  var meetingSubjectLine: String = ""
  var meetingLocation: String = ""
  var SU_Times: List[String] = List()
  var SU_Durations: List[String] = List()
  var meetingDuration: String = "60"
  // default
  var meetingAttendees: List[MeetingAttendee] = List()
  var numberOfAttendees: Int = 0
  var numberOfAttendeesConfirmed: Int = 0
  var mostRecentEmail: MeetingEmail = null
  var numberOfEmailsReceivedForThisRequest: Int = 0
  var numberOfEmailsSentForThisRequest: Int = 0

  def setNumberOfAttendees(count: Int) = {
    this.numberOfAttendees = count
    for (i <- 1 to count) {
      this.meetingAttendees = this.meetingAttendees :+ new MeetingAttendee()
    }
  }

  def getNumberOfAttendees(): Int = {
    this.numberOfAttendees
  }

  def getAttendees(): Set[String] = {
    var meetingAttendees: Set[String] = Set()
    this.meetingAttendees.foreach(meetingAttendee => meetingAttendees = meetingAttendees + meetingAttendee.emailID)
    meetingAttendees
  }

  def getAttendeeNames(): List[String] = {
    var meetingAttendeeNames: List[String] = List()
    this.meetingAttendees.foreach(meetingAttendee => meetingAttendeeNames = meetingAttendeeNames :+ meetingAttendee.name)
    meetingAttendeeNames
  }

  def isAttendee(emailFrom: String): Boolean = {
    !this.meetingAttendees.filter(meetingAttendee => (meetingAttendee.emailID == emailFrom)).isEmpty
  }

  def printMeetingDetails() = {
    logger.debug("Meeting ID: " + this.ID)
    logger.debug("\nNumber of attendees: " + this.numberOfAttendees)
    for (i <- 0 to (this.numberOfAttendees - 1)) {
      meetingAttendees(i).printAttendeeDetails()
    }
    logger.debug("Meeting SU_Times: " + this.SU_Times.foreach(println))
  }

  def updateAttendeeMeetingRequest(emailID: String, meetingRequestID: Int) = {
    val existingMeetingRequests = userToMeetingRequestsMapping getOrElse(emailID, List())
    val updatedMeetingRequests = existingMeetingRequests :+ meetingRequestID
    userToMeetingRequestsMapping = userToMeetingRequestsMapping + (emailID -> updatedMeetingRequests)
  }

  def initMeetingRequest(meetingEmail: MeetingEmail) = {
    // ID = meetingRequestID
    // co.axoni.processemail.Emily.meetingRequestID += 1
    // Identify attendees
    val toList: String = meetingEmail.getTo()
    val ccList: String = meetingEmail.getCC()
    var toAttendees: List[String] = List()
    var ccAttendees: List[String] = List()
    if (toList != "") {
      toAttendees = toList.split(",").toList
    }
    if (ccList != "") {
      ccAttendees = ccList.split(",").toList
    }
    val attendees: List[String] = addMeetingAttendees(toAttendees, ccAttendees)

    this.setNumberOfAttendees(1 + attendees.size)
    logger.debug("Number of attendees: " + this.getNumberOfAttendees())
    this.meetingAttendees.head.name_$eq(meetingEmail.getFrom().split("<")(0))
    this.meetingAttendees.head.emailID_$eq(meetingEmail.getFromEmailID())
    // this.updateAttendeeMeetingRequest(this.meetingAttendees.head.emailID, ID)
    this.meetingAttendees.head.organizer_$eq(true)
    for (i <- 1 to attendees.length) {
      this.meetingAttendees(i).name_$eq(attendees(i - 1).split("<")(0))
      if (attendees(i - 1).contains("<") && attendees(i - 1).contains(">")) {
        this.meetingAttendees(i).emailID_$eq(attendees(i - 1).split("<")(1).split(">")(0).replaceAll("\\s+", ""))
      } else {
        this.meetingAttendees(i).emailID_$eq(attendees(i - 1).replaceAll("\\s+", ""))
      }
      // this.updateAttendeeMeetingRequest(this.meetingAttendees(i).emailID, ID)
    }
    this.meetingSubjectLine = meetingEmail.getSubject()
    this.numberOfEmailsReceivedForThisRequest = 1
    this.mostRecentEmail = meetingEmail
  }

  def addMeetingAttendees(toAttendees: List[String], ccAttendees: List[String]): List[String] = {
    var attendees: List[String] = List()
    for (i <- toAttendees.indices) {
      if (!toAttendees(i).contains("emily@axoni.co")) {
        attendees = attendees :+ toAttendees(i)
      }
    }
    for (i <- ccAttendees.indices) {
      if (!ccAttendees(i).contains("emily@axoni.co")) {
        attendees = attendees :+ ccAttendees(i)
      }
    }
    attendees
  }


  def isPreviouslyProposedMeetingTime(previouslyProposedMeetingTimes: List[String], newProposedMeetingTimeRangeStart: String): Boolean = {
    val newProposedMeetingDay: String = ZonedDateTime.parse(newProposedMeetingTimeRangeStart).format(DateTimeFormatter.ofPattern("EE"))
    if (previouslyProposedMeetingTimes.contains(newProposedMeetingTimeRangeStart)
      || previouslyProposedMeetingTimes.exists(ZonedDateTime.parse(_).format(DateTimeFormatter.ofPattern("EE")).contentEquals(newProposedMeetingDay))) true
    else false
  }

  def determinePotentialMeetingTimes(supervisorTrainingContent: Map[String, String]) = {
    var calendarBusyTimesStr: String = ""
    val organizer = this.meetingAttendees.head
    var meetingTimes: mutable.Buffer[MeetingTime] = Emily.NLP.getTimes(EmailHandler.stripThreadAndFullStopsFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",mostRecentEmail.getText())), mostRecentEmail.getDatestamp(), Emily.EMILY_TRIGGER_NO).asScala
    // Filter out times which we have already proposed earlier, if any
    var filteredMeetingTime: mutable.Buffer[MeetingTime] = ListBuffer()
    if (organizer.indexOfMeetingTimesThatWorkGroup1 != -1 || organizer.indexOfMeetingTimesThatWorkGroup2 != -1) {
      meetingTimes.foreach(newMeetingTimeProposed => {
        if (newMeetingTimeProposed.timeType == "TIME" || newMeetingTimeProposed.timeType == "OFFSET") {
          val timeStr: String = MeetingTimeHelperFunctions.getMeetingTimeRange(newMeetingTimeProposed.time).startTime + organizer.timeZoneOffset
          val newProposedMeetingTimeRangeStart: String = ZonedDateTime.parse(timeStr).toString
          if (!isPreviouslyProposedMeetingTime((organizer.meetingTimesThatWorkGroup1 ++ organizer.meetingTimesThatWorkGroup2), newProposedMeetingTimeRangeStart)) {
            filteredMeetingTime = filteredMeetingTime :+ newMeetingTimeProposed
          } else {
            logger.debug("Filtering -> " + newProposedMeetingTimeRangeStart)
          }
        }
      })
      meetingTimes = filteredMeetingTime
    }

    // Reinit SU times and durations and organizer times that work
    this.SU_Times = List()
    this.SU_Durations = List()
    organizer.meetingTimesThatWorkGroup1 = List()
    organizer.meetingTimesThatWorkGroup2 = List()
    organizer.indexOfMeetingTimesThatWorkGroup1 = 0
    organizer.indexOfMeetingTimesThatWorkGroup2 = 0

    meetingTimes.foreach(entry => {
      entry.timeType match {
        case "TIME" | "OFFSET" => {
          this.SU_Times = this.SU_Times :+ entry.time
          this.SU_Times = this.SU_Times.distinct
        }
        case "DURATION" => {
          this.SU_Durations = this.SU_Durations :+ entry.time
        }
      }
    })

    this.SU_Times = this.SU_Times.distinct
    this.SU_Durations = this.SU_Durations.distinct

    if (this.SU_Times.isEmpty) throw new IllegalStateException(Emily.addDebugAndExceptionMessages("No SU_Times in email body??!"))
    if (this.SU_Times.length > 2) throw new IllegalStateException(Emily.addDebugAndExceptionMessages("More than 2 SU_Times not supported yet!"))

    var suTime_count = 0
    this.SU_Times.foreach(suTime => {
      suTime_count = suTime_count + 1
      if (!MeetingTimeHelperFunctions.isExactTime(suTime)) {
        val aToken = UserCredentialsDB.getAccessToken(organizer.emailID)
        if (aToken != "None") {
          implicit val system = ActorSystem()
          import system.dispatcher

          // Below logic to handle proposed meeting time behind current clock at organizer TZ. Hence move forward past current time
          var startTimeFixed = false
          var endTimeFixed = false
          var proposedMeetingTimeRangeStart = ZonedDateTime.parse(MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).startTime + organizer.timeZoneOffset)
          var proposedMeetingTimeRangeEnd = ZonedDateTime.parse(MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).endTime + organizer.timeZoneOffset)
          val proposedMeetingTimeRangeInMinutes = ChronoUnit.MINUTES.between(proposedMeetingTimeRangeStart, proposedMeetingTimeRangeEnd)
          while (proposedMeetingTimeRangeStart.isBefore(ZonedDateTime.now(ZoneId.of(organizer.timeZoneOffset)))) {
            proposedMeetingTimeRangeStart = proposedMeetingTimeRangeStart.plusMinutes(Emily.ALTERNATIVE_TIME_INCREMENT_MINUTES)
            startTimeFixed = true
          }
          if (proposedMeetingTimeRangeEnd.isBefore(ZonedDateTime.now(ZoneId.of(organizer.timeZoneOffset)))) {
            proposedMeetingTimeRangeEnd = proposedMeetingTimeRangeStart.plusMinutes(proposedMeetingTimeRangeInMinutes)
            endTimeFixed = true
          }
          val proposedMeetingTimeRange =
            new MeetingTimeRange(
              if (startTimeFixed) proposedMeetingTimeRangeStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) else MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).startTime + organizer.timeZoneOffset,
              if (endTimeFixed) proposedMeetingTimeRangeEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) else MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).endTime + organizer.timeZoneOffset
            )

          val response: Future[String] = MeetingCalendar.getGoogleCalendarAvailability(
            aToken, organizer.emailID,
            proposedMeetingTimeRange.startTime,
            proposedMeetingTimeRange.endTime,
            organizer.timeZone
          )
          calendarBusyTimesStr = Await.result(response, 30 seconds)
          var calendarBusyTimeRangeList: List[MeetingTimeRange] = List()
          val calendarBusyTimes: JsArray = calendarBusyTimesStr.parseJson.asInstanceOf[JsArray]
          calendarBusyTimes.elements.foreach(x => {
            print("start time: " + x.asJsObject.getFields("start").head.toString() + " end time:" + x.asJsObject.getFields("end").head.toString() + "\n")
            calendarBusyTimeRangeList = calendarBusyTimeRangeList :+ new MeetingTimeRange(x.asJsObject.getFields("start").head.toString().replace("\"", ""), x.asJsObject.getFields("end").head.toString().replace("\"", ""))
          })
          this.SU_Durations.length match {
            case 1 => meetingDuration = this.SU_Durations.head
            case 0 => print("No SU_Durations in email body. Assuming default of 60 mins")
            case _ => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Multiple SU_Durations in email body not supported yet!"))
          }
          val meetingTimesThatWork: List[MeetingTimeRange] = MeetingTimeHelperFunctions.determineMeetingTimesThatWork(proposedMeetingTimeRange, calendarBusyTimeRangeList, MeetingTimeHelperFunctions.getMeetingDuration(meetingDuration))
          meetingTimesThatWork.foreach(meetingTimeThatWorks => organizer.addMeetingTimeThatWorks(meetingTimeThatWorks.startTime, suTime_count))
        }
        else {
          // Organizer has not signed up with Google Calendar Access
          throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Not Supported Yet: Organizer has not signed up with Google Calendar Access!"))
        }
      }
      else {
        // Verify if the exact time specified is actually free
        this.SU_Durations.length match {
          case 1 => meetingDuration = this.SU_Durations.head
          case 0 => print("No SU_Durations in email body. Assuming default of 60 mins")
          case _ => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Multiple SU_Durations in email body not supported yet!"))
        }
        val aToken = UserCredentialsDB.getAccessToken(organizer.emailID)
        if (aToken != "None") {
          implicit val system = ActorSystem()
          import system.dispatcher
          val timeFmt = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'kk:mm:ss")
          val startTime = ZonedDateTime.parse(MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).startTime + organizer.timeZoneOffset).format(timeFmt).toString + organizer.timeZoneOffset
          val endTime = ZonedDateTime.parse(MeetingTimeHelperFunctions.getMeetingTimeRange(suTime).endTime + organizer.timeZoneOffset).plusMinutes(60).format(timeFmt).toString + organizer.timeZoneOffset

          // Below logic to handle proposed meeting time behind current clock at organizer TZ. Hence move forward past current time
          var startTimeFixed = false
          var endTimeFixed = false
          var proposedMeetingTimeRangeStart = ZonedDateTime.parse(startTime)
          var proposedMeetingTimeRangeEnd = ZonedDateTime.parse(endTime)
          val proposedMeetingTimeRangeInMinutes = ChronoUnit.MINUTES.between(proposedMeetingTimeRangeStart, proposedMeetingTimeRangeEnd)
          while (proposedMeetingTimeRangeStart.isBefore(ZonedDateTime.now(ZoneId.of(organizer.timeZoneOffset)))) {
            proposedMeetingTimeRangeStart = proposedMeetingTimeRangeStart.plusMinutes(Emily.ALTERNATIVE_TIME_INCREMENT_MINUTES)
            startTimeFixed = true
          }
          if (proposedMeetingTimeRangeEnd.isBefore(ZonedDateTime.now(ZoneId.of(organizer.timeZoneOffset)))) {
            proposedMeetingTimeRangeEnd = proposedMeetingTimeRangeStart.plusMinutes(proposedMeetingTimeRangeInMinutes)
            endTimeFixed = true
          }
          val proposedMeetingTimeRange =
            new MeetingTimeRange(
              if (startTimeFixed) proposedMeetingTimeRangeStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) else startTime,
              if (endTimeFixed) proposedMeetingTimeRangeEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) else endTime
            )

          val response: Future[String] = MeetingCalendar.getGoogleCalendarAvailability(
            aToken, organizer.emailID,
            proposedMeetingTimeRange.startTime,
            proposedMeetingTimeRange.endTime,
            organizer.timeZone)
          calendarBusyTimesStr = Await.result(response, 30 seconds)
          var calendarBusyTimeRangeList: List[MeetingTimeRange] = List()
          val calendarBusyTimes: JsArray = calendarBusyTimesStr.parseJson.asInstanceOf[JsArray]
          calendarBusyTimes.elements.foreach(x => {
            print("start time: " + x.asJsObject.getFields("start").head.toString() + " end time:" + x.asJsObject.getFields("end").head.toString() + "\n")
            calendarBusyTimeRangeList = calendarBusyTimeRangeList :+ new MeetingTimeRange(x.asJsObject.getFields("start").head.toString().replace("\"", ""), x.asJsObject.getFields("end").head.toString().replace("\"", ""))
          })
          val meetingTimesThatWork: List[MeetingTimeRange] = MeetingTimeHelperFunctions.determineMeetingTimesThatWork(proposedMeetingTimeRange, calendarBusyTimeRangeList, MeetingTimeHelperFunctions.getMeetingDuration(meetingDuration))
          meetingTimesThatWork.foreach(meetingTimeThatWorks => organizer.addMeetingTimeThatWorks(meetingTimeThatWorks.startTime, suTime_count))
        }
      }
    })
    if (organizer.numberOfMeetingTimesThatWorkGroup1() == 0 && organizer.numberOfMeetingTimesThatWorkGroup2() == 0) {
      throw new IllegalStateException(Emily.addDebugAndExceptionMessages("No meeting times work for organizer on proposed dateTime. Send email to organizer requesting for alternative dateTimes"))
    }
  }


  def nextSteps(supervisorTrainingContent: Map[String, String]): List[TestOutput] = {
    val organizer = this.meetingAttendees.head
    var returnVal: List[TestOutput] = List()
    // This method updates the state of the MeetingRequest object based on the mostRecentEmail received
    if (this.happened) return returnVal
    if (this.confirmed) {
      // Check if mostRecentEmail is about rescheduling
      return returnVal
    }
    val meetingAttendee: MeetingAttendee = this.emailFromAttendee(mostRecentEmail)
    meetingAttendee.addReceivedEmail(mostRecentEmail)
    if (meetingAttendee.organizer) {
      if (meetingAttendee.meetingTimesThatWorkGroup1.isEmpty && this.numberOfEmailsReceivedForThisRequest == 1) {
        logger.debug("First email from Organizer: New Meeting Request")
      }
      else {
        logger.debug("Email from Organizer on an existing meeting request. Reschedule? Cancel?")
      }
      this.determinePotentialMeetingTimes(supervisorTrainingContent)
      logger.debug("Meeting Request from Organizer:")
      // Organizer is assumed confirmed for proposed dates
      this.numberOfAttendeesConfirmed = 1
      // Send email to other attendees with proposed times
      for (i <- 0 to (this.numberOfAttendees - 1)) {
        val meetingAttendee = this.meetingAttendees(i)
        if (!meetingAttendee.organizer) {
          val emailText: StringBuffer = EmailHandler.draftRequestEmail(meetingAttendee, organizer, this.meetingDuration)
          logger.debug(emailText.toString)
          returnVal = returnVal :+ EmailHandler.sendEmail(this.meetingAttendees(i).emailID, "Re: " + mostRecentEmail.getSubject(), emailText.toString, mostRecentEmail.emailReferences :+ mostRecentEmail.emailMessageID)
          this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
          this.meetingAttendees(i).sentEmail_$eq(true)
          this.meetingAttendees(i).waitingToHearBack_$eq(true)
        }
      }
    }
    else {
      // Attendee
      if (!meetingAttendee.confirmed) {
        if (meetingAttendee.sentEmail) {
          if (meetingAttendee.waitingToHearBack) {
            if (isEmailConfirmingTime(mostRecentEmail,supervisorTrainingContent)) { // Confirmed
              val group = MeetingTimeHelperFunctions.determineConfirmedGroupOfMeetingTimesThatWork(this, mostRecentEmail, supervisorTrainingContent)
              meetingAttendee.confirmed_$eq(true)
              meetingAttendee.waitingToHearBack_$eq(false)
              this.numberOfAttendeesConfirmed = this.numberOfAttendeesConfirmed + 1
              meetingAttendee.addMeetingTimeThatWorksAndIncrementIndex(organizer.getLatestMeetingTimeThatWorks(group), group)
              val emailSubject: StringBuffer = new StringBuffer()
              emailSubject.append("Re: " + mostRecentEmail.getSubject())
              if (this.numberOfAttendeesConfirmed == this.numberOfAttendees) { // All Confirmed
                // All attendees confirmed
                // Check if all confirms are for same time. Else, send alternatives from organizer's original request

                if (!this.allAttendeesConfirmedSameTime()) { // Resend invites from alternatives in organizer's original request
                  // throw new IllegalStateException("All confirms are NOT for same time. Sending alternatives from organizer's original request")
                  logger.debug("All confirms are NOT for same time. Sending alternatives from organizer's original request")
                  if (organizer.numberOfMeetingTimesThatWorkGroup1() > organizer.indexOfMeetingTimesThatWorkGroup1) organizer.indexOfMeetingTimesThatWorkGroup1 = organizer.indexOfMeetingTimesThatWorkGroup1 + 1
                  if (organizer.numberOfMeetingTimesThatWorkGroup2() > organizer.indexOfMeetingTimesThatWorkGroup2) organizer.indexOfMeetingTimesThatWorkGroup2 = organizer.indexOfMeetingTimesThatWorkGroup2 + 1
                  this.numberOfAttendeesConfirmed = 1 // Organizer
                  // Send email to other attendees with proposed times
                  for (i <- 0 to (this.numberOfAttendees - 1)) {
                    val meetingAttendee = this.meetingAttendees(i)
                    if (!meetingAttendee.organizer) {
                      meetingAttendee.confirmed_$eq(false)
                      val emailSubject = new StringBuffer()
                      emailSubject.append("Re: " + mostRecentEmail.getSubject())
                      val emailText: StringBuffer = EmailHandler.draftProposeAlternativeTime(this, meetingAttendee, organizer, this.meetingDuration)
                      logger.debug(emailText.toString)
                      returnVal = returnVal :+ EmailHandler.sendEmail(this.meetingAttendees(i).emailID, emailSubject.toString, emailText.toString, mostRecentEmail.emailReferences :+ mostRecentEmail.emailMessageID)
                      this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
                      this.meetingAttendees(i).sentEmail_$eq(true)
                      this.meetingAttendees(i).waitingToHearBack_$eq(true)
                    }
                  }
                }
                else { // Send out calendar invite to all attendees and mark meeting as confirmed
                  // Added by Pradeep for creating calendar entry
                  //MeetingCalendar.addCalendarEntryForOrganizer(ICSMeetingTimes(0), ICSMeetingTimes(1), this.meetingSubjectLine,
                  //this)
                  //End : Pradeep addition
                  val ICSMeetingTimes = MeetingTimeHelperFunctions.formatTimeAndDurationForICS(organizer.getLatestMeetingTimeThatWorks(group), this.meetingDuration, organizer.timeZoneOffset, mostRecentEmail.getTimestamp())
                  val ICSFilePath: String = MeetingCalendar.createICSFileRequest(this, ICSMeetingTimes(2), "Meeting",
                    ICSMeetingTimes(0), ICSMeetingTimes(1), "Office of " + organizer.name)
                  if (ICSFilePath == null) {
                    logger.debug("ICS File Error!!")
                  } else {
                    for (i <- 0 to (this.numberOfAttendees - 1)) {
                      val meetingAttendee = this.meetingAttendees(i)
                      val emailSubject = new StringBuffer()
                      emailSubject.append("Re: " + mostRecentEmail.getSubject())
                      val emailText = EmailHandler.draftCalendarInviteEmail(meetingAttendee, organizer)
                      logger.debug(emailText.toString)
                      returnVal = returnVal :+ EmailHandler.sendEmailWithAttachment(meetingAttendee.emailID, emailSubject.toString, emailText.toString, ICSFilePath)
                      this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
                    }
                  }
                  this.confirmed = true
                }
              }
              else { // Not All Confirmed
                // Confirmed but waiting for other attendees
                val emailText: StringBuffer = EmailHandler.draftAckConfirmEmail(meetingAttendee, organizer)
                logger.debug(emailText.toString)
                returnVal = returnVal :+ EmailHandler.sendEmail(meetingAttendee.emailID, emailSubject.toString, emailText.toString, mostRecentEmail.emailReferences :+ mostRecentEmail.emailMessageID)
                this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
              }
            }
            else { // Not Confirmed. Propose alternative(s)
              if (mostRecentEmail.hasProposedAlternativeTimes(supervisorTrainingContent)) { // Alternatives from participant's proposal
                logger.debug("Attendee has proposed alternative times.")
                determinePotentialMeetingTimes(supervisorTrainingContent)
              }
              else { // Alternatives from organizer's original request
                if (organizer.numberOfMeetingTimesThatWorkGroup1() > organizer.indexOfMeetingTimesThatWorkGroup1) organizer.indexOfMeetingTimesThatWorkGroup1 = organizer.indexOfMeetingTimesThatWorkGroup1 + 1
                if (organizer.numberOfMeetingTimesThatWorkGroup2() > organizer.indexOfMeetingTimesThatWorkGroup2) organizer.indexOfMeetingTimesThatWorkGroup2 = organizer.indexOfMeetingTimesThatWorkGroup2 + 1
              }
              if ((organizer.numberOfMeetingTimesThatWorkGroup1() == 0 && organizer.numberOfMeetingTimesThatWorkGroup2() == 0)
                || (organizer.numberOfMeetingTimesThatWorkGroup1() == organizer.indexOfMeetingTimesThatWorkGroup1 && organizer.numberOfMeetingTimesThatWorkGroup2() == organizer.indexOfMeetingTimesThatWorkGroup2)) {
                val emailSubject = new StringBuffer()
                emailSubject.append("Re: " + mostRecentEmail.getSubject())
                val emailText: StringBuffer = EmailHandler.draftOrganizerEmailNoTimesWork(organizer)
                returnVal = returnVal :+ EmailHandler.sendEmail(this.meetingAttendees.head.emailID, emailSubject.toString, emailText.toString, mostRecentEmail.emailReferences :+ mostRecentEmail.emailMessageID)
                this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
                this.meetingAttendees.head.sentEmail_$eq(true)
                this.meetingAttendees.head.waitingToHearBack_$eq(true)
              }
              else { // More times that work exist
                this.numberOfAttendeesConfirmed = 1 // Organizer
                // Send email to other attendees with proposed times
                for (i <- 0 to (this.numberOfAttendees - 1)) {
                  val meetingAttendee = this.meetingAttendees(i)
                  if (!meetingAttendee.organizer) {
                    meetingAttendee.confirmed_$eq(false)
                    val emailSubject = new StringBuffer()
                    emailSubject.append("Re: " + mostRecentEmail.getSubject())
                    val emailText: StringBuffer = EmailHandler.draftProposeAlternativeTime(this, meetingAttendee, organizer, this.meetingDuration)
                    logger.debug(emailText.toString)
                    returnVal = returnVal :+ EmailHandler.sendEmail(this.meetingAttendees(i).emailID, emailSubject.toString, emailText.toString, mostRecentEmail.emailReferences :+ mostRecentEmail.emailMessageID)
                    this.numberOfEmailsSentForThisRequest = this.numberOfEmailsSentForThisRequest + 1
                    this.meetingAttendees(i).sentEmail_$eq(true)
                    this.meetingAttendees(i).waitingToHearBack_$eq(true)
                  }
                }
              }
            }
          }
          else throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Unsupported! Email from attendee on whom we are not waiting to hear back. Reschedule? Cancel?"))
        }
        else throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Unsupported! Email from attendee to whom we have not send email??"))
      }
      else throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Unsupported! Email from attendee who has already confirmed. Reschedule? Cancel?"))
    }
    returnVal
  }

  private def isEmailConfirmingTime(mostRecentEmail: MeetingEmail, supervisorTrainingContent: Map[String, String]): Boolean = {
    // Classify email intent as confirming participation for proposed time and loc
    val confirm: Boolean = ClassifyEmail.classifyEmails(EmailHandler.stripThreadFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",mostRecentEmail.getText())))
    logger.debug("Email confirm?: " + confirm)
    confirm
  }

  private def emailFromAttendee(email: MeetingEmail): MeetingAttendee = {
    for (i <- 0 to (this.numberOfAttendees - 1)) {
      logger.debug(i + ": " + this.meetingAttendees(i).emailID)
      if (this.meetingAttendees(i).emailID.equals(email.getFromEmailID())) {
        logger.debug("Found attendee")
        return this.meetingAttendees(i)
      }
    }
    null
  }

  def allAttendeesConfirmedSameTime(): Boolean = {
    val organizer = this.meetingAttendees.head
    for (group <- 1 to 2) {
      if (!this.meetingAttendees.exists(_.getLatestMeetingTimeThatWorks(group) != organizer.getLatestMeetingTimeThatWorks(group))) return true
    }
    false
  }

  def this(DBmeetingRequest: BasicDBObject) = {
    this
    this.ID = DBmeetingRequest.get("ID").asInstanceOf[Int]
    this.confirmed = DBmeetingRequest.get("confirmed").asInstanceOf[Boolean]
    this.happened = DBmeetingRequest.get("happened").asInstanceOf[Boolean]
    this.meetingSubjectLine = DBmeetingRequest.get("meetingSubjectLine").asInstanceOf[String]
    this.meetingLocation = DBmeetingRequest.get("meetingLocation").asInstanceOf[String]
    this.SU_Times = DBmeetingRequest.get("SU_Times").asInstanceOf[BasicDBList].toList.asInstanceOf[List[String]]
    this.SU_Durations = DBmeetingRequest.get("SU_Durations").asInstanceOf[BasicDBList].toList.asInstanceOf[List[String]]
    this.meetingDuration = DBmeetingRequest.get("meetingDuration").asInstanceOf[String]

    val DBListOfMeetingAttendees: List[Any] = DBmeetingRequest.get("meetingAttendees").asInstanceOf[BasicDBList].toList
    DBListOfMeetingAttendees.foreach(DBmeetingAttendee => {
     val meetingAttendee = new MeetingAttendee(DBmeetingAttendee.asInstanceOf[BasicDBObject])
      this.meetingAttendees = this.meetingAttendees :+ meetingAttendee
    })
    this.numberOfAttendees = DBmeetingRequest.get("numberOfAttendees").asInstanceOf[Int]
    this.numberOfAttendeesConfirmed = DBmeetingRequest.get("numberOfAttendeesConfirmed").asInstanceOf[Int]
    val DBmeetingEmail = DBmeetingRequest.get("mostRecentEmail")
    this.mostRecentEmail = new MeetingEmail(DBmeetingEmail.asInstanceOf[BasicDBObject])
    this.numberOfEmailsReceivedForThisRequest = DBmeetingRequest.get("numberOfEmailsReceivedForThisRequest").asInstanceOf[Int]
    this.numberOfEmailsSentForThisRequest = DBmeetingRequest.get("numberOfEmailsSentForThisRequest").asInstanceOf[Int]

  }
}
