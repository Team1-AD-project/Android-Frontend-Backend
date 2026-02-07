package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "user_vouchers")
@CompoundIndex(name = "uid_status_exp_idx", def = "{'userId': 1, 'status': 1, 'expiresAt': 1}")
public class UserVoucher {

    @Id
    private String id;

    /**
     * 对应 users.userid（注意：不是 Mongo _id）
     */
    @Indexed
    private String userId;

    /**
     * 对应 goods._id（voucher 商品在 goods 里：category=service, type=voucher）
     */
    @Indexed
    private String goodsId;

    /**
     * 冗余：方便展示（即使 goods 改名，用户历史券也稳定）
     */
    private String voucherName;

    /**
     * 冗余：方便展示（可选）
     */
    private String imageUrl;

    /**
     * 关联本次兑换产生的订单（可选，但非常建议，便于追溯）
     */
    @Indexed
    private String orderId;

    /**
     * 券的状态：ACTIVE / USED / EXPIRED
     */
    @Indexed
    private VoucherStatus status = VoucherStatus.ACTIVE;

    /**
     * 发放时间（兑换成功时间）
     */
    private LocalDateTime issuedAt;

    /**
     * 过期时间（issuedAt + 30 days，由后端逻辑写入）
     */
    @Indexed
    private LocalDateTime expiresAt;

    /**
     * 使用时间（status=USED 才有）
     */
    private LocalDateTime usedAt;

    /**
     * 兑换码（可选：如果没有真实 code，你也可以不用它）
     */
    private String code;

    /**
     * 审计字段（可选）
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserVoucher() {}

    // ---------- getters / setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGoodsId() { return goodsId; }
    public void setGoodsId(String goodsId) { this.goodsId = goodsId; }

    public String getVoucherName() { return voucherName; }
    public void setVoucherName(String voucherName) { this.voucherName = voucherName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public VoucherStatus getStatus() { return status; }
    public void setStatus(VoucherStatus status) { this.status = status; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ---------- helper methods（可选，但很实用） ----------

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && now != null && expiresAt.isBefore(now);
    }

    public void touch(LocalDateTime now) {
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }
}
