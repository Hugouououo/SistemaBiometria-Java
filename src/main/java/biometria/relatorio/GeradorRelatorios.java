package biometria.relatorio;

import biometria.model.TentativaAutenticacao;
import biometria.model.UsuarioBiometrico;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gera relatórios estatísticos sobre o funcionamento do sistema:
 * total de cadastros, taxa de sucesso/falha nas autenticações,
 * ranking de usuários mais autenticados e score médio de similaridade.
 *
 * Este componente atende ao item de "funções extras e relatórios
 * adicionais" solicitado no enunciado do trabalho.
 */
public class GeradorRelatorios {

    public String gerarRelatorioGeral(List<UsuarioBiometrico> usuarios, List<TentativaAutenticacao> tentativas) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================\n");
        sb.append("   RELATÓRIO GERAL DO SISTEMA DE BIOMETRIA\n");
        sb.append("======================================================\n\n");

        sb.append("--- Base de Cadastros ---\n");
        sb.append("Total de usuários cadastrados: ").append(usuarios.size()).append("\n\n");
        for (UsuarioBiometrico u : usuarios) {
            sb.append(String.format("  [%s] %s - matrícula %s - cadastrado em %s%n",
                    u.getId(), u.getNomeCompleto(), u.getMatricula(), u.getDataCadastroFormatada()));
        }

        sb.append("\n--- Estatísticas de Autenticação ---\n");
        long total = tentativas.size();
        long sucessos = tentativas.stream()
                .filter(t -> t.getResultado() == TentativaAutenticacao.Resultado.AUTENTICADO_SUCESSO).count();
        long falhas = tentativas.stream()
                .filter(t -> t.getResultado() == TentativaAutenticacao.Resultado.AUTENTICADO_FALHA).count();
        long erros = tentativas.stream()
                .filter(t -> t.getResultado() == TentativaAutenticacao.Resultado.ERRO_PROCESSAMENTO).count();

        sb.append("Total de tentativas registradas: ").append(total).append("\n");
        sb.append("  Sucessos: ").append(sucessos)
                .append(total > 0 ? String.format(" (%.1f%%)", 100.0 * sucessos / total) : "").append("\n");
        sb.append("  Falhas: ").append(falhas)
                .append(total > 0 ? String.format(" (%.1f%%)", 100.0 * falhas / total) : "").append("\n");
        sb.append("  Erros de processamento: ").append(erros).append("\n");

        double scoreMedio = tentativas.stream()
                .mapToDouble(TentativaAutenticacao::getScoreSimilaridade)
                .average().orElse(0.0);
        sb.append(String.format("Score médio de similaridade: %.4f%n", scoreMedio));

        sb.append("\n--- Ranking de Usuários Mais Autenticados ---\n");
        Map<String, Long> ranking = tentativas.stream()
                .filter(t -> t.getUsuarioIdentificado() != null)
                .collect(Collectors.groupingBy(TentativaAutenticacao::getUsuarioIdentificado, Collectors.counting()));

        if (ranking.isEmpty()) {
            sb.append("  Nenhuma autenticação bem-sucedida registrada ainda.\n");
        } else {
            ranking.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .forEach(e -> sb.append("  ").append(e.getKey()).append(": ")
                            .append(e.getValue()).append(" autenticação(ões)\n"));
        }

        sb.append("\n======================================================\n");
        return sb.toString();
    }

    /** Relatório detalhado de todas as tentativas, em ordem cronológica de registro. */
    public String gerarRelatorioDetalhadoTentativas(List<TentativaAutenticacao> tentativas) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================\n");
        sb.append("   HISTÓRICO DETALHADO DE TENTATIVAS DE AUTENTICAÇÃO\n");
        sb.append("======================================================\n\n");

        if (tentativas.isEmpty()) {
            sb.append("Nenhuma tentativa registrada até o momento.\n");
            return sb.toString();
        }

        int i = 1;
        for (TentativaAutenticacao t : tentativas) {
            sb.append(i++).append(") ").append(t.getDataHoraFormatada()).append("\n");
            sb.append("   Imagem: ").append(t.getImagemUtilizada()).append("\n");
            sb.append("   Resultado: ").append(t.getResultado()).append("\n");
            sb.append("   Usuário: ").append(t.getUsuarioIdentificado() == null ? "Não identificado" : t.getUsuarioIdentificado()).append("\n");
            sb.append(String.format("   Score: %.4f%n", t.getScoreSimilaridade()));
            sb.append("   Obs: ").append(t.getObservacao()).append("\n\n");
        }
        return sb.toString();
    }
}
