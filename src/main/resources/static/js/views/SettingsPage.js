/**
 * Settings Page
 * System configuration and preferences
 */
import Layout from '../components/Layout.js';

const SettingsPage = {
    init: () => {
        SettingsPage.render();
    },

    render: () => {
        const root = document.getElementById('root');

        const content = `
            <div class="page-header">
                <div class="page-title">
                    <h1>Settings</h1>
                    <p>Configure system preferences</p>
                </div>
            </div>

            <div class="settings-grid">
                <!-- General Settings -->
                <div class="card">
                    <div class="card-header">
                        <h3>General Settings</h3>
                    </div>
                    <div class="card-body">
                        <div class="form-group">
                            <label class="form-label">Platform Name</label>
                            <input type="text" class="form-input" value="EcoGo Admin" placeholder="Platform name">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Contact Email</label>
                            <input type="email" class="form-input" value="admin@ecogo.com" placeholder="Contact email">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Language</label>
                            <select class="form-select">
                                <option value="en" selected>English</option>
                                <option value="zh">Chinese</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Notification Settings -->
                <div class="card">
                    <div class="card-header">
                        <h3>Notifications</h3>
                    </div>
                    <div class="card-body">
                        <div class="setting-item">
                            <div class="setting-info">
                                <h4>Email Notifications</h4>
                                <p>Receive email alerts for important events</p>
                            </div>
                            <label class="toggle">
                                <input type="checkbox" checked>
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                        <div class="setting-item">
                            <div class="setting-info">
                                <h4>Weekly Reports</h4>
                                <p>Receive weekly summary reports</p>
                            </div>
                            <label class="toggle">
                                <input type="checkbox" checked>
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                        <div class="setting-item">
                            <div class="setting-info">
                                <h4>User Activity Alerts</h4>
                                <p>Get notified when users complete activities</p>
                            </div>
                            <label class="toggle">
                                <input type="checkbox">
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                    </div>
                </div>

                <!-- Security Settings -->
                <div class="card">
                    <div class="card-header">
                        <h3>Security</h3>
                    </div>
                    <div class="card-body">
                        <div class="form-group">
                            <label class="form-label">Current Password</label>
                            <input type="password" class="form-input" placeholder="Enter current password">
                        </div>
                        <div class="form-group">
                            <label class="form-label">New Password</label>
                            <input type="password" class="form-input" placeholder="Enter new password">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Confirm Password</label>
                            <input type="password" class="form-input" placeholder="Confirm new password">
                        </div>
                        <button class="btn btn-primary">Update Password</button>
                    </div>
                </div>

                <!-- System Info -->
                <div class="card">
                    <div class="card-header">
                        <h3>System Information</h3>
                    </div>
                    <div class="card-body">
                        <div class="info-row">
                            <span class="info-label">Version</span>
                            <span class="info-value">1.0.0</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Environment</span>
                            <span class="info-value">Development</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Database</span>
                            <span class="info-value">MongoDB</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Server Port</span>
                            <span class="info-value">8090</span>
                        </div>
                    </div>
                </div>
            </div>

            <style>
                .settings-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
                    gap: 24px;
                }
                .card-header {
                    padding: 16px 24px;
                    border-bottom: 1px solid var(--border);
                    background: var(--bg-secondary);
                }
                .card-header h3 {
                    margin: 0;
                    font-size: 16px;
                    font-weight: 600;
                }
                .setting-item {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 16px 0;
                    border-bottom: 1px solid var(--border-light);
                }
                .setting-item:last-child {
                    border-bottom: none;
                }
                .setting-info h4 {
                    margin: 0 0 4px 0;
                    font-size: 14px;
                    font-weight: 600;
                }
                .setting-info p {
                    margin: 0;
                    font-size: 13px;
                    color: var(--text-muted);
                }
                .toggle {
                    position: relative;
                    display: inline-block;
                    width: 48px;
                    height: 26px;
                }
                .toggle input {
                    opacity: 0;
                    width: 0;
                    height: 0;
                }
                .toggle-slider {
                    position: absolute;
                    cursor: pointer;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background-color: #ccc;
                    transition: 0.3s;
                    border-radius: 26px;
                }
                .toggle-slider:before {
                    position: absolute;
                    content: "";
                    height: 20px;
                    width: 20px;
                    left: 3px;
                    bottom: 3px;
                    background-color: white;
                    transition: 0.3s;
                    border-radius: 50%;
                }
                .toggle input:checked + .toggle-slider {
                    background-color: var(--primary);
                }
                .toggle input:checked + .toggle-slider:before {
                    transform: translateX(22px);
                }
                .info-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 12px 0;
                    border-bottom: 1px solid var(--border-light);
                }
                .info-row:last-child {
                    border-bottom: none;
                }
                .info-label {
                    color: var(--text-muted);
                }
                .info-value {
                    font-weight: 600;
                }
            </style>
        `;

        root.innerHTML = Layout.render(content, 'settings');
    }
};

export default SettingsPage;
