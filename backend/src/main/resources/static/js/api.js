const TOKEN_KEY = "piersen.token";
const SESSION_KEY = "piersen.session";

const saveSession = (session) => {
    localStorage.setItem(TOKEN_KEY, session.token);
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
};

const getToken = () => localStorage.getItem(TOKEN_KEY);

const getSession = () => JSON.parse(localStorage.getItem(SESSION_KEY) || "null");

const clearSession = () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(SESSION_KEY);
};

const logout = () => {
    clearSession();
    window.location.href = "/index.html";
};

const requireRole = (role) => {
    const session = getSession();
    if (!session || session.role !== role) {
        logout();
    }
    return session;
};

const api = async (path, options = {}) => {
    const response = await fetch(path, {
        method: options.method || "GET",
        headers: Object.assign({ "Content-Type": "application/json" },
            getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
        body: options.body ? JSON.stringify(options.body) : undefined
    });
    if (response.status === 401 || response.status === 403) {
        const problem = await response.json().catch(() => ({}));
        throw new Error(problem.message || "Acesso negado");
    }
    if (!response.ok) {
        const problem = await response.json().catch(() => ({}));
        throw new Error(problem.message || `Erro ${response.status}`);
    }
    if (response.status === 204 || response.headers.get("content-length") === "0") {
        return null;
    }
    return response.json().catch(() => null);
};

const feedback = (elementId, message, ok) => {
    const box = document.getElementById(elementId);
    box.textContent = message;
    box.className = `feedback show ${ok ? "ok" : "error"}`;
};

const money = (value) => Number(value || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

const date = (value) => (value ? new Date(value).toLocaleDateString("pt-BR") : "-");

const dateTime = (value) => (value ? new Date(value).toLocaleString("pt-BR") : "-");

const tag = (value) => `<span class="tag ${value}">${value}</span>`;

const hours = (minutes) => `${Math.floor(minutes / 60)}h${String(minutes % 60).padStart(2, "0")}`;

const render = (elementId, rows, emptyMessage) => {
    const target = document.getElementById(elementId);
    if (!rows.length) {
        target.innerHTML = `<p class="empty">${emptyMessage}</p>`;
        return;
    }
    target.innerHTML = rows.join("");
};

const showPanel = (name) => {
    document.querySelectorAll(".panel").forEach((panel) => panel.classList.toggle("active", panel.id === `panel-${name}`));
    document.querySelectorAll(".sidebar button").forEach((button) => button.classList.toggle("active", button.dataset.panel === name));
};
