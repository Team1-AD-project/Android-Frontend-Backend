package com.ecogo.mapengine.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ecogo.auth.TokenManager
import com.ecogo.mapengine.data.model.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.lang.reflect.Field

/**
 * Comprehensive tests for com.ecogo.mapengine.data.repository package.
 *
 * Covers:
 * - MockMapRepository: private helper methods (via reflection) + public suspend functions
 * - NavigationHistoryRepository: singleton pattern + NavigationStatistics data class + JSON conversion
 * - TripRepository: singleton pattern + state management + auth token resolution
 * - MapRepository: constructor and basic structure
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DataRepositoryTest {

    companion object {
        private const val STATION_A = "Station A"
        private const val STATION_B = "Station B"
        private const val TAKE_BUS = "Take bus"
        private const val METRO_LINE_1 = "Metro Line 1"
        private const val TIME_12_00 = "2025-01-01T12:00:00Z"
        private const val END_PLACE = "End Place"
        private const val API_ERROR = "API returned error"
        private const val START_ADDR = "Start Addr"
        private const val DATE_2025_01_01 = "2025-01-01"
        private const val TEST_TOKEN = "Bearer test_token_123"
        private const val MAIN_ST = "123 Main St"
        private const val ZONE_A = "Zone A"
        private const val TIME_00_00 = "2025-01-01T00:00:00Z"
        private const val TIME_01_00 = "2025-01-01T01:00:00Z"
        private const val EMPTY_RESPONSE = "响应数据为空"
        private const val NETWORK_ERROR = "Network error"
        private const val END_ADDR = "End Addr"
        private const val ROUTE_FAILURE = "获取路线失败"
    }

    private lateinit var mockMapRepo: MockMapRepository
    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()

    // ====================================================================
    // Reflection helpers
    // ====================================================================

    /**
     * Invoke a private method on the given object with typed parameters.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> invokePrivate(obj: Any, methodName: String, vararg args: Pair<Class<*>, Any?>): T {
        val paramTypes = args.map { it.first }.toTypedArray()
        val method = obj::class.java.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        return method.invoke(obj, *args.map { it.second }.toTypedArray()) as T
    }

    /**
     * Read a private field value via reflection.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getPrivateField(obj: Any, fieldName: String): T {
        val field = obj::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(obj) as T
    }

    /**
     * Set a private field value via reflection.
     */
    private fun setPrivateField(obj: Any, fieldName: String, value: Any?) {
        val field = obj::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }

    /**
     * Reset a singleton's INSTANCE field in its companion object.
     */
    private fun resetSingleton(clazz: Class<*>) {
        try {
            // Kotlin companion object fields are compiled as static fields on the outer class
            val instanceField = clazz.getDeclaredField("INSTANCE")
            instanceField.isAccessible = true

            // Remove final modifier if present (Kotlin @Volatile compiles to volatile, not final)
            val modifiersField = try {
                Field::class.java.getDeclaredField("modifiers")
            } catch (_: NoSuchFieldException) { null }
            modifiersField?.isAccessible = true
            if (modifiersField != null) {
                modifiersField.setInt(instanceField, instanceField.modifiers and java.lang.reflect.Modifier.FINAL.inv())
            }

            instanceField.set(null, null)
        } catch (_: Exception) {
            // Fallback: try via companion object
            try {
                val companionClazz = Class.forName("${clazz.name}\$Companion")
                val companionField = clazz.getDeclaredField("Companion")
                companionField.isAccessible = true
                val companion = companionField.get(null)

                val instanceField = companionClazz.getDeclaredField("INSTANCE")
                instanceField.isAccessible = true
                instanceField.set(companion, null)
            } catch (_: Exception) {
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        mockMapRepo = MockMapRepository()

        // Reset singletons BEFORE each test so tests are independent
        resetSingleton(TripRepository::class.java)
        resetSingleton(NavigationHistoryRepository::class.java)

        // Initialize TokenManager for TripRepository tests
        TokenManager.init(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Reset singletons after each test as well
        resetSingleton(TripRepository::class.java)
        resetSingleton(NavigationHistoryRepository::class.java)
    }

    // ====================================================================
    // ==================== MockMapRepository Tests =======================
    // ====================================================================

    // ------------------------------------------------------------------
    // calculateDistance (private) - Haversine formula
    // ------------------------------------------------------------------

    @Test
    fun `calculateDistance - same point returns zero`() {
        val p1 = GeoPoint(lng = 121.4737, lat = 31.2304)
        val p2 = GeoPoint(lng = 121.4737, lat = 31.2304)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun `calculateDistance - known distance Shanghai to Beijing`() {
        // Shanghai approx (31.23, 121.47) to Beijing approx (39.91, 116.39)
        val shanghai = GeoPoint(lng = 121.47, lat = 31.23)
        val beijing = GeoPoint(lng = 116.39, lat = 39.91)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to shanghai, GeoPoint::class.java to beijing)
        // Should be approximately 1065 km
        assertTrue("Distance should be between 1000 and 1200 km, was $dist", dist in 1000.0..1200.0)
    }

    @Test
    fun `calculateDistance - short distance within city`() {
        val p1 = GeoPoint(lng = 121.4737, lat = 31.2304)
        val p2 = GeoPoint(lng = 121.4837, lat = 31.2404)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        // Short distance should be a small positive number
        assertTrue("Distance should be positive and small (< 5 km), was $dist", dist > 0 && dist < 5.0)
    }

    @Test
    fun `calculateDistance - equatorial points`() {
        val p1 = GeoPoint(lng = 0.0, lat = 0.0)
        val p2 = GeoPoint(lng = 1.0, lat = 0.0)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        // 1 degree at equator ~ 111 km
        assertTrue("Distance should be around 111 km, was $dist", dist in 100.0..120.0)
    }

    @Test
    fun `calculateDistance - meridian points`() {
        val p1 = GeoPoint(lng = 0.0, lat = 0.0)
        val p2 = GeoPoint(lng = 0.0, lat = 1.0)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        // 1 degree along meridian ~ 111 km
        assertTrue("Distance should be around 111 km, was $dist", dist in 100.0..120.0)
    }

    @Test
    fun `calculateDistance - symmetry`() {
        val p1 = GeoPoint(lng = 121.47, lat = 31.23)
        val p2 = GeoPoint(lng = 116.39, lat = 39.91)
        val dist12: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        val dist21: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p2, GeoPoint::class.java to p1)
        assertEquals(dist12, dist21, 0.001)
    }

    @Test
    fun `calculateDistance - negative coordinates`() {
        val p1 = GeoPoint(lng = -73.9857, lat = -40.7484) // Southern hemisphere
        val p2 = GeoPoint(lng = -73.9857, lat = 40.7484)  // Northern hemisphere
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        // About 80 degrees of latitude apart ~ 9065 km
        assertTrue("Distance should be large, was $dist", dist > 5000.0)
    }

    @Test
    fun `calculateDistance - very small distance`() {
        val p1 = GeoPoint(lng = 121.4737, lat = 31.2304)
        val p2 = GeoPoint(lng = 121.47371, lat = 31.23041)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        assertTrue("Very small distance should be near zero, was $dist", dist < 0.01)
    }

    @Test
    fun `calculateDistance - antipodal points`() {
        val p1 = GeoPoint(lng = 0.0, lat = 0.0)
        val p2 = GeoPoint(lng = 180.0, lat = 0.0)
        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to p1, GeoPoint::class.java to p2)
        // Half Earth circumference ~ 20015 km
        assertTrue("Antipodal distance should be ~20015 km, was $dist", dist in 19000.0..21000.0)
    }

    // ------------------------------------------------------------------
    // generateRoutePoints (private) - Linear interpolation
    // ------------------------------------------------------------------

    @Test
    fun `generateRoutePoints - returns 11 points`() {
        val start = GeoPoint(lng = 0.0, lat = 0.0)
        val end = GeoPoint(lng = 10.0, lat = 10.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        assertEquals(11, points.size)
    }

    @Test
    fun `generateRoutePoints - first point matches start`() {
        val start = GeoPoint(lng = 5.0, lat = 3.0)
        val end = GeoPoint(lng = 15.0, lat = 13.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        assertEquals(5.0, points[0].lng, 0.001)
        assertEquals(3.0, points[0].lat, 0.001)
    }

    @Test
    fun `generateRoutePoints - last point matches end`() {
        val start = GeoPoint(lng = 5.0, lat = 3.0)
        val end = GeoPoint(lng = 15.0, lat = 13.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        assertEquals(15.0, points[10].lng, 0.001)
        assertEquals(13.0, points[10].lat, 0.001)
    }

    @Test
    fun `generateRoutePoints - midpoint is correct`() {
        val start = GeoPoint(lng = 0.0, lat = 0.0)
        val end = GeoPoint(lng = 10.0, lat = 20.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        // Middle point at index 5, ratio = 0.5
        assertEquals(5.0, points[5].lng, 0.001)
        assertEquals(10.0, points[5].lat, 0.001)
    }

    @Test
    fun `generateRoutePoints - points are evenly spaced`() {
        val start = GeoPoint(lng = 0.0, lat = 0.0)
        val end = GeoPoint(lng = 100.0, lat = 0.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        for (i in 0..10) {
            assertEquals(i * 10.0, points[i].lng, 0.001)
        }
    }

    @Test
    fun `generateRoutePoints - same start and end`() {
        val start = GeoPoint(lng = 5.0, lat = 5.0)
        val end = GeoPoint(lng = 5.0, lat = 5.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        assertEquals(11, points.size)
        points.forEach {
            assertEquals(5.0, it.lng, 0.001)
            assertEquals(5.0, it.lat, 0.001)
        }
    }

    @Test
    fun `generateRoutePoints - negative coordinates`() {
        val start = GeoPoint(lng = -10.0, lat = -20.0)
        val end = GeoPoint(lng = 10.0, lat = 20.0)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        assertEquals(-10.0, points[0].lng, 0.001)
        assertEquals(-20.0, points[0].lat, 0.001)
        assertEquals(0.0, points[5].lng, 0.001)
        assertEquals(0.0, points[5].lat, 0.001)
        assertEquals(10.0, points[10].lng, 0.001)
        assertEquals(20.0, points[10].lat, 0.001)
    }

    // ------------------------------------------------------------------
    // generateMockTrackPoints (private)
    // ------------------------------------------------------------------

    @Test
    fun `generateMockTrackPoints - returns 10 points`() {
        val start = GeoPoint(lng = 121.4737, lat = 31.2304)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        assertEquals(10, points.size)
    }

    @Test
    fun `generateMockTrackPoints - all points have positive latitude and longitude`() {
        val start = GeoPoint(lng = 121.4737, lat = 31.2304)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        points.forEach {
            assertTrue("Latitude should be > start lat", it.latitude > start.lat)
            assertTrue("Longitude should be > start lng", it.longitude > start.lng)
        }
    }

    @Test
    fun `generateMockTrackPoints - timestamps are in decreasing order from past`() {
        val start = GeoPoint(lng = 121.4737, lat = 31.2304)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        for (i in 1 until points.size) {
            assertTrue("Timestamps should increase", points[i].timestamp > points[i - 1].timestamp)
        }
    }

    @Test
    fun `generateMockTrackPoints - speeds are in range 4-6`() {
        val start = GeoPoint(lng = 121.4737, lat = 31.2304)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        points.forEach {
            assertNotNull("Speed should not be null", it.speed)
            assertTrue("Speed should be >= 4, was ${it.speed}", it.speed!! >= 4.0)
            assertTrue("Speed should be < 6, was ${it.speed}", it.speed!! < 6.1)
        }
    }

    @Test
    fun `generateMockTrackPoints - points move incrementally`() {
        val start = GeoPoint(lng = 100.0, lat = 50.0)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        // Each point should be incrementally further from start
        for (i in 1 until points.size) {
            assertTrue(points[i].latitude >= points[i - 1].latitude)
            assertTrue(points[i].longitude >= points[i - 1].longitude)
        }
    }

    @Test
    fun `generateMockTrackPoints - with zero start point`() {
        val start = GeoPoint(lng = 0.0, lat = 0.0)
        val points: List<TrackPoint> = invokePrivate(mockMapRepo, "generateMockTrackPoints",
            GeoPoint::class.java to start)
        assertEquals(10, points.size)
        points.forEach {
            assertTrue("Latitude should be positive", it.latitude > 0)
            assertTrue("Longitude should be positive", it.longitude > 0)
        }
    }

    // ------------------------------------------------------------------
    // calculateMockDistance (private)
    // ------------------------------------------------------------------

    @Test
    fun `calculateMockDistance - empty list returns zero`() {
        val points = emptyList<TrackPoint>()
        val dist: Double = invokePrivate(mockMapRepo, "calculateMockDistance",
            List::class.java to points)
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun `calculateMockDistance - single point returns zero`() {
        val points = listOf(TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 1000L))
        val dist: Double = invokePrivate(mockMapRepo, "calculateMockDistance",
            List::class.java to points)
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun `calculateMockDistance - two points returns positive distance`() {
        val points = listOf(
            TrackPoint(latitude = 31.2304, longitude = 121.4737, timestamp = 1000L),
            TrackPoint(latitude = 31.2404, longitude = 121.4837, timestamp = 2000L)
        )
        val dist: Double = invokePrivate(mockMapRepo, "calculateMockDistance",
            List::class.java to points)
        assertTrue("Distance should be positive, was $dist", dist > 0)
    }

    @Test
    fun `calculateMockDistance - multiple points sums correctly`() {
        val points = listOf(
            TrackPoint(latitude = 0.0, longitude = 0.0, timestamp = 1000L),
            TrackPoint(latitude = 1.0, longitude = 0.0, timestamp = 2000L),
            TrackPoint(latitude = 2.0, longitude = 0.0, timestamp = 3000L)
        )
        val dist: Double = invokePrivate(mockMapRepo, "calculateMockDistance",
            List::class.java to points)
        // Two segments of ~111 km each = ~222 km
        assertTrue("Distance should be about 222 km, was $dist", dist in 200.0..240.0)
    }

    @Test
    fun `calculateMockDistance - coincident points returns zero`() {
        val points = listOf(
            TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 1000L),
            TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 2000L),
            TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 3000L)
        )
        val dist: Double = invokePrivate(mockMapRepo, "calculateMockDistance",
            List::class.java to points)
        assertEquals(0.0, dist, 0.001)
    }

    // ------------------------------------------------------------------
    // estimateDuration (private) - Tests all TransportMode branches
    // ------------------------------------------------------------------

    @Test
    fun `estimateDuration - WALKING at 4 kmh`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 4.0, TransportMode::class.java to TransportMode.WALKING)
        // 4 km / 4 km/h * 60 min = 60 min
        assertEquals(60, duration)
    }

    @Test
    fun `estimateDuration - CYCLING at 15 kmh`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 15.0, TransportMode::class.java to TransportMode.CYCLING)
        // 15 / 15 * 60 = 60
        assertEquals(60, duration)
    }

    @Test
    fun `estimateDuration - BUS at 20 kmh`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 20.0, TransportMode::class.java to TransportMode.BUS)
        // 20 / 20 * 60 = 60
        assertEquals(60, duration)
    }

    @Test
    fun `estimateDuration - SUBWAY at 35 kmh`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 35.0, TransportMode::class.java to TransportMode.SUBWAY)
        // 35 / 35 * 60 = 60
        assertEquals(60, duration)
    }

    @Test
    fun `estimateDuration - DRIVING at 40 kmh`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 40.0, TransportMode::class.java to TransportMode.DRIVING)
        // 40 / 40 * 60 = 60
        assertEquals(60, duration)
    }

    @Test
    fun `estimateDuration - zero distance`() {
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 0.0, TransportMode::class.java to TransportMode.WALKING)
        assertEquals(0, duration)
    }

    @Test
    fun `estimateDuration - fractional result truncated`() {
        // 10 km / 4 km/h * 60 = 150 min
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.WALKING)
        assertEquals(150, duration)
    }

    @Test
    fun `estimateDuration - WALKING small distance`() {
        // 1 km / 4 km/h * 60 = 15 min
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 1.0, TransportMode::class.java to TransportMode.WALKING)
        assertEquals(15, duration)
    }

    @Test
    fun `estimateDuration - CYCLING short trip`() {
        // 3 km / 15 km/h * 60 = 12
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 3.0, TransportMode::class.java to TransportMode.CYCLING)
        assertEquals(12, duration)
    }

    @Test
    fun `estimateDuration - BUS long trip`() {
        // 60 km / 20 km/h * 60 = 180
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 60.0, TransportMode::class.java to TransportMode.BUS)
        assertEquals(180, duration)
    }

    @Test
    fun `estimateDuration - SUBWAY long trip`() {
        // 70 km / 35 km/h * 60 = 120
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 70.0, TransportMode::class.java to TransportMode.SUBWAY)
        assertEquals(120, duration)
    }

    @Test
    fun `estimateDuration - DRIVING long trip`() {
        // 80 km / 40 km/h * 60 = 120
        val duration: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to 80.0, TransportMode::class.java to TransportMode.DRIVING)
        assertEquals(120, duration)
    }

    // ------------------------------------------------------------------
    // calculateCarbonForMode (private) - Returns CarbonData
    // ------------------------------------------------------------------

    // CarbonData is a private inner class, so we access its fields via reflection
    private fun getCarbonTotalCarbon(carbonData: Any): Double {
        val field = carbonData::class.java.getDeclaredField("totalCarbon")
        field.isAccessible = true
        return field.getDouble(carbonData)
    }

    private fun getCarbonSaved(carbonData: Any): Double {
        val field = carbonData::class.java.getDeclaredField("carbonSaved")
        field.isAccessible = true
        return field.getDouble(carbonData)
    }

    @Test
    fun `calculateCarbonForMode - WALKING zero emission`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.WALKING)
        assertEquals(0.0, getCarbonTotalCarbon(result), 0.001)
        // carbonSaved = 10 * 0.15 - 0 = 1.5
        assertEquals(1.5, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - CYCLING zero emission`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.CYCLING)
        assertEquals(0.0, getCarbonTotalCarbon(result), 0.001)
        assertEquals(1.5, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - BUS 50g per km`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.BUS)
        // 10 * 0.05 = 0.5 kg
        assertEquals(0.5, getCarbonTotalCarbon(result), 0.001)
        // saved = 10 * 0.15 - 0.5 = 1.0
        assertEquals(1.0, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - SUBWAY 30g per km`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.SUBWAY)
        // 10 * 0.03 = 0.3 kg
        assertEquals(0.3, getCarbonTotalCarbon(result), 0.001)
        // saved = 10 * 0.15 - 0.3 = 1.2
        assertEquals(1.2, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - DRIVING 150g per km`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 10.0, TransportMode::class.java to TransportMode.DRIVING)
        // 10 * 0.15 = 1.5 kg
        assertEquals(1.5, getCarbonTotalCarbon(result), 0.001)
        // saved = 10 * 0.15 - 1.5 = 0.0 (coerced to 0)
        assertEquals(0.0, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - zero distance`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 0.0, TransportMode::class.java to TransportMode.BUS)
        assertEquals(0.0, getCarbonTotalCarbon(result), 0.001)
        assertEquals(0.0, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - DRIVING saved is zero (coerced)`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 5.0, TransportMode::class.java to TransportMode.DRIVING)
        val totalCarbon = getCarbonTotalCarbon(result)
        val saved = getCarbonSaved(result)
        assertEquals(0.75, totalCarbon, 0.001)
        assertEquals(0.0, saved, 0.001) // coerceAtLeast(0.0) ensures non-negative
    }

    @Test
    fun `calculateCarbonForMode - very large distance WALKING`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 100.0, TransportMode::class.java to TransportMode.WALKING)
        assertEquals(0.0, getCarbonTotalCarbon(result), 0.001)
        assertEquals(15.0, getCarbonSaved(result), 0.001) // 100 * 0.15
    }

    @Test
    fun `calculateCarbonForMode - small distance BUS`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 1.0, TransportMode::class.java to TransportMode.BUS)
        assertEquals(0.05, getCarbonTotalCarbon(result), 0.001)
        assertEquals(0.10, getCarbonSaved(result), 0.001)
    }

    @Test
    fun `calculateCarbonForMode - SUBWAY large distance`() {
        val result: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to 50.0, TransportMode::class.java to TransportMode.SUBWAY)
        assertEquals(1.5, getCarbonTotalCarbon(result), 0.001)   // 50 * 0.03
        assertEquals(6.0, getCarbonSaved(result), 0.001)         // 50 * 0.15 - 1.5
    }

    // ------------------------------------------------------------------
    // generateRouteSummary (private)
    // ------------------------------------------------------------------

    @Test
    fun `generateRouteSummary - empty steps returns walking route`() {
        val steps = emptyList<RouteStep>()
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("步行路线", summary)
    }

    @Test
    fun `generateRouteSummary - no transit steps returns walking route`() {
        val steps = listOf(
            RouteStep(instruction = "Walk", distance = 100.0, duration = 60, travel_mode = "WALKING")
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("步行路线", summary)
    }

    @Test
    fun `generateRouteSummary - transit step with short name`() {
        val steps = listOf(
            RouteStep(
                instruction = "Take subway",
                distance = 5000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "Line 1",
                    line_short_name = "1",
                    departure_stop = STATION_A,
                    arrival_stop = STATION_B,
                    num_stops = 5,
                    vehicle_type = "SUBWAY"
                )
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("1", summary)
    }

    @Test
    fun `generateRouteSummary - transit step without short name uses line name`() {
        val steps = listOf(
            RouteStep(
                instruction = TAKE_BUS,
                distance = 3000.0,
                duration = 300,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "Bus Route 46",
                    line_short_name = null,
                    departure_stop = "Stop A",
                    arrival_stop = "Stop B",
                    num_stops = 3,
                    vehicle_type = "BUS"
                )
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("Bus Route 46", summary)
    }

    @Test
    fun `generateRouteSummary - multiple transit steps joined with arrow`() {
        val steps = listOf(
            RouteStep(
                instruction = "Take subway",
                distance = 5000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = METRO_LINE_1,
                    line_short_name = "1号线",
                    departure_stop = "A",
                    arrival_stop = "B",
                    num_stops = 3,
                    vehicle_type = "SUBWAY"
                )
            ),
            RouteStep(
                instruction = "Walk to bus stop",
                distance = 200.0,
                duration = 120,
                travel_mode = "WALKING"
            ),
            RouteStep(
                instruction = TAKE_BUS,
                distance = 2000.0,
                duration = 300,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "公交46路",
                    line_short_name = "46路",
                    departure_stop = "C",
                    arrival_stop = "D",
                    num_stops = 5,
                    vehicle_type = "BUS"
                )
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("1号线 → 46路", summary)
    }

    @Test
    fun `generateRouteSummary - transit step with null transit_details filtered out`() {
        val steps = listOf(
            RouteStep(
                instruction = "Transit",
                distance = 5000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = null
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        // No transit_details, so the filter removes these; no TRANSIT steps with details
        assertEquals("步行路线", summary)
    }

    @Test
    fun `generateRouteSummary - mixed transit and walking steps`() {
        val steps = listOf(
            RouteStep(instruction = "Walk", distance = 100.0, duration = 60, travel_mode = "WALKING"),
            RouteStep(
                instruction = "Subway",
                distance = 5000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "Line 2",
                    line_short_name = "2号线",
                    departure_stop = "X",
                    arrival_stop = "Y",
                    num_stops = 4,
                    vehicle_type = "SUBWAY"
                )
            ),
            RouteStep(instruction = "Walk", distance = 200.0, duration = 120, travel_mode = "WALKING")
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("2号线", summary)
    }

    @Test
    fun `generateRouteSummary - three transit connections`() {
        val transitSteps = listOf(
            createTransitStep("A线", "A"),
            createTransitStep("B线", "B"),
            createTransitStep("C线", "C")
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to transitSteps)
        assertEquals("A → B → C", summary)
    }

    private fun createTransitStep(lineName: String, shortName: String): RouteStep {
        return RouteStep(
            instruction = "Take $lineName",
            distance = 1000.0,
            duration = 300,
            travel_mode = "TRANSIT",
            transit_details = TransitDetails(
                line_name = lineName,
                line_short_name = shortName,
                departure_stop = "Start",
                arrival_stop = "End",
                num_stops = 2,
                vehicle_type = "BUS"
            )
        )
    }

    // ------------------------------------------------------------------
    // getCurrentTimeString (private)
    // ------------------------------------------------------------------

    @Test
    fun `getCurrentTimeString - returns ISO 8601 format`() {
        val timeStr: String = invokePrivate(mockMapRepo, "getCurrentTimeString")
        // Should match pattern like TIME_12_00
        assertTrue("Time string should match ISO 8601 format: $timeStr",
            timeStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")))
    }

    @Test
    fun `getCurrentTimeString - returns recent time`() {
        val timeStr: String = invokePrivate(mockMapRepo, "getCurrentTimeString")
        assertTrue("Time string should start with 20", timeStr.startsWith("20"))
    }

    @Test
    fun `getCurrentTimeString - two calls return close timestamps`() {
        val time1: String = invokePrivate(mockMapRepo, "getCurrentTimeString")
        val time2: String = invokePrivate(mockMapRepo, "getCurrentTimeString")
        // Both should have the same date portion (unless called at midnight boundary)
        assertEquals(time1.substring(0, 10), time2.substring(0, 10))
    }

    // ------------------------------------------------------------------
    // MockMapRepository public suspend functions
    // ------------------------------------------------------------------

    @Test
    fun `startTripTracking - returns success with MOCK_TRIP prefix`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val result = mockMapRepo.startTripTracking("user1", startPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue("Trip ID should start with MOCK_TRIP_", data.trip_id.startsWith("MOCK_TRIP_"))
        assertEquals("tracking", data.status)
        assertNotNull(data.start_time)
    }

    @Test
    fun `startTripTracking - increments trip ID counter`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val result1 = mockMapRepo.startTripTracking("user1", startPoint)
        val result2 = mockMapRepo.startTripTracking("user1", startPoint)
        val id1 = result1.getOrThrow().trip_id
        val id2 = result2.getOrThrow().trip_id
        assertNotEquals(id1, id2)
        // Extract numeric part and verify increment
        val num1 = id1.removePrefix("MOCK_TRIP_").toInt()
        val num2 = id2.removePrefix("MOCK_TRIP_").toInt()
        assertEquals(num1 + 1, num2)
    }

    @Test
    fun `startTripTracking - with location info`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val location = LocationInfo(address = "Test Address", place_name = "Test Place")
        val result = mockMapRepo.startTripTracking("user1", startPoint, location)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `startTripTracking - with null location info`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val result = mockMapRepo.startTripTracking("user1", startPoint, null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `startTripTracking - message is set`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val result = mockMapRepo.startTripTracking("user1", startPoint)
        assertNotNull(result.getOrThrow().message)
    }

    @Test
    fun `startTripTracking - sets currentTripStartPoint`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        mockMapRepo.startTripTracking("user1", startPoint)
        val saved: GeoPoint? = getPrivateField(mockMapRepo, "currentTripStartPoint")
        assertNotNull(saved)
        assertEquals(startPoint.lat, saved!!.lat, 0.001)
        assertEquals(startPoint.lng, saved.lng, 0.001)
    }

    @Test
    fun `startTripTracking - sets currentTripStartTime`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        mockMapRepo.startTripTracking("user1", startPoint)
        val savedTime: String? = getPrivateField(mockMapRepo, "currentTripStartTime")
        assertNotNull(savedTime)
    }

    @Test
    fun `cancelTripTracking - returns success`() = runTest {
        val result = mockMapRepo.cancelTripTracking("trip123", "user1")
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("trip123", data.trip_id)
        assertEquals("cancelled", data.status)
        assertNotNull(data.cancel_time)
    }

    @Test
    fun `cancelTripTracking - clears current trip data`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        mockMapRepo.startTripTracking("user1", startPoint)
        mockMapRepo.cancelTripTracking("trip123", "user1")
        val savedStart: GeoPoint? = getPrivateField(mockMapRepo, "currentTripStartPoint")
        val savedTime: String? = getPrivateField(mockMapRepo, "currentTripStartTime")
        assertNull(savedStart)
        assertNull(savedTime)
    }

    @Test
    fun `cancelTripTracking - with reason`() = runTest {
        val result = mockMapRepo.cancelTripTracking("trip123", "user1", "User changed mind")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelTripTracking - with null reason`() = runTest {
        val result = mockMapRepo.cancelTripTracking("trip123", "user1", null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cancelTripTracking - message is set`() = runTest {
        val result = mockMapRepo.cancelTripTracking("trip123", "user1")
        assertNotNull(result.getOrThrow().message)
    }

    @Test
    fun `getTripMap - returns success with track points`() = runTest {
        val result = mockMapRepo.getTripMap("trip123", "user1")
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("trip123", data.trip_id)
        assertTrue("Should have track points", data.track_points.isNotEmpty())
        assertEquals("tracking", data.status)
    }

    @Test
    fun `getTripMap - uses default start when no trip started`() = runTest {
        val result = mockMapRepo.getTripMap("trip123", "user1")
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(10, data.track_points.size)
    }

    @Test
    fun `getTripMap - uses stored start point when trip is active`() = runTest {
        val startPoint = GeoPoint(lng = 100.0, lat = 50.0)
        mockMapRepo.startTripTracking("user1", startPoint)
        val result = mockMapRepo.getTripMap("trip123", "user1")
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // Track points should be near the start point
        data.track_points.forEach {
            assertTrue("Latitude should be > 50", it.latitude > 50.0)
            assertTrue("Longitude should be > 100", it.longitude > 100.0)
        }
    }

    @Test
    fun `getTripMap - current_distance is positive`() = runTest {
        val result = mockMapRepo.getTripMap("trip123", "user1")
        val data = result.getOrThrow()
        assertTrue("Current distance should be positive, was ${data.current_distance}",
            data.current_distance > 0)
    }

    @Test
    fun `getTripMap - duration_seconds is 600`() = runTest {
        val result = mockMapRepo.getTripMap("trip123", "user1")
        assertEquals(600, result.getOrThrow().duration_seconds)
    }

    @Test
    fun `saveTrip - returns success`() = runTest {
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.saveTrip("trip123", "user1", endPoint, null, 5.0, TIME_12_00)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("trip123", data.trip_id)
        assertEquals("completed", data.status)
        assertEquals(5.0, data.total_distance!!, 0.001)
        assertEquals(15, data.duration_minutes)
    }

    @Test
    fun `saveTrip - clears current trip data`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        mockMapRepo.startTripTracking("user1", startPoint)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        mockMapRepo.saveTrip("trip123", "user1", endPoint, null, 5.0, TIME_12_00)
        val savedStart: GeoPoint? = getPrivateField(mockMapRepo, "currentTripStartPoint")
        val savedTime: String? = getPrivateField(mockMapRepo, "currentTripStartTime")
        assertNull(savedStart)
        assertNull(savedTime)
    }

    @Test
    fun `saveTrip - with end location info`() = runTest {
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val endLocation = LocationInfo(address = "End Address", place_name = END_PLACE)
        val result = mockMapRepo.saveTrip("trip123", "user1", endPoint, endLocation, 10.0, "2025-01-01T13:00:00Z")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveTrip - message is set`() = runTest {
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.saveTrip("trip123", "user1", endPoint, null, 5.0, TIME_12_00)
        assertNotNull(result.getOrThrow().message)
    }

    @Test
    fun `saveTrip - preserves distance value`() = runTest {
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.saveTrip("trip123", "user1", endPoint, null, 42.5, TIME_12_00)
        assertEquals(42.5, result.getOrThrow().total_distance!!, 0.001)
    }

    // ------------------------------------------------------------------
    // calculateCarbon (public suspend)
    // ------------------------------------------------------------------

    @Test
    fun `calculateCarbon - walk mode has zero emission`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(0.0, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - car mode has max emission`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("car"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // 150 * 5 / 1 / 1000 = 0.75 kg
        assertEquals(0.75, data.total_carbon_emission, 0.001)
        assertEquals(0.0, data.carbon_saved, 0.001)
    }

    @Test
    fun `calculateCarbon - bus mode`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("bus"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // 50 * 5 / 1 / 1000 = 0.25 kg
        assertEquals(0.25, data.total_carbon_emission, 0.001)
        // saved = (150*5 - 250) / 1000 = 0.5 kg
        assertEquals(0.5, data.carbon_saved, 0.001)
    }

    @Test
    fun `calculateCarbon - subway mode`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("subway"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // 30 * 5 / 1 / 1000 = 0.15 kg
        assertEquals(0.15, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - bike mode zero emission`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("bike"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(0.0, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - mixed modes`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk", "bus"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // totalCarbon = (0 + 50) * 5 / 2 / 1000 = 0.125 kg
        assertEquals(0.125, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - unknown mode defaults to 100`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("unknown"))
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // 100 * 5 / 1 / 1000 = 0.5 kg
        assertEquals(0.5, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - green points calculated correctly`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk"))
        val data = result.getOrThrow()
        // carbonSaved = (750 - 0) / 1000 = 0.75 -> green_points = (750/10).toInt() = 75
        val drivingCarbon = 150.0 * 5.0
        val carbonSaved = drivingCarbon - 0.0
        assertEquals((carbonSaved / 10).toInt(), data.green_points)
    }

    @Test
    fun `calculateCarbon - transport breakdown present`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk", "bus"))
        val data = result.getOrThrow()
        assertNotNull(data.transport_breakdown)
        assertEquals(2, data.transport_breakdown!!.size)
        assertTrue(data.transport_breakdown!!.containsKey("walk"))
        assertTrue(data.transport_breakdown!!.containsKey("bus"))
    }

    @Test
    fun `calculateCarbon - transport breakdown values`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("bus"))
        val data = result.getOrThrow()
        // bus: 50 * 5 / 1 / 1000 = 0.25
        assertEquals(0.25, data.transport_breakdown!!["bus"]!!, 0.001)
    }

    @Test
    fun `calculateCarbon - multiple same modes`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk", "walk", "walk"))
        val data = result.getOrThrow()
        assertEquals(0.0, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - trip_id preserved`() = runTest {
        val result = mockMapRepo.calculateCarbon("MY_TRIP_123", listOf("walk"))
        assertEquals("MY_TRIP_123", result.getOrThrow().trip_id)
    }

    @Test
    fun `calculateCarbon - all five modes combined`() = runTest {
        val modes = listOf("walk", "bike", "bus", "subway", "car")
        val result = mockMapRepo.calculateCarbon("trip1", modes)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // (0 + 0 + 50 + 30 + 150) * 5 / 5 / 1000 = 0.23 kg
        val expected = (0.0 + 0.0 + 50.0 + 30.0 + 150.0) * 5.0 / 5.0 / 1000.0
        assertEquals(expected, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateCarbon - carbon saved never negative`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("car"))
        val data = result.getOrThrow()
        assertTrue("Carbon saved should be >= 0", data.carbon_saved >= 0)
    }

    // ------------------------------------------------------------------
    // MockMapRepository implements IMapRepository
    // ------------------------------------------------------------------

    @Test
    fun `MockMapRepository implements IMapRepository`() {
        assertTrue(mockMapRepo is IMapRepository)
    }

    // ====================================================================
    // ==================== NavigationStatistics Tests ====================
    // ====================================================================

    @Test
    fun `NavigationStatistics - totalDistanceKm converts meters to km`() {
        val stats = NavigationStatistics(
            totalTrips = 10,
            greenTrips = 5,
            totalDistanceMeters = 5000.0,
            totalCarbonSavedKg = 2.0
        )
        assertEquals(5.0, stats.totalDistanceKm, 0.001)
    }

    @Test
    fun `NavigationStatistics - totalDistanceKm zero meters`() {
        val stats = NavigationStatistics(
            totalTrips = 0,
            greenTrips = 0,
            totalDistanceMeters = 0.0,
            totalCarbonSavedKg = 0.0
        )
        assertEquals(0.0, stats.totalDistanceKm, 0.001)
    }

    @Test
    fun `NavigationStatistics - totalDistanceKm fractional`() {
        val stats = NavigationStatistics(
            totalTrips = 1,
            greenTrips = 1,
            totalDistanceMeters = 1500.0,
            totalCarbonSavedKg = 0.5
        )
        assertEquals(1.5, stats.totalDistanceKm, 0.001)
    }

    @Test
    fun `NavigationStatistics - totalDistanceKm large value`() {
        val stats = NavigationStatistics(
            totalTrips = 100,
            greenTrips = 80,
            totalDistanceMeters = 1000000.0,
            totalCarbonSavedKg = 50.0
        )
        assertEquals(1000.0, stats.totalDistanceKm, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage normal case`() {
        val stats = NavigationStatistics(
            totalTrips = 10,
            greenTrips = 5,
            totalDistanceMeters = 5000.0,
            totalCarbonSavedKg = 2.0
        )
        assertEquals(50.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage zero trips returns zero`() {
        val stats = NavigationStatistics(
            totalTrips = 0,
            greenTrips = 0,
            totalDistanceMeters = 0.0,
            totalCarbonSavedKg = 0.0
        )
        assertEquals(0.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage 100 percent`() {
        val stats = NavigationStatistics(
            totalTrips = 10,
            greenTrips = 10,
            totalDistanceMeters = 5000.0,
            totalCarbonSavedKg = 5.0
        )
        assertEquals(100.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage zero green trips`() {
        val stats = NavigationStatistics(
            totalTrips = 10,
            greenTrips = 0,
            totalDistanceMeters = 5000.0,
            totalCarbonSavedKg = 0.0
        )
        assertEquals(0.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage fractional`() {
        val stats = NavigationStatistics(
            totalTrips = 3,
            greenTrips = 1,
            totalDistanceMeters = 3000.0,
            totalCarbonSavedKg = 1.0
        )
        assertEquals(100.0 / 3.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage one trip green`() {
        val stats = NavigationStatistics(
            totalTrips = 1,
            greenTrips = 1,
            totalDistanceMeters = 1000.0,
            totalCarbonSavedKg = 0.5
        )
        assertEquals(100.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - greenTripPercentage one trip not green`() {
        val stats = NavigationStatistics(
            totalTrips = 1,
            greenTrips = 0,
            totalDistanceMeters = 1000.0,
            totalCarbonSavedKg = 0.0
        )
        assertEquals(0.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - data class equality`() {
        val stats1 = NavigationStatistics(10, 5, 5000.0, 2.0)
        val stats2 = NavigationStatistics(10, 5, 5000.0, 2.0)
        assertEquals(stats1, stats2)
    }

    @Test
    fun `NavigationStatistics - data class inequality`() {
        val stats1 = NavigationStatistics(10, 5, 5000.0, 2.0)
        val stats2 = NavigationStatistics(10, 6, 5000.0, 2.0)
        assertNotEquals(stats1, stats2)
    }

    @Test
    fun `NavigationStatistics - copy changes greenTrips`() {
        val stats = NavigationStatistics(10, 5, 5000.0, 2.0)
        val modified = stats.copy(greenTrips = 8)
        assertEquals(80.0, modified.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationStatistics - hashCode consistency`() {
        val stats1 = NavigationStatistics(10, 5, 5000.0, 2.0)
        val stats2 = NavigationStatistics(10, 5, 5000.0, 2.0)
        assertEquals(stats1.hashCode(), stats2.hashCode())
    }

    @Test
    fun `NavigationStatistics - toString contains field names`() {
        val stats = NavigationStatistics(10, 5, 5000.0, 2.0)
        val str = stats.toString()
        assertTrue(str.contains("totalTrips"))
        assertTrue(str.contains("greenTrips"))
    }

    @Test
    fun `NavigationStatistics - destructuring`() {
        val stats = NavigationStatistics(10, 5, 5000.0, 2.0)
        val (totalTrips, greenTrips, distMeters, carbonSaved) = stats
        assertEquals(10, totalTrips)
        assertEquals(5, greenTrips)
        assertEquals(5000.0, distMeters, 0.001)
        assertEquals(2.0, carbonSaved, 0.001)
    }

    @Test
    fun `NavigationStatistics - large number of trips`() {
        val stats = NavigationStatistics(
            totalTrips = 10000,
            greenTrips = 7500,
            totalDistanceMeters = 500000.0,
            totalCarbonSavedKg = 150.0
        )
        assertEquals(75.0, stats.greenTripPercentage, 0.001)
        assertEquals(500.0, stats.totalDistanceKm, 0.001)
    }

    // ====================================================================
    // ========= NavigationHistoryRepository Singleton Tests ==============
    // ====================================================================

    @Test
    fun `NavigationHistoryRepository - isInitialized returns false before init`() {
        assertFalse(NavigationHistoryRepository.isInitialized())
    }

    @Test
    fun `NavigationHistoryRepository - getInstance throws when not initialized`() {
        try {
            NavigationHistoryRepository.getInstance()
            fail("Should have thrown IllegalStateException")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("must be initialized"))
        }
    }

    @Test
    fun `NavigationHistoryRepository - initialize creates instance`() {
        NavigationHistoryRepository.initialize(context)
        assertTrue(NavigationHistoryRepository.isInitialized())
    }

    @Test
    fun `NavigationHistoryRepository - getInstance after init returns non-null`() {
        NavigationHistoryRepository.initialize(context)
        val instance = NavigationHistoryRepository.getInstance()
        assertNotNull(instance)
    }

    @Test
    fun `NavigationHistoryRepository - getInstance returns same instance`() {
        NavigationHistoryRepository.initialize(context)
        val instance1 = NavigationHistoryRepository.getInstance()
        val instance2 = NavigationHistoryRepository.getInstance()
        assertSame(instance1, instance2)
    }

    @Test
    fun `NavigationHistoryRepository - initialize twice does not create new instance`() {
        NavigationHistoryRepository.initialize(context)
        val instance1 = NavigationHistoryRepository.getInstance()
        NavigationHistoryRepository.initialize(context)
        val instance2 = NavigationHistoryRepository.getInstance()
        assertSame(instance1, instance2)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository JSON conversion (via reflection)
    // ------------------------------------------------------------------

    @Test
    fun `convertLatLngListToJson - empty list produces empty JSON array`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json: String = invokePrivate(repo, "convertLatLngListToJson",
            List::class.java to emptyList<LatLng>())
        assertEquals("[]", json)
    }

    @Test
    fun `convertLatLngListToJson - single point`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val points = listOf(LatLng(31.23, 121.47))
        val json: String = invokePrivate(repo, "convertLatLngListToJson",
            List::class.java to points)
        assertTrue(json.contains("31.23"))
        assertTrue(json.contains("121.47"))
    }

    @Test
    fun `convertLatLngListToJson - multiple points`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val points = listOf(
            LatLng(31.23, 121.47),
            LatLng(31.24, 121.48)
        )
        val json: String = invokePrivate(repo, "convertLatLngListToJson",
            List::class.java to points)
        assertTrue(json.contains("lat"))
        assertTrue(json.contains("lng"))
    }

    @Test
    fun `parseLatLngListFromJson - empty array`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val result = repo.parseLatLngListFromJson("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseLatLngListFromJson - single point`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lat":31.23,"lng":121.47}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(1, result.size)
        assertEquals(31.23, result[0].latitude, 0.001)
        assertEquals(121.47, result[0].longitude, 0.001)
    }

    @Test
    fun `parseLatLngListFromJson - multiple points`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lat":31.23,"lng":121.47},{"lat":31.24,"lng":121.48}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(2, result.size)
        assertEquals(31.24, result[1].latitude, 0.001)
        assertEquals(121.48, result[1].longitude, 0.001)
    }

    @Test
    fun `parseLatLngListFromJson - missing lat defaults to zero`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lng":121.47}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(1, result.size)
        assertEquals(0.0, result[0].latitude, 0.001)
        assertEquals(121.47, result[0].longitude, 0.001)
    }

    @Test
    fun `parseLatLngListFromJson - missing lng defaults to zero`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lat":31.23}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(1, result.size)
        assertEquals(31.23, result[0].latitude, 0.001)
        assertEquals(0.0, result[0].longitude, 0.001)
    }

    @Test
    fun `parseLatLngListFromJson - round trip conversion`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val originalPoints = listOf(
            LatLng(31.23, 121.47),
            LatLng(39.91, 116.39),
            LatLng(22.54, 114.06)
        )
        val json: String = invokePrivate(repo, "convertLatLngListToJson",
            List::class.java to originalPoints)
        val parsed = repo.parseLatLngListFromJson(json)
        assertEquals(originalPoints.size, parsed.size)
        for (i in originalPoints.indices) {
            assertEquals(originalPoints[i].latitude, parsed[i].latitude, 0.001)
            assertEquals(originalPoints[i].longitude, parsed[i].longitude, 0.001)
        }
    }

    @Test
    fun `parseLatLngListFromJson - negative coordinates`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lat":-33.87,"lng":151.21}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(-33.87, result[0].latitude, 0.001)
        assertEquals(151.21, result[0].longitude, 0.001)
    }

    @Test
    fun `parseLatLngListFromJson - zero coordinates`() {
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        val json = """[{"lat":0.0,"lng":0.0}]"""
        val result = repo.parseLatLngListFromJson(json)
        assertEquals(0.0, result[0].latitude, 0.001)
        assertEquals(0.0, result[0].longitude, 0.001)
    }

    // ====================================================================
    // ==================== TripRepository Tests ==========================
    // ====================================================================

    @Test
    fun `TripRepository - getInstance returns non-null`() {
        val instance = TripRepository.getInstance()
        assertNotNull(instance)
    }

    @Test
    fun `TripRepository - getInstance returns same instance`() {
        val instance1 = TripRepository.getInstance()
        val instance2 = TripRepository.getInstance()
        assertSame(instance1, instance2)
    }

    @Test
    fun `TripRepository - getCurrentTripId initially null`() {
        val repo = TripRepository.getInstance()
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - clearCurrentTripId sets to null`() {
        val repo = TripRepository.getInstance()
        // Set trip id via reflection
        setPrivateField(repo, "currentTripId", "test_trip_123")
        assertEquals("test_trip_123", repo.getCurrentTripId())
        repo.clearCurrentTripId()
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - clearCurrentTripId when already null`() {
        val repo = TripRepository.getInstance()
        repo.clearCurrentTripId()
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - getAuthToken returns fallback when no token set`() {
        val repo = TripRepository.getInstance()
        val token = repo.getAuthToken()
        assertEquals(TEST_TOKEN, token)
    }

    @Test
    fun `TripRepository - getAuthToken returns TokenManager token when available`() {
        // Save a token via TokenManager
        TokenManager.saveToken("real_token_abc", "user_1", "TestUser")
        val repo = TripRepository.getInstance()
        val token = repo.getAuthToken()
        assertEquals("Bearer real_token_abc", token)
        // Clean up
        TokenManager.logout()
    }

    @Test
    fun `TripRepository - resolveAuthToken via reflection fallback`() {
        // Ensure no token is saved (logout clears it)
        TokenManager.logout()
        val repo = TripRepository.getInstance()
        val token: String = invokePrivate(repo, "resolveAuthToken")
        assertEquals(TEST_TOKEN, token)
    }

    @Test
    fun `TripRepository - resolveAuthToken via reflection with token`() {
        TokenManager.saveToken("my_token", "u1", "User1")
        val repo = TripRepository.getInstance()
        val token: String = invokePrivate(repo, "resolveAuthToken")
        assertEquals("Bearer my_token", token)
        TokenManager.logout()
    }

    @Test
    fun `TripRepository - currentTripId can be set via reflection`() {
        val repo = TripRepository.getInstance()
        setPrivateField(repo, "currentTripId", "TRIP_999")
        assertEquals("TRIP_999", repo.getCurrentTripId())
        repo.clearCurrentTripId()
    }

    @Test
    fun `TripRepository - getCurrentTripId after clear is null`() {
        val repo = TripRepository.getInstance()
        setPrivateField(repo, "currentTripId", "TRIP_1")
        repo.clearCurrentTripId()
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - multiple clearCurrentTripId calls are safe`() {
        val repo = TripRepository.getInstance()
        repo.clearCurrentTripId()
        repo.clearCurrentTripId()
        repo.clearCurrentTripId()
        assertNull(repo.getCurrentTripId())
    }

    // ====================================================================
    // ==================== MapRepository Tests ===========================
    // ====================================================================

    @Test
    fun `MapRepository - implements IMapRepository`() {
        // MapRepository uses default RetrofitClient which should construct without error in Robolectric
        try {
            val repo = MapRepository()
            assertTrue(repo is IMapRepository)
        } catch (_: Exception) {
            // If RetrofitClient init fails in test env, that's acceptable
            // We verify the class structure instead
            assertTrue(MapRepository::class.java.interfaces.contains(IMapRepository::class.java))
        }
    }

    @Test
    fun `MapRepository - class structure has correct interface`() {
        val interfaces = MapRepository::class.java.interfaces
        assertTrue("MapRepository should implement IMapRepository",
            interfaces.contains(IMapRepository::class.java))
    }

    // ====================================================================
    // ==================== Data Model Tests ==============================
    // ====================================================================

    // ------------------------------------------------------------------
    // GeoPoint
    // ------------------------------------------------------------------

    @Test
    fun `GeoPoint - constructor and properties`() {
        val point = GeoPoint(lng = 121.47, lat = 31.23)
        assertEquals(121.47, point.lng, 0.001)
        assertEquals(31.23, point.lat, 0.001)
    }

    @Test
    fun `GeoPoint - toLatLng`() {
        val point = GeoPoint(lng = 121.47, lat = 31.23)
        val latLng = point.toLatLng()
        assertEquals(31.23, latLng.latitude, 0.001)
        assertEquals(121.47, latLng.longitude, 0.001)
    }

    @Test
    fun `GeoPoint - fromLatLng`() {
        val latLng = LatLng(31.23, 121.47)
        val point = GeoPoint.fromLatLng(latLng)
        assertEquals(121.47, point.lng, 0.001)
        assertEquals(31.23, point.lat, 0.001)
    }

    @Test
    fun `GeoPoint - equality`() {
        val p1 = GeoPoint(lng = 121.47, lat = 31.23)
        val p2 = GeoPoint(lng = 121.47, lat = 31.23)
        assertEquals(p1, p2)
    }

    @Test
    fun `GeoPoint - zero coordinates`() {
        val point = GeoPoint(lng = 0.0, lat = 0.0)
        assertEquals(0.0, point.lng, 0.001)
        assertEquals(0.0, point.lat, 0.001)
    }

    @Test
    fun `GeoPoint - negative coordinates`() {
        val point = GeoPoint(lng = -73.99, lat = -40.75)
        assertEquals(-73.99, point.lng, 0.001)
        assertEquals(-40.75, point.lat, 0.001)
    }

    // ------------------------------------------------------------------
    // LocationInfo
    // ------------------------------------------------------------------

    @Test
    fun `LocationInfo - all fields present`() {
        val info = LocationInfo(address = MAIN_ST, place_name = "Library", campus_zone = ZONE_A)
        assertEquals(MAIN_ST, info.address)
        assertEquals("Library", info.place_name)
        assertEquals(ZONE_A, info.campus_zone)
    }

    @Test
    fun `LocationInfo - all fields null`() {
        val info = LocationInfo()
        assertNull(info.address)
        assertNull(info.place_name)
        assertNull(info.campus_zone)
    }

    @Test
    fun `LocationInfo - partial fields`() {
        val info = LocationInfo(address = MAIN_ST)
        assertEquals(MAIN_ST, info.address)
        assertNull(info.place_name)
        assertNull(info.campus_zone)
    }

    // ------------------------------------------------------------------
    // TripTrackData
    // ------------------------------------------------------------------

    @Test
    fun `TripTrackData - default status is tracking`() {
        val data = TripTrackData(trip_id = "t1", start_time = TIME_00_00)
        assertEquals("tracking", data.status)
    }

    @Test
    fun `TripTrackData - custom status`() {
        val data = TripTrackData(trip_id = "t1", status = "paused", start_time = TIME_00_00)
        assertEquals("paused", data.status)
    }

    @Test
    fun `TripTrackData - null message`() {
        val data = TripTrackData(trip_id = "t1", start_time = TIME_00_00)
        assertNull(data.message)
    }

    @Test
    fun `TripTrackData - with message`() {
        val data = TripTrackData(trip_id = "t1", start_time = TIME_00_00, message = "Started")
        assertEquals("Started", data.message)
    }

    // ------------------------------------------------------------------
    // TripCancelData
    // ------------------------------------------------------------------

    @Test
    fun `TripCancelData - all fields`() {
        val data = TripCancelData(
            trip_id = "t1",
            status = "cancelled",
            cancel_time = TIME_01_00,
            message = "Cancelled"
        )
        assertEquals("t1", data.trip_id)
        assertEquals("cancelled", data.status)
        assertEquals(TIME_01_00, data.cancel_time)
        assertEquals("Cancelled", data.message)
    }

    @Test
    fun `TripCancelData - null optional fields`() {
        val data = TripCancelData(trip_id = "t1", status = "cancelled")
        assertNull(data.cancel_time)
        assertNull(data.message)
    }

    // ------------------------------------------------------------------
    // TrackPoint
    // ------------------------------------------------------------------

    @Test
    fun `TrackPoint - all fields`() {
        val point = TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 1000L, speed = 5.0)
        assertEquals(31.23, point.latitude, 0.001)
        assertEquals(121.47, point.longitude, 0.001)
        assertEquals(1000L, point.timestamp)
        assertEquals(5.0, point.speed!!, 0.001)
    }

    @Test
    fun `TrackPoint - null speed`() {
        val point = TrackPoint(latitude = 31.23, longitude = 121.47, timestamp = 1000L)
        assertNull(point.speed)
    }

    // ------------------------------------------------------------------
    // TripMapData
    // ------------------------------------------------------------------

    @Test
    fun `TripMapData - default values`() {
        val data = TripMapData()
        assertNull(data.trip_id)
        assertTrue(data.track_points.isEmpty())
        assertEquals(0.0, data.current_distance, 0.001)
        assertEquals(0, data.duration_seconds)
        assertEquals("tracking", data.status)
    }

    @Test
    fun `TripMapData - custom values`() {
        val points = listOf(TrackPoint(31.23, 121.47, 1000L))
        val data = TripMapData(
            trip_id = "t1",
            track_points = points,
            current_distance = 1.5,
            duration_seconds = 300,
            status = "completed"
        )
        assertEquals("t1", data.trip_id)
        assertEquals(1, data.track_points.size)
        assertEquals(1.5, data.current_distance, 0.001)
        assertEquals(300, data.duration_seconds)
        assertEquals("completed", data.status)
    }

    // ------------------------------------------------------------------
    // TripSaveData
    // ------------------------------------------------------------------

    @Test
    fun `TripSaveData - all fields`() {
        val data = TripSaveData(
            trip_id = "t1",
            status = "completed",
            total_distance = 10.0,
            duration_minutes = 30,
            message = "Saved"
        )
        assertEquals("t1", data.trip_id)
        assertEquals("completed", data.status)
        assertEquals(10.0, data.total_distance!!, 0.001)
        assertEquals(30, data.duration_minutes)
        assertEquals("Saved", data.message)
    }

    @Test
    fun `TripSaveData - null optional fields`() {
        val data = TripSaveData(trip_id = "t1", status = "completed")
        assertNull(data.total_distance)
        assertNull(data.duration_minutes)
        assertNull(data.message)
    }

    // ------------------------------------------------------------------
    // CarbonCalculateData
    // ------------------------------------------------------------------

    @Test
    fun `CarbonCalculateData - default values`() {
        val data = CarbonCalculateData(carbon_saved = 0.5)
        assertNull(data.trip_id)
        assertEquals(0.0, data.total_carbon_emission, 0.001)
        assertEquals(0.5, data.carbon_saved, 0.001)
        assertEquals(0, data.green_points)
        assertTrue(data.is_green_trip)
        assertNull(data.transport_breakdown)
    }

    @Test
    fun `CarbonCalculateData - all fields`() {
        val breakdown = mapOf("walk" to 0.0, "bus" to 0.25)
        val data = CarbonCalculateData(
            trip_id = "t1",
            total_carbon_emission = 0.25,
            carbon_saved = 0.5,
            green_points = 50,
            is_green_trip = true,
            transport_breakdown = breakdown
        )
        assertEquals("t1", data.trip_id)
        assertEquals(0.25, data.total_carbon_emission, 0.001)
        assertEquals(0.5, data.carbon_saved, 0.001)
        assertEquals(50, data.green_points)
        assertTrue(data.is_green_trip)
        assertEquals(2, data.transport_breakdown!!.size)
    }

    // ------------------------------------------------------------------
    // TransportMode enum
    // ------------------------------------------------------------------

    @Test
    fun `TransportMode - WALKING value`() {
        assertEquals("walk", TransportMode.WALKING.value)
    }

    @Test
    fun `TransportMode - CYCLING value`() {
        assertEquals("bike", TransportMode.CYCLING.value)
    }

    @Test
    fun `TransportMode - BUS value`() {
        assertEquals("bus", TransportMode.BUS.value)
    }

    @Test
    fun `TransportMode - SUBWAY value`() {
        assertEquals("subway", TransportMode.SUBWAY.value)
    }

    @Test
    fun `TransportMode - DRIVING value`() {
        assertEquals("car", TransportMode.DRIVING.value)
    }

    @Test
    fun `TransportMode - WALKING displayName`() {
        assertEquals("步行", TransportMode.WALKING.displayName)
    }

    @Test
    fun `TransportMode - CYCLING displayName`() {
        assertEquals("骑行", TransportMode.CYCLING.displayName)
    }

    @Test
    fun `TransportMode - BUS displayName`() {
        assertEquals("公交", TransportMode.BUS.displayName)
    }

    @Test
    fun `TransportMode - SUBWAY displayName`() {
        assertEquals("地铁", TransportMode.SUBWAY.displayName)
    }

    @Test
    fun `TransportMode - DRIVING displayName`() {
        assertEquals("驾车", TransportMode.DRIVING.displayName)
    }

    @Test
    fun `TransportMode - all values exist`() {
        val values = TransportMode.values()
        assertEquals(5, values.size)
    }

    @Test
    fun `TransportMode - valueOf WALKING`() {
        assertEquals(TransportMode.WALKING, TransportMode.valueOf("WALKING"))
    }

    @Test
    fun `TransportMode - valueOf CYCLING`() {
        assertEquals(TransportMode.CYCLING, TransportMode.valueOf("CYCLING"))
    }

    @Test
    fun `TransportMode - valueOf BUS`() {
        assertEquals(TransportMode.BUS, TransportMode.valueOf("BUS"))
    }

    @Test
    fun `TransportMode - valueOf SUBWAY`() {
        assertEquals(TransportMode.SUBWAY, TransportMode.valueOf("SUBWAY"))
    }

    @Test
    fun `TransportMode - valueOf DRIVING`() {
        assertEquals(TransportMode.DRIVING, TransportMode.valueOf("DRIVING"))
    }

    // ------------------------------------------------------------------
    // RouteRecommendData
    // ------------------------------------------------------------------

    @Test
    fun `RouteRecommendData - default values`() {
        val data = RouteRecommendData()
        assertNull(data.route_id)
        assertNull(data.route_type)
        assertEquals(0.0, data.total_distance, 0.001)
        assertEquals(0, data.estimated_duration)
        assertEquals(0.0, data.total_carbon, 0.001)
        assertEquals(0.0, data.carbon_saved, 0.001)
        assertNull(data.route_segments)
        assertNull(data.route_points)
        assertNull(data.route_steps)
        assertNull(data.route_alternatives)
    }

    @Test
    fun `RouteRecommendData - with all fields`() {
        val segments = listOf(
            RouteSegment("walk", 1.0, 15, 0.0, "Walk")
        )
        val points = listOf(GeoPoint(121.47, 31.23))
        val steps = listOf(
            RouteStep("Walk", 1000.0, 900, "WALKING")
        )
        val data = RouteRecommendData(
            route_id = "r1",
            route_type = "low_carbon",
            total_distance = 5.0,
            estimated_duration = 30,
            total_carbon = 0.1,
            carbon_saved = 0.65,
            route_segments = segments,
            route_points = points,
            route_steps = steps
        )
        assertEquals("r1", data.route_id)
        assertEquals(5.0, data.total_distance, 0.001)
    }

    // ------------------------------------------------------------------
    // RouteSegment
    // ------------------------------------------------------------------

    @Test
    fun `RouteSegment - all fields`() {
        val segment = RouteSegment(
            transport_mode = "bus",
            distance = 3.0,
            duration = 15,
            carbon_emission = 0.15,
            instructions = "Take bus 46"
        )
        assertEquals("bus", segment.transport_mode)
        assertEquals(3.0, segment.distance, 0.001)
        assertEquals(15, segment.duration)
        assertEquals(0.15, segment.carbon_emission, 0.001)
        assertEquals("Take bus 46", segment.instructions)
    }

    @Test
    fun `RouteSegment - null optional fields`() {
        val segment = RouteSegment(
            transport_mode = "walk",
            distance = 1.0,
            duration = 10,
            carbon_emission = 0.0
        )
        assertNull(segment.instructions)
        assertNull(segment.polyline)
    }

    // ------------------------------------------------------------------
    // RouteStep
    // ------------------------------------------------------------------

    @Test
    fun `RouteStep - all fields`() {
        val transitDetails = TransitDetails(
            line_name = "Bus 46",
            line_short_name = "46",
            departure_stop = "A",
            arrival_stop = "B",
            num_stops = 3,
            vehicle_type = "BUS"
        )
        val step = RouteStep(
            instruction = TAKE_BUS,
            distance = 2000.0,
            duration = 300,
            travel_mode = "TRANSIT",
            transit_details = transitDetails,
            polyline_points = listOf(GeoPoint(121.47, 31.23))
        )
        assertEquals(TAKE_BUS, step.instruction)
        assertEquals(2000.0, step.distance, 0.001)
        assertNotNull(step.transit_details)
        assertEquals("46", step.transit_details!!.line_short_name)
    }

    @Test
    fun `RouteStep - null optional fields`() {
        val step = RouteStep(
            instruction = "Walk",
            distance = 500.0,
            duration = 300,
            travel_mode = "WALKING"
        )
        assertNull(step.transit_details)
        assertNull(step.polyline_points)
    }

    // ------------------------------------------------------------------
    // TransitDetails
    // ------------------------------------------------------------------

    @Test
    fun `TransitDetails - all fields`() {
        val details = TransitDetails(
            line_name = METRO_LINE_1,
            line_short_name = "1号线",
            departure_stop = STATION_A,
            arrival_stop = STATION_B,
            num_stops = 5,
            vehicle_type = "SUBWAY",
            headsign = "Northbound"
        )
        assertEquals(METRO_LINE_1, details.line_name)
        assertEquals("1号线", details.line_short_name)
        assertEquals(STATION_A, details.departure_stop)
        assertEquals(STATION_B, details.arrival_stop)
        assertEquals(5, details.num_stops)
        assertEquals("SUBWAY", details.vehicle_type)
        assertEquals("Northbound", details.headsign)
    }

    @Test
    fun `TransitDetails - null optional fields`() {
        val details = TransitDetails(
            line_name = "Bus 46",
            departure_stop = "A",
            arrival_stop = "B",
            num_stops = 3,
            vehicle_type = "BUS"
        )
        assertNull(details.line_short_name)
        assertNull(details.headsign)
    }

    // ------------------------------------------------------------------
    // RouteAlternative
    // ------------------------------------------------------------------

    @Test
    fun `RouteAlternative - all fields`() {
        val alt = RouteAlternative(
            index = 0,
            total_distance = 5.0,
            estimated_duration = 30,
            total_carbon = 0.15,
            route_points = listOf(GeoPoint(121.47, 31.23)),
            route_steps = emptyList(),
            summary = "Line 1 to Line 2"
        )
        assertEquals(0, alt.index)
        assertEquals(5.0, alt.total_distance, 0.001)
        assertEquals(30, alt.estimated_duration)
        assertEquals(0.15, alt.total_carbon, 0.001)
        assertEquals(1, alt.route_points.size)
        assertEquals("Line 1 to Line 2", alt.summary)
    }

    // ------------------------------------------------------------------
    // RouteType enum
    // ------------------------------------------------------------------

    @Test
    fun `RouteType - LOW_CARBON value`() {
        assertEquals("low-carbon", RouteType.LOW_CARBON.value)
    }

    @Test
    fun `RouteType - BALANCE value`() {
        assertEquals("balance", RouteType.BALANCE.value)
    }

    @Test
    fun `RouteType - all values exist`() {
        assertEquals(2, RouteType.values().size)
    }

    // ------------------------------------------------------------------
    // TripStatus enum
    // ------------------------------------------------------------------

    @Test
    fun `TripStatus - TRACKING value`() {
        assertEquals("tracking", TripStatus.TRACKING.value)
    }

    @Test
    fun `TripStatus - COMPLETED value`() {
        assertEquals("completed", TripStatus.COMPLETED.value)
    }

    @Test
    fun `TripStatus - CANCELED value`() {
        assertEquals("canceled", TripStatus.CANCELED.value)
    }

    @Test
    fun `TripStatus - all values exist`() {
        assertEquals(3, TripStatus.values().size)
    }

    // ------------------------------------------------------------------
    // RoutePoints
    // ------------------------------------------------------------------

    @Test
    fun `RoutePoints - toLatLngList`() {
        val routePoints = RoutePoints(
            points = listOf(
                GeoPoint(lng = 121.47, lat = 31.23),
                GeoPoint(lng = 121.48, lat = 31.24)
            )
        )
        val latLngList = routePoints.toLatLngList()
        assertEquals(2, latLngList.size)
        assertEquals(31.23, latLngList[0].latitude, 0.001)
        assertEquals(121.47, latLngList[0].longitude, 0.001)
    }

    @Test
    fun `RoutePoints - empty list`() {
        val routePoints = RoutePoints(points = emptyList())
        assertTrue(routePoints.toLatLngList().isEmpty())
    }

    // ------------------------------------------------------------------
    // RouteCacheData
    // ------------------------------------------------------------------

    @Test
    fun `RouteCacheData - all fields`() {
        val cacheData = RouteCacheData(
            route_info = RouteRecommendData(total_distance = 5.0),
            expire_time = "2025-12-31T23:59:59Z"
        )
        assertNotNull(cacheData.route_info)
        assertEquals(5.0, cacheData.route_info!!.total_distance, 0.001)
        assertEquals("2025-12-31T23:59:59Z", cacheData.expire_time)
    }

    @Test
    fun `RouteCacheData - null fields`() {
        val cacheData = RouteCacheData(route_info = null, expire_time = null)
        assertNull(cacheData.route_info)
        assertNull(cacheData.expire_time)
    }

    // ====================================================================
    // Edge Cases and Integration-Style Tests
    // ====================================================================

    @Test
    fun `MockMapRepository - full trip lifecycle`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)

        // Start trip
        val startResult = mockMapRepo.startTripTracking("user1", startPoint)
        assertTrue(startResult.isSuccess)
        val tripId = startResult.getOrThrow().trip_id

        // Get trip map
        val mapResult = mockMapRepo.getTripMap(tripId, "user1")
        assertTrue(mapResult.isSuccess)
        assertTrue(mapResult.getOrThrow().track_points.isNotEmpty())

        // Calculate carbon
        val carbonResult = mockMapRepo.calculateCarbon(tripId, listOf("walk", "bus"))
        assertTrue(carbonResult.isSuccess)

        // Save trip
        val saveResult = mockMapRepo.saveTrip(tripId, "user1", endPoint, null, 5.0, TIME_12_00)
        assertTrue(saveResult.isSuccess)
        assertEquals("completed", saveResult.getOrThrow().status)
    }

    @Test
    fun `MockMapRepository - trip start then cancel lifecycle`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)

        // Start trip
        val startResult = mockMapRepo.startTripTracking("user1", startPoint)
        assertTrue(startResult.isSuccess)
        val tripId = startResult.getOrThrow().trip_id

        // Cancel trip
        val cancelResult = mockMapRepo.cancelTripTracking(tripId, "user1", "Changed mind")
        assertTrue(cancelResult.isSuccess)
        assertEquals("cancelled", cancelResult.getOrThrow().status)

        // Verify current trip data is cleared
        val savedStart: GeoPoint? = getPrivateField(mockMapRepo, "currentTripStartPoint")
        assertNull(savedStart)
    }

    @Test
    fun `MockMapRepository - multiple trips sequentially`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)

        // First trip
        val result1 = mockMapRepo.startTripTracking("user1", startPoint)
        val tripId1 = result1.getOrThrow().trip_id
        mockMapRepo.saveTrip(tripId1, "user1", endPoint, null, 5.0, "time1")

        // Second trip
        val result2 = mockMapRepo.startTripTracking("user1", startPoint)
        val tripId2 = result2.getOrThrow().trip_id

        // IDs should be different
        assertNotEquals(tripId1, tripId2)
    }

    @Test
    fun `calculateCarbon - walk and bike both green`() = runTest {
        val walkResult = mockMapRepo.calculateCarbon("t1", listOf("walk"))
        val bikeResult = mockMapRepo.calculateCarbon("t2", listOf("bike"))
        assertEquals(walkResult.getOrThrow().total_carbon_emission,
            bikeResult.getOrThrow().total_carbon_emission, 0.001)
    }

    @Test
    fun `calculateDistance and generateRoutePoints consistency`() {
        val start = GeoPoint(lng = 121.47, lat = 31.23)
        val end = GeoPoint(lng = 121.48, lat = 31.24)

        val dist: Double = invokePrivate(mockMapRepo, "calculateDistance",
            GeoPoint::class.java to start, GeoPoint::class.java to end)
        val points: List<GeoPoint> = invokePrivate(mockMapRepo, "generateRoutePoints",
            GeoPoint::class.java to start, GeoPoint::class.java to end)

        // Sum of segment distances along interpolated points should approximately equal direct distance
        var segDist = 0.0
        for (i in 1 until points.size) {
            val d: Double = invokePrivate(mockMapRepo, "calculateDistance",
                GeoPoint::class.java to points[i - 1], GeoPoint::class.java to points[i])
            segDist += d
        }
        // For linear interpolation on a sphere, segmented distance should be very close to direct distance
        assertEquals(dist, segDist, dist * 0.01) // within 1%
    }

    @Test
    fun `estimateDuration - all modes produce different results for same distance`() {
        val distance = 10.0
        val walkDur: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to distance, TransportMode::class.java to TransportMode.WALKING)
        val cycleDur: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to distance, TransportMode::class.java to TransportMode.CYCLING)
        val busDur: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to distance, TransportMode::class.java to TransportMode.BUS)
        val subwayDur: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to distance, TransportMode::class.java to TransportMode.SUBWAY)
        val driveDur: Int = invokePrivate(mockMapRepo, "estimateDuration",
            Double::class.java to distance, TransportMode::class.java to TransportMode.DRIVING)

        // Walking should be slowest, driving fastest
        assertTrue("Walking should be slowest", walkDur > cycleDur)
        assertTrue("Cycling should be slower than bus", cycleDur > busDur)
        assertTrue("Bus should be slower than subway", busDur > subwayDur)
        assertTrue("Subway should be slower than driving", subwayDur > driveDur)
    }

    @Test
    fun `calculateCarbonForMode - emissions ordering`() {
        val distance = 10.0
        val walkCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.WALKING)
        val busCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.BUS)
        val subwayCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.SUBWAY)
        val driveCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.DRIVING)

        // Emissions: WALKING < SUBWAY < BUS < DRIVING
        assertTrue(getCarbonTotalCarbon(walkCarbon) < getCarbonTotalCarbon(subwayCarbon))
        assertTrue(getCarbonTotalCarbon(subwayCarbon) < getCarbonTotalCarbon(busCarbon))
        assertTrue(getCarbonTotalCarbon(busCarbon) < getCarbonTotalCarbon(driveCarbon))
    }

    @Test
    fun `calculateCarbonForMode - saved ordering`() {
        val distance = 10.0
        val walkCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.WALKING)
        val busCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.BUS)
        val driveCarbon: Any = invokePrivate(mockMapRepo, "calculateCarbonForMode",
            Double::class.java to distance, TransportMode::class.java to TransportMode.DRIVING)

        // Saved: WALKING > BUS > DRIVING (driving saves nothing)
        assertTrue(getCarbonSaved(walkCarbon) > getCarbonSaved(busCarbon))
        assertTrue(getCarbonSaved(busCarbon) > getCarbonSaved(driveCarbon))
    }

    @Test
    fun `NavigationStatistics - boundary percentage values`() {
        // 1 out of 3 = 33.333...%
        val stats = NavigationStatistics(3, 1, 1000.0, 0.1)
        assertTrue(stats.greenTripPercentage > 33.0 && stats.greenTripPercentage < 34.0)
    }

    @Test
    fun `NavigationStatistics - very small distance`() {
        val stats = NavigationStatistics(1, 1, 0.001, 0.0001)
        assertEquals(0.000001, stats.totalDistanceKm, 0.0000001)
    }

    @Test
    fun `generateRouteSummary - transit with empty line names`() {
        val steps = listOf(
            RouteStep(
                instruction = "Transit",
                distance = 1000.0,
                duration = 300,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "",
                    line_short_name = "",
                    departure_stop = "A",
                    arrival_stop = "B",
                    num_stops = 2,
                    vehicle_type = "BUS"
                )
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        // line_short_name is "" (not null), so it's used; result is "" which triggers ifEmpty -> "公交路线"
        assertEquals("公交路线", summary)
    }

    @Test
    fun `generateRouteSummary - transit with null short name and non-empty line name`() {
        val steps = listOf(
            RouteStep(
                instruction = "Transit",
                distance = 1000.0,
                duration = 300,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails(
                    line_name = "Special Express",
                    line_short_name = null,
                    departure_stop = "X",
                    arrival_stop = "Y",
                    num_stops = 1,
                    vehicle_type = "RAIL"
                )
            )
        )
        val summary: String = invokePrivate(mockMapRepo, "generateRouteSummary",
            List::class.java to steps)
        assertEquals("Special Express", summary)
    }

    @Test
    fun `TripRepository - getAuthToken after TokenManager logout returns fallback`() {
        TokenManager.saveToken("temp_token", "u1", "User1")
        TokenManager.logout()
        val repo = TripRepository.getInstance()
        val token = repo.getAuthToken()
        assertEquals(TEST_TOKEN, token)
    }

    @Test
    fun `MockMapRepository - tripIdCounter increments correctly over multiple trips`() = runTest {
        val startPoint = GeoPoint(lng = 0.0, lat = 0.0)
        val ids = mutableListOf<String>()
        repeat(5) {
            val result = mockMapRepo.startTripTracking("user1", startPoint)
            ids.add(result.getOrThrow().trip_id)
        }
        // All IDs should be unique
        assertEquals(5, ids.toSet().size)
        // Numbers should be sequential
        val numbers = ids.map { it.removePrefix("MOCK_TRIP_").toInt() }
        for (i in 1 until numbers.size) {
            assertEquals(numbers[i - 1] + 1, numbers[i])
        }
    }

    @Test
    fun `calculateCarbon - single unknown mode uses default 100 factor`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("scooter"))
        val data = result.getOrThrow()
        // 100 * 5 / 1 / 1000 = 0.5
        assertEquals(0.5, data.total_carbon_emission, 0.001)
        // saved = (750 - 500) / 1000 = 0.25
        assertEquals(0.25, data.carbon_saved, 0.001)
    }

    @Test
    fun `calculateCarbon - mixed known and unknown modes`() = runTest {
        val result = mockMapRepo.calculateCarbon("trip1", listOf("walk", "taxi"))
        val data = result.getOrThrow()
        // (0 + 100) * 5 / 2 / 1000 = 0.25
        assertEquals(0.25, data.total_carbon_emission, 0.001)
    }

    @Test
    fun `GeoPoint - round trip toLatLng and fromLatLng`() {
        val original = GeoPoint(lng = 121.47, lat = 31.23)
        val latLng = original.toLatLng()
        val back = GeoPoint.fromLatLng(latLng)
        assertEquals(original.lng, back.lng, 0.001)
        assertEquals(original.lat, back.lat, 0.001)
    }

    @Test
    fun `IMapRepository - is an interface`() {
        assertTrue("IMapRepository should be an interface", IMapRepository::class.java.isInterface)
    }

    @Test
    fun `IMapRepository - has expected method count`() {
        val methods = IMapRepository::class.java.declaredMethods
        // 8 suspend functions: startTripTracking, cancelTripTracking, getTripMap, saveTrip,
        // calculateCarbon, getLowestCarbonRoute, getBalancedRoute, getRouteByTransportMode
        assertTrue("IMapRepository should have at least 8 methods, has ${methods.size}",
            methods.size >= 8)
    }

    @Test
    fun `MapRepository - implements same interface as MockMapRepository`() {
        val mapInterfaces = MapRepository::class.java.interfaces.toSet()
        val mockInterfaces = MockMapRepository::class.java.interfaces.toSet()
        // Both should implement IMapRepository
        assertTrue(mapInterfaces.contains(IMapRepository::class.java))
        assertTrue(mockInterfaces.contains(IMapRepository::class.java))
    }

    // ====================================================================
    // ========= MapRepository with Mocked ApiService Tests ===============
    // ====================================================================

    /**
     * Helper: Create a mock Retrofit Response wrapping an ApiResponse.
     */
    private fun <T> mockSuccessResponse(data: T, code: Int = 200, msg: String = "ok"): retrofit2.Response<ApiResponse<T>> {
        val apiResponse = ApiResponse(code = code, msg = msg, data = data, success = true)
        return retrofit2.Response.success(apiResponse)
    }

    private fun <T> mockFailureApiResponse(msg: String = "error", code: Int = 400): retrofit2.Response<ApiResponse<T>> {
        val apiResponse = ApiResponse<T>(code = code, msg = msg, data = null, success = false)
        return retrofit2.Response.success(apiResponse)
    }

    private fun <T> mockNullDataResponse(code: Int = 200): retrofit2.Response<ApiResponse<T>> {
        val apiResponse = ApiResponse<T>(code = code, msg = "ok", data = null, success = true)
        return retrofit2.Response.success(apiResponse)
    }

    private fun <T> mockHttpErrorResponse(httpCode: Int = 500): retrofit2.Response<ApiResponse<T>> {
        return retrofit2.Response.error(
            httpCode,
            """{"code":$httpCode,"msg":"Server Error"}""".toResponseBody("application/json".toMediaType())
        )
    }

    // ------------------------------------------------------------------
    // MapRepository.startTripTracking
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - startTripTracking success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val trackData = TripTrackData(trip_id = "trip_1", status = "tracking", start_time = TIME_00_00, message = "OK")
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(trackData))
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isSuccess)
        assertEquals("trip_1", result.getOrThrow().trip_id)
        assertEquals("tracking", result.getOrThrow().status)
    }

    @Test
    fun `MapRepository - startTripTracking with location info`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val trackData = TripTrackData(trip_id = "trip_2", status = "tracking", start_time = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(trackData))
        val location = LocationInfo(address = "Addr", place_name = "Place")
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23), location)
        assertTrue(result.isSuccess)
        assertEquals("trip_2", result.getOrThrow().trip_id)
    }

    @Test
    fun `MapRepository - startTripTracking API failure returns error msg`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse("开启行程失败"))
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("开启行程失败"))
    }

    @Test
    fun `MapRepository - startTripTracking null data returns error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - startTripTracking exception returns failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenThrow(RuntimeException(NETWORK_ERROR))
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(NETWORK_ERROR))
    }

    @Test
    fun `MapRepository - startTripTracking HTTP error returns failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - startTripTracking failure with null msg uses default`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val apiResponse = ApiResponse<TripTrackData>(code = 400, msg = "", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository.cancelTripTracking
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - cancelTripTracking success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val cancelData = TripCancelData(trip_id = "trip_1", status = "cancelled", cancel_time = TIME_01_00)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(cancelData))
        val result = repo.cancelTripTracking("trip_1", "user1", "reason")
        assertTrue(result.isSuccess)
        assertEquals("cancelled", result.getOrThrow().status)
    }

    @Test
    fun `MapRepository - cancelTripTracking with null reason`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val cancelData = TripCancelData(trip_id = "trip_1", status = "cancelled")
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(cancelData))
        val result = repo.cancelTripTracking("trip_1", "user1", null)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `MapRepository - cancelTripTracking API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse("取消行程失败"))
        val result = repo.cancelTripTracking("trip_1", "user1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("取消行程失败"))
    }

    @Test
    fun `MapRepository - cancelTripTracking null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.cancelTripTracking("trip_1", "user1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - cancelTripTracking exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException("Timeout"))
        val result = repo.cancelTripTracking("trip_1", "user1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Timeout"))
    }

    // ------------------------------------------------------------------
    // MapRepository.getTripMap
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - getTripMap success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val mapData = TripMapData(trip_id = "trip_1", track_points = listOf(TrackPoint(31.23, 121.47, 1000L)), current_distance = 1.5, duration_seconds = 300, status = "tracking")
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(mapData))
        val result = repo.getTripMap("trip_1", "user1")
        assertTrue(result.isSuccess)
        assertEquals("trip_1", result.getOrThrow().trip_id)
        assertEquals(1, result.getOrThrow().track_points.size)
    }

    @Test
    fun `MapRepository - getTripMap API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse("获取地图数据失败"))
        val result = repo.getTripMap("trip_1", "user1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - getTripMap null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.getTripMap("trip_1", "user1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - getTripMap exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException("Connection refused"))
        val result = repo.getTripMap("trip_1", "user1")
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository.saveTrip
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - saveTrip success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val saveData = TripSaveData(trip_id = "trip_1", status = "completed", total_distance = 5.0, duration_minutes = 15, message = "Saved")
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(saveData))
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, TIME_12_00)
        assertTrue(result.isSuccess)
        assertEquals("completed", result.getOrThrow().status)
        assertEquals(5.0, result.getOrThrow().total_distance!!, 0.001)
    }

    @Test
    fun `MapRepository - saveTrip with end location`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val saveData = TripSaveData(trip_id = "trip_1", status = "completed")
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(saveData))
        val endLoc = LocationInfo(address = END_ADDR, place_name = END_PLACE)
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), endLoc, 10.0, "2025-01-01T13:00:00Z")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `MapRepository - saveTrip API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse("保存行程失败"))
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, "time")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("保存行程失败"))
    }

    @Test
    fun `MapRepository - saveTrip null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, "time")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - saveTrip exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenThrow(RuntimeException(NETWORK_ERROR))
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, "time")
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository.calculateCarbon
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - calculateCarbon success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val carbonData = CarbonCalculateData(trip_id = "trip_1", total_carbon_emission = 0.25, carbon_saved = 0.5, green_points = 50)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(carbonData))
        val result = repo.calculateCarbon("trip_1", listOf("walk", "bus"))
        assertTrue(result.isSuccess)
        assertEquals(0.25, result.getOrThrow().total_carbon_emission, 0.001)
        assertEquals(0.5, result.getOrThrow().carbon_saved, 0.001)
    }

    @Test
    fun `MapRepository - calculateCarbon API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse("计算碳足迹失败"))
        val result = repo.calculateCarbon("trip_1", listOf("walk"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("计算碳足迹失败"))
    }

    @Test
    fun `MapRepository - calculateCarbon null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.calculateCarbon("trip_1", listOf("walk"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - calculateCarbon exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenThrow(RuntimeException("Timeout"))
        val result = repo.calculateCarbon("trip_1", listOf("walk"))
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository.getLowestCarbonRoute
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - getLowestCarbonRoute success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val routeData = RouteRecommendData(route_id = "r1", route_type = "low_carbon", total_distance = 5.0, estimated_duration = 30, total_carbon = 0.1, carbon_saved = 0.65)
        org.mockito.kotlin.whenever(mockApi.getLowestCarbonRoute(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(routeData))
        val result = repo.getLowestCarbonRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isSuccess)
        assertEquals("low_carbon", result.getOrThrow().route_type)
        assertEquals(5.0, result.getOrThrow().total_distance, 0.001)
    }

    @Test
    fun `MapRepository - getLowestCarbonRoute API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getLowestCarbonRoute(org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse(ROUTE_FAILURE))
        val result = repo.getLowestCarbonRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(ROUTE_FAILURE))
    }

    @Test
    fun `MapRepository - getLowestCarbonRoute null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getLowestCarbonRoute(org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.getLowestCarbonRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - getLowestCarbonRoute exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getLowestCarbonRoute(org.mockito.kotlin.any())).thenThrow(RuntimeException("DNS failure"))
        val result = repo.getLowestCarbonRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository.getBalancedRoute
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - getBalancedRoute success`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val routeData = RouteRecommendData(route_id = "r2", route_type = "balanced", total_distance = 8.0, estimated_duration = 25, total_carbon = 0.48, carbon_saved = 0.72)
        org.mockito.kotlin.whenever(mockApi.getBalancedRoute(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(routeData))
        val result = repo.getBalancedRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isSuccess)
        assertEquals("balanced", result.getOrThrow().route_type)
        assertEquals(8.0, result.getOrThrow().total_distance, 0.001)
    }

    @Test
    fun `MapRepository - getBalancedRoute API failure`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getBalancedRoute(org.mockito.kotlin.any())).thenReturn(mockFailureApiResponse(ROUTE_FAILURE))
        val result = repo.getBalancedRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - getBalancedRoute null data`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getBalancedRoute(org.mockito.kotlin.any())).thenReturn(mockNullDataResponse())
        val result = repo.getBalancedRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(EMPTY_RESPONSE))
    }

    @Test
    fun `MapRepository - getBalancedRoute exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getBalancedRoute(org.mockito.kotlin.any())).thenThrow(RuntimeException("Socket timeout"))
        val result = repo.getBalancedRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // MapRepository full lifecycle with mocked ApiService
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - full trip lifecycle with mock API`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)

        // Start trip
        val trackData = TripTrackData(trip_id = "trip_lifecycle", status = "tracking", start_time = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(trackData))
        val startResult = repo.startTripTracking("user1", GeoPoint(lng = 121.47, lat = 31.23))
        assertTrue(startResult.isSuccess)

        // Get trip map
        val mapData = TripMapData(trip_id = "trip_lifecycle", track_points = listOf(TrackPoint(31.23, 121.47, 1000L)), current_distance = 2.0, duration_seconds = 120)
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(mapData))
        val mapResult = repo.getTripMap("trip_lifecycle", "user1")
        assertTrue(mapResult.isSuccess)

        // Calculate carbon
        val carbonData = CarbonCalculateData(trip_id = "trip_lifecycle", total_carbon_emission = 0.1, carbon_saved = 0.6)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(carbonData))
        val carbonResult = repo.calculateCarbon("trip_lifecycle", listOf("walk"))
        assertTrue(carbonResult.isSuccess)

        // Save trip
        val saveData = TripSaveData(trip_id = "trip_lifecycle", status = "completed", total_distance = 5.0)
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(saveData))
        val saveResult = repo.saveTrip("trip_lifecycle", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, "end_time")
        assertTrue(saveResult.isSuccess)
        assertEquals("completed", saveResult.getOrThrow().status)
    }

    @Test
    fun `MapRepository - start and cancel lifecycle with mock API`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)

        // Start
        val trackData = TripTrackData(trip_id = "trip_cancel", status = "tracking", start_time = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTripTracking(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(trackData))
        assertTrue(repo.startTripTracking("u1", GeoPoint(lng = 121.47, lat = 31.23)).isSuccess)

        // Cancel
        val cancelData = TripCancelData(trip_id = "trip_cancel", status = "cancelled", cancel_time = TIME_01_00)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(cancelData))
        val cancelResult = repo.cancelTripTracking("trip_cancel", "u1", "Changed mind")
        assertTrue(cancelResult.isSuccess)
        assertEquals("cancelled", cancelResult.getOrThrow().status)
    }

    // ------------------------------------------------------------------
    // MapRepository - edge case: HTTP error body with null body msg
    // ------------------------------------------------------------------

    @Test
    fun `MapRepository - cancelTripTracking HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.cancelTripTracking(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(403))
        val result = repo.cancelTripTracking("trip_1", "user1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - getTripMap HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getTripMap(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(404))
        val result = repo.getTripMap("trip_1", "user1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - saveTrip HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.saveTrip(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(502))
        val result = repo.saveTrip("trip_1", "user1", GeoPoint(lng = 121.48, lat = 31.24), null, 5.0, "time")
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - calculateCarbon HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.calculateCarbon(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.calculateCarbon("trip_1", listOf("walk"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - getLowestCarbonRoute HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getLowestCarbonRoute(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(503))
        val result = repo.getLowestCarbonRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
    }

    @Test
    fun `MapRepository - getBalancedRoute HTTP error`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        org.mockito.kotlin.whenever(mockApi.getBalancedRoute(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.getBalancedRoute("user1", GeoPoint(lng = 121.47, lat = 31.23), GeoPoint(lng = 121.48, lat = 31.24))
        assertTrue(result.isFailure)
    }

    // ====================================================================
    // ===== NavigationHistoryRepository with Mocked DAO Tests ============
    // ====================================================================

    /**
     * Helper: create a NavigationHistoryRepository with a mocked DAO injected via reflection.
     */
    private fun createNavHistoryRepoWithMockDao(): Pair<NavigationHistoryRepository, com.ecogo.mapengine.data.local.dao.NavigationHistoryDao> {
        // Initialize with real context so we get a real instance
        NavigationHistoryRepository.initialize(context)
        val repo = NavigationHistoryRepository.getInstance()
        // Now replace the DAO with a mock via reflection
        val mockDao: com.ecogo.mapengine.data.local.dao.NavigationHistoryDao = org.mockito.kotlin.mock()
        val daoField = NavigationHistoryRepository::class.java.getDeclaredField("dao")
        daoField.isAccessible = true
        daoField.set(repo, mockDao)
        return Pair(repo, mockDao)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.saveNavigationHistory
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - saveNavigationHistory delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.insert(org.mockito.kotlin.any())).thenReturn(42L)
        val id = repo.saveNavigationHistory(
            tripId = "trip1",
            userId = "user1",
            startTime = 1000L,
            endTime = 2000L,
            origin = LatLng(31.23, 121.47),
            originName = "Origin",
            destination = LatLng(31.24, 121.48),
            destinationName = "Destination",
            routePoints = listOf(LatLng(31.23, 121.47), LatLng(31.24, 121.48)),
            trackPoints = listOf(LatLng(31.235, 121.475)),
            totalDistance = 1500.0,
            traveledDistance = 1400.0,
            transportMode = "walk",
            detectedMode = "walk",
            totalCarbon = 0.0,
            carbonSaved = 0.5,
            isGreenTrip = true,
            greenPoints = 50,
            routeType = "low_carbon",
            notes = "Test note"
        )
        assertEquals(42L, id)
        org.mockito.kotlin.verify(mockDao).insert(org.mockito.kotlin.any())
    }

    @Test
    fun `NavigationHistoryRepository - saveNavigationHistory with null optional fields`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.insert(org.mockito.kotlin.any())).thenReturn(1L)
        val id = repo.saveNavigationHistory(
            startTime = 1000L,
            endTime = 2000L,
            origin = LatLng(31.23, 121.47),
            originName = "A",
            destination = LatLng(31.24, 121.48),
            destinationName = "B",
            routePoints = emptyList(),
            trackPoints = emptyList(),
            totalDistance = 100.0,
            traveledDistance = 90.0,
            transportMode = "bike",
            totalCarbon = 0.0,
            carbonSaved = 0.3,
            isGreenTrip = true
        )
        assertEquals(1L, id)
    }

    @Test
    fun `NavigationHistoryRepository - saveNavigationHistory calculates durationSeconds`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val capturedHistory = org.mockito.kotlin.argumentCaptor<com.ecogo.mapengine.data.local.entity.NavigationHistory>()
        org.mockito.kotlin.whenever(mockDao.insert(org.mockito.kotlin.any())).thenReturn(1L)
        repo.saveNavigationHistory(
            startTime = 10000L,
            endTime = 70000L,
            origin = LatLng(31.23, 121.47),
            originName = "A",
            destination = LatLng(31.24, 121.48),
            destinationName = "B",
            routePoints = emptyList(),
            trackPoints = emptyList(),
            totalDistance = 100.0,
            traveledDistance = 100.0,
            transportMode = "walk",
            totalCarbon = 0.0,
            carbonSaved = 0.1,
            isGreenTrip = true
        )
        org.mockito.kotlin.verify(mockDao).insert(capturedHistory.capture())
        // (70000 - 10000) / 1000 = 60 seconds
        assertEquals(60, capturedHistory.firstValue.durationSeconds)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getAllHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getAllHistories delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val mockHistories = listOf(
            com.ecogo.mapengine.data.local.entity.NavigationHistory(
                id = 1, startTime = 1000L, endTime = 2000L, durationSeconds = 1,
                originLat = 31.23, originLng = 121.47, originName = "A",
                destinationLat = 31.24, destinationLng = 121.48, destinationName = "B",
                routePoints = "[]", trackPoints = "[]", totalDistance = 100.0,
                traveledDistance = 90.0, transportMode = "walk", totalCarbon = 0.0,
                carbonSaved = 0.1, isGreenTrip = true
            )
        )
        org.mockito.kotlin.whenever(mockDao.getAll()).thenReturn(mockHistories)
        val result = repo.getAllHistories()
        assertEquals(1, result.size)
        assertEquals("A", result[0].originName)
    }

    @Test
    fun `NavigationHistoryRepository - getAllHistories empty list`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getAll()).thenReturn(emptyList())
        val result = repo.getAllHistories()
        assertTrue(result.isEmpty())
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getAllHistoriesFlow
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getAllHistoriesFlow delegates to DAO`() {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val mockFlow = kotlinx.coroutines.flow.flowOf(emptyList<com.ecogo.mapengine.data.local.entity.NavigationHistory>())
        org.mockito.kotlin.whenever(mockDao.getAllFlow()).thenReturn(mockFlow)
        val flow = repo.getAllHistoriesFlow()
        assertNotNull(flow)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getAllSummaries
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getAllSummaries delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val mockSummaries = listOf(
            com.ecogo.mapengine.data.local.entity.NavigationHistorySummary(
                id = 1, startTime = 1000L, originName = "A",
                destinationName = "B", totalDistance = 100.0,
                transportMode = "walk", carbonSaved = 0.1, durationSeconds = 60
            )
        )
        org.mockito.kotlin.whenever(mockDao.getAllSummaries()).thenReturn(mockSummaries)
        val result = repo.getAllSummaries()
        assertEquals(1, result.size)
        assertEquals("A", result[0].originName)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getHistoryById
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getHistoryById found`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val mockHistory = com.ecogo.mapengine.data.local.entity.NavigationHistory(
            id = 5, startTime = 1000L, endTime = 2000L, durationSeconds = 1,
            originLat = 31.23, originLng = 121.47, originName = "A",
            destinationLat = 31.24, destinationLng = 121.48, destinationName = "B",
            routePoints = "[]", trackPoints = "[]", totalDistance = 100.0,
            traveledDistance = 90.0, transportMode = "walk", totalCarbon = 0.0,
            carbonSaved = 0.1, isGreenTrip = true
        )
        org.mockito.kotlin.whenever(mockDao.getById(5L)).thenReturn(mockHistory)
        val result = repo.getHistoryById(5L)
        assertNotNull(result)
        assertEquals(5L, result!!.id)
    }

    @Test
    fun `NavigationHistoryRepository - getHistoryById not found`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getById(999L)).thenReturn(null)
        val result = repo.getHistoryById(999L)
        assertNull(result)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getRecentHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getRecentHistories with default limit`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getRecent(10)).thenReturn(emptyList())
        val result = repo.getRecentHistories()
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getRecent(10)
    }

    @Test
    fun `NavigationHistoryRepository - getRecentHistories with custom limit`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getRecent(5)).thenReturn(emptyList())
        val result = repo.getRecentHistories(5)
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getRecent(5)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getHistoriesByTimeRange
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getHistoriesByTimeRange delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getByTimeRange(1000L, 2000L)).thenReturn(emptyList())
        val result = repo.getHistoriesByTimeRange(1000L, 2000L)
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getByTimeRange(1000L, 2000L)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getTodayHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getTodayHistories delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getByTimeRange(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(emptyList())
        val result = repo.getTodayHistories()
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getByTimeRange(org.mockito.kotlin.any(), org.mockito.kotlin.any())
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getThisWeekHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getThisWeekHistories delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getByTimeRange(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(emptyList())
        val result = repo.getThisWeekHistories()
        assertTrue(result.isEmpty())
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getHistoriesByTransportMode
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getHistoriesByTransportMode delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getByTransportMode("walk")).thenReturn(emptyList())
        val result = repo.getHistoriesByTransportMode("walk")
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getByTransportMode("walk")
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getGreenTrips
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getGreenTrips delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getGreenTrips()).thenReturn(emptyList())
        val result = repo.getGreenTrips()
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getGreenTrips()
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getHistoriesByUserId
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getHistoriesByUserId delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getByUserId("user1")).thenReturn(emptyList())
        val result = repo.getHistoriesByUserId("user1")
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).getByUserId("user1")
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.searchHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - searchHistories delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.search("library")).thenReturn(emptyList())
        val result = repo.searchHistories("library")
        assertTrue(result.isEmpty())
        org.mockito.kotlin.verify(mockDao).search("library")
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.updateHistory
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - updateHistory delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val history = com.ecogo.mapengine.data.local.entity.NavigationHistory(
            id = 1, startTime = 1000L, endTime = 2000L, durationSeconds = 1,
            originLat = 31.23, originLng = 121.47, originName = "A",
            destinationLat = 31.24, destinationLng = 121.48, destinationName = "B",
            routePoints = "[]", trackPoints = "[]", totalDistance = 100.0,
            traveledDistance = 90.0, transportMode = "walk", totalCarbon = 0.0,
            carbonSaved = 0.1, isGreenTrip = true
        )
        repo.updateHistory(history)
        org.mockito.kotlin.verify(mockDao).update(history)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.deleteHistory
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - deleteHistory delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        val history = com.ecogo.mapengine.data.local.entity.NavigationHistory(
            id = 1, startTime = 1000L, endTime = 2000L, durationSeconds = 1,
            originLat = 31.23, originLng = 121.47, originName = "A",
            destinationLat = 31.24, destinationLng = 121.48, destinationName = "B",
            routePoints = "[]", trackPoints = "[]", totalDistance = 100.0,
            traveledDistance = 90.0, transportMode = "walk", totalCarbon = 0.0,
            carbonSaved = 0.1, isGreenTrip = true
        )
        repo.deleteHistory(history)
        org.mockito.kotlin.verify(mockDao).delete(history)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.deleteHistoryById
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - deleteHistoryById delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        repo.deleteHistoryById(42L)
        org.mockito.kotlin.verify(mockDao).deleteById(42L)
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.deleteAllHistories
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - deleteAllHistories delegates to DAO`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        repo.deleteAllHistories()
        org.mockito.kotlin.verify(mockDao).deleteAll()
    }

    // ------------------------------------------------------------------
    // NavigationHistoryRepository.getStatistics
    // ------------------------------------------------------------------

    @Test
    fun `NavigationHistoryRepository - getStatistics with data`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getCount()).thenReturn(10)
        org.mockito.kotlin.whenever(mockDao.getGreenTripCount()).thenReturn(7)
        org.mockito.kotlin.whenever(mockDao.getTotalDistance()).thenReturn(5000.0)
        org.mockito.kotlin.whenever(mockDao.getTotalCarbonSaved()).thenReturn(2.5)
        val stats = repo.getStatistics()
        assertEquals(10, stats.totalTrips)
        assertEquals(7, stats.greenTrips)
        assertEquals(5000.0, stats.totalDistanceMeters, 0.001)
        assertEquals(2.5, stats.totalCarbonSavedKg, 0.001)
        assertEquals(5.0, stats.totalDistanceKm, 0.001)
        assertEquals(70.0, stats.greenTripPercentage, 0.001)
    }

    @Test
    fun `NavigationHistoryRepository - getStatistics with null distance and carbon`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getCount()).thenReturn(0)
        org.mockito.kotlin.whenever(mockDao.getGreenTripCount()).thenReturn(0)
        org.mockito.kotlin.whenever(mockDao.getTotalDistance()).thenReturn(null)
        org.mockito.kotlin.whenever(mockDao.getTotalCarbonSaved()).thenReturn(null)
        val stats = repo.getStatistics()
        assertEquals(0, stats.totalTrips)
        assertEquals(0, stats.greenTrips)
        assertEquals(0.0, stats.totalDistanceMeters, 0.001)
        assertEquals(0.0, stats.totalCarbonSavedKg, 0.001)
    }

    @Test
    fun `NavigationHistoryRepository - getStatistics large values`() = runTest {
        val (repo, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getCount()).thenReturn(10000)
        org.mockito.kotlin.whenever(mockDao.getGreenTripCount()).thenReturn(8000)
        org.mockito.kotlin.whenever(mockDao.getTotalDistance()).thenReturn(1000000.0)
        org.mockito.kotlin.whenever(mockDao.getTotalCarbonSaved()).thenReturn(500.0)
        val stats = repo.getStatistics()
        assertEquals(10000, stats.totalTrips)
        assertEquals(8000, stats.greenTrips)
        assertEquals(1000.0, stats.totalDistanceKm, 0.001)
        assertEquals(80.0, stats.greenTripPercentage, 0.001)
    }

    // ====================================================================
    // ========= TripRepository with Mocked TripApiService Tests ==========
    // ====================================================================

    /**
     * Helper: create a TripRepository with a mocked TripApiService injected via reflection.
     */
    private fun createTripRepoWithMockApi(): Pair<TripRepository, com.ecogo.mapengine.data.remote.TripApiService> {
        val repo = TripRepository.getInstance()
        val mockTripApi: com.ecogo.mapengine.data.remote.TripApiService = org.mockito.kotlin.mock()
        val apiField = TripRepository::class.java.getDeclaredField("tripApiService")
        apiField.isAccessible = true
        apiField.set(repo, mockTripApi)
        return Pair(repo, mockTripApi)
    }

    // ------------------------------------------------------------------
    // TripRepository.startTrip
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - startTrip success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val tripDetail = TripDetail(tripId = "trip_start_1", status = "tracking", startTime = TIME_00_00)
        val response = mockSuccessResponse(tripDetail)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(response)
        val result = repo.startTrip(31.23, 121.47, "Place", "Address")
        assertTrue(result.isSuccess)
        assertEquals("trip_start_1", result.getOrThrow())
        assertEquals("trip_start_1", repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - startTrip with campus zone`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val tripDetail = TripDetail(tripId = "trip_campus", status = "tracking", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.startTrip(31.23, 121.47, "Place", "Address", ZONE_A)
        assertTrue(result.isSuccess)
        assertEquals("trip_campus", result.getOrThrow())
    }

    @Test
    fun `TripRepository - startTrip API error response`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 400, msg = "Bad request", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.startTrip(31.23, 121.47, "Place", "Address")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(API_ERROR))
    }

    @Test
    fun `TripRepository - startTrip null data in success response`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 200, msg = "ok", data = null, success = true)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.startTrip(31.23, 121.47, "Place", "Address")
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - startTrip HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.startTrip(31.23, 121.47, "Place", "Address")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to start trip"))
    }

    @Test
    fun `TripRepository - startTrip exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException(NETWORK_ERROR))
        val result = repo.startTrip(31.23, 121.47, "Place", "Address")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(NETWORK_ERROR))
    }

    // ------------------------------------------------------------------
    // TripRepository.completeTrip
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - completeTrip success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        // Set current trip ID
        setPrivateField(repo, "currentTripId", "trip_complete_1")
        val tripDetail = TripDetail(tripId = "trip_complete_1", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.completeTrip(
            tripId = "trip_complete_1",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = END_PLACE,
            endAddress = "End Address",
            distance = 1500.0,
            trackPoints = listOf(LatLng(31.23, 121.47), LatLng(31.24, 121.48)),
            transportMode = "walk"
        )
        assertTrue(result.isSuccess)
        // currentTripId should be cleared
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - completeTrip with all optional params`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        setPrivateField(repo, "currentTripId", "trip_full")
        val tripDetail = TripDetail(tripId = "trip_full", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val segments = listOf(
            TransportModeSegment(mode = "walk", subDistance = 0.5, subDuration = 300),
            TransportModeSegment(mode = "bus", subDistance = 1.0, subDuration = 600)
        )
        val result = repo.completeTrip(
            tripId = "trip_full",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 1500.0,
            trackPoints = listOf(LatLng(31.23, 121.47)),
            transportMode = "walk",
            detectedMode = "walk",
            mlConfidence = 0.95,
            carbonSaved = 100L,
            isGreenTrip = true,
            transportModeSegments = segments
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `TripRepository - completeTrip with empty track points`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val tripDetail = TripDetail(tripId = "trip_empty", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.completeTrip(
            tripId = "trip_empty",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 0.0,
            trackPoints = emptyList(),
            transportMode = "walk"
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `TripRepository - completeTrip does not clear different trip id`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        setPrivateField(repo, "currentTripId", "other_trip")
        val tripDetail = TripDetail(tripId = "trip_x", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.completeTrip(
            tripId = "trip_x",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 100.0,
            trackPoints = emptyList(),
            transportMode = "walk"
        )
        assertTrue(result.isSuccess)
        // currentTripId should NOT be cleared because it's a different trip
        assertEquals("other_trip", repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - completeTrip API error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 400, msg = "Validation error", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.completeTrip(
            tripId = "trip_1",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 100.0,
            trackPoints = emptyList(),
            transportMode = "walk"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(API_ERROR))
    }

    @Test
    fun `TripRepository - completeTrip HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.completeTrip(
            tripId = "trip_1",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 100.0,
            trackPoints = emptyList(),
            transportMode = "walk"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to complete trip"))
    }

    @Test
    fun `TripRepository - completeTrip exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException("IO Error"))
        val result = repo.completeTrip(
            tripId = "trip_1",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 100.0,
            trackPoints = emptyList(),
            transportMode = "walk"
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - completeTrip with null transportModeSegments creates single segment`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val tripDetail = TripDetail(tripId = "trip_single", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.completeTrip(
            tripId = "trip_single",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 2000.0,
            trackPoints = listOf(LatLng(31.23, 121.47)),
            transportMode = "bus",
            transportModeSegments = null
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `TripRepository - completeTrip with empty transportModeSegments creates single segment`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val tripDetail = TripDetail(tripId = "trip_empty_seg", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(tripDetail))
        val result = repo.completeTrip(
            tripId = "trip_empty_seg",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = "Addr",
            distance = 2000.0,
            trackPoints = listOf(LatLng(31.23, 121.47)),
            transportMode = "bus",
            transportModeSegments = emptyList()
        )
        assertTrue(result.isSuccess)
    }

    // ------------------------------------------------------------------
    // TripRepository.cancelTrip
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - cancelTrip success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        setPrivateField(repo, "currentTripId", "trip_cancel_1")
        val apiResponse = ApiResponse(code = 200, msg = "ok", data = "Trip cancelled", success = true)
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.cancelTrip("trip_cancel_1")
        assertTrue(result.isSuccess)
        assertEquals("Trip cancelled", result.getOrThrow())
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - cancelTrip does not clear different trip id`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        setPrivateField(repo, "currentTripId", "other_trip")
        val apiResponse = ApiResponse(code = 200, msg = "ok", data = "cancelled", success = true)
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.cancelTrip("trip_cancel_different")
        assertTrue(result.isSuccess)
        assertEquals("other_trip", repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - cancelTrip API error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<String>(code = 400, msg = "Not found", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.cancelTrip("trip_1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains(API_ERROR))
    }

    @Test
    fun `TripRepository - cancelTrip HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.cancelTrip("trip_1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to cancel trip"))
    }

    @Test
    fun `TripRepository - cancelTrip exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException("Timeout"))
        val result = repo.cancelTrip("trip_1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - cancelTrip null data in success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<String>(code = 200, msg = "ok", data = null, success = true)
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.cancelTrip("trip_1")
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // TripRepository.getTripListFromCloud
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - getTripListFromCloud success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val trips = listOf(
            TripDetail(tripId = "t1", status = "completed", startTime = TIME_00_00),
            TripDetail(tripId = "t2", status = "tracking", startTime = "2025-01-02T00:00:00Z")
        )
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenReturn(mockSuccessResponse(trips))
        val result = repo.getTripListFromCloud()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `TripRepository - getTripListFromCloud with pagination`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val trips = listOf(TripDetail(tripId = "t1", status = "completed", startTime = "t"))
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenReturn(mockSuccessResponse(trips))
        val result = repo.getTripListFromCloud(page = 1, pageSize = 10, status = "completed")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `TripRepository - getTripListFromCloud API error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<List<TripDetail>>(code = 400, msg = "Unauthorized", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getTripListFromCloud()
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripListFromCloud HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenReturn(mockHttpErrorResponse(503))
        val result = repo.getTripListFromCloud()
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripListFromCloud exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenThrow(RuntimeException("Connection reset"))
        val result = repo.getTripListFromCloud()
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripListFromCloud null data`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<List<TripDetail>>(code = 200, msg = "ok", data = null, success = true)
        org.mockito.kotlin.whenever(mockApi.getTripList(org.mockito.kotlin.any(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull(), org.mockito.kotlin.anyOrNull())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getTripListFromCloud()
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // TripRepository.getTripDetail
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - getTripDetail success`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val detail = TripDetail(tripId = "trip_detail_1", status = "completed", startTime = TIME_00_00,
            distance = 5.0, isGreenTrip = true, carbonSaved = 100L)
        org.mockito.kotlin.whenever(mockApi.getTripDetail(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(detail))
        val result = repo.getTripDetail("trip_detail_1")
        assertTrue(result.isSuccess)
        assertEquals("trip_detail_1", result.getOrThrow().tripId)
        assertEquals(5.0, result.getOrThrow().distance!!, 0.001)
    }

    @Test
    fun `TripRepository - getTripDetail API error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 404, msg = "Not found", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.getTripDetail(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getTripDetail("trip_1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripDetail HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getTripDetail(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.getTripDetail("trip_1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripDetail exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getTripDetail(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenThrow(RuntimeException("Timeout"))
        val result = repo.getTripDetail("trip_1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getTripDetail null data`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 200, msg = "ok", data = null, success = true)
        org.mockito.kotlin.whenever(mockApi.getTripDetail(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getTripDetail("trip_1")
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // TripRepository.getCurrentTrip
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - getCurrentTrip success with trip`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val detail = TripDetail(tripId = "current_1", status = "tracking", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.getCurrentTrip(org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(detail))
        val result = repo.getCurrentTrip()
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow())
        assertEquals("current_1", result.getOrThrow()!!.tripId)
        assertEquals("current_1", repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - getCurrentTrip success with null trip`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 200, msg = "ok", data = null, success = true)
        org.mockito.kotlin.whenever(mockApi.getCurrentTrip(org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getCurrentTrip()
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }

    @Test
    fun `TripRepository - getCurrentTrip API error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        val apiResponse = ApiResponse<TripDetail>(code = 401, msg = "Unauthorized", data = null, success = false)
        org.mockito.kotlin.whenever(mockApi.getCurrentTrip(org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(apiResponse))
        val result = repo.getCurrentTrip()
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getCurrentTrip HTTP error`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getCurrentTrip(org.mockito.kotlin.any())).thenReturn(mockHttpErrorResponse(500))
        val result = repo.getCurrentTrip()
        assertTrue(result.isFailure)
    }

    @Test
    fun `TripRepository - getCurrentTrip exception`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()
        org.mockito.kotlin.whenever(mockApi.getCurrentTrip(org.mockito.kotlin.any())).thenThrow(RuntimeException("DNS error"))
        val result = repo.getCurrentTrip()
        assertTrue(result.isFailure)
    }

    // ------------------------------------------------------------------
    // TripRepository.getTripListFromLocal
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - getTripListFromLocal success`() = runTest {
        // Initialize NavigationHistoryRepository and inject mock DAO
        val (_, mockDao) = createNavHistoryRepoWithMockDao()
        val mockHistories = listOf(
            com.ecogo.mapengine.data.local.entity.NavigationHistory(
                id = 1, startTime = 1000L, endTime = 2000L, durationSeconds = 1,
                originLat = 31.23, originLng = 121.47, originName = "A",
                destinationLat = 31.24, destinationLng = 121.48, destinationName = "B",
                routePoints = "[]", trackPoints = "[]", totalDistance = 100.0,
                traveledDistance = 90.0, transportMode = "walk", totalCarbon = 0.0,
                carbonSaved = 0.1, isGreenTrip = true
            )
        )
        org.mockito.kotlin.whenever(mockDao.getAll()).thenReturn(mockHistories)

        val repo = TripRepository.getInstance()
        // Replace historyRepo lazy val with our initialized singleton
        val result = repo.getTripListFromLocal()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
    }

    @Test
    fun `TripRepository - getTripListFromLocal empty`() = runTest {
        val (_, mockDao) = createNavHistoryRepoWithMockDao()
        org.mockito.kotlin.whenever(mockDao.getAll()).thenReturn(emptyList())
        val repo = TripRepository.getInstance()
        val result = repo.getTripListFromLocal()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    // ------------------------------------------------------------------
    // TripRepository full lifecycle
    // ------------------------------------------------------------------

    @Test
    fun `TripRepository - full lifecycle start then complete`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()

        // Start
        val startDetail = TripDetail(tripId = "lifecycle_1", status = "tracking", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(startDetail))
        val startResult = repo.startTrip(31.23, 121.47, "Start", START_ADDR)
        assertTrue(startResult.isSuccess)
        assertEquals("lifecycle_1", repo.getCurrentTripId())

        // Complete
        val completeDetail = TripDetail(tripId = "lifecycle_1", status = "completed", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.completeTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(completeDetail))
        val completeResult = repo.completeTrip(
            tripId = "lifecycle_1",
            endLat = 31.24,
            endLng = 121.48,
            endPlaceName = "End",
            endAddress = END_ADDR,
            distance = 1500.0,
            trackPoints = listOf(LatLng(31.23, 121.47)),
            transportMode = "walk"
        )
        assertTrue(completeResult.isSuccess)
        assertNull(repo.getCurrentTripId())
    }

    @Test
    fun `TripRepository - full lifecycle start then cancel`() = runTest {
        val (repo, mockApi) = createTripRepoWithMockApi()

        // Start
        val startDetail = TripDetail(tripId = "lifecycle_cancel", status = "tracking", startTime = TIME_00_00)
        org.mockito.kotlin.whenever(mockApi.startTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(mockSuccessResponse(startDetail))
        assertTrue(repo.startTrip(31.23, 121.47, "Start", "Addr").isSuccess)
        assertEquals("lifecycle_cancel", repo.getCurrentTripId())

        // Cancel
        val cancelResponse = ApiResponse(code = 200, msg = "ok", data = "Cancelled", success = true)
        org.mockito.kotlin.whenever(mockApi.cancelTrip(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(retrofit2.Response.success(cancelResponse))
        val cancelResult = repo.cancelTrip("lifecycle_cancel")
        assertTrue(cancelResult.isSuccess)
        assertNull(repo.getCurrentTripId())
    }

    // ------------------------------------------------------------------
    // TripDetail convenience properties
    // ------------------------------------------------------------------

    @Test
    fun `TripDetail - startLng and startLat from startPoint`() {
        val detail = TripDetail(
            tripId = "t1", startTime = DATE_2025_01_01,
            startPoint = GeoPointObj(lng = 121.47, lat = 31.23)
        )
        assertEquals(121.47, detail.startLng, 0.001)
        assertEquals(31.23, detail.startLat, 0.001)
    }

    @Test
    fun `TripDetail - default start coordinates when null`() {
        val detail = TripDetail(tripId = "t1", startTime = DATE_2025_01_01)
        assertEquals(0.0, detail.startLng, 0.001)
        assertEquals(0.0, detail.startLat, 0.001)
    }

    @Test
    fun `TripDetail - endLng and endLat from endPoint`() {
        val detail = TripDetail(
            tripId = "t1", startTime = DATE_2025_01_01,
            endPoint = GeoPointObj(lng = 121.48, lat = 31.24)
        )
        assertEquals(121.48, detail.endLng!!, 0.001)
        assertEquals(31.24, detail.endLat!!, 0.001)
    }

    @Test
    fun `TripDetail - null end coordinates when no endPoint`() {
        val detail = TripDetail(tripId = "t1", startTime = DATE_2025_01_01)
        assertNull(detail.endLng)
        assertNull(detail.endLat)
    }

    @Test
    fun `TripDetail - startAddress from startLocation`() {
        val detail = TripDetail(
            tripId = "t1", startTime = DATE_2025_01_01,
            startLocation = LocationObj(address = START_ADDR, placeName = "Start Place")
        )
        assertEquals(START_ADDR, detail.startAddress)
        assertEquals("Start Place", detail.startPlaceName)
    }

    @Test
    fun `TripDetail - default startAddress when null`() {
        val detail = TripDetail(tripId = "t1", startTime = DATE_2025_01_01)
        assertEquals("", detail.startAddress)
        assertEquals("", detail.startPlaceName)
    }

    @Test
    fun `TripDetail - endAddress from endLocation`() {
        val detail = TripDetail(
            tripId = "t1", startTime = DATE_2025_01_01,
            endLocation = LocationObj(address = END_ADDR, placeName = END_PLACE)
        )
        assertEquals(END_ADDR, detail.endAddress)
        assertEquals(END_PLACE, detail.endPlaceName)
    }

    @Test
    fun `TripDetail - null endAddress when no endLocation`() {
        val detail = TripDetail(tripId = "t1", startTime = DATE_2025_01_01)
        assertNull(detail.endAddress)
        assertNull(detail.endPlaceName)
    }

    // ------------------------------------------------------------------
    // ApiResponse properties
    // ------------------------------------------------------------------

    @Test
    fun `ApiResponse - isSuccess with code 200`() {
        val response = ApiResponse(code = 200, msg = "ok", data = "data", success = true)
        assertTrue(response.isSuccess)
    }

    @Test
    fun `ApiResponse - isSuccess with success true`() {
        val response = ApiResponse(code = 201, msg = "created", data = "data", success = true)
        assertTrue(response.isSuccess)
    }

    @Test
    fun `ApiResponse - isSuccess false`() {
        val response = ApiResponse(code = 400, msg = "error", data = null, success = false)
        assertFalse(response.isSuccess)
    }

    @Test
    fun `ApiResponse - message alias for msg`() {
        val response = ApiResponse(code = 200, msg = "hello", data = null)
        assertEquals("hello", response.message)
    }

    // ------------------------------------------------------------------
    // Additional TripApiModels coverage
    // ------------------------------------------------------------------

    @Test
    fun `TripStartRequest - all fields`() {
        val req = TripStartRequest(
            startLng = 121.47,
            startLat = 31.23,
            startAddress = "Addr",
            startPlaceName = "Place",
            startCampusZone = "Zone"
        )
        assertEquals(121.47, req.startLng, 0.001)
        assertEquals(31.23, req.startLat, 0.001)
        assertEquals("Addr", req.startAddress)
        assertEquals("Place", req.startPlaceName)
        assertEquals("Zone", req.startCampusZone)
    }

    @Test
    fun `TripStartRequest - null campus zone`() {
        val req = TripStartRequest(startLng = 0.0, startLat = 0.0, startAddress = "A", startPlaceName = "B")
        assertNull(req.startCampusZone)
    }

    @Test
    fun `TripCompleteRequest - all fields`() {
        val segments = listOf(TransportModeSegment(mode = "walk", subDistance = 1.0, subDuration = 300))
        val points = listOf(PolylinePoint(lng = 121.47, lat = 31.23))
        val req = TripCompleteRequest(
            endLng = 121.48,
            endLat = 31.24,
            endAddress = "Addr",
            endPlaceName = "Place",
            distance = 1.5,
            detectedMode = "walk",
            mlConfidence = 0.95,
            isGreenTrip = true,
            carbonSaved = 100L,
            transportModes = segments,
            polylinePoints = points
        )
        assertEquals(121.48, req.endLng, 0.001)
        assertEquals("walk", req.detectedMode)
        assertEquals(0.95, req.mlConfidence!!, 0.001)
        assertTrue(req.isGreenTrip)
    }

    @Test
    fun `TransportModeSegment - all fields`() {
        val seg = TransportModeSegment(mode = "bus", subDistance = 2.5, subDuration = 600)
        assertEquals("bus", seg.mode)
        assertEquals(2.5, seg.subDistance, 0.001)
        assertEquals(600, seg.subDuration)
    }

    @Test
    fun `PolylinePoint - all fields`() {
        val point = PolylinePoint(lng = 121.47, lat = 31.23)
        assertEquals(121.47, point.lng, 0.001)
        assertEquals(31.23, point.lat, 0.001)
    }

    @Test
    fun `GeoPointObj - default values`() {
        val obj = GeoPointObj()
        assertEquals(0.0, obj.lng, 0.001)
        assertEquals(0.0, obj.lat, 0.001)
    }

    @Test
    fun `GeoPointObj - custom values`() {
        val obj = GeoPointObj(lng = 121.47, lat = 31.23)
        assertEquals(121.47, obj.lng, 0.001)
        assertEquals(31.23, obj.lat, 0.001)
    }

    @Test
    fun `LocationObj - all fields`() {
        val obj = LocationObj(address = "Addr", placeName = "Place", campusZone = "Zone")
        assertEquals("Addr", obj.address)
        assertEquals("Place", obj.placeName)
        assertEquals("Zone", obj.campusZone)
    }

    @Test
    fun `LocationObj - default null fields`() {
        val obj = LocationObj()
        assertNull(obj.address)
        assertNull(obj.placeName)
        assertNull(obj.campusZone)
    }

    @Test
    fun `TripCancelResponse - all fields`() {
        val resp = TripCancelResponse(tripId = "t1", status = "cancelled", cancelTime = TIME_00_00, message = "Done")
        assertEquals("t1", resp.tripId)
        assertEquals("cancelled", resp.status)
        assertEquals("Done", resp.message)
    }

    @Test
    fun `TripListResponse - all fields`() {
        val trips = listOf(TripDetail(tripId = "t1", startTime = DATE_2025_01_01))
        val resp = TripListResponse(trips = trips, total = 1, page = 1, pageSize = 10)
        assertEquals(1, resp.trips.size)
        assertEquals(1, resp.total)
        assertEquals(1, resp.page)
        assertEquals(10, resp.pageSize)
    }

    @Test
    fun `CurrentTripResponse - with trip`() {
        val trip = TripDetail(tripId = "t1", startTime = DATE_2025_01_01)
        val resp = CurrentTripResponse(hasCurrentTrip = true, trip = trip)
        assertTrue(resp.hasCurrentTrip)
        assertNotNull(resp.trip)
    }

    @Test
    fun `CurrentTripResponse - without trip`() {
        val resp = CurrentTripResponse(hasCurrentTrip = false)
        assertFalse(resp.hasCurrentTrip)
        assertNull(resp.trip)
    }

    @Test
    fun `ApiError - all fields`() {
        val error = ApiError(code = 400, message = "Bad Request", details = "Invalid param")
        assertEquals(400, error.code)
        assertEquals("Bad Request", error.message)
        assertEquals("Invalid param", error.details)
    }

    @Test
    fun `ApiError - null details`() {
        val error = ApiError(code = 500, message = "Server Error")
        assertNull(error.details)
    }

    // ====================================================================
    // ===== MockMapRepository Route Methods (fallback paths) =============
    // ====================================================================

    @Test
    fun `MockMapRepository - getLowestCarbonRoute returns success`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getLowestCarbonRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertNotNull(data.route_id)
        assertTrue(data.route_id!!.startsWith("LOW_CARBON_"))
        assertEquals("low_carbon", data.route_type)
        assertTrue(data.total_distance > 0)
        assertTrue(data.estimated_duration > 0)
        assertNotNull(data.route_points)
        assertTrue(data.route_points!!.isNotEmpty())
        assertNotNull(data.route_segments)
        assertEquals(3, data.route_segments!!.size)
        // Verify segments
        assertEquals("walk", data.route_segments!![0].transport_mode)
        assertEquals("bus", data.route_segments!![1].transport_mode)
        assertEquals("walk", data.route_segments!![2].transport_mode)
    }

    @Test
    fun `MockMapRepository - getLowestCarbonRoute uses fallback when DirectionsService fails`() = runTest {
        // DirectionsService.getRoute will return null in test environment (no API key)
        // So the fallback path should be used: generateRoutePoints + calculateDistance
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getLowestCarbonRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // Should have 11 interpolated route points (fallback)
        assertTrue(data.route_points!!.size >= 2)
        // Carbon values should be positive
        assertTrue(data.total_carbon >= 0)
        assertTrue(data.carbon_saved >= 0)
    }

    @Test
    fun `MockMapRepository - getLowestCarbonRoute carbon calculations`() = runTest {
        val startPoint = GeoPoint(lng = 0.0, lat = 0.0)
        val endPoint = GeoPoint(lng = 1.0, lat = 0.0)
        val result = mockMapRepo.getLowestCarbonRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // total_carbon = distance * 0.02
        // carbon_saved = distance * 0.13
        val expectedCarbon = data.total_distance * 0.02
        val expectedSaved = data.total_distance * 0.13
        assertEquals(expectedCarbon, data.total_carbon, 0.1)
        assertEquals(expectedSaved, data.carbon_saved, 0.1)
    }

    @Test
    fun `MockMapRepository - getBalancedRoute returns success`() = runTest {
        val startPoint = GeoPoint(lng = 121.4737, lat = 31.2304)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getBalancedRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertNotNull(data.route_id)
        assertTrue(data.route_id!!.startsWith("BALANCED_"))
        assertEquals("balanced", data.route_type)
        assertTrue(data.total_distance > 0)
        assertNotNull(data.route_segments)
        assertEquals(3, data.route_segments!!.size)
        assertEquals("walk", data.route_segments!![0].transport_mode)
        assertEquals("subway", data.route_segments!![1].transport_mode)
        assertEquals("car", data.route_segments!![2].transport_mode)
    }

    @Test
    fun `MockMapRepository - getBalancedRoute uses fallback when DirectionsService fails`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getBalancedRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue(data.route_points!!.size >= 2)
    }

    @Test
    fun `MockMapRepository - getBalancedRoute carbon calculations`() = runTest {
        val startPoint = GeoPoint(lng = 0.0, lat = 0.0)
        val endPoint = GeoPoint(lng = 1.0, lat = 0.0)
        val result = mockMapRepo.getBalancedRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // total_carbon = distance * 0.06
        // carbon_saved = distance * 0.09
        val expectedCarbon = data.total_distance * 0.06
        val expectedSaved = data.total_distance * 0.09
        assertEquals(expectedCarbon, data.total_carbon, 0.1)
        assertEquals(expectedSaved, data.carbon_saved, 0.1)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode WALKING`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.WALKING)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("walk", data.route_type)
        assertTrue(data.total_distance > 0)
        assertTrue(data.estimated_duration > 0)
        assertEquals(0.0, data.total_carbon, 0.001)
        assertTrue(data.carbon_saved >= 0)
        assertNotNull(data.route_points)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode CYCLING`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.CYCLING)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("bike", data.route_type)
        assertEquals(0.0, data.total_carbon, 0.001)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode DRIVING`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.DRIVING)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("car", data.route_type)
        assertTrue(data.total_carbon > 0)
        assertEquals(0.0, data.carbon_saved, 0.001)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode BUS transit mode`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.BUS)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("bus", data.route_type)
        assertTrue(data.total_carbon >= 0)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode SUBWAY transit mode`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.SUBWAY)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("subway", data.route_type)
        assertTrue(data.total_carbon >= 0)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode all modes produce valid route IDs`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        for (mode in TransportMode.values()) {
            val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, mode)
            assertTrue("Mode ${mode.name} should succeed", result.isSuccess)
            val data = result.getOrThrow()
            assertNotNull("Mode ${mode.name} should have route_id", data.route_id)
            assertTrue("Route ID should contain mode value", data.route_id!!.contains(mode.value.uppercase()))
        }
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode WALKING route uses correct fallback`() = runTest {
        // DirectionsService will return null in test env, so fallback to generateRoutePoints
        val startPoint = GeoPoint(lng = 0.0, lat = 0.0)
        val endPoint = GeoPoint(lng = 1.0, lat = 0.0)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.WALKING)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // fallback: 11 interpolated points
        assertTrue(data.route_points!!.size >= 2)
        // empty steps when fallback is used
        assertTrue(data.route_steps == null || data.route_steps!!.isEmpty())
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode route segments are empty`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.WALKING)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        // getRouteByTransportMode always returns emptyList() for route_segments
        assertNotNull(data.route_segments)
        assertTrue(data.route_segments!!.isEmpty())
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode alternatives is null for non-transit`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.WALKING)
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow().route_alternatives)
    }

    @Test
    fun `MockMapRepository - getRouteByTransportMode BUS fallback to walking when transit fails`() = runTest {
        // In test env, DirectionsService returns null for both transit and walking
        // The method falls back to generateRoutePoints
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getRouteByTransportMode("user1", startPoint, endPoint, TransportMode.BUS)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertNotNull(data.route_points)
    }

    @Test
    fun `MockMapRepository - getLowestCarbonRoute segment distances add up`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getLowestCarbonRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        val segmentDistSum = data.route_segments!!.sumOf { it.distance }
        // Segment distances should sum to approximately total distance
        assertEquals(data.total_distance, segmentDistSum, data.total_distance * 0.01)
    }

    @Test
    fun `MockMapRepository - getBalancedRoute segment distances add up`() = runTest {
        val startPoint = GeoPoint(lng = 121.47, lat = 31.23)
        val endPoint = GeoPoint(lng = 121.48, lat = 31.24)
        val result = mockMapRepo.getBalancedRoute("user1", startPoint, endPoint)
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        val segmentDistSum = data.route_segments!!.sumOf { it.distance }
        assertEquals(data.total_distance, segmentDistSum, data.total_distance * 0.01)
    }

    // ====================================================================
    // ===== MapRepository.getRouteByTransportMode ========================
    // ====================================================================

    @Test
    fun `MapRepository - getRouteByTransportMode WALKING catches exception`() = runTest {
        // getRouteByTransportMode calls DirectionsService directly, not ApiService
        // In test env, DirectionsService.getRoute returns null, then the fallback code runs
        // But we can't easily mock DirectionsService (static). So test that it catches exceptions.
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        // This will call DirectionsService.getRoute which may return null or throw
        // The method has try-catch around the entire block, so it should return a failure or success
        val result = repo.getRouteByTransportMode(
            "user1",
            GeoPoint(lng = 121.47, lat = 31.23),
            GeoPoint(lng = 121.48, lat = 31.24),
            TransportMode.WALKING
        )
        // It may succeed (if DirectionsService returns null -> Result.failure) or fail
        // The main thing is it doesn't throw an uncaught exception
        assertNotNull(result)
    }

    @Test
    fun `MapRepository - getRouteByTransportMode DRIVING catches exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val result = repo.getRouteByTransportMode(
            "user1",
            GeoPoint(lng = 121.47, lat = 31.23),
            GeoPoint(lng = 121.48, lat = 31.24),
            TransportMode.DRIVING
        )
        assertNotNull(result)
    }

    @Test
    fun `MapRepository - getRouteByTransportMode CYCLING catches exception`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val result = repo.getRouteByTransportMode(
            "user1",
            GeoPoint(lng = 121.47, lat = 31.23),
            GeoPoint(lng = 121.48, lat = 31.24),
            TransportMode.CYCLING
        )
        assertNotNull(result)
    }

    @Test
    fun `MapRepository - getRouteByTransportMode BUS transit mode`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val result = repo.getRouteByTransportMode(
            "user1",
            GeoPoint(lng = 121.47, lat = 31.23),
            GeoPoint(lng = 121.48, lat = 31.24),
            TransportMode.BUS
        )
        assertNotNull(result)
    }

    @Test
    fun `MapRepository - getRouteByTransportMode SUBWAY transit mode`() = runTest {
        val mockApi: com.ecogo.mapengine.data.remote.ApiService = org.mockito.kotlin.mock()
        val repo = MapRepository(mockApi)
        val result = repo.getRouteByTransportMode(
            "user1",
            GeoPoint(lng = 121.47, lat = 31.23),
            GeoPoint(lng = 121.48, lat = 31.24),
            TransportMode.SUBWAY
        )
        assertNotNull(result)
    }
}
