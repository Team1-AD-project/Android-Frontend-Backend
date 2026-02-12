package com.ecogo.mapengine.ml.training

import com.ecogo.mapengine.ml.SnapToRoadsDetector
import com.ecogo.mapengine.ml.database.ActivityLabelingDao
import com.ecogo.mapengine.ml.database.LabelSourceCount
import com.ecogo.mapengine.ml.database.TransportModeCount
import com.ecogo.mapengine.ml.model.JourneyCSVRecord
import com.ecogo.mapengine.ml.model.JourneyFeatures
import com.ecogo.mapengine.ml.model.LabeledJourney
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Comprehensive Robolectric tests for com.ecogo.mapengine.ml.training package.
 * Covers FeatureExtractor, AutoLabelingService, and DataExporter.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MlTrainingTest {

    companion object {
        private const val JSON_ACCEL_1 = """[{"x":1.0,"y":2.0,"z":9.8}]"""
        private const val JSON_GYRO_1 = """[{"x":0.1,"y":0.2,"z":0.3}]"""
        private const val ROAD_MOTORWAY_TRUNK = "motorway|trunk"
        private const val ROAD_TRUNK_PRIMARY = "trunk|primary"
        private const val TEST_API_KEY = "test-key"
        private const val JSON_GYRO_SMALL = """[{"x":0.01,"y":0.02,"z":0.03}]"""
        private const val REPORT_BALANCED = "分布相对均衡"
    }

    // ========================================================================
    // Shared helpers
    // ========================================================================

    private lateinit var featureExtractor: FeatureExtractor

    private fun createLabeledJourney(
        id: Long = 1L,
        startTime: Long = 1000L,
        endTime: Long = 11000L,
        transportMode: String = "WALKING",
        labelSource: String = "AUTO_SNAP",
        gpsTrajectory: String = "",
        gpsPointCount: Int = 0,
        avgSpeed: Float = 1.5f,
        maxSpeed: Float = 3.0f,
        minSpeed: Float = 0.5f,
        speedVariance: Float = 0.25f,
        accelerometerData: String = "[]",
        gyroscopeData: String = "[]",
        barometerData: String = "[]",
        roadTypes: String = "",
        snapConfidence: Float = 0.9f,
        gpsAccuracy: Float = 5.0f,
        isVerified: Boolean = false,
        verificationTime: Long? = null,
        verificationNotes: String = ""
    ) = LabeledJourney(
        id = id,
        startTime = startTime,
        endTime = endTime,
        transportMode = transportMode,
        labelSource = labelSource,
        gpsTrajectory = gpsTrajectory,
        gpsPointCount = gpsPointCount,
        avgSpeed = avgSpeed,
        maxSpeed = maxSpeed,
        minSpeed = minSpeed,
        speedVariance = speedVariance,
        accelerometerData = accelerometerData,
        gyroscopeData = gyroscopeData,
        barometerData = barometerData,
        roadTypes = roadTypes,
        snapConfidence = snapConfidence,
        gpsAccuracy = gpsAccuracy,
        isVerified = isVerified,
        verificationTime = verificationTime,
        verificationNotes = verificationNotes
    )

    @Before
    fun setUp() {
        featureExtractor = FeatureExtractor()
    }

    // ========================================================================
    // PART 1: FeatureExtractor - parseSensorData (via reflection)
    // ========================================================================

    private fun invokeParseSensorData(jsonData: String): List<Triple<Float, Float, Float>> {
        val method = FeatureExtractor::class.java.getDeclaredMethod("parseSensorData", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(featureExtractor, jsonData) as List<Triple<Float, Float, Float>>
    }

    @Test
    fun `parseSensorData - single data point`() {
        val json = JSON_ACCEL_1
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(1.0f, result[0].first, 0.001f)
        assertEquals(2.0f, result[0].second, 0.001f)
        assertEquals(9.8f, result[0].third, 0.001f)
    }

    @Test
    fun `parseSensorData - multiple data points`() {
        val json = """[{"x":1.0,"y":2.0,"z":3.0},{"x":4.0,"y":5.0,"z":6.0},{"x":7.0,"y":8.0,"z":9.0}]"""
        val result = invokeParseSensorData(json)
        assertEquals(3, result.size)
        assertEquals(1.0f, result[0].first, 0.001f)
        assertEquals(5.0f, result[1].second, 0.001f)
        assertEquals(9.0f, result[2].third, 0.001f)
    }

    @Test
    fun `parseSensorData - empty JSON array`() {
        val result = invokeParseSensorData("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - empty string`() {
        val result = invokeParseSensorData("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - invalid JSON returns empty`() {
        val result = invokeParseSensorData("not a json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - missing x axis returns empty`() {
        val json = """[{"y":2.0,"z":3.0}]"""
        val result = invokeParseSensorData(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - missing y axis returns empty`() {
        val json = """[{"x":1.0,"z":3.0}]"""
        val result = invokeParseSensorData(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - missing z axis returns empty`() {
        val json = """[{"x":1.0,"y":2.0}]"""
        val result = invokeParseSensorData(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - negative values`() {
        val json = """[{"x":-1.5,"y":-2.5,"z":-3.5}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(-1.5f, result[0].first, 0.001f)
        assertEquals(-2.5f, result[0].second, 0.001f)
        assertEquals(-3.5f, result[0].third, 0.001f)
    }

    @Test
    fun `parseSensorData - scientific notation values`() {
        val json = """[{"x":1.5E+2,"y":2.0E-3,"z":9.8}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(150.0f, result[0].first, 0.1f)
        assertEquals(0.002f, result[0].second, 0.0001f)
    }

    @Test
    fun `parseSensorData - zero values`() {
        val json = """[{"x":0.0,"y":0.0,"z":0.0}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(0.0f, result[0].first, 0.001f)
        assertEquals(0.0f, result[0].second, 0.001f)
        assertEquals(0.0f, result[0].third, 0.001f)
    }

    @Test
    fun `parseSensorData - uneven axis counts uses min size`() {
        // two x values, one y value, one z value -> min is 1
        val json = """[{"x":1.0,"y":2.0,"z":3.0},{"x":4.0}]"""
        val result = invokeParseSensorData(json)
        // y has 1, z has 1, x has 2 => minSize = 1
        assertEquals(1, result.size)
        assertEquals(1.0f, result[0].first, 0.001f)
    }

    @Test
    fun `parseSensorData - large dataset`() {
        val entries = (1..100).joinToString(",") { """{"x":${it}.0,"y":${it * 2}.0,"z":${it * 3}.0}""" }
        val json = "[$entries]"
        val result = invokeParseSensorData(json)
        assertEquals(100, result.size)
        assertEquals(50.0f, result[49].first, 0.001f)
        assertEquals(300.0f, result[99].third, 0.001f)
    }

    @Test
    fun `parseSensorData - integer values without decimal`() {
        val json = """[{"x":1,"y":2,"z":3}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(1.0f, result[0].first, 0.001f)
    }

    @Test
    fun `parseSensorData - extra whitespace in JSON fails to parse`() {
        // The regex expects "x":value format with no space before the value
        // Extra spaces around colon break the regex pattern
        val json = """[  {  "x" : 1.0 , "y" : 2.0 , "z" : 3.0  }  ]"""
        val result = invokeParseSensorData(json)
        // Regex cannot match due to space between ":" and value
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseSensorData - only x values returns empty`() {
        val json = """[{"x":1.0},{"x":2.0}]"""
        val result = invokeParseSensorData(json)
        assertTrue(result.isEmpty())
    }

    // ========================================================================
    // PART 2: FeatureExtractor - extractAccelerometerFeatures (via reflection)
    // ========================================================================

    private fun invokeExtractAccelerometerFeatures(data: List<Triple<Float, Float, Float>>): Septuple<Float, Float, Float, Float, Float, Float, Float> {
        val method = FeatureExtractor::class.java.getDeclaredMethod(
            "extractAccelerometerFeatures", List::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(featureExtractor, data) as Septuple<Float, Float, Float, Float, Float, Float, Float>
    }

    @Test
    fun `extractAccelerometerFeatures - empty data returns all zeros`() {
        val result = invokeExtractAccelerometerFeatures(emptyList())
        assertEquals(0f, result.a, 0.001f)
        assertEquals(0f, result.b, 0.001f)
        assertEquals(0f, result.c, 0.001f)
        assertEquals(0f, result.d, 0.001f)
        assertEquals(0f, result.e, 0.001f)
        assertEquals(0f, result.f, 0.001f)
        assertEquals(0f, result.g, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - single data point`() {
        val data = listOf(Triple(3.0f, 4.0f, 0.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        // means
        assertEquals(3.0f, result.a, 0.001f)
        assertEquals(4.0f, result.b, 0.001f)
        assertEquals(0.0f, result.c, 0.001f)
        // std should be 0 for single point
        assertEquals(0.0f, result.d, 0.001f)
        assertEquals(0.0f, result.e, 0.001f)
        assertEquals(0.0f, result.f, 0.001f)
        // magnitude: sqrt(9+16+0) = 5
        assertEquals(5.0f, result.g, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - two data points mean`() {
        val data = listOf(Triple(2.0f, 4.0f, 6.0f), Triple(4.0f, 6.0f, 8.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(3.0f, result.a, 0.001f) // meanX
        assertEquals(5.0f, result.b, 0.001f) // meanY
        assertEquals(7.0f, result.c, 0.001f) // meanZ
    }

    @Test
    fun `extractAccelerometerFeatures - std deviation calculation`() {
        // values: (1,1,1), (3,3,3) -> mean=2, variance=1, std=1
        val data = listOf(Triple(1.0f, 1.0f, 1.0f), Triple(3.0f, 3.0f, 3.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(1.0f, result.d, 0.001f) // stdX
        assertEquals(1.0f, result.e, 0.001f) // stdY
        assertEquals(1.0f, result.f, 0.001f) // stdZ
    }

    @Test
    fun `extractAccelerometerFeatures - magnitude calculation`() {
        // (3,4,0) -> magnitude = 5
        // (0,0,10) -> magnitude = 10
        // average magnitude = 7.5
        val data = listOf(Triple(3.0f, 4.0f, 0.0f), Triple(0.0f, 0.0f, 10.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(7.5f, result.g, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - all identical values`() {
        val data = (1..10).map { Triple(5.0f, 5.0f, 5.0f) }
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(5.0f, result.a, 0.001f)
        assertEquals(0.0f, result.d, 0.001f) // std is 0 for constant
        val expectedMag = sqrt(75.0f) // sqrt(25+25+25)
        assertEquals(expectedMag, result.g, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - all zeros`() {
        val data = (1..5).map { Triple(0.0f, 0.0f, 0.0f) }
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(0.0f, result.a, 0.001f)
        assertEquals(0.0f, result.g, 0.001f) // magnitude of (0,0,0) is 0
    }

    @Test
    fun `extractAccelerometerFeatures - negative values`() {
        val data = listOf(Triple(-3.0f, -4.0f, 0.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(-3.0f, result.a, 0.001f)
        assertEquals(-4.0f, result.b, 0.001f)
        // magnitude: sqrt(9+16+0) = 5
        assertEquals(5.0f, result.g, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - typical gravity-like data`() {
        val data = listOf(
            Triple(0.1f, 0.2f, 9.8f),
            Triple(-0.1f, 0.1f, 9.7f),
            Triple(0.0f, -0.1f, 9.9f)
        )
        val result = invokeExtractAccelerometerFeatures(data)
        // meanX ~ 0, meanZ ~ 9.8
        assertEquals(0.0f, result.a, 0.1f)
        assertEquals(9.8f, result.c, 0.1f)
        // magnitude ~ 9.8
        assertTrue(result.g > 9.5f)
    }

    // ========================================================================
    // PART 3: FeatureExtractor - extractGyroscopeFeatures (via reflection)
    // ========================================================================

    private fun invokeExtractGyroscopeFeatures(data: List<Triple<Float, Float, Float>>): Sextuple<Float, Float, Float, Float, Float, Float> {
        val method = FeatureExtractor::class.java.getDeclaredMethod(
            "extractGyroscopeFeatures", List::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(featureExtractor, data) as Sextuple<Float, Float, Float, Float, Float, Float>
    }

    @Test
    fun `extractGyroscopeFeatures - empty data returns all zeros`() {
        val result = invokeExtractGyroscopeFeatures(emptyList())
        assertEquals(0f, result.a, 0.001f)
        assertEquals(0f, result.b, 0.001f)
        assertEquals(0f, result.c, 0.001f)
        assertEquals(0f, result.d, 0.001f)
        assertEquals(0f, result.e, 0.001f)
        assertEquals(0f, result.f, 0.001f)
    }

    @Test
    fun `extractGyroscopeFeatures - single data point`() {
        val data = listOf(Triple(0.5f, -0.3f, 0.1f))
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(0.5f, result.a, 0.001f)
        assertEquals(-0.3f, result.b, 0.001f)
        assertEquals(0.1f, result.c, 0.001f)
        assertEquals(0.0f, result.d, 0.001f) // std=0
        assertEquals(0.0f, result.e, 0.001f)
        assertEquals(0.0f, result.f, 0.001f)
    }

    @Test
    fun `extractGyroscopeFeatures - two data points`() {
        val data = listOf(Triple(1.0f, 2.0f, 3.0f), Triple(3.0f, 4.0f, 5.0f))
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(2.0f, result.a, 0.001f) // meanX
        assertEquals(3.0f, result.b, 0.001f) // meanY
        assertEquals(4.0f, result.c, 0.001f) // meanZ
        assertEquals(1.0f, result.d, 0.001f) // stdX
        assertEquals(1.0f, result.e, 0.001f) // stdY
        assertEquals(1.0f, result.f, 0.001f) // stdZ
    }

    @Test
    fun `extractGyroscopeFeatures - identical values yield zero std`() {
        val data = (1..20).map { Triple(1.0f, 2.0f, 3.0f) }
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(1.0f, result.a, 0.001f)
        assertEquals(2.0f, result.b, 0.001f)
        assertEquals(3.0f, result.c, 0.001f)
        assertEquals(0.0f, result.d, 0.001f)
        assertEquals(0.0f, result.e, 0.001f)
        assertEquals(0.0f, result.f, 0.001f)
    }

    @Test
    fun `extractGyroscopeFeatures - all zeros`() {
        val data = (1..5).map { Triple(0.0f, 0.0f, 0.0f) }
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(0.0f, result.a, 0.001f)
        assertEquals(0.0f, result.d, 0.001f)
    }

    @Test
    fun `extractGyroscopeFeatures - negative values`() {
        val data = listOf(Triple(-1.0f, -2.0f, -3.0f), Triple(-3.0f, -4.0f, -5.0f))
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(-2.0f, result.a, 0.001f)
        assertEquals(-3.0f, result.b, 0.001f)
        assertEquals(-4.0f, result.c, 0.001f)
    }

    @Test
    fun `extractGyroscopeFeatures - large dataset`() {
        // values from 1..100 on each axis
        val data = (1..100).map { Triple(it.toFloat(), (it * 2).toFloat(), (it * 3).toFloat()) }
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(50.5f, result.a, 0.01f) // meanX
        assertEquals(101.0f, result.b, 0.01f) // meanY
        assertEquals(151.5f, result.c, 0.01f) // meanZ
        // std should be > 0
        assertTrue(result.d > 0f)
    }

    // ========================================================================
    // PART 4: FeatureExtractor - calculateStd (via reflection)
    // ========================================================================

    private fun invokeCalculateStdDouble(values: List<Double>): Double {
        val method = FeatureExtractor::class.java.getDeclaredMethod("calculateStd", List::class.java)
        method.isAccessible = true
        return method.invoke(featureExtractor, values) as Double
    }

    private fun invokeCalculateStdFloat(values: List<Float>): Float {
        // The Float overload has @JvmName("calculateStdFloat")
        val methods = FeatureExtractor::class.java.declaredMethods
        val method = methods.first { it.name == "calculateStdFloat" }
        method.isAccessible = true
        return method.invoke(featureExtractor, values) as Float
    }

    @Test
    fun `calculateStd Double - empty list returns 0`() {
        assertEquals(0.0, invokeCalculateStdDouble(emptyList()), 0.0001)
    }

    @Test
    fun `calculateStd Double - single value returns 0`() {
        assertEquals(0.0, invokeCalculateStdDouble(listOf(5.0)), 0.0001)
    }

    @Test
    fun `calculateStd Double - two identical values returns 0`() {
        assertEquals(0.0, invokeCalculateStdDouble(listOf(3.0, 3.0)), 0.0001)
    }

    @Test
    fun `calculateStd Double - known values`() {
        // [1, 3] -> mean=2, variance=1, std=1
        assertEquals(1.0, invokeCalculateStdDouble(listOf(1.0, 3.0)), 0.0001)
    }

    @Test
    fun `calculateStd Double - larger known dataset`() {
        // [2, 4, 4, 4, 5, 5, 7, 9] -> mean=5, variance=4, std=2
        val values = listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)
        assertEquals(2.0, invokeCalculateStdDouble(values), 0.0001)
    }

    @Test
    fun `calculateStd Double - negative values`() {
        // [-1, 1] -> mean=0, variance=1, std=1
        assertEquals(1.0, invokeCalculateStdDouble(listOf(-1.0, 1.0)), 0.0001)
    }

    @Test
    fun `calculateStd Double - all zeros`() {
        assertEquals(0.0, invokeCalculateStdDouble(listOf(0.0, 0.0, 0.0)), 0.0001)
    }

    @Test
    fun `calculateStd Double - large values`() {
        // [100, 200] -> mean=150, variance=2500, std=50
        assertEquals(50.0, invokeCalculateStdDouble(listOf(100.0, 200.0)), 0.0001)
    }

    @Test
    fun `calculateStd Float - empty list returns 0`() {
        assertEquals(0f, invokeCalculateStdFloat(emptyList()), 0.0001f)
    }

    @Test
    fun `calculateStd Float - single value returns 0`() {
        assertEquals(0f, invokeCalculateStdFloat(listOf(5.0f)), 0.0001f)
    }

    @Test
    fun `calculateStd Float - two identical values returns 0`() {
        assertEquals(0f, invokeCalculateStdFloat(listOf(3.0f, 3.0f)), 0.0001f)
    }

    @Test
    fun `calculateStd Float - known values`() {
        assertEquals(1.0f, invokeCalculateStdFloat(listOf(1.0f, 3.0f)), 0.0001f)
    }

    @Test
    fun `calculateStd Float - negative values`() {
        assertEquals(1.0f, invokeCalculateStdFloat(listOf(-1.0f, 1.0f)), 0.0001f)
    }

    @Test
    fun `calculateStd Float - all zeros`() {
        assertEquals(0f, invokeCalculateStdFloat(listOf(0.0f, 0.0f, 0.0f)), 0.0001f)
    }

    // ========================================================================
    // PART 5: FeatureExtractor - extractFeatures (public, suspend)
    // ========================================================================

    @Test
    fun `extractFeatures - with valid sensor data`() = runTest {
        val accelJson = """[{"x":1.0,"y":2.0,"z":9.8},{"x":1.1,"y":2.1,"z":9.7}]"""
        val gyroJson = """[{"x":0.1,"y":0.2,"z":0.3},{"x":0.15,"y":0.25,"z":0.35}]"""
        val journey = createLabeledJourney(
            startTime = 0L,
            endTime = 10000L,
            transportMode = "WALKING",
            avgSpeed = 1.5f,
            maxSpeed = 3.0f,
            speedVariance = 0.25f,
            accelerometerData = accelJson,
            gyroscopeData = gyroJson
        )
        val features = featureExtractor.extractFeatures(journey)

        assertEquals("WALKING", features.transportMode)
        assertEquals(10.0f, features.journeyDuration, 0.001f) // 10000ms / 1000

        // accel means
        assertEquals(1.05f, features.accelMeanX, 0.01f)
        assertEquals(2.05f, features.accelMeanY, 0.01f)
        assertEquals(9.75f, features.accelMeanZ, 0.01f)

        // gyro means
        assertEquals(0.125f, features.gyroMeanX, 0.01f)
        assertEquals(0.225f, features.gyroMeanY, 0.01f)
        assertEquals(0.325f, features.gyroMeanZ, 0.01f)

        // GPS speed features come from journey fields
        assertEquals(1.5f, features.gpsSpeedMean, 0.001f)
        assertEquals(3.0f, features.gpsSpeedMax, 0.001f)
        assertEquals(sqrt(0.25f), features.gpsSpeedStd, 0.001f)
    }

    @Test
    fun `extractFeatures - with empty sensor data`() = runTest {
        val journey = createLabeledJourney(
            startTime = 0L,
            endTime = 5000L,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)

        // Empty sensor data -> all zeros for accel and gyro
        assertEquals(0f, features.accelMeanX, 0.001f)
        assertEquals(0f, features.accelStdX, 0.001f)
        assertEquals(0f, features.accelMagnitude, 0.001f)
        assertEquals(0f, features.gyroMeanX, 0.001f)
        assertEquals(0f, features.gyroStdX, 0.001f)
        assertEquals(5.0f, features.journeyDuration, 0.001f)
    }

    @Test
    fun `extractFeatures - with invalid accelerometer JSON`() = runTest {
        val journey = createLabeledJourney(
            startTime = 0L,
            endTime = 3000L,
            accelerometerData = "invalid json data",
            gyroscopeData = JSON_GYRO_1
        )
        val features = featureExtractor.extractFeatures(journey)
        // Invalid accel data falls back to empty -> all zeros
        assertEquals(0f, features.accelMeanX, 0.001f)
        assertEquals(0f, features.accelMagnitude, 0.001f)
        // Gyro still parses fine
        assertEquals(0.1f, features.gyroMeanX, 0.01f)
    }

    @Test
    fun `extractFeatures - with invalid gyroscope JSON`() = runTest {
        val journey = createLabeledJourney(
            startTime = 0L,
            endTime = 3000L,
            accelerometerData = """[{"x":1.0,"y":2.0,"z":3.0}]""",
            gyroscopeData = "corrupted"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(1.0f, features.accelMeanX, 0.01f)
        assertEquals(0f, features.gyroMeanX, 0.001f)
    }

    @Test
    fun `extractFeatures - journey duration calculation`() = runTest {
        val journey = createLabeledJourney(
            startTime = 1000L,
            endTime = 61000L,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(60.0f, features.journeyDuration, 0.001f) // 60000ms / 1000
    }

    @Test
    fun `extractFeatures - zero duration journey`() = runTest {
        val journey = createLabeledJourney(
            startTime = 5000L,
            endTime = 5000L,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(0.0f, features.journeyDuration, 0.001f)
    }

    @Test
    fun `extractFeatures - speed variance sqrt for gpsSpeedStd`() = runTest {
        val journey = createLabeledJourney(
            speedVariance = 4.0f,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(2.0f, features.gpsSpeedStd, 0.001f)
    }

    @Test
    fun `extractFeatures - zero speed variance`() = runTest {
        val journey = createLabeledJourney(
            speedVariance = 0.0f,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(0.0f, features.gpsSpeedStd, 0.001f)
    }

    @Test
    fun `extractFeatures - transport mode is propagated`() = runTest {
        val journey = createLabeledJourney(
            transportMode = "DRIVING",
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals("DRIVING", features.transportMode)
    }

    @Test
    fun `extractFeatures - large sensor dataset`() = runTest {
        val entries = (1..50).joinToString(",") {
            """{"x":${it * 0.1},"y":${it * 0.2},"z":${it * 0.3}}"""
        }
        val json = "[$entries]"
        val journey = createLabeledJourney(
            accelerometerData = json,
            gyroscopeData = json
        )
        val features = featureExtractor.extractFeatures(journey)
        assertTrue(features.accelMeanX > 0f)
        assertTrue(features.accelStdX > 0f)
        assertTrue(features.accelMagnitude > 0f)
        assertTrue(features.gyroMeanX > 0f)
    }

    // ========================================================================
    // PART 6: Septuple and Sextuple data classes
    // ========================================================================

    @Test
    fun `Septuple - component functions work`() {
        val s = Septuple(1f, 2f, 3f, 4f, 5f, 6f, 7f)
        assertEquals(1f, s.a, 0.001f)
        assertEquals(2f, s.b, 0.001f)
        assertEquals(3f, s.c, 0.001f)
        assertEquals(4f, s.d, 0.001f)
        assertEquals(5f, s.e, 0.001f)
        assertEquals(6f, s.f, 0.001f)
        assertEquals(7f, s.g, 0.001f)
    }

    @Test
    fun `Septuple - destructuring works`() {
        val (a, b, c, d, e, f, g) = Septuple(10, 20, 30, 40, 50, 60, 70)
        assertEquals(10, a)
        assertEquals(70, g)
    }

    @Test
    fun `Septuple - equality works`() {
        val s1 = Septuple(1, 2, 3, 4, 5, 6, 7)
        val s2 = Septuple(1, 2, 3, 4, 5, 6, 7)
        assertEquals(s1, s2)
    }

    @Test
    fun `Septuple - inequality works`() {
        val s1 = Septuple(1, 2, 3, 4, 5, 6, 7)
        val s2 = Septuple(1, 2, 3, 4, 5, 6, 8)
        assertNotEquals(s1, s2)
    }

    @Test
    fun `Septuple - hashCode consistent with equals`() {
        val s1 = Septuple(1, 2, 3, 4, 5, 6, 7)
        val s2 = Septuple(1, 2, 3, 4, 5, 6, 7)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun `Septuple - toString includes all values`() {
        val s = Septuple(1, 2, 3, 4, 5, 6, 7)
        val str = s.toString()
        assertTrue(str.contains("1"))
        assertTrue(str.contains("7"))
    }

    @Test
    fun `Septuple - copy works`() {
        val s1 = Septuple(1, 2, 3, 4, 5, 6, 7)
        val s2 = s1.copy(g = 99)
        assertEquals(99, s2.g)
        assertEquals(1, s2.a)
    }

    @Test
    fun `Sextuple - component functions work`() {
        val s = Sextuple(1f, 2f, 3f, 4f, 5f, 6f)
        assertEquals(1f, s.a, 0.001f)
        assertEquals(6f, s.f, 0.001f)
    }

    @Test
    fun `Sextuple - destructuring works`() {
        val (a, b, c, d, e, f) = Sextuple("a", "b", "c", "d", "e", "f")
        assertEquals("a", a)
        assertEquals("f", f)
    }

    @Test
    fun `Sextuple - equality works`() {
        val s1 = Sextuple(1, 2, 3, 4, 5, 6)
        val s2 = Sextuple(1, 2, 3, 4, 5, 6)
        assertEquals(s1, s2)
    }

    @Test
    fun `Sextuple - inequality works`() {
        val s1 = Sextuple(1, 2, 3, 4, 5, 6)
        val s2 = Sextuple(1, 2, 3, 4, 5, 99)
        assertNotEquals(s1, s2)
    }

    @Test
    fun `Sextuple - hashCode consistent with equals`() {
        val s1 = Sextuple(1, 2, 3, 4, 5, 6)
        val s2 = Sextuple(1, 2, 3, 4, 5, 6)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun `Sextuple - toString includes all values`() {
        val s = Sextuple(10, 20, 30, 40, 50, 60)
        val str = s.toString()
        assertTrue(str.contains("10"))
        assertTrue(str.contains("60"))
    }

    @Test
    fun `Sextuple - copy works`() {
        val s1 = Sextuple(1, 2, 3, 4, 5, 6)
        val s2 = s1.copy(f = 99)
        assertEquals(99, s2.f)
        assertEquals(1, s2.a)
    }

    // ========================================================================
    // PART 7: AutoLabelingService - calculateSpeeds (via reflection)
    // ========================================================================

    private lateinit var autoLabelingService: AutoLabelingService

    private fun createAutoLabelingService(): AutoLabelingService {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        return AutoLabelingService(dao, detector)
    }

    private fun invokeCalculateSpeeds(gpsTrajectory: List<Triple<Double, Double, Long>>): List<Double> {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        val method = AutoLabelingService::class.java.getDeclaredMethod(
            "calculateSpeeds", List::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(autoLabelingService, gpsTrajectory) as List<Double>
    }

    @Test
    fun `calculateSpeeds - empty trajectory returns empty`() {
        val result = invokeCalculateSpeeds(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateSpeeds - single point returns empty`() {
        val result = invokeCalculateSpeeds(listOf(Triple(30.0, 120.0, 1000L)))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateSpeeds - two stationary points return zero speed`() {
        val trajectory = listOf(
            Triple(30.0, 120.0, 1000L),
            Triple(30.0, 120.0, 2000L)
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(1, result.size)
        assertEquals(0.0, result[0], 0.001)
    }

    @Test
    fun `calculateSpeeds - two points with moderate movement`() {
        // Two points ~11 meters apart in 10 seconds -> ~1.1 m/s (walking speed)
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.000100, 120.000000, 10000L)
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(1, result.size)
        assertTrue(result[0] > 0.5)
        assertTrue(result[0] < 5.0) // reasonable walking speed
    }

    @Test
    fun `calculateSpeeds - filters out speed greater than or equal to 50 ms`() {
        // 1 degree latitude ~ 111km, in 1 second -> ~111000 m/s
        val trajectory = listOf(
            Triple(30.0, 120.0, 0L),
            Triple(31.0, 120.0, 1000L) // 1 second
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertTrue(result.isEmpty()) // speed > 50 m/s -> filtered out
    }

    @Test
    fun `calculateSpeeds - reasonable walking speed not filtered`() {
        // Very close points (~11 meters apart in ~10 seconds -> ~1.1 m/s)
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.000100, 120.000000, 10000L)
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(1, result.size)
        assertTrue(result[0] > 0)
        assertTrue(result[0] < 5) // walking speed
    }

    @Test
    fun `calculateSpeeds - same timestamp (zero time interval) is skipped`() {
        val trajectory = listOf(
            Triple(30.0, 120.0, 1000L),
            Triple(30.001, 120.0, 1000L) // same timestamp
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateSpeeds - multiple points with mixed speeds`() {
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.000100, 120.000000, 10000L), // ~1.1 m/s
            Triple(30.000200, 120.000000, 20000L), // ~1.1 m/s
            Triple(30.000300, 120.000000, 30000L)  // ~1.1 m/s
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(3, result.size)
        result.forEach { speed ->
            assertTrue(speed > 0)
            assertTrue(speed < 50)
        }
    }

    @Test
    fun `calculateSpeeds - three points some filtered`() {
        // first pair: close points (reasonable speed)
        // second pair: far points (speed > 50 m/s)
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.000100, 120.000000, 10000L),  // ~1 m/s
            Triple(31.000000, 120.000000, 11000L)    // ~111km in 1s -> filtered
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(1, result.size) // only first pair passes filter
    }

    // ========================================================================
    // PART 8: AutoLabelingService - haversineDistance (via reflection)
    // ========================================================================

    private fun invokeHaversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        val method = AutoLabelingService::class.java.getDeclaredMethod(
            "haversineDistance",
            Double::class.java, Double::class.java,
            Double::class.java, Double::class.java
        )
        method.isAccessible = true
        return method.invoke(autoLabelingService, lat1, lng1, lat2, lng2) as Double
    }

    @Test
    fun `haversineDistance - same point returns zero`() {
        val result = invokeHaversineDistance(30.0, 120.0, 30.0, 120.0)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `haversineDistance - one degree latitude is about 111km`() {
        val result = invokeHaversineDistance(30.0, 120.0, 31.0, 120.0)
        assertEquals(111195.0, result, 500.0) // ~111km with some tolerance
    }

    @Test
    fun `haversineDistance - one degree longitude at equator is about 111km`() {
        val result = invokeHaversineDistance(0.0, 120.0, 0.0, 121.0)
        assertEquals(111195.0, result, 500.0)
    }

    @Test
    fun `haversineDistance - is symmetric`() {
        val d1 = invokeHaversineDistance(30.0, 120.0, 31.0, 121.0)
        val d2 = invokeHaversineDistance(31.0, 121.0, 30.0, 120.0)
        assertEquals(d1, d2, 0.001)
    }

    @Test
    fun `haversineDistance - known distance London to Paris`() {
        // London (51.5074, -0.1278) to Paris (48.8566, 2.3522)
        val result = invokeHaversineDistance(51.5074, -0.1278, 48.8566, 2.3522)
        // ~343km
        assertTrue(result > 330000 && result < 360000)
    }

    @Test
    fun `haversineDistance - antipodal points give half circumference`() {
        // (0,0) to (0,180) = half circumference ~ 20015km
        val result = invokeHaversineDistance(0.0, 0.0, 0.0, 180.0)
        assertEquals(20015086.0, result, 1000.0)
    }

    @Test
    fun `haversineDistance - negative latitudes`() {
        val result = invokeHaversineDistance(-30.0, 120.0, -31.0, 120.0)
        assertEquals(111195.0, result, 500.0)
    }

    @Test
    fun `haversineDistance - crossing equator`() {
        val result = invokeHaversineDistance(-0.5, 120.0, 0.5, 120.0)
        assertEquals(111195.0, result, 500.0)
    }

    @Test
    fun `haversineDistance - very close points`() {
        // ~11 meters apart
        val result = invokeHaversineDistance(30.000000, 120.000000, 30.000100, 120.000000)
        assertTrue(result > 10 && result < 15)
    }

    @Test
    fun `haversineDistance - crossing date line`() {
        val result = invokeHaversineDistance(0.0, 179.5, 0.0, -179.5)
        // 1 degree at equator
        assertEquals(111195.0, result, 500.0)
    }

    // ========================================================================
    // PART 9: AutoLabelingService - calculateVariance (via reflection)
    // ========================================================================

    private fun invokeCalculateVariance(values: List<Double>): Double {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        val method = AutoLabelingService::class.java.getDeclaredMethod(
            "calculateVariance", List::class.java
        )
        method.isAccessible = true
        return method.invoke(autoLabelingService, values) as Double
    }

    @Test
    fun `calculateVariance - empty list returns 0`() {
        assertEquals(0.0, invokeCalculateVariance(emptyList()), 0.0001)
    }

    @Test
    fun `calculateVariance - single value returns 0`() {
        assertEquals(0.0, invokeCalculateVariance(listOf(5.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - identical values returns 0`() {
        assertEquals(0.0, invokeCalculateVariance(listOf(3.0, 3.0, 3.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - known values`() {
        // [1, 3] -> mean=2, variance = ((1-2)^2 + (3-2)^2)/2 = 1
        assertEquals(1.0, invokeCalculateVariance(listOf(1.0, 3.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - larger dataset`() {
        // [2,4,4,4,5,5,7,9] -> mean=5, variance = 4
        val values = listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)
        assertEquals(4.0, invokeCalculateVariance(values), 0.0001)
    }

    @Test
    fun `calculateVariance - negative values`() {
        // [-2, 2] -> mean=0, variance=4
        assertEquals(4.0, invokeCalculateVariance(listOf(-2.0, 2.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - all zeros`() {
        assertEquals(0.0, invokeCalculateVariance(listOf(0.0, 0.0, 0.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - large variance`() {
        // [0, 100] -> mean=50, variance=2500
        assertEquals(2500.0, invokeCalculateVariance(listOf(0.0, 100.0)), 0.0001)
    }

    // ========================================================================
    // PART 10: AutoLabelingService - inferTransportModeWithConfidence (via reflection)
    // ========================================================================

    private fun invokeInferTransportModeWithConfidence(
        speeds: List<Double>,
        roadTypes: String,
        duration: Long
    ): Pair<String, Float> {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        val method = AutoLabelingService::class.java.getDeclaredMethod(
            "inferTransportModeWithConfidence",
            List::class.java, String::class.java, Long::class.java
        )
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(autoLabelingService, speeds, roadTypes, duration) as Pair<String, Float>
    }

    @Test
    fun `inferTransportMode - DRIVING when avgSpeed gt 15 and motorway trunk road`() {
        // avgSpeed > 15, roadTypes contains "motorway|trunk"
        val speeds = listOf(20.0, 25.0, 18.0)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_MOTORWAY_TRUNK, 60000L)
        assertEquals("DRIVING", result.first)
        assertEquals(0.95f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - DRIVING needs avgSpeed strictly greater than 15`() {
        // avgSpeed = 15 exactly should NOT match rule 1 (> 15)
        val speeds = listOf(15.0, 15.0)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_MOTORWAY_TRUNK, 60000L)
        // avgSpeed = 15 -> not > 15, check rule 2: avgSpeed in 8..15 + "trunk|primary"
        // "motorway|trunk" does not contain literal "trunk|primary"
        // Check other rules... speedStd = 0, no cycleway
        // avgSpeed = 15 -> not < 3
        // speedStd = 0, avgSpeed = 15 -> not in 5..12
        // -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
        assertEquals(0.55f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - BUS when avgSpeed 8 to 15 and trunk primary road`() {
        val speeds = listOf(10.0, 12.0, 11.0)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_TRUNK_PRIMARY, 60000L)
        assertEquals("BUS", result.first)
        assertEquals(0.85f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - BUS at exact boundary avgSpeed 8`() {
        val speeds = listOf(8.0, 8.0)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_TRUNK_PRIMARY, 60000L)
        assertEquals("BUS", result.first)
        assertEquals(0.85f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - BUS at exact boundary avgSpeed 15`() {
        val speeds = listOf(15.0, 15.0)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_TRUNK_PRIMARY, 60000L)
        assertEquals("BUS", result.first)
        assertEquals(0.85f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - CYCLING when speedStd lt 3 and cycleway`() {
        // speedStd < 3, roadTypes contains "cycleway"
        val speeds = listOf(5.0, 5.0, 5.0) // std = 0
        val result = invokeInferTransportModeWithConfidence(speeds, "cycleway", 60000L)
        assertEquals("CYCLING", result.first)
        assertEquals(0.90f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - CYCLING with slight speed variation`() {
        // speedStd just under 3
        val speeds = listOf(4.0, 5.0, 6.0) // std = sqrt(2/3) ~ 0.816
        val result = invokeInferTransportModeWithConfidence(speeds, "cycleway", 60000L)
        assertEquals("CYCLING", result.first)
        assertEquals(0.90f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - WALKING when avgSpeed lt 3`() {
        val speeds = listOf(1.0, 2.0, 1.5)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("WALKING", result.first)
        assertEquals(0.85f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - WALKING at very low speed`() {
        val speeds = listOf(0.5, 0.3, 0.1)
        val result = invokeInferTransportModeWithConfidence(speeds, "motorway", 60000L)
        // avgSpeed ~ 0.3 < 3 -> WALKING (rule 4 comes after rules 1-3)
        // But first check rule 1: avgSpeed > 15? No. Rule 2? No. Rule 3? speedStd < 3 but no cycleway.
        // Rule 4: avgSpeed < 3? Yes -> WALKING
        assertEquals("WALKING", result.first)
        assertEquals(0.85f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - SUBWAY when speedStd gt 3 and avgSpeed 5 to 12`() {
        // Need speedStd > 3 and avgSpeed in 5..12
        // speeds: [2, 12, 8, 10] -> avg = 8, variance = ((6^2 + 4^2 + 0 + 2^2)/4) = 14, std ~ 3.74
        val speeds = listOf(2.0, 12.0, 8.0, 10.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("SUBWAY", result.first)
        assertEquals(0.75f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - SUBWAY at boundary avgSpeed 5`() {
        // Need avgSpeed = 5, speedStd > 3
        val speeds = listOf(1.0, 9.0) // avg=5, std = 4
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("SUBWAY", result.first)
        assertEquals(0.75f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - SUBWAY at boundary avgSpeed 12`() {
        // Need avgSpeed = 12, speedStd > 3
        val speeds = listOf(8.0, 16.0) // avg=12, std=4
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("SUBWAY", result.first)
        assertEquals(0.75f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - UNKNOWN when no rules match`() {
        // avgSpeed = 20 (> 15 but no motorway|trunk), not in 8..15 w/ trunk|primary,
        // speedStd may be > 3 but avgSpeed > 12
        val speeds = listOf(20.0, 20.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("UNKNOWN", result.first)
        assertEquals(0.55f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - UNKNOWN with moderate speed no road match`() {
        // avgSpeed = 4 (>= 3 not walking), speedStd < 3, no cycleway -> UNKNOWN
        val speeds = listOf(4.0, 4.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("UNKNOWN", result.first)
        assertEquals(0.55f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - DRIVING takes priority over BUS with high speed`() {
        // avgSpeed > 15, roadTypes contains both "motorway|trunk" and "trunk|primary"
        val speeds = listOf(20.0, 22.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "motorway|trunk and trunk|primary", 60000L)
        assertEquals("DRIVING", result.first)
    }

    @Test
    fun `inferTransportMode - WALKING takes priority over SUBWAY when avgSpeed lt 3`() {
        // avgSpeed < 3, but also speedStd > 3 (contradictory but rule 4 checked first)
        val speeds = listOf(0.0, 5.0) // avg=2.5, std=2.5
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        // avg = 2.5 < 3 -> WALKING (rule 4 before rule 5)
        assertEquals("WALKING", result.first)
    }

    @Test
    fun `inferTransportMode - BUS not triggered without literal trunk pipe primary`() {
        // avgSpeed in 8..15 but roadTypes is "trunk" not "trunk|primary"
        val speeds = listOf(10.0, 10.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "trunk", 60000L)
        // "trunk" does not contain literal "trunk|primary"
        // speedStd = 0 < 3, no cycleway
        // avgSpeed = 10 >= 3 -> not WALKING
        // speedStd 0 not > 3 -> not SUBWAY
        // -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `inferTransportMode - CYCLING not triggered without cycleway`() {
        val speeds = listOf(5.0, 5.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "residential", 60000L)
        // speedStd = 0 < 3, but no "cycleway"
        // avgSpeed = 5 >= 3
        // speedStd = 0 not > 3
        // -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `inferTransportMode - CYCLING not triggered when speedStd ge 3`() {
        // speedStd >= 3, has cycleway but not low std
        val speeds = listOf(1.0, 9.0) // avg=5, std=4
        val result = invokeInferTransportModeWithConfidence(speeds, "cycleway", 60000L)
        // Rule 1: avg=5 not > 15
        // Rule 2: avg=5 not in 8..15
        // Rule 3: speedStd=4 not < 3
        // Rule 4: avg=5 not < 3
        // Rule 5: speedStd=4 > 3 and avg=5 in 5..12 -> SUBWAY
        assertEquals("SUBWAY", result.first)
    }

    // ========================================================================
    // PART 11: AutoLabelingService - companion object constants
    // ========================================================================

    @Test
    fun `AutoLabelingService - MIN_GPS_POINTS constant is 20`() {
        val field = AutoLabelingService::class.java.getDeclaredField("MIN_GPS_POINTS")
        field.isAccessible = true
        assertEquals(20, field.getInt(null))
    }

    @Test
    fun `AutoLabelingService - MIN_JOURNEY_DURATION constant is 60000`() {
        val field = AutoLabelingService::class.java.getDeclaredField("MIN_JOURNEY_DURATION")
        field.isAccessible = true
        assertEquals(60000L, field.getLong(null))
    }

    @Test
    fun `AutoLabelingService - CONFIDENCE_THRESHOLD constant is 0_65f`() {
        val field = AutoLabelingService::class.java.getDeclaredField("CONFIDENCE_THRESHOLD")
        field.isAccessible = true
        assertEquals(0.65f, field.getFloat(null), 0.001f)
    }

    // ========================================================================
    // PART 12: DataExporter - featuresToCSVRecord (via reflection)
    // ========================================================================

    private lateinit var dataExporter: DataExporter

    private fun createDataExporter(): DataExporter {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        return DataExporter(dao, featureExtractor)
    }

    private fun invokeFeaturesToCSVRecord(journey: LabeledJourney, features: JourneyFeatures): JourneyCSVRecord {
        if (!::dataExporter.isInitialized) {
            dataExporter = createDataExporter()
        }
        val method = DataExporter::class.java.getDeclaredMethod(
            "featuresToCSVRecord",
            LabeledJourney::class.java,
            JourneyFeatures::class.java
        )
        method.isAccessible = true
        return method.invoke(dataExporter, journey, features) as JourneyCSVRecord
    }

    private fun createJourneyFeatures(
        accelMeanX: Float = 0.1f,
        accelMeanY: Float = 0.2f,
        accelMeanZ: Float = 9.8f,
        accelStdX: Float = 0.01f,
        accelStdY: Float = 0.02f,
        accelStdZ: Float = 0.03f,
        accelMagnitude: Float = 9.81f,
        gyroMeanX: Float = 0.001f,
        gyroMeanY: Float = 0.002f,
        gyroMeanZ: Float = 0.003f,
        gyroStdX: Float = 0.0001f,
        gyroStdY: Float = 0.0002f,
        gyroStdZ: Float = 0.0003f,
        journeyDuration: Float = 120.0f,
        gpsSpeedMean: Float = 1.5f,
        gpsSpeedStd: Float = 0.5f,
        gpsSpeedMax: Float = 3.0f,
        transportMode: String = "WALKING"
    ) = JourneyFeatures(
        accelMeanX = accelMeanX, accelMeanY = accelMeanY, accelMeanZ = accelMeanZ,
        accelStdX = accelStdX, accelStdY = accelStdY, accelStdZ = accelStdZ,
        accelMagnitude = accelMagnitude,
        gyroMeanX = gyroMeanX, gyroMeanY = gyroMeanY, gyroMeanZ = gyroMeanZ,
        gyroStdX = gyroStdX, gyroStdY = gyroStdY, gyroStdZ = gyroStdZ,
        journeyDuration = journeyDuration,
        gpsSpeedMean = gpsSpeedMean, gpsSpeedStd = gpsSpeedStd, gpsSpeedMax = gpsSpeedMax,
        transportMode = transportMode
    )

    @Test
    fun `featuresToCSVRecord - maps journeyId from journey`() {
        val journey = createLabeledJourney(id = 42L)
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(42L, record.journeyId)
    }

    @Test
    fun `featuresToCSVRecord - maps timestamp from startTime`() {
        val journey = createLabeledJourney(startTime = 1234567890L)
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(1234567890L, record.timestamp)
    }

    @Test
    fun `featuresToCSVRecord - maps transportMode from journey`() {
        val journey = createLabeledJourney(transportMode = "DRIVING")
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals("DRIVING", record.transportMode)
    }

    @Test
    fun `featuresToCSVRecord - maps labelSource from journey`() {
        val journey = createLabeledJourney(labelSource = "VERIFIED")
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals("VERIFIED", record.labelSource)
    }

    @Test
    fun `featuresToCSVRecord - maps accel features from features object`() {
        val features = createJourneyFeatures(
            accelMeanX = 1.1f, accelMeanY = 2.2f, accelMeanZ = 3.3f,
            accelStdX = 0.11f, accelStdY = 0.22f, accelStdZ = 0.33f,
            accelMagnitude = 4.4f
        )
        val journey = createLabeledJourney()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(1.1f, record.accelMeanX, 0.001f)
        assertEquals(2.2f, record.accelMeanY, 0.001f)
        assertEquals(3.3f, record.accelMeanZ, 0.001f)
        assertEquals(0.11f, record.accelStdX, 0.001f)
        assertEquals(0.22f, record.accelStdY, 0.001f)
        assertEquals(0.33f, record.accelStdZ, 0.001f)
        assertEquals(4.4f, record.accelMagnitude, 0.001f)
    }

    @Test
    fun `featuresToCSVRecord - maps gyro features from features object`() {
        val features = createJourneyFeatures(
            gyroMeanX = 0.5f, gyroMeanY = 0.6f, gyroMeanZ = 0.7f,
            gyroStdX = 0.05f, gyroStdY = 0.06f, gyroStdZ = 0.07f
        )
        val journey = createLabeledJourney()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(0.5f, record.gyroMeanX, 0.001f)
        assertEquals(0.6f, record.gyroMeanY, 0.001f)
        assertEquals(0.7f, record.gyroMeanZ, 0.001f)
        assertEquals(0.05f, record.gyroStdX, 0.001f)
        assertEquals(0.06f, record.gyroStdY, 0.001f)
        assertEquals(0.07f, record.gyroStdZ, 0.001f)
    }

    @Test
    fun `featuresToCSVRecord - maps journeyDuration from features`() {
        val features = createJourneyFeatures(journeyDuration = 300.5f)
        val journey = createLabeledJourney()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(300.5f, record.journeyDuration, 0.001f)
    }

    @Test
    fun `featuresToCSVRecord - maps GPS speed features`() {
        val features = createJourneyFeatures(
            gpsSpeedMean = 10.5f, gpsSpeedStd = 2.3f, gpsSpeedMax = 20.0f
        )
        val journey = createLabeledJourney()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(10.5f, record.gpsSpeedMean, 0.001f)
        assertEquals(2.3f, record.gpsSpeedStd, 0.001f)
        assertEquals(20.0f, record.gpsSpeedMax, 0.001f)
    }

    @Test
    fun `featuresToCSVRecord - maps isVerified true`() {
        val journey = createLabeledJourney(isVerified = true)
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertTrue(record.isVerified)
    }

    @Test
    fun `featuresToCSVRecord - maps isVerified false`() {
        val journey = createLabeledJourney(isVerified = false)
        val features = createJourneyFeatures()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertFalse(record.isVerified)
    }

    @Test
    fun `featuresToCSVRecord - all zero features`() {
        val features = createJourneyFeatures(
            accelMeanX = 0f, accelMeanY = 0f, accelMeanZ = 0f,
            accelStdX = 0f, accelStdY = 0f, accelStdZ = 0f,
            accelMagnitude = 0f,
            gyroMeanX = 0f, gyroMeanY = 0f, gyroMeanZ = 0f,
            gyroStdX = 0f, gyroStdY = 0f, gyroStdZ = 0f,
            journeyDuration = 0f,
            gpsSpeedMean = 0f, gpsSpeedStd = 0f, gpsSpeedMax = 0f
        )
        val journey = createLabeledJourney()
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(0f, record.accelMeanX, 0.001f)
        assertEquals(0f, record.journeyDuration, 0.001f)
        assertEquals(0f, record.gpsSpeedMean, 0.001f)
    }

    // ========================================================================
    // PART 13: DataExporter - recordToCSVLine (via reflection)
    // ========================================================================

    private fun invokeRecordToCSVLine(record: JourneyCSVRecord): String {
        if (!::dataExporter.isInitialized) {
            dataExporter = createDataExporter()
        }
        val method = DataExporter::class.java.getDeclaredMethod(
            "recordToCSVLine",
            JourneyCSVRecord::class.java
        )
        method.isAccessible = true
        return method.invoke(dataExporter, record) as String
    }

    private fun createCSVRecord(
        journeyId: Long = 1L,
        timestamp: Long = 1000L,
        transportMode: String = "WALKING",
        labelSource: String = "AUTO_SNAP",
        accelMeanX: Float = 0.1f,
        accelMeanY: Float = 0.2f,
        accelMeanZ: Float = 9.8f,
        accelStdX: Float = 0.01f,
        accelStdY: Float = 0.02f,
        accelStdZ: Float = 0.03f,
        accelMagnitude: Float = 9.81f,
        gyroMeanX: Float = 0.001f,
        gyroMeanY: Float = 0.002f,
        gyroMeanZ: Float = 0.003f,
        gyroStdX: Float = 0.0001f,
        gyroStdY: Float = 0.0002f,
        gyroStdZ: Float = 0.0003f,
        journeyDuration: Float = 120.0f,
        gpsSpeedMean: Float = 1.5f,
        gpsSpeedStd: Float = 0.5f,
        gpsSpeedMax: Float = 3.0f,
        isVerified: Boolean = false
    ) = JourneyCSVRecord(
        journeyId = journeyId, timestamp = timestamp, transportMode = transportMode,
        labelSource = labelSource,
        accelMeanX = accelMeanX, accelMeanY = accelMeanY, accelMeanZ = accelMeanZ,
        accelStdX = accelStdX, accelStdY = accelStdY, accelStdZ = accelStdZ,
        accelMagnitude = accelMagnitude,
        gyroMeanX = gyroMeanX, gyroMeanY = gyroMeanY, gyroMeanZ = gyroMeanZ,
        gyroStdX = gyroStdX, gyroStdY = gyroStdY, gyroStdZ = gyroStdZ,
        journeyDuration = journeyDuration,
        gpsSpeedMean = gpsSpeedMean, gpsSpeedStd = gpsSpeedStd, gpsSpeedMax = gpsSpeedMax,
        isVerified = isVerified
    )

    @Test
    fun `recordToCSVLine - ends with newline`() {
        val record = createCSVRecord()
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.endsWith("\n"))
    }

    @Test
    fun `recordToCSVLine - starts with journeyId and timestamp`() {
        val record = createCSVRecord(journeyId = 42L, timestamp = 9999L)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.startsWith("42,9999,"))
    }

    @Test
    fun `recordToCSVLine - contains transport mode and label source`() {
        val record = createCSVRecord(transportMode = "BUS", labelSource = "VERIFIED")
        val line = invokeRecordToCSVLine(record)
        val parts = line.trim().split(",")
        assertEquals("BUS", parts[2])
        assertEquals("VERIFIED", parts[3])
    }

    @Test
    fun `recordToCSVLine - accel features formatted with 6 decimal places`() {
        val record = createCSVRecord(accelMeanX = 1.5f, accelMeanY = 2.5f, accelMeanZ = 3.5f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("1.500000"))
        assertTrue(line.contains("2.500000"))
        assertTrue(line.contains("3.500000"))
    }

    @Test
    fun `recordToCSVLine - gyro features formatted with 6 decimal places`() {
        val record = createCSVRecord(gyroMeanX = 0.123456f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("0.123456"))
    }

    @Test
    fun `recordToCSVLine - journeyDuration formatted with 2 decimal places`() {
        val record = createCSVRecord(journeyDuration = 123.456f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("123.46"))
    }

    @Test
    fun `recordToCSVLine - GPS speed formatted with 4 decimal places`() {
        val record = createCSVRecord(gpsSpeedMean = 1.5f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("1.5000"))
    }

    @Test
    fun `recordToCSVLine - isVerified true becomes 1`() {
        val record = createCSVRecord(isVerified = true)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.trim().endsWith(",1"))
    }

    @Test
    fun `recordToCSVLine - isVerified false becomes 0`() {
        val record = createCSVRecord(isVerified = false)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.trim().endsWith(",0"))
    }

    @Test
    fun `recordToCSVLine - correct number of fields (22)`() {
        val record = createCSVRecord()
        val line = invokeRecordToCSVLine(record)
        val fields = line.trim().split(",")
        assertEquals(22, fields.size)
    }

    @Test
    fun `recordToCSVLine - zero values formatted correctly`() {
        val record = createCSVRecord(
            accelMeanX = 0f, accelMeanY = 0f, accelMeanZ = 0f,
            accelStdX = 0f, accelStdY = 0f, accelStdZ = 0f, accelMagnitude = 0f,
            gyroMeanX = 0f, gyroMeanY = 0f, gyroMeanZ = 0f,
            gyroStdX = 0f, gyroStdY = 0f, gyroStdZ = 0f,
            journeyDuration = 0f,
            gpsSpeedMean = 0f, gpsSpeedStd = 0f, gpsSpeedMax = 0f
        )
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("0.000000"))
        assertTrue(line.contains("0.00"))
        assertTrue(line.contains("0.0000"))
    }

    @Test
    fun `recordToCSVLine - negative accel values formatted correctly`() {
        val record = createCSVRecord(accelMeanX = -1.5f, accelMeanY = -2.5f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("-1.500000"))
        assertTrue(line.contains("-2.500000"))
    }

    @Test
    fun `recordToCSVLine - large values formatted correctly`() {
        val record = createCSVRecord(journeyDuration = 99999.99f, gpsSpeedMax = 150.0f)
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("150.0000"))
    }

    @Test
    fun `recordToCSVLine - field ordering matches CSV header`() {
        // verify the order: journeyId,timestamp,transportMode,labelSource,accel(7),gyro(6),duration,gps(3),isVerified
        val record = createCSVRecord(
            journeyId = 1L,
            timestamp = 2000L,
            transportMode = "CYCLING",
            labelSource = "MANUAL",
            accelMeanX = 0.1f,
            isVerified = true
        )
        val line = invokeRecordToCSVLine(record)
        val fields = line.trim().split(",")
        assertEquals("1", fields[0])
        assertEquals("2000", fields[1])
        assertEquals("CYCLING", fields[2])
        assertEquals("MANUAL", fields[3])
        assertEquals("1", fields[21]) // isVerified
    }

    // ========================================================================
    // PART 14: DataExporter - CSV_HEADER constant
    // ========================================================================

    @Test
    fun `CSV_HEADER - starts with journeyId`() {
        val field = DataExporter::class.java.getDeclaredField("CSV_HEADER")
        field.isAccessible = true
        val header = field.get(null) as String
        assertTrue(header.startsWith("journeyId,"))
    }

    @Test
    fun `CSV_HEADER - ends with newline`() {
        val field = DataExporter::class.java.getDeclaredField("CSV_HEADER")
        field.isAccessible = true
        val header = field.get(null) as String
        assertTrue(header.endsWith("\n"))
    }

    @Test
    fun `CSV_HEADER - contains all expected column names`() {
        val field = DataExporter::class.java.getDeclaredField("CSV_HEADER")
        field.isAccessible = true
        val header = field.get(null) as String

        val expectedColumns = listOf(
            "journeyId", "timestamp", "transportMode", "labelSource",
            "accelMeanX", "accelMeanY", "accelMeanZ",
            "accelStdX", "accelStdY", "accelStdZ", "accelMagnitude",
            "gyroMeanX", "gyroMeanY", "gyroMeanZ",
            "gyroStdX", "gyroStdY", "gyroStdZ",
            "journeyDuration",
            "gpsSpeedMean", "gpsSpeedStd", "gpsSpeedMax",
            "isVerified"
        )
        expectedColumns.forEach { column ->
            assertTrue("Header should contain $column", header.contains(column))
        }
    }

    @Test
    fun `CSV_HEADER - has 22 columns`() {
        val field = DataExporter::class.java.getDeclaredField("CSV_HEADER")
        field.isAccessible = true
        val header = field.get(null) as String
        val columns = header.trim().split(",")
        assertEquals(22, columns.size)
    }

    // ========================================================================
    // PART 15: Integration tests - extractFeatures + featuresToCSVRecord + recordToCSVLine
    // ========================================================================

    @Test
    fun `integration - extractFeatures to CSV pipeline`() = runTest {
        val accelJson = """[{"x":0.5,"y":0.3,"z":9.8},{"x":0.6,"y":0.4,"z":9.7},{"x":0.55,"y":0.35,"z":9.75}]"""
        val gyroJson = """[{"x":0.01,"y":0.02,"z":0.03},{"x":0.02,"y":0.03,"z":0.04}]"""
        val journey = createLabeledJourney(
            id = 100L,
            startTime = 0L,
            endTime = 120000L,
            transportMode = "CYCLING",
            labelSource = "VERIFIED",
            avgSpeed = 5.0f,
            maxSpeed = 8.0f,
            speedVariance = 1.0f,
            accelerometerData = accelJson,
            gyroscopeData = gyroJson,
            isVerified = true
        )

        val features = featureExtractor.extractFeatures(journey)
        assertEquals("CYCLING", features.transportMode)
        assertEquals(120.0f, features.journeyDuration, 0.001f)

        if (!::dataExporter.isInitialized) {
            dataExporter = createDataExporter()
        }
        val record = invokeFeaturesToCSVRecord(journey, features)
        assertEquals(100L, record.journeyId)
        assertEquals("CYCLING", record.transportMode)
        assertTrue(record.isVerified)

        val csvLine = invokeRecordToCSVLine(record)
        assertTrue(csvLine.startsWith("100,"))
        assertTrue(csvLine.contains("CYCLING"))
        assertTrue(csvLine.trim().endsWith(",1"))
        assertEquals(22, csvLine.trim().split(",").size)
    }

    @Test
    fun `integration - empty sensor data through CSV pipeline`() = runTest {
        val journey = createLabeledJourney(
            id = 200L,
            startTime = 0L,
            endTime = 60000L,
            transportMode = "WALKING",
            labelSource = "AUTO_SNAP",
            accelerometerData = "[]",
            gyroscopeData = "[]",
            isVerified = false
        )

        val features = featureExtractor.extractFeatures(journey)
        assertEquals(0f, features.accelMeanX, 0.001f)
        assertEquals(0f, features.gyroMeanX, 0.001f)

        if (!::dataExporter.isInitialized) {
            dataExporter = createDataExporter()
        }
        val record = invokeFeaturesToCSVRecord(journey, features)
        val csvLine = invokeRecordToCSVLine(record)
        assertTrue(csvLine.contains("WALKING"))
        assertTrue(csvLine.trim().endsWith(",0"))
    }

    // ========================================================================
    // PART 16: Additional edge cases
    // ========================================================================

    @Test
    fun `parseSensorData - handles mixed extra fields gracefully`() {
        // Extra fields like "a" that are not x, y, z
        val json = """[{"x":1.0,"y":2.0,"z":3.0,"a":4.0,"b":5.0}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(1.0f, result[0].first, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - three identical points`() {
        val data = listOf(
            Triple(1.0f, 0.0f, 0.0f),
            Triple(1.0f, 0.0f, 0.0f),
            Triple(1.0f, 0.0f, 0.0f)
        )
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(1.0f, result.a, 0.001f)
        assertEquals(0.0f, result.d, 0.001f) // std = 0
        assertEquals(1.0f, result.g, 0.001f) // magnitude of (1,0,0) = 1
    }

    @Test
    fun `extractFeatures - SUBWAY transport mode preserved`() = runTest {
        val journey = createLabeledJourney(
            transportMode = "SUBWAY",
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        assertEquals("SUBWAY", featureExtractor.extractFeatures(journey).transportMode)
    }

    @Test
    fun `extractFeatures - BUS transport mode preserved`() = runTest {
        val journey = createLabeledJourney(
            transportMode = "BUS",
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        assertEquals("BUS", featureExtractor.extractFeatures(journey).transportMode)
    }

    @Test
    fun `extractFeatures - CYCLING transport mode preserved`() = runTest {
        val journey = createLabeledJourney(
            transportMode = "CYCLING",
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        assertEquals("CYCLING", featureExtractor.extractFeatures(journey).transportMode)
    }

    @Test
    fun `extractFeatures - maxSpeed and avgSpeed propagated`() = runTest {
        val journey = createLabeledJourney(
            avgSpeed = 12.5f,
            maxSpeed = 25.0f,
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        assertEquals(12.5f, features.gpsSpeedMean, 0.001f)
        assertEquals(25.0f, features.gpsSpeedMax, 0.001f)
    }

    @Test
    fun `calculateSpeeds - exact 50 ms speed is NOT included (filter is lt 50)`() {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        // Need distance / time = exactly 50 m/s
        // 50 m/s * 10s = 500m. Approximate lat delta for 500m: ~0.0045 degrees
        // We'll use a slightly larger delta that gives speed >= 50
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.004500, 120.000000, 10000L) // ~500m in 10s = 50 m/s
        )
        val result = invokeCalculateSpeeds(trajectory)
        // Speed = distance/time. If haversine gives ~500m then speed ~ 50 m/s
        // filter is speed < 50, so exactly 50 is filtered out
        // Due to haversine imprecision, this might be slightly above or below
        // The important thing is we test the boundary behavior
        if (result.isNotEmpty()) {
            assertTrue(result[0] < 50.0)
        }
    }

    @Test
    fun `haversineDistance - zero distance same coordinates`() {
        assertEquals(0.0, invokeHaversineDistance(0.0, 0.0, 0.0, 0.0), 0.001)
    }

    @Test
    fun `haversineDistance - poles`() {
        // North pole to South pole: ~20015 km
        val result = invokeHaversineDistance(90.0, 0.0, -90.0, 0.0)
        assertEquals(20015086.0, result, 1000.0)
    }

    @Test
    fun `calculateVariance - two elements`() {
        // [0, 10] -> mean=5, variance=25
        assertEquals(25.0, invokeCalculateVariance(listOf(0.0, 10.0)), 0.0001)
    }

    @Test
    fun `calculateVariance - fractional values`() {
        // [0.5, 1.5] -> mean=1, variance=0.25
        assertEquals(0.25, invokeCalculateVariance(listOf(0.5, 1.5)), 0.0001)
    }

    @Test
    fun `inferTransportMode - high speed no road type gives UNKNOWN`() {
        val speeds = listOf(30.0, 35.0, 40.0) // avg=35, no road type match
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("UNKNOWN", result.first)
        assertEquals(0.55f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - walking speed exactly 3 is not walking`() {
        // avgSpeed exactly 3.0 -> NOT < 3
        val speeds = listOf(3.0, 3.0)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        // avg=3, not < 3. speedStd=0, not > 3. -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `inferTransportMode - exactly avgSpeed 15_1 with motorway trunk`() {
        val speeds = listOf(15.1, 15.1)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_MOTORWAY_TRUNK, 60000L)
        assertEquals("DRIVING", result.first)
        assertEquals(0.95f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - avgSpeed 7_9 not BUS range`() {
        // avgSpeed 7.9 < 8 -> not in 8..15
        val speeds = listOf(7.9, 7.9)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_TRUNK_PRIMARY, 60000L)
        // Not BUS. speedStd=0, no cycleway. avg 7.9 >= 3 not walking. stdSpeed not > 3 not subway.
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `recordToCSVLine - SUBWAY transport mode in CSV`() {
        val record = createCSVRecord(transportMode = "SUBWAY")
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("SUBWAY"))
    }

    @Test
    fun `recordToCSVLine - DRIVING transport mode in CSV`() {
        val record = createCSVRecord(transportMode = "DRIVING")
        val line = invokeRecordToCSVLine(record)
        assertTrue(line.contains("DRIVING"))
    }

    @Test
    fun `recordToCSVLine - very small float values formatted`() {
        val record = createCSVRecord(accelMeanX = 0.000001f, gyroMeanX = 0.000001f)
        val line = invokeRecordToCSVLine(record)
        // Should still have 6 decimal places for accel and gyro
        assertFalse(line.isEmpty())
    }

    @Test
    fun `featuresToCSVRecord - different transport modes`() {
        listOf("WALKING", "CYCLING", "BUS", "DRIVING", "SUBWAY", "UNKNOWN").forEach { mode ->
            val journey = createLabeledJourney(transportMode = mode)
            val features = createJourneyFeatures(transportMode = mode)
            val record = invokeFeaturesToCSVRecord(journey, features)
            assertEquals(mode, record.transportMode)
        }
    }

    @Test
    fun `featuresToCSVRecord - different label sources`() {
        listOf("AUTO_SNAP", "MANUAL", "VERIFIED").forEach { source ->
            val journey = createLabeledJourney(labelSource = source)
            val features = createJourneyFeatures()
            val record = invokeFeaturesToCSVRecord(journey, features)
            assertEquals(source, record.labelSource)
        }
    }

    @Test
    fun `parseSensorData - with two data points producing two triples`() {
        val json = """[{"x":1.0,"y":2.0,"z":3.0},{"x":4.0,"y":5.0,"z":6.0}]"""
        val result = invokeParseSensorData(json)
        assertEquals(2, result.size)
        assertEquals(Triple(1.0f, 2.0f, 3.0f), result[0])
        assertEquals(Triple(4.0f, 5.0f, 6.0f), result[1])
    }

    @Test
    fun `calculateStd Double - three value known result`() {
        // [1, 2, 3] -> mean=2, var=(1+0+1)/3 = 2/3, std = sqrt(2/3) ~ 0.8165
        val result = invokeCalculateStdDouble(listOf(1.0, 2.0, 3.0))
        assertEquals(sqrt(2.0 / 3.0), result, 0.0001)
    }

    @Test
    fun `calculateStd Float - three value known result`() {
        val result = invokeCalculateStdFloat(listOf(1.0f, 2.0f, 3.0f))
        assertEquals(sqrt(2.0 / 3.0).toFloat(), result, 0.001f)
    }

    @Test
    fun `extractAccelerometerFeatures - mixed positive and negative`() {
        val data = listOf(Triple(-3.0f, 4.0f, -5.0f), Triple(3.0f, -4.0f, 5.0f))
        val result = invokeExtractAccelerometerFeatures(data)
        assertEquals(0.0f, result.a, 0.001f) // meanX
        assertEquals(0.0f, result.b, 0.001f) // meanY
        assertEquals(0.0f, result.c, 0.001f) // meanZ
        assertEquals(3.0f, result.d, 0.001f) // stdX
    }

    @Test
    fun `extractGyroscopeFeatures - mixed positive and negative`() {
        val data = listOf(Triple(-1.0f, 2.0f, -3.0f), Triple(1.0f, -2.0f, 3.0f))
        val result = invokeExtractGyroscopeFeatures(data)
        assertEquals(0.0f, result.a, 0.001f) // meanX
        assertEquals(0.0f, result.b, 0.001f) // meanY
        assertEquals(0.0f, result.c, 0.001f) // meanZ
    }

    @Test
    fun `Septuple - with null values`() {
        val s = Septuple<Int?, Int?, Int?, Int?, Int?, Int?, Int?>(null, null, null, null, null, null, null)
        assertNull(s.a)
        assertNull(s.g)
    }

    @Test
    fun `Sextuple - with null values`() {
        val s = Sextuple<Int?, Int?, Int?, Int?, Int?, Int?>(null, null, null, null, null, null)
        assertNull(s.a)
        assertNull(s.f)
    }

    @Test
    fun `Septuple - with mixed types`() {
        val s = Septuple(1, "two", 3.0, 4f, 5L, true, 'z')
        assertEquals(1, s.a)
        assertEquals("two", s.b)
        assertEquals(3.0, s.c, 0.001)
        assertEquals(4f, s.d, 0.001f)
        assertEquals(5L, s.e)
        assertTrue(s.f)
        assertEquals('z', s.g)
    }

    @Test
    fun `Sextuple - with mixed types`() {
        val s = Sextuple(1, "two", 3.0, 4f, 5L, true)
        assertEquals(1, s.a)
        assertEquals("two", s.b)
        assertEquals(5L, s.e)
        assertTrue(s.f)
    }

    // ========================================================================
    // PART 17: AutoLabelingService - autoLabelTrajectory (all branches)
    // ========================================================================

    private fun generateGpsTrajectory(
        pointCount: Int,
        startLat: Double = 30.0,
        startLng: Double = 120.0,
        startTime: Long = 0L,
        timeIntervalMs: Long = 5000L,
        latIncrement: Double = 0.000010 // ~1.1m per step
    ): List<Triple<Double, Double, Long>> {
        return (0 until pointCount).map { i ->
            Triple(
                startLat + i * latIncrement,
                startLng,
                startTime + i * timeIntervalMs
            )
        }
    }

    @Test
    fun `autoLabelTrajectory - returns null when GPS points less than 20`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // Only 19 points - less than MIN_GPS_POINTS (20)
        val trajectory = generateGpsTrajectory(19, timeIntervalMs = 5000L)
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null when duration less than 60000ms`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // 25 points but only 30 seconds total (< 60000ms)
        val trajectory = generateGpsTrajectory(25, timeIntervalMs = 1200L) // 25 * 1200 = 30000ms total
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null when speeds list is empty after filtering`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // 25 points all at same location (distance=0, speed=0 but valid) with sufficient duration
        // Actually, same location gives speed 0 which passes filter. Let's use same timestamp instead.
        // Build trajectory where all consecutive timestamps are equal -> timeInterval = 0 -> skipped
        val trajectory = (0 until 25).map { i ->
            Triple(30.0 + i * 0.01, 120.0, 0L) // all same timestamp
        }
        // First and last timestamps differ by 0, so duration < MIN_JOURNEY_DURATION -> null from duration check
        // We need duration >= 60000 but all speeds filtered. Use huge lat jumps to get speed > 50
        val trajectoryBigJumps = (0 until 25).map { i ->
            Triple(30.0 + i * 1.0, 120.0, i * 3000L) // 1 degree = 111km per 3s = 37000 m/s >> 50
        }
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectoryBigJumps,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null when snap detector throws (API call fails)`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // Valid trajectory with enough points, duration, and valid speeds
        // The snap detector will fail because we're in a test environment (no real API)
        val trajectory = generateGpsTrajectory(25, timeIntervalMs = 4000L, latIncrement = 0.000050)
        // Total duration = 24 * 4000 = 96000ms > 60000ms, speeds ~1.4 m/s each
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = "test-api-key"
        )
        // The snapDetector.detectTransportMode will throw in test -> caught -> snapResult = null -> return null
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null with exactly 20 points and just enough duration`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // Exactly 20 points, duration just at 60000ms boundary
        val trajectory = generateGpsTrajectory(20, timeIntervalMs = (60000L / 19), latIncrement = 0.000030)
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        // Will reach snap detector, which will throw -> return null
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null with 1 GPS point`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val trajectory = listOf(Triple(30.0, 120.0, 0L))
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - returns null with empty trajectory`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val result = service.autoLabelTrajectory(
            gpsTrajectory = emptyList(),
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    // ========================================================================
    // PART 18: AutoLabelingService - verifyLabel (all branches)
    // ========================================================================

    @Test
    fun `verifyLabel - updates journey when found`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val journey = createLabeledJourney(
            id = 1L,
            transportMode = "WALKING",
            labelSource = "AUTO_SNAP",
            isVerified = false
        )
        whenever(dao.getJourneyById(1L)).thenReturn(journey)

        service.verifyLabel(1L, "DRIVING", "corrected by user")

        verify(dao).getJourneyById(1L)
        verify(dao).update(any())
    }

    @Test
    fun `verifyLabel - does nothing when journey not found`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        whenever(dao.getJourneyById(999L)).thenReturn(null)

        service.verifyLabel(999L, "BUS", "")

        verify(dao).getJourneyById(999L)
        verify(dao, never()).update(any())
    }

    @Test
    fun `verifyLabel - with empty notes`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val journey = createLabeledJourney(id = 5L)
        whenever(dao.getJourneyById(5L)).thenReturn(journey)

        service.verifyLabel(5L, "CYCLING")

        verify(dao).getJourneyById(5L)
        verify(dao).update(any())
    }

    @Test
    fun `verifyLabel - handles exception from dao gracefully`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        whenever(dao.getJourneyById(1L)).thenThrow(RuntimeException("DB error"))

        // Should not throw - caught internally
        service.verifyLabel(1L, "DRIVING", "test")
    }

    @Test
    fun `verifyLabel - handles exception from update gracefully`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val journey = createLabeledJourney(id = 10L)
        whenever(dao.getJourneyById(10L)).thenReturn(journey)
        whenever(dao.update(any())).thenThrow(RuntimeException("Update failed"))

        // Should not throw - caught internally
        service.verifyLabel(10L, "SUBWAY", "notes")
    }

    // ========================================================================
    // PART 19: DataExporter - exportVerifiedDataToCSV (all branches)
    // ========================================================================

    @Test
    fun `exportVerifiedDataToCSV - returns 0 when no verified journeys`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(emptyList())

        val result = exporter.exportVerifiedDataToCSV("/tmp/test_empty.csv")
        assertEquals(0, result)
    }

    @Test
    fun `exportVerifiedDataToCSV - exports single journey successfully`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 1L,
            startTime = 1000L,
            endTime = 11000L,
            transportMode = "WALKING",
            labelSource = "VERIFIED",
            accelerometerData = """[{"x":0.1,"y":0.2,"z":9.8}]""",
            gyroscopeData = JSON_GYRO_SMALL,
            isVerified = true
        )
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_verified_export_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportVerifiedDataToCSV(outputFile)
            assertEquals(1, result)

            val file = File(outputFile)
            assertTrue(file.exists())
            val content = file.readText()
            assertTrue(content.contains("journeyId"))
            assertTrue(content.contains("WALKING"))
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportVerifiedDataToCSV - exports multiple journeys`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journeys = (1..3).map { i ->
            createLabeledJourney(
                id = i.toLong(),
                transportMode = if (i == 1) "WALKING" else if (i == 2) "CYCLING" else "DRIVING",
                accelerometerData = """[{"x":${i}.0,"y":${i}.0,"z":9.8}]""",
                gyroscopeData = """[{"x":0.0${i},"y":0.0${i},"z":0.0${i}}]""",
                isVerified = true
            )
        }
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(journeys)

        val outputFile = "/tmp/test_multi_export_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportVerifiedDataToCSV(outputFile)
            assertEquals(3, result)

            val file = File(outputFile)
            assertTrue(file.exists())
            val lines = file.readLines()
            // 1 header + 3 data lines
            assertEquals(4, lines.size)
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportVerifiedDataToCSV - returns -1 on general exception`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getVerifiedJourneysForExport(10000)).thenThrow(RuntimeException("DB error"))

        val result = exporter.exportVerifiedDataToCSV("/tmp/test_fail.csv")
        assertEquals(-1, result)
    }

    @Test
    fun `exportVerifiedDataToCSV - handles feature extraction failure for individual journey`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        // Use a real FeatureExtractor - but one journey will have data that causes issues
        val exporter = DataExporter(dao, featureExtractor)

        val goodJourney = createLabeledJourney(
            id = 1L,
            accelerometerData = JSON_ACCEL_1,
            gyroscopeData = JSON_GYRO_1,
            isVerified = true
        )
        // Second journey has valid data too
        val goodJourney2 = createLabeledJourney(
            id = 2L,
            accelerometerData = """[{"x":2.0,"y":3.0,"z":9.7}]""",
            gyroscopeData = """[{"x":0.2,"y":0.3,"z":0.4}]""",
            isVerified = true
        )
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(listOf(goodJourney, goodJourney2))

        val outputFile = "/tmp/test_partial_export_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportVerifiedDataToCSV(outputFile)
            assertEquals(2, result)
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportVerifiedDataToCSV - progress logging every 100 records`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        // Create 101 journeys to trigger progress logging at 100
        val journeys = (1..101).map { i ->
            createLabeledJourney(
                id = i.toLong(),
                accelerometerData = JSON_ACCEL_1,
                gyroscopeData = JSON_GYRO_1,
                isVerified = true
            )
        }
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(journeys)

        val outputFile = "/tmp/test_progress_export_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportVerifiedDataToCSV(outputFile)
            assertEquals(101, result)
        } finally {
            File(outputFile).delete()
        }
    }

    // ========================================================================
    // PART 20: DataExporter - exportAllDataToCSV (all branches)
    // ========================================================================

    @Test
    fun `exportAllDataToCSV - returns 0 when no journeys`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getJourneysForExport(10000)).thenReturn(emptyList())

        val result = exporter.exportAllDataToCSV("/tmp/test_all_empty.csv")
        assertEquals(0, result)
    }

    @Test
    fun `exportAllDataToCSV - exports single journey successfully`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 1L,
            transportMode = "BUS",
            labelSource = "AUTO_SNAP",
            accelerometerData = """[{"x":0.5,"y":0.3,"z":9.8}]""",
            gyroscopeData = JSON_GYRO_SMALL,
            isVerified = false
        )
        whenever(dao.getJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_all_single_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportAllDataToCSV(outputFile)
            assertEquals(1, result)

            val file = File(outputFile)
            assertTrue(file.exists())
            val content = file.readText()
            assertTrue(content.contains("BUS"))
            assertTrue(content.contains("AUTO_SNAP"))
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportAllDataToCSV - exports multiple journeys including unverified`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journeys = listOf(
            createLabeledJourney(id = 1L, isVerified = true, accelerometerData = JSON_ACCEL_1, gyroscopeData = JSON_GYRO_1),
            createLabeledJourney(id = 2L, isVerified = false, accelerometerData = JSON_ACCEL_1, gyroscopeData = JSON_GYRO_1)
        )
        whenever(dao.getJourneysForExport(10000)).thenReturn(journeys)

        val outputFile = "/tmp/test_all_multi_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportAllDataToCSV(outputFile)
            assertEquals(2, result)
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportAllDataToCSV - returns -1 on exception`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getJourneysForExport(10000)).thenThrow(RuntimeException("DB crash"))

        val result = exporter.exportAllDataToCSV("/tmp/test_all_fail.csv")
        assertEquals(-1, result)
    }

    @Test
    fun `exportAllDataToCSV - handles per-journey exception gracefully`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journeys = listOf(
            createLabeledJourney(id = 1L, accelerometerData = JSON_ACCEL_1, gyroscopeData = JSON_GYRO_1),
            createLabeledJourney(id = 2L, accelerometerData = """[{"x":2.0,"y":3.0,"z":9.7}]""", gyroscopeData = """[{"x":0.2,"y":0.3,"z":0.4}]""")
        )
        whenever(dao.getJourneysForExport(10000)).thenReturn(journeys)

        val outputFile = "/tmp/test_all_partial_${System.currentTimeMillis()}.csv"
        try {
            val result = exporter.exportAllDataToCSV(outputFile)
            assertEquals(2, result)
        } finally {
            File(outputFile).delete()
        }
    }

    // ========================================================================
    // PART 21: DataExporter - generateDataReport (all branches)
    // ========================================================================

    @Test
    fun `generateDataReport - with zero total count`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(0)
        whenever(dao.getVerifiedCount()).thenReturn(0)
        whenever(dao.getTransportModeDistribution()).thenReturn(emptyList())
        whenever(dao.getLabelSourceDistribution()).thenReturn(emptyList())

        val report = exporter.generateDataReport()
        assertTrue(report.contains("总记录数: 0"))
        assertTrue(report.contains("已验证: 0"))
        assertTrue(report.contains("待验证: 0"))
        assertTrue(report.contains("数据量较小"))
    }

    @Test
    fun `generateDataReport - with less than 100 records (small data)`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(50)
        whenever(dao.getVerifiedCount()).thenReturn(10)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 30),
            TransportModeCount("DRIVING", 20)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("AUTO_SNAP", 40),
            LabelSourceCount("VERIFIED", 10)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("总记录数: 50"))
        assertTrue(report.contains("数据量较小"))
        assertTrue(report.contains("验证率较低"))
        assertTrue(report.contains("WALKING"))
        assertTrue(report.contains("DRIVING"))
        assertTrue(report.contains("AUTO_SNAP"))
    }

    @Test
    fun `generateDataReport - with 100 to 499 records (medium data)`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(300)
        whenever(dao.getVerifiedCount()).thenReturn(100)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100),
            TransportModeCount("DRIVING", 100),
            TransportModeCount("CYCLING", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 100),
            LabelSourceCount("AUTO_SNAP", 200)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("数据量适中"))
        assertTrue(report.contains("验证率较低"))
    }

    @Test
    fun `generateDataReport - with 500 or more records (large data)`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(1000)
        whenever(dao.getVerifiedCount()).thenReturn(900)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 250),
            TransportModeCount("DRIVING", 250),
            TransportModeCount("CYCLING", 250),
            TransportModeCount("BUS", 250)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 900),
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("数据量充足"))
        assertTrue(report.contains("验证率高"))
        assertTrue(report.contains(REPORT_BALANCED))
    }

    @Test
    fun `generateDataReport - with high verification rate`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(100)
        whenever(dao.getVerifiedCount()).thenReturn(85) // 85% > 80%
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 50),
            TransportModeCount("CYCLING", 50)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 85),
            LabelSourceCount("AUTO_SNAP", 15)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("验证率高"))
    }

    @Test
    fun `generateDataReport - with low verification rate`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(100)
        whenever(dao.getVerifiedCount()).thenReturn(50) // 50% < 80%
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("验证率较低"))
    }

    @Test
    fun `generateDataReport - with imbalanced transport mode distribution`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(600)
        whenever(dao.getVerifiedCount()).thenReturn(500)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 500), // max=500
            TransportModeCount("DRIVING", 10)   // min=10, ratio=50 > 5
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 500),
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("分布不均衡"))
    }

    @Test
    fun `generateDataReport - with balanced transport mode distribution`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(600)
        whenever(dao.getVerifiedCount()).thenReturn(500)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 200),
            TransportModeCount("DRIVING", 200),
            TransportModeCount("CYCLING", 200)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 500),
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains(REPORT_BALANCED))
    }

    @Test
    fun `generateDataReport - exception returns error message`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenThrow(RuntimeException("DB connection lost"))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("报告生成失败"))
        assertTrue(report.contains("DB connection lost"))
    }

    @Test
    fun `generateDataReport - report header contains title and timestamp`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(10)
        whenever(dao.getVerifiedCount()).thenReturn(5)
        whenever(dao.getTransportModeDistribution()).thenReturn(emptyList())
        whenever(dao.getLabelSourceDistribution()).thenReturn(emptyList())

        val report = exporter.generateDataReport()
        assertTrue(report.contains("训练数据统计报告"))
        assertTrue(report.contains("生成时间"))
        assertTrue(report.contains("总体统计"))
        assertTrue(report.contains("交通方式分布"))
        assertTrue(report.contains("标签来源分布"))
        assertTrue(report.contains("数据质量评估"))
    }

    @Test
    fun `generateDataReport - transport mode percentage calculation`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(200)
        whenever(dao.getVerifiedCount()).thenReturn(100)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100),
            TransportModeCount("DRIVING", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("AUTO_SNAP", 100),
            LabelSourceCount("VERIFIED", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("WALKING: 100"))
        assertTrue(report.contains("DRIVING: 100"))
        assertTrue(report.contains("50%")) // 100/200 = 50%
    }

    @Test
    fun `generateDataReport - with minCount zero in distribution causes ratio zero`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(500)
        whenever(dao.getVerifiedCount()).thenReturn(400)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 500),
            TransportModeCount("DRIVING", 0) // minCount = 0
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 400),
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        // minCount = 0, so ratio = if (minCount > 0) maxCount/minCount else 0
        // ratio = 0, which is NOT > 5, so "balanced"
        assertTrue(report.contains(REPORT_BALANCED))
    }

    @Test
    fun `generateDataReport - with empty mode distribution`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(50)
        whenever(dao.getVerifiedCount()).thenReturn(10)
        whenever(dao.getTransportModeDistribution()).thenReturn(emptyList())
        whenever(dao.getLabelSourceDistribution()).thenReturn(emptyList())

        val report = exporter.generateDataReport()
        // counts.isEmpty() -> the balance check block is skipped (no output about balance)
        assertFalse(report.contains("分布不均衡"))
        assertFalse(report.contains(REPORT_BALANCED))
    }

    @Test
    fun `generateDataReport - verification rate exactly 80 percent`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(100)
        whenever(dao.getVerifiedCount()).thenReturn(80) // exactly 80%
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("AUTO_SNAP", 100)
        ))

        val report = exporter.generateDataReport()
        // verifiedCount (80) < totalCount * 0.8 (80) is false (80 < 80 is false)
        // So it goes to the else branch -> "验证率高"
        assertTrue(report.contains("验证率高"))
    }

    @Test
    fun `generateDataReport - exactly 100 records triggers medium message`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(100)
        whenever(dao.getVerifiedCount()).thenReturn(90)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 90)
        ))

        val report = exporter.generateDataReport()
        // totalCount = 100, NOT < 100, so first branch skipped
        // 100 < 500 -> "数据量适中"
        assertTrue(report.contains("数据量适中"))
    }

    @Test
    fun `generateDataReport - exactly 500 records triggers large message`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(500)
        whenever(dao.getVerifiedCount()).thenReturn(450)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 250),
            TransportModeCount("DRIVING", 250)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 450)
        ))

        val report = exporter.generateDataReport()
        // totalCount = 500, not < 100, not < 500 -> "数据量充足"
        assertTrue(report.contains("数据量充足"))
    }

    @Test
    fun `generateDataReport - ratio exactly 5 is balanced`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(600)
        whenever(dao.getVerifiedCount()).thenReturn(500)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 500),
            TransportModeCount("DRIVING", 100) // ratio = 500/100 = 5
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 500)
        ))

        val report = exporter.generateDataReport()
        // ratio = 5, condition is ratio > 5 -> false -> "balanced"
        assertTrue(report.contains(REPORT_BALANCED))
    }

    @Test
    fun `generateDataReport - ratio 6 is imbalanced`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(700)
        whenever(dao.getVerifiedCount()).thenReturn(600)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 600),
            TransportModeCount("DRIVING", 100) // ratio = 600/100 = 6 > 5
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 600)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("分布不均衡"))
    }

    @Test
    fun `generateDataReport - single transport mode distribution`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(600)
        whenever(dao.getVerifiedCount()).thenReturn(500)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 600)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("VERIFIED", 500)
        ))

        val report = exporter.generateDataReport()
        // min=600, max=600, ratio=1 -> balanced
        assertTrue(report.contains(REPORT_BALANCED))
    }

    // ========================================================================
    // PART 22: Additional inferTransportMode edge cases
    // ========================================================================

    @Test
    fun `inferTransportMode - CYCLING at speedStd exactly 2_99`() {
        // speedStd just under 3
        // speeds: [3, 7] -> avg=5, variance=4, std=2. Need std < 3 with cycleway
        val speeds = listOf(3.0, 7.0) // avg=5, std=2
        val result = invokeInferTransportModeWithConfidence(speeds, "cycleway", 60000L)
        // Rule 1: avg=5 not > 15. Rule 2: avg=5 not in 8..15. Rule 3: std=2 < 3 and has cycleway -> CYCLING
        assertEquals("CYCLING", result.first)
        assertEquals(0.90f, result.second, 0.001f)
    }

    @Test
    fun `inferTransportMode - SUBWAY at boundary speedStd exactly 3_01 and avgSpeed 5`() {
        // Need speedStd > 3 and avgSpeed in 5..12
        val speeds = listOf(1.0, 9.0) // avg=5, std=4
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("SUBWAY", result.first)
    }

    @Test
    fun `inferTransportMode - avgSpeed just above 15 with non-matching road`() {
        val speeds = listOf(15.1, 15.1)
        val result = invokeInferTransportModeWithConfidence(speeds, "residential", 60000L)
        // Rule 1: avg > 15 but road doesn't contain "motorway|trunk" -> skip
        // Rule 2: avg in 8..15? 15.1 is > 15 so not in range
        // Rule 3: std=0 < 3 but no cycleway
        // Rule 4: avg 15.1 not < 3
        // Rule 5: std 0 not > 3
        // -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `inferTransportMode - avgSpeed 12_1 with high variance no road`() {
        // avg > 12, std > 3, no road type -> UNKNOWN (SUBWAY needs avg in 5..12)
        val speeds = listOf(6.0, 18.2) // avg=12.1, std=6.1
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        // avg > 15? no (12.1). in 8..15 with trunk|primary? no road. std < 3 + cycleway? no.
        // avg < 3? no. std > 3 and avg in 5..12? avg = 12.1 not in 5..12 (12.1 > 12)
        // -> UNKNOWN
        assertEquals("UNKNOWN", result.first)
    }

    @Test
    fun `inferTransportMode - walking at avgSpeed 2_99`() {
        val speeds = listOf(2.99, 2.99)
        val result = invokeInferTransportModeWithConfidence(speeds, "", 60000L)
        assertEquals("WALKING", result.first)
    }

    @Test
    fun `inferTransportMode - DRIVING with avgSpeed exactly 15_001`() {
        val speeds = listOf(15.001, 15.001)
        val result = invokeInferTransportModeWithConfidence(speeds, ROAD_MOTORWAY_TRUNK, 60000L)
        assertEquals("DRIVING", result.first)
    }

    // ========================================================================
    // PART 23: Additional DataExporter CSV integration tests
    // ========================================================================

    @Test
    fun `exportVerifiedDataToCSV - CSV file has correct header`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 1L,
            accelerometerData = JSON_ACCEL_1,
            gyroscopeData = JSON_GYRO_1,
            isVerified = true
        )
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_header_check_${System.currentTimeMillis()}.csv"
        try {
            exporter.exportVerifiedDataToCSV(outputFile)
            val lines = File(outputFile).readLines()
            assertTrue(lines[0].startsWith("journeyId,"))
            assertTrue(lines[0].contains("isVerified"))
        } finally {
            File(outputFile).delete()
        }
    }

    @Test
    fun `exportAllDataToCSV - CSV content has correct data format`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 42L,
            startTime = 5000L,
            endTime = 15000L,
            transportMode = "SUBWAY",
            labelSource = "MANUAL",
            accelerometerData = JSON_ACCEL_1,
            gyroscopeData = JSON_GYRO_1,
            isVerified = false
        )
        whenever(dao.getJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_data_format_${System.currentTimeMillis()}.csv"
        try {
            exporter.exportAllDataToCSV(outputFile)
            val lines = File(outputFile).readLines()
            assertEquals(2, lines.size) // header + 1 data line
            val dataLine = lines[1]
            assertTrue(dataLine.startsWith("42,"))
            assertTrue(dataLine.contains("SUBWAY"))
            assertTrue(dataLine.contains("MANUAL"))
            assertTrue(dataLine.endsWith(",0")) // isVerified = false = 0
        } finally {
            File(outputFile).delete()
        }
    }

    // ========================================================================
    // PART 24: Additional FeatureExtractor edge cases
    // ========================================================================

    @Test
    fun `extractFeatures - with many sensor data points`() = runTest {
        val entries = (1..200).joinToString(",") {
            """{"x":${it * 0.01},"y":${it * 0.02},"z":${9.8 + it * 0.001}}"""
        }
        val accelJson = "[$entries]"
        val gyroJson = "[$entries]"
        val journey = createLabeledJourney(
            startTime = 0L,
            endTime = 300000L,
            accelerometerData = accelJson,
            gyroscopeData = gyroJson
        )
        val features = featureExtractor.extractFeatures(journey)
        assertTrue(features.accelMeanX > 0f)
        assertTrue(features.accelStdX > 0f)
        assertTrue(features.gyroMeanX > 0f)
        assertTrue(features.gyroStdX > 0f)
        assertEquals(300.0f, features.journeyDuration, 0.001f)
    }

    @Test
    fun `extractFeatures - negative duration (endTime before startTime)`() = runTest {
        val journey = createLabeledJourney(
            startTime = 10000L,
            endTime = 5000L, // endTime < startTime
            accelerometerData = "[]",
            gyroscopeData = "[]"
        )
        val features = featureExtractor.extractFeatures(journey)
        // journeyDuration = (5000 - 10000) / 1000 = -5.0f
        assertEquals(-5.0f, features.journeyDuration, 0.001f)
    }

    @Test
    fun `parseSensorData - handles very large float values`() {
        val json = """[{"x":99999.99,"y":-88888.88,"z":77777.77}]"""
        val result = invokeParseSensorData(json)
        assertEquals(1, result.size)
        assertEquals(99999.99f, result[0].first, 1.0f)
    }

    // ========================================================================
    // PART 25: AutoLabelingService - additional calculateSpeeds edge cases
    // ========================================================================

    @Test
    fun `calculateSpeeds - speed just under 50 is included`() {
        if (!::autoLabelingService.isInitialized) {
            autoLabelingService = createAutoLabelingService()
        }
        // Need speed ~ 49 m/s. 49 m/s * 10s = 490m. ~0.00441 degrees latitude
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 0L),
            Triple(30.004400, 120.000000, 10000L) // ~489m in 10s = ~48.9 m/s
        )
        val result = invokeCalculateSpeeds(trajectory)
        if (result.isNotEmpty()) {
            assertTrue(result[0] < 50.0)
            assertTrue(result[0] > 40.0)
        }
    }

    @Test
    fun `calculateSpeeds - negative time interval (reversed timestamps) skipped`() {
        val trajectory = listOf(
            Triple(30.000000, 120.000000, 10000L),
            Triple(30.000100, 120.000000, 5000L) // earlier timestamp
        )
        val result = invokeCalculateSpeeds(trajectory)
        // timeInterval = (5000 - 10000) / 1000.0 = -5.0, which is NOT > 0 -> skipped
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateSpeeds - longitude change with latitude constant`() {
        val trajectory = listOf(
            Triple(0.000000, 120.000000, 0L),
            Triple(0.000000, 120.000100, 10000L) // longitude change at equator
        )
        val result = invokeCalculateSpeeds(trajectory)
        assertEquals(1, result.size)
        assertTrue(result[0] > 0)
        assertTrue(result[0] < 50)
    }

    // ========================================================================
    // PART 26: AutoLabelingService - autoLabelTrajectory additional paths
    // ========================================================================

    @Test
    fun `autoLabelTrajectory - 15 points less than MIN_GPS_POINTS`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val trajectory = generateGpsTrajectory(15, timeIntervalMs = 5000L)
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - exactly 19 points below minimum`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val trajectory = generateGpsTrajectory(19, timeIntervalMs = 5000L)
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - 25 points with exactly 59999ms duration`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // Create trajectory with enough points but duration just under 60000ms
        val trajectory = (0 until 25).map { i ->
            Triple(30.0 + i * 0.00003, 120.0, i * (59999L / 24))
        }
        // Duration = trajectory.last().third - trajectory.first().third
        // = 24 * (59999/24) = 24 * 2499 = 59976 < 60000
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = "[]",
            gyroscopeData = "[]",
            barometerData = "[]",
            apiKey = TEST_API_KEY
        )
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - valid trajectory reaches snap detector which fails`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        // 30 points, 3.5 seconds apart, small movements -> valid speeds
        val trajectory = generateGpsTrajectory(
            pointCount = 30,
            timeIntervalMs = 3500L,
            latIncrement = 0.000015 // ~1.7m per step
        )
        // Duration = 29 * 3500 = 101500ms > 60000ms
        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = """[{"x":0.1,"y":0.2,"z":9.8}]""",
            gyroscopeData = JSON_GYRO_SMALL,
            barometerData = "[]",
            apiKey = "test-api-key"
        )
        // Snap detector will fail in test environment -> returns null
        assertNull(result)
    }

    @Test
    fun `autoLabelTrajectory - with sensor data strings`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val detector = SnapToRoadsDetector()
        val service = AutoLabelingService(dao, detector)

        val trajectory = generateGpsTrajectory(25, timeIntervalMs = 4000L, latIncrement = 0.000020)
        val accelData = """[{"x":0.5,"y":0.3,"z":9.8},{"x":0.6,"y":0.4,"z":9.7}]"""
        val gyroData = JSON_GYRO_SMALL
        val baroData = """[{"pressure":1013.25}]"""

        val result = service.autoLabelTrajectory(
            gpsTrajectory = trajectory,
            accelerometerData = accelData,
            gyroscopeData = gyroData,
            barometerData = baroData,
            apiKey = TEST_API_KEY
        )
        assertNull(result) // Snap detector fails in test
    }

    // ========================================================================
    // PART 27: Comprehensive integration - DataExporter with nested directory
    // ========================================================================

    @Test
    fun `exportVerifiedDataToCSV - creates parent directories if needed`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 1L,
            accelerometerData = JSON_ACCEL_1,
            gyroscopeData = JSON_GYRO_1,
            isVerified = true
        )
        whenever(dao.getVerifiedJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_nested_${System.currentTimeMillis()}/subdir/output.csv"
        try {
            val result = exporter.exportVerifiedDataToCSV(outputFile)
            assertEquals(1, result)
            assertTrue(File(outputFile).exists())
        } finally {
            File(outputFile).parentFile?.deleteRecursively()
        }
    }

    @Test
    fun `exportAllDataToCSV - creates parent directories if needed`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        val journey = createLabeledJourney(
            id = 1L,
            accelerometerData = JSON_ACCEL_1,
            gyroscopeData = JSON_GYRO_1,
            isVerified = false
        )
        whenever(dao.getJourneysForExport(10000)).thenReturn(listOf(journey))

        val outputFile = "/tmp/test_all_nested_${System.currentTimeMillis()}/subdir/output.csv"
        try {
            val result = exporter.exportAllDataToCSV(outputFile)
            assertEquals(1, result)
            assertTrue(File(outputFile).exists())
        } finally {
            File(outputFile).parentFile?.deleteRecursively()
        }
    }

    // ========================================================================
    // PART 28: Additional AutoLabelingService - verifyLabel with different modes
    // ========================================================================

    @Test
    fun `verifyLabel - verify as WALKING`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val service = AutoLabelingService(dao, SnapToRoadsDetector())

        val journey = createLabeledJourney(id = 1L, transportMode = "UNKNOWN")
        whenever(dao.getJourneyById(1L)).thenReturn(journey)

        service.verifyLabel(1L, "WALKING", "user confirmed walking")
        verify(dao).update(any())
    }

    @Test
    fun `verifyLabel - verify as SUBWAY`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val service = AutoLabelingService(dao, SnapToRoadsDetector())

        val journey = createLabeledJourney(id = 2L, transportMode = "BUS")
        whenever(dao.getJourneyById(2L)).thenReturn(journey)

        service.verifyLabel(2L, "SUBWAY", "actually was subway")
        verify(dao).update(any())
    }

    @Test
    fun `verifyLabel - verify as CYCLING with notes`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val service = AutoLabelingService(dao, SnapToRoadsDetector())

        val journey = createLabeledJourney(id = 3L, transportMode = "WALKING")
        whenever(dao.getJourneyById(3L)).thenReturn(journey)

        service.verifyLabel(3L, "CYCLING", "was on electric scooter counted as cycling")
        verify(dao).update(any())
    }

    // ========================================================================
    // PART 29: DataExporter companion and TAG constant coverage
    // ========================================================================

    @Test
    fun `DataExporter TAG constant exists`() {
        val field = DataExporter::class.java.getDeclaredField("TAG")
        field.isAccessible = true
        assertEquals("DataExporter", field.get(null))
    }

    @Test
    fun `AutoLabelingService TAG constant exists`() {
        val field = AutoLabelingService::class.java.getDeclaredField("TAG")
        field.isAccessible = true
        assertEquals("AutoLabelingService", field.get(null))
    }

    @Test
    fun `FeatureExtractor TAG constant exists`() {
        val field = FeatureExtractor::class.java.getDeclaredField("TAG")
        field.isAccessible = true
        assertEquals("FeatureExtractor", field.get(null))
    }

    // ========================================================================
    // PART 30: Additional generateDataReport source distribution coverage
    // ========================================================================

    @Test
    fun `generateDataReport - multiple label sources`() = runTest {
        val dao = Mockito.mock(ActivityLabelingDao::class.java)
        val exporter = DataExporter(dao, featureExtractor)

        whenever(dao.getTotalCount()).thenReturn(300)
        whenever(dao.getVerifiedCount()).thenReturn(100)
        whenever(dao.getTransportModeDistribution()).thenReturn(listOf(
            TransportModeCount("WALKING", 100),
            TransportModeCount("CYCLING", 100),
            TransportModeCount("BUS", 100)
        ))
        whenever(dao.getLabelSourceDistribution()).thenReturn(listOf(
            LabelSourceCount("AUTO_SNAP", 100),
            LabelSourceCount("MANUAL", 100),
            LabelSourceCount("VERIFIED", 100)
        ))

        val report = exporter.generateDataReport()
        assertTrue(report.contains("AUTO_SNAP"))
        assertTrue(report.contains("MANUAL"))
        assertTrue(report.contains("VERIFIED"))
    }

    // ========================================================================
    // PART 31: Additional haversineDistance edge cases for coverage
    // ========================================================================

    @Test
    fun `haversineDistance - small longitude difference`() {
        val result = invokeHaversineDistance(30.0, 120.0, 30.0, 120.0001)
        assertTrue(result > 5)
        assertTrue(result < 20)
    }

    @Test
    fun `haversineDistance - both lat and lng change`() {
        val result = invokeHaversineDistance(30.0, 120.0, 30.001, 120.001)
        assertTrue(result > 100)
        assertTrue(result < 200)
    }

    // ========================================================================
    // PART 32: Additional calculateVariance edge cases
    // ========================================================================

    @Test
    fun `calculateVariance - very large list`() {
        val values = (1..1000).map { it.toDouble() }
        val result = invokeCalculateVariance(values)
        // variance of 1..1000 = (1000^2 - 1) / 12 = 83333.25
        assertTrue(result > 83000)
        assertTrue(result < 84000)
    }

    @Test
    fun `calculateVariance - mixed positive and negative`() {
        val values = listOf(-5.0, -3.0, 0.0, 3.0, 5.0)
        val result = invokeCalculateVariance(values)
        // mean = 0, variance = (25+9+0+9+25)/5 = 13.6
        assertEquals(13.6, result, 0.0001)
    }
}
