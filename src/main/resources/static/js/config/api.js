/**
 * API 配置文件
 * 统一管理后端 API 地址
 * 由于前后端在同一服务器，使用相对路径
 */
const API_CONFIG = {
    BASE_URL: '/api/v1',

    // 各模块 API 路径
    ENDPOINTS: {
        // 认证
        AUTH: {
            LOGIN: '/auth/login',
            REGISTER: '/auth/register'
        },
        // 用户管理
        USERS: '/users',
        // 广告管理
        ADVERTISEMENTS: '/advertisements',
        // 排行榜管理
        LEADERBOARD_PERIODS: '/leaderboards/periods', // Get available periods
        LEADERBOARD_RANKINGS: '/leaderboards/rankings', // Get rankings for a period
        // 活动管理
        ACTIVITIES: '/activities',
        // 碳积分记录
        CARBON_RECORDS: '/carbon-records',
        // 统计数据
        STATISTICS: {
            DASHBOARD: '/statistics/dashboard',
            CARBON_REDUCTION: '/statistics/carbon-reduction',
            ACTIVE_USERS: '/statistics/active-users',
            REDEMPTION_VOLUME: '/statistics/redemption-volume',
            EMISSION_HEATMAP: '/statistics/emission-heatmap'
        },
        // 优惠券管理
        VOUCHERS: '/vouchers'
    },

    // 便捷访问方法
    get vouchers() {
        return {
            list: `${this.BASE_URL}${this.ENDPOINTS.VOUCHERS}`,
            redeem: `${this.BASE_URL}${this.ENDPOINTS.VOUCHERS}/redeem`
        };
    }
};

export default API_CONFIG;
