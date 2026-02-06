package com.ecogo.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
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

class ShopGoodsDetailFragment : Fragment() {

    private var _binding: FragmentVoucherDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ShopGoodsDetailFragmentArgs by navArgs()
    private val repo = EcoGoRepository()

    private val currentUserId: String
        get() = requireNotNull(TokenManager.getUserId()) { "User not logged in or userId missing" }

    private var goodsName: String = ""
    private var goodsPrice: Double = 0.0
    private var goodsStock: Int = 0
    private var goodsActive: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVoucherDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnUse.visibility = View.GONE

        // Shop 详情页不展示 voucher code 区
        binding.layoutVoucherInfo.visibility = View.GONE
        binding.layoutRedeemAction.visibility = View.VISIBLE

        binding.btnRedeem.setOnClickListener { confirmRedeem() }

        loadDetail()
    }

    private fun loadDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val g = repo.getGoodsById(args.goodsId).getOrThrow()

                goodsName = g.name
                goodsPrice = g.price ?: 0.0
                goodsStock = g.stock
                goodsActive = g.isActive

                binding.textName.text = g.name
                binding.textDescription.text = g.description ?: ""
                binding.textCost.text = "$" + String.format("%.2f", goodsPrice)

                binding.textInstructions.text =
                    "Instructions:\n" +
                            "1. Tap Redeem Now to create an order.\n" +
                            "2. Stock will be reduced if successful.\n"

                binding.btnRedeem.text = "Redeem Now"
                binding.btnRedeem.isEnabled = goodsActive && goodsStock > 0

            } catch (e: Exception) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(e.message ?: "Failed to load item detail.")
                    .setPositiveButton("OK") { _, _ -> findNavController().navigateUp() }
                    .show()
            }
        }
    }

    private fun confirmRedeem() {
        if (!goodsActive || goodsStock <= 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Not available")
                .setMessage("This item is currently unavailable.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Redemption")
            .setMessage("Redeem \"$goodsName\" for $" + String.format("%.2f", goodsPrice) + "?")
            .setPositiveButton("Confirm") { _, _ -> performRedeem() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performRedeem() {
        binding.btnRedeem.isEnabled = false

        val order = OrderCreateRequest(
            userId = currentUserId,
            items = listOf(
                OrderItemRequest(
                    goodsId = args.goodsId,
                    goodsName = goodsName,
                    quantity = 1,
                    price = goodsPrice,
                    subtotal = goodsPrice
                )
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repo.createRedemptionOrder(order).getOrThrow()
                showSuccessDialog()
            } catch (e: Exception) {
                binding.btnRedeem.isEnabled = true
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Redeem failed")
                    .setMessage(e.message ?: "Failed to redeem.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<TextView?>(R.id.text_title)?.text = "Success"
        dialog.findViewById<TextView?>(R.id.text_message)?.text = "Order created successfully"
        dialog.findViewById<com.google.android.material.button.MaterialButton?>(R.id.button_ok)
            ?.setOnClickListener {
                dialog.dismiss()
                findNavController().navigateUp()
            }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
