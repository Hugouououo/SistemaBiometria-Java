package biometria.autenticacao;

/**
 * Responsável por calcular o grau de similaridade entre dois templates
 * biométricos (vetores de características), utilizando a similaridade
 * de cosseno — métrica amplamente usada em reconhecimento de padrões
 * pois é invariante à magnitude do vetor, apenas à sua orientação.
 */
public class ComparadorBiometrico {

    /**
     * Calcula a similaridade de cosseno entre dois vetores.
     * Resultado varia de -1 (totalmente diferentes) a 1 (idênticos).
     * Como os vetores já vêm normalizados (L2) do extrator, o resultado
     * prático fica entre 0 e 1 para este domínio.
     */
    public double calcularSimilaridade(double[] templateA, double[] templateB) {
        if (templateA == null || templateB == null) {
            throw new IllegalArgumentException("Templates não podem ser nulos.");
        }
        if (templateA.length != templateB.length) {
            throw new IllegalArgumentException(
                    "Templates de tamanhos incompatíveis: " + templateA.length + " vs " + templateB.length);
        }

        double produtoEscalar = 0.0;
        double normaA = 0.0;
        double normaB = 0.0;

        for (int i = 0; i < templateA.length; i++) {
            produtoEscalar += templateA[i] * templateB[i];
            normaA += templateA[i] * templateA[i];
            normaB += templateB[i] * templateB[i];
        }

        double denominador = Math.sqrt(normaA) * Math.sqrt(normaB);
        if (denominador < 1e-9) {
            return 0.0;
        }
        return produtoEscalar / denominador;
    }
}
