package com.example.EcoGo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ChatRequestDto;
import com.example.EcoGo.dto.ChatResponseDto;
import com.example.EcoGo.dto.ResponseMessage;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @PostMapping("/send")
    public ResponseMessage<ChatResponseDto> send(@RequestBody ChatRequestDto request) {
        String reply = "Hi! I'm LiNUS. I can help with routes, buses, and green tips. You said: " + request.getMessage();
        return ResponseMessage.success(new ChatResponseDto(reply));
    }
}
