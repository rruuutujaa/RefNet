import { api } from "../../services/api.js";
import { storage } from "../../services/storage.js";

const roleToPage = {
    CANDIDATE: "./pages/candidate-dashboard.html",
    EMPLOYEE: "./pages/employee-dashboard.html",
    HR_ADMIN: "./pages/hr-admin-dashboard.html",
    ADMIN: "./pages/super-admin-dashboard.html"
};

document.getElementById("login-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = document.getElementById("login-btn");
    const error = document.getElementById("auth-error");
    error.classList.add("d-none");

    try {
        btn.disabled = true;
        btn.textContent = "Signing in...";
        const payload = {
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value
        };
        const auth = await api.auth.login(payload);
        storage.setAccessToken(auth.access_token);
        storage.setRefreshToken(auth.refresh_token);

        const me = await api.users.me();
        storage.setUser(me);
        const role = (me.roles && me.roles[0]) || "CANDIDATE";
        window.location.href = roleToPage[role] || roleToPage.CANDIDATE;
    } catch (err) {
        error.textContent = err.message || "Login failed";
        error.classList.remove("d-none");
    } finally {
        btn.disabled = false;
        btn.textContent = "Sign In";
    }
});
