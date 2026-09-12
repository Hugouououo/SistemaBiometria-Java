package biometria.ui;

import biometria.autenticacao.ServicoAutenticacao;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal da aplicação. Organiza as funcionalidades em abas:
 * Cadastro, Identificação/Autenticação, Usuários e Relatórios.
 */
public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Sistema de Identificação e Autenticação Biométrica");

        ServicoAutenticacao servico = new ServicoAutenticacao();

        PainelCadastro painelCadastro = new PainelCadastro(servico);
        PainelAutenticacao painelAutenticacao = new PainelAutenticacao(servico);
        PainelUsuarios painelUsuarios = new PainelUsuarios(servico);
        PainelRelatorios painelRelatorios = new PainelRelatorios(servico);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("1. Cadastro", painelCadastro);
        abas.addTab("2. Identificação / Autenticação", painelAutenticacao);
        abas.addTab("3. Usuários Cadastrados", painelUsuarios);
        abas.addTab("4. Relatórios", painelRelatorios);

        // Atualiza listas dependentes sempre que o usuário troca de aba.
        abas.addChangeListener(e -> {
            int index = abas.getSelectedIndex();
            String titulo = abas.getTitleAt(index);
            if (titulo.contains("Identificação")) {
                painelAutenticacao.atualizarListaUsuarios();
            } else if (titulo.contains("Usuários")) {
                painelUsuarios.atualizarTabela();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(abas);
        setSize(800, 650);
        setMinimumSize(new Dimension(700, 550));
        setLocationRelativeTo(null);
    }
}
