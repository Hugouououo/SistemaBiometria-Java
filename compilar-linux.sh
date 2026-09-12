echo "=== Compilando Sistema de Biometria ==="

if [ -d "bin" ]; then
    rm -rf bin
fi
mkdir -p bin
echo "✓ Pasta 'bin' criada"

echo "Compilando arquivos Java..."
find src -name "*.java" > /tmp/sources.txt
java_count=$(wc -l < /tmp/sources.txt)

if [ "$java_count" -eq 0 ]; then
    echo "✗ Nenhum arquivo .java encontrado em src/"
    exit 1
fi

echo "  Encontrados $java_count arquivo(s) .java"

# Compilar
javac -d bin -encoding UTF-8 @/tmp/sources.txt 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Compilação bem-sucedida"
else
    echo "✗ Erro na compilação"
    exit 1
fi

rm /tmp/sources.txt

echo ""
echo "Iniciando aplicação..."
java -cp bin biometria.Main

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Execução concluída"
else
    echo ""
    echo "✗ Erro na execução"
fi
