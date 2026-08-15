# Piersen HR

Plataforma de autoatendimento de RH: o **RH** administra o ciclo de vida do funcionário
(admissão, alteração contratual, aprovações e desligamento) e o **Funcionário** acessa seu
próprio portal para bater ponto, pedir férias, enviar atestados e consultar holerites.

Projeto da disciplina de **DevOps** — AcademicBit.

## Stack

| Camada | Tecnologia |
|---|---|
| API | Java 17 · Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| Banco | MySQL 8 |
| Frontend | HTML · CSS · JavaScript (fetch + JWT, sem framework) |
| Testes | JUnit 5 · Mockito · AssertJ · JaCoCo |
| Build | Maven |
| CI | Azure Pipelines · GitHub Actions |

## Pré-requisitos

- **JDK 17** ou superior
- **MySQL 8** instalado localmente + **MySQL Workbench**

Não é necessário instalar o Maven: o projeto usa o **Maven Wrapper** (`mvnw`), que baixa a
versão correta na primeira execução.

## Como executar

### 1. Preparar o banco no MySQL Workbench

Abra o MySQL Workbench, conecte na instância local (`localhost:3306`) com o usuário `root`
e execute o script [`database/setup.sql`](database/setup.sql).

Ele cria o schema `piersen_hr` e o usuário `piersen` / `piersen` usado pela aplicação.
As tabelas são criadas automaticamente pelo Hibernate na primeira execução da API
(`spring.jpa.hibernate.ddl-auto=update`) — depois de subir a aplicação, rode `SHOW TABLES;`
no Workbench para conferir.

**Enquanto esse script não for executado, a API não sobe** — o erro será
`Access denied for user 'piersen'@'localhost'`.

Se preferir usar o próprio `root` em vez de criar o usuário `piersen`, rode apenas o
`CREATE DATABASE` do script e suba a API com as credenciais do root:

```powershell
$env:PIERSEN_DB_USER = "root"
$env:PIERSEN_DB_PASSWORD = "<sua senha do root>"
.\mvnw.cmd spring-boot:run
```

### 2. Subir a API

Windows (PowerShell):

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Acesse **http://localhost:8080**.

## Acessos da base demo

Na primeira inicialização, o sistema cria uma base populada com funcionários,
marcações de ponto, solicitações pendentes e holerites — para que qualquer pessoa
que clone o repositório encontre o sistema utilizável.

**Senha de todos os usuários demo:** `Piersen@2026`

| Perfil | E-mail | Cargo |
|---|---|---|
| RH | `carla.menezes.rh@gmail.com` | Analista de RH Senior |
| Funcionário | `pedro.alves92@hotmail.com` | Desenvolvedor Backend |
| Funcionário | `juliana.prado@outlook.com` | Analista Financeiro |
| Funcionário | `rafael.nogueira.dev@gmail.com` | Desenvolvedor Frontend |
| Funcionário | `mariana.castro88@yahoo.com.br` | Designer de Produto |
| Funcionário | `thiago.barbosa@uol.com.br` | Analista de Suporte |
| Desligado | `larissa.fontes@bol.com.br` | Assistente Administrativo |

`larissa.fontes@bol.com.br` está DESLIGADA — use essa conta para demonstrar o bloqueio de login
com o histórico preservado no banco.

Para subir com a base vazia: `PIERSEN_SEED_ENABLED=false`.

## Telas

| Página | Descrição |
|---|---|
| `/index.html` | Login (redireciona conforme o papel) |
| `/rh.html` | Módulo do RH — quadro, admissão, contrato, conferência de ponto, aprovações, holerites |
| `/portal.html` | Portal do Funcionário — ponto, férias, atestados, holerites, dados pessoais, avisos |

## Testes

```powershell
cd backend
.\mvnw.cmd test
```

**52 testes, todos passando:**

- **46 testes unitários** das regras de negócio, com Mockito e relógio fixo
  (`EmployeeService`, `AuthService`, `TimePunchService`, `VacationService`,
  `CertificateService`, `PayslipService`);
- **6 testes de integração** (`PiersenHrSmokeTest`) que sobem a aplicação inteira em banco
  H2 e validam login, carga da base demo, bloqueio do funcionário desligado, separação de
  papéis, marcação de ponto e entrega do frontend.

Os testes não dependem do MySQL — rodam isolados e podem ser executados no pipeline de CI.

Relatório de cobertura: `backend/target/site/jacoco/index.html` (gerado por `mvn verify`).

## Estrutura

```
.
├── azure-pipelines.yml          Pipeline de CI do Azure DevOps
├── database/setup.sql           Script para rodar no MySQL Workbench
├── .github/workflows/ci.yml     Pipeline de CI do GitHub Actions
├── backend/
│   ├── mvnw / mvnw.cmd          Maven Wrapper (dispensa instalar o Maven)
│   ├── Dockerfile               Empacotamento da API (opcional, usado no CI)
│   ├── pom.xml
│   └── src/
│       ├── main/java/br/com/academicbit/piersen/
│       │   ├── config/          Clock e carga da base demo
│       │   ├── controller/      Endpoints REST
│       │   ├── domain/          Entidades JPA e enums
│       │   ├── dto/             Records de entrada e saída
│       │   ├── exception/       Exceções de negócio e handler HTTP
│       │   ├── repository/      Spring Data JPA
│       │   ├── security/        JWT, filtro e configuração
│       │   └── service/         Regras de negócio
│       ├── main/resources/static/   Frontend (HTML, CSS, JS)
│       └── test/java/...            Testes unitários
└── docs/
    ├── api.md                   Referência dos endpoints
    ├── regras-de-negocio.md     Regras do Piersen HR e onde estão implementadas
    └── backlog-azure-boards.md  Backlog Scrum para cadastrar no Azure Boards
```

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `PIERSEN_DB_URL` | `jdbc:mysql://localhost:3306/piersen_hr` | URL do MySQL local |
| `PIERSEN_DB_USER` | `piersen` | Usuário do banco |
| `PIERSEN_DB_PASSWORD` | `piersen` | Senha do banco |
| `PIERSEN_JWT_SECRET` | chave de desenvolvimento | Segredo de assinatura do JWT |
| `PIERSEN_JWT_EXPIRATION` | `480` | Validade do token em minutos |
| `PIERSEN_SEED_ENABLED` | `true` | Carrega a base demo na primeira execução |
| `PIERSEN_PORT` | `8080` | Porta da aplicação |

> Os valores padrão servem para desenvolvimento local. Em qualquer ambiente
> compartilhado, defina `PIERSEN_JWT_SECRET` e as credenciais do banco por variável de ambiente.

## Documentação

- [Regras de negócio](docs/regras-de-negocio.md)
- [Referência da API](docs/api.md)
- [Backlog para o Azure Boards](docs/backlog-azure-boards.md)
