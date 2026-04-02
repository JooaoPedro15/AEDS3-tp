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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ArvoreBMais<K extends Comparable<K> & Serializable, V extends Serializable> {

    private final Path caminhoArquivo;
    private final TreeMap<K, ArrayList<V>> dados;

    public ArvoreBMais(String caminhoArquivo) {
        this.caminhoArquivo = Paths.get(caminhoArquivo);
        this.dados = carregar();
    }

    public synchronized boolean create(K chave, V valor) {
        ArrayList<V> lista = dados.computeIfAbsent(chave, k -> new ArrayList<>());

        if (lista.contains(valor)) {
            return false;
        }

        lista.add(valor);
        salvar();
        return true;
    }

    public synchronized ArrayList<V> read(K chave) {
        ArrayList<V> lista = dados.get(chave);

        if (lista == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(lista);
    }

    public synchronized boolean delete(K chave, V valor) {
        ArrayList<V> lista = dados.get(chave);

        if (lista == null || !lista.remove(valor)) {
            return false;
        }

        if (lista.isEmpty()) {
            dados.remove(chave);
        }

        salvar();
        return true;
    }

    public synchronized boolean delete(K chave) {
        if (!dados.containsKey(chave)) {
            return false;
        }

        dados.remove(chave);
        salvar();
        return true;
    }

    public synchronized void clear() {
        dados.clear();
        salvar();
    }

    public synchronized Map<K, ArrayList<V>> snapshot() {
        TreeMap<K, ArrayList<V>> copia = new TreeMap<>();

        for (Map.Entry<K, ArrayList<V>> entrada : dados.entrySet()) {
            copia.put(entrada.getKey(), new ArrayList<>(entrada.getValue()));
        }

        return copia;
    }

    @SuppressWarnings("unchecked")
    private TreeMap<K, ArrayList<V>> carregar() {
        try {
            Path pasta = caminhoArquivo.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            if (!Files.exists(caminhoArquivo) || Files.size(caminhoArquivo) == 0) {
                return new TreeMap<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo.toFile()))) {
                Object objeto = ois.readObject();

                if (objeto instanceof TreeMap<?, ?>) {
                    return (TreeMap<K, ArrayList<V>>) objeto;
                }
            }
        } catch (Exception ignored) {
            // Arquivo com formato invalido ou primeira execucao.
        }

        return new TreeMap<>();
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
            throw new RuntimeException("Erro ao salvar arvore em " + caminhoArquivo, e);
        }
    }
}

