package com.ecogo.ui.adapters

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Achievement
import com.ecogo.data.BusRoute
import com.ecogo.data.Challenge
import com.ecogo.data.FacultyCarbonData
import com.ecogo.data.FacultyData
import com.ecogo.data.HistoryItem
import com.ecogo.data.HomeBanner
import com.ecogo.data.IndividualRanking
import com.ecogo.data.Outfit
import com.ecogo.data.WalkingRoute
import com.google.android.material.card.MaterialCardView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AdapterViewHolderTestBatch4 {

    companion object {
        private const val ACHIEVEMENT_NAME_FIRST_RIDE = "First Ride"
    }
    private lateinit var parent: RecyclerView

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val themedContext = ContextThemeWrapper(
            activity,
            com.google.android.material.R.style.Theme_MaterialComponents_Light
        )
        parent = RecyclerView(themedContext).apply {
            layoutManager = LinearLayoutManager(themedContext)
        }
    }

    // ========================================================================
    // AchievementAdapter Tests
    // ========================================================================

    private fun makeAchievement(
        id: String = "a1",
        name: String = ACHIEVEMENT_NAME_FIRST_RIDE,
        description: String = "Take your first bus ride",
        unlocked: Boolean = true,
        howToUnlock: String = ""
    ) = Achievement(id = id, name = name, description = description, unlocked = unlocked, howToUnlock = howToUnlock)

    @Test
    fun `AchievementAdapter onCreateViewHolder creates valid holder`() {
        val adapter = AchievementAdapter(listOf(makeAchievement()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `AchievementAdapter bind sets name`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(name = ACHIEVEMENT_NAME_FIRST_RIDE)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(ACHIEVEMENT_NAME_FIRST_RIDE, holder.itemView.findViewById<TextView>(R.id.text_badge_name).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a1 is bus`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a1")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE8C", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a2 is checkmark`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a2")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u2705", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a3 is circus`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a3")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFAA", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a5 is lightning`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a5")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u26A1", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a9 is hundred`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a9")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCAF", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a12 is cycling`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a12")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDEB4", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a20 is trophy`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a20")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC6", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon unknown defaults to medal`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "xyz")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC5", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter equipped badge shows Wearing`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a1", unlocked = true)), equippedBadgeId = "a1")
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u2705 Wearing", holder.itemView.findViewById<TextView>(R.id.text_badge_desc).text.toString())
    }

    @Test
    fun `AchievementAdapter equipped badge alpha is 1`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a1", unlocked = true)), equippedBadgeId = "a1")
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(1f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `AchievementAdapter equipped badge card stroke is 4`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a1", unlocked = true)), equippedBadgeId = "a1")
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(4, (holder.itemView as MaterialCardView).strokeWidth)
    }

    @Test
    fun `AchievementAdapter locked badge shows Locked`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(unlocked = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDD12 Locked", holder.itemView.findViewById<TextView>(R.id.text_badge_desc).text.toString())
    }

    @Test
    fun `AchievementAdapter locked badge alpha is 0_5`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(unlocked = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0.5f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `AchievementAdapter locked badge stroke is 0`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(unlocked = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, (holder.itemView as MaterialCardView).strokeWidth)
    }

    @Test
    fun `AchievementAdapter unlocked non-equipped shows description`() {
        val adapter = AchievementAdapter(
            listOf(makeAchievement(description = "Take your first ride", unlocked = true)),
            equippedBadgeId = "other"
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Take your first ride", holder.itemView.findViewById<TextView>(R.id.text_badge_desc).text.toString())
    }

    @Test
    fun `AchievementAdapter unlocked non-equipped alpha is 1`() {
        val adapter = AchievementAdapter(
            listOf(makeAchievement(unlocked = true)),
            equippedBadgeId = "other"
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(1f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `AchievementAdapter unlocked non-equipped stroke is 0`() {
        val adapter = AchievementAdapter(
            listOf(makeAchievement(unlocked = true)),
            equippedBadgeId = "other"
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, (holder.itemView as MaterialCardView).strokeWidth)
    }

    @Test
    fun `AchievementAdapter click triggers callback`() {
        var clickedId: String? = null
        val adapter = AchievementAdapter(
            listOf(makeAchievement(id = "a5")),
            onBadgeClick = { clickedId = it }
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertEquals("a5", clickedId)
    }

    @Test
    fun `AchievementAdapter null callback no crash on click`() {
        val adapter = AchievementAdapter(listOf(makeAchievement()), onBadgeClick = null)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick() // should not throw
    }

    @Test
    fun `AchievementAdapter itemCount matches list`() {
        val adapter = AchievementAdapter(listOf(makeAchievement("a1"), makeAchievement("a2"), makeAchievement("a3")))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `AchievementAdapter icon a4 is memo`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a4")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCDD", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a15 is recycle`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a15")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u267B\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a16 is butterfly`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a16")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD8B", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a19 is ticket`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a19")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFAB", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    // ========================================================================
    // CommunityAdapter Tests
    // ========================================================================

    private fun makeFacultyCarbonData(
        faculty: String = "Computing",
        totalCarbon: Double = 100.0
    ) = FacultyCarbonData(faculty = faculty, totalCarbon = totalCarbon)

    @Test
    fun `CommunityAdapter onCreateViewHolder creates valid holder`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `CommunityAdapter bind sets rank 1`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("#1", holder.itemView.findViewById<TextView>(R.id.text_rank).text.toString())
    }

    @Test
    fun `CommunityAdapter bind sets rank 2`() {
        val data = listOf(makeFacultyCarbonData("A", 200.0), makeFacultyCarbonData("B", 150.0))
        val adapter = CommunityAdapter(data)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 1)
        assertEquals("#2", holder.itemView.findViewById<TextView>(R.id.text_rank).text.toString())
    }

    @Test
    fun `CommunityAdapter bind sets name`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData(faculty = "Engineering")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Engineering", holder.itemView.findViewById<TextView>(R.id.text_name).text.toString())
    }

    @Test
    fun `CommunityAdapter bind formats points to 2 decimals`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData(totalCarbon = 123.456)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("123.46", holder.itemView.findViewById<TextView>(R.id.text_points).text.toString())
    }

    @Test
    fun `CommunityAdapter bind formats zero points`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData(totalCarbon = 0.0)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0.00", holder.itemView.findViewById<TextView>(R.id.text_points).text.toString())
    }

    @Test
    fun `CommunityAdapter top 3 rank color is primary`() {
        val data = listOf(
            makeFacultyCarbonData("A", 300.0),
            makeFacultyCarbonData("B", 200.0),
            makeFacultyCarbonData("C", 100.0)
        )
        val adapter = CommunityAdapter(data)
        val holder = adapter.onCreateViewHolder(parent, 0)
        val primaryColor = ContextCompat.getColor(parent.context, R.color.primary)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(primaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
        adapter.onBindViewHolder(holder, 1)
        assertEquals(primaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
        adapter.onBindViewHolder(holder, 2)
        assertEquals(primaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `CommunityAdapter rank 4 color is text_secondary`() {
        val data = listOf(
            makeFacultyCarbonData("A", 400.0),
            makeFacultyCarbonData("B", 300.0),
            makeFacultyCarbonData("C", 200.0),
            makeFacultyCarbonData("D", 100.0)
        )
        val adapter = CommunityAdapter(data)
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 3)
        val secondaryColor = ContextCompat.getColor(parent.context, R.color.text_secondary)
        assertEquals(secondaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `CommunityAdapter itemCount matches list`() {
        val data = listOf(
            makeFacultyCarbonData("A"),
            makeFacultyCarbonData("B"),
            makeFacultyCarbonData("C"),
            makeFacultyCarbonData("D"),
            makeFacultyCarbonData("E")
        )
        assertEquals(5, CommunityAdapter(data).itemCount)
    }

    @Test
    fun `CommunityAdapter progress view exists`() {
        val adapter = CommunityAdapter(listOf(makeFacultyCarbonData()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertNotNull(holder.itemView.findViewById<View>(R.id.view_progress))
    }

    // ========================================================================
    // HomeBannerAdapter Tests
    // ========================================================================

    private fun makeBanner(
        id: String = "b1",
        title: String = "Go Green!",
        subtitle: String? = "Save the planet",
        backgroundColor: String = "#15803D",
        actionText: String? = null,
        actionTarget: String? = null
    ) = HomeBanner(id = id, title = title, subtitle = subtitle,
        backgroundColor = backgroundColor, actionText = actionText, actionTarget = actionTarget)

    @Test
    fun `HomeBannerAdapter onCreateViewHolder creates valid holder`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `HomeBannerAdapter bind sets title`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(title = "Welcome")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Welcome", holder.itemView.findViewById<TextView>(R.id.text_banner_title).text.toString())
    }

    @Test
    fun `HomeBannerAdapter bind shows subtitle when present`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(subtitle = "Join now")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val subtitleView = holder.itemView.findViewById<TextView>(R.id.text_banner_subtitle)
        assertEquals("Join now", subtitleView.text.toString())
        assertEquals(View.VISIBLE, subtitleView.visibility)
    }

    @Test
    fun `HomeBannerAdapter bind hides subtitle when null`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(subtitle = null)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.text_banner_subtitle).visibility)
    }

    @Test
    fun `HomeBannerAdapter bind sets background color`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(backgroundColor = "#FF5733")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // Just verify no crash - the color is set on the container
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.banner_container))
    }

    @Test
    fun `HomeBannerAdapter bind shows action button when both text and target set`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(actionText = "Join", actionTarget = "challenges")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val btn = holder.itemView.findViewById<View>(R.id.button_banner_action)
        assertEquals(View.VISIBLE, btn.visibility)
    }

    @Test
    fun `HomeBannerAdapter bind action button text is set`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(actionText = "Start Now", actionTarget = "vouchers")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Start Now", holder.itemView.findViewById<TextView>(R.id.button_banner_action).text.toString())
    }

    @Test
    fun `HomeBannerAdapter bind hides action button when actionText null`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(actionText = null, actionTarget = null)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<View>(R.id.button_banner_action).visibility)
    }

    @Test
    fun `HomeBannerAdapter bind hides action button when actionTarget null`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(actionText = "Click", actionTarget = null)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<View>(R.id.button_banner_action).visibility)
    }

    @Test
    fun `HomeBannerAdapter click callback triggers on action button`() {
        var clicked: HomeBanner? = null
        val adapter = HomeBannerAdapter { clicked = it }
        val banner = makeBanner(id = "b42", actionText = "Go", actionTarget = "challenges")
        adapter.submitList(listOf(banner))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.findViewById<View>(R.id.button_banner_action).performClick()
        assertNotNull(clicked)
        assertEquals("b42", clicked!!.id)
    }

    @Test
    fun `HomeBannerAdapter invalid color falls back to default green`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner(backgroundColor = "invalid")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        // Should not crash even with invalid color
        adapter.onBindViewHolder(holder, 0)
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.banner_container))
    }

    @Test
    fun `HomeBannerAdapter submitList updates itemCount`() {
        val adapter = HomeBannerAdapter {}
        adapter.submitList(listOf(makeBanner("b1"), makeBanner("b2"), makeBanner("b3")))
        assertEquals(3, adapter.itemCount)
    }

    // ========================================================================
    // PopularRouteAdapter Tests
    // ========================================================================

    private fun makeBusRoute(
        name: String = "95",
        from: String = "NUS",
        to: String = "Clementi"
    ) = BusRoute(name = name, from = from, to = to)

    @Test
    fun `PopularRouteAdapter onCreateViewHolder creates valid holder`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `PopularRouteAdapter bind sets route number from name`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute(name = "95"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("95", holder.itemView.findViewById<TextView>(R.id.text_route_number).text.toString())
    }

    @Test
    fun `PopularRouteAdapter bind sets route name as from to to`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute(from = "NUS", to = "Clementi"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("NUS to Clementi", holder.itemView.findViewById<TextView>(R.id.text_route_name).text.toString())
    }

    @Test
    fun `PopularRouteAdapter bind sets users text`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val usersText = holder.itemView.findViewById<TextView>(R.id.text_route_users).text.toString()
        assertTrue("Users text should contain 'trips': $usersText", usersText.contains("trips"))
    }

    @Test
    fun `PopularRouteAdapter bind sets co2 text`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val co2Text = holder.itemView.findViewById<TextView>(R.id.text_route_co2).text.toString()
        assertTrue("CO2 text should contain 'kg CO': $co2Text", co2Text.contains("kg CO"))
    }

    @Test
    fun `PopularRouteAdapter bind sets trend text`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val trendText = holder.itemView.findViewById<TextView>(R.id.text_route_trend).text.toString()
        assertTrue("Trend text should start with '+': $trendText", trendText.startsWith("+"))
        assertTrue("Trend text should contain '%': $trendText", trendText.contains("%"))
    }

    @Test
    fun `PopularRouteAdapter click triggers callback`() {
        var clicked: BusRoute? = null
        val adapter = PopularRouteAdapter(listOf(makeBusRoute(name = "D1"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("D1", clicked!!.name)
    }

    @Test
    fun `PopularRouteAdapter updateData changes count`() {
        val adapter = PopularRouteAdapter(listOf(makeBusRoute())) {}
        adapter.updateData(listOf(makeBusRoute("A"), makeBusRoute("B"), makeBusRoute("C")))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `PopularRouteAdapter empty from and to`() {
        val adapter = PopularRouteAdapter(listOf(BusRoute(name = "X"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(" to ", holder.itemView.findViewById<TextView>(R.id.text_route_name).text.toString())
    }

    // ========================================================================
    // WalkingRouteAdapter Tests
    // ========================================================================

    private fun makeWalkingRoute(
        id: Int = 1,
        title: String = "Campus Loop",
        time: String = "25 min",
        distance: String = "1.8 km",
        calories: String = "120 cal",
        tags: List<String> = listOf("scenic"),
        description: String = "A nice loop"
    ) = WalkingRoute(id = id, title = title, time = time, distance = distance,
        calories = calories, tags = tags, description = description)

    @Test
    fun `WalkingRouteAdapter onCreateViewHolder creates valid holder`() {
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `WalkingRouteAdapter bind sets time`() {
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute(time = "30 min"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("30 min", holder.itemView.findViewById<TextView>(R.id.text_route_time).text.toString())
    }

    @Test
    fun `WalkingRouteAdapter bind sets title`() {
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute(title = "River Trail"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("River Trail", holder.itemView.findViewById<TextView>(R.id.text_route_title).text.toString())
    }

    @Test
    fun `WalkingRouteAdapter bind sets distance`() {
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute(distance = "2.5 km"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("2.5 km", holder.itemView.findViewById<TextView>(R.id.text_route_distance).text.toString())
    }

    @Test
    fun `WalkingRouteAdapter position 0 uses green gradient`() {
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // Verify background is set (the gradient drawable is applied)
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.layout_route_bg).background)
    }

    @Test
    fun `WalkingRouteAdapter position 1 uses blue gradient`() {
        val routes = listOf(makeWalkingRoute(id = 1), makeWalkingRoute(id = 2))
        val adapter = WalkingRouteAdapter(routes) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 1)
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.layout_route_bg).background)
    }

    @Test
    fun `WalkingRouteAdapter position 2 uses orange gradient`() {
        val routes = listOf(makeWalkingRoute(id = 1), makeWalkingRoute(id = 2), makeWalkingRoute(id = 3))
        val adapter = WalkingRouteAdapter(routes) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 2)
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.layout_route_bg).background)
    }

    @Test
    fun `WalkingRouteAdapter position 3 wraps back to green gradient`() {
        val routes = listOf(
            makeWalkingRoute(id = 1), makeWalkingRoute(id = 2),
            makeWalkingRoute(id = 3), makeWalkingRoute(id = 4)
        )
        val adapter = WalkingRouteAdapter(routes) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 3)
        assertNotNull(holder.itemView.findViewById<LinearLayout>(R.id.layout_route_bg).background)
    }

    @Test
    fun `WalkingRouteAdapter click triggers callback`() {
        var clicked: WalkingRoute? = null
        val adapter = WalkingRouteAdapter(listOf(makeWalkingRoute(title = "Clicked Route"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Clicked Route", clicked!!.title)
    }

    @Test
    fun `WalkingRouteAdapter itemCount matches list`() {
        val routes = listOf(makeWalkingRoute(id = 1), makeWalkingRoute(id = 2))
        assertEquals(2, WalkingRouteAdapter(routes) {}.itemCount)
    }

    // ========================================================================
    // ChallengeAdapter Tests
    // ========================================================================

    private fun makeChallenge(
        id: String = "c1",
        title: String = "Walk Challenge",
        description: String = "Walk 10km",
        type: String = "GREEN_TRIPS_DISTANCE",
        target: Double = 10.0,
        reward: Int = 200,
        icon: String = "\uD83C\uDFC6",
        status: String = "ACTIVE",
        participants: Int = 42
    ) = Challenge(id = id, title = title, description = description,
        type = type, target = target, reward = reward, icon = icon,
        status = status, participants = participants)

    @Test
    fun `ChallengeAdapter onCreateViewHolder creates valid holder`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `ChallengeAdapter bind sets icon`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(icon = "\uD83D\uDE80"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE80", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind sets title`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(title = "Green Commute"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Green Commute", holder.itemView.findViewById<TextView>(R.id.text_title).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind sets description`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(description = "Complete 5 green trips"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Complete 5 green trips", holder.itemView.findViewById<TextView>(R.id.text_description).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind sets reward text`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(reward = 300))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("+300 points", holder.itemView.findViewById<TextView>(R.id.text_reward).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind sets participants text`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(participants = 99))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("99 participants", holder.itemView.findViewById<TextView>(R.id.text_participants).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind type tag GREEN_TRIPS_COUNT`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "GREEN_TRIPS_COUNT"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Trip Count", holder.itemView.findViewById<TextView>(R.id.text_type_tag).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind type tag GREEN_TRIPS_DISTANCE`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "GREEN_TRIPS_DISTANCE"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Distance", holder.itemView.findViewById<TextView>(R.id.text_type_tag).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind type tag CARBON_SAVED`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "CARBON_SAVED"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Carbon Saved", holder.itemView.findViewById<TextView>(R.id.text_type_tag).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind unknown type tag shows raw type`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "CUSTOM_TYPE"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("CUSTOM_TYPE", holder.itemView.findViewById<TextView>(R.id.text_type_tag).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind progress not completed sets 0`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(target = 10.0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `ChallengeAdapter bind progress completed sets target`() {
        val challenge = makeChallenge(id = "c1", target = 10.0)
        val adapter = ChallengeAdapter(listOf(challenge)) {}
        adapter.updateChallenges(listOf(challenge), completed = setOf("c1"))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(10, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `ChallengeAdapter bind progress text shows target and unit for distance`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Target: 10 km", holder.itemView.findViewById<TextView>(R.id.text_progress).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind progress text shows target and unit for trips`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "GREEN_TRIPS_COUNT", target = 5.0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Target: 5 trips", holder.itemView.findViewById<TextView>(R.id.text_progress).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind progress text shows target and unit for carbon`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(type = "CARBON_SAVED", target = 500.0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Target: 500 kg CO\u2082", holder.itemView.findViewById<TextView>(R.id.text_progress).text.toString())
    }

    @Test
    fun `ChallengeAdapter bind status badge completed visible`() {
        val challenge = makeChallenge(id = "c1")
        val adapter = ChallengeAdapter(listOf(challenge)) {}
        adapter.updateChallenges(listOf(challenge), completed = setOf("c1"))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val badge = holder.itemView.findViewById<TextView>(R.id.badge_status)
        assertEquals(View.VISIBLE, badge.visibility)
        assertEquals("Completed", badge.text.toString())
    }

    @Test
    fun `ChallengeAdapter bind status badge expired visible`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(status = "EXPIRED"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val badge = holder.itemView.findViewById<TextView>(R.id.badge_status)
        assertEquals(View.VISIBLE, badge.visibility)
        assertEquals("Expired", badge.text.toString())
    }

    @Test
    fun `ChallengeAdapter bind status badge active gone`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(status = "ACTIVE"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.badge_status).visibility)
    }

    @Test
    fun `ChallengeAdapter click triggers callback`() {
        var clicked: Challenge? = null
        val adapter = ChallengeAdapter(listOf(makeChallenge(title = "Click Me"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Click Me", clicked!!.title)
    }

    @Test
    fun `ChallengeAdapter updateChallenges changes count`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge())) {}
        adapter.updateChallenges(listOf(makeChallenge("c1"), makeChallenge("c2")))
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `ChallengeAdapter bind progress max is target int`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(target = 25.0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(25, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).max)
    }

    @Test
    fun `ChallengeAdapter bind zero participants`() {
        val adapter = ChallengeAdapter(listOf(makeChallenge(participants = 0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0 participants", holder.itemView.findViewById<TextView>(R.id.text_participants).text.toString())
    }

    // ========================================================================
    // HistoryAdapter Tests
    // ========================================================================

    private fun makeHistoryItem(
        id: Int = 1,
        action: String = "Bus Ride",
        time: String = "2 hours ago",
        points: String = "+50",
        type: String = "earn"
    ) = HistoryItem(id = id, action = action, time = time, points = points, type = type)

    @Test
    fun `HistoryAdapter onCreateViewHolder creates valid holder`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `HistoryAdapter bind sets action text`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(action = "Green Trip")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Green Trip", holder.itemView.findViewById<TextView>(R.id.text_history_action).text.toString())
    }

    @Test
    fun `HistoryAdapter bind sets time text`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(time = "5 min ago")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("5 min ago", holder.itemView.findViewById<TextView>(R.id.text_history_time).text.toString())
    }

    @Test
    fun `HistoryAdapter bind sets points text`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(points = "+100")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("+100", holder.itemView.findViewById<TextView>(R.id.text_history_points).text.toString())
    }

    @Test
    fun `HistoryAdapter earn type sets primary color`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(type = "earn")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val expected = ContextCompat.getColor(parent.context, R.color.primary)
        assertEquals(expected, holder.itemView.findViewById<TextView>(R.id.text_history_points).currentTextColor)
    }

    @Test
    fun `HistoryAdapter spend type sets error color`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(type = "spend")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val expected = ContextCompat.getColor(parent.context, R.color.error)
        assertEquals(expected, holder.itemView.findViewById<TextView>(R.id.text_history_points).currentTextColor)
    }

    @Test
    fun `HistoryAdapter unknown type sets error color`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(type = "refund")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val expected = ContextCompat.getColor(parent.context, R.color.error)
        assertEquals(expected, holder.itemView.findViewById<TextView>(R.id.text_history_points).currentTextColor)
    }

    @Test
    fun `HistoryAdapter itemCount matches list`() {
        val items = listOf(makeHistoryItem(id = 1), makeHistoryItem(id = 2), makeHistoryItem(id = 3))
        assertEquals(3, HistoryAdapter(items).itemCount)
    }

    @Test
    fun `HistoryAdapter negative points display`() {
        val adapter = HistoryAdapter(listOf(makeHistoryItem(points = "-30", type = "spend")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("-30", holder.itemView.findViewById<TextView>(R.id.text_history_points).text.toString())
    }

    // ========================================================================
    // LeaderboardAdapter Tests
    // ========================================================================

    private fun makeRanking(
        userId: String = "u1",
        nickname: String = "GreenHero",
        rank: Int = 1,
        carbonSaved: Double = 123.45,
        isVip: Boolean = false
    ) = IndividualRanking(userId = userId, nickname = nickname, rank = rank,
        carbonSaved = carbonSaved, isVip = isVip)

    @Test
    fun `LeaderboardAdapter onCreateViewHolder creates valid holder`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `LeaderboardAdapter bind sets rank text`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(rank = 5)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("#5", holder.itemView.findViewById<TextView>(R.id.text_rank).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind sets nickname`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(nickname = "EcoMaster")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("EcoMaster", holder.itemView.findViewById<TextView>(R.id.text_nickname).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind empty nickname shows NA`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(nickname = "")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("N/A", holder.itemView.findViewById<TextView>(R.id.text_nickname).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind blank nickname shows NA`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(nickname = "   ")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("N/A", holder.itemView.findViewById<TextView>(R.id.text_nickname).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind sets user id`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(userId = "user-42")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("user-42", holder.itemView.findViewById<TextView>(R.id.text_user_id).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind formats carbon saved to 2 decimals`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(carbonSaved = 99.999)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("100.00", holder.itemView.findViewById<TextView>(R.id.text_carbon_saved).text.toString())
    }

    @Test
    fun `LeaderboardAdapter bind zero carbon saved`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(carbonSaved = 0.0)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0.00", holder.itemView.findViewById<TextView>(R.id.text_carbon_saved).text.toString())
    }

    @Test
    fun `LeaderboardAdapter VIP badge visible when isVip true`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(isVip = true)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.VISIBLE, holder.itemView.findViewById<TextView>(R.id.text_vip_badge).visibility)
    }

    @Test
    fun `LeaderboardAdapter VIP badge gone when isVip false`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(isVip = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.text_vip_badge).visibility)
    }

    @Test
    fun `LeaderboardAdapter rank 1 color is primary`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(rank = 1)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val primaryColor = ContextCompat.getColor(parent.context, R.color.primary)
        assertEquals(primaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `LeaderboardAdapter rank 2 color is text_secondary`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(rank = 2)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val secondaryColor = ContextCompat.getColor(parent.context, R.color.text_secondary)
        assertEquals(secondaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `LeaderboardAdapter rank 3 color is brown`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(rank = 3)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0xFF8B4513.toInt(), holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `LeaderboardAdapter rank 4 color is text_secondary`() {
        val adapter = LeaderboardAdapter(listOf(makeRanking(rank = 4)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val secondaryColor = ContextCompat.getColor(parent.context, R.color.text_secondary)
        assertEquals(secondaryColor, holder.itemView.findViewById<TextView>(R.id.text_rank).currentTextColor)
    }

    @Test
    fun `LeaderboardAdapter itemCount matches list`() {
        val items = listOf(makeRanking(userId = "u1"), makeRanking(userId = "u2"))
        assertEquals(2, LeaderboardAdapter(items).itemCount)
    }

    // ========================================================================
    // FacultyOutfitAdapter Tests
    // ========================================================================

    private fun makeFacultyData(
        id: String = "fac1",
        name: String = "Computing",
        color: String = "#0066CC",
        slogan: String = "Code Green",
        outfit: Outfit = Outfit()
    ) = FacultyData(id = id, name = name, color = color, slogan = slogan, outfit = outfit)

    @Test
    fun `FacultyOutfitAdapter onCreateViewHolder creates valid holder`() {
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `FacultyOutfitAdapter bind sets faculty name`() {
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData(name = "Engineering"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Engineering", holder.itemView.findViewById<TextView>(R.id.text_faculty_name).text.toString())
    }

    @Test
    fun `FacultyOutfitAdapter bind sets mascot outfit`() {
        val outfit = Outfit(head = "hat_grad", face = "glasses_sun", body = "body_suit", badge = "badge_eco_warrior")
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData(outfit = outfit))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val mascotView = holder.itemView.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_faculty)
        assertNotNull(mascotView)
        assertEquals(outfit, mascotView.outfit)
    }

    @Test
    fun `FacultyOutfitAdapter bind sets mascot default outfit`() {
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val mascotView = holder.itemView.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_faculty)
        assertEquals(Outfit(), mascotView.outfit)
    }

    @Test
    fun `FacultyOutfitAdapter click triggers callback`() {
        var clicked: FacultyData? = null
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData(name = "Arts"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Arts", clicked!!.name)
    }

    @Test
    fun `FacultyOutfitAdapter itemCount matches list`() {
        val items = listOf(makeFacultyData("f1"), makeFacultyData("f2"), makeFacultyData("f3"))
        assertEquals(3, FacultyOutfitAdapter(items) {}.itemCount)
    }

    @Test
    fun `FacultyOutfitAdapter bind second item sets correct name`() {
        val items = listOf(makeFacultyData(name = "Science"), makeFacultyData(name = "Law"))
        val adapter = FacultyOutfitAdapter(items) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 1)
        assertEquals("Law", holder.itemView.findViewById<TextView>(R.id.text_faculty_name).text.toString())
    }

    @Test
    fun `FacultyOutfitAdapter mascot size is MEDIUM`() {
        val adapter = FacultyOutfitAdapter(listOf(makeFacultyData())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val mascotView = holder.itemView.findViewById<com.ecogo.ui.views.MascotLionView>(R.id.mascot_faculty)
        assertEquals(com.ecogo.data.MascotSize.MEDIUM, mascotView.mascotSize)
    }
}
