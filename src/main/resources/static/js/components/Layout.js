/**
 * Layout 组件 - EcoGo Admin 主布局
 * 包含侧边栏和顶部导航
 * 使用 SVG 图标 (遵循 UI/UX Pro Max 指南)
 */

// SVG 图标定义
const Icons = {
    logo: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>`,
    dashboard: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>`,
    megaphone: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 11l18-5v12L3 13v-2z"/><path d="M11.6 16.8a3 3 0 11-5.8-1.6"/></svg>`,
    calendar: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
    trophy: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M6 9H4.5a2.5 2.5 0 010-5H6"/><path d="M18 9h1.5a2.5 2.5 0 000-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 1012 0V2z"/></svg>`,
    users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>`,
    leaf: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M11 20A7 7 0 019.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/><path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/></svg>`,
    settings: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg>`,
    bell: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>`,
    search: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`,
    logout: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16,17 21,12 16,7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>`,
    arrowUp: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="12" y1="19" x2="12" y2="5"/><polyline points="5,12 12,5 19,12"/></svg>`,
    arrowDown: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="12" y1="5" x2="12" y2="19"/><polyline points="19,12 12,19 5,12"/></svg>`
};

const Layout = {
    render: (content, activeNav = '') => {
        return `
            <div class="app-layout">
                <!-- 侧边栏 -->
                <aside class="sidebar">
                    <div class="sidebar-header">
                        <div class="logo">
                            ${Icons.leaf}
                        </div>
                        <h2>EcoGo</h2>
                    </div>
                    <nav class="sidebar-nav">
                        <div class="nav-section">Overview</div>
                        <a href="#/dashboard" class="nav-item ${activeNav === 'dashboard' ? 'active' : ''}">
                            <span class="icon">${Icons.dashboard}</span>
                            <span>Dashboard</span>
                        </a>

                        <div class="nav-section">Management</div>
                        <a href="#/ads" class="nav-item ${activeNav === 'ads' ? 'active' : ''}">
                            <span class="icon">${Icons.megaphone}</span>
                            <span>Advertisements</span>
                        </a>
                        <a href="#/activities" class="nav-item ${activeNav === 'activities' ? 'active' : ''}">
                            <span class="icon">${Icons.calendar}</span>
                            <span>Activities</span>
                        </a>
                        <a href="#/leaderboard" class="nav-item ${activeNav === 'leaderboard' ? 'active' : ''}">
                            <span class="icon">${Icons.trophy}</span>
                            <span>Leaderboard</span>
                        </a>

                        <div class="nav-section">Users</div>
                        <a href="#/users" class="nav-item ${activeNav === 'users' ? 'active' : ''}">
                            <span class="icon">${Icons.users}</span>
                            <span>User Management</span>
                        </a>
                        <a href="#/carbon" class="nav-item ${activeNav === 'carbon' ? 'active' : ''}">
                            <span class="icon">${Icons.leaf}</span>
                            <span>Carbon Credits</span>
                        </a>

                        <div class="nav-section">System</div>
                        <a href="#/settings" class="nav-item ${activeNav === 'settings' ? 'active' : ''}">
                            <span class="icon">${Icons.settings}</span>
                            <span>Settings</span>
                        </a>
                    </nav>
                </aside>

                <!-- 主内容区 -->
                <div class="main-content">
                    <header class="main-header">
                        <div class="header-left">
                            <nav class="breadcrumb">
                                <a href="#/dashboard">Home</a>
                                <span class="separator">/</span>
                                <span class="current">${Layout.getPageTitle(activeNav)}</span>
                            </nav>
                        </div>
                        <div class="header-right">
                            <button class="header-btn" title="Search">
                                ${Icons.search}
                            </button>
                            <button class="header-btn" title="Notifications">
                                ${Icons.bell}
                            </button>
                            <div class="user-info">
                                <div class="user-avatar">A</div>
                                <div class="user-details">
                                    <div class="user-name">Admin</div>
                                    <div class="user-role">Administrator</div>
                                </div>
                            </div>
                        </div>
                    </header>
                    <main class="page-content">
                        ${content}
                    </main>
                </div>
            </div>
        `;
    },

    getPageTitle: (nav) => {
        const titles = {
            'dashboard': 'Dashboard',
            'ads': 'Advertisements',
            'activities': 'Activities',
            'leaderboard': 'Leaderboard',
            'users': 'User Management',
            'carbon': 'Carbon Credits',
            'settings': 'Settings'
        };
        return titles[nav] || 'Home';
    },

    // 导出图标供其他组件使用
    Icons
};

export default Layout;
