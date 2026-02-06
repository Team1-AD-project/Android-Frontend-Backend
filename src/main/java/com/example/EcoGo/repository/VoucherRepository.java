package com.example.EcoGo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.EcoGo.model.Voucher;

public interface VoucherRepository extends MongoRepository<Voucher, String> {
}
