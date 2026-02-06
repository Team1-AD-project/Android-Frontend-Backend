package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ProductDTO;
import com.example.EcoGo.dto.RedeemRequest;
import com.example.EcoGo.dto.PaymentIntentRequest;
import com.example.EcoGo.dto.ConfirmPaymentRequest;
import com.example.EcoGo.model.Order;
import com.example.EcoGo.service.ShopService;
import com.example.EcoGo.service.StripeService;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shop")
@CrossOrigin(origins = "*")
public class ShopController {
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private StripeService stripeService;
    
    @Value("${stripe.publishable.key}")
    private String publishableKey;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    /**
     * 1. 获取所有商品（Voucher + Goods）
     * GET /api/v1/shop/products?type=all&category=food&page=1&size=20
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getAllProducts(
        @RequestParam(required = false) String type,  // "voucher", "goods", "all"
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<ProductDTO> allProducts = shopService.getAllProducts(type, category);
            
            // 分页逻辑
            int total = allProducts.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            
            List<ProductDTO> products;
            if (fromIndex < total) {
                products = allProducts.subList(fromIndex, toIndex);
            } else {
                products = List.of();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取商品列表成功");
            response.put("data", Map.of(
                "data", products,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
                )
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取商品列表失败: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 2. 获取单个商品详情
     * GET /api/v1/shop/products/{id}
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable String id) {
        try {
            ProductDTO product = shopService.getProductById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取商品成功");
            response.put("data", product);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 404);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取商品失败: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 3. 积分兑换商品
     * POST /api/v1/shop/redeem
     */
    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeemProduct(@RequestBody RedeemRequest request) {
        try {
            // 验证请求参数
            if (request.getUserId() == null || request.getProductId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "缺少必要参数");
                errorResponse.put("data", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Order order = shopService.redeemProduct(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "兑换成功");
            response.put("data", Map.of(
                "order", order,
                "remainingPoints", null  // 需要从User获取
            ));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "兑换失败: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 4. 创建支付Intent
     * POST /api/v1/shop/payment-intent
     */
    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestBody PaymentIntentRequest request) {
        try {
            // 验证请求参数
            if (request.getUserId() == null || request.getProductId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "缺少必要参数");
                errorResponse.put("data", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 获取商品信息
            ProductDTO product = shopService.getProductById(request.getProductId());
            
            if (product.getCashPrice() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "该商品不支持现金购买");
                errorResponse.put("data", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 创建PaymentIntent
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", request.getUserId());
            metadata.put("productId", request.getProductId());
            metadata.put("productType", product.getType());
            metadata.put("productName", product.getName());
            
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                product.getCashPrice(),
                "sgd",
                metadata
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "创建支付成功");
            response.put("data", Map.of(
                "clientSecret", paymentIntent.getClientSecret(),
                "publishableKey", publishableKey
            ));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "创建支付失败: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 5. 确认支付
     * POST /api/v1/shop/confirm-payment
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody ConfirmPaymentRequest request) {
        try {
            // 验证请求参数
            if (request.getUserId() == null || request.getProductId() == null || request.getPaymentIntentId() == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "缺少必要参数");
                errorResponse.put("data", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 验证PaymentIntent状态
            PaymentIntent paymentIntent = stripeService.getPaymentIntent(request.getPaymentIntentId());
            
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("code", 400);
                errorResponse.put("message", "支付未成功，状态: " + paymentIntent.getStatus());
                errorResponse.put("data", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 创建订单
            Order order = shopService.createCashOrder(
                request.getUserId(),
                request.getProductId(),
                paymentIntent.getId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "购买成功！感谢您支持绿色出行项目");
            response.put("data", order);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 400);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("code", 500);
            errorResponse.put("message", "确认支付失败: " + e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 6. Stripe Webhook (用于接收支付状态更新)
     * POST /api/v1/shop/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            com.stripe.model.Event event = stripeService.constructEvent(payload, sigHeader, webhookSecret);
            
            // 处理不同事件类型
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    // 支付成功，可以发送通知等
                    System.out.println("Payment succeeded: " + event.getId());
                    break;
                case "payment_intent.payment_failed":
                    // 支付失败，记录日志
                    System.out.println("Payment failed: " + event.getId());
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.getType());
            }
            
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }
}
