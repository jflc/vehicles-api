package com.github.jflc.service

import com.github.jflc.util.Point
import com.github.jflc.util.distance
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.UUID
import java.util.Collections.singletonList
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.changestream.FullDocument
import io.ktor.application.ApplicationEvents
import io.ktor.application.EventDefinition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.conversions.Bson
import org.litote.kmongo.or
import java.util.function.Consumer

/**
 * Vehicle database model
 */
data class Vehicle(
    val id: UUID
)

/**
 * Vehicle location database model
 */
data class Location(
    val vehicleId: UUID,
    val lat: Double,
    val lng: Double,
    val at: Instant
)

/**
 * Service responsible to handle vehicles actions
 */
class VehiclesService(db: MongoDatabase, private val monitor: ApplicationEvents) {

    private val vehiclesCollection = db.getCollection<Vehicle>("vehicles")
    private val locationsCollection = db.getCollection<Location>("locations")
    private val center = Point(52.53, 13.403)
    private val locationEventDefinition = EventDefinition<Location>()

    /**
     * Creates a new vehicle
     *
     * @param id vehicle id
     */
    fun createVehicle(id: UUID) {
        val vehicle = Vehicle(id)
        vehiclesCollection.insertOne(vehicle)
    }

    /**
     * Find all existing vehicles
     *
     * @return list of vehicles
     */
    fun findVehicles() = vehiclesCollection.find().toList()

    /**
     * Delete a vehicle by id
     *
     * @param id vehicle id
     * @return true if vehicles exists, false if not
     */
    fun deleteVehicle(id: UUID): Boolean {
        val result = vehiclesCollection.deleteOne(Vehicle::id eq id)
        if (result.deletedCount == 0L) {
            return false
        }
        locationsCollection.deleteMany(Location::vehicleId eq id)
        return true
    }

    /**
     * Update vehicle location
     *
     * @param id vehicle id
     * @param lat vehicle latitude
     * @param lng vehicle longitude
     * @return true if vehicles exists, false if not
     */
    fun locationUpdate(id: UUID, lat: Double, lng: Double, at: Instant): Boolean {
        val vehicle = vehiclesCollection.findOne(Vehicle::id eq id) ?: return false
        val p = Point(lat, lng)
        if (p.distance(center) > 3.5) {
            throw IllegalArgumentException("Invalid location")
        }
        val location = Location(
            vehicleId = id,
            lat = lat,
            lng = lng,
            at = at
        )
        locationsCollection.insertOne(location)
        monitor.raise(locationEventDefinition, location)
        return true
    }

    /**
     * Find all vehicle locations
     *
     * @param id vehicle id
     * @return list of locations or null if vehicle doesn't exists
     */
    fun findLocations(id: UUID): List<Location>? {
        vehiclesCollection.findOne(Vehicle::id eq id) ?: return null
        return locationsCollection.find(Location::vehicleId eq id).toList()
    }

    /**
     * Watch for vehicle location updates
     *
     * @param id vehicle id
     * @param callback call when new location is available
     */
    fun watchLocations(id: UUID, callback:  suspend (location: Location) -> Unit) {
        monitor.subscribe(locationEventDefinition) { GlobalScope.launch {
            if (it.vehicleId === id) {
                callback(it)
            }
        } }

    }

}