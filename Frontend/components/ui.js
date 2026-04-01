export function kpiCard(title, value) {
    return `
        <div class="card">
            <div class="card-body">
                <div class="small text-muted">${title}</div>
                <div class="kpi-value">${value}</div>
            </div>
        </div>
    `;
}

export function simpleTable(headers, rows, emptyText = "No data.") {
    const head = headers.map((h) => `<th>${h}</th>`).join("");
    const body = rows.length
        ? rows.map((r) => `<tr>${r.map((c) => `<td>${c}</td>`).join("")}</tr>`).join("")
        : `<tr><td colspan="${headers.length}" class="text-muted">${emptyText}</td></tr>`;
    return `<table class="table table-sm"><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
}

export function modal(id, title, body = "") {
    return `
        <div class="modal fade" id="${id}" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">${title}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">${body}</div>
                </div>
            </div>
        </div>
    `;
}
