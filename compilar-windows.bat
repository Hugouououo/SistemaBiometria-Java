@echo off
REM Script de compilação para Windows (cmd.exe / Batch)
REM Pré-requisito: JDK 17+ instalado (javac no PATH)

setlocal enabledelayedexpansion

echo === Compilando Sistema de Biometria ===

REM 1. Criar diretório de saída
if exist bin (
    rmdir /s /q bin
)
mkdir bin
echo. Pasta 'bin' criada

REM 2. Compilar todos os arquivos .java
echo. Compilando arquivos Java...

REM Criar lista de arquivos
set "java_files="
for /r src %%f in (*.java) do (
    set "java_files=!java_files! %%f"
)

if "!java_files!"=="" (
    echo. Nenhum arquivo .java encontrado em src\
    exit /b 1
)

REM Contar arquivos (aproximado)
echo.   Encontrados vários arquivo(s) .java

REM Compilar
javac -d bin -encoding UTF-8 !java_files! 2>&1
if %errorlevel% equ 0 (
    echo. Compilacao bem-sucedida
) else (
    echo. Erro na compilacao
    exit /b 1
)

REM 3. Executar
echo.
echo. Iniciando aplicacao...
java -cp bin biometria.Main

if %errorlevel% equ 0 (
    echo.
    echo. Execucao concluida
) else (
    echo.
    echo. Erro na execucao
)

endlocal
