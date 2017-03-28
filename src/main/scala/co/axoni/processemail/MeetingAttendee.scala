package co.axoni.processemail

import co.axoni.database._
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

class MeetingAttendee {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
  private var _organizer: Boolean = false
  private var _name: String = ""
  private var _emailID: String = ""
  //private var _meetingTimesThatWork: List[String] = List()
  var meetingTimesThatWorkGroup1: List[String] = List()
  var meetingTimesThatWorkGroup2: List[String] = List()
  private var _confirmed: Boolean = false
  private var _waitingToHearBack: Boolean = false
  private var _sentEmail: Boolean = false
  private var _timeZone: String = "Asia/Kolkata"
  private var _timeZoneOffset: String = "+05:30"
  private var _indexOfMeetingTimesThatWorkGroup1: Int = -1
  private var _indexOfMeetingTimesThatWorkGroup2: Int = -1

  var meetingEmailsSentTo: List[MeetingEmail] = List()
  var meetingEmailsReceivedFrom:  List[MeetingEmail] = List()
  var numberOfEmailsReceivedFrom: Int = 0
  var numberOfEmailsSentTo: Int = 0

  def addReceivedEmail(meetingEmail: MeetingEmail) = {
    meetingEmailsReceivedFrom = meetingEmailsReceivedFrom :+ meetingEmail
    this.numberOfEmailsReceivedFrom = this.numberOfEmailsReceivedFrom + 1
  }

  def addSentEmail(meetingEmail: MeetingEmail) =  {
    meetingEmailsSentTo = meetingEmailsSentTo :+ meetingEmail
    this.numberOfEmailsSentTo = this.numberOfEmailsSentTo + 1
  }

  def organizer = _organizer
  def organizer_=(organizer: Boolean) = _organizer = organizer

  def name = _name
  def name_=(name: String) = _name = name

  def emailID = _emailID
  def emailID_=(emailID: String) = _emailID = emailID

  //def meetingTimesThatWork = _meetingTimesThatWork
  //def meetingTimesThatWork_=(meetingTimesThatWork: List[String]) = _meetingTimesThatWork = meetingTimesThatWork

  def indexOfMeetingTimesThatWorkGroup1 = _indexOfMeetingTimesThatWorkGroup1
  def indexOfMeetingTimesThatWorkGroup1_=(indexOfMeetingTimesThatWorkGroup1: Int) = _indexOfMeetingTimesThatWorkGroup1 = indexOfMeetingTimesThatWorkGroup1

  def indexOfMeetingTimesThatWorkGroup2 = _indexOfMeetingTimesThatWorkGroup2
  def indexOfMeetingTimesThatWorkGroup2_=(indexOfMeetingTimesThatWorkGroup2: Int) = _indexOfMeetingTimesThatWorkGroup2 = indexOfMeetingTimesThatWorkGroup2

  def numberOfMeetingTimesThatWorkGroup1() : Int = {
    //_meetingTimesThatWork.length
    meetingTimesThatWorkGroup1.length
  }

  def numberOfMeetingTimesThatWorkGroup2() : Int = {
    //_meetingTimesThatWork.length
    meetingTimesThatWorkGroup2.length
  }

  def getLatestMeetingTimeThatWorks(group: Int) : String = {
    //_meetingTimesThatWork(index)
    group match {
      case 1 =>
        if (this.indexOfMeetingTimesThatWorkGroup1 == -1)
          "None" //TBD throw new IllegalStateException("indexOfMeetingTimesThatWorkGroup1 = -1")
        else meetingTimesThatWorkGroup1(this.indexOfMeetingTimesThatWorkGroup1)
      case 2 =>
        if (this.indexOfMeetingTimesThatWorkGroup2 == -1)
          "None" //TBD throw new IllegalStateException("indexOfMeetingTimesThatWorkGroup2 = -1")
        else meetingTimesThatWorkGroup2(this.indexOfMeetingTimesThatWorkGroup2)
      case _ => new IllegalStateException(Emily.addDebugAndExceptionMessages("Unrecognized group for meetingTimesThatWork")).toString
    }
  }

  def addMeetingTimeThatWorks(time: String, group: Int) = {
    // _meetingTimesThatWork = _meetingTimesThatWork :+ time
    group match {
      case 1 => meetingTimesThatWorkGroup1 = meetingTimesThatWorkGroup1 :+ time
      case 2 => meetingTimesThatWorkGroup2 = meetingTimesThatWorkGroup2 :+ time
      case _ => new IllegalStateException(Emily.addDebugAndExceptionMessages("Unrecognized group for meetingTimesThatWork"))
    }
  }

  def addMeetingTimeThatWorksAndIncrementIndex(time: String, group: Int) = {
    // _meetingTimesThatWork = _meetingTimesThatWork :+ time
    group match {
      case 1 => {
        meetingTimesThatWorkGroup1 = meetingTimesThatWorkGroup1 :+ time
        indexOfMeetingTimesThatWorkGroup1 = indexOfMeetingTimesThatWorkGroup1 + 1
      }
      case 2 => {
        meetingTimesThatWorkGroup2 = meetingTimesThatWorkGroup2 :+ time
        indexOfMeetingTimesThatWorkGroup2 = indexOfMeetingTimesThatWorkGroup2 + 1
      }
      case _ => new IllegalStateException(Emily.addDebugAndExceptionMessages("Unrecognized group for meetingTimesThatWork"))
    }
  }


  def confirmed = _confirmed
  def confirmed_=(organizer: Boolean) = _confirmed = confirmed

  def waitingToHearBack = _waitingToHearBack
  def waitingToHearBack_=(waitingToHearBack: Boolean) = _waitingToHearBack = waitingToHearBack

  def haveCalendarAccess(): Boolean = {
    UserCredentialsDB.getAccessToken(this.emailID) != "None"
  }

  def sentEmail = _sentEmail
  def sentEmail_=(sentEmail:Boolean) = _sentEmail = sentEmail

  def timeZone = _timeZone
  def timeZone_=(timeZone: String) = _timeZone = timeZone

  def timeZoneOffset = _timeZoneOffset
  def timeZoneOffset_=(timeZoneOffset: String) = _timeZoneOffset = timeZoneOffset


  def printAttendeeDetails() = {
    logger.debug("Attendee name: " + this.name)
    logger.debug("Attendee emailID: " + this.emailID)
    logger.debug("Attendee is organizer: " + this.organizer)
  }

  def this(DBmeetingAttendee: BasicDBObject) = {
    this
    this.organizer = DBmeetingAttendee.get("organizer").asInstanceOf[Boolean]
    this.name = DBmeetingAttendee.get("name").asInstanceOf[String]
    this.emailID = DBmeetingAttendee.get("emailID").asInstanceOf[String]
    this.meetingTimesThatWorkGroup1 = DBmeetingAttendee.get("meetingTimesThatWorkGroup1").asInstanceOf[BasicDBList].toList.asInstanceOf[List[String]]
    this.meetingTimesThatWorkGroup2 = DBmeetingAttendee.get("meetingTimesThatWorkGroup2").asInstanceOf[BasicDBList].toList.asInstanceOf[List[String]]
    this.confirmed = DBmeetingAttendee.get("confirmed").asInstanceOf[Boolean]
    this.waitingToHearBack = DBmeetingAttendee.get("waitingToHearBack").asInstanceOf[Boolean]
    this.sentEmail = DBmeetingAttendee.get("sentEmail").asInstanceOf[Boolean]
    this.timeZone = DBmeetingAttendee.get("timeZone").asInstanceOf[String]
    this.timeZoneOffset = DBmeetingAttendee.get("timeZoneOffset").asInstanceOf[String]
    this.indexOfMeetingTimesThatWorkGroup1 = DBmeetingAttendee.get("indexOfMeetingTimesThatWorkGroup1").asInstanceOf[Int]
    this.indexOfMeetingTimesThatWorkGroup2 = DBmeetingAttendee.get("indexOfMeetingTimesThatWorkGroup2").asInstanceOf[Int]

    val DBListOfMeetingEmailsSentTo = DBmeetingAttendee.get("meetingEmailsSentTo").asInstanceOf[BasicDBList].toList
    DBListOfMeetingEmailsSentTo.foreach(DBmeetingEmailSentTo => {
      val meetingEmailSentTo = new MeetingEmail(DBmeetingEmailSentTo.asInstanceOf[BasicDBObject])
      this.meetingEmailsSentTo = this.meetingEmailsSentTo :+ meetingEmailSentTo
    })

    val DBListOfMeetingEmailsReceivedFrom = DBmeetingAttendee.get("meetingEmailsReceivedFrom").asInstanceOf[BasicDBList].toList
    DBListOfMeetingEmailsReceivedFrom.foreach(DBmeetingEmailReceivedFrom => {
      val meetingEmailReceivedFrom = new MeetingEmail(DBmeetingEmailReceivedFrom.asInstanceOf[BasicDBObject])
      this.meetingEmailsReceivedFrom = this.meetingEmailsReceivedFrom :+ meetingEmailReceivedFrom
    })

    this.numberOfEmailsReceivedFrom = DBmeetingAttendee.get("numberOfEmailsReceivedFrom").asInstanceOf[Int]
    this.numberOfEmailsSentTo = DBmeetingAttendee.get("numberOfEmailsSentTo").asInstanceOf[Int]
  }
}
