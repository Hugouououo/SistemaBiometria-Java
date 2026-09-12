package biometria.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registra o resultado de uma tentativa de autenticação/identificação,
 * usado para geração dos relatórios adicionais do sistema.
 */
public class TentativaAutenticacao implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Resultado {
        AUTENTICADO_SUCESSO,
        AUTENTICADO_FALHA,
        ERRO_PROCESSAMENTO
    }

    private final LocalDateTime dataHora;
    private final String usuarioIdentificado; // pode ser null se não reconhecido
    private final double scoreSimilaridade;
    private final Resultado resultado;
    private final String imagemUtilizada;
    private final String observacao;

    public TentativaAutenticacao(String usuarioIdentificado, double scoreSimilaridade,
                                  Resultado resultado, String imagemUtilizada, String observacao) {
        this.dataHora = LocalDateTime.now();
        this.usuarioIdentificado = usuarioIdentificado;
        this.scoreSimilaridade = scoreSimilaridade;
        this.resultado = resultado;
        this.imagemUtilizada = imagemUtilizada;
        this.observacao = observacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getDataHoraFormatada() {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getUsuarioIdentificado() {
        return usuarioIdentificado;
    }

    public double getScoreSimilaridade() {
        return scoreSimilaridade;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public String getImagemUtilizada() {
        return imagemUtilizada;
    }

    public String getObservacao() {
        return observacao;
    }

    /** Formata a linha para gravação em arquivo de log CSV. */
    public String toCsvLine() {
        return String.join(";",
                getDataHoraFormatada(),
                usuarioIdentificado == null ? "N/A" : usuarioIdentificado,
                String.format("%.4f", scoreSimilaridade),
                resultado.name(),
                imagemUtilizada == null ? "" : imagemUtilizada,
                observacao == null ? "" : observacao
        );
    }

    public static String cabecalhoCsv() {
        return "DataHora;UsuarioIdentificado;Score;Resultado;Imagem;Observacao";
    }
}
