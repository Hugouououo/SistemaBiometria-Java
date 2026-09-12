package biometria.processamento;

import biometria.util.ProcessamentoBiometricoException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExtratorCaracteristicas {

    private static final int TAMANHO_NORMALIZADO = 128; // px x px
    private static final int TAMANHO_CELULA = 16;        // célula do HOG

    public double[] extrair(File arquivoImagem) throws ProcessamentoBiometricoException {
        if (arquivoImagem == null || !arquivoImagem.exists()) {
            throw new ProcessamentoBiometricoException(
                    "Arquivo de imagem não encontrado: " + (arquivoImagem == null ? "null" : arquivoImagem.getPath()));
        }

        BufferedImage imagemOriginal;
        try {
            imagemOriginal = ImageIO.read(arquivoImagem);
        } catch (IOException e) {
            throw new ProcessamentoBiometricoException("Falha ao ler o arquivo de imagem: " + arquivoImagem.getName(), e);
        }

        if (imagemOriginal == null) {
            throw new ProcessamentoBiometricoException(
                    "Formato de imagem não suportado ou arquivo corrompido: " + arquivoImagem.getName());
        }

        BufferedImage cinza = converterParaCinza(imagemOriginal);
        BufferedImage normalizada = redimensionar(cinza, TAMANHO_NORMALIZADO, TAMANHO_NORMALIZADO);
        double[] vetorHog = calcularHOG(normalizada);

        return normalizarL2(vetorHog);
    }

    private BufferedImage converterParaCinza(BufferedImage original) {
        BufferedImage cinza = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int luminancia = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                int pixelCinza = (luminancia << 16) | (luminancia << 8) | luminancia;
                cinza.setRGB(x, y, pixelCinza);
            }
        }
        return cinza;
    }

    private BufferedImage redimensionar(BufferedImage original, int largura, int altura) {
        BufferedImage redimensionada = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D g2d = redimensionada.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, largura, altura, null);
        g2d.dispose();
        return redimensionada;
    }

    private double[] calcularHOG(BufferedImage img) {
        int largura = img.getWidth();
        int altura = img.getHeight();
        int[][] gray = new int[altura][largura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                gray[y][x] = img.getRaster().getSample(x, y, 0);
            }
        }

        int celulasX = largura / TAMANHO_CELULA;
        int celulasY = altura / TAMANHO_CELULA;
        int numBins = 9; // 0-180 graus em 9 faixas de 20 graus
        double[] vetor = new double[celulasX * celulasY * numBins];

        int idxVetor = 0;
        for (int cy = 0; cy < celulasY; cy++) {
            for (int cx = 0; cx < celulasX; cx++) {
                double[] histograma = new double[numBins];

                for (int y = cy * TAMANHO_CELULA; y < (cy + 1) * TAMANHO_CELULA; y++) {
                    for (int x = cx * TAMANHO_CELULA; x < (cx + 1) * TAMANHO_CELULA; x++) {
                        if (x <= 0 || y <= 0 || x >= largura - 1 || y >= altura - 1) continue;

                        int gx = gray[y][x + 1] - gray[y][x - 1];
                        int gy = gray[y + 1][x] - gray[y - 1][x];

                        double magnitude = Math.sqrt(gx * gx + gy * gy);
                        double anguloRad = Math.atan2(gy, gx);
                        double anguloGraus = Math.toDegrees(anguloRad);
                        if (anguloGraus < 0) anguloGraus += 180; // sem sinal (0-180)

                        int bin = (int) (anguloGraus / 20.0) % numBins;
                        histograma[bin] += magnitude;
                    }
                }

                for (double valor : histograma) {
                    vetor[idxVetor++] = valor;
                }
            }
        }
        return vetor;
    }

    private double[] normalizarL2(double[] vetor) {
        double somaQuadrados = 0.0;
        for (double v : vetor) {
            somaQuadrados += v * v;
        }
        double norma = Math.sqrt(somaQuadrados);
        if (norma < 1e-9) {
            return vetor; // vetor nulo (imagem sem gradientes relevantes) - evita NaN
        }
        double[] normalizado = new double[vetor.length];
        for (int i = 0; i < vetor.length; i++) {
            normalizado[i] = vetor[i] / norma;
        }
        return normalizado;
    }
}
