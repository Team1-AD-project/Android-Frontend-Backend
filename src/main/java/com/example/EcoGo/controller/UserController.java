package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.dto.UserResponseDto;
import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.UserInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器
 * 路径规范：/api/v1/user/xxx（v1表示版本，便于后续迭代）
 */
@RestController
// @RequestMapping("/api/v1/user") // 推荐：统一提取路径前缀
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 1. 使用构造器注入代替字段注入（@Autowired），保证依赖不可变且便于单元测试
    private final UserInterface userService;

    public UserController(UserInterface userService) {
        this.userService = userService;
    }

    /**
     * 根据用户名查询用户信息
     * 请求示例：GET http://localhost:8080/api/v1/user/test
     */
    @GetMapping("/api/v1/user/{username}")
    public ResponseMessage<UserResponseDto> getUserByUsername(
            // 2. 添加 @PathVariable 明确绑定，虽然同名可省略但写上更清晰
            @PathVariable("username") String username) {

        // 3. 基础校验（虽然 PathVariable 不会为空字符串，但作为防御性编程）
        if (username == null || username.trim().isEmpty()) {
            // 抛出带有具体错误信息的业务异常
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名不能为空");
        }

        logger.info("开始查询用户信息，用户名：{}", username);
        UserResponseDto userDTO = userService.getUserByUsername(username);
        logger.debug("查询用户信息成功，用户：{}", userDTO);

        return ResponseMessage.success(userDTO);
    }
}