import requests
import json
class Templates:
    SERVER_TEST = "http://localhost:8080/testSupervisor"
    CLEAN_SERVER_STATE = "http://localhost:8080/cleanServerState"
    CLEAN_COMMAND_EMPTY_MEETINGREQUESTS = "Empty Meeting Requests"
    HEADER_USER_INIT = """
    Date: Tue, 1 Mar 2016 05:39:24 +0530
    Message-ID: <INIT-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com>
    """
    HEADER_USER_RESPONSE = """
    Date: Tue, 1 Mar 2016 06:30:24 +0530
    Message-ID: <RESP-YZmSJviXnVCBtuXjKpZ39v6-1f=6Tt0F_SPv8FSFf2v7Arw@mail.gmail.com>
    References: <INIT-YZmSd9z=DC5Ywb=AOHK5Op8JSHehAGWyFERz0Vt=nYwZFbA@mail.gmail.com> <cmMJfk9CTAGgIkGB0YfQ2w@ismtpd0017p1sin1.sendgrid.net>
    """

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

class Templates1:
    TEXT_INIT = "emily, pls schedule a meeting for next Monday."


if __name__ == "__main__":
    r = requests.post(Templates.CLEAN_SERVER_STATE,
                      json={"command": Templates.CLEAN_COMMAND_EMPTY_MEETINGREQUESTS
                      })
    response = r.text
    print(response)
    r1 = requests.post(Templates.SERVER_TEST,
                      json={"headers": Templates.HEADER_USER_INIT,
                            "text": Templates1.TEXT_INIT,
                            "from": Templates.EMAILID_RGK,
                            "to": Templates.EMAILID_B + "," + Templates.EMAILID_C,
                            "cc": Templates.EMAILID_emily,
                            "subject": Templates.SUBJECT_INIT
                      })
    print(r1.text)
    r2 = requests.post(Templates.SERVER_TEST,
                      json={"headers": Templates.HEADER_USER_INIT,
                            "text": Templates.TEXT_CONFIRM,
                            "from": Templates.EMAILID_B,
                            "to": Templates.EMAILID_emily,
                            "cc":Templates.EMAILID_emily,
                            "subject": Templates.SUBJECT_REPLY
                      })
    print(r2.text)
