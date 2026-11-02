/**
 * Authentication and RBAC Manager for HospitalOOP70
 */
const Auth = {
    init() {
        const isLoginPage = window.location.pathname.endsWith('login.html');
        const token = API.getToken();
        const user = API.getUser();

        if (isLoginPage) {
            if (token && user) {
                window.location.href = '/index.html';
            }
            this.setupLoginForm();
        } else {
            if (!token || !user) {
                // If not logged in, auto-login as demo admin or redirect
                this.fallbackDemoLogin();
            } else {
                this.applyUserInterface(user);
            }
        }
    },

    fallbackDemoLogin() {
        // Fallback for seamless demo testing: admin / hospital123
        const demoUser = {
            username: 'admin',
            role: 'Administrator',
            fullName: 'Dr. System Administrator',
            isAdmin: true,
            isDoctor: true,
            isReceptionist: true,
            isStaff: true
        };
        API.setUser(demoUser);
        API.setToken('HMS-DEMO-TOKEN');
        this.applyUserInterface(demoUser);
    },

    setupLoginForm() {
        const form = document.getElementById('login-form');
        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            const roleSelect = document.getElementById('role');
            const errorAlert = document.getElementById('login-error');
            const submitBtn = document.getElementById('login-btn');

            const username = usernameInput.value.trim();
            const password = passwordInput.value.trim();
            const role = roleSelect ? roleSelect.value : 'Administrator';

            if (!username || !password) {
                errorAlert.textContent = 'Please enter both username and password.';
                errorAlert.style.display = 'block';
                return;
            }

            errorAlert.style.display = 'none';
            submitBtn.disabled = true;
            submitBtn.textContent = 'Authenticating...';

            try {
                const res = await API.post('/auth/login', { username, password, role });
                if (res.success && res.token) {
                    API.setToken(res.token);
                    API.setUser(res.user);
                    showToast('Login successful! Redirecting...', 'success');
                    setTimeout(() => {
                        window.location.href = '/index.html';
                    }, 500);
                } else {
                    errorAlert.textContent = res.error || 'Invalid credentials or role mismatch.';
                    errorAlert.style.display = 'block';
                }
            } catch (err) {
                errorAlert.textContent = err.message || 'Login failed. Verify server is running.';
                errorAlert.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Sign In to Portal';
            }
        });
    },

    applyUserInterface(user) {
        const roleBadge = document.getElementById('header-user-role');
        if (roleBadge) roleBadge.textContent = user.role.toUpperCase();

        const nameLabel = document.getElementById('header-user-name');
        if (nameLabel) nameLabel.textContent = user.fullName;

        const avatar = document.getElementById('header-user-avatar');
        if (avatar) avatar.textContent = (user.fullName || 'A').charAt(0).toUpperCase();

        // Enforce RBAC on navigation links
        this.enforceRBAC(user);
    },

    enforceRBAC(user) {
        if (user.role === 'Administrator') return; // Admin has full access

        const restrictions = {
            'Doctor': ['nav-admissions', 'nav-discharge'],
            'Receptionist': ['nav-opd', 'nav-workflow', 'nav-admissions', 'nav-discharge', 'nav-doctors'],
            'Hospital Staff': ['nav-opd', 'nav-workflow', 'nav-registration', 'nav-priority-queue']
        };

        const hideList = restrictions[user.role] || [];
        hideList.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
    },

    async logout() {
        try {
            await API.post('/auth/logout', {});
        } catch (ignored) {}
        API.clearToken();
        showToast('Logged out successfully.', 'info');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 400);
    }
};

document.addEventListener('DOMContentLoaded', () => Auth.init());

