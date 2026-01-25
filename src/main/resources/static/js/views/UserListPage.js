/**
 * User Management Page - Coming Soon
 * Placeholder page for user management functionality
 */
import Layout from '../components/Layout.js';

const Icons = {
    users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width: 64px; height: 64px; opacity: 0.6;">
        <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 00-3-3.87"/>
        <path d="M16 3.13a4 4 0 010 7.75"/>
    </svg>`
};

const UserListPage = {
    init: () => {
        UserListPage.render();
    },

    render: () => {
        const root = document.getElementById('root');

        const content = `
            <div class="page-header">
                <div class="page-title">
                    <h1>User Management</h1>
                    <p>Manage platform users and permissions</p>
                </div>
            </div>

            <div class="card">
                <div class="card-body" style="text-align: center; padding: 80px 40px;">
                    <div style="margin-bottom: 24px; color: var(--primary);">
                        ${Icons.users}
                    </div>
                    <h2 style="margin-bottom: 16px; color: var(--text-dark);">Coming Soon</h2>
                    <p style="color: var(--text-muted); max-width: 400px; margin: 0 auto 32px;">
                        User management functionality is currently under development.
                        This feature will allow you to manage users, roles, and permissions.
                    </p>
                    <div style="display: flex; gap: 12px; justify-content: center;">
                        <a href="#/dashboard" class="btn btn-primary">Back to Dashboard</a>
                    </div>
                </div>
            </div>
        `;

        root.innerHTML = Layout.render(content, 'users');
    }
};

export default UserListPage;
