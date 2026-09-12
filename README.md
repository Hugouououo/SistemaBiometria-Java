# Sistema de Identificação e Autenticação Biométrica

Projeto acadêmico desenvolvido em Java para a APS do 6º Semestre da UNIP

ferramenta de identificação e autenticação biométrica, com objeto de
análise obtido diretamente de **arquivos de imagem**.

## Como compilar e executar

**Pré-requisito**: JDK 17 ou superior instalado (`javac` disponível no PATH).

### Opção 1: Scripts automáticos (recomendado)

#### Linux / macOS
```bash
chmod +x compilar-linux.sh
./compilar-linux.sh
```

#### Windows (PowerShell)
```powershell
# Pode ser necessário permitir execução de scripts:
# Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

.\compilar-windows.ps1
```

#### Windows (cmd.exe)
```cmd
compilar-windows.bat
```

### Opção 2: Compilação manual

#### Linux / macOS / PowerShell
```bash
# a partir da raiz do projeto (onde está a pasta src/)
mkdir -p bin
find src -name "*.java" > sources.txt
javac -d bin -encoding UTF-8 @sources.txt
rm sources.txt
java -cp bin biometria.Main
```

#### Windows (cmd.exe)
```cmd
REM a partir da raiz do projeto
mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin -encoding UTF-8 @sources.txt
del sources.txt
java -cp bin biometria.Main
```

**Nota**: A aplicação cria automaticamente a pasta `dados/` (com
subpastas `cadastros/` e `logs/`) no diretório de execução, usada para
persistir a base de usuários e o histórico de autenticações.

---

## Guia de uso da interface gráfica

A aplicação abre com uma janela em abas. Cada aba tem uma função:

### 1. **Cadastro**
   - Preencha **Nome Completo** (ex: "João da Silva")
   - Preencha **Matrícula / ID** (ex: "2023001")
   - Clique em "Selecionar imagem biométrica..." e escolha uma imagem
     (jpg, png, bmp).
   - Uma prévia da imagem aparece à direita
   - Clique em "Cadastrar Usuário"
   - O usuário aparece no histórico de status, com um ID único gerado

### 2. **Identificação / Autenticação**
   - Clique em "Selecionar imagem para verificação..." e escolha uma
     imagem de teste
   - **IDENTIFICAÇÃO (1:N)**: Clique "Identificar Usuário"
     → O sistema procura na base inteira e diz quem é a pessoa
   - **AUTENTICAÇÃO (1:1)**: Selecione um usuário no combo e clique
     "Autenticar Usuário Selecionado"
     → O sistema confirma se a imagem corresponde àquele usuário
   - Uma **barra de score** mostra a percentagem de similaridade
   - O resultado aparece no painel de texto abaixo

### 3. **Usuários Cadastrados**
   - Exibe uma tabela com todos os usuários da base
   - Mostra: ID, Nome, Matrícula, Data de Cadastro
   - Clique "Remover Selecionado" para deletar um usuário
   - Clique "Atualizar Lista" para sincronizar com o arquivo em disco

### 4. **Relatórios**
   - **"Relatório Geral / Estatísticas"**: mostra total de cadastros,
     taxa de sucesso/falha nas autenticações, score médio e ranking
     dos usuários mais autenticados
   - **"Histórico Detalhado de Tentativas"**: lista todas as tentativas
     de autenticação, com data/hora, score e resultado
   - **"Exportar Relatório Atual (.txt)"**: salva o relatório visível
     em um arquivo de texto para impressão ou envio

## Como funciona (visão técnica)

1. **Extração de características** (`ExtratorCaracteristicas`):
   a imagem é convertida para escala de cinza, normalizada para um
   tamanho fixo (128x128) e processada com um **Histograma de
   Gradientes Orientados (HOG)**. O resultado é um vetor numérico
   (template biométrico) normalizado.

2. **Comparação** (`ComparadorBiometrico`): dois templates são
   comparados por **similaridade de cosseno**, gerando um score de
   0 a 1 (0% a 100%).

3. **Identificação (1:N)**: a imagem informada é comparada contra
   *todos* os usuários da base; o mais parecido é retornado, se o
   score ultrapassar o limiar de aceitação (80%, configurável em
   `ServicoAutenticacao.LIMIAR_SIMILARIDADE`).

4. **Autenticação (1:1)**: verifica se a imagem corresponde à
   identidade alegada de um usuário específico já selecionado.

5. **Persistência**: os usuários cadastrados (nome, matrícula,
   template biométrico) são salvos em `dados/cadastros/base_usuarios.dat`
   via serialização de objetos Java. Cada tentativa de autenticação é
   gravada em `dados/logs/log_autenticacoes.csv`.

6. **Relatórios** (função extra): a aba "Relatórios" gera estatísticas
   como total de cadastros, taxa de sucesso/falha nas autenticações,
   score médio de similaridade e ranking dos usuários mais autenticados,
   podendo ser exportado para `.txt`.

## Estrutura do projeto

```
src/main/java/biometria/
├── Main.java                          # ponto de entrada
├── model/
│   ├── UsuarioBiometrico.java         # dados do usuário + template
│   └── TentativaAutenticacao.java     # registro de cada tentativa
├── processamento/
│   └── ExtratorCaracteristicas.java   # extração do template (HOG)
├── autenticacao/
│   ├── ComparadorBiometrico.java      # similaridade de cosseno
│   └── ServicoAutenticacao.java       # orquestra cadastro/verificação
├── persistencia/
│   └── RepositorioUsuarios.java       # salva/carrega base de usuários
├── relatorio/
│   ├── RegistradorLog.java            # grava log CSV de tentativas
│   └── GeradorRelatorios.java         # estatísticas e relatórios
├── util/
│   └── ProcessamentoBiometricoException.java  # tratamento de erros
└── ui/                                # interface gráfica (Swing)
    ├── JanelaPrincipal.java
    ├── PainelCadastro.java
    ├── PainelAutenticacao.java
    ├── PainelUsuarios.java
    └── PainelRelatorios.java
```

## Tratamento de erros implementado

- Validação de arquivo inexistente, corrompido ou em formato não
  suportado (`ProcessamentoBiometricoException`).
- Validação de campos obrigatórios no cadastro (nome, matrícula, imagem).
- Tratamento de falhas de leitura/escrita em disco (`IOException`),
  com mensagens amigáveis ao usuário via `JOptionPane`.
- Recuperação segura caso a base de dados esteja ausente ou corrompida
  (inicia com base vazia e avisa no console, em vez de travar).
