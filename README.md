# HelpHealth - App de Intercambialidade de Medicamentos

Este projeto é um aplicativo de resposta rápida projetado para uso no balcão de farmácias. Ele guia a tomada de decisão do usuário (compra de medicamentos genéricos ou similares equivalentes) com segurança clínica, operando 100% offline por meio de dados oficiais do governo (Anvisa / CMED / Ministério da Saúde).

---

## 🏗️ Arquitetura do Sistema

O projeto é estruturado como um monorepo contendo o motor de consolidação e ingestão de dados (Backend) e a interface móvel multiplataforma (Mobile App).

```
HelpHealth/
├── backend/                              # Motor de Ingestão e Geração SQLite (Spring Boot + Maven)
│   ├── src/main/java/com/duartefilipe/helphealth/backend/
│   │   ├── model/                        # Entidades JPA (Fabricante, Medicamento, PrecoCmed)
│   │   └── repository/                   # Repositórios de dados
│   └── pom.xml
│
├── mobile/                               # Aplicativo Multiplataforma (Kotlin Multiplatform + Gradle)
│   ├── shared/                           # Lógica de Negócios & Telas Compartilhadas (Compose Multiplatform)
│   ├── androidApp/                       # Invólucro do Aplicativo Android (Jetpack Compose)
│   ├── iosApp/                           # Invólucro do Aplicativo iOS (SwiftUI)
│   └── settings.gradle.kts
│
└── documento_projeto_anvisa_app.pdf      # Especificação Técnica Oficial do Projeto
```

---

## 🛠️ Tecnologias Utilizadas

### Backend (Ingestão & Consolidação)
* **Java 21** & **Spring Boot 3.3+**
* **Maven** para gerenciamento de dependências
* **Spring Data JPA** & **PostgreSQL** para o banco de dados principal
* **OpenCSV** para leitura e processamento mensal de planilhas de dados abertos
* **SQLite JDBC Driver** para compilação e exportação automatizada da base do aplicativo para arquivo SQLite compactado (`.db.gz`)

### Mobile (Interface & Lógica Offline)
* **Kotlin Multiplatform (KMP)**
* **Compose Multiplatform** para compartilhamento da interface (UI) entre Android e iOS
* **SQLDelight 2.0.1** para consultas SQL locais de altíssima performance (Zero Latência)
* **ConnectivityObserver** para alteração dinâmica de comportamento Online / Offline (Bulário, Farmácia Popular, etc.)

---

## 🗄️ Modelagem do Banco de Dados (PostgreSQL / SQLite)

### 1. Fabricantes (`fabricantes`)
* `cnpj` (VARCHAR(14), PK): CNPJ do laboratório.
* `razao_social` (VARCHAR(255), Not Null): Razão social.
* `nome_fantasia` (VARCHAR(255)): Nome fantasia.

### 2. Medicamentos (`medicamentos`)
* `id` (SERIAL, PK): Identificador único do registro.
* `ean` (VARCHAR(14), Unique): Código de barras do produto.
* `nome_comercial` (VARCHAR(150), Not Null): Nome comercial de marca.
* `principio_ativo` (TEXT, Not Null): Fórmula ou princípio ativo do medicamento.
* `concentracao` (VARCHAR(100)): Concentração da substância.
* `forma_farmaceutica` (VARCHAR(100)): Ex: Comprimido, Xarope, Solução Injetável.
* `categoria_regulatoria` (VARCHAR(50)): Referência, Genérico ou Similar.
* `tarja` (VARCHAR(50)): Tarja Vermelha, Preta, etc.
* `retencao_receita` (BOOLEAN): Define se exige controle especial.
* `precisa_refrigeracao` (BOOLEAN): Requer refrigeração (termolábeis).
* `link_bula_paciente` (TEXT): URL oficial da bula na Anvisa.
* `faz_parte_farmacia_popular` (BOOLEAN): Disponível no programa do governo.
* `cnpj_fabricante` (FK): Vinculado à tabela de Fabricantes.
* `status_registro` (VARCHAR(20)): Ativo, Cancelado, etc.

### 3. Preços CMED (`precos_cmed`)
* `id` (SERIAL, PK): Identificador do preço.
* `ean` (FK): Vinculado ao medicamento.
* `uf` (VARCHAR(2), Not Null): Estado brasileiro da alíquota de ICMS.
* `pmc_zero_icms` (DECIMAL): Preço Máximo ao Consumidor sem imposto.
* `pmc_18_icms` (DECIMAL): Preço Máximo ao Consumidor com ICMS (ex: alíquota padrão SP).

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java SDK 21**
* **Maven 3.9+** (ou Maven wrapper incluso)
* **PostgreSQL** rodando localmente ou remotamente

### Iniciando o Backend
1. Navegue até o diretório do backend:
   ```bash
   cd backend
   ```
2. Configure as credenciais do seu banco de dados PostgreSQL no arquivo `src/main/resources/application.properties`.
3. Compile e execute o servidor Spring Boot:
   ```bash
   ./mvnw spring-boot:run
   ```

### Executando o Aplicativo Móvel (Gradle)
1. Navegue até o diretório mobile:
   ```bash
   cd mobile
   ```
2. Sincronize as dependências e compile o projeto:
   ```bash
   ./gradlew help
   ```
3. Para executar no emulador Android:
   ```bash
   ./gradlew :androidApp:installDebug
   ```
4. Para abrir o iOSApp no Xcode, navegue até a pasta `iosApp` e abra o projeto utilizando o Xcode.
