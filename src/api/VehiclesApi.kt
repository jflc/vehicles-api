package com.github.jflc.api
import com.fasterxml.jackson.annotation.JsonFormat
import com.github.jflc.service.VehiclesService
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoDatabase
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.getOrFail
import io.ktor.websocket.webSocket
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.toList
import java.text.DateFormat
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Create vehicle request content data
 */
data class CreateVehicleRequest(val id: UUID)

/**
 * Update vehicle location request data content
 */
data class UpdateLocationRequest(
    val lat: Double,
    val lng: Double,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss.SSSX")
    val at: Date
)

/**
 * Update vehicle location request data content
 */
data class ReadLocationResponse(
    val lat: Double,
    val lng: Double,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss.SSSX")
    val at: Date
)

/**
 * Vehicles API routing
 */
fun Route.vehicles(service: VehiclesService) {
    route("/vehicles") {

        // vehicle registration
        post {
            val vehicle = call.receive<CreateVehicleRequest>()
            service.createVehicle(vehicle.id)
            call.respond(HttpStatusCode.NoContent)
        }

        // list vehicles
        get {
            val result = service.findVehicles()
            call.respond(HttpStatusCode.OK, result)
        }

        // vehicle de-registration
        delete("/{id}") {
            val id = call.parameters.getOrFail("id")
            val result = service.deleteVehicle(UUID.fromString(id))
            val responseStatusCode = if (result) HttpStatusCode.NoContent else HttpStatusCode.NotFound
            call.respond(responseStatusCode)

        }

        // location update
        post("/{id}/locations") {
            val id = call.parameters.getOrFail("id")
            val location = call.receive<UpdateLocationRequest>()
            val result = service.locationUpdate(
                id = UUID.fromString(id),
                lat = location.lat,
                lng = location.lng,
                at = location.at.toInstant()
            )
            val responseStatusCode = if (result) HttpStatusCode.NoContent else HttpStatusCode.NotFound
            call.respond(responseStatusCode)
        }

        // list vehicle locations
        get("/{id}/locations") {
            val id = call.parameters.getOrFail("id")
            val locations = service.findLocations(UUID.fromString(id))
            if (locations == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val result = locations.map {
                    ReadLocationResponse(
                        lat = it.lat,
                        lng = it.lng,
                        at = Date.from(it.at)
                    )
                }
                call.respond(HttpStatusCode.OK, result)
            }
        }

        webSocket("/{id}/locations/stream") {
            val id = call.parameters.getOrFail("id")
            service.watchLocations(UUID.fromString(id)) {
                outgoing.send(Frame.Text("(${it.lat}, ${it.lng})"))
            }
            for (frame in incoming) {
                println(frame)
            }
        }
    }
}