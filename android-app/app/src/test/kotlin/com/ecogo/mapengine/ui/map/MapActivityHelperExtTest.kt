package com.ecogo.mapengine.ui.map

import com.ecogo.data.Advertisement
import com.ecogo.mapengine.data.model.GeoPoint
import com.ecogo.mapengine.data.model.RouteStep
import com.ecogo.mapengine.data.model.TransitDetails
import com.ecogo.mapengine.data.model.TransportMode
import com.ecogo.mapengine.ml.TransportModeLabel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MapActivityHelperExtTest {

    companion object {
        private const val INSTRUCTION_TAKE_BUS = "Take bus"
        private const val STOP_A_NAME = "Stop A"
        private const val STOP_B_NAME = "Stop B"
    }

    // ==================== isRunningOnEmulator ====================

    @Test
    fun `isRunningOnEmulator returns boolean without crash`() {
        // On Robolectric the Build fields are set to "robolectric" values,
        // so this just verifies the method runs without exception
        val result = MapActivityHelper.isRunningOnEmulator()
        // Robolectric sets FINGERPRINT to "robolectric", which doesn't start with "generic" or "unknown"
        // but does set MODEL etc. - just verify it's a Boolean
        assertNotNull(result)
    }

    @Test
    fun `isRunningOnEmulator returns consistent result`() {
        // Calling twice should return the same result
        val first = MapActivityHelper.isRunningOnEmulator()
        val second = MapActivityHelper.isRunningOnEmulator()
        assertEquals(first, second)
    }

    // ==================== getTransitStepColorName ====================

    @Test
    fun `getTransitStepColorName WALKING returns walking`() {
        assertEquals("walking", MapActivityHelper.getTransitStepColorName("WALKING", null))
    }

    @Test
    fun `getTransitStepColorName WALKING ignores vehicleType`() {
        assertEquals("walking", MapActivityHelper.getTransitStepColorName("WALKING", "BUS"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT SUBWAY returns subway`() {
        assertEquals("subway", MapActivityHelper.getTransitStepColorName("TRANSIT", "SUBWAY"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT subway lowercase returns subway`() {
        assertEquals("subway", MapActivityHelper.getTransitStepColorName("TRANSIT", "subway"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT METRO_RAIL returns subway`() {
        assertEquals("subway", MapActivityHelper.getTransitStepColorName("TRANSIT", "METRO_RAIL"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT BUS returns bus`() {
        assertEquals("bus", MapActivityHelper.getTransitStepColorName("TRANSIT", "BUS"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT INTERCITY_BUS returns bus`() {
        assertEquals("bus", MapActivityHelper.getTransitStepColorName("TRANSIT", "INTERCITY_BUS"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT TROLLEYBUS returns bus`() {
        assertEquals("bus", MapActivityHelper.getTransitStepColorName("TRANSIT", "TROLLEYBUS"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT RAIL returns rail`() {
        assertEquals("rail", MapActivityHelper.getTransitStepColorName("TRANSIT", "RAIL"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT HEAVY_RAIL returns rail`() {
        assertEquals("rail", MapActivityHelper.getTransitStepColorName("TRANSIT", "HEAVY_RAIL"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT COMMUTER_TRAIN returns rail`() {
        assertEquals("rail", MapActivityHelper.getTransitStepColorName("TRANSIT", "COMMUTER_TRAIN"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT HIGH_SPEED_TRAIN returns rail`() {
        assertEquals("rail", MapActivityHelper.getTransitStepColorName("TRANSIT", "HIGH_SPEED_TRAIN"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT LONG_DISTANCE_TRAIN returns rail`() {
        assertEquals("rail", MapActivityHelper.getTransitStepColorName("TRANSIT", "LONG_DISTANCE_TRAIN"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT TRAM returns tram`() {
        assertEquals("tram", MapActivityHelper.getTransitStepColorName("TRANSIT", "TRAM"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT MONORAIL returns tram`() {
        assertEquals("tram", MapActivityHelper.getTransitStepColorName("TRANSIT", "MONORAIL"))
    }

    @Test
    fun `getTransitStepColorName TRANSIT null vehicleType returns bus`() {
        assertEquals("bus", MapActivityHelper.getTransitStepColorName("TRANSIT", null))
    }

    @Test
    fun `getTransitStepColorName TRANSIT unknown vehicleType returns bus`() {
        assertEquals("bus", MapActivityHelper.getTransitStepColorName("TRANSIT", "FERRY"))
    }

    @Test
    fun `getTransitStepColorName DRIVING returns driving`() {
        assertEquals("driving", MapActivityHelper.getTransitStepColorName("DRIVING", null))
    }

    @Test
    fun `getTransitStepColorName BICYCLING returns cycling`() {
        assertEquals("cycling", MapActivityHelper.getTransitStepColorName("BICYCLING", null))
    }

    @Test
    fun `getTransitStepColorName unknown mode returns remaining`() {
        assertEquals("remaining", MapActivityHelper.getTransitStepColorName("FLYING", null))
    }

    @Test
    fun `getTransitStepColorName empty string returns remaining`() {
        assertEquals("remaining", MapActivityHelper.getTransitStepColorName("", null))
    }

    // ==================== analyzeRouteSteps ====================

    @Test
    fun `analyzeRouteSteps null returns both false`() {
        val result = MapActivityHelper.analyzeRouteSteps(null)
        assertFalse(result.hasTransitSteps)
        assertFalse(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps empty list returns both false`() {
        val result = MapActivityHelper.analyzeRouteSteps(emptyList())
        assertFalse(result.hasTransitSteps)
        assertFalse(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps with transit step and polyline`() {
        val steps = listOf(
            RouteStep(
                instruction = INSTRUCTION_TAKE_BUS,
                distance = 1000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, STOP_A_NAME, STOP_B_NAME, 5, "BUS"),
                polyline_points = listOf(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0))
            )
        )
        val result = MapActivityHelper.analyzeRouteSteps(steps)
        assertTrue(result.hasTransitSteps)
        assertTrue(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps with transit step no polyline`() {
        val steps = listOf(
            RouteStep(
                instruction = INSTRUCTION_TAKE_BUS,
                distance = 1000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, STOP_A_NAME, STOP_B_NAME, 5, "BUS"),
                polyline_points = null
            )
        )
        val result = MapActivityHelper.analyzeRouteSteps(steps)
        assertTrue(result.hasTransitSteps)
        assertFalse(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps with transit step empty polyline`() {
        val steps = listOf(
            RouteStep(
                instruction = INSTRUCTION_TAKE_BUS,
                distance = 1000.0,
                duration = 600,
                travel_mode = "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, STOP_A_NAME, STOP_B_NAME, 5, "BUS"),
                polyline_points = emptyList()
            )
        )
        val result = MapActivityHelper.analyzeRouteSteps(steps)
        assertTrue(result.hasTransitSteps)
        assertFalse(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps walking only`() {
        val steps = listOf(
            RouteStep(
                instruction = "Walk",
                distance = 500.0,
                duration = 300,
                travel_mode = "WALKING",
                polyline_points = listOf(GeoPoint(0.0, 0.0), GeoPoint(0.5, 0.5))
            )
        )
        val result = MapActivityHelper.analyzeRouteSteps(steps)
        assertFalse(result.hasTransitSteps)
        assertTrue(result.hasStepPolylines)
    }

    @Test
    fun `analyzeRouteSteps mixed transit and walking with polylines`() {
        val steps = listOf(
            RouteStep("Walk", 200.0, 120, "WALKING", polyline_points = listOf(GeoPoint(0.0, 0.0))),
            RouteStep("Bus", 1000.0, 600, "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, "A", "B", 3, "BUS"),
                polyline_points = listOf(GeoPoint(1.0, 1.0), GeoPoint(2.0, 2.0)))
        )
        val result = MapActivityHelper.analyzeRouteSteps(steps)
        assertTrue(result.hasTransitSteps)
        assertTrue(result.hasStepPolylines)
    }

    // ==================== getTrackingUIState ====================

    @Test
    fun `getTrackingUIState Idle`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Idle)
        assertEquals("start_tracking", state.buttonText)
        assertTrue(state.buttonEnabled)
        assertTrue(state.chipGroupVisible)
        assertTrue(state.searchVisible)
        assertFalse(state.routeInfoVisible)
        assertTrue(state.hideTimer)
        assertTrue(state.isIdle)
    }

    @Test
    fun `getTrackingUIState Starting`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Starting)
        assertEquals("正在开始...", state.buttonText)
        assertFalse(state.buttonEnabled)
        assertFalse(state.chipGroupVisible)
        assertFalse(state.searchVisible)
        assertFalse(state.routeInfoVisible)
        assertFalse(state.hideTimer)
        assertFalse(state.isIdle)
    }

    @Test
    fun `getTrackingUIState Tracking`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Tracking("trip-123"))
        assertEquals("stop_tracking", state.buttonText)
        assertTrue(state.buttonEnabled)
        assertFalse(state.chipGroupVisible)
        assertFalse(state.searchVisible)
        assertTrue(state.routeInfoVisible)
        assertFalse(state.hideTimer)
        assertFalse(state.isIdle)
    }

    @Test
    fun `getTrackingUIState Tracking with different tripId`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Tracking("different-id"))
        assertEquals("stop_tracking", state.buttonText)
        assertTrue(state.buttonEnabled)
    }

    @Test
    fun `getTrackingUIState Stopping`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Stopping)
        assertEquals("正在结束...", state.buttonText)
        assertFalse(state.buttonEnabled)
        assertFalse(state.chipGroupVisible)
        assertFalse(state.searchVisible)
        assertFalse(state.routeInfoVisible)
        assertFalse(state.hideTimer)
        assertFalse(state.isIdle)
    }

    @Test
    fun `getTrackingUIState Completed`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Completed)
        assertEquals("start_tracking", state.buttonText)
        assertTrue(state.buttonEnabled)
        assertTrue(state.chipGroupVisible)
        assertTrue(state.searchVisible)
        assertFalse(state.routeInfoVisible)
        assertTrue(state.hideTimer)
        assertFalse(state.isIdle)
    }

    @Test
    fun `getTrackingUIState with navigation mode parameter`() {
        val state = MapActivityHelper.getTrackingUIState(TripState.Tracking("t1"), isNavigationMode = true, remainingKm = 5.0f)
        assertEquals("stop_tracking", state.buttonText)
        assertTrue(state.routeInfoVisible)
    }

    // ==================== prepareTripCompletionData ====================

    @Test
    fun `prepareTripCompletionData with segments detects dominant mode`() {
        val segments = listOf(
            MapActivityHelper.ModeSegment(TransportModeLabel.BUS, 0L, 5000L),
            MapActivityHelper.ModeSegment(TransportModeLabel.WALKING, 5000L, 6000L)
        )
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = segments,
            lastMlConfidence = 0.85f,
            userSelectedModeValue = "bus",
            distanceMeters = 2000.0,
            selectedTransportMode = TransportMode.BUS
        )
        assertEquals("bus", result.detectedMode)
        assertEquals("bus", result.userSelectedMode)
        assertTrue(result.isGreenTrip)
        assertTrue(result.carbonSavedGrams > 0)
        assertNotNull(result.mlConfidence)
        assertEquals(0.85, result.mlConfidence!!, 0.01)
    }

    @Test
    fun `prepareTripCompletionData empty segments returns null detectedMode`() {
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = emptyList(),
            lastMlConfidence = 0.9f,
            userSelectedModeValue = "walk",
            distanceMeters = 1000.0,
            selectedTransportMode = TransportMode.WALKING
        )
        assertNull(result.detectedMode)
        assertEquals("walk", result.userSelectedMode)
        assertTrue(result.isGreenTrip)
        assertNull(result.mlConfidence)
    }

    @Test
    fun `prepareTripCompletionData null userSelectedMode defaults to walk`() {
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = emptyList(),
            lastMlConfidence = 0f,
            userSelectedModeValue = null,
            distanceMeters = 500.0,
            selectedTransportMode = null
        )
        assertEquals("walk", result.userSelectedMode)
        assertTrue(result.isGreenTrip)
    }

    @Test
    fun `prepareTripCompletionData car mode is not green`() {
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = emptyList(),
            lastMlConfidence = 0f,
            userSelectedModeValue = "car",
            distanceMeters = 5000.0,
            selectedTransportMode = TransportMode.DRIVING
        )
        assertEquals("car", result.userSelectedMode)
        assertFalse(result.isGreenTrip)
        assertEquals(0L, result.carbonSavedGrams)
    }

    @Test
    fun `prepareTripCompletionData zero confidence returns null mlConfidence`() {
        val segments = listOf(
            MapActivityHelper.ModeSegment(TransportModeLabel.WALKING, 0L, 1000L)
        )
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = segments,
            lastMlConfidence = 0f,
            userSelectedModeValue = "walk",
            distanceMeters = 1000.0,
            selectedTransportMode = TransportMode.WALKING
        )
        assertEquals("walk", result.detectedMode)
        assertNull(result.mlConfidence)
    }

    @Test
    fun `prepareTripCompletionData with walking mode calculates carbon correctly`() {
        val result = MapActivityHelper.prepareTripCompletionData(
            modeSegments = emptyList(),
            lastMlConfidence = 0f,
            userSelectedModeValue = "walk",
            distanceMeters = 1000.0,
            selectedTransportMode = TransportMode.WALKING
        )
        // 1km walking: saved = (0.15 - 0) * 1 * 1000 = 150g
        assertEquals(150L, result.carbonSavedGrams)
    }

    // ==================== buildRouteInfoTexts ====================

    @Test
    fun `buildRouteInfoTexts low carbon route`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = "low_carbon",
            carbonSaved = 1.5,
            totalCarbon = 0.3,
            totalDistance = 10.0,
            estimatedDuration = 30,
            duration = null,
            hasAlternatives = false,
            hasTransitSteps = false
        )
        assertEquals("低碳路线", result.routeTypeText)
        assertTrue(result.carbonSavedText.contains("比驾车减少"))
        assertTrue(result.headerText.contains("低碳路线"))
        assertTrue(result.headerText.contains("环保指数"))
        assertEquals("预计: 30 分钟", result.durationText)
        assertTrue(result.showCumulativeImpact)
        assertFalse(result.showRouteOptions)
        assertFalse(result.showRouteSteps)
    }

    @Test
    fun `buildRouteInfoTexts balanced route`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = "balanced",
            carbonSaved = 0.5,
            totalCarbon = 1.0,
            totalDistance = 5.0,
            estimatedDuration = 15,
            duration = 20,
            hasAlternatives = true,
            hasTransitSteps = true
        )
        assertEquals("平衡路线", result.routeTypeText)
        assertTrue(result.showRouteOptions)
        assertTrue(result.showRouteSteps)
    }

    @Test
    fun `buildRouteInfoTexts null route type`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = null,
            carbonSaved = 0.0,
            totalCarbon = 2.0,
            totalDistance = 20.0,
            estimatedDuration = 0,
            duration = 45,
            hasAlternatives = false,
            hasTransitSteps = false
        )
        assertEquals("推荐路线", result.routeTypeText)
        assertFalse(result.showCumulativeImpact)
        assertEquals("预计: 45 分钟", result.durationText) // Falls back to duration
    }

    @Test
    fun `buildRouteInfoTexts zero duration uses fallback`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = "low_carbon",
            carbonSaved = 0.0,
            totalCarbon = 0.0,
            totalDistance = 0.0,
            estimatedDuration = 0,
            duration = 10,
            hasAlternatives = false,
            hasTransitSteps = false
        )
        assertEquals("预计: 10 分钟", result.durationText)
    }

    @Test
    fun `buildRouteInfoTexts zero duration null fallback`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = "low_carbon",
            carbonSaved = 0.0,
            totalCarbon = 0.0,
            totalDistance = 0.0,
            estimatedDuration = 0,
            duration = null,
            hasAlternatives = false,
            hasTransitSteps = false
        )
        assertEquals("预计: 0 分钟", result.durationText)
    }

    @Test
    fun `buildRouteInfoTexts carbon color green for zero`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = null, carbonSaved = 0.0, totalCarbon = 0.0,
            totalDistance = 1.0, estimatedDuration = 10, duration = null,
            hasAlternatives = false, hasTransitSteps = false
        )
        assertEquals("#4CAF50", result.carbonColorHex)
    }

    @Test
    fun `buildRouteInfoTexts carbon color red for high`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = null, carbonSaved = 0.0, totalCarbon = 5.0,
            totalDistance = 1.0, estimatedDuration = 10, duration = null,
            hasAlternatives = false, hasTransitSteps = false
        )
        assertEquals("#FF5722", result.carbonColorHex)
    }

    @Test
    fun `buildRouteInfoTexts eco rating five stars for zero carbon`() {
        val result = MapActivityHelper.buildRouteInfoTexts(
            routeType = null, carbonSaved = 1.0, totalCarbon = 0.0,
            totalDistance = 5.0, estimatedDuration = 20, duration = null,
            hasAlternatives = false, hasTransitSteps = false
        )
        assertTrue(result.ecoRating.contains("⭐⭐⭐⭐⭐"))
    }

    // ==================== determineTrackingMode ====================

    @Test
    fun `determineTrackingMode no route points no steps`() {
        val result = MapActivityHelper.determineTrackingMode(null, null)
        assertFalse(result.isNavigationMode)
        assertFalse(result.hasTransitWithPolylines)
        assertFalse(result.hasTransitFallback)
    }

    @Test
    fun `determineTrackingMode empty route points`() {
        val result = MapActivityHelper.determineTrackingMode(emptyList<Any>(), null)
        assertFalse(result.isNavigationMode)
        assertFalse(result.hasTransitWithPolylines)
        assertFalse(result.hasTransitFallback)
    }

    @Test
    fun `determineTrackingMode has route points no transit`() {
        val steps = listOf(
            RouteStep("Walk", 500.0, 300, "WALKING")
        )
        val result = MapActivityHelper.determineTrackingMode(listOf("point1"), steps)
        assertTrue(result.isNavigationMode)
        assertFalse(result.hasTransitWithPolylines)
        assertFalse(result.hasTransitFallback)
    }

    @Test
    fun `determineTrackingMode transit with polylines`() {
        val steps = listOf(
            RouteStep("Bus", 1000.0, 600, "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, "A", "B", 3, "BUS"),
                polyline_points = listOf(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0)))
        )
        val result = MapActivityHelper.determineTrackingMode(listOf("point1"), steps)
        assertTrue(result.isNavigationMode)
        assertTrue(result.hasTransitWithPolylines)
        assertFalse(result.hasTransitFallback)
    }

    @Test
    fun `determineTrackingMode transit fallback no polylines with route points`() {
        val steps = listOf(
            RouteStep("Bus", 1000.0, 600, "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, "A", "B", 3, "BUS"),
                polyline_points = null)
        )
        val result = MapActivityHelper.determineTrackingMode(listOf("point1"), steps)
        assertTrue(result.isNavigationMode)
        assertFalse(result.hasTransitWithPolylines)
        assertTrue(result.hasTransitFallback)
    }

    @Test
    fun `determineTrackingMode transit no polylines no route points`() {
        val steps = listOf(
            RouteStep("Bus", 1000.0, 600, "TRANSIT",
                transit_details = TransitDetails("Bus 1", null, "A", "B", 3, "BUS"),
                polyline_points = null)
        )
        val result = MapActivityHelper.determineTrackingMode(null, steps)
        assertFalse(result.isNavigationMode)
        assertFalse(result.hasTransitWithPolylines)
        assertFalse(result.hasTransitFallback)
    }

    // ==================== processTransportModeDetection ====================

    @Test
    fun `processTransportModeDetection WALKING`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.WALKING, 0.95f)
        assertEquals("walk", result.detectedTransportMode)
        assertEquals("🚶", result.modeIcon)
        assertEquals("步行", result.modeText)
        assertEquals(95, result.confidencePercent)
        assertTrue(result.navigationModeText.contains("当前交通"))
        assertTrue(result.navigationModeText.contains("步行"))
        assertTrue(result.navigationModeText.contains("95%"))
        assertTrue(result.trackingModeText.contains("检测到"))
    }

    @Test
    fun `processTransportModeDetection CYCLING`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.CYCLING, 0.8f)
        assertEquals("bike", result.detectedTransportMode)
        assertEquals("🚴", result.modeIcon)
        assertEquals("骑行", result.modeText)
        assertEquals(80, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection BUS`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.BUS, 0.75f)
        assertEquals("bus", result.detectedTransportMode)
        assertEquals("🚌", result.modeIcon)
        assertEquals("公交", result.modeText)
        assertEquals(75, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection SUBWAY`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.SUBWAY, 0.6f)
        assertEquals("subway", result.detectedTransportMode)
        assertEquals("🚇", result.modeIcon)
        assertEquals("地铁", result.modeText)
        assertEquals(60, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection DRIVING`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.DRIVING, 0.5f)
        assertEquals("car", result.detectedTransportMode)
        assertEquals("🚗", result.modeIcon)
        assertEquals("驾车", result.modeText)
        assertEquals(50, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection UNKNOWN`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.UNKNOWN, 0.3f)
        assertEquals("walk", result.detectedTransportMode)
        assertEquals("❓", result.modeIcon)
        assertEquals("未知", result.modeText)
        assertEquals(30, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection zero confidence`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.WALKING, 0f)
        assertEquals(0, result.confidencePercent)
    }

    @Test
    fun `processTransportModeDetection full confidence`() {
        val result = MapActivityHelper.processTransportModeDetection(TransportModeLabel.BUS, 1.0f)
        assertEquals(100, result.confidencePercent)
    }

    // ==================== shouldShowStartButton ====================

    @Test
    fun `shouldShowStartButton both true returns true`() {
        assertTrue(MapActivityHelper.shouldShowStartButton(true, true))
    }

    @Test
    fun `shouldShowStartButton no origin returns false`() {
        assertFalse(MapActivityHelper.shouldShowStartButton(false, true))
    }

    @Test
    fun `shouldShowStartButton no destination returns false`() {
        assertFalse(MapActivityHelper.shouldShowStartButton(true, false))
    }

    @Test
    fun `shouldShowStartButton both false returns false`() {
        assertFalse(MapActivityHelper.shouldShowStartButton(false, false))
    }

    // ==================== MILESTONES constant ====================

    @Test
    fun `MILESTONES has expected values`() {
        assertEquals(5, MapActivityHelper.MILESTONES.size)
        assertEquals(1000f, MapActivityHelper.MILESTONES[0])
        assertEquals(2000f, MapActivityHelper.MILESTONES[1])
        assertEquals(3000f, MapActivityHelper.MILESTONES[2])
        assertEquals(5000f, MapActivityHelper.MILESTONES[3])
        assertEquals(10000f, MapActivityHelper.MILESTONES[4])
    }

    @Test
    fun `MILESTONES is sorted ascending`() {
        for (i in 0 until MapActivityHelper.MILESTONES.size - 1) {
            assertTrue(MapActivityHelper.MILESTONES[i] < MapActivityHelper.MILESTONES[i + 1])
        }
    }

    // ==================== filterActiveAds ====================

    private fun makeAd(
        id: String = "1",
        position: String = "banner",
        status: String = "Active"
    ): Advertisement {
        return Advertisement(
            id = id,
            name = "Ad $id",
            description = "Description $id",
            status = status,
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            imageUrl = "https://example.com/img$id.jpg",
            linkUrl = "https://example.com/$id",
            position = position,
            impressions = 100,
            clicks = 10,
            clickRate = 0.1
        )
    }

    @Test
    fun `filterActiveAds null returns empty`() {
        assertEquals(emptyList<Advertisement>(), MapActivityHelper.filterActiveAds(null))
    }

    @Test
    fun `filterActiveAds empty list returns empty`() {
        assertEquals(emptyList<Advertisement>(), MapActivityHelper.filterActiveAds(emptyList()))
    }

    @Test
    fun `filterActiveAds keeps banner Active ads`() {
        val ads = listOf(
            makeAd("1", "banner", "Active"),
            makeAd("2", "banner", "Active")
        )
        val result = MapActivityHelper.filterActiveAds(ads)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterActiveAds filters out non-banner`() {
        val ads = listOf(
            makeAd("1", "banner", "Active"),
            makeAd("2", "sidebar", "Active")
        )
        val result = MapActivityHelper.filterActiveAds(ads)
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun `filterActiveAds filters out inactive`() {
        val ads = listOf(
            makeAd("1", "banner", "Active"),
            makeAd("2", "banner", "Inactive"),
            makeAd("3", "banner", "Paused")
        )
        val result = MapActivityHelper.filterActiveAds(ads)
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }

    @Test
    fun `filterActiveAds filters both position and status`() {
        val ads = listOf(
            makeAd("1", "banner", "Active"),
            makeAd("2", "sidebar", "Active"),
            makeAd("3", "banner", "Inactive"),
            makeAd("4", "sidebar", "Inactive")
        )
        val result = MapActivityHelper.filterActiveAds(ads)
        assertEquals(1, result.size)
    }

    // ==================== isVipFromProfile ====================

    @Test
    fun `isVipFromProfile all null returns false`() {
        assertFalse(MapActivityHelper.isVipFromProfile(null, null, null, null, null))
    }

    @Test
    fun `isVipFromProfile all false returns false`() {
        assertFalse(MapActivityHelper.isVipFromProfile(false, false, null, null, false))
    }

    @Test
    fun `isVipFromProfile vipInfoActive true`() {
        assertTrue(MapActivityHelper.isVipFromProfile(true, null, null, null, null))
    }

    @Test
    fun `isVipFromProfile userVipActive true`() {
        assertTrue(MapActivityHelper.isVipFromProfile(null, true, null, null, null))
    }

    @Test
    fun `isVipFromProfile vipInfoPlan not null`() {
        assertTrue(MapActivityHelper.isVipFromProfile(null, null, "premium", null, null))
    }

    @Test
    fun `isVipFromProfile userVipPlan not null`() {
        assertTrue(MapActivityHelper.isVipFromProfile(null, null, null, "basic", null))
    }

    @Test
    fun `isVipFromProfile isAdmin true`() {
        assertTrue(MapActivityHelper.isVipFromProfile(null, null, null, null, true))
    }

    @Test
    fun `isVipFromProfile multiple true conditions`() {
        assertTrue(MapActivityHelper.isVipFromProfile(true, true, "pro", "pro", true))
    }

    @Test
    fun `isVipFromProfile isAdmin false with null others`() {
        assertFalse(MapActivityHelper.isVipFromProfile(null, null, null, null, false))
    }

    // ==================== generateTripCompletionMessage ====================

    @Test
    fun `generateTripCompletionMessage green trip`() {
        val msg = MapActivityHelper.generateTripCompletionMessage(true, 1.25, 50)
        assertTrue(msg.contains("绿色出行完成"))
        assertTrue(msg.contains("1.25"))
        assertTrue(msg.contains("50"))
        assertTrue(msg.contains("积分"))
    }

    @Test
    fun `generateTripCompletionMessage non-green trip`() {
        val msg = MapActivityHelper.generateTripCompletionMessage(false, 2.50, 0)
        assertTrue(msg.contains("行程完成"))
        assertTrue(msg.contains("2.50"))
        assertFalse(msg.contains("绿色出行"))
    }

    @Test
    fun `generateTripCompletionMessage zero carbon`() {
        val msg = MapActivityHelper.generateTripCompletionMessage(true, 0.0, 0)
        assertTrue(msg.contains("0.00"))
    }

    @Test
    fun `generateTripCompletionMessage large values`() {
        val msg = MapActivityHelper.generateTripCompletionMessage(true, 123.456, 999)
        assertTrue(msg.contains("123.46"))
        assertTrue(msg.contains("999"))
    }

    // ==================== isValidTripId ====================

    @Test
    fun `isValidTripId valid id returns true`() {
        assertTrue(MapActivityHelper.isValidTripId("trip-123"))
    }

    @Test
    fun `isValidTripId null returns false`() {
        assertFalse(MapActivityHelper.isValidTripId(null))
    }

    @Test
    fun `isValidTripId MOCK_ prefix returns false`() {
        assertFalse(MapActivityHelper.isValidTripId("MOCK_trip-123"))
    }

    @Test
    fun `isValidTripId restored-trip returns false`() {
        assertFalse(MapActivityHelper.isValidTripId("restored-trip"))
    }

    @Test
    fun `isValidTripId empty string returns true`() {
        // Empty string is not null, doesn't start with MOCK_, not "restored-trip"
        assertTrue(MapActivityHelper.isValidTripId(""))
    }

    @Test
    fun `isValidTripId MOCK without underscore returns true`() {
        assertTrue(MapActivityHelper.isValidTripId("MOCK123"))
    }

    @Test
    fun `isValidTripId uuid format returns true`() {
        assertTrue(MapActivityHelper.isValidTripId("550e8400-e29b-41d4-a716-446655440000"))
    }

    // ==================== getRouteWidth ====================

    @Test
    fun `getRouteWidth WALKING returns 8f`() {
        assertEquals(8f, MapActivityHelper.getRouteWidth(TransportMode.WALKING))
    }

    @Test
    fun `getRouteWidth CYCLING returns 12f`() {
        assertEquals(12f, MapActivityHelper.getRouteWidth(TransportMode.CYCLING))
    }

    @Test
    fun `getRouteWidth DRIVING returns 12f`() {
        assertEquals(12f, MapActivityHelper.getRouteWidth(TransportMode.DRIVING))
    }

    @Test
    fun `getRouteWidth BUS returns 12f`() {
        assertEquals(12f, MapActivityHelper.getRouteWidth(TransportMode.BUS))
    }

    @Test
    fun `getRouteWidth SUBWAY returns 12f`() {
        assertEquals(12f, MapActivityHelper.getRouteWidth(TransportMode.SUBWAY))
    }

    @Test
    fun `getRouteWidth null returns 12f`() {
        assertEquals(12f, MapActivityHelper.getRouteWidth(null))
    }

    // ==================== shouldUseDashedLine ====================

    @Test
    fun `shouldUseDashedLine WALKING returns true`() {
        assertTrue(MapActivityHelper.shouldUseDashedLine(TransportMode.WALKING))
    }

    @Test
    fun `shouldUseDashedLine CYCLING returns false`() {
        assertFalse(MapActivityHelper.shouldUseDashedLine(TransportMode.CYCLING))
    }

    @Test
    fun `shouldUseDashedLine DRIVING returns false`() {
        assertFalse(MapActivityHelper.shouldUseDashedLine(TransportMode.DRIVING))
    }

    @Test
    fun `shouldUseDashedLine BUS returns false`() {
        assertFalse(MapActivityHelper.shouldUseDashedLine(TransportMode.BUS))
    }

    @Test
    fun `shouldUseDashedLine null returns false`() {
        assertFalse(MapActivityHelper.shouldUseDashedLine(null))
    }

    // ==================== calculateFallbackPointsPerStep ====================

    @Test
    fun `calculateFallbackPointsPerStep last step takes remaining`() {
        val result = MapActivityHelper.calculateFallbackPointsPerStep(
            totalPoints = 100,
            stepDistance = 500.0,
            totalStepDistance = 2000.0,
            isLastStep = true,
            currentPointIndex = 75
        )
        assertEquals(25, result) // 100 - 75
    }

    @Test
    fun `calculateFallbackPointsPerStep proportional allocation`() {
        val result = MapActivityHelper.calculateFallbackPointsPerStep(
            totalPoints = 100,
            stepDistance = 500.0,
            totalStepDistance = 1000.0,
            isLastStep = false,
            currentPointIndex = 0
        )
        assertEquals(50, result) // 100 * (500/1000) = 50
    }

    @Test
    fun `calculateFallbackPointsPerStep minimum 2 for non-last step`() {
        val result = MapActivityHelper.calculateFallbackPointsPerStep(
            totalPoints = 100,
            stepDistance = 1.0,
            totalStepDistance = 100000.0,
            isLastStep = false,
            currentPointIndex = 0
        )
        assertEquals(2, result) // ratio too small, coerced to 2
    }

    @Test
    fun `calculateFallbackPointsPerStep last step at beginning`() {
        val result = MapActivityHelper.calculateFallbackPointsPerStep(
            totalPoints = 50,
            stepDistance = 100.0,
            totalStepDistance = 100.0,
            isLastStep = true,
            currentPointIndex = 0
        )
        assertEquals(50, result) // 50 - 0
    }

    @Test
    fun `calculateFallbackPointsPerStep equal distribution`() {
        val result = MapActivityHelper.calculateFallbackPointsPerStep(
            totalPoints = 100,
            stepDistance = 250.0,
            totalStepDistance = 1000.0,
            isLastStep = false,
            currentPointIndex = 0
        )
        assertEquals(25, result) // 100 * 0.25
    }

    // ==================== formatNavigationInfoText ====================

    @Test
    fun `formatNavigationInfoText returns navigation pair`() {
        val result = MapActivityHelper.formatNavigationInfoText(5000f, 3000f)
        assertEquals("navigation", result.first)
        assertTrue(result.second.contains("剩余"))
        assertTrue(result.second.contains("3.00"))
    }

    @Test
    fun `formatNavigationInfoText zero remaining`() {
        val result = MapActivityHelper.formatNavigationInfoText(5000f, 0f)
        assertEquals("navigation", result.first)
        assertTrue(result.second.contains("0.00"))
    }

    @Test
    fun `formatNavigationInfoText large remaining`() {
        val result = MapActivityHelper.formatNavigationInfoText(0f, 50000f)
        assertEquals("navigation", result.first)
        assertTrue(result.second.contains("50.00"))
    }

    // ==================== formatTrackingInfoText ====================

    @Test
    fun `formatTrackingInfoText returns tracking pair`() {
        val result = MapActivityHelper.formatTrackingInfoText(1000f)
        assertEquals("tracking", result.first)
        assertEquals("实时记录GPS轨迹", result.second)
    }

    @Test
    fun `formatTrackingInfoText zero distance`() {
        val result = MapActivityHelper.formatTrackingInfoText(0f)
        assertEquals("tracking", result.first)
        assertEquals("实时记录GPS轨迹", result.second)
    }

    // ==================== RouteAnalysis data class ====================

    @Test
    fun `RouteAnalysis construction`() {
        val ra = MapActivityHelper.RouteAnalysis(true, false)
        assertTrue(ra.hasTransitSteps)
        assertFalse(ra.hasStepPolylines)
    }

    @Test
    fun `RouteAnalysis equality`() {
        val ra1 = MapActivityHelper.RouteAnalysis(true, true)
        val ra2 = MapActivityHelper.RouteAnalysis(true, true)
        assertEquals(ra1, ra2)
    }

    @Test
    fun `RouteAnalysis inequality`() {
        val ra1 = MapActivityHelper.RouteAnalysis(true, true)
        val ra2 = MapActivityHelper.RouteAnalysis(true, false)
        assertNotEquals(ra1, ra2)
    }

    @Test
    fun `RouteAnalysis copy`() {
        val ra1 = MapActivityHelper.RouteAnalysis(true, false)
        val ra2 = ra1.copy(hasStepPolylines = true)
        assertTrue(ra2.hasTransitSteps)
        assertTrue(ra2.hasStepPolylines)
    }

    @Test
    fun `RouteAnalysis hashCode consistent with equality`() {
        val ra1 = MapActivityHelper.RouteAnalysis(true, true)
        val ra2 = MapActivityHelper.RouteAnalysis(true, true)
        assertEquals(ra1.hashCode(), ra2.hashCode())
    }

    @Test
    fun `RouteAnalysis toString contains fields`() {
        val ra = MapActivityHelper.RouteAnalysis(true, false)
        val str = ra.toString()
        assertTrue(str.contains("hasTransitSteps=true"))
        assertTrue(str.contains("hasStepPolylines=false"))
    }

    @Test
    fun `RouteAnalysis destructuring`() {
        val ra = MapActivityHelper.RouteAnalysis(false, true)
        val (transit, polylines) = ra
        assertFalse(transit)
        assertTrue(polylines)
    }

    // ==================== TrackingUIState data class ====================

    @Test
    fun `TrackingUIState construction`() {
        val state = MapActivityHelper.TrackingUIState(
            buttonText = "Start",
            buttonEnabled = true,
            chipGroupVisible = true,
            searchVisible = true,
            routeInfoVisible = false,
            hideTimer = true,
            isIdle = true
        )
        assertEquals("Start", state.buttonText)
        assertTrue(state.buttonEnabled)
        assertTrue(state.isIdle)
    }

    @Test
    fun `TrackingUIState equality`() {
        val s1 = MapActivityHelper.TrackingUIState("a", true, true, true, false, true, true)
        val s2 = MapActivityHelper.TrackingUIState("a", true, true, true, false, true, true)
        assertEquals(s1, s2)
    }

    @Test
    fun `TrackingUIState copy`() {
        val s1 = MapActivityHelper.TrackingUIState("a", true, true, true, false, true, true)
        val s2 = s1.copy(buttonText = "b")
        assertEquals("b", s2.buttonText)
        assertTrue(s2.buttonEnabled)
    }

    @Test
    fun `TrackingUIState destructuring`() {
        val state = MapActivityHelper.TrackingUIState("X", false, true, false, true, false, true)
        val (text, enabled, chip, search, route, timer, idle) = state
        assertEquals("X", text)
        assertFalse(enabled)
        assertTrue(chip)
        assertFalse(search)
        assertTrue(route)
        assertFalse(timer)
        assertTrue(idle)
    }

    @Test
    fun `TrackingUIState hashCode consistent`() {
        val s1 = MapActivityHelper.TrackingUIState("a", true, true, true, false, true, true)
        val s2 = MapActivityHelper.TrackingUIState("a", true, true, true, false, true, true)
        assertEquals(s1.hashCode(), s2.hashCode())
    }

    @Test
    fun `TrackingUIState toString contains fields`() {
        val state = MapActivityHelper.TrackingUIState("text", true, false, true, false, true, false)
        val str = state.toString()
        assertTrue(str.contains("buttonText=text"))
        assertTrue(str.contains("buttonEnabled=true"))
    }

    // ==================== TripCompletionData data class ====================

    @Test
    fun `TripCompletionData construction`() {
        val data = MapActivityHelper.TripCompletionData(
            detectedMode = "bus",
            userSelectedMode = "bus",
            isGreenTrip = true,
            carbonSavedGrams = 150,
            mlConfidence = 0.95
        )
        assertEquals("bus", data.detectedMode)
        assertEquals(150L, data.carbonSavedGrams)
    }

    @Test
    fun `TripCompletionData with null detectedMode`() {
        val data = MapActivityHelper.TripCompletionData(null, "walk", true, 0, null)
        assertNull(data.detectedMode)
        assertNull(data.mlConfidence)
    }

    @Test
    fun `TripCompletionData equality`() {
        val d1 = MapActivityHelper.TripCompletionData("bus", "bus", true, 100, 0.9)
        val d2 = MapActivityHelper.TripCompletionData("bus", "bus", true, 100, 0.9)
        assertEquals(d1, d2)
    }

    @Test
    fun `TripCompletionData copy`() {
        val d1 = MapActivityHelper.TripCompletionData("bus", "bus", true, 100, 0.9)
        val d2 = d1.copy(isGreenTrip = false)
        assertFalse(d2.isGreenTrip)
        assertEquals("bus", d2.detectedMode)
    }

    @Test
    fun `TripCompletionData destructuring`() {
        val data = MapActivityHelper.TripCompletionData("walk", "walk", true, 200, 0.85)
        val (detected, user, green, carbon, conf) = data
        assertEquals("walk", detected)
        assertEquals("walk", user)
        assertTrue(green)
        assertEquals(200L, carbon)
        assertEquals(0.85, conf!!, 0.01)
    }

    // ==================== RouteInfoTexts data class ====================

    @Test
    fun `RouteInfoTexts construction`() {
        val texts = MapActivityHelper.RouteInfoTexts(
            routeTypeText = "低碳路线",
            carbonSavedText = "Saved 1kg",
            carbonColorHex = "#4CAF50",
            ecoRating = "⭐⭐⭐⭐⭐",
            headerText = "Header",
            durationText = "30 min",
            showCumulativeImpact = true,
            showRouteOptions = false,
            showRouteSteps = true
        )
        assertEquals("低碳路线", texts.routeTypeText)
        assertTrue(texts.showCumulativeImpact)
    }

    @Test
    fun `RouteInfoTexts equality`() {
        val t1 = MapActivityHelper.RouteInfoTexts("a", "b", "c", "d", "e", "f", true, false, true)
        val t2 = MapActivityHelper.RouteInfoTexts("a", "b", "c", "d", "e", "f", true, false, true)
        assertEquals(t1, t2)
    }

    @Test
    fun `RouteInfoTexts copy`() {
        val t1 = MapActivityHelper.RouteInfoTexts("a", "b", "c", "d", "e", "f", true, false, true)
        val t2 = t1.copy(routeTypeText = "New")
        assertEquals("New", t2.routeTypeText)
        assertEquals("b", t2.carbonSavedText)
    }

    @Test
    fun `RouteInfoTexts destructuring`() {
        val texts = MapActivityHelper.RouteInfoTexts("a", "b", "c", "d", "e", "f", true, false, true)
        val (rt, cs, cc, er, ht, dt, sci, sro, srs) = texts
        assertEquals("a", rt)
        assertEquals("b", cs)
        assertEquals("c", cc)
        assertEquals("d", er)
        assertEquals("e", ht)
        assertEquals("f", dt)
        assertTrue(sci)
        assertFalse(sro)
        assertTrue(srs)
    }

    // ==================== TrackingMode data class ====================

    @Test
    fun `TrackingMode construction`() {
        val tm = MapActivityHelper.TrackingMode(true, false, true)
        assertTrue(tm.isNavigationMode)
        assertFalse(tm.hasTransitWithPolylines)
        assertTrue(tm.hasTransitFallback)
    }

    @Test
    fun `TrackingMode equality`() {
        val tm1 = MapActivityHelper.TrackingMode(true, true, false)
        val tm2 = MapActivityHelper.TrackingMode(true, true, false)
        assertEquals(tm1, tm2)
    }

    @Test
    fun `TrackingMode inequality`() {
        val tm1 = MapActivityHelper.TrackingMode(true, true, false)
        val tm2 = MapActivityHelper.TrackingMode(false, true, false)
        assertNotEquals(tm1, tm2)
    }

    @Test
    fun `TrackingMode copy`() {
        val tm = MapActivityHelper.TrackingMode(true, false, false)
        val tm2 = tm.copy(hasTransitWithPolylines = true)
        assertTrue(tm2.hasTransitWithPolylines)
    }

    @Test
    fun `TrackingMode destructuring`() {
        val tm = MapActivityHelper.TrackingMode(true, false, true)
        val (nav, transit, fallback) = tm
        assertTrue(nav)
        assertFalse(transit)
        assertTrue(fallback)
    }

    @Test
    fun `TrackingMode hashCode consistent`() {
        val tm1 = MapActivityHelper.TrackingMode(true, true, true)
        val tm2 = MapActivityHelper.TrackingMode(true, true, true)
        assertEquals(tm1.hashCode(), tm2.hashCode())
    }

    // ==================== TransportModeUpdate data class ====================

    @Test
    fun `TransportModeUpdate construction`() {
        val update = MapActivityHelper.TransportModeUpdate(
            detectedTransportMode = "bus",
            modeIcon = "🚌",
            modeText = "公交",
            confidencePercent = 85,
            navigationModeText = "🚌 当前交通: 公交 (85%)",
            trackingModeText = "🚌 检测到: 公交 (85%)"
        )
        assertEquals("bus", update.detectedTransportMode)
        assertEquals(85, update.confidencePercent)
    }

    @Test
    fun `TransportModeUpdate equality`() {
        val u1 = MapActivityHelper.TransportModeUpdate("a", "b", "c", 50, "d", "e")
        val u2 = MapActivityHelper.TransportModeUpdate("a", "b", "c", 50, "d", "e")
        assertEquals(u1, u2)
    }

    @Test
    fun `TransportModeUpdate copy`() {
        val u1 = MapActivityHelper.TransportModeUpdate("a", "b", "c", 50, "d", "e")
        val u2 = u1.copy(confidencePercent = 99)
        assertEquals(99, u2.confidencePercent)
        assertEquals("a", u2.detectedTransportMode)
    }

    @Test
    fun `TransportModeUpdate destructuring`() {
        val update = MapActivityHelper.TransportModeUpdate("walk", "🚶", "步行", 95, "nav", "track")
        val (mode, icon, text, pct, navText, trackText) = update
        assertEquals("walk", mode)
        assertEquals("🚶", icon)
        assertEquals("步行", text)
        assertEquals(95, pct)
        assertEquals("nav", navText)
        assertEquals("track", trackText)
    }

    @Test
    fun `TransportModeUpdate hashCode consistent`() {
        val u1 = MapActivityHelper.TransportModeUpdate("a", "b", "c", 50, "d", "e")
        val u2 = MapActivityHelper.TransportModeUpdate("a", "b", "c", 50, "d", "e")
        assertEquals(u1.hashCode(), u2.hashCode())
    }

    @Test
    fun `TransportModeUpdate toString contains fields`() {
        val update = MapActivityHelper.TransportModeUpdate("bus", "🚌", "公交", 75, "n", "t")
        val str = update.toString()
        assertTrue(str.contains("detectedTransportMode=bus"))
        assertTrue(str.contains("confidencePercent=75"))
    }
}
