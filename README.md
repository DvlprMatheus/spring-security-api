# Spring Security API

API REST de autenticação e cadastro de usuários, construída com Spring Boot e Spring Security. A aplicação oferece cadastro e login com JWT, proteção de rotas e persistência em PostgreSQL. O ambiente de desenvolvimento é preparado para uso em Dev Container.

## Funcionalidades

- Cadastro de usuários com validação de username, e-mail e senha.
- Login com username e senha e emissão de token JWT.
- Autenticação stateless por meio de um filtro JWT.
- Senhas armazenadas com hash BCrypt.
- Estrutura de usuários e roles com relacionamento muitos-para-muitos.
- Tratamento centralizado de erros e respostas de validação.
- Migrações de banco de dados com Flyway e carga inicial das roles `ADMIN` e `USER`.
- Testes automatizados com banco H2, sem dependência de um PostgreSQL para a suíte de testes.
- Pipeline de CI com build, testes, verificação de formatação e envio do grafo de dependências.

## Tecnologias

- Java 21
- Spring Boot 4.0.2
- Spring Security, Spring Web MVC e Spring Data JPA
- PostgreSQL 16 e Flyway
- JWT com JJWT 0.12.5
- Gradle Wrapper
- JUnit 5, Mockito e H2
- Spotless com Google Java Format
- Docker e Dev Containers

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/com/dvlprmatheus/security/
│   │   ├── api/
│   │   │   ├── controller/       # Endpoints REST
│   │   │   ├── exception/        # Exceções e tratamento global
│   │   │   ├── request/          # DTOs de requisição
│   │   │   └── response/         # DTOs de resposta
│   │   ├── config/
│   │   │   ├── jwt/              # Configurações JWT
│   │   │   └── security/         # Segurança e filtro de autenticação
│   │   ├── entity/               # Entidades JPA
│   │   ├── repository/           # Acesso a dados
│   │   ├── service/              # Autenticação e carregamento de usuários
│   │   └── utils/                # Utilitários JWT
│   └── resources/
│       ├── db/migration/         # Migrações Flyway
│       └── application.yaml      # Configuração da aplicação
└── test/
    ├── java/                     # Testes unitários e de integração
    └── resources/
        └── application-test.yaml # Configuração de testes com H2
```

## Pré-requisitos

- Visual Studio Code com a extensão Dev Containers.
- Docker Desktop ou outro ambiente compatível com Docker Compose.

O Dev Container fornece o ambiente Java e inicia o serviço PostgreSQL definido em `.devcontainer/docker-compose.yml`. Para abrir o ambiente, abra o repositório no VS Code e selecione **Reopen in Container**. Os comandos abaixo devem ser executados no terminal do container.

## Configuração

As configurações de conexão e JWT podem ser definidas por variáveis de ambiente. Os valores padrão abaixo correspondem à configuração de desenvolvimento.

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `POSTGRES_URL` | `jdbc:postgresql://postgres:5432/spring-security-db` | URL JDBC do PostgreSQL no Dev Container. |
| `POSTGRES_USER` | `postgres` | Usuário do banco. |
| `POSTGRES_PASSWORD` | `postgres` | Senha do banco. |
| `JWT_SECRET` | Valor de desenvolvimento definido em `application.yaml` | Chave usada para assinar e validar tokens. |

O token expira após 24 horas. O valor padrão de `JWT_SECRET` é apenas para desenvolvimento: configure uma chave própria, forte e com pelo menos 32 bytes em qualquer ambiente compartilhado ou de produção. As migrações Flyway são executadas na inicialização da aplicação.

## Executar a aplicação

No terminal do Dev Container, na raiz do repositório:

```bash
./gradlew bootRun
```

A API ficará disponível em `http://localhost:8080`.

## Endpoints

### Registrar usuário

`POST /auth/register` — público; retorna `201 Created`.

```bash
curl -i -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario123","email":"usuario@example.com","password":"senha123"}'
```

Username deve ter entre 3 e 50 caracteres, o e-mail deve ser válido e ter no máximo 100 caracteres, e a senha deve conter pelo menos 6 caracteres. Username e e-mail precisam ser únicos.

### Fazer login

`POST /auth/login` — público; retorna `200 OK`.

```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario123","password":"senha123"}'
```

O cadastro e o login retornam um objeto com o token, o tipo `Bearer` e o username:

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "username": "usuario123"
}
```

### Verificar autenticação

`GET /v1/user/test` — requer autenticação; retorna `200 OK` quando recebe um token válido.

```bash
curl -i http://localhost:8080/v1/user/test \
  -H "Authorization: Bearer <jwt>"
```

Resposta:

```json
{
  "message": "Authentication working correctly!",
  "username": "usuario123"
}
```

A configuração de segurança libera os caminhos `/auth/**`, `/health` e `/public/**`; os demais requerem autenticação. O token deve ser enviado no cabeçalho `Authorization` com o prefixo `Bearer`.

## Respostas de erro

As exceções da aplicação e erros de validação são tratados globalmente. As respostas incluem timestamp, status HTTP, mensagem e caminho; erros de validação também incluem `fieldErrors`.

```json
{
  "timestamp": "2026-11-25T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error in provided data",
  "path": "/auth/register",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "email-invalido"
    }
  ]
}
```

Entre os status tratados pela API estão `400 Bad Request` para dados inválidos, `401 Unauthorized` para falhas de autenticação, `403 Forbidden` para contas bloqueadas ou desabilitadas e `409 Conflict` para username ou e-mail já cadastrados.

## Banco de dados

O Flyway gerencia o schema e executa as migrações em `src/main/resources/db/migration`:

- `V0001`: cria a tabela de roles.
- `V0002`: cria a tabela de usuários.
- `V0003`: cria o relacionamento entre usuários e roles.
- `V0004`: insere as roles iniciais `ADMIN` e `USER`.

## Testes e formatação

Execute a suíte de testes:

```bash
./gradlew test
```

Os testes usam H2 em modo compatível com PostgreSQL e configuração isolada em `src/test/resources/application-test.yaml`.

Verifique ou aplique a formatação Java:

```bash
./gradlew spotlessCheck
./gradlew spotlessApply
```

O workflow do GitHub Actions é executado em pushes e pull requests direcionados à branch `main`. Ele executa `./gradlew build`, `./gradlew spotlessCheck` e, em um job separado, envia o grafo de dependências do Gradle.

## Licença

Consulte o arquivo [LICENSE](./LICENSE) para informações sobre a licença do projeto.
