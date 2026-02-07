package com.example.EcoGo.repository;

import com.example.EcoGo.model.CarbonRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CarbonRecordRepository extends MongoRepository<CarbonRecord, String> {
    List<CarbonRecord> findByUserId(String userId);
    List<CarbonRecord> findByUserIdAndType(String userId, String type);
    List<CarbonRecord> findBySource(String source);

    // 统计查询
    List<CarbonRecord> findByType(String type);

    // 查询兑换记录（消费类型）
    List<CarbonRecord> findBySourceAndType(String source, String type);

    // 查询时间范围内的记录
    List<CarbonRecord> findByCreatedAtAfter(LocalDateTime dateTime);
}
