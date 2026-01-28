package com.example.EcoGo.exception.errorcode;

/**
 * 错误码枚举类：集中管理所有错误码和提示信息
 * 编码规则：
 * - 4xxx：客户端错误（如参数错、权限错）
 * - 5xxx：服务端错误（如数据库错、系统错）
 * - 按模块细分：40xx=通用客户端错，41xx=用户模块错，42xx=订单模块错...
 */
public enum ErrorCode {
    // 通用客户端错误（40xx）
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(4001, "参数错误：%s"), // 带占位符，动态填充具体参数
    NOT_LOGIN(4002, "请先登录"),
    NO_PERMISSION(4003, "无权限访问"),

    // 用户模块错误（41xx）
    USER_NOT_FOUND(4101, "用户不存在"),
    USER_NAME_DUPLICATE(4102, "用户名已被占用"),
    PASSWORD_ERROR(4103, "密码错误"),

    // 订单模块错误（42xx）
    ORDER_NOT_FOUND(4201, "订单不存在"),
    ORDER_STATUS_ERROR(4202, "订单状态错误，当前状态：%s"),

    // Product module error (43xx)
    PRODUCT_NOT_EXIST(4301, "The product does not exist,productId:%s"),
    PRODUCT_LIST_EMPTY(4302, "There are currently no products available"),
    PARAM_CANNOT_BE_NULL(4303, "Request parameter cannot be empty:%s"),
    PRODUCT_NAME_DUPLICATE(4304, "Product name already exists:%s"),

    // Activity module error (44xx)
    ACTIVITY_NOT_FOUND(4401, "活动不存在"),
    ACTIVITY_FULL(4402, "活动人数已满"),
    ALREADY_JOINED(4403, "您已参加该活动"),

    // Advertisement module error (45xx)
    ADVERTISEMENT_NOT_FOUND(4501, "广告不存在"),

    // 服务端错误（5xxx）
    DB_ERROR(5001, "数据库操作失败"),
    SYSTEM_ERROR(5002, "系统内部错误，请稍后重试"),
    REDIS_ERROR(5003, "缓存服务异常");

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