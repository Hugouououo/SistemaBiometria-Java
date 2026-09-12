package biometria;

import biometria.ui.JanelaPrincipal;

import javax.swing.*;

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
