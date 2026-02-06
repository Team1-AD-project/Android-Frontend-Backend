package com.ecogo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.databinding.FragmentVoucherDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

/**
 * Voucher Detail Page
 * Display voucher code, QR code, instructions, etc.
 */
class VoucherDetailFragment : Fragment() {

    private var _binding: FragmentVoucherDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: VoucherDetailFragmentArgs by navArgs()
    private var voucherId: String = ""
    private var isRedeemed = false

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
        
        voucherId = args.voucherId
        
        setupUI()
        loadVoucherDetail()
        setupAnimations()
    }
    
    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnRedeem.setOnClickListener {
            redeemVoucher()
        }
        
        binding.btnUse.setOnClickListener {
            useVoucher()
        }
    }
    
    private fun loadVoucherDetail() {
        val voucher = MockData.VOUCHERS.find { it.id == voucherId }
        
        if (voucher == null) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error")
                .setMessage("Voucher not found")
                .setPositiveButton("OK") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
            return
        }
        
        // Display voucher information
        binding.textName.text = voucher.name
        binding.textDescription.text = voucher.description
        binding.textCost.text = "${voucher.cost} Points"
        
        // Generate voucher code (mock)
        val code = generateVoucherCode()
        binding.textVoucherCode.text = code
        
        // Set expiry date (mock)
        val expiryDate = "2026/03/31"
        binding.textExpiry.text = "Valid until: $expiryDate"
        
        // Instructions
        binding.textInstructions.text = """
            Instructions:
            1. Show this code to the merchant
            2. Enjoy your discount after verification
            3. Each voucher can only be used once
            4. Expires automatically after expiry date
        """.trimIndent()
        
        // Update UI based on redemption status
        updateUIForRedeemStatus(isRedeemed)
    }
    
    private fun updateUIForRedeemStatus(redeemed: Boolean) {
        if (redeemed) {
            binding.layoutVoucherInfo.visibility = View.VISIBLE
            binding.layoutRedeemAction.visibility = View.GONE
            binding.btnUse.isEnabled = true
        } else {
            binding.layoutVoucherInfo.visibility = View.GONE
            binding.layoutRedeemAction.visibility = View.VISIBLE
            binding.btnUse.isEnabled = false
        }
    }
    
    private fun generateVoucherCode(): String {
        // Generate random voucher code
        val uuid = UUID.randomUUID().toString().take(12).uppercase()
        return uuid.chunked(4).joinToString("-")
    }
    
    private fun redeemVoucher() {
        // Show redemption confirmation dialog
        val voucher = MockData.VOUCHERS.find { it.id == voucherId }
        voucher?.let {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Redemption")
                .setMessage("Are you sure you want to redeem this voucher for ${it.cost} points?")
                .setPositiveButton("Confirm") { _, _ ->
                    performRedeem(it.cost)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun performRedeem(cost: Int) {
        // TODO: Call API to redeem
        isRedeemed = true
        updateUIForRedeemStatus(true)
        
        // Show success dialog
        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val title: TextView? = dialog.findViewById(R.id.text_title)
        val message: TextView? = dialog.findViewById(R.id.text_message)
        val button: com.google.android.material.button.MaterialButton? = dialog.findViewById(R.id.button_ok)
        
        title?.text = "Redemption Successful"
        message?.text = "Voucher added to your collection\nRemaining points: -$cost"
        button?.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun useVoucher() {
        // Use voucher
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Use")
            .setMessage("Are you sure you want to use this voucher? This action cannot be undone.")
            .setPositiveButton("Use Now") { _, _ ->
                // TODO: Call API to mark as used
                
                android.widget.Toast.makeText(
                    requireContext(),
                    "Voucher used successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupAnimations() {
        val popIn = AnimationUtils.loadAnimation(requireContext(), R.anim.pop_in)
        binding.cardVoucher.startAnimation(popIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
