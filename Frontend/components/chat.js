import { formatDate } from "../utils/formatters.js";

export function mountChat(containerId) {
    const root = document.getElementById(containerId);
    root.innerHTML = `
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <strong>Messages</strong>
                <select id="chat-user-select" class="form-select form-select-sm w-auto"></select>
            </div>
            <div class="card-body">
                <div id="chat-box" class="chat-box mb-3"></div>
                <div class="input-group">
                    <input id="chat-input" class="form-control" placeholder="Type a message...">
                    <button id="chat-send" class="btn btn-primary">Send</button>
                </div>
            </div>
        </div>
    `;
}

export function renderChatUsers(users) {
    const select = document.getElementById("chat-user-select");
    if (!select) return;
    select.innerHTML = users.map((u) => `<option value="${u.id}">${u.name}</option>`).join("");
}

export function renderChatMessages(messages, meId) {
    const box = document.getElementById("chat-box");
    if (!box) return;
    box.innerHTML = messages.map((m) => `
        <div class="chat-message ${String(m.senderId) === String(meId) ? "me" : ""}">
            <div class="bubble">
                <div>${m.content}</div>
                <small class="text-muted">${formatDate(m.createdAt)}</small>
            </div>
        </div>
    `).join("");
    box.scrollTop = box.scrollHeight;
}
