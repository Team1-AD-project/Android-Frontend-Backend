package com.example.EcoGo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.model.Faculty;
import com.example.EcoGo.repository.FacultyRepository;

@RestController
@RequestMapping("/api/v1/faculties")
public class FacultyController {
    private final FacultyRepository facultyRepository;

    public FacultyController(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    @GetMapping
    public ResponseMessage<List<Faculty>> getFaculties() {
        return ResponseMessage.success(facultyRepository.findAll());
    }
}
