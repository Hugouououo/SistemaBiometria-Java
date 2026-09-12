package biometria.persistencia;

import biometria.model.UsuarioBiometrico;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Responsável pela persistência dos usuários cadastrados no sistema.
 * Utiliza serialização de objetos Java para gravar/ler a base de dados
 * em um arquivo local, dispensando a necessidade de um SGBD externo.
 */
public class RepositorioUsuarios {

    private static final String ARQUIVO_BASE = "dados/cadastros/base_usuarios.dat";

    private List<UsuarioBiometrico> usuarios;

    public RepositorioUsuarios() {
        this.usuarios = carregar();
    }

    /** Adiciona um novo usuário e persiste imediatamente a base atualizada. */
    public void adicionar(UsuarioBiometrico usuario) throws IOException {
        usuarios.add(usuario);
        salvar();
    }

    /** Remove um usuário pelo id e persiste a alteração. */
    public boolean remover(String id) throws IOException {
        boolean removeu = usuarios.removeIf(u -> u.getId().equals(id));
        if (removeu) {
            salvar();
        }
        return removeu;
    }

    public List<UsuarioBiometrico> listarTodos() {
        return new ArrayList<>(usuarios);
    }

    public Optional<UsuarioBiometrico> buscarPorId(String id) {
        return usuarios.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public int total() {
        return usuarios.size();
    }

    @SuppressWarnings("unchecked")
    private List<UsuarioBiometrico> carregar() {
        File arquivo = new File(ARQUIVO_BASE);
        if (!arquivo.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            return (List<UsuarioBiometrico>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Aviso: não foi possível carregar a base de usuários. Iniciando base vazia. Motivo: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void salvar() throws IOException {
        File arquivo = new File(ARQUIVO_BASE);
        File diretorioPai = arquivo.getParentFile();
        if (diretorioPai != null && !diretorioPai.exists()) {
            diretorioPai.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            oos.writeObject(usuarios);
        }
    }
}
