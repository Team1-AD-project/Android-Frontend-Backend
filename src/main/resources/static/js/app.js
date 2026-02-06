/**
 * EcoGo Admin - Main Application Entry
 * This file handles routing and delegates initialization to the appropriate page components.
 */
import LoginPage from './views/LoginPage.js';
import DashboardPage from './views/DashboardPage.js';
import UserListPage from './views/UserListPage.js';
import ActivityListPage from './views/ActivityListPage.js';
import LeaderboardPage from './views/LeaderboardPage.js';
import CarbonCreditsPage from './views/CarbonCreditsPage.js';
import AdvertisementListPage from './views/AdvertisementListPage.js';
import AdvertisementFormPage from './views/AdvertisementFormPage.js';
import SettingsPage from './views/SettingsPage.js';
import { VoucherListPage } from './views/VoucherListPage.js';
import { VoucherFormPage } from './views/VoucherFormPage.js';

// --- Route Configuration ---
const routes = {
    // Special Pages
    '/login': LoginPage,

    // Main Navigation
    '/': DashboardPage,
    '/dashboard': DashboardPage,
    '/users': UserListPage,
    '/activities': ActivityListPage,
    '/leaderboard': LeaderboardPage,
    '/carbon': CarbonCreditsPage,
    '/settings': SettingsPage,

    // Advertisement specific routes
    '/ads': AdvertisementListPage,
    '/ad/new': AdvertisementFormPage,
    '/ad/edit': AdvertisementFormPage,

    // Voucher specific routes
    '/vouchers': VoucherListPage,
    '/voucher/new': VoucherFormPage,
    '/voucher/edit': VoucherFormPage
};

// --- Router Handler ---
const router = () => {
    const hash = window.location.hash || '#/';
    // Remove query parameters before parsing the path
    const pathWithoutQuery = hash.substring(1).split('?')[0];
    const parts = pathWithoutQuery.split('/');

    let routeKey = '/' + (parts[1] || '');
    if (parts.length > 2 && (parts[2] === 'new' || parts[2] === 'edit')) {
        routeKey += '/' + parts[2];
    }

    let page = routes[routeKey];
    
    // Handle class-based pages (VoucherListPage, VoucherFormPage)
    if (!page && routeKey.startsWith('/voucher/edit')) {
        const voucherId = parts[3];
        page = new VoucherFormPage(voucherId);
    } else if (page === VoucherListPage) {
        page = new VoucherListPage();
    } else if (page === VoucherFormPage) {
        page = new VoucherFormPage();
    }

    page = page || routes['/'];

    if (page && typeof page.init === 'function') {
        page.init().then(() => {
            const content = page.render();
            document.getElementById('app').innerHTML = content;
            // Store instance for global handlers
            if (page instanceof VoucherListPage) {
                window.voucherListPageInstance = page;
            } else if (page instanceof VoucherFormPage) {
                window.voucherFormPageInstance = page;
            }
        });
    } else if (page && typeof page.render === 'function') {
        const content = page.render();
        document.getElementById('app').innerHTML = content;
    } else if (page && page.init) {
        page.init();
    } else if (page && page.render) {
        page.render();
    } else {
        console.error(`No handler or page found for route: ${routeKey}`);
        // Optionally, render a 404 page
        document.getElementById('app').innerHTML = `<h1>404 Not Found</h1>`;
    }
};

// --- Event Listeners ---
window.addEventListener('hashchange', router);
window.addEventListener('load', router);
