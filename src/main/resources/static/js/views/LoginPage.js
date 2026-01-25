/**
 * Login Page - EcoGo Admin
 * 现代化登录页面设计
 */

const LoginPage = {
    render: () => {
        const root = document.getElementById('root');

        root.innerHTML = `
            <div class="login-container">
                <!-- 左侧品牌区 -->
                <div class="login-left">
                    <div class="login-brand">
                        <div class="logo">
                            <svg viewBox="0 0 24 24" fill="currentColor">
                                <path d="M11 20A7 7 0 019.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/>
                                <path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12" fill="none" stroke="currentColor" stroke-width="2"/>
                            </svg>
                        </div>
                        <h1>EcoGo Admin</h1>
                        <p>Carbon Credits Management Platform for a Sustainable Future</p>
                    </div>

                    <div class="login-features">
                        <div class="login-feature">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
                                <polyline points="22,4 12,14.01 9,11.01"/>
                            </svg>
                            <span>Real-time Carbon Tracking</span>
                        </div>
                        <div class="login-feature">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
                                <circle cx="9" cy="7" r="4"/>
                                <path d="M23 21v-2a4 4 0 00-3-3.87"/>
                                <path d="M16 3.13a4 4 0 010 7.75"/>
                            </svg>
                            <span>User & Activity Management</span>
                        </div>
                        <div class="login-feature">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="20" x2="18" y2="10"/>
                                <line x1="12" y1="20" x2="12" y2="4"/>
                                <line x1="6" y1="20" x2="6" y2="14"/>
                            </svg>
                            <span>Analytics & Insights Dashboard</span>
                        </div>
                    </div>
                </div>

                <!-- 右侧登录表单 -->
                <div class="login-right">
                    <div class="login-box">
                        <h2>Welcome Back</h2>
                        <p class="subtitle">Sign in to your admin account</p>

                        <form id="login-form">
                            <div class="form-group">
                                <label class="form-label">Email Address</label>
                                <input type="email" class="form-input" name="email" placeholder="admin@ecogo.com" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Password</label>
                                <input type="password" class="form-input" name="password" placeholder="Enter your password" required>
                            </div>

                            <div class="form-group" style="display: flex; justify-content: space-between; align-items: center;">
                                <label style="display: flex; align-items: center; gap: 8px; cursor: pointer; font-weight: normal;">
                                    <input type="checkbox" name="remember" style="width: 16px; height: 16px;">
                                    <span>Remember me</span>
                                </label>
                                <a href="#" style="color: var(--primary); text-decoration: none; font-weight: 500;">Forgot password?</a>
                            </div>

                            <button type="submit" class="btn btn-primary btn-lg">
                                Sign In
                            </button>
                        </form>

                        <div class="login-footer">
                            <p>Don't have an account? <a href="#/register">Contact Administrator</a></p>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // 绑定表单提交
        document.getElementById('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(e.target);
            const email = formData.get('email');
            const password = formData.get('password');

            // 简单的前端验证 - 实际应用中应该调用后端 API
            if (email && password) {
                // 模拟登录成功
                localStorage.setItem('isLoggedIn', 'true');
                localStorage.setItem('userEmail', email);
                window.location.hash = '#/dashboard';
            }
        });
    }
};

export default LoginPage;
