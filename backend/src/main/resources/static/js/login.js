const redirectByRole = (role) => {
    window.location.href = role === "RH" ? "/rh.html" : "/portal.html";
};

const submitLogin = async (event) => {
    event.preventDefault();
    try {
        const session = await api("/api/auth/login", {
            method: "POST",
            body: { email: document.getElementById("email").value, password: document.getElementById("password").value }
        });
        saveSession(session);
        redirectByRole(session.role);
    } catch (error) {
        feedback("login-feedback", error.message, false);
    }
};

document.getElementById("login-form").addEventListener("submit", submitLogin);
