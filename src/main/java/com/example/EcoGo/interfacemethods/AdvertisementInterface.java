package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.Advertisement;

import java.util.List;

public interface AdvertisementInterface {
    List<Advertisement> getAllAdvertisements();
    Advertisement getAdvertisementById(String id);
    Advertisement createAdvertisement(Advertisement advertisement);
    Advertisement updateAdvertisement(String id, Advertisement advertisement);
    void deleteAdvertisement(String id);
    List<Advertisement> getAdvertisementsByStatus(String status);

    // 新增：更新广告状态
    Advertisement updateAdvertisementStatus(String id, String status);

    // 新增：获取当前有效广告（Mobile端展示）
    List<Advertisement> getActiveAdvertisements();
}
