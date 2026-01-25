/**
 * 用户管理服务
 * 调用后端 REST API
 */
import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const userService = {
    /**
     * 获取所有用户
     * Note: User management is coming soon, returning empty array for now
     */
    async getUsers() {
        // User management feature is coming soon
        // Return empty array to prevent Dashboard from crashing
        return [];
    },

    /**
     * 根据 ID 获取用户
     */
    async getUserById(id) {
        const response = await httpService.get(`${API_CONFIG.ENDPOINTS.USERS}/${id}`);
        return response.data;
    },

    /**
     * 创建用户
     */
    async createUser(userData) {
        const response = await httpService.post(API_CONFIG.ENDPOINTS.USERS, userData);
        return response.data;
    },

    /**
     * 更新用户
     */
    async updateUser(id, userData) {
        const response = await httpService.put(`${API_CONFIG.ENDPOINTS.USERS}/${id}`, userData);
        return response.data;
    },

    /**
     * 删除用户
     */
    async deleteUser(id) {
        const response = await httpService.delete(`${API_CONFIG.ENDPOINTS.USERS}/${id}`);
        return response;
    },

    /**
     * 用户登录
     */
    async login(username, password) {
        const response = await httpService.post(API_CONFIG.ENDPOINTS.AUTH.LOGIN, {
            username,
            password
        });
        return response.data;
    },

    /**
     * 用户注册
     */
    async register(username, password, email) {
        const response = await httpService.post(API_CONFIG.ENDPOINTS.AUTH.REGISTER, {
            username,
            password,
            email
        });
        return response.data;
    }
};

export default userService;
