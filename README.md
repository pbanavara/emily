# Emily is a conversational agent to help negotiate and schedule meetings over email.

### Basic flow
* At the core is a classifier model for recognizing incoming emails as meetings.
* Next is a datetime library integration to convert english phrases into specific date and time.
* For instance 'day after tomoorrow' -> YYYY:MM::DD:H:M:S
* Then there is a workflow engine to send emails back and forth.
* Finally there is a human in the loop web interface to inspect outgoing emails for correctness and validity.

### Improvements worked upon in 2023
* Incorporate langchain to simplify the workflow and use better language models.
* Remove human in the loop interfaces altogether.
* Remove dependency on outside email and calendaring applications.
