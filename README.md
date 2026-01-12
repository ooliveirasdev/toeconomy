# 💰 ToEconomy — Plugin de Economia para PaperMC 1.8.x

**ToEconomy** é um plugin de economia desenvolvido em **Java** para servidores **PaperMC 1.8.x**, criado com o objetivo de ser **simples, funcional e evolutivo**, servindo tanto para uso prático quanto como **projeto de portfólio**.

O projeto foca no gerenciamento de saldos dos jogadores e na aplicação de boas práticas em Java, além da evolução gradual no uso de **bancos de dados relacionais**.

---

## ✨ Funcionalidades

### 💵 Sistema de Economia
- Gerenciamento de saldo por jogador
- Saldo persistente em banco de dados
- Suporte a transferências entre jogadores

### 📊 Banco de Dados
- Atualmente utiliza **SQLite**
- Estrutura preparada para futuras migrações (ex: MySQL)
- Ideal para demonstrar evolução no uso de bancos relacionais

### 🧩 Simples e Extensível
- Código organizado e modular
- Fácil de expandir com novas funcionalidades
- Pensado para evolução contínua

---

## ⌨️ Comandos

### `/money`
Comando principal de gerenciamento de economia.

**Subcomandos:**
- `/money` → Mostra o saldo do jogador
- `/money add <jogador> <valor>` → Adiciona dinheiro
- `/money remove <jogador> <valor>` → Remove dinheiro
- `/money set <jogador> <valor>` → Define o saldo

### `/pay <jogador> <valor>`
- Transfere dinheiro para outro jogador
- Valida saldo e valores inválidos

---

## 🔐 Permissões

| Permissão | Descrição |
|---------|----------|
| `toeconomy.basic` | Permite usar `/money` e `/pay` |
| `toeconomy.admin` | Permite usar os subcomandos administrativos do `/money` |

---

## 🛠️ Tecnologias Utilizadas

- **Java**
- **PaperMC 1.8.x**
- **SQLite**
- JDBC
- Estrutura orientada a objetos

---

## 📂 Estrutura do Projeto (exemplo)

```bash
📁 ToEconomy
├── 📁 src/main/java/com.toplugins.toeconomy/
│   ├── 📁 commands
│   ├── 📁 listeners
│   ├── DatabaseWorker.java
│   ├── EconomyService.java
|   ├── SQLiteDb.java
│   └── Main.java
├── 📄 plugin.yml
└── 📄 README.md
