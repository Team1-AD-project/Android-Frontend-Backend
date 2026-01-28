package com.example.EcoGo.exception;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Global Error Controller to handle 404/400 and other container-level errors
 * that bypass @RestControllerAdvice.
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseMessage<Void> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = "Unknown Error";

        int code = ErrorCode.SYSTEM_ERROR.getCode();

        if (statusCode != null) {
            if (statusCode == 404) {
                code = 404;
                message = "Resource Not Found";
            } else if (statusCode == 400) {
                code = ErrorCode.PARAM_ERROR.getCode();
                // Use the standard formatting: "Parameter error: %s"
                message = ErrorCode.PARAM_ERROR.getMessage("Invalid URL or parameters");
            } else if (statusCode == 500) {
                code = ErrorCode.SYSTEM_ERROR.getCode();
                message = ErrorCode.SYSTEM_ERROR.getMessage();
            } else {
                code = statusCode;
                message = "Error: " + statusCode;
            }
        }

        return new ResponseMessage<>(code, message, null);
    }
}
