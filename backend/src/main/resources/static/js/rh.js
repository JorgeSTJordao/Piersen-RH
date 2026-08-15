const session = requireRole("RH");

document.getElementById("user-name").textContent = session ? session.name : "";

let employees = [];

const employeeRow = (employee) => `<tr>
    <td>${employee.name}</td>
    <td>${employee.position}</td>
    <td>${employee.department}</td>
    <td>${money(employee.salary)}</td>
    <td>${date(employee.admissionDate)}</td>
    <td>${date(employee.terminationDate)}</td>
    <td>${tag(employee.status)}</td>
    <td>${employee.status === "ATIVO" ? `<button class="danger small" onclick="terminate(${employee.id})">Desligar</button>` : ""}</td>
</tr>`;

const employeesTable = () => `<table>
    <thead><tr><th>Nome</th><th>Cargo</th><th>Departamento</th><th>Salario</th><th>Admissao</th><th>Desligamento</th><th>Status</th><th></th></tr></thead>
    <tbody>${employees.map(employeeRow).join("")}</tbody>
</table>`;

const fillEmployeeSelects = () => {
    const options = employees.filter((employee) => employee.status === "ATIVO")
        .map((employee) => `<option value="${employee.id}">${employee.name} - ${employee.position}</option>`).join("");
    document.getElementById("ct-employee").innerHTML = options;
    document.getElementById("ps-employee").innerHTML = options;
};

const loadEmployees = async () => {
    employees = await api("/api/employees");
    render("employees-table", [employeesTable()], "Nenhum funcionario cadastrado.");
    document.getElementById("stat-total").textContent = employees.length;
    document.getElementById("stat-ativos").textContent = employees.filter((employee) => employee.status === "ATIVO").length;
    document.getElementById("stat-desligados").textContent = employees.filter((employee) => employee.status === "DESLIGADO").length;
    fillEmployeeSelects();
};

const terminate = async (employeeId) => {
    try {
        await api(`/api/employees/${employeeId}`, { method: "DELETE" });
        feedback("quadro-feedback", "Funcionario desligado. Acesso ao portal revogado e historico preservado.", true);
        await loadEmployees();
    } catch (error) {
        feedback("quadro-feedback", error.message, false);
    }
};

const admit = async (event) => {
    event.preventDefault();
    try {
        const created = await api("/api/employees", {
            method: "POST",
            body: {
                name: document.getElementById("adm-name").value,
                cpf: document.getElementById("adm-cpf").value,
                email: document.getElementById("adm-email").value,
                position: document.getElementById("adm-position").value,
                department: document.getElementById("adm-department").value,
                salary: Number(document.getElementById("adm-salary").value),
                admissionDate: document.getElementById("adm-date").value
            }
        });
        feedback("admission-feedback", `${created.name} admitido. Credenciais enviadas para ${created.email}.`, true);
        document.getElementById("admission-form").reset();
        await loadEmployees();
    } catch (error) {
        feedback("admission-feedback", error.message, false);
    }
};

const changeContract = async (event) => {
    event.preventDefault();
    try {
        const updated = await api(`/api/employees/${document.getElementById("ct-employee").value}/contract`, {
            method: "PUT",
            body: {
                position: document.getElementById("ct-position").value,
                salary: Number(document.getElementById("ct-salary").value)
            }
        });
        feedback("contract-feedback", `Contrato de ${updated.name} atualizado para ${updated.position}.`, true);
        await loadEmployees();
    } catch (error) {
        feedback("contract-feedback", error.message, false);
    }
};

const punchRow = (punch) => `<tr>
    <td>#${punch.employeeId}</td>
    <td>${dateTime(punch.punchedAt)}</td>
    <td>${punch.type}</td>
    <td>${tag(punch.status)}</td>
    <td>
        <button class="primary small" onclick="reviewPunch(${punch.id}, 'CONFERIDO')">Conferir</button>
        <button class="ghost small" onclick="reviewPunch(${punch.id}, 'ABONADO')">Abonar</button>
    </td>
</tr>`;

const loadPunches = async () => {
    const punches = await api("/api/time-punches/pending");
    render("punches-table", punches.length ? [`<table>
        <thead><tr><th>Funcionario</th><th>Marcacao</th><th>Tipo</th><th>Status</th><th></th></tr></thead>
        <tbody>${punches.map(punchRow).join("")}</tbody></table>`] : [],
        "Nenhuma marcacao aguardando conferencia.");
};

const reviewPunch = async (punchId, status) => {
    try {
        await api(`/api/time-punches/${punchId}/review?status=${status}`, { method: "PUT" });
        feedback("punch-feedback", `Marcacao atualizada para ${status}.`, true);
        await loadPunches();
    } catch (error) {
        feedback("punch-feedback", error.message, false);
    }
};

const vacationRow = (request) => `<tr>
    <td>${request.employeeName}</td>
    <td>${date(request.startDate)}</td>
    <td>${date(request.endDate)}</td>
    <td>${request.daysRequested}</td>
    <td>${tag(request.status)}</td>
    <td>
        <button class="primary small" onclick="decideVacation(${request.id}, true)">Aprovar</button>
        <button class="danger small" onclick="decideVacation(${request.id}, false)">Recusar</button>
    </td>
</tr>`;

const loadVacations = async () => {
    const requests = await api("/api/vacations/pending");
    render("vacations-table", requests.length ? [`<table>
        <thead><tr><th>Funcionario</th><th>Inicio</th><th>Fim</th><th>Dias</th><th>Status</th><th></th></tr></thead>
        <tbody>${requests.map(vacationRow).join("")}</tbody></table>`] : [],
        "Nenhuma solicitacao de ferias pendente.");
    document.getElementById("stat-pendencias").textContent = requests.length;
};

const decideVacation = async (requestId, approved) => {
    try {
        await api(`/api/vacations/${requestId}/decision`, {
            method: "PUT",
            body: { approved, note: approved ? "Aprovado pelo RH" : "Recusado pelo RH" }
        });
        feedback("vacation-feedback", `Solicitacao ${approved ? "aprovada" : "recusada"} e funcionario notificado.`, true);
        await loadVacations();
    } catch (error) {
        feedback("vacation-feedback", error.message, false);
    }
};

const certificateRow = (certificate) => `<tr>
    <td>${certificate.employeeName}</td>
    <td>${date(certificate.absenceDate)}</td>
    <td>${certificate.daysOff}</td>
    <td><a href="${certificate.documentUrl}" target="_blank" rel="noopener">documento</a></td>
    <td>${tag(certificate.status)}</td>
    <td>
        <button class="primary small" onclick="decideCertificate(${certificate.id}, true)">Aprovar e abonar</button>
        <button class="danger small" onclick="decideCertificate(${certificate.id}, false)">Recusar</button>
    </td>
</tr>`;

const loadCertificates = async () => {
    const certificates = await api("/api/certificates/pending");
    render("certificates-table", certificates.length ? [`<table>
        <thead><tr><th>Funcionario</th><th>Ausencia</th><th>Dias</th><th>Anexo</th><th>Status</th><th></th></tr></thead>
        <tbody>${certificates.map(certificateRow).join("")}</tbody></table>`] : [],
        "Nenhum atestado aguardando analise.");
};

const decideCertificate = async (certificateId, approved) => {
    try {
        await api(`/api/certificates/${certificateId}/decision`, {
            method: "PUT",
            body: { approved, note: approved ? "Horas abonadas" : "Documento recusado" }
        });
        feedback("certificate-feedback", `Atestado ${approved ? "aprovado" : "recusado"}.`, true);
        await loadCertificates();
    } catch (error) {
        feedback("certificate-feedback", error.message, false);
    }
};

const generatePayslip = async (event) => {
    event.preventDefault();
    const employeeId = document.getElementById("ps-employee").value;
    try {
        await api(`/api/payslips/employees/${employeeId}?reference=${document.getElementById("ps-reference").value}`,
            { method: "POST" });
        feedback("payslip-feedback", "Holerite gerado e disponivel no portal do funcionario.", true);
        await loadPayslips(employeeId);
    } catch (error) {
        feedback("payslip-feedback", error.message, false);
    }
};

const loadPayslips = async (employeeId) => {
    const payslips = await api(`/api/payslips/employees/${employeeId}`);
    render("payslips-table", payslips.length ? [`<table>
        <thead><tr><th>Competencia</th><th>Bruto</th><th>Descontos</th><th>Liquido</th></tr></thead>
        <tbody>${payslips.map((payslip) => `<tr><td>${payslip.referenceMonth}</td><td>${money(payslip.grossSalary)}</td><td>${money(payslip.deductions)}</td><td>${money(payslip.netSalary)}</td></tr>`).join("")}</tbody>
        </table>`] : [], "Nenhum holerite emitido para este funcionario.");
};

const bootstrap = async () => {
    try {
        await Promise.all([loadEmployees(), loadPunches(), loadVacations(), loadCertificates()]);
    } catch (error) {
        feedback("quadro-feedback", error.message, false);
    }
};

document.getElementById("admission-form").addEventListener("submit", admit);
document.getElementById("contract-form").addEventListener("submit", changeContract);
document.getElementById("payslip-form").addEventListener("submit", generatePayslip);

bootstrap();
