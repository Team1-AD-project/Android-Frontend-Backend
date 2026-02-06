package com.example.EcoGo.service;

import com.example.EcoGo.dto.ProductDTO;
import com.example.EcoGo.dto.RedeemRequest;
import com.example.EcoGo.model.Goods;
import com.example.EcoGo.model.Order;
import com.example.EcoGo.model.User;
import com.example.EcoGo.model.Voucher;
import com.example.EcoGo.repository.GoodsRepository;
import com.example.EcoGo.repository.OrderRepository;
import com.example.EcoGo.repository.UserRepository;
import com.example.EcoGo.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShopService {
    
    @Autowired
    private GoodsRepository goodsRepository;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 获取所有商品（Voucher + Goods）
     */
    public List<ProductDTO> getAllProducts(String type, String category) {
        List<ProductDTO> products = new ArrayList<>();
        
        // 从Goods转换
        if (!"voucher".equals(type)) {
            List<Goods> goods = goodsRepository.findAll();
            products.addAll(convertGoodsToProducts(goods));
        }
        
        // 从Voucher转换
        if (!"goods".equals(type)) {
            List<Voucher> vouchers = voucherRepository.findAll();
            products.addAll(convertVouchersToProducts(vouchers));
        }
        
        // 分类筛选
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                .filter(p -> category.equals(p.getCategory()))
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    /**
     * 根据ID获取商品
     */
    public ProductDTO getProductById(String productId) {
        // 先尝试从Goods查找
        Goods goods = goodsRepository.findById(productId).orElse(null);
        if (goods != null) {
            return convertGoodsToProduct(goods);
        }
        
        // 再尝试从Voucher查找
        Voucher voucher = voucherRepository.findById(productId).orElse(null);
        if (voucher != null) {
            return convertVoucherToProduct(voucher);
        }
        
        throw new RuntimeException("商品未找到: " + productId);
    }
    
    /**
     * 积分兑换商品
     */
    public Order redeemProduct(RedeemRequest request) {
        // 1. 获取用户信息
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("用户未找到"));
        
        // 2. 获取商品信息
        ProductDTO product = getProductById(request.getProductId());
        
        // 3. 验证积分价格
        if (product.getPointsPrice() == null) {
            throw new RuntimeException("该商品不支持积分兑换");
        }
        
        int requiredPoints = product.getPointsPrice() * request.getQuantity();
        
        // 4. 验证用户积分是否足够
        if (user.getCurrentPoints() < requiredPoints) {
            throw new RuntimeException("积分不足。需要 " + requiredPoints + " 积分，当前 " + user.getCurrentPoints() + " 积分");
        }
        
        // 5. 验证库存
        if (product.getStock() != null && product.getStock() < request.getQuantity()) {
            throw new RuntimeException("库存不足");
        }
        
        // 6. 扣除用户积分
        user.setCurrentPoints(user.getCurrentPoints() - requiredPoints);
        userRepository.save(user);
        
        // 7. 减少库存（如果是Goods）
        if ("goods".equals(product.getType()) && product.getStock() != null) {
            Goods goods = goodsRepository.findById(product.getId()).orElse(null);
            if (goods != null) {
                goods.setStock(goods.getStock() - request.getQuantity());
                goodsRepository.save(goods);
            }
        }
        
        // 8. 创建订单
        Order order = new Order();
        order.setOrderNumber("RD" + System.currentTimeMillis());
        order.setUserId(request.getUserId());
        order.setStatus("PAID");
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("points");
        order.setIsRedemptionOrder(true);
        order.setPointsUsed(requiredPoints);
        order.setTotalAmount(0.0);
        order.setFinalAmount(0.0);
        
        // 创建订单项
        List<Order.OrderItem> items = new ArrayList<>();
        Order.OrderItem item = new Order.OrderItem();
        item.setGoodsId(product.getId());
        item.setGoodsName(product.getName());
        item.setQuantity(request.getQuantity());
        item.setPrice(0.0);
        item.setSubtotal(0.0);
        items.add(item);
        order.setItems(items);
        
        return orderRepository.save(order);
    }
    
    /**
     * 创建现金购买订单
     */
    public Order createCashOrder(String userId, String productId, String stripePaymentIntentId) {
        // 1. 获取用户信息
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户未找到"));
        
        // 2. 获取商品信息
        ProductDTO product = getProductById(productId);
        
        // 3. 验证现金价格
        if (product.getCashPrice() == null) {
            throw new RuntimeException("该商品不支持现金购买");
        }
        
        // 4. 验证库存
        if (product.getStock() != null && product.getStock() < 1) {
            throw new RuntimeException("库存不足");
        }
        
        // 5. 减少库存（如果是Goods）
        if ("goods".equals(product.getType()) && product.getStock() != null) {
            Goods goods = goodsRepository.findById(product.getId()).orElse(null);
            if (goods != null) {
                goods.setStock(goods.getStock() - 1);
                goodsRepository.save(goods);
            }
        }
        
        // 6. 创建订单
        Order order = new Order();
        order.setOrderNumber("CS" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setStatus("PAID");
        order.setPaymentStatus("PAID");
        order.setPaymentMethod("cash");
        order.setIsRedemptionOrder(false);
        order.setStripePaymentIntentId(stripePaymentIntentId);
        order.setTotalAmount(product.getCashPrice());
        order.setFinalAmount(product.getCashPrice());
        
        // 创建订单项
        List<Order.OrderItem> items = new ArrayList<>();
        Order.OrderItem item = new Order.OrderItem();
        item.setGoodsId(product.getId());
        item.setGoodsName(product.getName());
        item.setQuantity(1);
        item.setPrice(product.getCashPrice());
        item.setSubtotal(product.getCashPrice());
        items.add(item);
        order.setItems(items);
        
        return orderRepository.save(order);
    }
    
    /**
     * Goods转Product
     */
    private List<ProductDTO> convertGoodsToProducts(List<Goods> goodsList) {
        return goodsList.stream()
            .map(this::convertGoodsToProduct)
            .collect(Collectors.toList());
    }
    
    private ProductDTO convertGoodsToProduct(Goods goods) {
        ProductDTO product = new ProductDTO();
        product.setId(goods.getId());
        product.setName(goods.getName());
        product.setDescription(goods.getDescription());
        product.setType("goods");
        product.setCategory(goods.getCategory());
        product.setPointsPrice(goods.getRedemptionPoints() > 0 ? goods.getRedemptionPoints() : null);
        product.setCashPrice(goods.getCashPrice());
        product.setAvailable(goods.getIsActive());
        product.setStock(goods.getStock());
        product.setImageUrl(goods.getImageUrl());
        product.setBrand(goods.getBrand());
        return product;
    }
    
    /**
     * Voucher转Product
     */
    private List<ProductDTO> convertVouchersToProducts(List<Voucher> voucherList) {
        return voucherList.stream()
            .map(this::convertVoucherToProduct)
            .collect(Collectors.toList());
    }
    
    private ProductDTO convertVoucherToProduct(Voucher voucher) {
        ProductDTO product = new ProductDTO();
        product.setId(voucher.getId());
        product.setName(voucher.getName());
        product.setDescription(voucher.getDescription());
        product.setType("voucher");
        
        // 根据名称推断分类
        String category = "other";
        if (voucher.getName().toLowerCase().contains("food") || 
            voucher.getName().toLowerCase().contains("starbucks") ||
            voucher.getName().toLowerCase().contains("subway")) {
            category = "food";
        } else if (voucher.getName().toLowerCase().contains("grab") || 
                   voucher.getName().toLowerCase().contains("transport")) {
            category = "transport";
        }
        product.setCategory(category);
        
        product.setPointsPrice(voucher.getCost());
        product.setCashPrice(voucher.getCost() != null ? voucher.getCost() * 0.006 : null); // 1积分≈0.006 SGD
        product.setAvailable(true);
        product.setStock(null); // 无限库存
        return product;
    }
}
