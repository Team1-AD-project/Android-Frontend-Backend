package com.example.EcoGo.service;

import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.CarbonRecordInterface;
// import com.example.EcoGo.interfacemethods.UserInterface; // Removed dependency
import com.example.EcoGo.model.CarbonRecord;
import com.example.EcoGo.repository.CarbonRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CarbonRecordImplementation implements CarbonRecordInterface {

    @Autowired
    private CarbonRecordRepository carbonRecordRepository;

    // @Autowired
    // private UserInterface userService; // Removed dependency

    @Override
    public List<CarbonRecord> getAllRecords() {
        return carbonRecordRepository.findAll();
    }

    @Override
    public List<CarbonRecord> getRecordsByUserId(String userId) {
        return carbonRecordRepository.findByUserId(userId);
    }

    @Override
    public CarbonRecord createRecord(CarbonRecord record) {
        return carbonRecordRepository.save(record);
    }

    @Override
    public CarbonRecord earnCredits(String userId, Integer credits, String source, String description) {
        // 创建积分记录
        CarbonRecord record = new CarbonRecord(userId, "EARN", credits, source, description);
        CarbonRecord saved = carbonRecordRepository.save(record);

        // 更新用户总积分 (Temporarily disabled)
        // userService.addCarbonCredits(userId, credits);

        return saved;
    }

    @Override
    public CarbonRecord spendCredits(String userId, Integer credits, String source, String description) {
        // 检查用户积分是否足够
        Integer totalCredits = getTotalCreditsByUserId(userId);
        if (totalCredits < credits) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 创建消耗记录
        CarbonRecord record = new CarbonRecord(userId, "SPEND", credits, source, description);
        CarbonRecord saved = carbonRecordRepository.save(record);

        // 扣减用户总积分 (Temporarily disabled)
        // userService.addCarbonCredits(userId, -credits);

        return saved;
    }

    @Override
    public Integer getTotalCreditsByUserId(String userId) {
        List<CarbonRecord> records = carbonRecordRepository.findByUserId(userId);
        int total = 0;
        for (CarbonRecord record : records) {
            if ("EARN".equals(record.getType())) {
                total += record.getCredits();
            } else if ("SPEND".equals(record.getType())) {
                total -= record.getCredits();
            }
        }
        return total;
    }
}
