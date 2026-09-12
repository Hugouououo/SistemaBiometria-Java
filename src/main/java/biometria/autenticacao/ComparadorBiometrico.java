package biometria.autenticacao;

public class ComparadorBiometrico {

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
