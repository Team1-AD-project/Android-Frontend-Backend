import { httpService } from './httpService.js';
import { API_CONFIG } from '../config/api.js';

/**
 * 优惠券服务 - 处理优惠券相关的API调用
 */
export const voucherService = {
    /**
     * 获取所有优惠券
     * @returns {Promise<Array>} 优惠券列表
     */
    async getVouchers() {
        try {
            const response = await httpService.get(API_CONFIG.vouchers.list);
            return response.data || [];
        } catch (error) {
            console.error('获取优惠券失败:', error);
            throw error;
        }
    },

    /**
     * 根据ID获取优惠券
     * @param {string} id - 优惠券ID
     * @returns {Promise<Object>} 优惠券对象
     */
    async getVoucherById(id) {
        try {
            const response = await httpService.get(`${API_CONFIG.vouchers.list}/${id}`);
            return response.data;
        } catch (error) {
            console.error(`获取优惠券 ${id} 失败:`, error);
            throw error;
        }
    },

    /**
     * 创建新优惠券
     * @param {Object} voucherData - 优惠券数据
     * @returns {Promise<Object>} 创建的优惠券
     */
    async createVoucher(voucherData) {
        try {
            const response = await httpService.post(API_CONFIG.vouchers.create || API_CONFIG.vouchers.list, voucherData);
            return response.data;
        } catch (error) {
            console.error('创建优惠券失败:', error);
            throw error;
        }
    },

    /**
     * 更新优惠券
     * @param {string} id - 优惠券ID
     * @param {Object} voucherData - 更新的数据
     * @returns {Promise<Object>} 更新后的优惠券
     */
    async updateVoucher(id, voucherData) {
        try {
            const response = await httpService.put(`${API_CONFIG.vouchers.list}/${id}`, voucherData);
            return response.data;
        } catch (error) {
            console.error(`更新优惠券 ${id} 失败:`, error);
            throw error;
        }
    },

    /**
     * 删除优惠券
     * @param {string} id - 优惠券ID
     * @returns {Promise<void>}
     */
    async deleteVoucher(id) {
        try {
            await httpService.delete(`${API_CONFIG.vouchers.list}/${id}`);
        } catch (error) {
            console.error(`删除优惠券 ${id} 失败:`, error);
            throw error;
        }
    },

    /**
     * 兑换优惠券
     * @param {string} voucherId - 优惠券ID
     * @param {string} userId - 用户ID
     * @returns {Promise<Object>} 兑换结果
     */
    async redeemVoucher(voucherId, userId) {
        try {
            const response = await httpService.post(API_CONFIG.vouchers.redeem, {
                voucherId,
                userId
            });
            return response.data;
        } catch (error) {
            console.error('兑换优惠券失败:', error);
            throw error;
        }
    },

    /**
     * 获取优惠券统计信息
     * @returns {Promise<Object>} 统计数据
     */
    async getVoucherStats() {
        try {
            const vouchers = await this.getVouchers();
            return {
                total: vouchers.length,
                available: vouchers.filter(v => v.available !== false).length,
                unavailable: vouchers.filter(v => v.available === false).length,
                totalValue: vouchers.reduce((sum, v) => sum + (v.cost || 0), 0)
            };
        } catch (error) {
            console.error('获取优惠券统计失败:', error);
            throw error;
        }
    }
};
