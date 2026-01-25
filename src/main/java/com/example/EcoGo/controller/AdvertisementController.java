package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.AdvertisementInterface;
import com.example.EcoGo.model.Advertisement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告管理接口控制器
 * 路径规范：/api/v1/advertisements
 */
@CrossOrigin
@RestController
@RequestMapping("/api/v1/advertisements")
public class AdvertisementController {
    private static final Logger logger = LoggerFactory.getLogger(AdvertisementController.class);

    @Autowired
    private AdvertisementInterface advertisementService;

    /**
     * 获取所有广告
     * GET /api/v1/advertisements
     */
    @GetMapping
    public ResponseMessage<List<Advertisement>> getAllAdvertisements() {
        logger.info("获取所有广告列表");
        List<Advertisement> advertisements = advertisementService.getAllAdvertisements();
        return ResponseMessage.success(advertisements);
    }

    /**
     * 根据ID获取广告
     * GET /api/v1/advertisements/{id}
     */
    @GetMapping("/{id}")
    public ResponseMessage<Advertisement> getAdvertisementById(@PathVariable String id) {
        logger.info("获取广告详情，ID：{}", id);
        Advertisement advertisement = advertisementService.getAdvertisementById(id);
        return ResponseMessage.success(advertisement);
    }

    /**
     * 创建新广告
     * POST /api/v1/advertisements
     */
    @PostMapping
    public ResponseMessage<Advertisement> createAdvertisement(@RequestBody Advertisement advertisement) {
        logger.info("创建新广告：{}", advertisement.getName());
        Advertisement created = advertisementService.createAdvertisement(advertisement);
        return ResponseMessage.success(created);
    }

    /**
     * 更新广告
     * PUT /api/v1/advertisements/{id}
     */
    @PutMapping("/{id}")
    public ResponseMessage<Advertisement> updateAdvertisement(
            @PathVariable String id,
            @RequestBody Advertisement advertisement) {
        logger.info("更新广告，ID：{}", id);
        Advertisement updated = advertisementService.updateAdvertisement(id, advertisement);
        return ResponseMessage.success(updated);
    }

    /**
     * 删除广告
     * DELETE /api/v1/advertisements/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAdvertisement(@PathVariable String id) {
        logger.info("删除广告，ID：{}", id);
        advertisementService.deleteAdvertisement(id);
        return ResponseMessage.success(null);
    }

    /**
     * 根据状态获取广告
     * GET /api/v1/advertisements/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseMessage<List<Advertisement>> getAdvertisementsByStatus(@PathVariable String status) {
        logger.info("按状态查询广告，状态：{}", status);
        List<Advertisement> advertisements = advertisementService.getAdvertisementsByStatus(status);
        return ResponseMessage.success(advertisements);
    }

    /**
     * 更新广告状态（上下架/暂停）
     * PATCH /api/v1/advertisements/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseMessage<Advertisement> updateAdvertisementStatus(
            @PathVariable String id,
            @RequestParam String status) {
        logger.info("更新广告状态，ID：{}，新状态：{}", id, status);
        Advertisement updated = advertisementService.updateAdvertisementStatus(id, status);
        return ResponseMessage.success(updated);
    }

    /**
     * 获取当前有效的广告（供Mobile端展示）
     * GET /api/v1/advertisements/active
     */
    @GetMapping("/active")
    public ResponseMessage<List<Advertisement>> getActiveAdvertisements() {
        logger.info("获取当前有效广告（Mobile端）");
        List<Advertisement> advertisements = advertisementService.getActiveAdvertisements();
        return ResponseMessage.success(advertisements);
    }
}
