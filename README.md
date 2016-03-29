#README
 
####Requirements
1. [ActiveMQ](http://activemq.apache.org). This project tested with version [5.12.0](http://activemq.apache.org/activemq-5120-release.html).
2. Embedded database: [hsqldb](http://mvnrepository.com/artifact/org.hsqldb/hsqldb/2.3.3) 

Send message using postman:
http://localhost:8080/notifications
Body - raw:
{"subject":"Test Targeted Notification", "content":"Test notification content for user 123456"}
