# Regras de Negócio — Piersen HR

O sistema divide as permissões em dois papéis: **RH (Administrador)** e **Funcionário (Usuário Final)**.

```
                  ┌─────────────────────────────────┐
                  │        PIERSEN HR SYSTEM        │
                  └───────────────┬─────────────────┘
                                  │
         ┌────────────────────────┴────────────────────────┐
         ▼                                                 ▼
  [ MÓDULO DO RH ]                               [ PORTAL DO FUNCIONÁRIO ]
  • Admissão & Desligamento                      • Marcação de Ponto
  • Alteração Salarial/Cargo                     • Solicitação de Férias
  • Aprovações & Relatórios                      • Consulta de Holerite/Dados
```

## 1. Admissão e liberação de acesso (RH ➔ Funcionário)

| Regra | Implementação |
|---|---|
| O RH cadastra CPF, e-mail, cargo, salário e data de admissão | `POST /api/employees` — `AdmissionRequest` |
| CPF e e-mail são únicos | `EmployeeService.admit` valida antes de gravar |
| A data de admissão não pode ser futura | `EmployeeService.admit` |
| O sistema cria a conta e envia as credenciais por e-mail | `PasswordGenerator` + `CredentialMailer` |
| O funcionário inicia com status ATIVO e papel FUNCIONARIO | `EmployeeService.admit` |
| Saldo inicial de férias de 30 dias | `INITIAL_VACATION_BALANCE_DAYS` |

## 2. Atividades diárias do funcionário (Funcionário ➔ Sistema)

| Regra | Implementação |
|---|---|
| Registro de entradas, saídas e intervalos | `POST /api/time-punches` |
| O sistema valida a sequência das marcações | `TimePunchService.validateSequence` |
| Toda marcação vai para conferência do RH | status inicial `REGISTRADO` |
| O funcionário atualiza apenas telefone, endereço e foto | `PUT /api/employees/me/personal-data` |
| O funcionário **não** altera salário, cargo ou departamento | `PersonalDataRequest` não expõe esses campos |
| Consulta de holerites e saldo de férias | `GET /api/payslips/me`, `GET /api/auth/me` |

## 3. Gestão de pedidos e aprovações (Funcionário ⇄ RH)

| Regra | Implementação |
|---|---|
| Solicitação de férias entra na fila do RH como PENDENTE | `VacationService.request` |
| Período mínimo de 5 dias | `MINIMUM_DAYS` |
| Antecedência mínima de 15 dias | `MINIMUM_NOTICE_DAYS` |
| Não pode exceder o saldo de férias | `VacationService.request` |
| Não pode haver períodos sobrepostos | consulta de sobreposição no repositório |
| Envio de atestados com documento anexado | `POST /api/certificates` |
| Aprovação do atestado abona as horas no espelho de ponto | `CertificateService.waivePunches` |
| A decisão do RH notifica o funcionário no portal | `NotificationService.notify` |
| Uma solicitação já avaliada não pode ser reavaliada | `VacationService.decide` / `CertificateService.decide` |

## 4. Desligamento e revogação de acesso (RH ➔ Ex-Funcionário)

| Regra | Implementação |
|---|---|
| O desligamento é um **soft delete** (status DESLIGADO) | `EmployeeService.terminate` |
| O login é bloqueado imediatamente | `AuthService.login` verifica `isActive()` |
| Não são aceitos novos pontos ou solicitações | `EmployeeService.requireActive` |
| A data de desligamento não pode anteceder a admissão | `EmployeeService.terminate` |
| O registro permanece no banco para histórico e relatórios | nenhum `delete` é executado |

## 5. Alteração de cargo e salário

| Regra | Implementação |
|---|---|
| Apenas o RH altera cargo e salário | `EmployeeService.changeContract` valida `isHr()` |
| Cada alteração gera histórico contratual | entidade `ContractChange` |
| Não é possível alterar contrato de desligado | `EmployeeService.changeContract` |
| O funcionário é notificado da mudança | `NotificationService.notify` |

## 6. Holerite

| Regra | Implementação |
|---|---|
| Desconto de INSS de 11% sobre o bruto | `PayslipService.deductionsFor` |
| IRRF de 7,5% apenas sobre base acima de R$ 2.259,20 | `PayslipService.deductionsFor` |
| Um holerite por competência por funcionário | `PayslipService.generate` |
| Competência não pode anteceder a admissão | `PayslipService.generate` |
