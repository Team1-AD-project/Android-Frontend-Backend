package com.ecogo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecogo.R
import com.ecogo.data.Product
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ProductAdapter(
    private var products: List<Product>,
    private val onPurchase: (Product, String) -> Unit  // (product, paymentMethod)
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view, onPurchase)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }
    
    override fun getItemCount() = products.size
    
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
    
    class ProductViewHolder(
        itemView: View,
        private val onPurchase: (Product, String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.text_icon)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val categoryTag: TextView = itemView.findViewById(R.id.text_category)
        private val pointsChip: Chip = itemView.findViewById(R.id.chip_points)
        private val cashChip: Chip = itemView.findViewById(R.id.chip_cash)
        private val redeemButton: MaterialButton = itemView.findViewById(R.id.button_redeem)
        private val buyButton: MaterialButton = itemView.findViewById(R.id.button_buy)
        
        fun bind(product: Product) {
            name.text = product.name
            description.text = product.description
            categoryTag.text = product.category
            
            // 图标
            icon.text = when {
                product.name.contains("Starbucks", ignoreCase = true) -> "☕"
                product.name.contains("Grab", ignoreCase = true) -> "🚗"
                product.name.contains("Foodpanda", ignoreCase = true) -> "🍕"
                product.name.contains("Bottle", ignoreCase = true) -> "🌱"
                product.name.contains("T-Shirt", ignoreCase = true) || product.name.contains("Tote", ignoreCase = true) -> "👕"
                product.name.contains("Tree", ignoreCase = true) -> "🌳"
                product.name.contains("Book", ignoreCase = true) -> "📚"
                else -> "🎁"
            }
            
            // 价格Chips
            if (product.pointsPrice != null) {
                pointsChip.text = "${product.pointsPrice} pts"
                pointsChip.visibility = View.VISIBLE
            } else {
                pointsChip.visibility = View.GONE
            }
            
            if (product.cashPrice != null) {
                cashChip.text = "$${"%.2f".format(product.cashPrice)} SGD"
                cashChip.visibility = View.VISIBLE
            } else {
                cashChip.visibility = View.GONE
            }
            
            // 支付按钮
            redeemButton.visibility = if (product.pointsPrice != null) View.VISIBLE else View.GONE
            buyButton.visibility = if (product.cashPrice != null) View.VISIBLE else View.GONE
            
            redeemButton.setOnClickListener {
                onPurchase(product, "points")
            }
            
            buyButton.setOnClickListener {
                onPurchase(product, "cash")
            }
            
            // 库存状态
            if (!product.available || (product.stock != null && product.stock <= 0)) {
                itemView.alpha = 0.5f
                redeemButton.isEnabled = false
                buyButton.isEnabled = false
            } else {
                itemView.alpha = 1.0f
                redeemButton.isEnabled = true
                buyButton.isEnabled = true
            }
        }
    }
}
