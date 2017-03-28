import unittest
import requests
import json
import sys


class Templates:
    SERVER_TEST = "http://localhost:8080/test"
    SERVER_TESTHILL = "http://localhost:8080/testHill"
    SERVER_TESTSUPERVISOR = "http://localhost:8080/testSupervisor"
    CLEAN_SERVER_STATE = "http://localhost:8080/cleanServerState"
    CLEAN_COMMAND_EMPTY_MEETINGREQUESTS = "Empty Meeting Requests"
    HEADER_USER_INIT1 = """
    Date: Mon, 21 Mar 2016 05:39:24 +0530
    Message-ID: <INIT1-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com>
    """
    HEADER_USER_RESPONSE1 = """
    Date: Mon, 21 Mar 2016 06:30:24 +0530
    Message-ID: <RESP1-YZmSJviXnVCBtuXjKpZ39v6-1f=6Tt0F_SPv8FSFf2v7Arw@mail.gmail.com>
    References: <INIT1-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com> <cmMJfk9CTAGgIkGB0YfQ2w@ismtpd0017p1sin1.sendgrid.net>
    """
    HEADER_USER_INIT2 = """
    Date: Mon, 21 Mar 2016 05:39:24 +0530
    Message-ID: <INIT2-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com>
    """
    HEADER_USER_RESPONSE2 = """
    Date: Mon, 21 Mar 2016 06:30:24 +0530
    Message-ID: <RESP2-YZmSJviXnVCBtuXjKpZ39v6-1f=6Tt0F_SPv8FSFf2v7Arw@mail.gmail.com>
    References: <INIT2-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com> <cmMJfk9CTAGgIkGB0YfQ2w@ismtpd0017p1sin1.sendgrid.net>
    """
    HEADER_USER_INIT3 = """
    Date: Mon, 21 Mar 2016 05:39:24 +0530
    Message-ID: <INIT3-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com>
    """
    HEADER_USER_RESPONSE3 = """
    Date: Mon, 21 Mar 2016 06:30:24 +0530
    Message-ID: <RESP3-YZmSJviXnVCBtuXjKpZ39v6-1f=6Tt0F_SPv8FSFf2v7Arw@mail.gmail.com>
    References: <INIT3-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com> <cmMJfk9CTAGgIkGB0YfQ2w@ismtpd0017p1sin1.sendgrid.net>
    """

    HEADER_USER_INIT = HEADER_USER_INIT1
    HEADER_USER_RESPONSE = HEADER_USER_RESPONSE1

    TEXT_INIT = "emily, pls schdeule a meeting for next Monday."
    TEXT_CONFIRM = "Works for me. Look forward."
    TESTER = "TESTER"
    EMAILID_emily = "emily@axoni.co"
    EMAILID_RGK = "pb@zenviron.io"
    EMAILID_A = "A@zenviron.io"
    EMAILID_B = "B@zenviron.io"
    EMAILID_C = "C@zenviron.io"
    EMAILID_D = "D@zenviron.io"
    EMAILID_TEST1 = "test1.zenviron@gmail.com"
    EMAILID_TEST2 = "test2.zenviron@gmail.com"
    EMAILID_BLANK = ""
    SUBJECT_INIT = "Meeting"
    SUBJECT_REPLY = "Re: Meeting"


#######################################################################################################


class Clean_Server_State(unittest.TestCase):
    def test_1_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"


#######################################################################################################

class Templates1:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."


class TestEmily1(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates1.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)

    def test_3_C_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)


#######################################################################################################

class Templates2:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."
    TEXT_DECLINE = "Sorry, I have a conflict at this time. Can we try another time?"


class TestEmily2(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates1.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_B_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates2.TEXT_DECLINE,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates3(Templates):
    SERVER_TEST_GOOGLE_CALENDAR = "http://localhost:8080/availability"
    STARTTIME_LOOKUP1 = "2017-01-26T00:00:00Z"
    ENDTIME_LOOKUP1 = "2017-01-27T00:00:00Z"
    STARTTIME_LOOKUP2 = "2017-01-26T00:00:00+05:30"
    ENDTIME_LOOKUP2 = "2017-01-26T01:00:00+05:30"
    STARTTIME_MEETING = "2017-01-26T10:30:00+05:30"
    ENDTIME_MEETING = "2017-01-26T11:30:00+05:30"
    STARTTIME_LOOKUP3 = "2016-02-22T09:00:00+00:00"
    ENDTIME_LOOKUP3 = "2016-02-22T18:00:00+00:00"
    TIMEZONE = "Asia/Kolkata"
    CALENDAR_ENTRY = ""


class TestEmily3(unittest.TestCase):
    def test_Google_Calendar1(self):
        payload = {'email': Templates.EMAILID_RGK, 'startTime': Templates3.STARTTIME_LOOKUP1,
                   'endTime': Templates3.ENDTIME_LOOKUP1, 'timeZone': Templates3.TIMEZONE}
        r = requests.get(Templates3.SERVER_TEST_GOOGLE_CALENDAR, payload)
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(response["status"] == "ok")
        self.assertTrue(json.loads(response["message"])[0]["start"] == Templates3.STARTTIME_MEETING)
        self.assertTrue(json.loads(response["message"])[0]["end"] == Templates3.ENDTIME_MEETING)

    def test_Google_Calendar2(self):
        payload = {'email': Templates.EMAILID_RGK, 'startTime': Templates3.STARTTIME_LOOKUP2,
                   'endTime': Templates3.ENDTIME_LOOKUP2, 'timeZone': Templates3.TIMEZONE}
        r = requests.get(Templates3.SERVER_TEST_GOOGLE_CALENDAR, payload)
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(response["status"] == "ok")

    def test_Google_Calendar3(self):
        payload = {'email': Templates.EMAILID_RGK, 'startTime': Templates3.STARTTIME_LOOKUP3,
                   'endTime': Templates3.ENDTIME_LOOKUP3, 'timeZone': ""}
        r = requests.get(Templates3.SERVER_TEST_GOOGLE_CALENDAR, payload)
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(response["status"] == "ok")


#######################################################################################################


class Templates4(Templates):
    SERVER_TEST_SUTIME = "http://localhost:8080/testSUTime"
    TEXT_INIT1 = "emily, pls schedeule a meeting for next Monday 10AM."
    TEXT_INIT2 = "emily, pls schedeule a meeting for sometime next Monday afternoon."
    TEXT_INIT3 = "emily, pls schedeule a meeting for this Friday or next Monday."
    TEXT_INIT4 = "emily, pls schedeule a meeting for this Friday morning or next Monday afternoon."
    TEXT_INIT5 = "emily, pls schedeule a meeting for tomorrow 10AM."
    TEXT_INIT6 = "emily, pls schedeule a meeting for tomorrow."
    TEXT_INIT7 = "emily, pls schedeule a meeting for tomorrow morning."
    TEXT_INIT8 = "emily, pls schedeule a meeting for this Thursday morning."
    # TEXT_INIT9 = "emily, pls schedeule a meeting today after two hours." // DOESN'T WORK


class TestEmily4(unittest.TestCase):
    def test_SUTime1(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT1
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT1
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)

    def test_SUTime2(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT2
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT2
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)

    def test_SUTime3(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT3
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT3
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)

    def test_SUTime4(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT4
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT4
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)

    def test_SUTime5(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT5
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT5
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)

    def test_SUTime6(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT6
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT6
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)

    def test_SUTime7(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT7
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT7
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)

    def test_SUTime8(self):
        r = requests.post(Templates4.SERVER_TEST_SUTIME,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates4.TEXT_INIT8
                                })
        response = json.loads(r.text)
        print Templates4.TEXT_INIT8
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 1)


#######################################################################################################


class Templates5(Templates):
    TEXT_INIT1 = "emily, pls schedeule a meeting for afternoon of January 26 2017."


class TestEmily5(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates5.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A + "," + Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates4.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################


class Templates6:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."
    TEXT_DECLINE = "Sorry, I have a conflict at this time. Can we try another time?"


class TestEmily6(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates6.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)

    def test_3_C_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates2.TEXT_DECLINE,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_4_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5_C_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[3]["to"] == Templates.TESTER)


#######################################################################################################



class Templates7:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."
    TEXT_DECLINE = "Sorry, I have a conflict at this time. Can we try another time?"


class TestEmily7(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates7.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C + "," + Templates.EMAILID_D,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_2_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_C_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates2.TEXT_DECLINE,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_4_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_4_C_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5_D_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_D,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 5)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[3]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[4]["to"] == Templates.TESTER)


#######################################################################################################



class Templates8:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."
    TEXT_DECLINE = "Sorry, I have a conflict at this time. Can we try another time?"


class TestEmily8(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates8.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C + "," + Templates.EMAILID_D,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_2_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_C_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates8.TEXT_DECLINE,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_3_D_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates8.TEXT_DECLINE,
                                "from": Templates.EMAILID_D,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_4_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5_C_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_6_D_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_D,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 5)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[3]["to"] == Templates.EMAILID_D)
        self.assertTrue(response[4]["to"] == Templates.TESTER)


#######################################################################################################


class Templates9:
    TEXT_INIT = "emily, pls schedule a meeting on afternoon of January 26 2017 for 2 hrs."
    EMAILID_RGK = "pb@zenviron.io"


class TestEmily9(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING SPECIFIED DURATION *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates9.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################



class Templates10:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."
    EMAILID_TEST1 = "test1.zenviron@gmail.com"
    EMAILID_TEST2 = "test2.zenviron@gmail.com"


class TestEmily10(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING NEW/EXISTING MEETING REQUEST DIFF-PARTICIPANTS *****")

    def test_1_A_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates10.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        for email in response:
            print email
        # print response

        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.TESTER)
        self.assertTrue(response[1]["text"] == "1")

    def test_2_B_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates10.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        # print response
        for email in response:
            print email
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["text"] == "2")
        self.assertTrue(response[1]["to"] == Templates.TESTER)


#######################################################################################################
class Templates11:
    TEXT_INIT1 = "emily, pls schedule a meeting for next Monday."
    TEXT_INIT2 = "emily, pls schedule a meeting for next Tuesday."
    EMAILID_TEST1 = "test1.zenviron@gmail.com"
    EMAILID_TEST2 = "test2.zenviron@gmail.com"


class TestEmily11(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING NEW/EXISTING MEETING REQUEST SAME-PARTICIPANTS DIFF-TIMES*****")

    def test_1_A_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates11.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        for email in response:
            print email
        # print response

        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["text"] == "1")
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_B_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates11.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        # print response
        for email in response:
            print email
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["text"] == "2")
        self.assertTrue(response[1]["to"] == Templates.TESTER)


#######################################################################################################

class Templates12:
    TEXT_INIT1 = "emily, pls schedule a meeting for 9AM next Monday."
    TEXT_INIT2 = "emily, pls schedule a meeting at 9AM next Tuesday for 30 mins."
    EMAILID_TEST1 = "test1.zenviron@gmail.com"


class TestEmily12(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING SPECIFIC/EXACT TIME REQUESTS *****")

    def test_1_A_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates12.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        for email in response:
            print email
        # print response

        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["text"] == "1")
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_A_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates12.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        for email in response:
            print email
        # print response

        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["text"] == "2")
        self.assertTrue(response[1]["to"] == Templates.TESTER)


#######################################################################################################

class Templates13:
    TEXT_INIT = "Emily, Can you please schedule a meeting with Rajeev for the day after tomorrow."
    EMAILID_TEST1 = "test1.zenviron@gmail.com"


class TestEmily13(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING FULL FLOW SPECIFIC/EXACT TIME REQUESTS *****")

    def test_1_A_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates13.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        for email in response:
            print email

        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["text"] == "1")
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_A_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_A,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        print "\n"
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        # self.assertTrue("DTSTART:20160214T090000+05:30DTEND:20160214T100000+05:30" in response[0]["attachment"])
        self.assertTrue(response[1]["to"] == Templates.EMAILID_A)
        # self.assertTrue("DTSTART:20160214T090000+05:30DTEND:20160214T100000+05:30" in response[1]["attachment"])
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates14(Templates):
    TEXT_INIT1 = "emily, pls schedeule a meeting for next Monday 10AM."
    SUBJECT_INIT1 = "Meeting for next Monday"
    TEXT_INIT2 = "emily, pls schedeule a meeting for next Thursday."
    SUBJECT_INIT2 = "Meeting for next Thursday"
    TEXT_INIT3 = "emily, pls schedeule a meeting for sometime tomorrow."
    SUBJECT_INIT3 = "Meeting for tomorrow"
    TEXT_INIT4 = "emily, pls schedeule a meeting for day after tomorrow."
    SUBJECT_INIT4 = "Meeting for day after tomorrow"
    TEXT_INIT5 = "emily, pls schedeule a meeting with Pradeep for this Sunday."
    SUBJECT_INIT5 = "Meeting for this Sunday"
    TEXT_INIT6 = "emily, pls schedeule a meeting for next Saturday."
    SUBJECT_INIT6 = "Meeting for next Saturday"
    TEXT_INIT7 = "emily, pls schedeule a meeting for today."
    SUBJECT_INIT7 = "Meeting for today"
    TEXT_INIT8 = "emily, pls schedeule a meeting for next week."
    SUBJECT_INIT8 = "Meeting for next week"
    # Below gets parsed as DURATION P1W?!?! !!!DOES NOT WORK!!!
    # TEXT_INIT9 = "emily, pls schedeule a meeting for week after next."
    # Below gets parsed as a specific day which is two weeks offset from today !!!WORKS!!!
    TEXT_INIT9 = "emily, pls schedeule a meeting for two weeks from now."
    SUBJECT_INIT9 = "Meeting for two weeks from now"
    # Below gets parsed as next week..the first next is ignored
    # TEXT_INIT9 = "emily, pls schedeule a meeting for next to next week."
    # Below gets parsed as 2016-02-29 INTERSECT P1W. We should be able to handle this. !!!TBD!!!
    # TEXT_INIT9 = "emily, pls schedeule a meeting for sometime in the week of Feb 29th."


class TestEmily14(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TESTING FULL FLOW RANGE TIME REQUESTS *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT1
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_1_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT1
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_1_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_2_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT2
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT2
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_3_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT3,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT3
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT3
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_3_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })

        response = r.text
        print response + "\n"

    def test_4_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT4,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT4
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_4_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT4
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_4_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })

        response = r.text
        print response + "\n"

    def test_5_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT5,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT5
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT5
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_5_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_6_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT6,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT6
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_6_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT6
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_6_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_7_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT7,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT7
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_7_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT7
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_7_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_8_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT8,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT8
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_8_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT8
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_8_zClean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_9_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates14.TEXT_INIT9,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates14.SUBJECT_INIT9
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_9_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates14.SUBJECT_INIT9
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates15(Templates):
    TEXT_INIT = "emily, pls schdeule a meeting for next Monday."
    TEXT_DECLINE_WITH_ALTERNATIVE = """
emily, Sorry that does not work but how about next Tuesday?

On Fri, Feb 19, 2016 at 10:37 PM, Emily <emily@axoni.co> wrote:

>
> Hello Rajeev Gopalakrishna .
>
> Rajeev Gopalakrishna has proposed a meeting with you as an attendee.
> Please let me know if 9:00AM-10:00AM Monday 21-02-16 works.
>
> Thanks,
> Emily.
>
> Emily | Executive Assistant to Rajeev Gopalakrishna
>
>
>
>"""


class TestEmily15(unittest.TestCase):
    def test_0_description(selfself):
        print("***** PARTICIPANT DECLINES AND PROPOSES ALTERNATIVE TIME *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates15.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_B_Declines_With_Alternative(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates15.TEXT_DECLINE_WITH_ALTERNATIVE,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates16(Templates):
    TEXT_INIT1 = "emily, pls schdeule a meeting for next Monday."
    TEXT_INIT2 = "emily, pls schdeule a meeting for next Tuesday."
    SUBJECT_INIT1 = "Meeting next Monday"
    SUBJECT_INIT2 = "Meeting next Tuesday"


class TestEmily16(unittest.TestCase):
    def test_0_description(selfself):
        print("***** SAME PARTICIPANTS (DIFF HEADERS) ON TWO MEETING REQUESTS WITH DIFF SUBJECT LINES  *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates16.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_TEST1 + "," + Templates.EMAILID_TEST2,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates16.SUBJECT_INIT1
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_TEST2)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_TEST1_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE1,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_TEST1,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates16.SUBJECT_INIT1
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_TEST2_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE1,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_TEST2,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates16.SUBJECT_INIT1
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_TEST2)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_4_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates16.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_TEST1 + "," + Templates.EMAILID_TEST2,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates16.SUBJECT_INIT2
                                })

        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_TEST2)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_5_TEST1_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE2,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_TEST1,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates16.SUBJECT_INIT2
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_6_TEST2_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE2,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_TEST2,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates16.SUBJECT_INIT2
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_TEST1)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_TEST2)
        self.assertTrue(response[3]["to"] == Templates.TESTER)


#######################################################################################################


class Templates17:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday 9am or next Tuesday 10am."
    TEXT_CONFIRM1 = "Monday works."
    TEXT_CONFIRM2 = "Tuesday works."
    TEXT_CONFIRM3 = "9am works."
    TEXT_CONFIRM4 = "10am works."
    TEXT_CONFIRM5 = "Mon 9am works."
    TEXT_CONFIRM6 = "Tue 10am works."


class TestEmily17(unittest.TestCase):
    def test_0_description(selfself):
        print("***** TWO OPTIONS PROPOSED AND PARTICIPANT CONFIRMS DAY OR TIME OR BOTH *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_1z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM1,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_1zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_2_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM2,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_3_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM3,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_3zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_4_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_4z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM4,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_4zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_5_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM5,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_5zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_6_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates17.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_6z_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates17.TEXT_CONFIRM6,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates18:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday or next Tuesday."
    TEXT_CONFIRM1 = "Monday works."
    TEXT_CONFIRM2 = "Tuesday works."


class TestEmily18(unittest.TestCase):
    def test_0_description(selfself):
        print("***** PARTICIPANTS CONFIRM DIFFERENT OPTIONS AND THEN CONFIRM SAME OPTION *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates18.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_1z_B_Confirms_Group1(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates18.TEXT_CONFIRM1,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_1z_C_Confirms_Group2(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates18.TEXT_CONFIRM2,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2z_B_Confirms_Group1(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates18.TEXT_CONFIRM1,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2z_C_Confirms_Group1(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates18.TEXT_CONFIRM1,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[3]["to"] == Templates.TESTER)

    def test_2zz_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"


#######################################################################################################

class Templates19:
    TEXT_INIT = "emily, pls schedule a meeting for April 25th 9am or May 2nd 10:30 am."
    TEXT_CONFIRM1 = "2nd works."
    TEXT_CONFIRM2 = "10 works."


class TestEmily19(unittest.TestCase):
    def test_0_description(selfself):
        print(
            "***** TWO OPTIONS. PARTICIPANTS CONFIRM WITH NO SU TIMES BUT WITH DATE/TIME NUMBERS E.G. 2ND/10 (DETECT USIGN POS CD/JJ TAGS) *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates19.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B + "," + Templates.EMAILID_C,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_1_zB_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates19.TEXT_CONFIRM1,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_1_zC_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates19.TEXT_CONFIRM2,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": "Re: " + Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[3]["to"] == Templates.TESTER)


#######################################################################################################

class Templates20(Templates):
    TEXT_INIT1 = "Pls schdeule a meeting for next Monday."
    TEXT_INIT2 = "Pls schdeule a meeting for next Monday."
    TEXT_DECLINE_WITH_ALTERNATIVE1 = "Sorry next Monday does not work but how about next Tuesday?"  # SU_time match
    TEXT_DECLINE_WITH_ALTERNATIVE2 = "Sorry Monday does not work but how about next Tuesday?"  # SU_time fails for Monday because "next" is missing but Day string matches


class TestEmily20(unittest.TestCase):
    def test_0_description(selfself):
        print(
            "***** PARTICIPANT DECLINES PROPOSED TIME AND PROPOSES ALTERNATIVE TIME (IGNORE TIME DECLINED AND DETECT ONLY NEW ALTERNATIVE TIME(S)) *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates20.TEXT_INIT1,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_B_Declines_With_Alternative(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates20.TEXT_DECLINE_WITH_ALTERNATIVE1,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_4_Clean_Server_State(self):
        r = requests.post(Templates.CLEAN_SERVER_STATE,
                          json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                                })
        response = r.text
        print response + "\n"

    def test_5_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates20.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_6_B_Declines_With_Alternative(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates20.TEXT_DECLINE_WITH_ALTERNATIVE2,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_7_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################

class Templates21:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."


class TestEmily21(unittest.TestCase):
    def test_0_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates21.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print "Length of Response:"
        print len(response)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_1_A_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE1,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_A,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates21.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print "Length of Response:"
        print len(response)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT3,
                                "text": Templates21.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_C,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print "Length of Response:"
        print len(response)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_4_C_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE3,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_C)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_5_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE2,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################


class Templates22:
    TEXT_INIT = "emily, pls schedule a meeting for next Tuesday."


class TestEmily22(unittest.TestCase):
    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates22.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A + "," + Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })

    def test_2_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_INIT2,
                                "text": Templates22.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_C + "," + Templates.EMAILID_D,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })

    """
    def test_1_A_Confirms(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_RESPONSE1,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_A,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
    def test_2_C_Confirms(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_RESPONSE2,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_C,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_RESPONSE1,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
    def test_4_D_Confirms(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_RESPONSE2,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_D,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
    """


#######################################################################################################

class Templates23:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday noon."
    TEXT_DECLINE = "Sorry, that doesnt work."
    TEXT_INIT2 = "Ok, lets try next Tuesday noon."
    TEXT_CONFIRM = "Ok works."


class TestEmily23(unittest.TestCase):
    def test_0_description(self):
        print("***** PARTICIPANT DECLINES SPECIFIC TIME PROPOSED *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates23.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A + "," + Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_2_A_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates23.TEXT_CONFIRM,
                                "from": Templates.EMAILID_A,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_2_B_Declines(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates23.TEXT_DECLINE,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_newMeetingProposalFromOrganizerToEmily(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates23.TEXT_INIT2,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

    def test_4_A_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
        json={"headers": Templates.HEADER_USER_RESPONSE,
        "text": Templates23.TEXT_CONFIRM,
        "from": Templates.EMAILID_A,
        "to": Templates.EMAILID_emily,
        "cc": Templates.EMAILID_BLANK,
        "subject": Templates.SUBJECT_REPLY
        })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_5_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates23.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 4)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_A)
        self.assertTrue(response[2]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[3]["to"] == Templates.TESTER)


#######################################################################################################
class Templates24:
    TEXT_INIT = "emily, pls schedule a meeting for next month."
    TEXT_DECLINE = "Sorry, that doesnt work."


class TestEmily24(unittest.TestCase):
    def test_0_description(self):
        print("***** MEETING NEXT MONTH *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_INIT,
                                "text": Templates24.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 2)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[1]["to"] == Templates.TESTER)

    def test_3_B_Confirms(self):
        r = requests.post(Templates.SERVER_TEST,
                          json={"headers": Templates.HEADER_USER_RESPONSE,
                                "text": Templates.TEXT_CONFIRM,
                                "from": Templates.EMAILID_B,
                                "to": Templates.EMAILID_emily,
                                "cc": Templates.EMAILID_BLANK,
                                "subject": Templates.SUBJECT_REPLY
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)


#######################################################################################################
class Templates25:
    TEXT_INIT = "emily, pls schedule a meeting for next Tuesday or Wednesday or Friday."


class TestEmily25(unittest.TestCase):
    def test_0_description(self):
        print("***** Multiple SU Times *****")

    def test_1_meetingInitiation(self):
        r = requests.post(Templates.SERVER_TESTSUPERVISOR,
                          json={"headers": Templates.HEADER_USER_INIT1,
                                "text": Templates25.TEXT_INIT,
                                "from": Templates.EMAILID_RGK,
                                "to": Templates.EMAILID_A + "," + Templates.EMAILID_B,
                                "cc": Templates.EMAILID_emily,
                                "subject": Templates.SUBJECT_INIT
                                })
        response = json.loads(r.text)
        print response
        sys.stdout.flush()
        self.assertTrue(len(response) == 3)
        self.assertTrue(response[0]["to"] == Templates.EMAILID_RGK)
        self.assertTrue(response[1]["to"] == Templates.EMAILID_B)
        self.assertTrue(response[2]["to"] == Templates.TESTER)

#######################################################################################################

print("Running Tests")
sys.stdout.flush()
loader = unittest.TestLoader()
suite_clean_server_state = loader.loadTestsFromTestCase(Clean_Server_State)
suite_1 = loader.loadTestsFromTestCase(TestEmily1)
suite_2 = loader.loadTestsFromTestCase(TestEmily2)
suite_3 = loader.loadTestsFromTestCase(TestEmily3)
suite_4 = loader.loadTestsFromTestCase(TestEmily4)
suite_5 = loader.loadTestsFromTestCase(TestEmily5)
suite_6 = loader.loadTestsFromTestCase(TestEmily6)
suite_7 = loader.loadTestsFromTestCase(TestEmily7)
suite_8 = loader.loadTestsFromTestCase(TestEmily8)
suite_9 = loader.loadTestsFromTestCase(TestEmily9)
suite_10 = loader.loadTestsFromTestCase(TestEmily10)
suite_11 = loader.loadTestsFromTestCase(TestEmily11)
suite_12 = loader.loadTestsFromTestCase(TestEmily12)
suite_13 = loader.loadTestsFromTestCase(TestEmily13)
suite_14 = loader.loadTestsFromTestCase(TestEmily14)
suite_15 = loader.loadTestsFromTestCase(TestEmily15)
suite_16 = loader.loadTestsFromTestCase(TestEmily16)
suite_17 = loader.loadTestsFromTestCase(TestEmily17)
suite_18 = loader.loadTestsFromTestCase(TestEmily18)
suite_19 = loader.loadTestsFromTestCase(TestEmily19)
suite_20 = loader.loadTestsFromTestCase(TestEmily20)
suite_21 = loader.loadTestsFromTestCase(TestEmily21)
suite_22 = loader.loadTestsFromTestCase(TestEmily22)
suite_23 = loader.loadTestsFromTestCase(TestEmily23)
suite_24 = loader.loadTestsFromTestCase(TestEmily24)
suite_25 = loader.loadTestsFromTestCase(TestEmily25)
'''

unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_1)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_2)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_3)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_4)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_5)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_6)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_7)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_8)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_9)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_10)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_11)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_12)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_13)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_14)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_15)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_16)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_17)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_18)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_19)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_20)
'''
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_21)
"""
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_22)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_23)
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_24)
"""
#Multiple SU times test
unittest.TextTestRunner(verbosity=3).run(suite_clean_server_state)
unittest.TextTestRunner(verbosity=3).run(suite_25)
"""
exit(0)
