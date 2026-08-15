const session = requireRole("FUNCIONARIO");

document.getElementById("user-name").textContent = session ? session.name : "";

const loadProfile = async () => {
    const me = await api("/api/auth/me");
    document.getElementById("stat-saldo").textContent = me.vacationBalanceDays;
    document.getElementById("stat-cargo").textContent = me.position;
    document.getElementById("pd-phone").value = me.phone || "";
    document.getElementById("pd-address").value = me.address || "";
    document.getElementById("pd-photo").value = me.photoUrl || "";
    document.getElementById("pd-position").value = me.position;
    document.getElementById("pd-department").value = me.department;
    document.getElementById("pd-salary").value = money(me.salary);
};

const loadTimeSheet = async () => {
    const sheet = await api("/api/time-punches/me/timesheet");
    document.getElementById("stat-horas").textContent = hours(sheet.workedMinutes);
    render("timesheet-table", sheet.punches.length ? [`<table>
        <thead><tr><th>Horario</th><th>Tipo</th><th>Status</th></tr></thead>
        <tbody>${sheet.punches.map((punch) => `<tr><td>${dateTime(punch.punchedAt)}</td><td>${punch.type}</td><td>${tag(punch.status)}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhuma marcacao registrada hoje.");
};

const punch = async (type) => {
    try {
        await api("/api/time-punches", { method: "POST", body: { type } });
        feedback("punch-feedback", `Marcacao de ${type} registrada e enviada para conferencia.`, true);
        await loadTimeSheet();
    } catch (error) {
        feedback("punch-feedback", error.message, false);
    }
};

const loadVacations = async () => {
    const requests = await api("/api/vacations/me");
    render("vacations-table", requests.length ? [`<table>
        <thead><tr><th>Inicio</th><th>Fim</th><th>Dias</th><th>Status</th><th>Observacao</th></tr></thead>
        <tbody>${requests.map((request) => `<tr><td>${date(request.startDate)}</td><td>${date(request.endDate)}</td><td>${request.daysRequested}</td><td>${tag(request.status)}</td><td>${request.decisionNote || "-"}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhuma solicitacao de ferias registrada.");
};

const requestVacation = async (event) => {
    event.preventDefault();
    try {
        await api("/api/vacations", {
            method: "POST",
            body: { startDate: document.getElementById("vc-start").value, endDate: document.getElementById("vc-end").value }
        });
        feedback("vacation-feedback", "Solicitacao enviada para aprovacao do RH.", true);
        document.getElementById("vacation-form").reset();
        await Promise.all([loadVacations(), loadProfile()]);
    } catch (error) {
        feedback("vacation-feedback", error.message, false);
    }
};

const loadCertificates = async () => {
    const certificates = await api("/api/certificates/me");
    render("certificates-table", certificates.length ? [`<table>
        <thead><tr><th>Ausencia</th><th>Dias</th><th>Status</th><th>Observacao</th></tr></thead>
        <tbody>${certificates.map((certificate) => `<tr><td>${date(certificate.absenceDate)}</td><td>${certificate.daysOff}</td><td>${tag(certificate.status)}</td><td>${certificate.decisionNote || "-"}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhum atestado enviado.");
};

const submitCertificate = async (event) => {
    event.preventDefault();
    try {
        await api("/api/certificates", {
            method: "POST",
            body: {
                absenceDate: document.getElementById("ce-date").value,
                daysOff: Number(document.getElementById("ce-days").value),
                documentUrl: document.getElementById("ce-url").value
            }
        });
        feedback("certificate-feedback", "Atestado enviado para analise do RH.", true);
        document.getElementById("certificate-form").reset();
        await loadCertificates();
    } catch (error) {
        feedback("certificate-feedback", error.message, false);
    }
};

const loadPayslips = async () => {
    const payslips = await api("/api/payslips/me");
    render("payslips-table", payslips.length ? [`<table>
        <thead><tr><th>Competencia</th><th>Bruto</th><th>Descontos</th><th>Liquido</th></tr></thead>
        <tbody>${payslips.map((payslip) => `<tr><td>${payslip.referenceMonth}</td><td>${money(payslip.grossSalary)}</td><td>${money(payslip.deductions)}</td><td>${money(payslip.netSalary)}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhum holerite disponivel.");
};

const savePersonalData = async (event) => {
    event.preventDefault();
    try {
        await api("/api/employees/me/personal-data", {
            method: "PUT",
            body: {
                phone: document.getElementById("pd-phone").value,
                address: document.getElementById("pd-address").value,
                photoUrl: document.getElementById("pd-photo").value
            }
        });
        feedback("personal-feedback", "Dados pessoais atualizados.", true);
        await loadProfile();
    } catch (error) {
        feedback("personal-feedback", error.message, false);
    }
};

const loadNotifications = async () => {
    const notifications = await api("/api/notifications/me");
    render("notifications-list", notifications.length ? [`<table>
        <thead><tr><th>Data</th><th>Mensagem</th><th>Status</th></tr></thead>
        <tbody>${notifications.map((notification) => `<tr><td>${dateTime(notification.createdAt)}</td><td>${notification.message}</td><td>${notification.read ? "lido" : "novo"}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhum aviso recebido.");
    document.getElementById("stat-avisos").textContent = notifications.filter((notification) => !notification.read).length;
};

const markNotificationsRead = async () => {
    await api("/api/notifications/me/read", { method: "PUT" });
    await loadNotifications();
};

const bootstrap = async () => {
    try {
        await Promise.all([loadProfile(), loadTimeSheet(), loadVacations(), loadCertificates(), loadPayslips(), loadNotifications()]);
    } catch (error) {
        feedback("punch-feedback", error.message, false);
    }
};

document.getElementById("vacation-form").addEventListener("submit", requestVacation);
document.getElementById("certificate-form").addEventListener("submit", submitCertificate);
document.getElementById("personal-form").addEventListener("submit", savePersonalData);

bootstrap();
