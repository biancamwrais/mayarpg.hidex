# Maya RPG - Projeto Interdisciplinar 3ADS

Sistema completo (App Android + Backend Node.js + Banco MySQL) para a Clínica Maya Yoshiko Yamamoto.

## Estrutura

```
maya-rpg/
├── android-app/      # App Android (Java) - paciente
├── backend/          # API Node.js + Express
├── admin-web/        # Site admin React (próxima fase)
├── database/         # Scripts SQL
└── docs/             # Documentação
```

---

## Passo 1 — Instalar pré-requisitos no Windows

1. **Node.js LTS** → https://nodejs.org (versão 20+)
2. **MySQL Community Server + Workbench** → https://dev.mysql.com/downloads/installer/
   - Use "Developer Default" e anote a senha do `root`
3. **Android Studio** → você já tem ✅

---

## Passo 2 — Configurar o Banco de Dados

1. Abra o **MySQL Workbench**
2. Conecte na instância local (Local instance MySQL)
3. Abra o arquivo `database/schema.sql`
4. Execute (ícone de raio ⚡ ou Ctrl+Shift+Enter)
5. Verifique no painel esquerdo: deve aparecer o schema `maya_rpg`

---

## Passo 3 — Subir o Backend

```bash
cd backend
npm install
copy .env.example .env       # no Windows
# edite o .env e coloque sua senha do MySQL em DB_PASSWORD

# Antes do primeiro login, gere os hashes de senha:
node scripts/gerarHashes.js
# Copie os UPDATEs gerados e cole no MySQL Workbench. Execute.

# Inicie o servidor:
npm start
# Acesse: http://localhost:3000  -> deve responder JSON
```

### Usuários de teste:
| E-mail | Senha | Perfil |
|---|---|---|
| `maya@clinica.com` | `maya123` | Profissional |
| `admin@clinica.com` | `admin123` | Admin |
| `maria.santos@email.com` | `paciente123` | Paciente |

---

## Passo 4 — Rodar o App Android

1. Abra o **Android Studio**
2. **File → Open** → selecione a pasta `android-app/`
3. Aguarde o Gradle sincronizar (pode levar alguns minutos da primeira vez)
4. Conecte um celular via USB **OU** crie um emulador (Tools → Device Manager → Create Device)
5. Clique em **Run** (▶️)

### IMPORTANTE — IP da API
O arquivo `android-app/app/src/main/java/com/mayarpg/app/network/ApiClient.java` usa:

- `http://10.0.2.2:3000/api/` → para **emulador** (10.0.2.2 é o localhost da sua máquina visto pelo emulador)
- Se for usar **celular físico**, troque para o IP da sua máquina na rede local, ex: `http://192.168.0.10:3000/api/`. Descubra com `ipconfig` no terminal.

### Testando o login
- Abra o app
- Faça login com `maria.santos@email.com` / `paciente123`
- A Home deve mostrar 4 exercícios, dor média, e a consulta agendada para amanhã

---

## O que está implementado

### ✅ Backend (100%)
Todos os endpoints REST que o app e o site admin precisam.

### ✅ Banco (100%)
11 tabelas + dados de seed + view do dashboard.

### ✅ App Android — Telas prontas:
- **Splash** (decide se vai pro Login ou Home conforme sessão JWT)
- **Login** — autentica via API, salva token
- **Cadastro** — cria conta paciente com validação LGPD e máscaras (CPF/Telefone/Data)
- **Home (Dashboard)** — saudação, card de tratamento com estatísticas (exercícios/hoje/dor média), categorias horizontais, ações rápidas, próximas consultas
- **MainActivity com Bottom Navigation** (5 abas)

### ⏳ App Android — Telas a implementar (próximas mensagens):
- Meus Exercícios + Bottom Sheet "Registrar Execução"
- Histórico (gráfico MPAndroidChart)
- Notificações (lista com swipe-to-dismiss)
- Agendamento (radio service + grid horários + form)
- Perfil (cards expansíveis + tema noturno + histórico de pagamentos)

### ⏳ Site Admin React (próxima fase)

---

## Atende aos requisitos do PI?

| Requisito (Programação Mobile - Entrega 1) | Status |
|---|---|
| Múltiplas Activities (Splash, Login, Cadastro, Main) | ✅ |
| Uso de Intents | ✅ (Login → Main, etc.) |
| Uso de Fragments | ✅ (Home + 4 stubs) |
| ConstraintLayout | ✅ |
| TextView, ImageView, Button | ✅ |
| Visualização do plano de exercícios | ⏳ próxima fase |

| Requisito (Entrega 2) | Status |
|---|---|
| Consumo de API REST | ✅ Retrofit |
| Autenticação | ✅ JWT |
| Tratamento de JSON | ✅ Gson |
| SQLite local | ⏳ próxima fase (Room) |
| Notificações | ⏳ próxima fase (FCM) |
| Registro de execução (check-in) | ⏳ próxima fase |
| Sincronização com backend | ✅ pronta no backend |

---

## Solução de problemas

**"Falha de conexão" no login do app**
- Confira se o backend está rodando (`npm start` mostra "API Maya RPG rodando")
- Confira o IP no `ApiClient.java`
- Se for emulador, deve ser `10.0.2.2` (não `localhost`)

**"Access denied for user 'root'" no backend**
- Verifique a senha em `.env`

**"Senha incorreta" no login mesmo com a senha certa**
- Você não rodou o `node scripts/gerarHashes.js` — os hashes do schema são placeholders
