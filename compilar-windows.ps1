Write-Host "=== Compilando Sistema de Biometria ===" -ForegroundColor Cyan

if (Test-Path "bin") {
    Remove-Item -Recurse -Force "bin"
}
New-Item -ItemType Directory -Path "bin" | Out-Null
Write-Host "Pasta 'bin' criada" -ForegroundColor Green

Write-Host "Compilando arquivos Java..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java"
$javaFilesPath = $javaFiles | ForEach-Object { $_.FullName }

if ($javaFilesPath.Count -eq 0) {
    Write-Host "Nenhum arquivo .java encontrado em src/" -ForegroundColor Red
    exit 1
}

Write-Host "Encontrados $($javaFilesPath.Count) arquivo(s) .java"

# Compilar
javac -d bin -encoding UTF-8 $javaFilesPath
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilacao bem-sucedida" -ForegroundColor Green
} else {
    Write-Host "Erro na compilacao" -ForegroundColor Red
    exit 1
}

Write-Host "`nIniciando aplicacao..." -ForegroundColor Yellow
java -cp bin biometria.Main

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nExecucao concluida" -ForegroundColor Green
} else {
    Write-Host "`nErro na execucao" -ForegroundColor Red
}