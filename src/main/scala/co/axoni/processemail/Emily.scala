package co.axoni.processemail

import java.util
import co.axoni.database.MongoOperations
import co.axoni.processemail._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer



object Emily {
  val logger = Logger(LoggerFactory.getLogger(this.getClass.getName))

  // Global data structures

  var debugAndExceptionMessages: String = ""
  var meetingRequestID: Int = 0
  var userToMeetingRequestsMapping: Map[String, List[Int]] = Map()
  var supervisorTransactionID: Int = 0

  val SENDGRID_ENABLE: Boolean = true
  var TEST_MODE: Boolean = false
  var SUPERVISOR_MODE: Boolean = false
  val EMILY_TRIGGER_YES: Boolean = true
  val EMILY_TRIGGER_NO: Boolean = false
  val NLP: StanfordCoreNlp = StanfordCoreNlp.getInstance()
  val ALTERNATIVE_TIME_INCREMENT_MINUTES: Long = 60


  def addDebugAndExceptionMessages(msg: String): String = {
    this.debugAndExceptionMessages = this.debugAndExceptionMessages + "\n" + msg
    msg
  }

  def cleanServerState() = {
    MongoOperations.cleanServerState()
    //allMeetingRequests = Nil
    userToMeetingRequestsMapping = Map()
    meetingRequestID = 0
    logger.debug("\n\n\n\n\n Cleaned Server State \n in DB and memory \n\n\n\n\n")
  }


}
