import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const leaderboardService = {

    /**
     * Fetches a list of all available periods (e.g., ["Week 4, 2026", "Week 3, 2026"])
     * from the backend via GET /api/v1/leaderboards/periods.
     */
    async getPeriods() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.LEADERBOARD_PERIODS);
            return response.data || [];
        } catch (error) {
            console.error("Failed to fetch leaderboard periods:", error);
            return []; // Return empty array on error
        }
    },

    /**
     * Fetches all rankings for a given period from the backend.
     * via GET /api/v1/leaderboards/rankings?period=...
     * @param {string} period - The period to fetch rankings for.
     */
    async getRankings(period) {
        if (!period) return [];
        try {
            const response = await httpService.get(`${API_CONFIG.ENDPOINTS.LEADERBOARD_RANKINGS}?period=${encodeURIComponent(period)}`);
            return response.data || [];
        } catch (error) {
            console.error(`Failed to fetch rankings for period ${period}:`, error);
            return []; // Return empty array on error
        }
    }
};

export default leaderboardService;
