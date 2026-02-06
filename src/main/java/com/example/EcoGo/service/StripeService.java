package com.example.EcoGo.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class StripeService {
    
    @Value("${stripe.api.key}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
    
    /**
     * 创建PaymentIntent
     */
    public PaymentIntent createPaymentIntent(Double amount, String currency, Map<String, String> metadata) 
        throws StripeException {
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount((long) (amount * 100))  // 转换为分
            .setCurrency(currency)
            .putAllMetadata(metadata)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .build()
            )
            .build();
        
        return PaymentIntent.create(params);
    }
    
    /**
     * 获取PaymentIntent
     */
    public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
    
    /**
     * 确认支付
     */
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent;
    }
    
    /**
     * 处理Webhook事件
     */
    public Event constructEvent(String payload, String sigHeader, String webhookSecret) 
        throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
