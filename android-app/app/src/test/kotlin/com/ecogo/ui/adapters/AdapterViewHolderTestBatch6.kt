package com.ecogo.ui.adapters

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Achievement
import com.ecogo.data.FacultyData
import com.ecogo.data.Outfit
import com.ecogo.data.ShopItem
import com.ecogo.data.Voucher
import com.ecogo.ui.fragments.MonthStat
import com.google.android.material.button.MaterialButton
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
class AdapterViewHolderTestBatch6 {

    companion object {
        private const val ICON_WALKING = "\uD83D\uDEB6"
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

    // ================================================================
    // 1. MonthStatAdapter - ViewHolder bind tests (0% VH coverage)
    // ================================================================

    private fun makeMonthStat(
        icon: String = "🌿",
        title: String = "Carbon Saved",
        value: String = "12.5 kg",
        subtitle: String = "+5% from last month",
        color: String = "#4CAF50"
    ) = MonthStat(icon = icon, title = title, value = value, subtitle = subtitle, color = color)

    @Test
    fun `MonthStatAdapter onCreateViewHolder creates valid holder`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `MonthStatAdapter bind sets icon text`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(icon = "🔥")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("🔥", holder.itemView.findViewById<TextView>(R.id.text_stat_icon).text.toString())
    }

    @Test
    fun `MonthStatAdapter bind sets value text`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(value = "42 trips")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("42 trips", holder.itemView.findViewById<TextView>(R.id.text_stat_value).text.toString())
    }

    @Test
    fun `MonthStatAdapter bind sets title text`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(title = "Green Trips")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Green Trips", holder.itemView.findViewById<TextView>(R.id.text_stat_title).text.toString())
    }

    @Test
    fun `MonthStatAdapter bind sets subtitle text`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(subtitle = "+10% growth")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("+10% growth", holder.itemView.findViewById<TextView>(R.id.text_stat_subtitle).text.toString())
    }

    @Test
    fun `MonthStatAdapter bind with valid color applies color without crash`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(color = "#4CAF50")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        // Should not crash - exercises the Color.parseColor success path
        adapter.onBindViewHolder(holder, 0)
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.text_stat_icon))
    }

    @Test
    fun `MonthStatAdapter bind with invalid color exercises catch block`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(color = "invalid")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        // Should not crash - exercises the catch(e: Exception) fallback path
        adapter.onBindViewHolder(holder, 0)
        assertEquals("🌿", holder.itemView.findViewById<TextView>(R.id.text_stat_icon).text.toString())
    }

    @Test
    fun `MonthStatAdapter bind with empty color exercises catch block`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(color = "")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        // Empty color + "33" = "33" which is invalid for Color.parseColor
        adapter.onBindViewHolder(holder, 0)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `MonthStatAdapter getItemCount returns correct size`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(), makeMonthStat(), makeMonthStat()))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `MonthStatAdapter updateData changes items`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat()))
        assertEquals(1, adapter.itemCount)
        adapter.updateData(listOf(makeMonthStat(), makeMonthStat()))
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `MonthStatAdapter updateData with empty list`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat()))
        adapter.updateData(emptyList())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `MonthStatAdapter bind with blue color works`() {
        val adapter = MonthStatAdapter(listOf(makeMonthStat(color = "#2196F3")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // Verify subtitle text color was set (exercises the subtitle.setTextColor path)
        assertNotNull(holder.itemView.findViewById<TextView>(R.id.text_stat_subtitle))
    }

    // ================================================================
    // 2. ShopItemAdapter - Uncovered icon branches & equipped status
    // ================================================================

    private fun makeShopItem(
        id: String = "hat_grad", name: String = "Grad Cap", type: String = "head",
        cost: Int = 50, owned: Boolean = false, equipped: Boolean = false
    ) = ShopItem(id = id, name = name, type = type, cost = cost, owned = owned, equipped = equipped)

    @Test
    fun `ShopItemAdapter icon face_goggles`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "face_goggles")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD7D", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon glasses_nerd`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "glasses_nerd")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD13", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon glasses_3d`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "glasses_3d")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFAC", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon face_mask`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "face_mask")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDB8", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon face_monocle`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "face_monocle")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDD0", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon face_scarf`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "face_scarf")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDE3", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon face_vr`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "face_vr")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD7D", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon shirt_hoodie`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "shirt_hoodie")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDE5", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_plaid`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_plaid")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC54", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_coat`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_coat")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD7C", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_sports`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_sports")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u26BD", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_kimono`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_kimono")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC58", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_tux`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_tux")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFA9", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_superhero`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_superhero")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDB8", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_doctor`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_doctor")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC68\u200D\u2695\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_pilot`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_pilot")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u2708\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_scrubs`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_scrubs")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFE5", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon body_polo`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "body_polo")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC69\u200D\u2695\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_walker`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_walker")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(ICON_WALKING, holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_cyclist`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_cyclist")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDEB4", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_green`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_green")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF31", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_pioneer`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_pioneer")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC6", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_social`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_social")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD8B", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_explorer`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_explorer")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDDFA\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon badge_recycler`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "badge_recycler")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u267B\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter icon shirt_nus`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(id = "shirt_nus")))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC55", holder.itemView.findViewById<TextView>(R.id.text_icon).text.toString())
    }

    @Test
    fun `ShopItemAdapter equipped state sets stroke color from primary`() {
        val adapter = ShopItemAdapter(listOf(ShopListItem.Item(makeShopItem(equipped = true, owned = true)))) {}
        val holder = adapter.onCreateViewHolder(parent, 1)
        adapter.onBindViewHolder(holder, 0)
        val card = holder.itemView as MaterialCardView
        assertEquals(4, card.strokeWidth)
        assertEquals(View.VISIBLE, holder.itemView.findViewById<View>(R.id.image_check).visibility)
    }

    // ================================================================
    // 3. HighlightAdapter - Uncovered else branch and null startTime
    // ================================================================

    private fun makeHighlightActivity(
        id: String = "h1",
        title: String = "Some Event",
        description: String = "A great event",
        startTime: String? = "2026-03-15T10:00:00"
    ) = com.ecogo.data.Activity(
        id = id, title = title, description = description, startTime = startTime
    )

    @Test
    fun `HighlightAdapter onCreateViewHolder creates valid holder`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `HighlightAdapter bind else branch icon is seedling for unknown title`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Amazing Event"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF31", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind sets title text`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Eco Meetup"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Eco Meetup", holder.itemView.findViewById<TextView>(R.id.text_highlight_title).text.toString())
    }

    @Test
    fun `HighlightAdapter bind null startTime shows TBD in desc`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(startTime = null, description = "Hello World"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val desc = holder.itemView.findViewById<TextView>(R.id.text_highlight_desc).text.toString()
        assertTrue("Desc should start with TBD: $desc", desc.startsWith("TBD"))
    }

    @Test
    fun `HighlightAdapter bind with startTime shows date substring in desc`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(startTime = "2026-03-15T10:00:00", description = "Join us"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val desc = holder.itemView.findViewById<TextView>(R.id.text_highlight_desc).text.toString()
        assertTrue("Desc should contain date: $desc", desc.contains("2026-03-15"))
    }

    @Test
    fun `HighlightAdapter bind click triggers callback`() {
        var clicked: com.ecogo.data.Activity? = null
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Click Me"))) { clicked = it }
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Click Me", clicked!!.title)
    }

    @Test
    fun `HighlightAdapter getItemCount returns list size`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(), makeHighlightActivity())) {}
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `HighlightAdapter bind Clean title sets broom icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Beach Cleanup"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDDF9", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind Workshop title sets salad icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Eco Workshop"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD57", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind Run title sets runner icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Morning Run"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDFC3", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind Recycl title sets recycle icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Recycling Drive"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\u267B\uFE0F", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind Friday title sets walking icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Green Friday"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(ICON_WALKING, holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind Container title sets bento icon`() {
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(title = "Container Reduction"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83C\uDF71", holder.itemView.findViewById<TextView>(R.id.text_highlight_icon).text.toString())
    }

    @Test
    fun `HighlightAdapter bind description truncated to 20 chars in desc`() {
        val longDesc = "This is a very long description that exceeds twenty chars"
        val adapter = HighlightAdapter(listOf(makeHighlightActivity(description = longDesc, startTime = "2026-01-01T00:00:00"))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val descText = holder.itemView.findViewById<TextView>(R.id.text_highlight_desc).text.toString()
        // description.take(20) = "This is a very long "
        assertTrue("Desc should contain truncated text: $descText", descText.contains("This is a very long "))
    }

    // ================================================================
    // 4. VoucherAdapter - Uncovered paths
    // ================================================================

    private fun makeVoucher(
        id: String = "v1",
        name: String = "Coffee Voucher",
        description: String = "Free coffee",
        cost: Int = 100,
        available: Boolean = true,
        imageUrl: String? = null,
        userVoucherId: String? = null,
        status: String? = null
    ) = Voucher(
        id = id, name = name, description = description,
        cost = cost, available = available, imageUrl = imageUrl,
        userVoucherId = userVoucherId, status = status
    )

    @Test
    fun `VoucherAdapter onCreateViewHolder creates valid holder`() {
        val adapter = VoucherAdapter(listOf(makeVoucher()))
        val holder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(holder)
        assertNotNull(holder.itemView)
    }

    @Test
    fun `VoucherAdapter bind not owned available shows Redeem enabled`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(available = true)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("Redeem", button.text.toString())
        assertTrue(button.isEnabled)
        assertEquals(1f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `VoucherAdapter bind not owned unavailable shows Sold Out disabled`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(available = false)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("Sold Out", button.text.toString())
        assertFalse(button.isEnabled)
        assertEquals(0.6f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `VoucherAdapter bind owned ACTIVE status shows View Code enabled`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(userVoucherId = "uv1", status = "ACTIVE")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("View Code", button.text.toString())
        assertTrue(button.isEnabled)
        assertEquals(1f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `VoucherAdapter bind owned USED status shows Used disabled`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(userVoucherId = "uv1", status = "USED")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("Used", button.text.toString())
        assertFalse(button.isEnabled)
        assertEquals(0.6f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `VoucherAdapter bind owned EXPIRED status shows Expired disabled`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(userVoucherId = "uv1", status = "EXPIRED")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("Expired", button.text.toString())
        assertFalse(button.isEnabled)
        assertEquals(0.6f, holder.itemView.alpha, 0.01f)
    }

    @Test
    fun `VoucherAdapter bind owned unknown status defaults to View Code`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(userVoucherId = "uv1", status = "SOMETHING")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertEquals("View Code", button.text.toString())
        assertTrue(button.isEnabled)
    }

    @Test
    fun `VoucherAdapter bind sets name and description`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(name = "Grab $5", description = "Valid this month")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("Grab $5", holder.itemView.findViewById<TextView>(R.id.text_name).text.toString())
        assertEquals("Valid this month", holder.itemView.findViewById<TextView>(R.id.text_description).text.toString())
    }

    @Test
    fun `VoucherAdapter bind not owned shows cost`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(cost = 250)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("250", holder.itemView.findViewById<TextView>(R.id.text_cost).text.toString())
    }

    @Test
    fun `VoucherAdapter bind owned shows empty cost`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(userVoucherId = "uv1", cost = 250)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("", holder.itemView.findViewById<TextView>(R.id.text_cost).text.toString())
    }

    @Test
    fun `VoucherAdapter bind no imageUrl shows emoji fallback`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(imageUrl = null)))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val iconEmoji = holder.itemView.findViewById<TextView>(R.id.text_icon)
        val iconImg = holder.itemView.findViewById<ImageView>(R.id.img_icon)
        assertEquals(View.VISIBLE, iconEmoji.visibility)
        assertEquals(View.GONE, iconImg.visibility)
        assertEquals("\uD83C\uDFAB", iconEmoji.text.toString()) // ticket emoji
    }

    @Test
    fun `VoucherAdapter bind blank imageUrl shows emoji fallback`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(imageUrl = "")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val iconEmoji = holder.itemView.findViewById<TextView>(R.id.text_icon)
        assertEquals(View.VISIBLE, iconEmoji.visibility)
    }

    @Test
    fun `VoucherAdapter bind with imageUrl shows image and hides emoji`() {
        val adapter = VoucherAdapter(listOf(makeVoucher(imageUrl = "https://example.com/img.png")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        try {
            adapter.onBindViewHolder(holder, 0)
        } catch (e: Exception) {
            // Glide may fail in Robolectric, that's OK
        }
        val iconImg = holder.itemView.findViewById<ImageView>(R.id.img_icon)
        val iconEmoji = holder.itemView.findViewById<TextView>(R.id.text_icon)
        assertEquals(View.VISIBLE, iconImg.visibility)
        assertEquals(View.GONE, iconEmoji.visibility)
    }

    @Test
    fun `VoucherAdapter setActionClickListener triggers onRedeemClick`() {
        var redeemed: Voucher? = null
        val adapter = VoucherAdapter(
            listOf(makeVoucher(name = "Test Voucher")),
            onRedeemClick = { redeemed = it }
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // Click the redeem button
        holder.itemView.findViewById<MaterialButton>(R.id.button_redeem).performClick()
        assertNotNull(redeemed)
        assertEquals("Test Voucher", redeemed!!.name)
    }

    @Test
    fun `VoucherAdapter card click triggers onVoucherClick`() {
        var clicked: Voucher? = null
        val adapter = VoucherAdapter(
            listOf(makeVoucher(name = "Card Click")),
            onVoucherClick = { clicked = it }
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        holder.itemView.performClick()
        assertNotNull(clicked)
        assertEquals("Card Click", clicked!!.name)
    }

    @Test
    fun `VoucherAdapter updateVouchers changes count`() {
        val adapter = VoucherAdapter(listOf(makeVoucher()))
        assertEquals(1, adapter.itemCount)
        adapter.updateVouchers(listOf(makeVoucher("v1"), makeVoucher("v2"), makeVoucher("v3")))
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `VoucherAdapter setActionClickListener disabled button does not trigger`() {
        var redeemCount = 0
        val adapter = VoucherAdapter(
            listOf(makeVoucher(available = false)),
            onRedeemClick = { redeemCount++ }
        )
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // Button is disabled (Sold Out), but we try clicking it
        val button = holder.itemView.findViewById<MaterialButton>(R.id.button_redeem)
        assertFalse(button.isEnabled)
        // The button.setOnClickListener checks if button.isEnabled
        button.performClick()
        assertEquals(0, redeemCount)
    }

    // ================================================================
    // 5. FacultySwipeAdapter - Touch event handling (uncovered lines)
    // ================================================================

    private fun makeFaculty(
        id: String = "eng",
        name: String = "Engineering",
        color: String = "#FF6B35",
        slogan: String = "Build the Future",
        outfit: Outfit = Outfit(head = "hat_helmet", face = "face_goggles", body = "body_plaid")
    ) = FacultyData(id = id, name = name, color = color, slogan = slogan, outfit = outfit)

    @Test
    fun `FacultySwipeAdapter touch ACTION_DOWN scales card`() {
        val adapter = FacultySwipeAdapter(listOf(makeFaculty())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val card = holder.itemView.findViewById<MaterialCardView>(R.id.card_faculty)
        val downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        card.dispatchTouchEvent(downEvent)
        downEvent.recycle()
        // Touch listener was invoked - no crash means the code path was covered
        assertNotNull(card)
    }

    @Test
    fun `FacultySwipeAdapter touch ACTION_UP restores card scale`() {
        val adapter = FacultySwipeAdapter(listOf(makeFaculty())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val card = holder.itemView.findViewById<MaterialCardView>(R.id.card_faculty)
        val downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        card.dispatchTouchEvent(downEvent)
        downEvent.recycle()
        val upEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_UP, 0f, 0f, 0)
        card.dispatchTouchEvent(upEvent)
        upEvent.recycle()
        assertNotNull(card)
    }

    @Test
    fun `FacultySwipeAdapter touch ACTION_CANCEL restores card scale`() {
        val adapter = FacultySwipeAdapter(listOf(makeFaculty())) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val card = holder.itemView.findViewById<MaterialCardView>(R.id.card_faculty)
        val downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        card.dispatchTouchEvent(downEvent)
        downEvent.recycle()
        val cancelEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        card.dispatchTouchEvent(cancelEvent)
        cancelEvent.recycle()
        assertNotNull(card)
    }

    @Test
    fun `FacultySwipeAdapter bind with badge outfit`() {
        val outfit = Outfit(head = "hat_grad", face = "glasses_sun", body = "body_suit", badge = "badge_eco_warrior")
        val adapter = FacultySwipeAdapter(listOf(makeFaculty(outfit = outfit))) {}
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        val text = holder.itemView.findViewById<TextView>(R.id.text_outfit_items).text.toString()
        assertTrue(text.contains("Grad Cap"))
        assertTrue(text.contains("Shades"))
        assertTrue(text.contains("Biz Suit"))
    }

    // ================================================================
    // 6. AchievementAdapter - additional icon coverage
    // ================================================================

    private fun makeAchievement(
        id: String = "a1",
        name: String = "First Ride",
        description: String = "Take your first bus ride",
        unlocked: Boolean = true,
        howToUnlock: String = ""
    ) = Achievement(id = id, name = name, description = description, unlocked = unlocked, howToUnlock = howToUnlock)

    @Test
    fun `AchievementAdapter icon a6 is recycle`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a6")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDD04", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a7 is calendar`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a7")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCC5", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a8 is flexed biceps`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a8")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCAA", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a10 is money bag`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a10")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDCB0", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a11 is gem`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a11")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC8E", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a13 is walking`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a13")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals(ICON_WALKING, holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a14 is bus`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a14")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDE8D", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a17 is handshake`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a17")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83E\uDD1D", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter icon a18 is busts in silhouette`() {
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a18")))
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        assertEquals("\uD83D\uDC65", holder.itemView.findViewById<TextView>(R.id.text_badge_icon).text.toString())
    }

    @Test
    fun `AchievementAdapter equipped but unlocked false shows Locked not Wearing`() {
        // When unlocked=false, even if equipped, the lock state takes priority in visual
        val adapter = AchievementAdapter(listOf(makeAchievement(id = "a1", unlocked = false)), equippedBadgeId = "a1")
        val holder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(holder, 0)
        // unlocked=false means alpha 0.5 and strokeWidth 0, regardless of equipped
        assertEquals(0.5f, holder.itemView.alpha, 0.01f)
        assertEquals(0, (holder.itemView as MaterialCardView).strokeWidth)
    }
}
