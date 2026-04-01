import { renderLayout, setUnreadCount } from "../../components/layout.js";
import { mountChat, renderChatMessages, renderChatUsers } from "../../components/chat.js";
import { api } from "../../services/api.js";
import { connectWebSocket, sendChatMessage } from "../../services/websocket.js";
import { storage } from "../../services/storage.js";
import { statusBadge, toPercent, formatDate } from "../../utils/formatters.js";

const me = storage.getUser();
let selectedPartnerId = null;

renderLayout({
    title: "Employee Dashboard",
    role: "Employee",
    activeKey: "dashboard",
    sidebarItems: [{ key: "dashboard", label: "Dashboard", href: "./employee-dashboard.html" }]
});

document.getElementById("page-content").innerHTML = `
    <div class="row g-3 mb-3" id="stats-row"></div>
    <div class="row g-3">
        <div class="col-12 col-xl-7">
            <div class="card"><div class="card-header">Referral Requests Received</div><div class="card-body"><div id="request-cards" class="row g-2"></div></div></div>
            <div class="card mt-3"><div class="card-header">All Referrals Submitted</div><div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Candidate</th><th>Job</th><th>Status</th><th>Updated</th></tr></thead><tbody id="referrals-table"></tbody></table></div></div>
        </div>
        <div class="col-12 col-xl-5">
            <div class="card"><div class="card-header">Notifications</div><div class="card-body" id="notifications-list"></div></div>
            <div class="mt-3" id="chat-root"></div>
        </div>
    </div>
`;
mountChat("chat-root");

async function loadRequests() {
    const requests = await api.referralRequests.received();
    document.getElementById("request-cards").innerHTML = requests.map((r) => `
        <div class="col-12">
            <div class="border rounded p-2">
                <div class="d-flex justify-content-between">
                    <div>
                        <strong>${r.candidateName || "-"}</strong>
                        <div class="small text-muted">${r.jobTitle || "-"}</div>
                        <a href="#" class="small">Resume download</a>
                    </div>
                    <div>${statusBadge(r.status)}</div>
                </div>
                <div class="small mt-1">${r.message || ""}</div>
                ${r.status === "PENDING" ? `<div class="mt-2"><button data-action="accept" data-id="${r.id}" class="btn btn-success btn-sm">Accept</button> <button data-action="decline" data-id="${r.id}" class="btn btn-outline-danger btn-sm">Decline</button></div>` : ""}
            </div>
        </div>
    `).join("") || `<div class="text-muted">No pending requests.</div>`;
}

async function loadReferrals() {
    const referrals = await api.referrals.mine();
    const accepted = referrals.filter((r) => r.status === "ACCEPTED").length;
    const hired = referrals.filter((r) => r.status === "HIRED").length;
    document.getElementById("stats-row").innerHTML = [
        ["Total Referred", referrals.length],
        ["Accepted", accepted],
        ["Hired", hired],
        ["Conversion Rate", toPercent(hired, referrals.length)]
    ].map(([label, value]) => `<div class="col-6 col-lg-3"><div class="card"><div class="card-body"><div class="small text-muted">${label}</div><div class="kpi-value">${value}</div></div></div></div>`).join("");

    document.getElementById("referrals-table").innerHTML = referrals.map((r) => `
        <tr>
            <td>${r.candidateName || "-"}</td>
            <td>${r.jobTitle || "-"}</td>
            <td>${statusBadge(r.status)}</td>
            <td>${formatDate(r.updatedAt)}</td>
        </tr>
    `).join("") || `<tr><td colspan="4" class="text-muted">No referrals submitted.</td></tr>`;
}

async function loadNotificationsAndChatUsers() {
    const [notifPage, notifCount, recent] = await Promise.all([api.notifications.list(0, 8), api.notifications.unreadCount(), api.chat.recent()]);
    setUnreadCount(notifCount || 0);
    const notifications = notifPage.content || [];
    document.getElementById("notifications-list").innerHTML = notifications.map((n) => `<div class="border-bottom py-2"><div class="fw-semibold">${n.title}</div><div class="small text-muted">${n.body}</div></div>`).join("") || `<div class="text-muted">No notifications.</div>`;
    const users = (recent || []).map((c) => ({ id: String(c.senderId) === String(me.id) ? c.receiverId : c.senderId, name: String(c.senderId) === String(me.id) ? c.receiverName : c.senderName }));
    renderChatUsers(users);
    if (users.length) {
        selectedPartnerId = users[0].id;
        const page = await api.chat.conversation(selectedPartnerId);
        renderChatMessages(page.content || [], me.id);
    }
}

document.addEventListener("click", async (e) => {
    const id = e.target.dataset.id;
    const action = e.target.dataset.action;
    if (action === "accept") {
        await api.referralRequests.accept(id);
        await loadRequests();
    }
    if (action === "decline") {
        const reason = window.prompt("Decline reason:");
        await api.referralRequests.decline(id, reason || "Not a match");
        await loadRequests();
    }
    if (e.target.id === "chat-send") {
        const input = document.getElementById("chat-input");
        if (selectedPartnerId && input.value.trim()) {
            sendChatMessage(selectedPartnerId, input.value.trim());
            input.value = "";
        }
    }
});

document.addEventListener("change", async (e) => {
    if (e.target.id === "chat-user-select") {
        selectedPartnerId = e.target.value;
        const page = await api.chat.conversation(selectedPartnerId);
        renderChatMessages(page.content || [], me.id);
        await api.chat.markRead(selectedPartnerId);
    }
});

async function init() {
    await Promise.all([loadRequests(), loadReferrals(), loadNotificationsAndChatUsers()]);
    await connectWebSocket({
        onMessage: async () => {
            if (selectedPartnerId) {
                const page = await api.chat.conversation(selectedPartnerId);
                renderChatMessages(page.content || [], me.id);
            }
            await loadNotificationsAndChatUsers();
        }
    });
    setInterval(loadNotificationsAndChatUsers, 30000);
}

init().catch(console.error);
