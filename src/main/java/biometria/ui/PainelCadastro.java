package biometria.ui;

import biometria.autenticacao.ServicoAutenticacao;
import biometria.model.UsuarioBiometrico;
import biometria.util.ProcessamentoBiometricoException;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Painel responsável pelo cadastro de novos usuários: coleta nome,
 * matrícula e a imagem biométrica, mostra uma prévia e realiza o cadastro.
 */
public class PainelCadastro extends JPanel {

    private final ServicoAutenticacao servico;

    private JTextField campoNome;
    private JTextField campoMatricula;
    private JLabel labelImagemPreview;
    private JLabel labelCaminhoArquivo;
    private File arquivoSelecionado;
    private JTextArea areaStatus;

    public PainelCadastro(ServicoAutenticacao servico) {
        this.servico = servico;
        montarLayout();
    }

    private void montarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formulario.add(new JLabel("Nome completo:"), gbc);
        campoNome = new JTextField(20);
        gbc.gridx = 1;
        formulario.add(campoNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formulario.add(new JLabel("Matrícula / ID:"), gbc);
        campoMatricula = new JTextField(20);
        gbc.gridx = 1;
        formulario.add(campoMatricula, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JButton botaoSelecionar = new JButton("Selecionar imagem biométrica...");
        formulario.add(botaoSelecionar, gbc);
        gbc.gridx = 1;
        labelCaminhoArquivo = new JLabel("Nenhum arquivo selecionado");
        formulario.add(labelCaminhoArquivo, gbc);

        botaoSelecionar.addActionListener(e -> selecionarImagem());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton botaoCadastrar = new JButton("Cadastrar Usuário");
        botaoCadastrar.addActionListener(e -> cadastrar());
        formulario.add(botaoCadastrar, gbc);

        labelImagemPreview = new JLabel();
        labelImagemPreview.setPreferredSize(new Dimension(200, 200));
        labelImagemPreview.setBorder(BorderFactory.createEtchedBorder());
        labelImagemPreview.setHorizontalAlignment(SwingConstants.CENTER);
        labelImagemPreview.setText("Prévia da imagem");

        JPanel painelSuperior = new JPanel(new BorderLayout(15, 15));
        painelSuperior.add(formulario, BorderLayout.CENTER);
        painelSuperior.add(labelImagemPreview, BorderLayout.EAST);

        areaStatus = new JTextArea(8, 40);
        areaStatus.setEditable(false);
        areaStatus.setLineWrap(true);
        areaStatus.setWrapStyleWord(true);

        add(painelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(areaStatus), BorderLayout.CENTER);
    }

    private void selecionarImagem() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens (jpg, jpeg, png, bmp)", "jpg", "jpeg", "png", "bmp"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = chooser.getSelectedFile();
            labelCaminhoArquivo.setText(arquivoSelecionado.getName());
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
            } else {
                labelImagemPreview.setIcon(null);
                labelImagemPreview.setText("Formato inválido");
            }
        } catch (IOException ex) {
            labelImagemPreview.setIcon(null);
            labelImagemPreview.setText("Erro ao carregar");
        }
    }

    private void cadastrar() {
        String nome = campoNome.getText();
        String matricula = campoMatricula.getText();

        if (arquivoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma imagem biométrica antes de cadastrar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UsuarioBiometrico usuario = servico.cadastrar(nome, matricula, arquivoSelecionado);
            areaStatus.append("✔ Usuário cadastrado com sucesso: " + usuario + "\n");
            limparFormulario();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dados inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (ProcessamentoBiometricoException ex) {
            areaStatus.append("✘ Erro no processamento da imagem: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de processamento", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            areaStatus.append("✘ Erro ao salvar cadastro: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Erro ao salvar o cadastro: " + ex.getMessage(),
                    "Erro de persistência", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoMatricula.setText("");
        arquivoSelecionado = null;
        labelCaminhoArquivo.setText("Nenhum arquivo selecionado");
        labelImagemPreview.setIcon(null);
        labelImagemPreview.setText("Prévia da imagem");
    }
}
