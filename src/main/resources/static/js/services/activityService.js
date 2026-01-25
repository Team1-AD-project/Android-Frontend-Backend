/**
 * 活动管理服务
 * 调用后端 REST API
 */
import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const activityService = {
    /**
     * 获取所有活动
     */
    async getActivities() {
        const response = await httpService.get(API_CONFIG.ENDPOINTS.ACTIVITIES);
        return response.data || [];
    },

    /**
     * 根据 ID 获取活动
     */
    async getActivityById(id) {
        const response = await httpService.get(`${API_CONFIG.ENDPOINTS.ACTIVITIES}/${id}`);
        return response.data;
    },

    /**
     * 创建活动
     */
    async createActivity(activityData) {
        const response = await httpService.post(API_CONFIG.ENDPOINTS.ACTIVITIES, activityData);
        return response.data;
    },

    /**
     * 更新活动
     */
    async updateActivity(id, activityData) {
        const response = await httpService.put(`${API_CONFIG.ENDPOINTS.ACTIVITIES}/${id}`, activityData);
        return response.data;
    },

    /**
     * 删除活动
     */
    async deleteActivity(id) {
        const response = await httpService.delete(`${API_CONFIG.ENDPOINTS.ACTIVITIES}/${id}`);
        return response;
    },

    /**
     * 发布活动
     */
    async publishActivity(id) {
        const response = await httpService.post(`${API_CONFIG.ENDPOINTS.ACTIVITIES}/${id}/publish`);
        return response.data;
    }
};

export default activityService;
