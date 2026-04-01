import { storage } from "./storage.js";

const API_BASE_URL = window.REFNET_API_BASE_URL || "http://localhost:8080/api/v1";

async function request(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    const accessToken = storage.getAccessToken();
    if (accessToken) headers.Authorization = `Bearer ${accessToken}`;

    const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Request failed: ${response.status}`);
    }

    if (response.status === 204) return null;
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) return response.json();
    return response.text();
}

export const api = {
    auth: {
        login(payload) {
            return request("/auth/login", { method: "POST", body: JSON.stringify(payload) });
        },
        refreshToken() {
            const refreshToken = storage.getRefreshToken();
            return request("/auth/refresh-token", { method: "POST", headers: { Authorization: `Bearer ${refreshToken}` } });
        }
    },
    users: {
        me() { return request("/users/me"); },
        search(params = "") { return request(`/users/search${params}`); },
        byId(id) { return request(`/users/${id}`); }
    },
    jobs: {
        list(params = "") { return request(`/jobs${params}`); },
        listAll() { return request("/jobs?page=0&size=100"); }
    },
    referralRequests: {
        sent() { return request("/referral-requests/sent"); },
        received() { return request("/referral-requests/received"); },
        accept(id) { return request(`/referral-requests/${id}/accept`, { method: "PATCH" }); },
        decline(id, reason) { return request(`/referral-requests/${id}/decline?reason=${encodeURIComponent(reason || "")}`, { method: "PATCH" }); }
    },
    referrals: {
        mine() { return request("/referrals/mine"); },
        received() { return request("/referrals/received"); },
        list(params = "") { return request(`/referrals${params}`); },
        history(id) { return request(`/referrals/${id}/history`); },
        updateStatus(id, payload) { return request(`/referrals/${id}/status`, { method: "PATCH", body: JSON.stringify(payload) }); }
    },
    notifications: {
        list(page = 0, size = 10) { return request(`/notifications?page=${page}&size=${size}`); },
        unreadCount() { return request("/notifications/unread-count"); }
    },
    chat: {
        recent() { return request("/chat/recent"); },
        conversation(userId, page = 0, size = 20) { return request(`/chat/${userId}?page=${page}&size=${size}`); },
        markRead(senderId) { return request(`/chat/read/${senderId}`, { method: "PATCH" }); }
    },
    analytics: {
        hrOverview() { return request("/analytics/hr/overview"); },
        hrReferralsOverTime() { return request("/analytics/hr/referrals-over-time"); },
        hrStatusBreakdown() { return request("/analytics/hr/status-breakdown"); },
        hrTopReferrers() { return request("/analytics/hr/top-referrers"); },
        adminOverview() { return request("/analytics/admin/platform-overview"); }
    },
    admin: {
        users(params = "") { return request(`/admin/users${params}`); },
        changeRole(userId, role) { return request(`/admin/users/${userId}/role`, { method: "PATCH", body: JSON.stringify({ role }) }); },
        changeActive(userId, isActive) { return request(`/admin/users/${userId}/active`, { method: "PATCH", body: JSON.stringify({ isActive }) }); },
        jobs(params = "") { return request(`/admin/jobs${params}`); },
        companies(params = "") { return request(`/admin/companies${params}`); },
        auditLogs(params = "") { return request(`/admin/audit-logs${params}`); }
    }
};
