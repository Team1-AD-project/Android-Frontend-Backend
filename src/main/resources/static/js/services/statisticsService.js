/**
 * Statistics Service
 * Handles API calls for statistics and dashboard data
 */
import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const statisticsService = {
    /**
     * Get comprehensive dashboard statistics
     */
    async getDashboardStats() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.STATISTICS.DASHBOARD);
            return response.data || response;
        } catch (error) {
            console.error('Failed to fetch dashboard stats:', error);
            throw error;
        }
    },

    /**
     * Get total carbon reduction
     */
    async getTotalCarbonReduction() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.STATISTICS.CARBON_REDUCTION);
            return response.data || response;
        } catch (error) {
            console.error('Failed to fetch carbon reduction:', error);
            throw error;
        }
    },

    /**
     * Get active user count
     * @param {number} days - Number of days to look back (default: 30)
     */
    async getActiveUserCount(days = 30) {
        try {
            const response = await httpService.get(`${API_CONFIG.ENDPOINTS.STATISTICS.ACTIVE_USERS}?days=${days}`);
            return response.data || response;
        } catch (error) {
            console.error('Failed to fetch active users:', error);
            throw error;
        }
    },

    /**
     * Get redemption volume
     */
    async getRedemptionVolume() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.STATISTICS.REDEMPTION_VOLUME);
            return response.data || response;
        } catch (error) {
            console.error('Failed to fetch redemption volume:', error);
            throw error;
        }
    },

    /**
     * Get emission heatmap data
     */
    async getEmissionHeatmap() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.STATISTICS.EMISSION_HEATMAP);
            return response.data || response;
        } catch (error) {
            console.error('Failed to fetch heatmap data:', error);
            throw error;
        }
    }
};

export default statisticsService;
