import { renderLayout, setUnreadCount } from "../../components/layout.js";
import { api } from "../../services/api.js";
import { formatDate } from "../../utils/formatters.js";

renderLayout({
    title: "Super Admin Dashboard",
    role: "Super Admin",
    activeKey: "dashboard",
    sidebarItems: [{ key: "dashboard", label: "Dashboard", href: "./super-admin-dashboard.html" }]
});

document.getElementById("page-content").innerHTML = `
    <div class="row g-3 mb-3" id="overview-cards"></div>
    <div class="card mb-3"><div class="card-header">Company Activity</div><div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Company</th><th>Users</th><th>Jobs</th><th>Referrals</th></tr></thead><tbody id="company-activity-body"></tbody></table></div></div>
    <div class="card mb-3">
        <div class="card-header d-flex justify-content-between align-items-center">
            <span>User Management</span>
            <input id="user-search" class="form-control form-control-sm w-25" placeholder="Search users">
        </div>
        <div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Active</th><th>Actions</th></tr></thead><tbody id="users-body"></tbody></table></div>
    </div>
    <div class="card mb-3"><div class="card-header">Job Management</div><div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Title</th><th>Department</th><th>Status</th></tr></thead><tbody id="jobs-body"></tbody></table></div></div>
    <div class="card">
        <div class="card-header d-flex gap-2">
            <span>Audit Logs</span>
            <input id="audit-search" class="form-control form-control-sm w-25" placeholder="Search audit logs">
            <input id="audit-type" class="form-control form-control-sm w-25" placeholder="Event type">
        </div>
        <div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Timestamp</th><th>Actor</th><th>Event</th><th>Entity</th></tr></thead><tbody id="audit-body"></tbody></table></div>
    </div>
`;

async function safeCall(fn, fallback) {
    try {
        return await fn();
    } catch (_e) {
        return fallback;
    }
}

async function loadOverview() {
    const [overview, fallbackJobs] = await Promise.all([
        safeCall(() => api.analytics.adminOverview(), null),
        safeCall(() => api.jobs.listAll(), { content: [] })
    ]);
    const data = overview || {
        totalCompanies: "-",
        totalUsers: "-",
        totalReferrals: "-",
        companyActivity: []
    };
    document.getElementById("overview-cards").innerHTML = [
        ["Total Companies", data.totalCompanies],
        ["Total Users", data.totalUsers],
        ["Total Referrals", data.totalReferrals]
    ].map(([k, v]) => `<div class="col-12 col-md-4"><div class="card"><div class="card-body"><div class="small text-muted">${k}</div><div class="kpi-value">${v}</div></div></div></div>`).join("");

    const activity = data.companyActivity?.length ? data.companyActivity : [];
    document.getElementById("company-activity-body").innerHTML = activity.map((c) => `<tr><td>${c.companyName}</td><td>${c.userCount}</td><td>${c.jobCount}</td><td>${c.referralCount}</td></tr>`).join("") || `<tr><td colspan="4" class="text-muted">Company analytics endpoint not available yet.</td></tr>`;

    const jobs = fallbackJobs.content || [];
    document.getElementById("jobs-body").innerHTML = jobs.map((j) => `<tr><td>${j.title}</td><td>${j.department || "-"}</td><td>${j.status}</td></tr>`).join("") || `<tr><td colspan="3" class="text-muted">No jobs found.</td></tr>`;
}

async function loadUsers(search = "") {
    const params = search ? `?q=${encodeURIComponent(search)}&page=0&size=50` : "?page=0&size=50";
    const data = await safeCall(() => api.admin.users(params), { content: [] });
    const users = data.content || [];
    document.getElementById("users-body").innerHTML = users.map((u) => `
        <tr>
            <td>${u.fullName || "-"}</td>
            <td>${u.email || "-"}</td>
            <td>
                <select class="form-select form-select-sm" data-role-user="${u.id}">
                    ${["CANDIDATE", "EMPLOYEE", "HR_ADMIN", "ADMIN"].map((r) => `<option ${u.role === r ? "selected" : ""}>${r}</option>`).join("")}
                </select>
            </td>
            <td>${u.isActive ? "Yes" : "No"}</td>
            <td><button class="btn btn-sm btn-outline-secondary" data-toggle-user="${u.id}" data-active="${u.isActive}">${u.isActive ? "Deactivate" : "Activate"}</button></td>
        </tr>
    `).join("") || `<tr><td colspan="5" class="text-muted">Admin user endpoint unavailable.</td></tr>`;
}

async function loadAuditLogs() {
    const q = document.getElementById("audit-search").value.trim();
    const type = document.getElementById("audit-type").value.trim();
    const query = new URLSearchParams({ page: 0, size: 50 });
    if (q) query.set("q", q);
    if (type) query.set("type", type);
    const data = await safeCall(() => api.admin.auditLogs(`?${query.toString()}`), { content: [] });
    const logs = data.content || [];
    document.getElementById("audit-body").innerHTML = logs.map((l) => `<tr><td>${formatDate(l.timestamp)}</td><td>${l.actorEmail || l.actorRole || "-"}</td><td>${l.eventType || "-"}</td><td>${l.entityType || "-"} ${l.entityId || ""}</td></tr>`).join("") || `<tr><td colspan="4" class="text-muted">Audit log endpoint unavailable.</td></tr>`;
}

document.addEventListener("change", async (e) => {
    if (e.target.dataset.roleUser) {
        await safeCall(() => api.admin.changeRole(e.target.dataset.roleUser, e.target.value), null);
    }
});

document.addEventListener("click", async (e) => {
    if (e.target.dataset.toggleUser) {
        const userId = e.target.dataset.toggleUser;
        const current = e.target.dataset.active === "true";
        await safeCall(() => api.admin.changeActive(userId, !current), null);
        await loadUsers(document.getElementById("user-search").value.trim());
    }
});

document.getElementById("user-search")?.addEventListener("input", async (e) => loadUsers(e.target.value.trim()));
document.getElementById("audit-search")?.addEventListener("input", loadAuditLogs);
document.getElementById("audit-type")?.addEventListener("input", loadAuditLogs);

async function init() {
    const unread = await safeCall(() => api.notifications.unreadCount(), 0);
    setUnreadCount(unread || 0);
    await Promise.all([loadOverview(), loadUsers(), loadAuditLogs()]);
}

init().catch(console.error);
