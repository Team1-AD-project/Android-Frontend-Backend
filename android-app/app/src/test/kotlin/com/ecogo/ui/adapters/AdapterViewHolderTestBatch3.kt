package com.ecogo.ui.adapters

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Challenge
import com.ecogo.data.ShopItem
import com.ecogo.data.UserChallengeProgress
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AdapterViewHolderTestBatch3 {

    companion object {
        private const val WIZARD_HAT_NAME = "Wizard Hat"
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

    private fun makeActivity(
        id: String = "a1",
        title: String = "Monthly Cleanup",
        description: String = "desc",
        type: String = "OFFLINE",
        status: String = "PUBLISHED",
        rewardCredits: Int = 30,
        currentParticipants: Int = 15,
        startTime: String? = "2026-02-15T10:00:00"
    ) = com.ecogo.data.Activity(
        id = id, title = title, description = description, type = type,
        status = status, rewardCredits = rewardCredits,
        currentParticipants = currentParticipants, startTime = startTime
    )

    @Test
    fun `MonthlyActivityAdapter onCreateViewHolder creates valid holder`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `MonthlyActivityAdapter bind sets title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Campus Cleanup"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Campus Cleanup", holder.itemView.findViewById<TextView>(R.id.text_activity_title).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind formats date from ISO 8601`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(startTime = "2026-02-15T10:00:00"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Feb 15, 2026", holder.itemView.findViewById<TextView>(R.id.text_activity_date).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind formats date for January`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(startTime = "2026-01-05T09:00:00"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Jan 05, 2026", holder.itemView.findViewById<TextView>(R.id.text_activity_date).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind formats date for December`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(startTime = "2025-12-25T18:30:00"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Dec 25, 2025", holder.itemView.findViewById<TextView>(R.id.text_activity_date).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind shows TBD when startTime is null`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(startTime = null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("TBD", holder.itemView.findViewById<TextView>(R.id.text_activity_date).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets participants joined text`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(currentParticipants = 42))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("42 joined", holder.itemView.findViewById<TextView>(R.id.text_activity_participants).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets zero participants`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(currentParticipants = 0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0 joined", holder.itemView.findViewById<TextView>(R.id.text_activity_participants).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets status PUBLISHED to Upcoming`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(status = "PUBLISHED"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Upcoming", holder.itemView.findViewById<TextView>(R.id.text_activity_status).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets status ONGOING to Ongoing`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(status = "ONGOING"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Ongoing", holder.itemView.findViewById<TextView>(R.id.text_activity_status).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets status ENDED to Ended`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(status = "ENDED"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Ended", holder.itemView.findViewById<TextView>(R.id.text_activity_status).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets status DRAFT to Draft`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(status = "DRAFT"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Draft", holder.itemView.findViewById<TextView>(R.id.text_activity_status).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets unknown status as-is`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(status = "CANCELLED"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("CANCELLED", holder.itemView.findViewById<TextView>(R.id.text_activity_status).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets reward credits`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(rewardCredits = 50))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("+50", holder.itemView.findViewById<TextView>(R.id.text_activity_reward).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets zero reward`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(rewardCredits = 0))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("+0", holder.itemView.findViewById<TextView>(R.id.text_activity_reward).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Clean title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Campus Cleanup Day"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDF9", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Workshop title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Eco Workshop"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD57", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Run title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Morning Run"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC3", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Recycl title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Recycling Drive"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u267B\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Friday title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Walk Friday"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDEB6", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Container title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Container Reduction"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF71", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Plant title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Plant a Tree"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF31", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Bike title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Bike to Work"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDEB2", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for Walk title`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Walk Challenge"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDEB6", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for ONLINE type fallback`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Mystery Event", type = "ONLINE"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCBB", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets icon for OFFLINE type fallback`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Mystery Event", type = "OFFLINE"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCCD", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind sets default icon for unknown type`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Something Else", type = "HYBRID"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF31", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter click triggers callback`() {
        var clicked: com.ecogo.data.Activity? = null
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "Click Test"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Click Test", clicked!!.title)
    }

    @Test
    fun `MonthlyActivityAdapter updateData changes items`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity())) {}
        adapter.updateData(listOf(makeActivity("a1"), makeActivity("a2"), makeActivity("a3")))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `MonthlyActivityAdapter updateData with empty list`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity())) {}
        adapter.updateData(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `MonthlyActivityAdapter icon matching is case insensitive`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(title = "CLEAN UP"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDF9", holder.itemView.findViewById<TextView>(R.id.text_activity_icon).text.toString())
    }

    @Test
    fun `MonthlyActivityAdapter bind formats June date`() {
        val adapter = MonthlyActivityAdapter(listOf(makeActivity(startTime = "2026-06-01T00:00:00"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Jun 01, 2026", holder.itemView.findViewById<TextView>(R.id.text_activity_date).text.toString())
    }

    private fun makeChallenge(
        id: String = "c1", title: String = "Walk Challenge",
        description: String = "Walk 10km", type: String = "GREEN_TRIPS_DISTANCE",
        target: Double = 10.0, reward: Int = 200, icon: String = "\uD83C\uDFC6"
    ) = Challenge(id = id, title = title, description = description,
        type = type, target = target, reward = reward, icon = icon)

    private fun makeProgress(
        current: Double = 5.0, target: Double = 10.0, progressPercent: Double = 50.0
    ) = UserChallengeProgress(id = "p1", challengeId = "c1", userId = "u1",
        status = "IN_PROGRESS", current = current, target = target,
        progressPercent = progressPercent, joinedAt = "2026-02-01T00:00:00")

    @Test
    fun `MonthlyChallengeAdapter onCreateViewHolder creates valid holder`() {
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(makeChallenge(), null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
    }

    @Test
    fun `MonthlyChallengeAdapter bind sets icon`() {
        val c = makeChallenge(icon = "\uD83D\uDE80")
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE80", holder.itemView.findViewById<TextView>(R.id.text_challenge_icon).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind sets title`() {
        val c = makeChallenge(title = "Green Commute")
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Green Commute", holder.itemView.findViewById<TextView>(R.id.text_challenge_title).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind sets description`() {
        val c = makeChallenge(description = "Complete 5 green trips")
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Complete 5 green trips", holder.itemView.findViewById<TextView>(R.id.text_challenge_description).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind with progress sets progress bar`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0)
        val p = makeProgress(current = 5.0, progressPercent = 50.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(50, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `MonthlyChallengeAdapter bind with progress shows decimal current for km`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0, reward = 200)
        val p = makeProgress(current = 5.5, progressPercent = 55.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("5.5/10 km \u00B7 +200 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind with progress shows integer current for trips`() {
        val c = makeChallenge(type = "GREEN_TRIPS_COUNT", target = 5.0, reward = 100)
        val p = makeProgress(current = 3.0, target = 5.0, progressPercent = 60.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("3/5 trips \u00B7 +100 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind with progress shows CO2 unit`() {
        val c = makeChallenge(type = "CARBON_SAVED", target = 500.0, reward = 300)
        val p = makeProgress(current = 123.5, target = 500.0, progressPercent = 24.7)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("123.5/500 g CO\u2082 \u00B7 +300 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind clamps progress to 100`() {
        val c = makeChallenge(type = "GREEN_TRIPS_COUNT", target = 5.0)
        val p = makeProgress(current = 7.0, target = 5.0, progressPercent = 140.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(100, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `MonthlyChallengeAdapter bind clamps negative progress to 0`() {
        val c = makeChallenge(type = "GREEN_TRIPS_COUNT", target = 5.0)
        val p = makeProgress(current = 0.0, target = 5.0, progressPercent = -5.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `MonthlyChallengeAdapter bind without progress sets bar to 0`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, holder.itemView.findViewById<ProgressBar>(R.id.progress_challenge).progress)
    }

    @Test
    fun `MonthlyChallengeAdapter bind without progress shows zero for km`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0, reward = 200)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0/10 km \u00B7 +200 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind without progress shows zero for trips`() {
        val c = makeChallenge(type = "GREEN_TRIPS_COUNT", target = 5.0, reward = 100)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0/5 trips \u00B7 +100 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind without progress shows zero for CO2`() {
        val c = makeChallenge(type = "CARBON_SAVED", target = 500.0, reward = 300)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0/500 g CO\u2082 \u00B7 +300 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind with unknown type shows empty unit`() {
        val c = makeChallenge(type = "UNKNOWN_TYPE", target = 10.0, reward = 50)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0/10  \u00B7 +50 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter bind shows decimal target`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 7.5, reward = 150)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("0/7.5 km \u00B7 +150 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    @Test
    fun `MonthlyChallengeAdapter click triggers callback`() {
        var clicked: Challenge? = null
        val c = makeChallenge(title = "Click Me")
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, null))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Click Me", clicked!!.title)
    }

    @Test
    fun `MonthlyChallengeAdapter updateData changes count`() {
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(makeChallenge(), null))) {}
        adapter.updateData(listOf(ChallengeWithProgress(makeChallenge("c1"), null), ChallengeWithProgress(makeChallenge("c2"), null)))
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `MonthlyChallengeAdapter bind integer current shows integer format`() {
        val c = makeChallenge(type = "GREEN_TRIPS_DISTANCE", target = 10.0, reward = 200)
        val p = makeProgress(current = 5.0, progressPercent = 50.0)
        val adapter = MonthlyChallengeAdapter(listOf(ChallengeWithProgress(c, p))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("5/10 km \u00B7 +200 pts", holder.itemView.findViewById<TextView>(R.id.text_challenge_progress).text.toString())
    }

    private fun makeShopItem(
        id: String = "hat_grad", name: String = "Grad Cap", type: String = "head",
        cost: Int = 50, owned: Boolean = false, equipped: Boolean = false
    ) = ShopItem(id = id, name = name, type = type, cost = cost, owned = owned, equipped = equipped)

    @Test
    fun `ShopItemAdapter creates header holder`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Header("H"))) {}
        assertNotNull(adapter.onCreateViewHolder(parent, 0))
    }

    @Test
    fun `ShopItemAdapter creates shop holder`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem()))) {}
        assertNotNull(adapter.onCreateViewHolder(parent, 1))
    }

    @Test
    fun `ShopItemAdapter getItemViewType header vs item`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Header("H"), ShopListItem.Item(makeShopItem()))) {}
        assertEquals(0, adapter.getItemViewType(0))
        assertEquals(1, adapter.getItemViewType(1))
    }

    @Test
    fun `ShopItemAdapter header bind sets title`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Header("Headwear"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Headwear", holder.itemView.findViewById<TextView>(R.id.text_section_title).text.toString())
    }

    @Test
    fun `ShopItemAdapter item bind sets name`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(name = WIZARD_HAT_NAME)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(WIZARD_HAT_NAME, holder.itemView.findViewById<TextView>(R.id.text_name).text.toString())
    }

    @Test
    fun `ShopItemAdapter item bind sets cost`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(cost = 500)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("500 pts", holder.itemView.findViewById<TextView>(R.id.text_cost).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_grad`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_grad")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF93", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_cap`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_cap")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDE2", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_helmet`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_helmet")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u26D1\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_beret`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_beret")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFA8", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_crown`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_crown")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC51", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_party`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_party")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF89", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_beanie`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_beanie")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u2744\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_cowboy`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_cowboy")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD20", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_chef`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_chef")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC68\u200D\uD83C\uDF73", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon hat_wizard`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "hat_wizard")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDD9", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon glasses_sun`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "glasses_sun")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDD76\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_suit`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_suit")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD35", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_ninja`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_ninja")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD77", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_eco_warrior`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_eco_warrior")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF3F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_streak`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_streak")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDD25", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_legend`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_legend")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u2B50", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon unknown defaults to gift`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "xyz")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF81", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter equipped shows status hides cost shows check`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(equipped = true, owned = true)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.VISIBLE, holder.itemView.findViewById<TextView>(R.id.text_status).visibility)
        assertEquals("Equipped", holder.itemView.findViewById<TextView>(R.id.text_status).text.toString())
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.text_cost).visibility)
        assertEquals(View.VISIBLE, holder.itemView.findViewById<View>(R.id.image_check).visibility)
    }

    @Test
    fun `ShopItemAdapter owned shows Owned hides cost hides check`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(owned = true, equipped = false)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Owned", holder.itemView.findViewById<TextView>(R.id.text_status).text.toString())
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.text_cost).visibility)
        assertEquals(View.GONE, holder.itemView.findViewById<View>(R.id.image_check).visibility)
    }

    @Test
    fun `ShopItemAdapter not owned shows cost hides status`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(owned = false, cost = 200)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(View.GONE, holder.itemView.findViewById<TextView>(R.id.text_status).visibility)
        assertEquals(View.VISIBLE, holder.itemView.findViewById<TextView>(R.id.text_cost).visibility)
        assertEquals("200 pts", holder.itemView.findViewById<TextView>(R.id.text_cost).text.toString())
    }

    @Test
    fun `ShopItemAdapter click triggers callback`() {
        var clicked: ShopItem? = null
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(name = "Clicked")))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertEquals("Clicked", clicked!!.name)
    }

    @Test
    fun `ShopItemAdapter isHeader true for header`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Header("H"), ShopListItem.Item(makeShopItem()))) {}
        assertTrue(adapter.isHeader(0))
        assertFalse(adapter.isHeader(1))
    }

    @Test
    fun `ShopItemAdapter isHeader false for out of bounds`() {
        assertFalse(ShopItemAdapter(emptyList()) {}.isHeader(99))
    }

    @Test
    fun `ShopItemAdapter updateItems changes count`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Header("Old"))) {}
        adapter.updateItems(listOf(ShopListItem.Header("N"), ShopListItem.Item(makeShopItem("i1")), ShopListItem.Item(makeShopItem("i2"))))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `ShopItemAdapter equipped card stroke 4`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(equipped = true, owned = true)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(4, (holder.itemView as com.google.android.material.card.MaterialCardView).strokeWidth)
    }

    @Test
    fun `ShopItemAdapter not owned card stroke 0`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(owned = false)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, (holder.itemView as com.google.android.material.card.MaterialCardView).strokeWidth)
    }

    private fun makeGoodsItem(
        id: String = "item1", name: String = "Crown", type: String = "head",
        cost: Int = 100, owned: Boolean = false
    ) = ShopItem(id = id, name = name, type = type, cost = cost, owned = owned)

    @Test
    fun `GoodsAdapter onCreateViewHolder creates valid holder`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem()))
        assertNotNull(adapter.onCreateViewHolder(parent, 0))
    }

    @Test
    fun `GoodsAdapter bind sets name`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Graduation Cap")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Graduation Cap", holder.itemView.findViewById<TextView>(R.id.text_goods_name).text.toString())
    }

    @Test
    fun `GoodsAdapter bind sets price`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(cost = 250)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("250", holder.itemView.findViewById<TextView>(R.id.text_goods_price).text.toString())
    }

    @Test
    fun `GoodsAdapter category chip head`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(type = "head")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC51 Headwear", holder.itemView.findViewById<Chip>(R.id.chip_category).text.toString())
    }

    @Test
    fun `GoodsAdapter category chip face`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(type = "face")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE0E Accessory", holder.itemView.findViewById<Chip>(R.id.chip_category).text.toString())
    }

    @Test
    fun `GoodsAdapter category chip body`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(type = "body")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC55 Outfit", holder.itemView.findViewById<Chip>(R.id.chip_category).text.toString())
    }

    @Test
    fun `GoodsAdapter category chip badge`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(type = "badge")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC5 Badge", holder.itemView.findViewById<Chip>(R.id.chip_category).text.toString())
    }

    @Test
    fun `GoodsAdapter category chip unknown`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(type = "other")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF81 Item", holder.itemView.findViewById<Chip>(R.id.chip_category).text.toString())
    }

    @Test
    fun `GoodsAdapter desc head default`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Hat", type = "head")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Stylish headwear for LiNUS avatar", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc head Crown`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Royal Crown", type = "head")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Royal headwear for your LiNUS avatar", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc head Wizard`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = WIZARD_HAT_NAME, type = "head")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Magical hat with mystical powers", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc face default`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Glasses", type = "face")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Cool accessory for LiNUS face", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc body default`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Shirt", type = "body")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Fashionable outfit for LiNUS avatar", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc badge default`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Badge", type = "badge")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Special achievement badge", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter desc unknown type`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "X", type = "other")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Customize your LiNUS avatar", holder.itemView.findViewById<TextView>(R.id.text_goods_description).text.toString())
    }

    @Test
    fun `GoodsAdapter owned disables button shows chip`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(owned = true)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertFalse(holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).isEnabled)
        assertEquals("Owned", holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).text.toString())
        assertEquals(View.VISIBLE, holder.itemView.findViewById<Chip>(R.id.chip_stock).visibility)
    }

    @Test
    fun `GoodsAdapter not owned enables button hides chip`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(owned = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertTrue(holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).isEnabled)
        assertEquals("Redeem", holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).text.toString())
        assertEquals(View.GONE, holder.itemView.findViewById<Chip>(R.id.chip_stock).visibility)
    }

    @Test
    fun `GoodsAdapter icon head fallback`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Hat", type = "head")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC51", holder.itemView.findViewById<TextView>(R.id.text_goods_icon).text.toString())
    }

    @Test
    fun `GoodsAdapter icon face fallback`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Glasses", type = "face")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE0E", holder.itemView.findViewById<TextView>(R.id.text_goods_icon).text.toString())
    }

    @Test
    fun `GoodsAdapter icon body fallback`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Shirt", type = "body")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC55", holder.itemView.findViewById<TextView>(R.id.text_goods_icon).text.toString())
    }

    @Test
    fun `GoodsAdapter icon badge fallback`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "Badge", type = "badge")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC5", holder.itemView.findViewById<TextView>(R.id.text_goods_icon).text.toString())
    }

    @Test
    fun `GoodsAdapter icon unknown fallback`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(name = "X", type = "unknown")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF81", holder.itemView.findViewById<TextView>(R.id.text_goods_icon).text.toString())
    }

    @Test
    fun `GoodsAdapter click triggers callback`() {
        var clicked: ShopItem? = null
        val adapter = GoodsAdapter { clicked = it }
        adapter.updateGoods(listOf(makeGoodsItem(name = "Click")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertEquals("Click", clicked!!.name)
    }

    @Test
    fun `GoodsAdapter owned alpha`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(owned = true)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0.6f, holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).alpha, 0.01f)
        assertEquals(0.9f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `GoodsAdapter not owned alpha`() {
        val adapter = GoodsAdapter()
        adapter.updateGoods(listOf(makeGoodsItem(owned = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(1.0f, holder.itemView.findViewById<MaterialButton>(R.id.btn_redeem_goods).alpha, 0.01f)
        assertEquals(1.0f, holder.itemView.alpha, 0.01f)
    }

    private fun userMsg(text: String = "Hello") = ChatMessageAdapter.ChatMessage(text = text, isUser = true)
    private fun aiMsg(text: String = "Hi") = ChatMessageAdapter.ChatMessage(text = text, isUser = false)
    private fun suggestionsMsg(s: List<String> = listOf("A", "B")) = ChatMessageAdapter.ChatMessage(text = "", isUser = false, suggestions = s)
    private fun bookingMsg(
        bookingId: String = "b1", fromName: String = "NUS", toName: String = "Orchard",
        departAt: String? = "10:00 AM", passengers: Int = 2, status: String = "confirmed"
    ) = ChatMessageAdapter.ChatMessage(text = "", isUser = false,
        bookingCard = ChatMessageAdapter.BookingCardData(bookingId = bookingId, fromName = fromName, toName = toName, departAt = departAt, passengers = passengers, status = status))

    @Test
    fun `ChatMessageAdapter creates UserMessageViewHolder`() {
        val adapter = ChatMessageAdapter(mutableListOf(userMsg()))
        assertTrue(adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_USER) is ChatMessageAdapter.UserMessageViewHolder)
    }

    @Test
    fun `ChatMessageAdapter creates AiMessageViewHolder`() {
        val adapter = ChatMessageAdapter(mutableListOf(aiMsg()))
        assertTrue(adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_AI) is ChatMessageAdapter.AiMessageViewHolder)
    }

    @Test
    fun `ChatMessageAdapter creates SuggestionsViewHolder`() {
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg()))
        assertTrue(adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS) is ChatMessageAdapter.SuggestionsViewHolder)
    }

    @Test
    fun `ChatMessageAdapter creates BookingCardViewHolder`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg()))
        assertTrue(adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD) is ChatMessageAdapter.BookingCardViewHolder)
    }

    @Test
    fun `ChatMessageAdapter view type USER`() {
        assertEquals(ChatMessageAdapter.VIEW_TYPE_USER, ChatMessageAdapter(mutableListOf(userMsg())).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter view type AI`() {
        assertEquals(ChatMessageAdapter.VIEW_TYPE_AI, ChatMessageAdapter(mutableListOf(aiMsg())).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter view type SUGGESTIONS`() {
        assertEquals(ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS, ChatMessageAdapter(mutableListOf(suggestionsMsg())).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter view type BOOKING_CARD`() {
        assertEquals(ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD, ChatMessageAdapter(mutableListOf(bookingMsg())).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter booking card priority over isUser`() {
        val msg = ChatMessageAdapter.ChatMessage(text = "t", isUser = true,
            bookingCard = ChatMessageAdapter.BookingCardData("b1", null, "A", "B", "10:00", 1, "pending"))
        assertEquals(ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD, ChatMessageAdapter(mutableListOf(msg)).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter suggestions priority over isUser`() {
        val msg = ChatMessageAdapter.ChatMessage(text = "t", isUser = true, suggestions = listOf("A"))
        assertEquals(ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS, ChatMessageAdapter(mutableListOf(msg)).getItemViewType(0))
    }

    @Test
    fun `ChatMessageAdapter user bind sets text`() {
        val adapter = ChatMessageAdapter(mutableListOf(userMsg("Hello World")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_USER)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Hello World", holder.itemView.findViewById<TextView>(R.id.text_message).text.toString())
    }

    @Test
    fun `ChatMessageAdapter ai bind sets text`() {
        val adapter = ChatMessageAdapter(mutableListOf(aiMsg("AI Response")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_AI)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("AI Response", holder.itemView.findViewById<TextView>(R.id.text_message).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking route text`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(fromName = "NUS", toName = "Orchard")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("NUS \u2192 Orchard", holder.itemView.findViewById<TextView>(R.id.text_route).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking departAt`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(departAt = "2:30 PM")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("2:30 PM", holder.itemView.findViewById<TextView>(R.id.text_depart_at).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking departAt null`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(departAt = null)))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Not set", holder.itemView.findViewById<TextView>(R.id.text_depart_at).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking passengers`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(passengers = 3)))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("3 passenger(s)", holder.itemView.findViewById<TextView>(R.id.text_passengers).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking status capitalized`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(status = "confirmed")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Confirmed", holder.itemView.findViewById<TextView>(R.id.text_status).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking pending status`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(status = "pending")))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Pending", holder.itemView.findViewById<TextView>(R.id.text_status).text.toString())
    }

    @Test
    fun `ChatMessageAdapter booking click callback`() {
        var clicked: ChatMessageAdapter.BookingCardData? = null
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg(bookingId = "b42")), onBookingCardClick = { clicked = it })
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.findViewById<View>(R.id.card_booking).performClick()
        assertEquals("b42", clicked!!.bookingId)
    }

    @Test
    fun `ChatMessageAdapter suggestions creates chips`() {
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg(listOf("A", "B", "C"))))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(3, holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).childCount)
    }

    @Test
    fun `ChatMessageAdapter suggestions chip text`() {
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg(listOf("Book", "Check"))))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        val cg = holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions)
        assertEquals("Book", (cg.getChildAt(0) as Chip).text.toString())
        assertEquals("Check", (cg.getChildAt(1) as Chip).text.toString())
    }

    @Test
    fun `ChatMessageAdapter suggestion chip click callback`() {
        var clicked: String? = null
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg(listOf("Try"))), onSuggestionClick = { clicked = it })
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        (holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).getChildAt(0) as Chip).performClick()
        assertEquals("Try", clicked)
    }

    @Test
    fun `ChatMessageAdapter empty suggestions no chips`() {
        val adapter = ChatMessageAdapter(mutableListOf(ChatMessageAdapter.ChatMessage("", false, suggestions = emptyList())))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(0, holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).childCount)
    }

    @Test
    fun `ChatMessageAdapter rebind clears chips`() {
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg(listOf("A", "B", "C")), suggestionsMsg(listOf("X"))))
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(3, holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).childCount)
        adapter.onBindViewHolder(holder, 1)
        assertEquals(1, holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).childCount)
    }

    @Test
    fun `ChatMessageAdapter addMessage increases count`() {
        val adapter = ChatMessageAdapter(mutableListOf(userMsg()))
        adapter.addMessage(aiMsg())
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `ChatMessageAdapter null booking callback no crash`() {
        val adapter = ChatMessageAdapter(mutableListOf(bookingMsg()), onBookingCardClick = null)
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_BOOKING_CARD)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.findViewById<View>(R.id.card_booking).performClick()
    }

    @Test
    fun `ChatMessageAdapter null suggestion callback no crash`() {
        val adapter = ChatMessageAdapter(mutableListOf(suggestionsMsg(listOf("T"))), onSuggestionClick = null)
        val holder = adapter.onCreateViewHolder(parent, ChatMessageAdapter.VIEW_TYPE_SUGGESTIONS)
        adapter.onBindViewHolder(holder, 0)
        (holder.itemView.findViewById<ChipGroup>(R.id.chipGroupSuggestions).getChildAt(0) as Chip).performClick()
    }
}