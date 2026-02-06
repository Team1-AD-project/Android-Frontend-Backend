package com.example.EcoGo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.model.HistoryItem;
import com.example.EcoGo.repository.HistoryRepository;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {
    private final HistoryRepository historyRepository;

    public HistoryController(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public ResponseMessage<List<HistoryItem>> getHistory(@RequestParam(required = false) String userId) {
        return ResponseMessage.success(historyRepository.findAll());
    }
}
