import leaderboardService from '../services/leaderboardService.js';
import Layout from '../components/Layout.js';

// Leaderboard page with search, pagination, export, VIP badges, and progress bars.
const RANKINGS_PER_PAGE = 15;

const LeaderboardPage = {

    state: {
        periods: [],
        currentPeriod: null,
        rankings: [],
        searchQuery: '',
        currentPage: 1
    },

    async init() {
        const root = document.getElementById('root');
        root.innerHTML = Layout.render(`<div class="loading">Loading Leaderboard...</div>`, 'leaderboard');

        try {
            const periods = await leaderboardService.getPeriods();
            const currentPeriod = this.getCurrentPeriod(periods);
            const rankings = currentPeriod ? await leaderboardService.getRankings(currentPeriod) : [];

            this.state = { periods, currentPeriod, rankings, searchQuery: '', currentPage: 1 };
            const content = this.renderPageContent();
            root.innerHTML = Layout.render(content, 'leaderboard');
            this.attachEventListeners();
        } catch (error) {
            console.error("Failed to render leaderboard:", error);
            root.innerHTML = Layout.render(`<div class="error-state">Error loading leaderboard data: ${error.message}</div>`, 'leaderboard');
        }
    },

    // Determines the current period to display from URL or defaults to the first available.
    getCurrentPeriod(periods) {
        // Look for a 'period' query parameter in the URL hash
        const urlParams = new URLSearchParams(window.location.hash.split('?')[1]);
        const periodFromUrl = urlParams.get('period');

        if (periodFromUrl && periods.includes(periodFromUrl)) {
            return periodFromUrl;
        }
        // Default to the first period in the list if none is specified in the URL
        return periods.length > 0 ? periods[0] : null;
    },

    getFilteredRankings() {
        const { rankings, searchQuery } = this.state;
        if (!searchQuery.trim()) return rankings;
        const q = searchQuery.trim().toLowerCase();
        return rankings.filter(r =>
            (r.nickname && r.nickname.toLowerCase().includes(q)) ||
            (r.userId && String(r.userId).toLowerCase().includes(q))
        );
    },

    getPaginatedRankings() {
        const filtered = this.getFilteredRankings();
        const page = this.state.currentPage;
        const start = (page - 1) * RANKINGS_PER_PAGE;
        return {
            list: filtered.slice(start, start + RANKINGS_PER_PAGE),
            total: filtered.length,
            totalPages: Math.max(1, Math.ceil(filtered.length / RANKINGS_PER_PAGE)),
            currentPage: page
        };
    },

    renderPageContent() {
        const { periods, currentPeriod, rankings } = this.state;
        const filtered = this.getFilteredRankings();
        const { list: pageRankings, total, totalPages, currentPage } = this.getPaginatedRankings();
        const totalSteps = rankings.reduce((sum, r) => sum + (r.steps || 0), 0);
        const averageSteps = rankings.length > 0 ? (totalSteps / rankings.length).toLocaleString('en-US', { maximumFractionDigits: 0 }) : 0;
        const maxSteps = rankings.length > 0 ? Math.max(...rankings.map(r => r.steps || 0), 1) : 1;
        const vipCount = rankings.filter(r => r.isVip).length;
        const top10Count = Math.min(10, rankings.length);

        const statCardsHtml = `
            <div class="stats-grid">
                ${this.renderStatCard("本周参与人数", rankings.length.toLocaleString(), `当前周期 · ${periods.length} 个周期`, "fa-users", "#ff9800")}
                ${this.renderStatCard("榜上 VIP", vipCount, `前 15 名中约 ${top10Count} 位可获奖励`, "fa-user-tie", "#2196f3")}
                ${this.renderStatCard("平均步数", averageSteps, "较上周趋势见详情", "fa-chart-line", "#4caf50")}
                ${this.renderStatCard("已发放奖励", "10", "自动发放给前 10 名", "fa-gift", "#9c27b0")}
            </div>`;

        const filterBarHtml = `
            <div class="filter-bar">
                <select id="period-selector" class="form-select period-select">
                    ${periods.map(p => `<option value="${p}" ${p === currentPeriod ? 'selected' : ''}>${p}</option>`).join('')}
                </select>
                <input type="search" id="leaderboard-search" class="form-input search-input" placeholder="搜索昵称或用户 ID…" value="${this.state.searchQuery.replace(/"/g, '&quot;')}">
                <span class="filter-bar-meta">${filtered.length} / ${rankings.length} 条</span>
            </div>`;

        const top3SectionHtml = `
            <div class="card card-podium">
                <div class="card-header">
                    <span>本周前三名</span>
                    <span class="card-header-sub">${currentPeriod || ''}</span>
                </div>
                <div class="card-body">
                    ${this.renderTop3(rankings)}
                </div>
            </div>`;

        const fullRankingsTableHtml = `
            <div class="card card-rankings">
                <div class="card-header card-header-actions">
                    <span>完整排名</span>
                    <div class="card-header-actions-right">
                        <button type="button" id="leaderboard-export-csv" class="btn btn-outline btn-sm">
                            <i class="fas fa-download"></i> 导出 CSV
                        </button>
                    </div>
                </div>
                <div class="card-body card-body-table">
                    <table class="leaderboard-table">
                        <thead><tr><th>排名</th><th>用户</th><th>步数</th><th>进度</th><th>周期</th></tr></thead>
                        <tbody>
                            ${pageRankings.map(r => this.renderRankRow(r, maxSteps)).join('')}
                            ${pageRankings.length === 0 ? `<tr><td colspan="5" class="empty-rows">${filtered.length === 0 && rankings.length > 0 ? '无匹配结果，请调整搜索条件' : '当前周期暂无排名数据'}</td></tr>` : ''}
                        </tbody>
                    </table>
                    ${totalPages > 1 ? this.renderPagination(total, totalPages, currentPage) : ''}
                </div>
            </div>`;

        return `
            <div class="page-header">
                <div class="page-title">
                    <h1>排行榜管理</h1>
                    <p>查看与管理每周用户步数排名及奖励发放</p>
                </div>
            </div>
            <div class="leaderboard-page-content">
                ${statCardsHtml}
                ${filterBarHtml}
                ${top3SectionHtml}
                ${fullRankingsTableHtml}
            </div>
        `;
    },

    renderRankRow(r, maxSteps) {
        const steps = r.steps || 0;
        const pct = maxSteps > 0 ? Math.min(100, (steps / maxSteps) * 100) : 0;
        const top10Class = r.rank >= 1 && r.rank <= 10 ? ' top-10' : '';
        const rankBadgeClass = r.rank === 1 ? 'rank-badge gold' : r.rank === 2 ? 'rank-badge silver' : r.rank === 3 ? 'rank-badge bronze' : 'rank-badge';
        return `
            <tr class="rank-row${top10Class}">
                <td><div class="${rankBadgeClass}">${r.rank}</div></td>
                <td>
                    <div class="rank-user-cell">
                        <span class="rank-name">${escapeHtml(r.nickname || 'N/A')}</span>
                        ${r.isVip ? '<span class="vip-badge" title="VIP">VIP</span>' : ''}
                        <span class="text-muted rank-user-id">${escapeHtml(r.userId || '')}</span>
                    </div>
                </td>
                <td class="rank-steps-cell">${steps.toLocaleString()}</td>
                <td><div class="steps-bar-wrap"><div class="steps-bar" style="width:${pct}%"></div><span class="steps-bar-label">${pct.toFixed(0)}%</span></div></td>
                <td>${escapeHtml(r.period || '')}</td>
            </tr>`;
    },

    renderPagination(total, totalPages, currentPage) {
        const prev = currentPage > 1 ? currentPage - 1 : 1;
        const next = currentPage < totalPages ? currentPage + 1 : totalPages;
        const start = (currentPage - 1) * RANKINGS_PER_PAGE + 1;
        const end = Math.min(currentPage * RANKINGS_PER_PAGE, total);
        const pageNumbers = this.getPageNumbers(currentPage, totalPages);
        return `
            <div class="pagination-wrap">
                <div class="pagination-info">第 ${start}–${end} 条，共 ${total} 条</div>
                <div class="pagination" data-current="${currentPage}" data-total-pages="${totalPages}">
                    <button type="button" class="btn btn-ghost btn-sm pagination-btn" data-page="${prev}" ${currentPage <= 1 ? 'disabled' : ''}><i class="fas fa-chevron-left"></i></button>
                    ${pageNumbers.map(p => p === '…' ? '<span class="pagination-ellipsis">…</span>' : `
                        <button type="button" class="btn btn-sm pagination-btn ${p === currentPage ? 'btn-primary' : 'btn-ghost'}" data-page="${p}">${p}</button>
                    `).join('')}
                    <button type="button" class="btn btn-ghost btn-sm pagination-btn" data-page="${next}" ${currentPage >= totalPages ? 'disabled' : ''}><i class="fas fa-chevron-right"></i></button>
                </div>
            </div>`;
    },

    getPageNumbers(current, totalPages) {
        if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i + 1);
        const pages = [];
        if (current <= 4) {
            for (let i = 1; i <= 5; i++) pages.push(i);
            pages.push('…', totalPages);
        } else if (current >= totalPages - 3) {
            pages.push(1, '…');
            for (let i = totalPages - 4; i <= totalPages; i++) pages.push(i);
        } else {
            pages.push(1, '…', current - 1, current, current + 1, '…', totalPages);
        }
        return pages;
    },

    // Helper function with a COMPLETE implementation.
    renderStatCard(title, value, subtitle, icon, color) {
        return `
        <div class="stat-card" style="--card-color: ${color};">
            <div class="card-icon"><i class="fas ${icon}"></i></div>
            <div class="card-content">
                <div class="card-title">${title}</div>
                <div class="card-value">${value}</div>
                <div class="card-subtitle">${subtitle}</div>
            </div>
        </div>`;
    },

    renderTop3(rankings) {
        const top3 = rankings.slice(0, 3);
        if (top3.length < 3) {
            return '<div class="top3-empty">当前数据不足，无法展示前三名</div>';
        }
        const [winner = {}, second = {}, third = {}] = top3;
        return `
            <div class="top3-container">
                <div class="rank-card rank-2">
                    <div class="rank-avatar silver"><i class="fas fa-medal"></i></div>
                    <div class="rank-name">${escapeHtml(second.nickname || 'N/A')}</div>
                    ${second.isVip ? '<span class="vip-badge">VIP</span>' : ''}
                    <div class="rank-steps">${(second.steps || 0).toLocaleString()} 步</div>
                </div>
                <div class="rank-card rank-1">
                    <div class="rank-avatar gold"><i class="fas fa-crown"></i></div>
                    <div class="rank-name">${escapeHtml(winner.nickname || 'N/A')}</div>
                    ${winner.isVip ? '<span class="vip-badge">VIP</span>' : ''}
                    <div class="rank-steps">${(winner.steps || 0).toLocaleString()} 步</div>
                </div>
                <div class="rank-card rank-3">
                    <div class="rank-avatar bronze"><i class="fas fa-medal"></i></div>
                    <div class="rank-name">${escapeHtml(third.nickname || 'N/A')}</div>
                    ${third.isVip ? '<span class="vip-badge">VIP</span>' : ''}
                    <div class="rank-steps">${(third.steps || 0).toLocaleString()} 步</div>
                </div>
            </div>
        `;
    },

    attachEventListeners() {
        const periodSelector = document.getElementById('period-selector');
        if (periodSelector) {
            periodSelector.addEventListener('change', (e) => {
                window.location.hash = `#/leaderboard?period=${encodeURIComponent(e.target.value)}`;
            });
        }
        const searchInput = document.getElementById('leaderboard-search');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.state.searchQuery = e.target.value;
                this.state.currentPage = 1;
                this.rerenderTableAndPagination();
            });
        }
        const exportBtn = document.getElementById('leaderboard-export-csv');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => this.exportCsv());
        }
        document.querySelectorAll('.pagination-btn:not([disabled])').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const page = parseInt(e.currentTarget.dataset.page, 10);
                if (page >= 1 && page <= (this.state.currentPage ? this.getPaginatedRankings().totalPages : 1)) {
                    this.state.currentPage = page;
                    this.rerenderTableAndPagination();
                }
            });
        });
    },

    rerenderTableAndPagination() {
        const root = document.getElementById('root');
        const main = root && root.querySelector('.main-content');
        if (!main) return;
        const { list, total, totalPages, currentPage } = this.getPaginatedRankings();
        const maxSteps = this.state.rankings.length > 0 ? Math.max(...this.state.rankings.map(r => r.steps || 0), 1) : 1;
        const tableCard = main.querySelector('.card-rankings');
        if (!tableCard) return;
        const tbody = tableCard.querySelector('.leaderboard-table tbody');
        const paginationWrap = tableCard.querySelector('.pagination-wrap');
        if (tbody) {
            tbody.innerHTML = list.map(r => this.renderRankRow(r, maxSteps)).join('') +
                (list.length === 0 ? `<tr><td colspan="5" class="empty-rows">无匹配结果</td></tr>` : '');
        }
        if (paginationWrap && totalPages > 1) {
            paginationWrap.outerHTML = this.renderPagination(total, totalPages, currentPage);
            tableCard.querySelectorAll('.pagination-btn:not([disabled])').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const page = parseInt(e.currentTarget.dataset.page, 10);
                    if (page >= 1) {
                        this.state.currentPage = page;
                        this.rerenderTableAndPagination();
                    }
                });
            });
        }
        const meta = main.querySelector('.filter-bar-meta');
        if (meta) meta.textContent = `${this.getFilteredRankings().length} / ${this.state.rankings.length} 条`;
    },

    exportCsv() {
        const filtered = this.getFilteredRankings();
        const headers = ['Rank', 'UserId', 'Nickname', 'Steps', 'Period', 'VIP'];
        const rows = filtered.map(r => [
            r.rank,
            r.userId || '',
            (r.nickname || '').replace(/"/g, '""'),
            r.steps || 0,
            (r.period || '').replace(/"/g, '""'),
            r.isVip ? 'Yes' : 'No'
        ]);
        const csv = [headers.join(','), ...rows.map(row => row.map(c => `"${c}"`).join(','))].join('\r\n');
        const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = `leaderboard-${(this.state.currentPeriod || 'export').replace(/[\s,]+/g, '-')}.csv`;
        a.click();
        URL.revokeObjectURL(a.href);
    }
};

function escapeHtml(str) {
    if (str == null) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

export default LeaderboardPage;
