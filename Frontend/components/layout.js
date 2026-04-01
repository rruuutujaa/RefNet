import { storage } from "../services/storage.js";

export function renderLayout({ title, role, activeKey, sidebarItems }) {
    const app = document.getElementById("app");
    app.innerHTML = `
        <div class="container-fluid rf-layout">
            <div class="row">
                <aside class="col-12 col-lg-2 p-3 rf-sidebar">
                    <h5 class="fw-bold">RefNet</h5>
                    <div class="small text-secondary-emphasis mb-3">${role}</div>
                    <nav class="nav flex-column gap-1 mb-3">
                        ${sidebarItems.map((i) => `<a class="nav-link ${i.key === activeKey ? "active" : ""}" href="${i.href}">${i.label}</a>`).join("")}
                    </nav>
                    <button id="logout-btn" class="btn btn-outline-light btn-sm w-100">Logout</button>
                </aside>
                <main class="col-12 col-lg-10 rf-main p-3 p-lg-4">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h1 class="h4 mb-0">${title}</h1>
                        <span id="notif-pill" class="badge text-bg-danger d-none">0 unread</span>
                    </div>
                    <div id="page-content"></div>
                </main>
            </div>
        </div>
    `;

    document.getElementById("logout-btn")?.addEventListener("click", () => {
        storage.clear();
        window.location.href = "../index.html";
    });
}

export function setUnreadCount(count) {
    const pill = document.getElementById("notif-pill");
    if (!pill) return;
    if (count > 0) {
        pill.textContent = `${count} unread`;
        pill.classList.remove("d-none");
    } else {
        pill.classList.add("d-none");
    }
}

export function renderTimeline(container, statuses, currentStatus) {
    const activeIndex = statuses.indexOf(currentStatus);
    container.innerHTML = statuses.map((status, idx) => {
        const cls = idx < activeIndex ? "done" : idx === activeIndex ? "current" : "";
        return `<span class="timeline-step ${cls}"><span class="dot"></span>${status.replaceAll("_", " ")}</span>`;
    }).join("");
}
