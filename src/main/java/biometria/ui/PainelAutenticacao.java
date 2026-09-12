package biometria.ui;

import biometria.autenticacao.ServicoAutenticacao;
import biometria.model.UsuarioBiometrico;
import biometria.util.ProcessamentoBiometricoException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Painel responsável pela verificação biométrica: permite tanto a
 * IDENTIFICAÇÃO (1:N, sem informar quem é o usuário) quanto a
 * AUTENTICAÇÃO (1:1, verificando uma identidade alegada específica).
 */
public class PainelAutenticacao extends JPanel {

    private final ServicoAutenticacao servico;

    private JLabel labelImagemPreview;
    private File arquivoSelecionado;
    private JComboBox<UsuarioBiometrico> comboUsuarios;
    private JTextArea areaResultado;
    private JProgressBar barraScore;

    public PainelAutenticacao(ServicoAutenticacao servico) {
        this.servico = servico;
        montarLayout();
    }

    private void montarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topo = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JButton botaoSelecionar = new JButton("Selecionar imagem para verificação...");
        topo.add(botaoSelecionar, gbc);
        botaoSelecionar.addActionListener(e -> selecionarImagem());

        gbc.gridx = 0; gbc.gridy = 1;
        topo.add(new JLabel("Identificação (1:N) — busca na base toda:"), gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        JButton botaoIdentificar = new JButton("Identificar Usuário");
        botaoIdentificar.addActionListener(e -> identificar());
        topo.add(botaoIdentificar, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        topo.add(new JSeparator(), gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        topo.add(new JLabel("Autenticação (1:1) — verifica identidade alegada:"), gbc);
        gbc.gridx = 0; gbc.gridy = 5;
        comboUsuarios = new JComboBox<>();
        topo.add(comboUsuarios, gbc);
        gbc.gridx = 0; gbc.gridy = 6;
        JButton botaoAutenticar = new JButton("Autenticar Usuário Selecionado");
        botaoAutenticar.addActionListener(e -> autenticar());
        topo.add(botaoAutenticar, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        barraScore = new JProgressBar(0, 100);
        barraScore.setStringPainted(true);
        barraScore.setString("Score de similaridade");
        topo.add(barraScore, gbc);

        labelImagemPreview = new JLabel("Prévia da imagem");
        labelImagemPreview.setPreferredSize(new Dimension(200, 200));
        labelImagemPreview.setBorder(BorderFactory.createEtchedBorder());
        labelImagemPreview.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel painelSuperior = new JPanel(new BorderLayout(15, 15));
        painelSuperior.add(topo, BorderLayout.CENTER);
        painelSuperior.add(labelImagemPreview, BorderLayout.EAST);

        areaResultado = new JTextArea(10, 40);
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(painelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
    }

    /** Deve ser chamado ao exibir a aba, para manter o combo de usuários atualizado. */
    public void atualizarListaUsuarios() {
        comboUsuarios.removeAllItems();
        List<UsuarioBiometrico> usuarios = servico.getRepositorio().listarTodos();
        for (UsuarioBiometrico u : usuarios) {
            comboUsuarios.addItem(u);
        }
    }

    private void selecionarImagem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens (jpg, jpeg, png, bmp)", "jpg", "jpeg", "png", "bmp"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = chooser.getSelectedFile();
            exibirPreview(arquivoSelecionado);
        }
    }

    private void exibirPreview(File arquivo) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(arquivo);
            if (img != null) {
                Image escalada = img.getScaledInstance(190, 190, Image.SCALE_SMOOTH);
                labelImagemPreview.setIcon(new ImageIcon(escalada));
                labelImagemPreview.setText(null);
            }
        } catch (IOException ex) {
            labelImagemPreview.setIcon(null);
            labelImagemPreview.setText("Erro ao carregar");
        }
    }

    private void identificar() {
        if (!validarImagemSelecionada()) return;

        try {
            ServicoAutenticacao.ResultadoIdentificacao resultado = servico.identificar(arquivoSelecionado);
            atualizarBarraScore(resultado.getScore());

            if (resultado.isSucesso()) {
                UsuarioBiometrico u = resultado.getUsuario();
                areaResultado.append(String.format(
                        "✔ IDENTIFICADO: %s (matrícula %s) — score de similaridade: %.2f%%%n%n",
                        u.getNomeCompleto(), u.getMatricula(), resultado.getScore() * 100));
            } else {
                areaResultado.append(String.format(
                        "✘ NÃO IDENTIFICADO — nenhum usuário da base atingiu o limiar mínimo (score máximo: %.2f%%)%n%n",
                        Math.max(resultado.getScore(), 0) * 100));
            }
        } catch (ProcessamentoBiometricoException ex) {
            areaResultado.append("✘ Erro no processamento: " + ex.getMessage() + "\n\n");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de processamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void autenticar() {
        if (!validarImagemSelecionada()) return;

        UsuarioBiometrico selecionado = (UsuarioBiometrico) comboUsuarios.getSelectedItem();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Não há usuários cadastrados na base para autenticar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ServicoAutenticacao.ResultadoIdentificacao resultado =
                    servico.autenticar(selecionado.getId(), arquivoSelecionado);
            atualizarBarraScore(resultado.getScore());

            if (resultado.isSucesso()) {
                areaResultado.append(String.format(
                        "✔ AUTENTICADO: identidade de %s confirmada — score: %.2f%%%n%n",
                        selecionado.getNomeCompleto(), resultado.getScore() * 100));
            } else {
                areaResultado.append(String.format(
                        "✘ FALHA NA AUTENTICAÇÃO: imagem não corresponde a %s — score: %.2f%%%n%n",
                        selecionado.getNomeCompleto(), resultado.getScore() * 100));
            }
        } catch (ProcessamentoBiometricoException ex) {
            areaResultado.append("✘ Erro no processamento: " + ex.getMessage() + "\n\n");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de processamento", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarImagemSelecionada() {
        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma imagem para verificação antes de continuar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void atualizarBarraScore(double score) {
        int percentual = (int) Math.round(Math.max(0, score) * 100);
        barraScore.setValue(percentual);
        barraScore.setString(String.format("Score: %.2f%%", score * 100));
    }
}
