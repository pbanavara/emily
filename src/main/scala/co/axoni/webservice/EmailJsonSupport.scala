package co.axoni.webservice

import co.axoni.processemail.{MeetingAttendee, MeetingEmail, MeetingRequest}
import spray.json.{RootJsonFormat, JsValue, DefaultJsonProtocol}
import spray.httpx

/**
  * Created by pbanavara on 11/01/16.
  */
case class EmailData(body: String, confirmStatus: Boolean)

case class EmailInput(body: String)

case class Result(status: String, message: String)

case class GoogleCalendarResult(access_token: String, token_type: String, expires_in: Int, refresh_token: String, id_token: String)

case class EmailIds(id: String)

case class GoogleCalendarAttendee(email: String)
case class GoogleCalendarTime(dateTime: String)
case class GoogleCalendarAvailability(timeMin: String, timeMax: String, timeZone: String, items: List[EmailIds])
case class GoogleCalendarEntry(start: GoogleCalendarTime, end: GoogleCalendarTime, summary: String, attendees: List[GoogleCalendarAttendee])

case class TestInput(headers: String, to: String, from: String, cc: String, subject: String, text: String)

case class TestOutput(headers: String, to: String, from: String, cc: String, subject: String, text: String, headerReferences: String, attachment: String)

case class CleanServerState(command: String)

case class TestSUTimeInput(headers: String, text: String)

case class TestSUTimeOutput(meetingTimes: String)

case class GoogleCalendarBusyTimes(start: String, end: String)

case class GoogleCalendarList(results: List[GoogleCalendarBusyTimes])

case class JsonMeetingEmail(
                             emailHeaders: String,
                             emailFrom: String,
                             emailTo: String,
                             emailCC: String,
                             emailSubject: String,
                             emailText: String,
                             emailDatestamp: String,
                             emailTimestamp: String,
                             emailTimeZoneOffset: String,
                             emailMessageID: String,
                             emailInReplyTo: String,
                             emailReferences: List[String])

case class JsonMeetingRequest(
                               ID: Int,
                               confirmed: Boolean,
                               happened: Boolean,
                               meetingSubjectLine: String,
                               meetingLocation: String,
                               SU_Times: List[String],
                               SU_Durations: List[String],
                               meetingDuration: String,
                               meetingAttendees: List[JsonMeetingAttendee],
                               numberOfAttendees: Int,
                               numberOfAttendeesConfirmed: Int,
                               mostRecentEmail: JsonMeetingEmail,
                               numberOfEmailsReceivedForThisRequest: Int,
                               numberOfEmailsSentForThisRequest: Int)

case class JsonMeetingAttendee(
                                organizer: Boolean,
                                name: String,
                                emailID: String,
                                meetingTimesThatWorkGroup1: List[String],
                                meetingTimesThatWorkGroup2: List[String],
                                confirmed: Boolean,
                                waitingToHearBack: Boolean,
                                sentEmail: Boolean,
                                timeZone: String,
                                timeZoneOffset: String,
                                indexOfMeetingTimesThatWorkGroup1: Int,
                                indexOfMeetingTimesThatWorkGroup2: Int,
                                meetingEmailsSentTo: List[JsonMeetingEmail],
                                meetingEmailsReceivedFrom: List[JsonMeetingEmail],
                                numberOfEmailsReceivedFrom: Int,
                                numberOfEmailsSentTo: Int)

case class DataStagedForSupervisorTraining(incomingMeetingEmail: JsonMeetingEmail, associatedMeetingRequest: JsonMeetingRequest, outgoingMeetingEmails: List[TestOutput], debugAndExceptionMessages: String, supervisorTransactionID: String, supervisorOverrideEmailText: String)

case class DataAfterSupervisorTraining(incomingMeetingEmail: JsonMeetingEmail, associatedMeetingRequest: JsonMeetingRequest, outgoingMeetingEmails: List[TestOutput], supervisorTransactionID: String, supervisorOverrideEmailText: String, supervisorVerified: String)

case class TestObject(id: String)

object EmailJsonSupport extends DefaultJsonProtocol with httpx.SprayJsonSupport {
  implicit val PortfolioFormats = jsonFormat1(EmailInput)
  implicit val emailFormat = jsonFormat2(EmailData)
  implicit val resultFormat = jsonFormat2(Result)
  implicit val GoogleCalendarResultFormat = jsonFormat5(GoogleCalendarResult)
  implicit val EmailIdsFormat = jsonFormat1(EmailIds)
  implicit val GoogleCalendarAvailabilityFormat = jsonFormat4(GoogleCalendarAvailability)
  implicit val TestInputFormat = jsonFormat6(TestInput)
  implicit val TestOutputFormat = jsonFormat8(TestOutput)
  implicit val CleanServerStateFormat = jsonFormat1(CleanServerState)
  implicit val TestSUTimeInputFormat = jsonFormat2(TestSUTimeInput)
  implicit val TestSUTimeOutputFormat = jsonFormat1(TestSUTimeOutput)
  implicit val JsonMeetingEmailFormat = jsonFormat12(JsonMeetingEmail)
  implicit val JsonMeetingAttendeeFormat = jsonFormat16(JsonMeetingAttendee)
  implicit val JsonMeetingRequestFormat = jsonFormat14(JsonMeetingRequest)
  implicit val GoogleCalendarBusyTimesFormat = jsonFormat2(GoogleCalendarBusyTimes)
  implicit val DataStagedForSupervisorCheckingFormat = jsonFormat6(DataStagedForSupervisorTraining)
  implicit val DataAfterSupervisorTrainingFormat = jsonFormat6(DataAfterSupervisorTraining)
  implicit val GoogleCalendarAttendeeFormat = jsonFormat1(GoogleCalendarAttendee)
  implicit val GoogleCalendarTimeFormat = jsonFormat1(GoogleCalendarTime)
  implicit val GoogleCalendarEntryFormat = jsonFormat4(GoogleCalendarEntry)

  implicit object GoogleCalendarListFormat extends RootJsonFormat[GoogleCalendarList] {
    def read(value: JsValue) = GoogleCalendarList(value.convertTo[List[GoogleCalendarBusyTimes]])

    def write(obj: GoogleCalendarList) = ???
  }

}
