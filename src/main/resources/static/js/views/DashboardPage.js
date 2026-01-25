/**
 * Dashboard Page - EcoGo Admin
 * 显示统计数据和概览信息
 * 使用 SVG 图标 (遵循 UI/UX Pro Max 指南)
 */
import Layout from '../components/Layout.js';
import userService from '../services/userService.js';
import advertisementService from '../services/advertisementService.js';
import activityService from '../services/activityService.js';

// SVG 图标
const Icons = {
    users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>`,
    megaphone: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 11l18-5v12L3 13v-2z"/><path d="M11.6 16.8a3 3 0 11-5.8-1.6"/></svg>`,
    calendar: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
    leaf: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M11 20A7 7 0 019.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/><path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/></svg>`,
    arrowUp: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5,12 12,5 19,12"/></svg>`,
    error: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`
};

const DashboardPage = {
    render: async () => {
        const root = document.getElementById('root');

        // 显示加载状态
        root.innerHTML = Layout.render(`<div class="loading"></div>`, 'dashboard');

        try {
            // 并行获取所有数据
            const [users, ads, activities] = await Promise.all([
                userService.getUsers(),
                advertisementService.getAdvertisements(),
                activityService.getActivities()
            ]);

            // 计算统计数据
            const totalUsers = users.length;
            const totalAds = ads.length;
            const totalActivities = activities.length;
            const activeAds = ads.filter(ad => ad.status === 'Active').length;
            const ongoingActivities = activities.filter(a => a.status === 'ONGOING' || a.status === 'PUBLISHED').length;
            const totalCredits = users.reduce((sum, u) => sum + (u.carbonCredits || 0), 0);

            const content = `
                <div class="page-header">
                    <div class="page-title">
                        <h1>Dashboard</h1>
                        <p>Welcome back! Here's an overview of your platform.</p>
                    </div>
                </div>

                <!-- 统计卡片 -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-info">
                            <h3>Total Users</h3>
                            <div class="value">${totalUsers}</div>
                            <div class="trend up">
                                ${Icons.arrowUp}
                                <span>Active users</span>
                            </div>
                        </div>
                        <div class="stat-icon primary">${Icons.users}</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <h3>Advertisements</h3>
                            <div class="value">${totalAds}</div>
                            <div class="trend up">
                                ${Icons.arrowUp}
                                <span>${activeAds} active</span>
                            </div>
                        </div>
                        <div class="stat-icon secondary">${Icons.megaphone}</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <h3>Activities</h3>
                            <div class="value">${totalActivities}</div>
                            <div class="trend up">
                                ${Icons.arrowUp}
                                <span>${ongoingActivities} ongoing</span>
                            </div>
                        </div>
                        <div class="stat-icon warning">${Icons.calendar}</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <h3>Carbon Credits</h3>
                            <div class="value">${totalCredits.toLocaleString()}</div>
                            <div class="trend up">
                                ${Icons.arrowUp}
                                <span>Growing steadily</span>
                            </div>
                        </div>
                        <div class="stat-icon success">${Icons.leaf}</div>
                    </div>
                </div>

                <!-- 数据表格 -->
                <div class="grid-2">
                    <!-- 最近活动 -->
                    <div class="card">
                        <div class="card-header">
                            <h2>Recent Activities</h2>
                            <a href="#/activities" class="btn btn-ghost btn-sm">View All</a>
                        </div>
                        <div class="card-body" style="padding: 0;">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Activity Name</th>
                                        <th>Status</th>
                                        <th>Rewards</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${activities.slice(0, 5).map(activity => `
                                        <tr>
                                            <td>${activity.title || 'Untitled'}</td>
                                            <td><span class="status-tag ${activity.status?.toLowerCase()}">${activity.status || 'N/A'}</span></td>
                                            <td>${activity.rewardCredits || 0} pts</td>
                                        </tr>
                                    `).join('')}
                                    ${activities.length === 0 ? '<tr><td colspan="3" style="text-align: center; padding: 40px; color: var(--text-muted);">No activities yet</td></tr>' : ''}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- 用户排行 -->
                    <div class="card">
                        <div class="card-header">
                            <h2>Top Users by Credits</h2>
                            <a href="#/users" class="btn btn-ghost btn-sm">View All</a>
                        </div>
                        <div class="card-body" style="padding: 0;">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Rank</th>
                                        <th>Username</th>
                                        <th>Carbon Credits</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${users.sort((a, b) => (b.carbonCredits || 0) - (a.carbonCredits || 0)).slice(0, 5).map((user, index) => `
                                        <tr>
                                            <td><span style="display: inline-flex; align-items: center; justify-content: center; width: 28px; height: 28px; background: ${index < 3 ? 'var(--primary)' : 'var(--border)'}; color: ${index < 3 ? '#fff' : 'var(--text-muted)'}; border-radius: 50%; font-weight: 600; font-size: 12px;">${index + 1}</span></td>
                                            <td>${user.username || 'Anonymous'}</td>
                                            <td style="font-weight: 600; color: var(--primary);">${(user.carbonCredits || 0).toLocaleString()}</td>
                                        </tr>
                                    `).join('')}
                                    ${users.length === 0 ? '<tr><td colspan="3" style="text-align: center; padding: 40px; color: var(--text-muted);">No users yet</td></tr>' : ''}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- 广告列表 -->
                <div class="card">
                    <div class="card-header">
                        <h2>Advertisement Overview</h2>
                        <a href="#/ads" class="btn btn-ghost btn-sm">View All</a>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <table>
                            <thead>
                                <tr>
                                    <th>Ad Name</th>
                                    <th>Status</th>
                                    <th>Start Date</th>
                                    <th>End Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${ads.slice(0, 5).map(ad => `
                                    <tr>
                                        <td>${ad.name || 'Untitled'}</td>
                                        <td><span class="status-tag ${ad.status?.toLowerCase()}">${ad.status || 'N/A'}</span></td>
                                        <td>${ad.startDate || '-'}</td>
                                        <td>${ad.endDate || '-'}</td>
                                    </tr>
                                `).join('')}
                                ${ads.length === 0 ? '<tr><td colspan="4" style="text-align: center; padding: 40px; color: var(--text-muted);">No advertisements yet</td></tr>' : ''}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            root.innerHTML = Layout.render(content, 'dashboard');
        } catch (error) {
            console.error('Failed to load data:', error);
            root.innerHTML = Layout.render(`
                <div class="empty-state">
                    ${Icons.error}
                    <h3>Failed to Load Data</h3>
                    <p>Please make sure the backend server is running.</p>
                    <p style="color: var(--text-light); font-size: 12px;">Error: ${error.message}</p>
                    <button class="btn btn-primary" onclick="location.reload()">Retry</button>
                </div>
            `, 'dashboard');
        }
    }
};

export default DashboardPage;
