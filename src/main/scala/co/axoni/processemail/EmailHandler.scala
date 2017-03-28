package co.axoni.processemail

import java.io.File

import co.axoni.database._
import co.axoni.webservice.{JsonMeetingAttendee, JsonMeetingEmail, JsonMeetingRequest, TestOutput}
import com.sendgrid._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json._
import co.axoni.webservice.EmailJsonSupport._


import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object EmailHandler {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  val testHeader = "Date: Fri, 12 Feb 2016 04:45:24 +0000"
  //case class MeetingMap(meetingEmail:MeetingEmail, meetingRequest: MeetingRequest)
  // private val NLP:StanfordCoreNlp = StanfordCoreNlp.getInstance()

  def draftRequestEmail(meetingAttendee: MeetingAttendee, organizer: MeetingAttendee, meetingDuration: String): StringBuffer = {
    val emailText: StringBuffer = new StringBuffer()
    emailText.append("<br>Hello " + meetingAttendee.name + ".<br>")
    emailText.append("<br>")
    emailText.append(organizer.name + " has proposed a meeting with you as an attendee. Please let me know if ")
    if (organizer.numberOfMeetingTimesThatWorkGroup1() > organizer.indexOfMeetingTimesThatWorkGroup1) {
      emailText.append(MeetingTimeHelperFunctions.formatTimeAndDurationNew(organizer.getLatestMeetingTimeThatWorks(1), meetingDuration))
    }
    if (organizer.numberOfMeetingTimesThatWorkGroup2() > organizer.indexOfMeetingTimesThatWorkGroup2) {
      emailText.append(" or " + MeetingTimeHelperFunctions.formatTimeAndDurationNew(organizer.getLatestMeetingTimeThatWorks(2), meetingDuration))
    }
    emailText.append(" works.")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
      + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(mostRecentEmailFromAttendee.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }

  def draftAckConfirmEmail(meetingAttendee: MeetingAttendee, organizer: MeetingAttendee): StringBuffer = {
    val emailText: StringBuffer = new StringBuffer()
    emailText.append("<br>Hello " + meetingAttendee.name + ".<br>")
    emailText.append("<br>")
    emailText.append("Thanks for confirming participation. I will send out a calendar invite shortly.")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      //emailText.append("On Sat, Feb 13, 2016 at 8:35 PM, sanjay arora <sanjay500@hotmail.com> wrote:")
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
        + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(meetingAttendee.meetingEmailsReceivedFrom.last.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }

  def draftCalendarInviteEmail(meetingAttendee: MeetingAttendee, organizer: MeetingAttendee): StringBuffer = {
    val emailText = new StringBuffer()
    emailText.append("<br>Hello " + meetingAttendee.name + ".<br>")
    emailText.append("<br>")
    emailText.append("Attached is the calendar invite. Have a great day!")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
        + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(meetingAttendee.meetingEmailsReceivedFrom.last.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }


  def draftAckNonConfirmEmail(meetingAttendee: MeetingAttendee, organizer: MeetingAttendee): StringBuffer = {
    val emailText: StringBuffer = new StringBuffer()
    emailText.append("<br>Hello " + meetingAttendee.name + ".<br>")
    emailText.append("<br>")
    emailText.append("Not a problem. I will get back with another option.")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
        + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(meetingAttendee.meetingEmailsReceivedFrom.last.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }

  def draftProposeAlternativeTime(meetingRequest: MeetingRequest, meetingAttendee: MeetingAttendee, organizer: MeetingAttendee, meetingDuration: String) = {
    val emailText: StringBuffer = new StringBuffer()
    emailText.append("<br>Hello " + meetingAttendee.name + ".<br>")
    emailText.append("<br>")
    if (meetingRequest.getNumberOfAttendees() > 2)
      emailText.append("Sorry. But the earlier proposed time doesn't work for one of the attendees. ")
    else
      emailText.append("Not a problem. " )
    emailText.append("Please let me know if ")
    if (organizer.numberOfMeetingTimesThatWorkGroup1() > organizer.indexOfMeetingTimesThatWorkGroup1) {
      emailText.append(MeetingTimeHelperFunctions.formatTimeAndDurationNew(organizer.getLatestMeetingTimeThatWorks(1), meetingDuration))
    }
    if (organizer.numberOfMeetingTimesThatWorkGroup2() > organizer.indexOfMeetingTimesThatWorkGroup2) {
      emailText.append(" or " + MeetingTimeHelperFunctions.formatTimeAndDurationNew(organizer.getLatestMeetingTimeThatWorks(2), meetingDuration))
    }
    emailText.append(" works.")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
        + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(meetingAttendee.meetingEmailsReceivedFrom.last.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }

  def draftOrganizerEmailNoTimesWork(organizer: MeetingAttendee): StringBuffer = {
    val meetingAttendee = organizer
    val emailText: StringBuffer = new StringBuffer()
    emailText.append("<br>Hello " + organizer.name + ".<br>")
    emailText.append("<br>")
    emailText.append("Looks like the proposed time does not work. Can you suggest an alternative?")
    emailText.append("<br><br>Thanks," + "<br>Emily.")
    emailText.append("<br><br>Emily | Executive Assistant to " + organizer.name)
    emailText.append("<br><br><br><br>")
    if (meetingAttendee.numberOfEmailsReceivedFrom > 0) {
      val mostRecentEmailFromAttendee = meetingAttendee.meetingEmailsReceivedFrom.last
      emailText.append("On " + MeetingTimeHelperFunctions.formatTimeForEmailThread(mostRecentEmailFromAttendee.getTimestamp()+meetingAttendee.timeZoneOffset)
        + ", " + mostRecentEmailFromAttendee.getFrom() + " <" + mostRecentEmailFromAttendee.getFromEmailID() + ">" + " wrote:")
      emailText.append(mostRecentEmailFromAttendee.getText().replaceAll("(^|\n)","<br>> "))
    }
    emailText
  }


  def sendEmail(emailTo: String, emailSubject: String, emailText: String, emailHeaderReferences: List[String]): TestOutput = {

    val sendgrid:SendGrid = new SendGrid("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")
    val email:SendGrid.Email = new SendGrid.Email()
    email.addTo(emailTo)
    // email.addCc("emily@axoni.co")
    email.setFromName("Emily")
    email.setFrom("emily@axoni.co")
    email.setSubject(emailSubject)
    email.addHeader("References", emailHeaderReferences.mkString(" "))
    email.setHtml(emailText)
    try {
      if (Emily.TEST_MODE) {
        TestOutput(testHeader,emailTo,"emily@axoni.co","",emailSubject,emailText,emailHeaderReferences.mkString(" "),"<<No Attachement>>")
      }
      else {
        logger.debug("SendGrid: Sending email to: " + emailTo)
        val response: SendGrid.Response = sendgrid.send(email)
        logger.debug("SendGrid: Response: " + response.getMessage())
        TestOutput("","","","","","","","")
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
        TestOutput("","","","","","","","")
    }
  }

  def sendEmailWithAttachment(emailTo: String, emailSubject: String, emailText: String, FilePath: String): TestOutput = {

    val sendgrid: SendGrid = new SendGrid("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")
    val email: SendGrid.Email = new SendGrid.Email()

    email.addTo(emailTo)
    // email.addCc("emily@axoni.co")
    email.setFrom("emily@axoni.co")
    email.setFromName("Emily")
    email.setSubject(emailSubject)
    email.setHtml(emailText)
    try {
      if (Emily.TEST_MODE) {
        TestOutput(testHeader, emailTo, "emily@axoni.co", "", emailSubject, emailText, "", "<<" + scala.io.Source.fromFile(FilePath).getLines().mkString + ">>")
      }
      else {
        val attachment: File = new File(FilePath)
        email.addAttachment("meeting.ics", attachment)
        logger.debug("SendGrid: Sending email to: " + emailTo)
        val response: SendGrid.Response = sendgrid.send(email)
        logger.debug("SendGrid: Response: " + response.getMessage())
        TestOutput("", "", "", "", "", "","","")
      }
    }
    catch {
      case e: Exception => e.printStackTrace()
        TestOutput("","","","","","","","")
    }
  }

  def processReceivedEmail(email: Map[String, String], supervisorTrainingContent: Map[String, String]) : List[TestOutput] = {
    var retval: List[TestOutput] = List()
    try {
        val meetingEmail: MeetingEmail = new MeetingEmail()
        email.foreach(println(_))
        meetingEmail.setHeaders(email.getOrElse("headers",""))
        logger.debug("headers:" + meetingEmail.getHeaders())

        // Extract email timestamp from header
        val dateRegexp = "\\s+Date: .*\\n".r
        val emailTimestamp: String = (dateRegexp findAllIn meetingEmail.getHeaders()).mkString.trim.substring(6)
        logger.debug("Header Date & Time Sent:" + emailTimestamp)
        logger.debug("Email TimeZoneOffset:" + emailTimestamp.split(" ")(5))
        meetingEmail.setTimeZoneOffset(emailTimestamp.split(" ")(5))
        val referenceTimestamp: mutable.Set[String] = Emily.NLP.parseTime(emailTimestamp).asScala
        referenceTimestamp.foreach(entry => {
          logger.debug(entry)
          meetingEmail.setDateAndTimestamp(entry)
          // TBD: Assuming that there might be multiple timestamps and setting it to the last one!?!?!
        })
        logger.debug("Reference Timestamp:" + meetingEmail.getDatestamp())

        // Extract email message ID from header
        val messageIDRegexp = "\\s*Message-ID:.*\\n".r
        val messageIDStr: Option[String] = messageIDRegexp findFirstIn  meetingEmail.getHeaders()
          messageIDStr match {
            case Some(messageID) => {
              meetingEmail.emailMessageID = messageID.mkString.trim.substring(12)
              logger.debug("Message-ID: " + meetingEmail.emailMessageID)
            }
            case None => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("No Message-ID detected in email header!?!?"))
          }

        // Extract in-reply-to from header
        val inReplyToRegexp = "\\s*In-Reply-To:.*\\n".r
        val inReplyToStr: Option[String] = inReplyToRegexp findFirstIn  meetingEmail.getHeaders()
        inReplyToStr match {
          case Some(inReplyTo) => {
            meetingEmail.emailInReplyTo = inReplyTo.mkString.trim.substring(12)
            logger.debug("In-ReplyTo: " + meetingEmail.emailInReplyTo)
          }
          case None => logger.debug("No In-Reply-To detected in email header.")
        }

        // Extract references from header
        val referencesRegexp = "\\s*References: (<.*>[,|\\n|\\s]*)+".r
        val referencesStr: Option[String] = referencesRegexp findFirstIn  meetingEmail.getHeaders()
        referencesStr match {
          case Some(references) => {
            meetingEmail.emailReferences = referencesStr.mkString("").replaceAll("\\n","").trim.substring(12).split(",|\\s").toList
            logger.debug("References processed: " + meetingEmail.emailReferences.mkString(" "))
          }
          case None => logger.debug("No References detected in email header.")
        }

        meetingEmail.setTo(email.getOrElse("to",""))
        logger.debug("to:" + meetingEmail.getTo())

        meetingEmail.setFrom(email.getOrElse("from",""))
        logger.debug("from:" + meetingEmail.getFrom())

        meetingEmail.setCC(email.getOrElse("cc",""))
        logger.debug("cc:" + meetingEmail.getCC())

        meetingEmail.setSubject(email.getOrElse("subject",""))
        logger.debug("subject:" + meetingEmail.getSubject())

        meetingEmail.setText(email.getOrElse("text",""))
        logger.debug("text:" + meetingEmail.getText())

        logger.debug("*** supervisorOverrideEmailText ***" + supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",""))


        retval = determineResponse(meetingEmail, supervisorTrainingContent)
    }
    catch {
      case e:Exception => {
        e.printStackTrace()
        Emily.addDebugAndExceptionMessages(e.printStackTrace().toString)
      }
        retval = List(TestOutput("","","","","","","",""))
    }
    retval :+ TestOutput("TEST-HEADER","TESTER","TESTEE","TEST-CC","NumberOfMeetingRequests",MongoOperations.collectionMeetingRequests.count().toString,"","<<No Attachement>>")
  }

  def determineResponse(meetingEmail: MeetingEmail, supervisorTrainingContent: Map[String, String]): List[TestOutput] = {
    val meetingRequest: MeetingRequest = determineMeetingRequest(meetingEmail, supervisorTrainingContent)
    if (meetingRequest != null) {
      meetingRequest.printMeetingDetails()
      try {
        val testOutput = meetingRequest.nextSteps(supervisorTrainingContent)
        if (Emily.SUPERVISOR_MODE) {
          if (supervisorTrainingContent.contains("supervisorTransactionID")) {
            val supervisorTransactionID = supervisorTrainingContent.getOrElse("supervisorTransactionID","")
            val supervisorOverrideEmailText = supervisorTrainingContent.getOrElse("supervisorOverrideEmailText","")
            MongoOperations.updateOrInsertDBInputAndExpectedOutputForSupervisorCheck(meetingEmail, meetingRequest, testOutput, "", supervisorTransactionID, supervisorOverrideEmailText)
          } else {
            val supervisorTransactionID = Emily.supervisorTransactionID.toString
            Emily.supervisorTransactionID = Emily.supervisorTransactionID + 1
            val supervisorOverrideEmailText = supervisorTrainingContent.getOrElse("supervisorOverrideEmailText","")
            MongoOperations.updateOrInsertDBInputAndExpectedOutputForSupervisorCheck(meetingEmail, meetingRequest, testOutput, "", supervisorTransactionID, supervisorOverrideEmailText)
            MongoOperations.updateOrInsertSupervisorTransaction(Emily.supervisorTransactionID)
          }
        } else {
          if (meetingRequest.ID == -1) { // New Meeting Request
            meetingRequest.ID = Emily.meetingRequestID
            Emily.meetingRequestID = Emily.meetingRequestID + 1
            for (i <- 0 to (meetingRequest.getNumberOfAttendees()-1)) {
              meetingRequest.updateAttendeeMeetingRequest(meetingRequest.meetingAttendees(i).emailID, meetingRequest.ID)
            }
          }
          MongoOperations.updateOrInsertMeetingRequest(meetingRequest)
          MongoOperations.updateOrInsertUserToMeetingRequestsMapping(Emily.userToMeetingRequestsMapping)
          MongoOperations.updateOrInsertMeetingRequestID(Emily.meetingRequestID)
          if (supervisorTrainingContent.contains("supervisorTransactionID")) {
            val supervisorTransactionID: String = supervisorTrainingContent.getOrElse("supervisorTransactionID","")
            MongoOperations.deleteSupervisorTransaction(supervisorTransactionID)
          }
        }
        testOutput
      } catch {
        case e: Exception => {
          println(e.getMessage)
          e.printStackTrace()
          Emily.addDebugAndExceptionMessages(e.printStackTrace().toString)
          if (Emily.SUPERVISOR_MODE) {
            if (supervisorTrainingContent.contains("supervisorTransactionID")) {
              val supervisorTransactionID = supervisorTrainingContent.getOrElse("supervisorTransactionID","")
              val supervisorOverrideEmailText = supervisorTrainingContent.getOrElse("supervisorOverrideEmailText","")
              MongoOperations.updateOrInsertDBInputAndExpectedOutputForSupervisorCheck(meetingEmail, meetingRequest, List(TestOutput("", "", "", "", "", "", "", "")), Emily.debugAndExceptionMessages, supervisorTransactionID, supervisorOverrideEmailText)
            } else {
              val supervisorTransactionID = Emily.supervisorTransactionID.toString
              Emily.supervisorTransactionID = Emily.supervisorTransactionID + 1
              val supervisorOverrideEmailText = supervisorTrainingContent.getOrElse("supervisorOverrideEmailText","")
              MongoOperations.updateOrInsertDBInputAndExpectedOutputForSupervisorCheck(meetingEmail, meetingRequest, List(TestOutput("", "", "", "", "", "", "", "")), Emily.debugAndExceptionMessages, supervisorTransactionID, supervisorOverrideEmailText)
              MongoOperations.updateOrInsertSupervisorTransaction(Emily.supervisorTransactionID)
            }
          }
          List(TestOutput("", "", "", "", "", "", "", ""))
        }
      }
    }
    else {
      logger.debug("Error? Received Email NOT related to meetings?")
      List(TestOutput("","","","","","","",""))
    }
  }

  def determineMeetingRequest(meetingEmail: MeetingEmail, supervisorTrainingContent: Map[String, String]): MeetingRequest = {
    val existingMeetingRequests:List[MeetingRequest] = MongoOperations.relatedToExistingMeetingRequestsDB(meetingEmail)
    if (createNewMeetingRequest(existingMeetingRequests, meetingEmail, supervisorTrainingContent, meetingEmail.getEmailParticipants())) { // New Meeting Request
      logger.debug("**New Meeting Request**")
      val newMeetingRequest:MeetingRequest = new MeetingRequest()
      newMeetingRequest.initMeetingRequest(meetingEmail)
      logger.debug("Adding Meeting to List")
      //Emily.addToMeetingRequests(newMeetingRequest)
      return newMeetingRequest
    }
    else { // Return the first one who is the "best match"
      logger.debug("**Existing Meeting Request**. Details:")
      existingMeetingRequests.head.printMeetingDetails()
      existingMeetingRequests.head.mostRecentEmail = meetingEmail
      return existingMeetingRequests.head
    }
  }

  def isRegisteredUser(emailID: String): Boolean = {
    UserCredentialsDB.getAccessToken(emailID) != "None"
  }

  def differentTimes(existingMeetingRequests: List[MeetingRequest], email: MeetingEmail, supervisorTrainingContent: Map[String, String]): Boolean = {
    var SU_Times: Set[String] = Set()
    val MeetingTimes: mutable.Buffer[MeetingTime] = Emily.NLP.getTimes(EmailHandler.stripThreadAndFullStopsFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",email.getText())), email.getDatestamp(), Emily.EMILY_TRIGGER_NO).asScala
    MeetingTimes.foreach(entry => {
      entry.timeType match {
        case "TIME" | "OFFSET" => SU_Times = SU_Times + entry.time
        case _ =>
      }
    })
    !existingMeetingRequests.exists(_.SU_Times.toSet.equals(SU_Times))
  }

  def differentParticipants(existingMeetingRequests: List[MeetingRequest], emailParticipants: Set[String]): Boolean = {
    //existingMeetingRequests.filter(meetingRequest => meetingRequest.getAttendees().equals(emailParticipants)).isEmpty
    !existingMeetingRequests.exists(_.getAttendees().equals(emailParticipants))
  }

  def createNewMeetingRequest(existingMeetingRequests: List[MeetingRequest], email: MeetingEmail, supervisorTrainingContent: Map[String, String], emailParticipants: Set[String]): Boolean = {
    if (existingMeetingRequests.isEmpty) true
    // if (this.isRegisteredUser(email.getFromEmailID()) && existingMeetingRequests.isEmpty) true
    //  || differentParticipants(existingMeetingRequests, emailParticipants - "emily@axoni.co"))
    // Different times may be because of rescheduling of the same meeting request
    //  || differentTimes(existingMeetingRequests, email, supervisorTrainingContent))) true
    else false
  }

  def removeOldEmailText(inputStr: String): String = {
    val datePattern = """((.*>.*)|(.*(On).*(wrote:)$)|(.*(Date):.*(\+\d\d\d\d)?$)|(.*(From):.*​))"""
    val reg = datePattern.r
    inputStr.split(datePattern, -1).head
  }

  def stripThreadFromEmailText(emailText: String) : String = {
    val datePattern = """(.*(On).*(wrote:))"""
    val strippedText = emailText.split(datePattern, -1).head
    logger.debug("strippedEmailText: ")
    logger.debug(strippedText)
    strippedText
  }

  def stripThreadAndFullStopsFromEmailText(emailText: String) : String = {
    val datePattern = """(.*(On).*(wrote:))"""
    val strippedText = emailText.split(datePattern, -1).head
    logger.debug("strippedEmailText: ")
    logger.debug(strippedText)
    strippedText.replaceAll("\\.","")
  }

  def buggyStripThreadFromEmailText(emailText: String) : String = {
    val emailLines = emailText.split("<br>")
    val strippedText = emailLines.takeWhile(line => !line.matches(".*On.*wrote:")).mkString("\n")
    logger.debug("strippedEmailText: ")
    logger.debug(strippedText)
    strippedText
  }
}

