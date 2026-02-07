package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.CarbonRecord;

import java.util.List;

public interface CarbonRecordInterface {
    List<CarbonRecord> getAllRecords();
    List<CarbonRecord> getRecordsByUserId(String userId);
    CarbonRecord createRecord(CarbonRecord record);

    // 业务方法
    CarbonRecord earnCredits(String userId, Integer credits, String source, String description);
    CarbonRecord spendCredits(String userId, Integer credits, String source, String description);
    Integer getTotalCreditsByUserId(String userId);
}
