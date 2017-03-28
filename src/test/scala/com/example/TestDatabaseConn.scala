package com.example

import co.axoni.database.UserCredentialsDB
import co.axoni.processemail.MeetingCalendar
import co.axoni.webservice.{Schema, UserToken}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.{Session, SessionFactory}

/**
  * Created by pbanavara on 11/01/16.
  */
object TestDatabaseConn {
  def storeAccessToken(emailId: String, accessToken: String, refreshToken: String): Unit = {
    val databaseUsername = "dbuser"
    val databasePassword = "dbpassword"
    val databaseConnection = "jdbc:mysql://localhost:3306/emily"
    Class.forName("com.mysql.jdbc.Driver")
    SessionFactory.concreteFactory = Some(() => Session.create(
      java.sql.DriverManager.getConnection(databaseConnection, databaseUsername, databasePassword),
      new MySQLAdapter))

    transaction {
      val user1:UserToken = new UserToken(emailId, accessToken, refreshToken)
      Schema.users.insert(user1)
      println("Inserted user1")
    }
  }

  def main(args: Array[String]): Unit = {
    //storeAccessToken("user@dummy1.com", "dummyAccess", "dummyRefresh")
    //MeetingCalendar.addCalendarEntryForOrganizer(UserCredentialsDB.getAccessToken("pb@zenviron.io"), "Test Meeting",
     // "2016-03-21T16:00:00+05:30", "2016-03-21T16:30:00+05:30")

  }
}
