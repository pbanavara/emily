package co.axoni.processemail

import java.time
import java.time.{LocalDateTime, LocalDate, ZonedDateTime}
import java.time.format.DateTimeFormatter
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._

import scala.collection.mutable
import scala.util.control.Breaks

/**
  * Created by rajeevgopalakrishna on 1/27/16.
  */



object MeetingTimeHelperData {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  val START_MORNING = "09:00:00"
  val END_MORNING = "12:00:00"
  val START_AFTERNOON = "13:00:00"
  val END_AFTERNOON = "16:00:00"
  val START_EVENING = "16:00:00"
  val END_EVENING = "18:00:00"
  val START_NIGHT = "18:00:00"
  val END_NIGHT = "21:00:00"

  val exactTimeFormat = """(\d\d\d\d)-(\d\d)-(\d\d)T([0-9]+[0-9]+:[0-9]+[0-9]+)""".r
  val rangeTimeFormat = """(\d\d\d\d)-(\d\d)-(\d\d)T(MO|AF|EV|NI)""".r
  val offsetMonthFormat = """(\d\d\d\d)-(\d\d)""".r
  val offsetWeekFormat = """(\d\d\d\d)-W(\d\d)""".r
  val unspecifiedTimeFormat = """(\d\d\d\d)-(\d\d)-(\d\d)""".r

  val durationFormat = """PT([0-9]+)(M|H|D)""".r
  val durationFormatDefault = """([0-9]+)""".r
}

object MeetingTimeHelperFunctions {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def determineConfirmedGroupOfMeetingTimesThatWork(meetingRequest: MeetingRequest, mostRecentEmail: MeetingEmail, supervisorTrainingContent: Map[String, String]) : Int = {
    // Using regexps and classifier determine which of the two groups (1 or 2) is being referenced

    var SU_Times: List[String] = List()

    val organizer = meetingRequest.meetingAttendees.head
    val meetingTimeProposedFromGroup1 = if (organizer.numberOfMeetingTimesThatWorkGroup1() > organizer.indexOfMeetingTimesThatWorkGroup1) organizer.getLatestMeetingTimeThatWorks(1) else ""
    val meetingTimeProposedFromGroup2 = if (organizer.numberOfMeetingTimesThatWorkGroup2() > organizer.indexOfMeetingTimesThatWorkGroup2) organizer.getLatestMeetingTimeThatWorks(2) else ""

    // Only 1 meeting time was proposed. So group 1
    if (meetingTimeProposedFromGroup2 == "") 1
    else {
      // Two meeting times were proposed. So determine which one is being confirmed
      val meetingTimesInResponse: mutable.Buffer[MeetingTime] = Emily.NLP.getTimes(EmailHandler.stripThreadAndFullStopsFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",mostRecentEmail.getText())), mostRecentEmail.getDatestamp(), Emily.EMILY_TRIGGER_NO).asScala
      meetingTimesInResponse.foreach(entry => {
        entry.timeType match {
          case "TIME" | "OFFSET" => SU_Times = SU_Times :+ entry.time
          case _ =>
        }
      })
      SU_Times = SU_Times.distinct
      if (SU_Times.length > 1) throw new IllegalStateException(Emily.addDebugAndExceptionMessages("For now, Emily confused by more than 1 SU_Times in email response!"))
      if (SU_Times.isEmpty) {
        val numbersInResponse: mutable.Buffer[String] = Emily.NLP.getNumbers(EmailHandler.stripThreadAndFullStopsFromEmailText(supervisorTrainingContent.getOrElse("supervisorOverrideEmailText",mostRecentEmail.getText()))).asScala
        logger.debug("Numbers found in Email Text: " + numbersInResponse.mkString(" "))
        if (numbersInResponse.isEmpty) throw new IllegalStateException(Emily.addDebugAndExceptionMessages("NO SU_Times or Numbers in response => Cannot determine which of the proposed times is being accepted by participant!"))

        val dateGroup1: String = ZonedDateTime.parse(meetingTimeProposedFromGroup1).format(DateTimeFormatter.ofPattern("d"))
        val timeGroup1: String = ZonedDateTime.parse(meetingTimeProposedFromGroup1).format(DateTimeFormatter.ofPattern("h"))
        val dateGroup2: String = ZonedDateTime.parse(meetingTimeProposedFromGroup2).format(DateTimeFormatter.ofPattern("d"))
        val timeGroup2: String = ZonedDateTime.parse(meetingTimeProposedFromGroup2).format(DateTimeFormatter.ofPattern("h"))
        logger.debug("dateGroup1: " + dateGroup1)
        logger.debug("timeGroup1: " + timeGroup1)
        logger.debug("dateGroup2: " + dateGroup2)
        logger.debug("timeGroup2: " + timeGroup2)
        numbersInResponse.foreach(number => {
          if (number.contentEquals(dateGroup1) || number.contentEquals(timeGroup1)) return 1
          if (number.contentEquals(dateGroup2) || number.contentEquals(timeGroup2)) return 2
        })
        throw new IllegalStateException(Emily.addDebugAndExceptionMessages("NO SU_Times in response and Numbers dont correspond to proposed meeting times => Cannot determine which of the proposed times is being accepted by participant!"))
      }
      SU_Times.foreach(suTime => {
        val acceptedTime = suTime match {
          case MeetingTimeHelperData.exactTimeFormat(year, month, day, time) => suTime
          case MeetingTimeHelperData.unspecifiedTimeFormat(year, month, day) => suTime + "T00:00"
          case _ => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("For now, Emily confused by unknown time format"))
        }
        if (LocalDateTime.parse(acceptedTime).format(DateTimeFormatter.ofPattern("EE"))
          .contentEquals(ZonedDateTime.parse(meetingTimeProposedFromGroup1).format(DateTimeFormatter.ofPattern("EE")))) return 1
        if (LocalDateTime.parse(acceptedTime).format(DateTimeFormatter.ofPattern("hh:mm"))
          .contentEquals(ZonedDateTime.parse(meetingTimeProposedFromGroup1).format(DateTimeFormatter.ofPattern("hh:mm")))) return 1
        if (LocalDateTime.parse(acceptedTime).format(DateTimeFormatter.ofPattern("EE"))
          .contentEquals(ZonedDateTime.parse(meetingTimeProposedFromGroup2).format(DateTimeFormatter.ofPattern("EE")))) return 2
        if (LocalDateTime.parse(acceptedTime).format(DateTimeFormatter.ofPattern("hh:mm"))
          .contentEquals(ZonedDateTime.parse(meetingTimeProposedFromGroup2).format(DateTimeFormatter.ofPattern("hh:mm")))) return 2
      })
      throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Emily doesn't understand which of the proposed times is being accepted by participant!"))
    }
  }

  def getMeetingDuration(duration: String): String = {
    duration match {
      case MeetingTimeHelperData.durationFormat(duration, unit) =>
        unit match {
          case "M" => duration
          case "H" => (duration.toInt * 60).toString
          case "D" => (duration.toInt * 24 * 60).toString
          case _ => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Unknown Duration Type (Not M/H/D)!"))
        }
      case MeetingTimeHelperData.durationFormatDefault(duration) => duration
      case _ => throw new IllegalStateException(Emily.addDebugAndExceptionMessages("Unknown Duration Format (Not PT([0-9]+)(M|H|D)!"))
    }
}

  def getMeetingTimeRange(time: String): MeetingTimeRange = {
    time match {
      case MeetingTimeHelperData.offsetMonthFormat(year, month) =>
        val dateStart = LocalDate.parse(year+"-"+month+"-01", DateTimeFormatter.ISO_LOCAL_DATE)
        val dateEnd = LocalDate.parse(year+"-"+month+"-01", DateTimeFormatter.ISO_LOCAL_DATE)
        new MeetingTimeRange(""
          + dateStart + "T"
          + MeetingTimeHelperData.START_MORNING, ""
          + dateEnd + "T"
          + MeetingTimeHelperData.END_EVENING)
      case MeetingTimeHelperData.offsetWeekFormat(year, week) =>
        val dateStart = LocalDate.parse(year+"-W"+week+"-1", DateTimeFormatter.ISO_WEEK_DATE)
        val dateEnd = LocalDate.parse(year+"-W"+week+"-5", DateTimeFormatter.ISO_WEEK_DATE)
        new MeetingTimeRange(""
          + dateStart + "T"
          + MeetingTimeHelperData.START_MORNING, ""
          + dateEnd + "T"
          + MeetingTimeHelperData.END_EVENING)
      case MeetingTimeHelperData.rangeTimeFormat(year, month, day, range) =>
        range match {
          case "MO" => new MeetingTimeRange(""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.START_MORNING, ""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.END_MORNING)
          case "AF" => new MeetingTimeRange(""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.START_AFTERNOON, ""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.END_AFTERNOON)
          case "EV" => new MeetingTimeRange(""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.START_EVENING, ""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.END_EVENING)
          case "NI" => new MeetingTimeRange(""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.START_NIGHT, ""
            + year + "-"
            + month + "-"
            + day + "T"
            + MeetingTimeHelperData.END_NIGHT)
        }
      case MeetingTimeHelperData.unspecifiedTimeFormat(year, month, day) =>
        new MeetingTimeRange(""
          + year + "-"
          + month + "-"
          + day + "T"
          + MeetingTimeHelperData.START_MORNING, ""
          + year + "-"
          + month + "-"
          + day + "T"
          + MeetingTimeHelperData.END_EVENING)
      case MeetingTimeHelperData.exactTimeFormat(year, month, day, time) =>
        new MeetingTimeRange(""
          + year + "-"
          + month + "-"
          + day + "T"
          + time + ":00", ""
          + year + "-"
          + month + "-"
          + day + "T"
          + time + ":00")
      case _ => null
    }
  }

  def formatTimeAndDuration(time: String, duration: String) : String = {
    val startTime: ZonedDateTime = ZonedDateTime.parse(time)
    val endTime = startTime.plusMinutes(MeetingTimeHelperFunctions.getMeetingDuration(duration).toLong)
    val startTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma-")
    val startDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern(" EEEE dd-MM-YY")
    val endTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma")
    val startTimeStr = startTime.format(startTimeFmt)
    val endTimeStr = endTime.format(endTimeFmt)
    val startDateStr = startTime.format(startDateFmt)
    startTimeStr + endTimeStr + startDateStr
  }

  def formatTimeAndDurationNew(time: String, duration: String) : String = {
    val startTime: ZonedDateTime = ZonedDateTime.parse(time)
    val durationStr = MeetingTimeHelperFunctions.getMeetingDuration(duration)
    val startTimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("' at' h:mma")
    val startTimeStr = startTime.format(startTimeFmt)
    val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d")
    val startDayStr = startTime.format(dayFmt)
    val monthYearFmt: DateTimeFormatter = DateTimeFormatter.ofPattern(" '('MMM YYYY')'")
    val startMonthYearStr = startTime.format(monthYearFmt)
    val dateSuffix = startDayStr.takeRight(1) match {
      case "1" => if (startDayStr.takeRight(2)(0) == 1) "th" else "st" // 11th but 1st, 21st, 31st
      case "2" => "nd"
      case "3" => "rd"
      case _ => "th"
    }
    // TBD: Determine when it makes sense to append month+year to date. e.g. when proposed date(s) and current date are not in the same month/year
     startDayStr + dateSuffix + startTimeStr  //+ startMonthYearStr
  }

  def formatTimeAndDurationForICS(time: String, duration: String, timeZoneOffset: String, createTime: String) : List[String] = {
    val startTime: ZonedDateTime = ZonedDateTime.parse(time)
    val endTime = startTime.plusMinutes(MeetingTimeHelperFunctions.getMeetingDuration(duration).toLong)
    //Changing the format for Google calendar entries
    val ICStimeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMdd'T'hhmmss")
    //val ICStimeFmt2: DateTimeFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'hh:mm:ss")
    val startTimeStr = startTime.format(ICStimeFmt) + timeZoneOffset
    val endTimeStr = endTime.format(ICStimeFmt) + timeZoneOffset
    val dtstamp = LocalDateTime.parse(createTime).format(ICStimeFmt) + timeZoneOffset
    List(startTimeStr, endTimeStr, dtstamp)
  }

  def formatTimeForEmailThread(time: String) : String = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EE',' MMM dd',' YYYY 'at' h:mm a")
    ZonedDateTime.parse(time).format(formatter)
  }


  def formatTime(time: String) : String = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma EE dd'th ('MMMM YY')'")
    ZonedDateTime.parse(time).format(formatter)
  }

  def determineMeetingTimesThatWork(proposedMeetingTimeRange: MeetingTimeRange, calendarBusyTimeRangeList: List[MeetingTimeRange], durationInMinutes: String): List[MeetingTimeRange] = {

    var meetingTimesThatWork: List[MeetingTimeRange] = List()
    val proposedMeetingTimeRangeStart = ZonedDateTime.parse(proposedMeetingTimeRange.startTime)
    val proposedMeetingTimeRangeEnd = ZonedDateTime.parse(proposedMeetingTimeRange.endTime)

    var checkStart = proposedMeetingTimeRangeStart
    while (checkStart.plusMinutes(durationInMinutes.toLong).isBefore(proposedMeetingTimeRangeEnd) || checkStart.plusMinutes(durationInMinutes.toLong).isEqual(proposedMeetingTimeRangeEnd)) {
      val checkEnd = checkStart.plusMinutes(durationInMinutes.toLong)
      val overlappingSlots = calendarBusyTimeRangeList.dropWhile(calendarBusySlot => {
        !(checkStart.isAfter(ZonedDateTime.parse(calendarBusySlot.startTime)) && checkStart.isBefore(ZonedDateTime.parse(calendarBusySlot.endTime))) &&
          !(checkEnd.isAfter(ZonedDateTime.parse(calendarBusySlot.startTime)) && checkEnd.isBefore(ZonedDateTime.parse(calendarBusySlot.endTime))) &&
          !checkStart.isEqual(ZonedDateTime.parse(calendarBusySlot.startTime))
      })
      if (overlappingSlots.isEmpty) {
        logger.debug("meetingTimeThatWorks --> start: " + checkStart.toString() + " end: " + checkStart.plusMinutes(durationInMinutes.toLong).toString())
        meetingTimesThatWork = meetingTimesThatWork :+ new MeetingTimeRange(checkStart.toString(), checkStart.plusMinutes(durationInMinutes.toLong).toString())
      }
      checkStart = checkStart.plusMinutes(Emily.ALTERNATIVE_TIME_INCREMENT_MINUTES)
    }
    meetingTimesThatWork
  }

  def isExactTime(timeStr: String): Boolean = {
    timeStr match {
      case MeetingTimeHelperData.exactTimeFormat(year, month, day, time) => true
      case _ => false
    }
  }
}




class MeetingTimeRange (val startTime: String, val endTime: String) {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))
}

class MeetingTime (val time: String, val timeType: String, val token: String, val tokenCount: Int, val sentenceCount: Int) {

  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  def isExactTime(): Boolean = {
    time match {
      case MeetingTimeHelperData.exactTimeFormat(year, month, day, time) => true
      case _ => false
    }
  }

  def isRangeTime(): Boolean = {
    time match {
      case MeetingTimeHelperData.rangeTimeFormat(year, month, day, range) => true
      case _ => false
    }
  }

  def isUnspecifiedTime(): Boolean = {
    time match {
      case MeetingTimeHelperData.unspecifiedTimeFormat(year, month, day) => true
      case _ => false
    }
  }

  class TimeConvert() {
    def convertTimeToUTC(localTime: String) = {

    }
  }
}
