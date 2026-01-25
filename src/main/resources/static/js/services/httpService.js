/**
 * HTTP 请求服务
 * 封装 fetch API，统一处理请求和响应
 */
import API_CONFIG from '../config/api.js';

const httpService = {
    /**
     * GET 请求
     */
    async get(endpoint) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            return await response.json();
        } catch (error) {
            console.error('GET 请求失败:', error);
            throw error;
        }
    },

    /**
     * POST 请求
     */
    async post(endpoint, data) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('POST 请求失败:', error);
            throw error;
        }
    },

    /**
     * PUT 请求
     */
    async put(endpoint, data) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('PUT 请求失败:', error);
            throw error;
        }
    },

    /**
     * DELETE 请求
     */
    async delete(endpoint) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            return await response.json();
        } catch (error) {
            console.error('DELETE 请求失败:', error);
            throw error;
        }
    }
};

export default httpService;
