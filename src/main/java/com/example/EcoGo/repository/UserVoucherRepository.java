package com.example.EcoGo.repository;

import com.example.EcoGo.model.UserVoucher;
import com.example.EcoGo.model.VoucherStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserVoucherRepository extends MongoRepository<UserVoucher, String> {

    List<UserVoucher> findByUserId(String userId);

    List<UserVoucher> findByUserIdAndStatus(String userId, VoucherStatus status);

    List<UserVoucher> findByUserIdAndStatusAndExpiresAtAfter(String userId, VoucherStatus status, LocalDateTime now);

    List<UserVoucher> findByUserIdAndStatusAndExpiresAtBefore(String userId, VoucherStatus status, LocalDateTime now);
}
