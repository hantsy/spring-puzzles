C:\Users\hantsy>curl http://localhost:8080/camel/posts
[{"id":24,"title":"Configure Oracle DataSource in Apache Tomcat 9","body":"test content","createdAt":"2020-08-25T17:45:03.32941"}]
C:\Users\hantsy>curl http://localhost:8080/camel/posts
{"timestamp":"2020-08-25T09:46:42.284+00:00","status":404,"error":"Not Found","message":"","path":"/camel/posts"}
C:\Users\hantsy>curl http://localhost:8080/api/posts
[{"id":25,"title":"Configure Oracle DataSource in Apache Tomcat 9","body":"test content","createdAt":"2020-08-25T17:45:59.164222"}]
C:\Users\hantsy>curl http://localhost:8080/api/posts
[{"id":26,"title":"Configure Oracle DataSource in Apache Tomcat 9","body":"test content","createdAt":{"nano":2647000,"year":2020,"monthValue":8,"dayOfMonth":25,"hour":17,"minute":48,"second":6,"dayOfWeek":"TUESDAY","dayOfYear":238,"month":"AUGUST","chronology":{"id":"ISO","calendarType":"iso8601"}}}]
C:\Users\hantsy>