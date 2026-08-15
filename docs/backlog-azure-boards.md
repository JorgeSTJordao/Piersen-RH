# Backlog do Piersen HR — Azure Boards (Scrum)

Referência para cadastrar o backlog no Azure Boards junto com a equipe.
Processo do projeto: **Scrum**. Hierarquia usada: `Epic → Feature → Product Backlog Item → Task`.

```
Epic
└── Feature
    └── Product Backlog Item
        └── Task
```

---

## Epic

**Título:** Piersen HR — Autoatendimento de RH e Funcionários

**Descrição:**
Disponibilizar uma plataforma onde o RH gerencia o ciclo de vida do funcionário
(admissão, alteração contratual e desligamento) e o funcionário realiza suas
atividades diárias (ponto, férias, atestados e consulta de holerite) em autoatendimento.

---

## Features

| Feature | Valor entregue |
|---|---|
| Admissão e Liberação de Acesso | Cadastrar o funcionário e criar automaticamente o acesso ao portal |
| Atividades Diárias do Funcionário | Registrar ponto, atualizar dados básicos e consultar informações |
| Gestão de Pedidos e Aprovações | Solicitar férias, enviar atestados e obter decisão do RH |
| Desligamento e Revogação de Acesso | Encerrar o contrato bloqueando o acesso e preservando o histórico |

---

## Product Backlog Items

```
Admissão e Liberação de Acesso
├── Cadastrar funcionário
├── Receber credenciais de acesso
└── Consultar quadro de funcionários

Atividades Diárias do Funcionário
├── Registrar ponto
├── Consultar espelho de ponto
└── Atualizar dados pessoais

Gestão de Pedidos e Aprovações
├── Solicitar férias
├── Enviar atestado médico
└── Aprovar ou recusar solicitações

Desligamento e Revogação de Acesso
├── Desligar funcionário
└── Consultar histórico de ex-funcionários
```

---

## PBI detalhado — "Registrar ponto"

**História do usuário**

```
Como funcionário do Piersen HR,
quero registrar minhas entradas, saídas e intervalos,
para que minha jornada seja apurada corretamente pelo RH.
```

**Critérios de aceitação**

1. Somente funcionário com status ATIVO consegue registrar ponto.
2. A primeira marcação do dia deve ser ENTRADA.
3. O sistema recusa sequências inválidas (ex.: duas entradas seguidas, saída sem entrada).
4. Toda marcação nasce com status REGISTRADO e vai para conferência do RH.
5. O espelho do dia apresenta as marcações e o total de horas trabalhadas, descontando o intervalo.

**Campos sugeridos** — Effort: 5 · Business Value: 90 · Tags: `piersen; ponto; mvp` · State: New

---

## PBI detalhado — "Solicitar férias"

**História do usuário**

```
Como funcionário do Piersen HR,
quero solicitar meu período de férias pelo portal,
para que o RH possa avaliar e aprovar sem troca de e-mails.
```

**Critérios de aceitação**

1. O período mínimo é de 5 dias.
2. A solicitação exige no mínimo 15 dias de antecedência.
3. O período solicitado não pode exceder o saldo de férias disponível.
4. Não é permitido sobrepor um período já solicitado ou aprovado.
5. A solicitação entra como PENDENTE e o funcionário é notificado quando o RH decide.

**Campos sugeridos** — Effort: 8 · Business Value: 80 · Tags: `piersen; ferias; mvp` · State: New

---

## Tasks técnicas (exemplo para "Registrar ponto")

```
PBI — Registrar ponto
├── [Backend] Modelar o domínio TimePunch e o enum PunchType
├── [Backend] Implementar POST /api/time-punches com validação de sequência
├── [Backend] Criar testes unitários do TimePunchService
├── [Frontend] Criar tela de marcação e espelho de ponto
├── [Frontend] Integrar o portal com a API via fetch/JWT
├── [Cloud] Criar Dockerfile para empacotar a API
├── [DevOps] Criar repositório e política de branches
└── [DevOps] Configurar pipeline de integração contínua
```

Tag por disciplina: `backend`, `frontend`, `cloud`, `devops`.
Estimativa em horas apenas nas Tasks (campo Remaining Work), nunca duplicando o Effort do PBI.

---

## Checklist de conclusão

- [ ] O projeto usa o processo Scrum.
- [ ] Existe um Epic com a visão do Piersen HR.
- [ ] Existem ao menos quatro Features.
- [ ] Os PBIs estão escritos sob a perspectiva do usuário.
- [ ] Ao menos um PBI possui critérios de aceitação verificáveis.
- [ ] As Tasks representam trabalho técnico e possuem tags de disciplina.
- [ ] A hierarquia está visível no Backlog (View options → Parents).
