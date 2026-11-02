/**
 * Reusable REST API Client for HospitalOOP70
 */
const API = {
    baseUrl: '/api',

    getToken() {
        return sessionStorage.getItem('hms_token') || localStorage.getItem('hms_token');
    },

    setToken(token) {
        sessionStorage.setItem('hms_token', token);
    },

    clearToken() {
        sessionStorage.removeItem('hms_token');
        localStorage.removeItem('hms_token');
        sessionStorage.removeItem('hms_user');
    },

    getUser() {
        const u = sessionStorage.getItem('hms_user');
        return u ? JSON.parse(u) : null;
    },

    setUser(user) {
        sessionStorage.setItem('hms_user', JSON.stringify(user));
    },

    async request(endpoint, options = {}) {
        const url = new URL(this.baseUrl + endpoint, window.location.origin);
        if (options.params) {
            Object.keys(options.params).forEach(k => {
                if (options.params[k] !== undefined && options.params[k] !== null && options.params[k] !== '') {
                    url.searchParams.append(k, options.params[k]);
                }
            });
        }

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            method: options.method || 'GET',
            headers,
            ...options
        };

        if (options.body && typeof options.body === 'object') {
            config.body = JSON.stringify(options.body);
        }

        try {
            const res = await fetch(url.toString(), config);
            if (res.status === 401 && !endpoint.includes('/auth/login')) {
                this.clearToken();
                window.location.href = '/login.html';
                throw new Error('Session expired. Please log in again.');
            }

            const data = await res.json();
            if (!res.ok && data && data.error) {
                throw new Error(data.error);
            }
            return data;
        } catch (err) {
            console.error(`API Error [${endpoint}]:`, err);
            throw err;
        }
    },

    get(endpoint, params) {
        return this.request(endpoint, { method: 'GET', params });
    },

    post(endpoint, body) {
        return this.request(endpoint, { method: 'POST', body });
    },

    put(endpoint, body) {
        return this.request(endpoint, { method: 'PUT', body });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

/**
 * Toast Notification Helper
 */
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    else if (type === 'error') icon = '❌';
    else if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'opacity 0.3s, transform 0.3s';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

