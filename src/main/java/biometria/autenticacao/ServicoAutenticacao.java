package biometria.autenticacao;

import biometria.model.TentativaAutenticacao;
import biometria.model.UsuarioBiometrico;
import biometria.persistencia.RepositorioUsuarios;
import biometria.processamento.ExtratorCaracteristicas;
import biometria.util.ProcessamentoBiometricoException;
import biometria.relatorio.RegistradorLog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço central da aplicação: orquestra o cadastro de novos usuários
 * (extração + persistência do template) e a autenticação/identificação
 * de uma nova imagem biométrica em relação à base cadastrada.
 */
public class ServicoAutenticacao {

    /** Limiar mínimo de similaridade para considerar uma autenticação válida. */
    public static final double LIMIAR_SIMILARIDADE = 0.80;

    private final ExtratorCaracteristicas extrator;
    private final ComparadorBiometrico comparador;
    private final RepositorioUsuarios repositorio;
    private final RegistradorLog registradorLog;

    public ServicoAutenticacao() {
        this.extrator = new ExtratorCaracteristicas();
        this.comparador = new ComparadorBiometrico();
        this.repositorio = new RepositorioUsuarios();
        this.registradorLog = new RegistradorLog();
    }

    /**
     * Cadastra um novo usuário extraindo o template a partir da imagem informada.
     */
    public UsuarioBiometrico cadastrar(String nomeCompleto, String matricula, File imagem)
            throws ProcessamentoBiometricoException, IOException {

        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome completo é obrigatório.");
        }
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("A matrícula/identificador é obrigatório.");
        }

        double[] template = extrator.extrair(imagem);

        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        UsuarioBiometrico usuario = new UsuarioBiometrico(id, nomeCompleto.trim(), matricula.trim(),
                template, imagem.getAbsolutePath());

        repositorio.adicionar(usuario);
        return usuario;
    }

    /**
     * Realiza a IDENTIFICAÇÃO 1:N — compara a imagem informada contra toda a
     * base cadastrada e retorna o usuário mais similar, se acima do limiar.
     */
    public ResultadoIdentificacao identificar(File imagem) throws ProcessamentoBiometricoException {
        double[] templateEntrada;
        try {
            templateEntrada = extrator.extrair(imagem);
        } catch (ProcessamentoBiometricoException e) {
            registradorLog.registrar(new TentativaAutenticacao(
                    null, 0.0, TentativaAutenticacao.Resultado.ERRO_PROCESSAMENTO,
                    imagem != null ? imagem.getName() : "N/A", e.getMessage()));
            throw e;
        }

        List<UsuarioBiometrico> todos = repositorio.listarTodos();

        UsuarioBiometrico melhorCandidato = null;
        double melhorScore = -1.0;

        for (UsuarioBiometrico candidato : todos) {
            double score = comparador.calcularSimilaridade(templateEntrada, candidato.getTemplate());
            if (score > melhorScore) {
                melhorScore = score;
                melhorCandidato = candidato;
            }
        }

        boolean sucesso = melhorCandidato != null && melhorScore >= LIMIAR_SIMILARIDADE;

        TentativaAutenticacao tentativa = new TentativaAutenticacao(
                sucesso ? melhorCandidato.getNomeCompleto() : null,
                Math.max(melhorScore, 0.0),
                sucesso ? TentativaAutenticacao.Resultado.AUTENTICADO_SUCESSO
                        : TentativaAutenticacao.Resultado.AUTENTICADO_FALHA,
                imagem.getName(),
                sucesso ? "Identificado com sucesso" : "Nenhum usuário compatível encontrado acima do limiar");

        registradorLog.registrar(tentativa);

        return new ResultadoIdentificacao(sucesso, melhorCandidato, melhorScore);
    }

    /**
     * Realiza a AUTENTICAÇÃO 1:1 — compara a imagem informada contra o
     * template de um usuário específico (verificação de identidade alegada).
     */
    public ResultadoIdentificacao autenticar(String idUsuario, File imagem) throws ProcessamentoBiometricoException {
        Optional<UsuarioBiometrico> usuarioOpt = repositorio.buscarPorId(idUsuario);
        if (!usuarioOpt.isPresent()) {
            throw new ProcessamentoBiometricoException("Usuário com id '" + idUsuario + "' não encontrado na base.");
        }

        double[] templateEntrada = extrator.extrair(imagem);
        UsuarioBiometrico usuario = usuarioOpt.get();
        double score = comparador.calcularSimilaridade(templateEntrada, usuario.getTemplate());
        boolean sucesso = score >= LIMIAR_SIMILARIDADE;

        TentativaAutenticacao tentativa = new TentativaAutenticacao(
                usuario.getNomeCompleto(),
                score,
                sucesso ? TentativaAutenticacao.Resultado.AUTENTICADO_SUCESSO
                        : TentativaAutenticacao.Resultado.AUTENTICADO_FALHA,
                imagem.getName(),
                sucesso ? "Identidade confirmada" : "Identidade não confirmada (score abaixo do limiar)");

        registradorLog.registrar(tentativa);

        return new ResultadoIdentificacao(sucesso, usuario, score);
    }

    public RepositorioUsuarios getRepositorio() {
        return repositorio;
    }

    public RegistradorLog getRegistradorLog() {
        return registradorLog;
    }

    /** Objeto de retorno simples representando o resultado de uma identificação/autenticação. */
    public static class ResultadoIdentificacao {
        private final boolean sucesso;
        private final UsuarioBiometrico usuario; // pode ser null se não houver base cadastrada
        private final double score;

        public ResultadoIdentificacao(boolean sucesso, UsuarioBiometrico usuario, double score) {
            this.sucesso = sucesso;
            this.usuario = usuario;
            this.score = score;
        }

        public boolean isSucesso() {
            return sucesso;
        }

        public UsuarioBiometrico getUsuario() {
            return usuario;
        }

        public double getScore() {
            return score;
        }
    }
}
