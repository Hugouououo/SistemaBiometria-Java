package biometria.ui;

import biometria.autenticacao.ServicoAutenticacao;
import biometria.model.UsuarioBiometrico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Painel que exibe todos os usuários cadastrados em formato de tabela,
 * permitindo também a remoção de cadastros.
 */
public class PainelUsuarios extends JPanel {

    private final ServicoAutenticacao servico;
    private DefaultTableModel modeloTabela;
    private JTable tabela;
    private JLabel labelTotal;

    public PainelUsuarios(ServicoAutenticacao servico) {
        this.servico = servico;
        montarLayout();
        atualizarTabela();
    }

    private void montarLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] colunas = {"ID", "Nome Completo", "Matrícula", "Data de Cadastro"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modeloTabela);
        tabela.setRowHeight(24);

        JPanel painelInferior = new JPanel(new BorderLayout());
        labelTotal = new JLabel();
        painelInferior.add(labelTotal, BorderLayout.WEST);

        JPanel botoes = new JPanel();
        JButton botaoAtualizar = new JButton("Atualizar Lista");
        botaoAtualizar.addActionListener(e -> atualizarTabela());
        JButton botaoRemover = new JButton("Remover Selecionado");
        botaoRemover.addActionListener(e -> removerSelecionado());
        botoes.add(botaoAtualizar);
        botoes.add(botaoRemover);
        painelInferior.add(botoes, BorderLayout.EAST);

        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
    }

    public void atualizarTabela() {
        modeloTabela.setRowCount(0);
        List<UsuarioBiometrico> usuarios = servico.getRepositorio().listarTodos();
        for (UsuarioBiometrico u : usuarios) {
            modeloTabela.addRow(new Object[]{
                    u.getId(), u.getNomeCompleto(), u.getMatricula(), u.getDataCadastroFormatada()
            });
        }
        labelTotal.setText("Total de usuários cadastrados: " + usuarios.size());
    }

    private void removerSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para remover.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) modeloTabela.getValueAt(linha, 0);
        String nome = (String) modeloTabela.getValueAt(linha, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Confirma a remoção do usuário '" + nome + "'?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                servico.getRepositorio().remover(id);
                atualizarTabela();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao remover usuário: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
