package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecogo.R
import com.ecogo.api.OrderCreateRequest
import com.ecogo.api.OrderItemRequest
import com.ecogo.auth.TokenManager
import com.ecogo.databinding.FragmentVoucherDetailBinding
import com.ecogo.repository.EcoGoRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import com.ecogo.utils.DateFormatters

class VoucherDetailFragment : Fragment() {

    private var _binding: FragmentVoucherDetailBinding? = null
    private val binding get() = _binding!!

    private val args: VoucherDetailFragmentArgs by navArgs()

    private val repository = EcoGoRepository()

    // 先跑通；后续从登录态/SharedPreferences取
    private val currentUserId: String
        get() = requireNotNull(TokenManager.getUserId()) {
            "User not logged in or userId missing"
        }


    private var goodsId: String = ""
    private var userVoucherId: String? = null

    // 缓存 goods 详情，Redeem 时要用
    private var goodsNameForRedeem: String = ""
    private var goodsPointsForRedeem: Int = 0
    private var goodsActiveForRedeem: Boolean = true
    private var goodsStockForRedeem: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goodsId = args.goodsId
        userVoucherId = args.userVoucherId

        setupUI()
        setupAnimations()
        loadDetail()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnRedeem.setOnClickListener {
            confirmRedeem()
        }

        // 你明确不做核销：直接隐藏 Use
        binding.btnUse.visibility = View.GONE
    }

    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardVoucher.startAnimation(popIn)
    }

    private fun loadDetail() {
        // 如果你有进度条控件就显示；没有就删掉这行
        // binding.progressLoading.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (userVoucherId.isNullOrBlank()) {
                    // ================== Marketplace：拉 goods 详情 ==================
                    val g = repository.getGoodsById(goodsId).getOrThrow()

                    goodsNameForRedeem = g.name
                    goodsPointsForRedeem = g.redemptionPoints
                    goodsActiveForRedeem = g.isActive
                    goodsStockForRedeem = g.stock ?: 0

                    binding.textName.text = g.name
                    binding.textDescription.text = g.description ?: ""
                    binding.textCost.text = "${g.redemptionPoints} Points"
                    binding.textVoucherCode.text = ""        // Marketplace 不显示 code
                    binding.textExpiry.text = ""             // goods 没 expiresAt 就留空（需要再扩展再加）

                    // UI：显示 Redeem，隐藏券信息
                    binding.layoutRedeemAction.visibility = View.VISIBLE
                    binding.layoutVoucherInfo.visibility = View.GONE

                    binding.btnRedeem.visibility = View.VISIBLE
                    binding.btnRedeem.text = "Redeem"
                    binding.btnRedeem.isEnabled = g.isActive && g.stock > 0
                    binding.btnUse.visibility = View.GONE

                    binding.textInstructions.text =
                        "Instructions:\n" +
                                "1. Tap Redeem Now to exchange points for this voucher.\n" +
                                "2. The voucher code will appear under My Coupons after redemption.\n" +
                                "3. Present the code to the merchant when using the voucher.\n" +
                                "4. Each voucher can be used once and expires automatically after the expiry date."


                } else {
                    // ================== Owned：拉 userVoucher 详情 ==================
                    val uv = repository.getUserVoucherDetail(userVoucherId!!).getOrThrow()

                    binding.textName.text = uv.voucherName
                    binding.textDescription.text = "" // userVoucher 没 description，先不显示
                    binding.textCost.text = ""        // owned 不展示点数
                    binding.textVoucherCode.text = uv.code ?: "N/A"
                    val nice = DateFormatters.formatExpiry(uv.expiresAt)
                    binding.textExpiry.text = if (nice.isNotBlank()) "Valid until: $nice" else ""

                    // UI：显示券信息，隐藏 Redeem
                    binding.layoutVoucherInfo.visibility = View.VISIBLE
                    binding.layoutRedeemAction.visibility = View.GONE


                    binding.btnRedeem.visibility = View.VISIBLE
                    binding.btnRedeem.text = when (uv.status) {
                        "ACTIVE" -> "View Code"
                        "USED" -> "Used"
                        "EXPIRED" -> "Expired"
                        else -> "View Code"
                    }

                    // ACTIVE 可以点（但你不做核销，所以点了也只是提示/返回列表）
                    // USED / EXPIRED 禁用
                    binding.btnRedeem.isEnabled = (uv.status == "ACTIVE")

                    binding.btnUse.visibility = View.GONE

                    binding.textInstructions.text =
                        "Instructions:\n" +
                                "1. Present this code to the merchant for verification.\n" +
                                "2. This voucher is valid until the expiry date shown above.\n" +
                                "3. This voucher is non-transferable and cannot be exchanged for cash."

                }
            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(e.message ?: "Failed to load voucher detail.")
                    .setPositiveButton("OK") { _, _ -> findNavController().navigateUp() }
                    .show()
            } finally {
                // binding.progressLoading.visibility = View.GONE
            }
        }
    }

    private fun confirmRedeem() {
        if (!userVoucherId.isNullOrBlank()) {
            // owned 不允许 redeem
            return
        }

        // 安全兜底：如果 goods 还没加载到就不让点
        if (goodsNameForRedeem.isBlank()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Please wait")
                .setMessage("Loading voucher details...")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // 基础校验
        if (!goodsActiveForRedeem || goodsStockForRedeem <= 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Not available")
                .setMessage("This voucher is currently unavailable.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Redemption")
            .setMessage("Redeem \"$goodsNameForRedeem\" for $goodsPointsForRedeem points?")
            .setPositiveButton("Confirm") { _, _ ->
                performRedeem()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performRedeem() {
        // 组装 redemption order（按你后端：orders/redemption）
        val order = OrderCreateRequest(
            userId = currentUserId,
            items = listOf(
                OrderItemRequest(
                    goodsId = goodsId,
                    goodsName = goodsNameForRedeem,
                    quantity = 1,
                    price = 0.0,
                    subtotal = 0.0
                )
            )
        )

        // 可选：按钮禁用避免连点
        binding.btnRedeem.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.createRedemptionOrder(order).getOrThrow()

                // 告诉上一页刷新 My Coupons
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("voucher_redeemed", true)

                // 成功提示（你也可以用你原来的 success dialog）
                val dialog = android.app.Dialog(requireContext())
                dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.dialog_success)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val title: TextView? = dialog.findViewById(R.id.text_title)
                val message: TextView? = dialog.findViewById(R.id.text_message)
                val button: com.google.android.material.button.MaterialButton? = dialog.findViewById(R.id.button_ok)

                title?.text = "Redemption Successful"
                message?.text = "Voucher added to your collection"
                button?.setOnClickListener {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }

                dialog.show()

            } catch (e: Exception) {
                binding.btnRedeem.isEnabled = true
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Redeem failed")
                    .setMessage(e.message ?: "Failed to redeem voucher.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
