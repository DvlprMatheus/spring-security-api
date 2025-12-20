# Spring Security API - Projeto Base

## 📋 Sobre o Projeto

Este é um projeto base completo de API REST desenvolvido em **Spring Boot** com **Spring Security** totalmente configurado e pronto para uso. O projeto implementa um sistema robusto de autenticação e autorização baseado em **JWT (JSON Web Tokens)**, servindo como template inicial para outros projetos que necessitam de funcionalidades de segurança.

### 🎯 Objetivo

Fornecer uma base sólida e pronta para uso com:
- Autenticação JWT completa
- Gerenciamento de usuários e roles
- Tratamento de exceções global
- Validação de dados
- Testes unitários e de integração
- Logging estruturado
- Migrações de banco de dados com Flyway

## 🛠️ Tecnologias e Dependências

### Stack Principal

- **Java 21** - Linguagem de programação
- **Spring Boot 4.0.1** - Framework principal
- **Spring Security** - Framework de segurança
- **Spring Data JPA** - Abstração de acesso a dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **PostgreSQL** - Banco de dados relacional
- **Flyway** - Controle de versão de banco de dados
- **JWT (JJWT 0.12.5)** - Geração e validação de tokens
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validação de dados

### Dependências de Produção

```gradle
// Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-flyway'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-webmvc'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'

// Database
implementation 'org.flywaydb:flyway-database-postgresql'
runtimeOnly 'org.postgresql:postgresql'

// Lombok
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'

// Development
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

### Dependências de Teste

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.boot:spring-boot-test-autoconfigure'
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
testImplementation 'org.springframework.boot:spring-boot-starter-flyway-test'
testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
testImplementation 'org.junit.platform:junit-platform-suite-api'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

## 📁 Estrutura do Projeto

```
src/main/java/com/dvlprmatheus/security/
├── api/
│   ├── controller/          # Controllers REST
│   │   ├── AuthenticationController.java
│   │   └── UserController.java
│   ├── exception/            # Exceções customizadas e handler global
│   │   ├── AuthenticationFailedException.java
│   │   ├── EmailAlreadyExistsException.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── UsernameAlreadyExistsException.java
│   ├── request/             # DTOs de requisição
│   │   ├── LoginRequest.java
│   │   └── RegisterRequest.java
│   └── response/            # DTOs de resposta
│       ├── AuthResponse.java
│       ├── ErrorResponse.java
│       └── TestResponse.java
├── config/
│   ├── jwt/                 # Configurações JWT
│   │   └── JwtProperties.java
│   └── security/            # Configurações de segurança
│       ├── SecurityConfig.java
│       └── filter/
│           └── JwtAuthenticationFilter.java
├── entity/                  # Entidades JPA
│   ├── AbstractEntity.java
│   ├── Role.java
│   └── User.java
├── repository/              # Repositórios Spring Data JPA
│   ├── RoleRepository.java
│   └── UserRepository.java
├── service/                  # Camada de serviços
│   ├── AuthenticationService.java
│   └── UserService.java
└── utils/                    # Utilitários
    └── JwtUtil.java
```

## 🔐 Funcionalidades de Segurança

### Autenticação JWT

- **Geração de Tokens**: Tokens JWT são gerados após login bem-sucedido
- **Validação Automática**: Filtro customizado valida tokens em cada requisição
- **Expiração Configurável**: Tokens com tempo de expiração configurável (padrão: 24 horas)
- **Extração de Claims**: Extração segura de informações do token

### Gerenciamento de Usuários

- **Registro de Usuários**: Endpoint para criação de novos usuários
- **Login**: Autenticação com username e senha
- **Validação de Credenciais**: Validação robusta de dados de entrada
- **Prevenção de Duplicatas**: Verificação de username e email únicos

### Autorização Baseada em Roles

- **Sistema de Roles**: Suporte a múltiplas roles por usuário
- **Many-to-Many**: Relacionamento flexível entre usuários e roles
- **Autorities**: Integração com Spring Security Authorities

## 🔧 Configurações

### Application Properties

O projeto utiliza `application.yaml` com as seguintes configurações principais:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgresdb:5432/${DB_NAME:postgres}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    hikari:
      maximum-pool-size: 10
  
  jpa:
    hibernate:
      ddl-auto: none  # Flyway gerencia o schema
    show-sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    out-of-order: true

jwt:
  expiration: 86400000  # 24 horas em milissegundos
  secret: ${JWT_SECRET:...}  # Configurar via variável de ambiente
```

### Variáveis de Ambiente

- `DB_NAME`: Nome do banco de dados (padrão: postgres)
- `DB_USER`: Usuário do banco (padrão: postgres)
- `DB_PASSWORD`: Senha do banco (padrão: postgres)
- `JWT_SECRET`: Chave secreta para assinatura JWT (obrigatório em produção)

## 🧪 Testes

### Estrutura de Testes

O projeto possui uma suíte completa de testes utilizando **JUnit 5** e **Mockito**:

#### Testes Unitários

- **AuthenticationServiceTest**: Testa a lógica de negócio de autenticação
  - Registro de usuários
  - Validação de duplicatas
  - Login com credenciais válidas/inválidas
  - Geração de tokens JWT

- **JwtUtilTest**: Testa utilitários de JWT
  - Geração de tokens
  - Extração de claims
  - Validação de tokens
  - Verificação de expiração

#### Testes de Integração

- **AuthenticationControllerTest**: Testa endpoints de autenticação
  - POST `/auth/register` - Registro de usuários
  - POST `/auth/login` - Login de usuários
  - Validação de requisições
  - Tratamento de erros

- **UserControllerTest**: Testa endpoints protegidos
  - GET `/v1/user/test` - Verificação de autenticação
  - Comportamento com/sem autenticação

- **SecurityApplicationTests**: Teste de contexto Spring
  - Verificação de carregamento do contexto

### Executando os Testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes com relatório
./gradlew test --info

# Executar apenas testes unitários
./gradlew test --tests "*Test"

# Executar apenas testes de integração
./gradlew test --tests "*ControllerTest"
```

## 📡 Endpoints da API

### Autenticação (Públicos)

#### POST `/auth/register`
Registra um novo usuário no sistema.

**Request Body:**
```json
{
  "username": "usuario123",
  "email": "usuario@example.com",
  "password": "senha123456"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario123"
}
```

#### POST `/auth/login`
Autentica um usuário e retorna um token JWT.

**Request Body:**
```json
{
  "username": "usuario123",
  "password": "senha123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario123"
}
```

### Usuário (Protegidos)

#### GET `/v1/user/test`
Endpoint de teste para verificar autenticação.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "Authentication working correctly!",
  "username": "usuario123"
}
```

## 🚨 Tratamento de Exceções

O projeto implementa um **GlobalExceptionHandler** que centraliza o tratamento de todas as exceções:

### Exceções Customizadas

- `AuthenticationFailedException`: Falha na autenticação
- `EmailAlreadyExistsException`: Email já cadastrado
- `UsernameAlreadyExistsException`: Username já cadastrado
- `ResourceNotFoundException`: Recurso não encontrado

### Respostas de Erro Padronizadas

Todas as exceções retornam um formato padronizado:

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error in provided data",
  "path": "/auth/register",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

## 📝 Logging

O projeto utiliza **SLF4J com Lombok** para logging estruturado:

- **@Slf4j**: Anotação do Lombok que gera automaticamente o logger
- **Níveis de Log**:
  - `log.info()`: Operações importantes (registro, login)
  - `log.debug()`: Informações detalhadas para debug
  - `log.warn()`: Avisos (credenciais inválidas, tokens inválidos)
  - `log.error()`: Erros inesperados

### Exemplo de Logs

```
INFO  - Attempting to register new user with username: usuario123
DEBUG - User created successfully with ID: 1
INFO  - User registered successfully: usuario123
WARN  - Login failed: invalid credentials for user: usuario123
```

## 🗄️ Banco de Dados

### Migrações Flyway

O projeto utiliza **Flyway** para versionamento do banco de dados:

- `V1__create_roles_table.sql`: Criação da tabela de roles
- `V2__create_users_table.sql`: Criação da tabela de usuários
- `V3__create_user_roles_table.sql`: Tabela de relacionamento usuário-role

### Modelo de Dados

- **User**: Entidade principal de usuário
  - Campos: id, username, email, password, createdAt, updatedAt
  - Relacionamento Many-to-Many com Role

- **Role**: Entidade de permissões
  - Campos: id, name, createdAt, updatedAt
  - Relacionamento Many-to-Many com User

## 🔒 Segurança Implementada

### Spring Security Configuration

- **JWT Authentication Filter**: Filtro customizado para validação de tokens
- **Password Encoding**: Uso de BCrypt para hash de senhas
- **CORS**: Configuração de Cross-Origin Resource Sharing
- **CSRF**: Proteção CSRF configurada
- **Session Management**: Gerenciamento de sessão stateless

### Validações

- **Bean Validation**: Validação de dados de entrada
- **Custom Validators**: Validações específicas de negócio
- **Constraint Violations**: Tratamento de violações de constraints

## 🚀 Como Usar Este Projeto Base

### 1. Clonar e Configurar

```bash
git clone <repository-url>
cd spring-security-api
```

### 2. Configurar Variáveis de Ambiente

Crie um arquivo `.env` ou configure as variáveis:

```bash
export DB_NAME=seu_banco
export DB_USER=seu_usuario
export DB_PASSWORD=sua_senha
export JWT_SECRET=sua_chave_secreta_aleatoria
```

### 3. Executar Migrações

As migrações Flyway são executadas automaticamente na inicialização.

### 4. Iniciar a Aplicação

```bash
./gradlew bootRun
```

### 5. Testar os Endpoints

```bash
# Registrar usuário
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","email":"teste@example.com","password":"senha123"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teste","password":"senha123"}'

# Endpoint protegido
curl -X GET http://localhost:8080/v1/user/test \
  -H "Authorization: Bearer <token>"
```

## 📦 Extensibilidade

Este projeto base pode ser facilmente estendido com:

- **Novos Endpoints**: Adicionar controllers na pasta `api/controller`
- **Novas Entidades**: Criar entidades em `entity` e repositórios em `repository`
- **Novos Serviços**: Implementar serviços em `service`
- **Novas Roles**: Adicionar roles no banco e configurar autorização
- **Novas Validações**: Criar validators customizados
- **Novos Filtros**: Adicionar filtros de segurança

## 📄 Licença

Este projeto está sob licença. Consulte o arquivo `LICENSE` para mais detalhes.

## 👥 Contribuindo

Este é um projeto base destinado a ser usado como template. Sinta-se livre para:

1. Fazer fork do projeto
2. Criar uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abrir um Pull Request

## 📚 Recursos Adicionais

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/) - Documentação sobre JWT
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Flyway Documentation](https://flywaydb.org/documentation/)

---
