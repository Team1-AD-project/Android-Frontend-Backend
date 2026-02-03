import httpService from './httpService.js';
import API_CONFIG from '../config/api.js';

const MOCK_PERIODS = ['Week 4, 2026', 'Week 3, 2026', 'Week 2, 2026'];

function getMockRankings(period) {
    const rows = [
        { userId: 'user_001', nickname: '小明', steps: 125000, isVip: true },
        { userId: 'user_002', nickname: 'EcoRunner', steps: 98200, isVip: true },
        { userId: 'user_003', nickname: '绿行侠', steps: 87600, isVip: false },
        { userId: 'user_004', nickname: '步数达人', steps: 75400, isVip: true },
        { userId: 'user_005', nickname: '晨跑王', steps: 68100, isVip: false },
        { userId: 'user_006', nickname: '林小绿', steps: 59200, isVip: true },
        { userId: 'user_007', nickname: '低碳生活', steps: 51800, isVip: false },
        { userId: 'user_008', nickname: '天天走路', steps: 44500, isVip: false },
        { userId: 'user_009', nickname: '校园行者', steps: 38100, isVip: true },
        { userId: 'user_010', nickname: '环保先锋', steps: 32600, isVip: false },
        { userId: 'user_011', nickname: '小步快跑', steps: 27800, isVip: false },
        { userId: 'user_012', nickname: '绿色出行', steps: 22100, isVip: false },
    ];
    return rows.map((r, i) => ({
        rank: i + 1,
        period: period || MOCK_PERIODS[0],
        userId: r.userId,
        nickname: r.nickname,
        steps: r.steps,
        isVip: r.isVip,
    }));
}

const leaderboardService = {

    async getPeriods() {
        try {
            const response = await httpService.get(API_CONFIG.ENDPOINTS.LEADERBOARD_PERIODS);
            const data = response.data || [];
            if (data.length > 0) return data;
            return MOCK_PERIODS;
        } catch (error) {
            console.warn("Leaderboard periods from API failed, using mock:", error?.message);
            return MOCK_PERIODS;
        }
    },

    async getRankings(period) {
        if (!period) return getMockRankings(MOCK_PERIODS[0]);
        try {
            const response = await httpService.get(`${API_CONFIG.ENDPOINTS.LEADERBOARD_RANKINGS}?period=${encodeURIComponent(period)}`);
            const data = response.data || [];
            if (data.length > 0) return data;
            return getMockRankings(period);
        } catch (error) {
            console.warn(`Leaderboard rankings for ${period} failed, using mock:`, error?.message);
            return getMockRankings(period);
        }
    }
};

export default leaderboardService;
