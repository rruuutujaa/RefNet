const ACCESS_TOKEN_KEY = "refnet_access_token";
const REFRESH_TOKEN_KEY = "refnet_refresh_token";
const USER_KEY = "refnet_user";

export const storage = {
    getAccessToken() {
        return sessionStorage.getItem(ACCESS_TOKEN_KEY) || "";
    },
    setAccessToken(token) {
        if (token) sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
    },
    getRefreshToken() {
        return localStorage.getItem(REFRESH_TOKEN_KEY) || "";
    },
    setRefreshToken(token) {
        if (token) localStorage.setItem(REFRESH_TOKEN_KEY, token);
    },
    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user || {}));
    },
    getUser() {
        try {
            return JSON.parse(localStorage.getItem(USER_KEY) || "{}");
        } catch (_e) {
            return {};
        }
    },
    clear() {
        sessionStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    }
};
