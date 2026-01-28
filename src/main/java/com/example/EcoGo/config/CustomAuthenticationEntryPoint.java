package com.example.EcoGo.config;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ResponseMessage<Void> responseMessage = new ResponseMessage<>(
                ErrorCode.NOT_LOGIN.getCode(),
                "Unauthorized: Please log in.",
                null);

        response.getWriter().write(objectMapper.writeValueAsString(responseMessage));
    }
}
