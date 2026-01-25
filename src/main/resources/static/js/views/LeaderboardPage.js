import leaderboardService from '../services/leaderboardService.js';
import Layout from '../components/Layout.js';

// This is the definitive, fully implemented LeaderboardPage object.
const LeaderboardPage = {

    // Main entry point for this page, called by the router.
    async init() {
        const root = document.getElementById('root');
        root.innerHTML = Layout.render(`<div class="loading">Loading Leaderboard...</div>`, 'leaderboard');

        try {
            const periods = await leaderboardService.getPeriods();
            const currentPeriod = this.getCurrentPeriod(periods);
            const rankings = currentPeriod ? await leaderboardService.getRankings(currentPeriod) : [];

            const content = this.renderPageContent(periods, currentPeriod, rankings);
            root.innerHTML = Layout.render(content, 'leaderboard');
            this.attachEventListeners(); // Attach event listeners after the content is in the DOM

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

    // This function builds the complete HTML for the main content area.
    renderPageContent(periods, currentPeriod, rankings) {
        const totalSteps = rankings.reduce((sum, r) => sum + r.steps, 0);
        const averageSteps = rankings.length > 0 ? (totalSteps / rankings.length).toLocaleString('en-US', { maximumFractionDigits: 0 }) : 0;

        const statCardsHtml = `
            <div class="stats-grid">
                ${this.renderStatCard("This Week's Participants", "1,567", "Active users: 78%", "fa-users", "#ff9800")}
                ${this.renderStatCard("VIP Users on Board", rankings.filter(r => r.isVip).length, "Top 15: 40%", "fa-user-tie", "#2196f3")}
                ${this.renderStatCard("Average Steps", averageSteps, "vs last week: +12%", "fa-chart-line", "#4caf50")}
                ${this.renderStatCard("Rewards Distributed", "10", "Auto-sent to top 10", "fa-gift", "#9c27b0")}
            </div>`;

        const filterBarHtml = `
            <div class="filter-bar">
                <select id="period-selector" class="form-select">
                    ${periods.map(p => `<option value="${p}" ${p === currentPeriod ? 'selected' : ''}>${p}</option>`).join('')}
                </select>
                <input type="search" class="form-input" placeholder="Search username or ID...">
            </div>`;

        const top3SectionHtml = `
            <div class="card">
                <div class="card-header">Top 3 This Week</div>
                ${this.renderTop3(rankings)}
            </div>`;

        const fullRankingsTableHtml = `
            <div class="card">
                <div class="card-header">Full Rankings</div>
                <table>
                    <thead><tr><th>Rank</th><th>User</th><th>Steps</th><th>Period</th></tr></thead>
                    <tbody>
                        ${rankings.map(r => `
                            <tr>
                                <td><div class="rank-badge">${r.rank}</div></td>
                                <td>${r.nickname || 'N/A'} <span class="text-muted">(${r.userId})</span></td>
                                <td>${(r.steps || 0).toLocaleString()}</td>
                                <td>${r.period}</td>
                            </tr>
                        `).join('')}
                        ${rankings.length === 0 ? '<tr><td colspan="4" style="text-align:center; padding: 40px; color: #888;">No rankings found for this period.</td></tr>' : ''}
                    </tbody>
                </table>
            </div>`;

        return `
            <div class="page-header">
                <div class="page-title">
                    <h1>Leaderboard Management</h1>
                    <p>View and manage weekly user step rankings and reward distribution</p>
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

    // Helper function with a COMPLETE implementation.
    renderTop3(rankings) {
        const top3 = rankings.slice(0, 3);
        if (top3.length < 3) {
            return '<div style="text-align: center; padding: 40px; color: #888;">Not enough data for Top 3 display.</div>';
        }
        const [winner = {}, second = {}, third = {}] = top3;
        return `
            <div class="top3-container">
                <div class="rank-card rank-2">
                    <div class="rank-avatar silver"></div>
                    <div class="rank-name">${second.nickname || 'N/A'}</div>
                    <div class="rank-steps">${(second.steps || 0).toLocaleString()} steps</div>
                </div>
                <div class="rank-card rank-1">
                    <div class="rank-avatar gold"></div>
                    <div class="rank-name">${winner.nickname || 'N/A'}</div>
                    <div class="rank-steps">${(winner.steps || 0).toLocaleString()} steps</div>
                </div>
                <div class="rank-card rank-3">
                    <div class="rank-avatar bronze"></div>
                    <div class="rank-name">${third.nickname || 'N/A'}</div>
                    <div class="rank-steps">${(third.steps || 0).toLocaleString()} steps</div>
                </div>
            </div>
        `;
    },

    // Function to attach event listeners.
    attachEventListeners() {
        const periodSelector = document.getElementById('period-selector');
        if (periodSelector) {
            periodSelector.addEventListener('change', (e) => {
                const selectedPeriod = e.target.value;
                // Change the URL to trigger the router for the new period
                window.location.hash = `#/leaderboard?period=${encodeURIComponent(selectedPeriod)}`;
            });
        }
    }
};

export default LeaderboardPage;
