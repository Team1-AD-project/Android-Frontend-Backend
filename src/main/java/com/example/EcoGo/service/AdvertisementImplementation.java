package com.example.EcoGo.service;

import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.AdvertisementInterface;
import com.example.EcoGo.model.Advertisement;
import com.example.EcoGo.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdvertisementImplementation implements AdvertisementInterface {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Override
    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    @Override
    public Advertisement getAdvertisementById(String id) {
        return advertisementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADVERTISEMENT_NOT_FOUND));
    }

    @Override
    public Advertisement createAdvertisement(Advertisement advertisement) {
        return advertisementRepository.save(advertisement);
    }

    @Override
    public Advertisement updateAdvertisement(String id, Advertisement advertisement) {
        Advertisement existingAd = advertisementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADVERTISEMENT_NOT_FOUND));

        existingAd.setName(advertisement.getName());
        existingAd.setStatus(advertisement.getStatus());
        existingAd.setStartDate(advertisement.getStartDate());
        existingAd.setEndDate(advertisement.getEndDate());

        return advertisementRepository.save(existingAd);
    }

    @Override
    public void deleteAdvertisement(String id) {
        if (!advertisementRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ADVERTISEMENT_NOT_FOUND);
        }
        advertisementRepository.deleteById(id);
    }

    @Override
    public List<Advertisement> getAdvertisementsByStatus(String status) {
        return advertisementRepository.findByStatus(status);
    }

    @Override
    public Advertisement updateAdvertisementStatus(String id, String status) {
        Advertisement existingAd = advertisementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADVERTISEMENT_NOT_FOUND));

        existingAd.setStatus(status);
        return advertisementRepository.save(existingAd);
    }

    @Override
    public List<Advertisement> getActiveAdvertisements() {
        // 获取状态为 Active 且在有效期内的广告
        return advertisementRepository.findByStatus("Active");
    }
}
