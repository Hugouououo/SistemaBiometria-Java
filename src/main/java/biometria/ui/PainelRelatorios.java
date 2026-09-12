package biometria.ui;

import biometria.autenticacao.ServicoAutenticacao;
import biometria.relatorio.GeradorRelatorios;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Painel de relatórios: exibe estatísticas de uso do sistema e permite
 * exportar o relatório para um arquivo .txt.
 */
public class PainelRelatorios extends JPanel {

    private final ServicoAutenticacao servico;
    private final GeradorRelatorios gerador;
    private JTextArea areaRelatorio;

    public PainelRelatorios(ServicoAutenticacao servico) {
        this.servico = servico;
        this.gerador = new GeradorRelatorios();
        montarLayout();
    }

    private void montarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel botoes = new JPanel();
        JButton botaoGeral = new JButton("Relatório Geral / Estatísticas");
        botaoGeral.addActionListener(e -> exibirRelatorioGeral());
        JButton botaoDetalhado = new JButton("Histórico Detalhado de Tentativas");
        botaoDetalhado.addActionListener(e -> exibirRelatorioDetalhado());
        JButton botaoExportar = new JButton("Exportar Relatório Atual (.txt)");
        botaoExportar.addActionListener(e -> exportarRelatorio());

        botoes.add(botaoGeral);
        botoes.add(botaoDetalhado);
        botoes.add(botaoExportar);

        areaRelatorio = new JTextArea();
        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        add(botoes, BorderLayout.NORTH);
        add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);
    }

    private void exibirRelatorioGeral() {
        String relatorio = gerador.gerarRelatorioGeral(
                servico.getRepositorio().listarTodos(),
                servico.getRegistradorLog().getHistorico());
        areaRelatorio.setText(relatorio);
        areaRelatorio.setCaretPosition(0);
    }

    private void exibirRelatorioDetalhado() {
        String relatorio = gerador.gerarRelatorioDetalhadoTentativas(
                servico.getRegistradorLog().getHistorico());
        areaRelatorio.setText(relatorio);
        areaRelatorio.setCaretPosition(0);
    }

    private void exportarRelatorio() {
        if (areaRelatorio.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gere um relatório antes de exportar.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("relatorio_biometria.txt"));
        int resultado = chooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File destino = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(destino)) {
                writer.write(areaRelatorio.getText());
                JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso para:\n" + destino.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao exportar relatório: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
