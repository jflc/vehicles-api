# Vehicles API

API to registers and de-register vehicles, update and notify vehicle locations

## Dependencies
* Java 1.8
* Gradle
* Docker
* Docker Compose

## How to run
```shell script
$ ./gradlew docker dockerComposeUp
```
Application will be listening at localhost:8080

## API Operations

### Vehicle registration

`POST /vehicles`

Request body:

```json
{ "id": "some-uuid-here" }
```

Response status code: 204


### Location update

`POST /vehicles/:id/locations`

Request body:

```json
{ "lat": 10.0, "lng": 20.0, "at": "2019-09-01T12:00:00Z" }
```

Response status code: 204

### Vehicle de-registration

`DELETE /vehicles/:id`

Response status code: 204

### Broadcasting vehicle locations

`Websocket /vehicles/:id/locations/stream`

Note: This operations is not working properly

## Tech Stack
* Web Framework: ktor (lightweight)
* Database: MongoDB
* Testing: Junit & Postman


## TODO
* Fix stream locations websocket
* Improve error messages
* Use event bus to broadcast location updates (e.g. Kafka)
* Improve test code coverage
