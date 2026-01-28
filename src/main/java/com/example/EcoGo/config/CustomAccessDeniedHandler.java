package com.example.EcoGo.config;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        ResponseMessage<Void> responseMessage = new ResponseMessage<>(
                ErrorCode.NO_PERMISSION.getCode(),
                "Access Denied: You do not have permission to access this resource.",
                null);

        response.getWriter().write(objectMapper.writeValueAsString(responseMessage));
    }
}
