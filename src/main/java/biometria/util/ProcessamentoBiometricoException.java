package biometria.util;

/**
 * Exceção lançada quando ocorre falha na leitura, validação ou
 * extração de características de uma imagem biométrica.
 */
public class ProcessamentoBiometricoException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProcessamentoBiometricoException(String mensagem) {
        super(mensagem);
    }

    public ProcessamentoBiometricoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
