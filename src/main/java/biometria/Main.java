package biometria;

import biometria.ui.JanelaPrincipal;

import javax.swing.*;

/**
 * Ponto de entrada da aplicação de Identificação e Autenticação Biométrica.
 *
 * Trabalho acadêmico: ferramenta de identificação e autenticação biométrica
 * baseada em imagens de impressão digital, obtidas de arquivos de imagem.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Caso o look and feel do sistema não esteja disponível, usa o padrão do Swing.
            }
            new JanelaPrincipal().setVisible(true);
        });
    }
}
