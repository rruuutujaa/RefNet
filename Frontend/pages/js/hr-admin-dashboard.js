import { renderLayout, setUnreadCount } from "../../components/layout.js";
import { api } from "../../services/api.js";
import { statusBadge, formatDate, toPercent } from "../../utils/formatters.js";

let pageIndex = 0;
let sortField = "updatedAt";
let sortDir = "desc";
let statusFilter = "";
let lineChart;
let pieChart;

renderLayout({
    title: "HR Admin Dashboard",
    role: "HR Admin",
    activeKey: "dashboard",
    sidebarItems: [{ key: "dashboard", label: "Dashboard", href: "./hr-admin-dashboard.html" }]
});

document.getElementById("page-content").innerHTML = `
    <div class="row g-3 mb-3" id="kpi-cards"></div>
    <div class="row g-3 mb-3">
        <div class="col-12 col-xl-7"><div class="card"><div class="card-header">Referrals Over Last 6 Months</div><div class="card-body"><canvas id="line-chart"></canvas></div></div></div>
        <div class="col-12 col-xl-5"><div class="card"><div class="card-header">Status Breakdown</div><div class="card-body"><canvas id="pie-chart"></canvas></div></div></div>
    </div>
    <div class="card mb-3"><div class="card-header">Top Referrers Leaderboard</div><div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Employee</th><th>Total Referrals</th><th>Hired</th><th>Conversion</th></tr></thead><tbody id="top-referrers-body"></tbody></table></div></div>
    <div class="card">
        <div class="card-header d-flex gap-2 align-items-center">
            <span>All Referrals</span>
            <select id="status-filter" class="form-select form-select-sm w-auto"><option value="">All</option><option>SUBMITTED</option><option>UNDER_REVIEW</option><option>ACCEPTED</option><option>HIRED</option><option>NOT_SELECTED</option></select>
            <button id="sort-toggle" class="btn btn-outline-secondary btn-sm">Sort by ${sortField} (${sortDir})</button>
        </div>
        <div class="card-body table-responsive">
            <table class="table table-sm">
                <thead><tr><th></th><th>Candidate</th><th>Job</th><th>Referrer</th><th>Status</th><th>Updated</th></tr></thead>
                <tbody id="referral-table-body"></tbody>
            </table>
            <div class="d-flex justify-content-between">
                <button id="prev-page" class="btn btn-outline-secondary btn-sm">Previous</button>
                <button id="next-page" class="btn btn-outline-secondary btn-sm">Next</button>
            </div>
        </div>
    </div>
`;

function drawCharts(referrals) {
    const byMonth = {};
    const byStatus = {};
    referrals.forEach((r) => {
        const month = new Date(r.submittedAt || Date.now()).toLocaleString(undefined, { month: "short" });
        byMonth[month] = (byMonth[month] || 0) + 1;
        byStatus[r.status] = (byStatus[r.status] || 0) + 1;
    });

    lineChart?.destroy();
    pieChart?.destroy();

    lineChart = new Chart(document.getElementById("line-chart"), {
        type: "line",
        data: { labels: Object.keys(byMonth), datasets: [{ label: "Referrals", data: Object.values(byMonth), borderColor: "#2563eb", tension: 0.3 }] }
    });
    pieChart = new Chart(document.getElementById("pie-chart"), {
        type: "pie",
        data: { labels: Object.keys(byStatus), datasets: [{ data: Object.values(byStatus) }] }
    });
}

function renderKpis(referrals) {
    const hired = referrals.filter((r) => r.status === "HIRED").length;
    const outcomes = referrals.filter((r) => ["HIRED", "NOT_SELECTED"].includes(r.status));
    const avgDays = outcomes.length
        ? Math.round(outcomes.reduce((sum, r) => sum + ((new Date(r.updatedAt) - new Date(r.submittedAt)) / (1000 * 60 * 60 * 24)), 0) / outcomes.length)
        : 0;
    document.getElementById("kpi-cards").innerHTML = [
        ["Total Referrals This Month", referrals.length],
        ["Conversion Rate", toPercent(hired, referrals.length)],
        ["Average Days To Outcome", avgDays]
    ].map(([label, val]) => `<div class="col-12 col-md-4"><div class="card"><div class="card-body"><div class="small text-muted">${label}</div><div class="kpi-value">${val}</div></div></div></div>`).join("");
}

async function loadTopReferrers(referrals) {
    const grouped = {};
    referrals.forEach((r) => {
        grouped[r.referrerName] = grouped[r.referrerName] || { total: 0, hired: 0 };
        grouped[r.referrerName].total += 1;
        if (r.status === "HIRED") grouped[r.referrerName].hired += 1;
    });
    const top = Object.entries(grouped).map(([name, v]) => ({ name, ...v })).sort((a, b) => b.total - a.total).slice(0, 10);
    document.getElementById("top-referrers-body").innerHTML = top.map((r) => `<tr><td>${r.name || "-"}</td><td>${r.total}</td><td>${r.hired}</td><td>${toPercent(r.hired, r.total)}</td></tr>`).join("") || `<tr><td colspan="4" class="text-muted">No data.</td></tr>`;
}

async function loadReferralsTable() {
    const params = new URLSearchParams({ page: pageIndex, size: 10 });
    if (statusFilter) params.set("status", statusFilter);
    const data = await api.referrals.list(`?${params.toString()}`);
    let rows = (data.content || []).sort((a, b) => {
        const x = a[sortField] || "";
        const y = b[sortField] || "";
        return sortDir === "asc" ? `${x}`.localeCompare(`${y}`) : `${y}`.localeCompare(`${x}`);
    });
    document.getElementById("referral-table-body").innerHTML = rows.map((r) => `
        <tr>
            <td><button class="btn btn-sm btn-outline-primary" data-expand="${r.id}">+</button></td>
            <td>${r.candidateName || "-"}</td>
            <td>${r.jobTitle || "-"}</td>
            <td>${r.referrerName || "-"}</td>
            <td>${statusBadge(r.status)}</td>
            <td>${formatDate(r.updatedAt)}</td>
        </tr>
        <tr id="exp-${r.id}" class="d-none"><td colspan="6"><div class="small text-muted">Loading details...</div></td></tr>
    `).join("");
    return data.content || [];
}

async function init() {
    const [allPage, unreadCount] = await Promise.all([api.referrals.list("?page=0&size=300"), api.notifications.unreadCount()]);
    const referrals = allPage.content || [];
    setUnreadCount(unreadCount || 0);
    renderKpis(referrals);
    drawCharts(referrals);
    await loadTopReferrers(referrals);
    await loadReferralsTable();
}

document.addEventListener("click", async (e) => {
    if (e.target.id === "prev-page" && pageIndex > 0) {
        pageIndex -= 1;
        await loadReferralsTable();
    }
    if (e.target.id === "next-page") {
        pageIndex += 1;
        await loadReferralsTable();
    }
    if (e.target.id === "sort-toggle") {
        sortDir = sortDir === "asc" ? "desc" : "asc";
        e.target.textContent = `Sort by ${sortField} (${sortDir})`;
        await loadReferralsTable();
    }
    if (e.target.dataset.expand) {
        const id = e.target.dataset.expand;
        const row = document.getElementById(`exp-${id}`);
        const isHidden = row.classList.contains("d-none");
        if (!isHidden) {
            row.classList.add("d-none");
            return;
        }
        const history = await api.referrals.history(id);
        row.innerHTML = `<td colspan="6"><div><strong>History Timeline</strong><ul>${history.map((h) => `<li>${h.oldStatus || "N/A"} -> ${h.newStatus} (${formatDate(h.changedAt)}) ${h.note || ""}</li>`).join("")}</ul><div class="small text-muted">Resume preview/download and notes are ready for backend linkage through referral detail endpoint.</div></div></td>`;
        row.classList.remove("d-none");
    }
});

document.getElementById("status-filter")?.addEventListener("change", async (e) => {
    statusFilter = e.target.value;
    pageIndex = 0;
    await loadReferralsTable();
});

init().catch(console.error);
