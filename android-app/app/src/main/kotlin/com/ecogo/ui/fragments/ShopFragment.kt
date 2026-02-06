package com.ecogo.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecogo.R
import com.ecogo.data.MockData
import com.ecogo.data.Product
import com.ecogo.data.RedeemResponse
import com.ecogo.databinding.FragmentShopBinding
import com.ecogo.repository.EcoGoRepository
import com.ecogo.ui.adapters.ProductAdapter
import com.google.android.material.button.MaterialButton
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {
    
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    private val repository = EcoGoRepository()
    
    private var currentPoints = 1250  // 从用户数据获取
    private var currentFilter = "all"  // "all", "voucher", "goods"
    
    private lateinit var paymentSheet: PaymentSheet
    private var currentProduct: Product? = null
    private var currentPaymentIntentId: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化Stripe PaymentSheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        
        setupUI()
        setupTabs()
        setupRecyclerView()
        loadProducts()
    }
    
    private fun setupUI() {
        binding.textPoints.text = "$currentPoints pts"
        binding.textCash.text = "$50.00 SGD"  // 模拟现金余额
    }
    
    private fun setupTabs() {
        binding.tabAll.setOnClickListener { filterProducts("all") }
        binding.tabVouchers.setOnClickListener { filterProducts("voucher") }
        binding.tabGoods.setOnClickListener { filterProducts("goods") }
    }
    
    private fun filterProducts(filter: String) {
        currentFilter = filter
        loadProducts()
    }
    
    private fun setupRecyclerView() {
        val adapter = ProductAdapter(emptyList()) { product: Product, paymentMethod: String ->
            handlePurchase(product, paymentMethod)
        }
        
        binding.recyclerProducts.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }
    
    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getShopProducts(
                type = if (currentFilter == "all") null else currentFilter
            )
            
            result.onSuccess { products: List<Product> ->
                // When API returns empty list (e.g. no vouchers in backend), use MockData so voucher/goods tabs show content
                val listToShow = if (products.isEmpty()) {
                    when (currentFilter) {
                        "voucher" -> MockData.PRODUCTS.filter { product -> product.type == "voucher" }
                        "goods" -> MockData.PRODUCTS.filter { product -> product.type == "goods" }
                        else -> MockData.PRODUCTS
                    }
                } else {
                    products
                }
                (binding.recyclerProducts.adapter as ProductAdapter).updateProducts(listToShow)
            }.onFailure { error: Throwable ->
                Toast.makeText(context, "加载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                val filteredProducts = when (currentFilter) {
                    "voucher" -> MockData.PRODUCTS.filter { product -> product.type == "voucher" }
                    "goods" -> MockData.PRODUCTS.filter { product -> product.type == "goods" }
                    else -> MockData.PRODUCTS
                }
                (binding.recyclerProducts.adapter as ProductAdapter).updateProducts(filteredProducts)
            }
        }
    }
    
    private fun handlePurchase(product: Product, paymentMethod: String) {
        when (paymentMethod) {
            "points" -> redeemWithPoints(product)
            "cash" -> buyWithCash(product)  // Phase 2实现
        }
    }
    
    private fun redeemWithPoints(product: Product) {
        if (product.pointsPrice == null) {
            Toast.makeText(context, "该商品不支持积分兑换", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (currentPoints < product.pointsPrice) {
            showInsufficientPointsDialog(product.pointsPrice)
            return
        }
        
        // 显示确认Dialog
        showRedeemConfirmDialog(product) {
            performRedeem(product)
        }
    }
    
    private fun showInsufficientPointsDialog(requiredPoints: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("积分不足")
            .setMessage("需要 $requiredPoints 积分，当前 $currentPoints 积分\n\n" +
                    "通过绿色出行、参加活动等方式获取更多积分！")
            .setPositiveButton("知道了", null)
            .show()
    }
    
    private fun showRedeemConfirmDialog(product: Product, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认兑换")
            .setMessage("确定使用 ${product.pointsPrice} 积分兑换\n${product.name}?")
            .setPositiveButton("确认") { _, _ -> onConfirm() }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performRedeem(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.redeemProduct(
                userId = "user123",
                productId = product.id,
                productType = product.type
            )
            
            result.onSuccess { response: RedeemResponse ->
                currentPoints -= (product.pointsPrice ?: 0)
                binding.textPoints.text = "$currentPoints pts"
                showSuccessDialog("成功兑换 ${product.name}!", "-${product.pointsPrice} pts")
                loadProducts()  // 刷新列表
            }.onFailure { error: Throwable ->
                Toast.makeText(context, "兑换失败: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showSuccessDialog(message: String, points: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent)
        
        val textMessage = dialog.findViewById<TextView>(R.id.text_message)
        val textPoints = dialog.findViewById<TextView>(R.id.text_points)
        val buttonOk = dialog.findViewById<MaterialButton>(R.id.button_ok)
        
        textMessage.text = message
        textPoints.text = points
        textPoints.visibility = View.VISIBLE
        
        buttonOk.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun buyWithCash(product: Product) {
        if (product.cashPrice == null) {
            Toast.makeText(context, "该商品不支持现金购买", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentProduct = product
        
        // 显示支付用途说明
        showPaymentInfoDialog(product) {
            initializePayment(product)
        }
    }
    
    private fun showPaymentInfoDialog(product: Product, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Support Green Transportation")
            .setMessage(
                "By purchasing ${product.name} for $${"%.2f".format(product.cashPrice)}, " +
                "you're supporting:\n\n" +
                "🌱 Expanding eco-friendly bus routes\n" +
                "🚴 Bicycle infrastructure maintenance\n" +
                "🌳 Campus tree planting initiatives\n" +
                "♻️ Sustainability education programs\n\n" +
                "Proceed to payment?"
            )
            .setPositiveButton("Continue") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun initializePayment(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.createPaymentIntent(
                userId = "user123",
                productId = product.id
            )
            
            result.onSuccess { paymentData: com.ecogo.api.PaymentIntentResponse ->
                // 配置Stripe
                PaymentConfiguration.init(
                    requireContext(),
                    paymentData.publishableKey
                )
                
                // 保存PaymentIntent ID
                currentPaymentIntentId = extractPaymentIntentId(paymentData.clientSecret)
                
                // 显示支付表单
                presentPaymentSheet(paymentData.clientSecret)
            }.onFailure { error: Throwable ->
                Toast.makeText(context, "初始化支付失败: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun extractPaymentIntentId(clientSecret: String): String {
        // ClientSecret格式: pi_xxxxx_secret_yyyyy
        return clientSecret.split("_secret_")[0]
    }
    
    private fun presentPaymentSheet(clientSecret: String) {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "EcoGo",
                allowsDelayedPaymentMethods = true
            )
        )
    }
    
    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // 支付成功，确认订单
                confirmPaymentWithBackend()
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(context, "Payment cancelled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(context, "Payment failed: ${result.error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun confirmPaymentWithBackend() {
        viewLifecycleOwner.lifecycleScope.launch {
            val product = currentProduct ?: return@launch
            val paymentIntentId = currentPaymentIntentId ?: return@launch
            
            val result = repository.confirmPayment(
                userId = "user123",
                productId = product.id,
                paymentIntentId = paymentIntentId
            )
            
            result.onSuccess { order: com.ecogo.api.OrderDto ->
                showPurchaseSuccessDialog(product)
                loadProducts()  // 刷新列表
            }.onFailure { error: Throwable ->
                Toast.makeText(context, "确认订单失败: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showPurchaseSuccessDialog(product: Product) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_purchase_success)
        dialog.window?.setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent)
        
        val message = dialog.findViewById<TextView>(R.id.text_message)
        val amount = dialog.findViewById<TextView>(R.id.text_amount)
        val impact = dialog.findViewById<TextView>(R.id.text_impact)
        val buttonOk = dialog.findViewById<MaterialButton>(R.id.button_ok)
        
        message.text = "Thank you for purchasing ${product.name}!"
        amount.text = "$${"%.2f".format(product.cashPrice)} SGD"
        impact.text = "Your contribution helps us expand green transportation options on campus"
        
        buttonOk.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
