# API — Piersen HR

Base: `http://localhost:8080`
Autenticação: `Authorization: Bearer <token>` (JWT obtido no login).

## Autenticação

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/auth/login` | público | Autentica e devolve o token |
| GET | `/api/auth/me` | autenticado | Dados do usuário logado |

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"carla.menezes.rh@gmail.com","password":"Piersen@2026"}'
```

## Funcionários

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/employees` | RH | Admite funcionário e gera credenciais |
| GET | `/api/employees` | RH | Lista o quadro (`?status=ATIVO`/`DESLIGADO`) |
| GET | `/api/employees/{id}` | RH | Consulta um funcionário |
| DELETE | `/api/employees/{id}` | RH | Desligamento (soft delete) |
| PUT | `/api/employees/{id}/contract` | RH | Altera cargo e salário |
| PUT | `/api/employees/me/personal-data` | autenticado | Atualiza telefone, endereço e foto |

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Bruno Tavares","cpf":"71029384756","email":"bruno.tavares.dev@gmail.com",
       "position":"Analista de QA","department":"Tecnologia","salary":5200.00,
       "admissionDate":"2026-08-01"}'
```

## Ponto

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/time-punches` | funcionário | Registra `ENTRADA`, `INICIO_INTERVALO`, `FIM_INTERVALO` ou `SAIDA` |
| GET | `/api/time-punches/me` | funcionário | Histórico de marcações |
| GET | `/api/time-punches/me/timesheet` | funcionário | Espelho do dia (`?day=2026-08-14`) |
| GET | `/api/time-punches/pending` | RH | Marcações aguardando conferência |
| PUT | `/api/time-punches/{id}/review` | RH | Confere ou abona (`?status=CONFERIDO`/`ABONADO`) |

## Férias

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/vacations` | funcionário | Solicita período |
| GET | `/api/vacations/me` | funcionário | Minhas solicitações |
| GET | `/api/vacations/pending` | RH | Fila de aprovação |
| PUT | `/api/vacations/{id}/decision` | RH | Aprova ou recusa |

## Atestados

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/certificates` | funcionário | Envia atestado |
| GET | `/api/certificates/me` | funcionário | Meus atestados |
| GET | `/api/certificates/pending` | RH | Fila de análise |
| PUT | `/api/certificates/{id}/decision` | RH | Aprova (abona horas) ou recusa |

## Holerites e avisos

| Método | Rota | Papel | Descrição |
|---|---|---|---|
| POST | `/api/payslips/employees/{id}?reference=2026-07` | RH | Gera holerite |
| GET | `/api/payslips/employees/{id}` | RH | Holerites de um funcionário |
| GET | `/api/payslips/me` | funcionário | Meus holerites |
| GET | `/api/notifications/me` | autenticado | Avisos recebidos |
| PUT | `/api/notifications/me/read` | autenticado | Marca todos como lidos |

## Códigos de erro

| Status | Situação |
|---|---|
| 400 | Falha de validação do payload |
| 403 | Acesso negado, credenciais inválidas ou funcionário desligado |
| 404 | Recurso não encontrado |
| 422 | Violação de regra de negócio |
