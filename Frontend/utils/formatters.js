export function formatDate(dateStr) {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleString();
}

export function toPercent(value, total) {
    if (!total) return "0%";
    return `${Math.round((value / total) * 100)}%`;
}

export function statusBadge(status) {
    const map = {
        PENDING: "warning",
        ACCEPTED: "success",
        DECLINED: "danger",
        SUBMITTED: "info",
        UNDER_REVIEW: "primary",
        HIRED: "success",
        NOT_SELECTED: "secondary"
    };
    const tone = map[status] || "secondary";
    return `<span class="badge text-bg-${tone}">${String(status || "UNKNOWN").replaceAll("_", " ")}</span>`;
}
