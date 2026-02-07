package com.example.EcoGo.exception.errorcode;

/**
 * 错误码枚举类：集中管理所有错误码和提示信息
 * 编码规则：
 * - 4xxx：客户端错误（如参数错、权限错）
 * - 5xxx：服务端错误（如数据库错、系统错）
 * - 按模块细分：40xx=通用客户端错，41xx=用户模块错，42xx=订单模块错...
 */
public enum ErrorCode {
    // Generic Client Errors (40xx)
    SUCCESS(200, "Operation successful"),
    PARAM_ERROR(4001, "Parameter error: %s"), // Dynamic placeholder
    NOT_LOGIN(4002, "Please login first"),
    NO_PERMISSION(4003, "Permission denied"),

    // User Module Errors (41xx)
    USER_NOT_FOUND(4101, "User not found"),
    USER_NAME_DUPLICATE(4102, "Username already taken"),
    PASSWORD_ERROR(4103, "Incorrect password"),

    // Order Module Errors (42xx)
    ORDER_NOT_FOUND(4201, "Order not found"),
    ORDER_STATUS_ERROR(4202, "Order status error, current status: %s"),

    // Product module error (43xx)
    PRODUCT_NOT_EXIST(4301, "The product does not exist,productId:%s"),
    PRODUCT_LIST_EMPTY(4302, "There are currently no products available"),
    PARAM_CANNOT_BE_NULL(4303, "Request parameter cannot be empty:%s"),
    PRODUCT_NAME_DUPLICATE(4304, "Product name already exists:%s"),

    // Activity module error (44xx)
    ACTIVITY_NOT_FOUND(4401, "Activity not found"),
    ACTIVITY_FULL(4402, "Activity is full"),
    ALREADY_JOINED(4403, "You have already joined this activity"),

    // Advertisement module error (45xx)
    ADVERTISEMENT_NOT_FOUND(4501, "Advertisement not found"),

    // Trip module error (46xx)
    TRIP_NOT_FOUND(4601, "Trip not found"),
    TRIP_STATUS_ERROR(4602, "Trip status error, current status: %s"),

    // Server Errors (5xxx)
    DB_ERROR(5001, "Database operation failed"),
    SYSTEM_ERROR(5002, "Internal server error, please try again later"),
    REDIS_ERROR(5003, "Cache service exception");

    // 错误码
    private final int code;
    // 错误信息（支持 %s 占位符）
    private final String message;

    // 构造方法（枚举类必须私有）
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // getter 方法
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 动态填充消息中的占位符（如 PARAM_ERROR.getMessage("手机号格式错误")）
     */
    public String getMessage(Object... args) {
        return String.format(message, args);
    }
}