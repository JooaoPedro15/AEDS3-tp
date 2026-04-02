package estruturas;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TabelaHashExtensivel<K extends Serializable, V extends Serializable> {

    private final Path caminhoArquivo;
    private final Map<K, V> dados;

    public TabelaHashExtensivel(String caminhoArquivo) {
        this.caminhoArquivo = Paths.get(caminhoArquivo);
        this.dados = carregar();
    }

    public synchronized boolean create(K chave, V valor) {
        if (dados.containsKey(chave)) {
            return false;
        }

        dados.put(chave, valor);
        salvar();
        return true;
    }

    public synchronized V read(K chave) {
        return dados.get(chave);
    }

    public synchronized boolean update(K chave, V valor) {
        if (!dados.containsKey(chave)) {
            return false;
        }

        dados.put(chave, valor);
        salvar();
        return true;
    }

    public synchronized void upsert(K chave, V valor) {
        dados.put(chave, valor);
        salvar();
    }

    public synchronized boolean delete(K chave) {
        if (!dados.containsKey(chave)) {
            return false;
        }

        dados.remove(chave);
        salvar();
        return true;
    }

    public synchronized boolean containsKey(K chave) {
        return dados.containsKey(chave);
    }

    public synchronized void clear() {
        dados.clear();
        salvar();
    }

    public synchronized Map<K, V> snapshot() {
        return new HashMap<>(dados);
    }

    @SuppressWarnings("unchecked")
    private Map<K, V> carregar() {
        try {
            Path pasta = caminhoArquivo.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            if (!Files.exists(caminhoArquivo) || Files.size(caminhoArquivo) == 0) {
                return new HashMap<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo.toFile()))) {
                Object objeto = ois.readObject();

                if (objeto instanceof Map<?, ?>) {
                    return (Map<K, V>) objeto;
                }
            }
        } catch (Exception ignored) {
            // Arquivo com formato invalido ou primeira execucao.
        }

        return new HashMap<>();
    }

    private void salvar() {
        try {
            Path pasta = caminhoArquivo.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo.toFile()))) {
                oos.writeObject(dados);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar tabela hash em " + caminhoArquivo, e);
        }
    }
}

