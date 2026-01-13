### 💰 ToEconomy — Plugin de Economia para PaperMC 1.8x a 1.21x

**ToEconomy** é um plugin de economia desenvolvido em **Java** para servidores **PaperMC 1.8.x**, criado com o objetivo de ser **simples, funcional e evolutivo**, servindo tanto para uso prático quanto como **projeto de portfólio**.

O projeto foca no gerenciamento de saldos dos jogadores e na aplicação de boas práticas em Java, além da evolução gradual no uso de **bancos de dados relacionais**.

---

## ✨ Funcionalidades

### 💵 Sistema de Economia
- Gerenciamento de saldo por jogador
- Saldo persistente em banco de dados
- Suporte a transferências entre jogadores

### 📊 Banco de Dados
- Estrutura preparada para o uso de SQLite & MySQL
- Otimização nativa reforçada
- Configuração simples pelo config.yml

### 🧩 Simples e Extensível
- Código organizado e modular
- Fácil de expandir com novas funcionalidades
- Pensado para evolução contínua

### ✅ Otimizado e Universal
- Código otimizado e compativel com outros plugins
- Integração com o Vault e PlaceholderAPI
- Otimização reforçada aos bancos de dados

---

## ⌨️ Comandos

### `/money`
Comando principal de gerenciamento de economia.

**Subcomandos:**
- `/money` → Mostra o saldo do jogador
- `/money adicionar <jogador> <valor>` → Adiciona dinheiro
- `/money deminuir <jogador> <valor>` → Remove dinheiro
- `/money definir <jogador> <valor>` → Define o saldo

### `/pay <jogador> <valor>`
- Transfere dinheiro para outro jogador
- Valida saldo e valores inválidos

---

## 🔐 Permissões

| Permissão | Descrição |
|---------|----------|
| `toeconomy.basic` | Permite usar `/money` e `/pay` |
| `toeconomy.admin` | Permite usar os subcomandos administrativos do `/money` |


## 🧩 Placeholders
| Placeholder | Descrição |
|---------|----------|
| `%toeconomy_balance%` | Retorna o saldo bruto Ex: `1500.0` |
| `%toeconomy_formatted_balance%` | Retorna o saldo formatado Ex: `1.5K` |

- Obs: Caso utilize o Vault e a placeholder dele não precisará mudar caso use saldo bruto.

---

## 🛠️ Tecnologias Utilizadas

- **Java**
- **PaperMC 1.8x a 1.21x**
- **SQLite**
- **MySQL**
- JDBC
- Estrutura orientada a objetos
