package util

import com.github.jflc.util.Point
import com.github.jflc.util.distance
import org.junit.Test

import org.junit.Assert.*

class PointTest {

    @Test
    fun testDistanceLisbonBerlin() {
        // given
        val p1 = Point(38.71667, -9.13333)
        val p2 = Point(52.520008,13.404954)

        // when
        val result = p1.distance(p2)

        // then
        assertEquals(2311.16 , result, 1.1)
    }

    @Test
    fun testDistanceSamePoint() {
        // given
        val p1 = Point(53.53, 13.403)
        val p2 = Point(53.53,13.403)

        // when
        val result = p1.distance(p2)

        // then
        assertEquals(0.0 , result, 0.0)
    }
}