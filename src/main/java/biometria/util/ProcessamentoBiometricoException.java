package biometria.util;

public class ProcessamentoBiometricoException extends Exception {

    private static final long serialVersionUID = 1L;

    public ProcessamentoBiometricoException(String mensagem) {
        super(mensagem);
    }

    public ProcessamentoBiometricoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
