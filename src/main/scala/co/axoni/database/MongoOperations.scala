package co.axoni.database

import co.axoni.processemail.Emily._
import co.axoni.processemail.{Emily, MeetingEmail, MeetingRequest}
import co.axoni.webservice._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.ValidBSONType.BasicDBObject
import spray.json._

/**
  * Created by pbanavara on 02/03/16.
  */
object MongoOperations {


  import co.axoni.webservice.EmailJsonSupport._


  val mongoClient = MongoClient("localhost", 27017)
  val db = mongoClient("emily")
  db.collectionNames

  val collectionMeetingRequests = db("meetingRequests")
  val collectionUserToMeetingRequestsMapping = db("userToMeetingRequestsMapping")
  val collectionMeetingRequestID = db("meetingRequestID")
  val collectionDataStagedForSupervisorChecking = db("dataStagedForSupervisorChecking")
  val collectionSupervisorTransactionID = db("supervisorTransactionID")

  def updateOrInsertSupervisorTransaction(supervisorTransactionID: Int) = {
    val supervisorTransactionIDObject = MongoDBObject()
    collectionSupervisorTransactionID.update(supervisorTransactionIDObject, $set("supervisorTransactionID" -> supervisorTransactionID), upsert = true)
  }

  def deleteSupervisorTransaction(supervisorTransactionID: String) = {
    val query = MongoDBObject("_id" -> supervisorTransactionID)
    val result = collectionDataStagedForSupervisorChecking.findAndRemove(query)
    result match {
      case None => logger.debug("ERROR: Did not find the supervisor transaction to remove. ID: " + supervisorTransactionID)
      case _ =>
    }
  }

  def getSupervisorTransactionID(): Int = {
    val query = MongoDBObject()
    val fields = MongoDBObject("supervisorTransactionID" -> 1, "_id" -> 0)
    val supervisorTransactionID = collectionSupervisorTransactionID.findOne(query, fields)
    supervisorTransactionID match {
      case Some(supervisorTransactionID) => {
        println("Found supervisorTransactionID in DB: " + supervisorTransactionID("supervisorTransactionID"))
        supervisorTransactionID("supervisorTransactionID").asInstanceOf[Int]
      }
      case None => {
        print("NO supervisorTransactionID found in DB")
        0
      }
    }
  }

  def updateOrInsertDBInputAndExpectedOutputForSupervisorCheck(meetingEmail: MeetingEmail, meetingRequest: MeetingRequest, expectedOutputs: List[TestOutput], debugAndExceptionMessages: String, supervisorTransactionID: String, supervisorOverrideEmailText: String) = {

    var query = MongoDBObject()
    if (supervisorTransactionID != "") query = MongoDBObject("_id" -> supervisorTransactionID)

    collectionDataStagedForSupervisorChecking.update(
      query, $set(
        "incomingMeetingEmail" -> DBObject(
        "emailHeaders" -> meetingEmail.emailHeaders,
        "emailFrom" -> meetingEmail.emailFrom,
        "emailTo" -> meetingEmail.emailTo,
        "emailCC" -> meetingEmail.emailCC,
        "emailSubject" -> meetingEmail.emailSubject,
        "emailText" -> meetingEmail.emailText,
        "emailDatestamp" -> meetingEmail.emailDatestamp,
        "emailTimestamp" -> meetingEmail.emailTimestamp,
        "emailTimeZoneOffset" -> meetingEmail.emailTimeZoneOffset,
        "emailMessageID" -> meetingEmail.emailMessageID,
        "emailInReplyTo" -> meetingEmail.emailInReplyTo,
        "emailReferences" -> meetingEmail.emailReferences
        ),
        "associatedMeetingRequest" -> DBObject(
        "ID" -> meetingRequest.ID,
        "confirmed" -> meetingRequest.confirmed,
        "happened" -> meetingRequest.happened,
        "meetingSubjectLine" -> meetingRequest.meetingSubjectLine,
        "meetingLocation" -> meetingRequest.meetingLocation,
        "SU_Times" -> meetingRequest.SU_Times,
        "SU_Durations" -> meetingRequest.SU_Durations,
        "meetingDuration" -> meetingRequest.meetingDuration,
        "meetingAttendees" -> meetingRequest.meetingAttendees.map(meetingAttendee => {
          DBObject(
            "organizer" -> meetingAttendee.organizer,
            "name" -> meetingAttendee.name,
            "emailID" -> meetingAttendee.emailID,
            "meetingTimesThatWorkGroup1" -> meetingAttendee.meetingTimesThatWorkGroup1,
            "meetingTimesThatWorkGroup2" -> meetingAttendee.meetingTimesThatWorkGroup2,
            "confirmed" -> meetingAttendee.confirmed,
            "waitingToHearBack" -> meetingAttendee.waitingToHearBack,
            "sentEmail" -> meetingAttendee.sentEmail,
            "timeZone" -> meetingAttendee.timeZone,
            "timeZoneOffset" -> meetingAttendee.timeZoneOffset,
            "indexOfMeetingTimesThatWorkGroup1" -> meetingAttendee.indexOfMeetingTimesThatWorkGroup1,
            "indexOfMeetingTimesThatWorkGroup2" -> meetingAttendee.indexOfMeetingTimesThatWorkGroup2,
            "meetingEmailsSentTo" -> meetingAttendee.meetingEmailsSentTo.map(meetingEmailSentTo => {
              DBObject(
                "emailHeaders" -> meetingEmailSentTo.emailHeaders,
                "emailFrom" -> meetingEmailSentTo.emailFrom,
                "emailTo" -> meetingEmailSentTo.emailTo,
                "emailCC" -> meetingEmailSentTo.emailCC,
                "emailSubject" -> meetingEmailSentTo.emailSubject,
                "emailText" -> meetingEmailSentTo.emailText,
                "emailDatestamp" -> meetingEmailSentTo.emailDatestamp,
                "emailTimestamp" -> meetingEmailSentTo.emailTimestamp,
                "emailTimeZoneOffset" -> meetingEmailSentTo.emailTimeZoneOffset,
                "emailMessageID" -> meetingEmailSentTo.emailMessageID,
                "emailInReplyTo" -> meetingEmailSentTo.emailInReplyTo,
                "emailReferences" -> meetingEmailSentTo.emailReferences
              )
            }),
            "meetingEmailsReceivedFrom" -> meetingAttendee.meetingEmailsReceivedFrom.map(meetingEmailReceivedFrom => {
              DBObject(
                "emailHeaders" -> meetingEmailReceivedFrom.emailHeaders,
                "emailFrom" -> meetingEmailReceivedFrom.emailFrom,
                "emailTo" -> meetingEmailReceivedFrom.emailTo,
                "emailCC" -> meetingEmailReceivedFrom.emailCC,
                "emailSubject" -> meetingEmailReceivedFrom.emailSubject,
                "emailText" -> meetingEmailReceivedFrom.emailText,
                "emailDatestamp" -> meetingEmailReceivedFrom.emailDatestamp,
                "emailTimestamp" -> meetingEmailReceivedFrom.emailTimestamp,
                "emailTimeZoneOffset" -> meetingEmailReceivedFrom.emailTimeZoneOffset,
                "emailMessageID" -> meetingEmailReceivedFrom.emailMessageID,
                "emailInReplyTo" -> meetingEmailReceivedFrom.emailInReplyTo,
                "emailReferences" -> meetingEmailReceivedFrom.emailReferences
              )
            }),
            "numberOfEmailsReceivedFrom" -> meetingAttendee.numberOfEmailsReceivedFrom,
            "numberOfEmailsSentTo" -> meetingAttendee.numberOfEmailsSentTo
          )
        }),
        "numberOfAttendees" -> meetingRequest.numberOfAttendees,
        "numberOfAttendeesConfirmed" -> meetingRequest.numberOfAttendeesConfirmed,
        "mostRecentEmail" -> DBObject(
          "emailHeaders" -> meetingRequest.mostRecentEmail.emailHeaders,
          "emailFrom" -> meetingRequest.mostRecentEmail.emailFrom,
          "emailTo" -> meetingRequest.mostRecentEmail.emailTo,
          "emailCC" -> meetingRequest.mostRecentEmail.emailCC,
          "emailSubject" -> meetingRequest.mostRecentEmail.emailSubject,
          "emailText" -> meetingRequest.mostRecentEmail.emailText,
          "emailDatestamp" -> meetingRequest.mostRecentEmail.emailDatestamp,
          "emailTimestamp" -> meetingRequest.mostRecentEmail.emailTimestamp,
          "emailTimeZoneOffset" -> meetingRequest.mostRecentEmail.emailTimeZoneOffset,
          "emailMessageID" -> meetingRequest.mostRecentEmail.emailMessageID,
          "emailInReplyTo" -> meetingRequest.mostRecentEmail.emailInReplyTo,
          "emailReferences" -> meetingRequest.mostRecentEmail.emailReferences
        ),
        "numberOfEmailsReceivedForThisRequest" -> meetingRequest.numberOfEmailsReceivedForThisRequest,
        "numberOfEmailsSentForThisRequest" -> meetingRequest.numberOfEmailsSentForThisRequest
      ),
        "outgoingMeetingEmails" -> expectedOutputs.map(expectedOutput => {
          DBObject (
            "headers" -> expectedOutput.headers,
            "to" -> expectedOutput.to,
            "from" -> expectedOutput.from,
            "cc" -> expectedOutput.cc,
            "subject" -> expectedOutput.subject,
            "text" -> expectedOutput.text,
            "headerReferences" -> expectedOutput.headerReferences,
            "attachment" -> expectedOutput.attachment
          )}
        ),
        "debugAndExceptionMessages" -> debugAndExceptionMessages,
        "_id" -> supervisorTransactionID,
        "supervisorOverrideEmailText" -> supervisorOverrideEmailText
      ), upsert = true)
  }

  def insertDBIncomingEmailAndMatchingMeetingRequestForSupervisorCheck(meetingEmail: MeetingEmail, meetingRequestID: Int) = {
    val query = MongoDBObject("meetingRequestID" -> meetingRequestID)
    collectionDataStagedForSupervisorChecking.update(query, $set("meetingEmail" ->
      DBObject(
          "emailHeaders" -> meetingEmail.emailHeaders,
          "emailFrom" -> meetingEmail.emailFrom,
          "emailTo" -> meetingEmail.emailTo,
          "emailCC" -> meetingEmail.emailCC,
          "emailSubject" -> meetingEmail.emailSubject,
          "emailText" -> meetingEmail.emailText,
          "emailDatestamp" -> meetingEmail.emailDatestamp,
          "emailTimestamp" -> meetingEmail.emailTimestamp,
          "emailTimeZoneOffset" -> meetingEmail.emailTimeZoneOffset,
          "emailMessageID" -> meetingEmail.emailMessageID,
          "emailInReplyTo" -> meetingEmail.emailInReplyTo,
          "emailReferences" -> meetingEmail.emailReferences
        ),
        "meetingRequestID" -> meetingRequestID
      ), upsert = true)
  }

  def updateOrInsertMeetingRequest(meetingRequest: MeetingRequest) = {
    val query = MongoDBObject("meetingRequest.ID" -> meetingRequest.ID)

    collectionMeetingRequests.update(query, $set("meetingRequest"
      -> DBObject(
      "ID" -> meetingRequest.ID,
      "confirmed" -> meetingRequest.confirmed,
      "happened" -> meetingRequest.happened,
      "meetingSubjectLine" -> meetingRequest.meetingSubjectLine,
      "meetingLocation" -> meetingRequest.meetingLocation,
      "SU_Times" -> meetingRequest.SU_Times,
      "SU_Durations" -> meetingRequest.SU_Durations,
      "meetingDuration" -> meetingRequest.meetingDuration,
      "meetingAttendees" -> meetingRequest.meetingAttendees.map(meetingAttendee => {
        DBObject(
          "organizer" -> meetingAttendee.organizer,
          "name" -> meetingAttendee.name,
          "emailID" -> meetingAttendee.emailID,
          "meetingTimesThatWorkGroup1" -> meetingAttendee.meetingTimesThatWorkGroup1,
          "meetingTimesThatWorkGroup2" -> meetingAttendee.meetingTimesThatWorkGroup2,
          "confirmed" -> meetingAttendee.confirmed,
          "waitingToHearBack" -> meetingAttendee.waitingToHearBack,
          "sentEmail" -> meetingAttendee.sentEmail,
          "timeZone" -> meetingAttendee.timeZone,
          "timeZoneOffset" -> meetingAttendee.timeZoneOffset,
          "indexOfMeetingTimesThatWorkGroup1" -> meetingAttendee.indexOfMeetingTimesThatWorkGroup1,
          "indexOfMeetingTimesThatWorkGroup2" -> meetingAttendee.indexOfMeetingTimesThatWorkGroup2,
          "meetingEmailsSentTo" -> meetingAttendee.meetingEmailsSentTo.map(meetingEmailSentTo => {
            DBObject(
              "emailHeaders" -> meetingEmailSentTo.emailHeaders,
              "emailFrom" -> meetingEmailSentTo.emailFrom,
              "emailTo" -> meetingEmailSentTo.emailTo,
              "emailCC" -> meetingEmailSentTo.emailCC,
              "emailSubject" -> meetingEmailSentTo.emailSubject,
              "emailText" -> meetingEmailSentTo.emailText,
              "emailDatestamp" -> meetingEmailSentTo.emailDatestamp,
              "emailTimestamp" -> meetingEmailSentTo.emailTimestamp,
              "emailTimeZoneOffset" -> meetingEmailSentTo.emailTimeZoneOffset,
              "emailMessageID" -> meetingEmailSentTo.emailMessageID,
              "emailInReplyTo" -> meetingEmailSentTo.emailInReplyTo,
              "emailReferences" -> meetingEmailSentTo.emailReferences
            )
          }),
          "meetingEmailsReceivedFrom" -> meetingAttendee.meetingEmailsReceivedFrom.map(meetingEmailReceivedFrom => {
            DBObject(
              "emailHeaders" -> meetingEmailReceivedFrom.emailHeaders,
              "emailFrom" -> meetingEmailReceivedFrom.emailFrom,
              "emailTo" -> meetingEmailReceivedFrom.emailTo,
              "emailCC" -> meetingEmailReceivedFrom.emailCC,
              "emailSubject" -> meetingEmailReceivedFrom.emailSubject,
              "emailText" -> meetingEmailReceivedFrom.emailText,
              "emailDatestamp" -> meetingEmailReceivedFrom.emailDatestamp,
              "emailTimestamp" -> meetingEmailReceivedFrom.emailTimestamp,
              "emailTimeZoneOffset" -> meetingEmailReceivedFrom.emailTimeZoneOffset,
              "emailMessageID" -> meetingEmailReceivedFrom.emailMessageID,
              "emailInReplyTo" -> meetingEmailReceivedFrom.emailInReplyTo,
              "emailReferences" -> meetingEmailReceivedFrom.emailReferences
            )
          }),
          "numberOfEmailsReceivedFrom" -> meetingAttendee.numberOfEmailsReceivedFrom,
          "numberOfEmailsSentTo" -> meetingAttendee.numberOfEmailsSentTo
        )
      }),
      "numberOfAttendees" -> meetingRequest.numberOfAttendees,
      "numberOfAttendeesConfirmed" -> meetingRequest.numberOfAttendeesConfirmed,
      "mostRecentEmail" -> DBObject(
        "emailHeaders" -> meetingRequest.mostRecentEmail.emailHeaders,
        "emailFrom" -> meetingRequest.mostRecentEmail.emailFrom,
        "emailTo" -> meetingRequest.mostRecentEmail.emailTo,
        "emailCC" -> meetingRequest.mostRecentEmail.emailCC,
        "emailSubject" -> meetingRequest.mostRecentEmail.emailSubject,
        "emailText" -> meetingRequest.mostRecentEmail.emailText,
        "emailDatestamp" -> meetingRequest.mostRecentEmail.emailDatestamp,
        "emailTimestamp" -> meetingRequest.mostRecentEmail.emailTimestamp,
        "emailTimeZoneOffset" -> meetingRequest.mostRecentEmail.emailTimeZoneOffset,
        "emailMessageID" -> meetingRequest.mostRecentEmail.emailMessageID,
        "emailInReplyTo" -> meetingRequest.mostRecentEmail.emailInReplyTo,
        "emailReferences" -> meetingRequest.mostRecentEmail.emailReferences
      ),
      "numberOfEmailsReceivedForThisRequest" -> meetingRequest.numberOfEmailsReceivedForThisRequest,
      "numberOfEmailsSentForThisRequest" -> meetingRequest.numberOfEmailsSentForThisRequest
    )), upsert = true)

  }


  def updateOrInsertUserToMeetingRequestsMapping(userToMeetingRequestsMapping: Map[String, List[Int]]) = {
    userToMeetingRequestsMapping.foreach { case (user, meetingRequestIDs) =>
      val query = MongoDBObject("user" -> user)
      collectionUserToMeetingRequestsMapping.update(query,
        $set("meetingRequestIDs" -> meetingRequestIDs), upsert = true)
    }
  }

  def updateOrInsertMeetingRequestID(meetingRequestID: Int) = {
    val meetingRequestIDObject = MongoDBObject()
    collectionMeetingRequestID.update(meetingRequestIDObject, $set("meetingRequestID" -> meetingRequestID), upsert = true)
  }

  def getMeetingRequestID(): Int = {
    val query = MongoDBObject()
    val fields = MongoDBObject("meetingRequestID" -> 1, "_id" -> 0)
    val meetingRequestID = collectionMeetingRequestID.findOne(query, fields)
    meetingRequestID match {
      case Some(meetingRequestID) => {
        println("Found MeetingRequestID in DB: " + meetingRequestID("meetingRequestID"))
        meetingRequestID("meetingRequestID").asInstanceOf[Int]
      }
      case None => {
        print("NO MeetingRequestID found in DB")
        0
      }
    }
  }

  def getUserToMeetingRequestsMapping(): Map[String, List[Int]] = {
    var userToMeetingRequestsMapping: Map[String, List[Int]] = Map()
    val query = MongoDBObject()
    val DBuserToMeetingRequestsMapping = collectionUserToMeetingRequestsMapping.find(query)
    DBuserToMeetingRequestsMapping.foreach(DBmapping => {
      userToMeetingRequestsMapping = userToMeetingRequestsMapping +
        (DBmapping("user").asInstanceOf[String] -> DBmapping("meetingRequestIDs").asInstanceOf[BasicDBList].toList.asInstanceOf[List[Int]])
    })
    print("From DB userToMeetingRequestsMapping:\n" + userToMeetingRequestsMapping)
    userToMeetingRequestsMapping
  }

  def getAllMeetingRequests(): List[MeetingRequest] = {
    var allMeetingRequests: List[MeetingRequest] = List()
    val query = MongoDBObject()
    val DBallMeetingRequests = collectionMeetingRequests.find(query)
    DBallMeetingRequests.foreach(DBmeetingRequest => {
      println("meetingRequest: " + DBmeetingRequest("meetingRequest"))
      val meetingRequest = new MeetingRequest(DBmeetingRequest("meetingRequest").asInstanceOf[BasicDBObject])
      meetingRequest.printMeetingDetails()
      allMeetingRequests = allMeetingRequests :+ meetingRequest
    })
    allMeetingRequests
  }

  def relatedToExistingMeetingRequestsDB(meetingEmail: MeetingEmail): List[MeetingRequest] = {
    // For now, rule-based determination
    // Add classification logic here
    val potentialMeetingRequestMatches = this.findMeetingRequestMatchesBasedOnEmailHeaderMessageIDs(meetingEmail)
    if (potentialMeetingRequestMatches.length > 1) logger.debug("!!WARNING!! Multiple meeting request matches based on Email Header Message IDs ?!?!")
    /*
    if (potentialMeetingRequestMatches.isEmpty) {
      val potentialMeetingRequestMatches1 = this.findMeetingRequestMatchesBasedOnEmailSender(meetingEmail)
      //val potentialMeetingRequestMatches2 = this.findMeetingRequestMatchesBasedOnEmailSubject(meetingEmail, potentialMeetingRequestMatches1)
      potentialMeetingRequestMatches1
    } else potentialMeetingRequestMatches
    */
    potentialMeetingRequestMatches
  }

  def findMeetingRequestMatchesBasedOnEmailHeaderMessageIDs(meetingEmail: MeetingEmail): List[MeetingRequest] = {
    var potentialMeetingRequestMatches: List[MeetingRequest] = List()
    val query = ("meetingRequest.meetingAttendees.meetingEmailsReceivedFrom.emailMessageID" $in meetingEmail.emailReferences)
    val DBallMeetingRequests = collectionMeetingRequests.find(query)
    DBallMeetingRequests.foreach(DBmeetingRequest => {
      println("DB meetingRequest: " + DBmeetingRequest("meetingRequest"))
      val meetingRequest = new MeetingRequest(DBmeetingRequest("meetingRequest").asInstanceOf[BasicDBObject])
      meetingRequest.printMeetingDetails()
      potentialMeetingRequestMatches = potentialMeetingRequestMatches :+ meetingRequest
    })
    if (potentialMeetingRequestMatches.isEmpty) {
      logger.debug("**NO-MATCH! Existing Meeting Request Attendee-Email MessageID**")
    }
    else {
      logger.debug("**MATCH! Existing Meeting Request Attendee-Email MessageID**")
    }
    potentialMeetingRequestMatches
  }


  def findMeetingRequestMatchesBasedOnEmailSender(meetingEmail: MeetingEmail) : List[MeetingRequest] = {
    var potentialMeetingRequestMatches: List[MeetingRequest] = List()
    val queryUserMeetingRequestMapping = MongoDBObject("user" -> meetingEmail.getFromEmailID())
    val DBmeetingRequestsUserMapping = collectionUserToMeetingRequestsMapping.findOne(queryUserMeetingRequestMapping)
    DBmeetingRequestsUserMapping match {
      case Some(meetingRequestsUserMapping) => {
        val queryUserMeetingRequests = ("meetingRequest.ID" $in meetingRequestsUserMapping("meetingRequestIDs").asInstanceOf[BasicDBList].toList.asInstanceOf[List[Int]])
        val DBallMeetingRequests = collectionMeetingRequests.find(queryUserMeetingRequests)
        DBallMeetingRequests.foreach(DBmeetingRequest => {
          println("DB meetingRequest: " + DBmeetingRequest("meetingRequest"))
          val meetingRequest = new MeetingRequest(DBmeetingRequest("meetingRequest").asInstanceOf[BasicDBObject])
          meetingRequest.printMeetingDetails()
          potentialMeetingRequestMatches = potentialMeetingRequestMatches :+ meetingRequest
        })
        if (potentialMeetingRequestMatches.isEmpty) {
          logger.debug("**NO-MATCH! Existing Meeting Request Attendee-Email Sender**")
        }
        else {
          logger.debug("**MATCH! Existing Meeting Request Attendee-Email Sender**")
        }
        potentialMeetingRequestMatches
      }
      case None => {
        logger.debug("**NO-MATCH! Existing Meeting Request Attendee-Email Sender**")
        List()
      }
    }
  }

  def cleanServerState() = {
    logger.debug("\n\n\n\n\n Cleaned Server State \n Dropped Database \n\n\n\n\n")
    this.db.dropDatabase()
  }

}
