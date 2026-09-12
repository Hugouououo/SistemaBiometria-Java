package biometria.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UsuarioBiometrico implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String nomeCompleto;
    private String matricula;
    private double[] template;      // vetor de características extraídas da imagem
    private String caminhoImagemOriginal;
    private LocalDateTime dataCadastro;

    public UsuarioBiometrico(String id, String nomeCompleto, String matricula,
                              double[] template, String caminhoImagemOriginal) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.matricula = matricula;
        this.template = template;
        this.caminhoImagemOriginal = caminhoImagemOriginal;
        this.dataCadastro = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getMatricula() {
        return matricula;
    }

    public double[] getTemplate() {
        return template;
    }

    public String getCaminhoImagemOriginal() {
        return caminhoImagemOriginal;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public String getDataCadastroFormatada() {
        return dataCadastro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("%s - %s (matrícula: %s)", id, nomeCompleto, matricula);
    }
}
