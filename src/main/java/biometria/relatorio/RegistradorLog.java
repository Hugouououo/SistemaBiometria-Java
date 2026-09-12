package biometria.relatorio;

import biometria.model.TentativaAutenticacao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Responsável por registrar (persistir) cada tentativa de autenticação
 * realizada no sistema, tanto em memória (para exibição em relatórios
 * durante a execução) quanto em arquivo CSV (para histórico permanente).
 */
public class RegistradorLog {

    private static final String ARQUIVO_LOG = "dados/logs/log_autenticacoes.csv";

    private final List<TentativaAutenticacao> historicoEmMemoria = new ArrayList<>();

    public RegistradorLog() {
        carregarHistoricoDeArquivo();
    }

    public void registrar(TentativaAutenticacao tentativa) {
        historicoEmMemoria.add(tentativa);
        gravarEmArquivo(tentativa);
    }

    public List<TentativaAutenticacao> getHistorico() {
        return new ArrayList<>(historicoEmMemoria);
    }

    private void gravarEmArquivo(TentativaAutenticacao tentativa) {
        File arquivo = new File(ARQUIVO_LOG);
        boolean novo = !arquivo.exists();
        File diretorioPai = arquivo.getParentFile();
        if (diretorioPai != null && !diretorioPai.exists()) {
            diretorioPai.mkdirs();
        }
        try (FileWriter writer = new FileWriter(arquivo, true)) {
            if (novo) {
                writer.write(TentativaAutenticacao.cabecalhoCsv() + System.lineSeparator());
            }
            writer.write(tentativa.toCsvLine() + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível gravar o log em arquivo. Motivo: " + e.getMessage());
        }
    }

    /** Recarrega o histórico em memória a partir do arquivo CSV, se existir (apenas para contagem/estatísticas simples). */
    private void carregarHistoricoDeArquivo() {
        File arquivo = new File(ARQUIVO_LOG);
        if (!arquivo.exists()) {
            return;
        }
        try (Scanner scanner = new Scanner(arquivo)) {
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // pula cabeçalho
            }
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();
                String[] campos = linha.split(";", -1);
                if (campos.length < 6) continue;

                String usuario = "N/A".equals(campos[1]) ? null : campos[1];
                double score = Double.parseDouble(campos[2].replace(",", "."));
                TentativaAutenticacao.Resultado resultado = TentativaAutenticacao.Resultado.valueOf(campos[3]);

                // Reconstrução simplificada apenas para fins de estatística (data/hora original não é reprocessada).
                TentativaAutenticacao tentativa = new TentativaAutenticacao(
                        usuario, score, resultado, campos[4], campos[5]);
                historicoEmMemoria.add(tentativa);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Aviso: não foi possível carregar o histórico de log existente. Motivo: " + e.getMessage());
        }
    }
}
