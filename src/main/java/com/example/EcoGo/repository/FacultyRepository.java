package com.example.EcoGo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.EcoGo.model.Faculty;

public interface FacultyRepository extends MongoRepository<Faculty, String> {
}
