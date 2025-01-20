# Booking Service

This application is part of the RentSphere system and provides the functionality for booking houses.

## REST API

|  Endpoint	  | Method   |   Req. body    | Status | Resp. body | Description    		   	 |
|:-----------:|:--------:|:--------------:|:------:|:----------:|:----------------------|
| `/bookings` | `GET`    |                | 200    |  Bookings  | Get all the bookings. |
| `/bookings` | `POST`   | BookingRequest | 200    |  Booking   | Submit a new booking. |

## Useful Commands

| Gradle Command	         | Description                                   |
|:---------------------------|:----------------------------------------------|
| `./gradlew bootRun`        | Run the application.                          |
| `./gradlew build`          | Build the application.                        |
| `./gradlew test`           | Run tests.                                    |
| `./gradlew bootJar`        | Package the application as a JAR.             |
| `./gradlew bootBuildImage` | Package the application as a container image. |

After building the application, you can also run it from the Java CLI:

```bash
java -jar build/libs/booking-service-0.0.1-SNAPSHOT.jar
```
