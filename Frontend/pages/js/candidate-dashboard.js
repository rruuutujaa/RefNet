import { renderLayout, renderTimeline, setUnreadCount } from "../../components/layout.js";
import { mountChat, renderChatMessages, renderChatUsers } from "../../components/chat.js";
import { api } from "../../services/api.js";
import { connectWebSocket, sendChatMessage } from "../../services/websocket.js";
import { storage } from "../../services/storage.js";
import { formatDate, statusBadge } from "../../utils/formatters.js";

const TIMELINE = ["SUBMITTED", "UNDER_REVIEW", "ACCEPTED", "HIRED", "NOT_SELECTED"];
const me = storage.getUser();
let recentConversations = [];
let selectedPartnerId = null;

renderLayout({
    title: "Candidate Dashboard",
    role: "Candidate",
    activeKey: "dashboard",
    sidebarItems: [{ key: "dashboard", label: "Dashboard", href: "./candidate-dashboard.html" }]
});

document.getElementById("page-content").innerHTML = `
    <div class="row g-3">
        <div class="col-12 col-lg-4"><div class="card"><div class="card-body"><h6>Profile Completion</h6><div class="progress"><div id="profile-progress" class="progress-bar" style="width:0%">0%</div></div></div></div></div>
        <div class="col-12 col-lg-4"><div class="card"><div class="card-body"><h6>Active Referrals</h6><div class="kpi-value" id="active-referrals">0</div></div></div></div>
        <div class="col-12 col-lg-4"><div class="card"><div class="card-body"><h6>Unread Messages</h6><div class="kpi-value" id="unread-messages">0</div></div></div></div>
    </div>
    <div class="row g-3 mt-1">
        <div class="col-12 col-xl-7">
            <div class="card"><div class="card-header">Referrals Received</div><div class="card-body table-responsive"><table class="table table-sm"><thead><tr><th>Job</th><th>Referrer</th><th>Status</th><th>Timeline</th></tr></thead><tbody id="referrals-body"></tbody></table></div></div>
        </div>
        <div class="col-12 col-xl-5">
            <div class="card"><div class="card-header">Referral Requests Sent</div><div class="card-body" id="requests-list"></div></div>
            <div class="card mt-3"><div class="card-header">Notifications</div><div class="card-body" id="notifications-list"></div></div>
        </div>
    </div>
    <div class="mt-3" id="chat-root"></div>
`;

mountChat("chat-root");

async function loadProfileCompletion() {
    const profile = me.profile || {};
    const checks = [profile.phone, profile.location, profile.skills?.length, profile.experienceYears >= 0, profile.linkedinUrl, profile.aboutMe, profile.resumeUrl, profile.profilePhotoUrl];
    const score = Math.round((checks.filter(Boolean).length / checks.length) * 100);
    const bar = document.getElementById("profile-progress");
    bar.style.width = `${score}%`;
    bar.textContent = `${score}%`;
}

async function loadReferrals() {
    const referrals = await api.referrals.received();
    document.getElementById("active-referrals").textContent = referrals.length;
    const tbody = document.getElementById("referrals-body");
    tbody.innerHTML = referrals.map((r) => `
        <tr>
            <td>${r.jobTitle || "-"}</td>
            <td>${r.referrerName || "-"}</td>
            <td>${statusBadge(r.status)}</td>
            <td><div id="tl-${r.id}"></div></td>
        </tr>
    `).join("") || `<tr><td colspan="4" class="text-muted">No referrals yet</td></tr>`;
    referrals.forEach((r) => renderTimeline(document.getElementById(`tl-${r.id}`), TIMELINE, r.status));
}

async function loadRequestsSent() {
    const requests = await api.referralRequests.sent();
    document.getElementById("requests-list").innerHTML = requests.map((r) => `
        <div class="d-flex justify-content-between border-bottom py-2">
            <div><strong>${r.jobTitle || "-"}</strong><div class="small text-muted">To: ${r.employeeName || "-"}</div></div>
            <div>${statusBadge(r.status)}</div>
        </div>
    `).join("") || `<div class="text-muted">No referral requests sent.</div>`;
}

async function loadNotifications() {
    const data = await api.notifications.list(0, 8);
    const notifications = data.content || [];
    document.getElementById("notifications-list").innerHTML = notifications.map((n) => `
        <div class="border-bottom py-2">
            <div class="fw-semibold">${n.title}</div>
            <div class="small text-muted">${n.body}</div>
            <div class="small text-secondary">${formatDate(n.createdAt)}</div>
        </div>
    `).join("") || `<div class="text-muted">No notifications.</div>`;
}

async function loadUnreadCounts() {
    const [notifCount, conversations] = await Promise.all([api.notifications.unreadCount(), api.chat.recent()]);
    setUnreadCount(notifCount || 0);
    recentConversations = conversations || [];
    const unreadMessages = recentConversations.filter((m) => !m.isRead && String(m.receiverId) === String(me.id)).length;
    document.getElementById("unread-messages").textContent = unreadMessages;
    const users = recentConversations.map((c) => ({
        id: String(c.senderId) === String(me.id) ? c.receiverId : c.senderId,
        name: String(c.senderId) === String(me.id) ? c.receiverName : c.senderName
    }));
    renderChatUsers(users);
    if (users.length) {
        selectedPartnerId = users[0].id;
        await loadConversation(selectedPartnerId);
    }
}

async function loadConversation(partnerId) {
    const page = await api.chat.conversation(partnerId);
    renderChatMessages(page.content || [], me.id);
    await api.chat.markRead(partnerId);
}

document.addEventListener("change", async (e) => {
    if (e.target.id === "chat-user-select") {
        selectedPartnerId = e.target.value;
        await loadConversation(selectedPartnerId);
    }
});

document.addEventListener("click", async (e) => {
    if (e.target.id === "chat-send") {
        const input = document.getElementById("chat-input");
        if (!input.value.trim() || !selectedPartnerId) return;
        sendChatMessage(selectedPartnerId, input.value.trim());
        input.value = "";
    }
});

async function init() {
    await Promise.all([loadProfileCompletion(), loadReferrals(), loadRequestsSent(), loadNotifications(), loadUnreadCounts()]);
    await connectWebSocket({
        onMessage: async () => {
            if (selectedPartnerId) await loadConversation(selectedPartnerId);
            await loadUnreadCounts();
            await loadNotifications();
        }
    });
    setInterval(loadUnreadCounts, 30000);
}

init().catch((e) => console.error(e));
