/**
 * Carbon Credits Page
 * Displays carbon statistics, charts, and transaction records
 */
import Layout from '../components/Layout.js';
import statisticsService from '../services/statisticsService.js';

const CarbonCreditsPage = {
    render: async () => {
        const root = document.getElementById('root');
        root.innerHTML = Layout.render(`<div class="loading"></div>`, 'carbon');

        try {
            const [dashboardStats, carbonReduction, activeUsers, redemptionVolume, heatmapData] = await Promise.all([
                statisticsService.getDashboardStats().catch(() => null),
                statisticsService.getTotalCarbonReduction().catch(() => 0),
                statisticsService.getActiveUserCount(30).catch(() => 0),
                statisticsService.getRedemptionVolume().catch(() => 0),
                statisticsService.getEmissionHeatmap().catch(() => null)
            ]);

            const content = `
                <div class="page-header">
                    <div class="page-title">
                        <h1>Carbon Credits</h1>
                        <p>Track and analyze carbon credit activities</p>
                    </div>
                    <div class="header-actions">
                        <select class="form-select" id="period-select" style="width: 150px;">
                            <option value="7">Last 7 days</option>
                            <option value="30" selected>Last 30 days</option>
                            <option value="90">Last 90 days</option>
                        </select>
                    </div>
                </div>

                <!-- Stats Cards -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon green">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M11 20A7 7 0 019.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/>
                                <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/>
                            </svg>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">${(carbonReduction || 0).toLocaleString()}</div>
                            <div class="stat-label">Total Carbon Reduction</div>
                            <div class="stat-change positive">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px;">
                                    <path d="M18 15l-6-6-6 6"/>
                                </svg>
                                +12.5% from last month
                            </div>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon blue">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
                                <circle cx="9" cy="7" r="4"/>
                                <path d="M23 21v-2a4 4 0 00-3-3.87"/>
                                <path d="M16 3.13a4 4 0 010 7.75"/>
                            </svg>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">${(activeUsers || 0).toLocaleString()}</div>
                            <div class="stat-label">Active Users (30 days)</div>
                            <div class="stat-change positive">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px;">
                                    <path d="M18 15l-6-6-6 6"/>
                                </svg>
                                +8.3% from last month
                            </div>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon orange">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M12 2v20M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>
                            </svg>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">${(redemptionVolume || 0).toLocaleString()}</div>
                            <div class="stat-label">Credits Redeemed</div>
                            <div class="stat-change positive">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 16px; height: 16px;">
                                    <path d="M18 15l-6-6-6 6"/>
                                </svg>
                                +15.2% from last month
                            </div>
                        </div>
                    </div>

                    <div class="stat-card">
                        <div class="stat-icon purple">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                                <line x1="16" y1="2" x2="16" y2="6"/>
                                <line x1="8" y1="2" x2="8" y2="6"/>
                                <line x1="3" y1="10" x2="21" y2="10"/>
                            </svg>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">${dashboardStats?.ongoingActivities || 0}</div>
                            <div class="stat-label">Ongoing Activities</div>
                            <div class="stat-change neutral">Active campaigns</div>
                        </div>
                    </div>
                </div>

                <!-- Charts Row -->
                <div class="charts-row">
                    <!-- Carbon Trend Chart -->
                    <div class="card chart-card">
                        <div class="card-header">
                            <h3>Carbon Credits Trend</h3>
                        </div>
                        <div class="card-body">
                            <div class="chart-container" id="trend-chart">
                                ${renderTrendChart(dashboardStats)}
                            </div>
                        </div>
                    </div>

                    <!-- Activity Distribution -->
                    <div class="card chart-card">
                        <div class="card-header">
                            <h3>Credit Sources</h3>
                        </div>
                        <div class="card-body">
                            <div class="distribution-chart">
                                ${renderDistributionChart()}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Heatmap Section -->
                <div class="card">
                    <div class="card-header">
                        <h3>Carbon Activity Heatmap</h3>
                        <span class="badge">Last 12 weeks</span>
                    </div>
                    <div class="card-body">
                        <div class="heatmap-container">
                            ${renderHeatmap(heatmapData)}
                        </div>
                        <div class="heatmap-legend">
                            <span>Less</span>
                            <div class="legend-scale">
                                <div class="legend-item" style="background: var(--primary-light);"></div>
                                <div class="legend-item" style="background: #86EFAC;"></div>
                                <div class="legend-item" style="background: #22C55E;"></div>
                                <div class="legend-item" style="background: #16A34A;"></div>
                                <div class="legend-item" style="background: #15803D;"></div>
                            </div>
                            <span>More</span>
                        </div>
                    </div>
                </div>

                <!-- Recent Transactions -->
                <div class="card">
                    <div class="card-header">
                        <h3>Recent Transactions</h3>
                        <a href="#" class="btn btn-sm btn-ghost">View All</a>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <table>
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Type</th>
                                    <th>Source</th>
                                    <th>Description</th>
                                    <th style="text-align: right;">Credits</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody id="transactions-table">
                                ${renderSampleTransactions()}
                            </tbody>
                        </table>
                    </div>
                </div>

                <style>
                    .stats-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
                        gap: 20px;
                        margin-bottom: 24px;
                    }
                    .stat-card {
                        background: var(--card);
                        border: 1px solid var(--border);
                        border-radius: 12px;
                        padding: 20px;
                        display: flex;
                        gap: 16px;
                    }
                    .stat-icon {
                        width: 48px;
                        height: 48px;
                        border-radius: 12px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        flex-shrink: 0;
                    }
                    .stat-icon svg {
                        width: 24px;
                        height: 24px;
                    }
                    .stat-icon.green {
                        background: #ECFDF5;
                        color: var(--primary);
                    }
                    .stat-icon.blue {
                        background: #DBEAFE;
                        color: #2563EB;
                    }
                    .stat-icon.orange {
                        background: #FEF3C7;
                        color: #F59E0B;
                    }
                    .stat-icon.purple {
                        background: #EDE9FE;
                        color: #7C3AED;
                    }
                    .stat-content {
                        flex: 1;
                    }
                    .stat-value {
                        font-size: 28px;
                        font-weight: 700;
                        color: var(--text);
                        line-height: 1.2;
                    }
                    .stat-label {
                        font-size: 14px;
                        color: var(--text-muted);
                        margin: 4px 0;
                    }
                    .stat-change {
                        font-size: 12px;
                        display: flex;
                        align-items: center;
                        gap: 4px;
                    }
                    .stat-change.positive {
                        color: var(--primary);
                    }
                    .stat-change.negative {
                        color: var(--danger);
                    }
                    .stat-change.neutral {
                        color: var(--text-muted);
                    }
                    .charts-row {
                        display: grid;
                        grid-template-columns: 2fr 1fr;
                        gap: 20px;
                        margin-bottom: 24px;
                    }
                    @media (max-width: 1024px) {
                        .charts-row {
                            grid-template-columns: 1fr;
                        }
                    }
                    .chart-card {
                        min-height: 320px;
                    }
                    .card-header {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 16px 24px;
                        border-bottom: 1px solid var(--border);
                    }
                    .card-header h3 {
                        margin: 0;
                        font-size: 16px;
                    }
                    .chart-container {
                        height: 240px;
                        display: flex;
                        align-items: flex-end;
                        gap: 8px;
                        padding: 20px 0;
                    }
                    .chart-bar {
                        flex: 1;
                        background: linear-gradient(180deg, var(--primary) 0%, #22C55E 100%);
                        border-radius: 4px 4px 0 0;
                        min-height: 20px;
                        transition: all 0.3s;
                        cursor: pointer;
                        position: relative;
                    }
                    .chart-bar:hover {
                        opacity: 0.8;
                    }
                    .chart-bar::after {
                        content: attr(data-label);
                        position: absolute;
                        bottom: -24px;
                        left: 50%;
                        transform: translateX(-50%);
                        font-size: 11px;
                        color: var(--text-muted);
                        white-space: nowrap;
                    }
                    .distribution-chart {
                        padding: 20px 0;
                    }
                    .dist-item {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                    }
                    .dist-color {
                        width: 12px;
                        height: 12px;
                        border-radius: 3px;
                    }
                    .dist-label {
                        flex: 1;
                        font-size: 14px;
                    }
                    .dist-value {
                        font-weight: 600;
                        font-size: 14px;
                    }
                    .dist-bar-container {
                        width: 100%;
                        height: 8px;
                        background: var(--background);
                        border-radius: 4px;
                        margin-top: 4px;
                        overflow: hidden;
                    }
                    .dist-bar {
                        height: 100%;
                        border-radius: 4px;
                        transition: width 0.5s ease;
                    }
                    .heatmap-container {
                        display: grid;
                        grid-template-columns: repeat(12, 1fr);
                        gap: 4px;
                        padding: 16px 0;
                    }
                    .heatmap-week {
                        display: flex;
                        flex-direction: column;
                        gap: 4px;
                    }
                    .heatmap-day {
                        width: 100%;
                        aspect-ratio: 1;
                        border-radius: 3px;
                        background: var(--primary-light);
                        cursor: pointer;
                        transition: transform 0.2s;
                    }
                    .heatmap-day:hover {
                        transform: scale(1.2);
                    }
                    .heatmap-day.level-1 { background: #DCFCE7; }
                    .heatmap-day.level-2 { background: #86EFAC; }
                    .heatmap-day.level-3 { background: #22C55E; }
                    .heatmap-day.level-4 { background: #16A34A; }
                    .heatmap-day.level-5 { background: #15803D; }
                    .heatmap-legend {
                        display: flex;
                        align-items: center;
                        justify-content: flex-end;
                        gap: 8px;
                        font-size: 12px;
                        color: var(--text-muted);
                    }
                    .legend-scale {
                        display: flex;
                        gap: 2px;
                    }
                    .legend-item {
                        width: 12px;
                        height: 12px;
                        border-radius: 2px;
                    }
                    .badge {
                        padding: 4px 12px;
                        background: var(--primary-light);
                        color: var(--primary);
                        border-radius: 20px;
                        font-size: 12px;
                        font-weight: 600;
                    }
                    .credit-earn {
                        color: var(--primary);
                    }
                    .credit-spend {
                        color: var(--danger);
                    }
                    .header-actions {
                        display: flex;
                        gap: 12px;
                    }
                </style>
            `;

            root.innerHTML = Layout.render(content, 'carbon');
        } catch (error) {
            console.error('Failed to load carbon credits page:', error);
            root.innerHTML = Layout.render(`
                <div class="page-header">
                    <div class="page-title">
                        <h1>Carbon Credits</h1>
                        <p>Track and analyze carbon credit activities</p>
                    </div>
                </div>
                <div class="card">
                    <div class="card-body">
                        <div class="empty-state">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width: 64px; height: 64px; color: var(--danger);">
                                <circle cx="12" cy="12" r="10"/>
                                <line x1="15" y1="9" x2="9" y2="15"/>
                                <line x1="9" y1="9" x2="15" y2="15"/>
                            </svg>
                            <h3>Failed to Load Data</h3>
                            <p>Please make sure the backend server is running.</p>
                            <button class="btn btn-primary" onclick="location.reload()">Retry</button>
                        </div>
                    </div>
                </div>
            `, 'carbon');
        }
    }
};

// Helper function to render trend chart
function renderTrendChart(stats) {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const currentMonth = new Date().getMonth();
    const data = [];

    // Generate sample data for last 8 months
    for (let i = 7; i >= 0; i--) {
        const monthIndex = (currentMonth - i + 12) % 12;
        data.push({
            label: months[monthIndex],
            value: Math.floor(Math.random() * 3000) + 1000
        });
    }

    const maxValue = Math.max(...data.map(d => d.value));

    return data.map(item =>
        `<div class="chart-bar" style="height: ${(item.value / maxValue) * 100}%;" data-label="${item.label}" title="${item.value.toLocaleString()} credits"></div>`
    ).join('');
}

// Helper function to render distribution chart
function renderDistributionChart() {
    const sources = [
        { name: 'Activities', value: 42, color: '#059669' },
        { name: 'Daily Check-in', value: 28, color: '#0891B2' },
        { name: 'Public Transport', value: 15, color: '#7C3AED' },
        { name: 'Recycling', value: 10, color: '#F59E0B' },
        { name: 'Other', value: 5, color: '#6B7280' }
    ];

    return sources.map(source => `
        <div class="dist-item">
            <div class="dist-color" style="background: ${source.color};"></div>
            <span class="dist-label">${source.name}</span>
            <span class="dist-value">${source.value}%</span>
        </div>
        <div class="dist-bar-container">
            <div class="dist-bar" style="width: ${source.value}%; background: ${source.color};"></div>
        </div>
    `).join('');
}

// Helper function to render heatmap
function renderHeatmap(data) {
    let html = '';
    for (let week = 0; week < 12; week++) {
        html += '<div class="heatmap-week">';
        for (let day = 0; day < 7; day++) {
            const level = Math.floor(Math.random() * 6);
            html += `<div class="heatmap-day ${level > 0 ? `level-${level}` : ''}" title="Week ${week + 1}, Day ${day + 1}"></div>`;
        }
        html += '</div>';
    }
    return html;
}

// Helper function to render sample transactions
function renderSampleTransactions() {
    const transactions = [
        { user: 'xu_xin', type: 'EARN', source: 'ACTIVITY', desc: 'Completed eco activity', credits: 100, date: '2024-01-20' },
        { user: 'liu_yang', type: 'EARN', source: 'CYCLING', desc: 'Cycling to work', credits: 50, date: '2024-01-20' },
        { user: 'wang_fang', type: 'SPEND', source: 'EXCHANGE', desc: 'Exchanged for gift card', credits: -200, date: '2024-01-19' },
        { user: 'wu_ting', type: 'EARN', source: 'PUBLIC_TRANSPORT', desc: 'Used public transport', credits: 30, date: '2024-01-19' },
        { user: 'zhang_wei', type: 'EARN', source: 'RECYCLING', desc: 'Recycled materials', credits: 45, date: '2024-01-18' },
        { user: 'chen_ming', type: 'SPEND', source: 'DONATION', desc: 'Donated to charity', credits: -100, date: '2024-01-18' }
    ];

    return transactions.map(t => `
        <tr>
            <td>
                <div style="display: flex; align-items: center; gap: 8px;">
                    <div class="avatar-sm">${t.user.charAt(0).toUpperCase()}</div>
                    <span>${t.user}</span>
                </div>
            </td>
            <td><span class="status-tag ${t.type.toLowerCase()}">${t.type}</span></td>
            <td>${t.source}</td>
            <td style="color: var(--text-muted);">${t.desc}</td>
            <td style="text-align: right;">
                <span class="${t.credits > 0 ? 'credit-earn' : 'credit-spend'}">
                    ${t.credits > 0 ? '+' : ''}${t.credits.toLocaleString()}
                </span>
            </td>
            <td style="color: var(--text-muted);">${t.date}</td>
        </tr>
    `).join('');
}

export default CarbonCreditsPage;
