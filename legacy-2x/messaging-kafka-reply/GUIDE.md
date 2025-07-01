# Request/Reply Pattern  with Spring Kafka



## Run

```bash
docker-compose up // serves a RabbitMQ server instance
mvn spring-boot:run
```

## Test

```bash
mvn test  // run the sample test for Amqp/RabbitMQ
mvn verify -Pit // for functional tests
```

## Reference

* [Using ReplyingKafkaTemplate](https://docs.spring.io/spring-kafka/reference/html/#replying-template)

* [How to run spring boot admin client and server in same application](https://stackoverflow.com/questions/51851985/how-to-run-spring-boot-admin-client-and-server-in-same-application)


