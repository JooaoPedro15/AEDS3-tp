package estruturas;

import aed3.RegistroArvoreBMais;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ArvoreBMais<K extends Comparable<K> & Serializable, V extends Serializable> {

    private static final int ORDEM_PADRAO = 5;
    private static final int TAMANHO_TEXTO = 160;

    public enum Tipo {
        INT_INT,
        STRING_INT
    }

    private final Path arquivoArvore;
    private final Tipo tipo;

    private aed3.ArvoreBMais<RegistroIntInt> indiceIntInt;
    private aed3.ArvoreBMais<RegistroStringInt> indiceStringInt;

    public ArvoreBMais(String caminhoArquivo) {
        this(caminhoArquivo, inferirTipo(caminhoArquivo));
    }

    public ArvoreBMais(String caminhoArquivo, Tipo tipo) {
        this.arquivoArvore = Paths.get(caminhoArquivo);
        this.tipo = tipo;
        abrirIndice();
    }

    public synchronized boolean create(K chave, V valor) {
        try {
            if (tipo == Tipo.INT_INT) {
                return indiceIntInt.create(new RegistroIntInt((Integer) chave, (Integer) valor));
            }

            return indiceStringInt.create(new RegistroStringInt((String) chave, (Integer) valor));
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized ArrayList<V> read(K chave) {
        try {
            ArrayList<V> resposta = new ArrayList<>();

            if (tipo == Tipo.INT_INT) {
                for (RegistroIntInt registro : indiceIntInt.read(new RegistroIntInt((Integer) chave))) {
                    resposta.add((V) Integer.valueOf(registro.id2));
                }
                return resposta;
            }

            for (RegistroStringInt registro : indiceStringInt.read(new RegistroStringInt((String) chave))) {
                resposta.add((V) Integer.valueOf(registro.id));
            }
            return resposta;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arvore B+", e);
        }
    }

    public synchronized boolean delete(K chave, V valor) {
        try {
            if (tipo == Tipo.INT_INT) {
                return indiceIntInt.delete(new RegistroIntInt((Integer) chave, (Integer) valor));
            }

            return indiceStringInt.delete(new RegistroStringInt((String) chave, (Integer) valor));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover da arvore B+", e);
        }
    }

    public synchronized boolean delete(K chave) {
        boolean removeu = false;

        for (V valor : read(chave)) {
            removeu = delete(chave, valor) || removeu;
        }

        return removeu;
    }

    public synchronized void clear() {
        fecharIndice();

        try {
            try (RandomAccessFile raf = new RandomAccessFile(arquivoArvore.toFile(), "rw")) {
                raf.setLength(0);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao limpar arvore B+", e);
        }

        abrirIndice();
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<K, ArrayList<V>> snapshot() {
        TreeMap<K, ArrayList<V>> mapa = new TreeMap<>();

        try {
            if (tipo == Tipo.INT_INT) {
                for (RegistroIntInt registro : indiceIntInt.read(null)) {
                    mapa.computeIfAbsent((K) Integer.valueOf(registro.id1), chave -> new ArrayList<>())
                        .add((V) Integer.valueOf(registro.id2));
                }
                return mapa;
            }

            for (RegistroStringInt registro : indiceStringInt.read(null)) {
                mapa.computeIfAbsent((K) registro.chave, chave -> new ArrayList<>())
                    .add((V) Integer.valueOf(registro.id));
            }
            return mapa;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar snapshot da arvore B+", e);
        }
    }

    private void abrirIndice() {
        prepararArquivo();

        try {
            if (tipo == Tipo.INT_INT) {
                indiceIntInt = new aed3.ArvoreBMais<>(
                    RegistroIntInt.class.getConstructor(),
                    ORDEM_PADRAO,
                    arquivoArvore.toString()
                );
                indiceStringInt = null;
                return;
            }

            indiceStringInt = new aed3.ArvoreBMais<>(
                RegistroStringInt.class.getConstructor(),
                ORDEM_PADRAO,
                arquivoArvore.toString()
            );
            indiceIntInt = null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao abrir arvore B+", e);
        }
    }

    private void fecharIndice() {
        try {
            if (indiceIntInt != null) {
                indiceIntInt.close();
            }
            if (indiceStringInt != null) {
                indiceStringInt.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fechar arvore B+", e);
        }
    }

    private void prepararArquivo() {
        try {
            Path pasta = arquivoArvore.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            if (!Files.exists(arquivoArvore)) {
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoArvore.toFile()))) {
                if (ois.readObject() instanceof Map<?, ?>) {
                    Files.deleteIfExists(arquivoArvore);
                }
            } catch (Exception ignored) {
                // Arquivo ja esta no formato da arvore.
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao preparar arquivo da arvore B+", e);
        }
    }

    private static Tipo inferirTipo(String caminhoArquivo) {
        return caminhoArquivo.toLowerCase().contains("nome") ? Tipo.STRING_INT : Tipo.INT_INT;
    }

    private static void escreverTextoFixo(DataOutputStream out, String valor) throws IOException {
        String texto = valor == null ? "" : valor;
        int limite = Math.min(texto.length(), TAMANHO_TEXTO);

        for (int i = 0; i < TAMANHO_TEXTO; i++) {
            char caractere = i < limite ? texto.charAt(i) : '\0';
            out.writeChar(caractere);
        }
    }

    private static String lerTextoFixo(DataInputStream in) throws IOException {
        StringBuilder sb = new StringBuilder(TAMANHO_TEXTO);

        for (int i = 0; i < TAMANHO_TEXTO; i++) {
            char caractere = in.readChar();
            if (caractere != '\0') {
                sb.append(caractere);
            }
        }

        return sb.toString();
    }

    public static class RegistroIntInt implements RegistroArvoreBMais<RegistroIntInt> {

        private int id1;
        private int id2;
        private boolean somentePrimeiraChave;

        public RegistroIntInt() {
            this(0, 0, false);
        }

        public RegistroIntInt(int id1) {
            this(id1, 0, true);
        }

        public RegistroIntInt(int id1, int id2) {
            this(id1, id2, false);
        }

        private RegistroIntInt(int id1, int id2, boolean somentePrimeiraChave) {
            this.id1 = id1;
            this.id2 = id2;
            this.somentePrimeiraChave = somentePrimeiraChave;
        }

        @Override
        public short size() {
            return 8;
        }

        @Override
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            out.writeInt(id1);
            out.writeInt(id2);
            return ba.toByteArray();
        }

        @Override
        public void fromByteArray(byte[] ba) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ba));
            id1 = in.readInt();
            id2 = in.readInt();
            somentePrimeiraChave = false;
        }

        @Override
        public int compareTo(RegistroIntInt obj) {
            int comparacao = Integer.compare(id1, obj.id1);

            if (comparacao != 0 || somentePrimeiraChave || obj.somentePrimeiraChave) {
                return comparacao;
            }

            return Integer.compare(id2, obj.id2);
        }

        @Override
        public RegistroIntInt clone() {
            return new RegistroIntInt(id1, id2, somentePrimeiraChave);
        }
    }

    public static class RegistroStringInt implements RegistroArvoreBMais<RegistroStringInt> {

        private String chave;
        private int id;
        private boolean somentePrimeiraChave;

        public RegistroStringInt() {
            this("", 0, false);
        }

        public RegistroStringInt(String chave) {
            this(chave, 0, true);
        }

        public RegistroStringInt(String chave, int id) {
            this(chave, id, false);
        }

        private RegistroStringInt(String chave, int id, boolean somentePrimeiraChave) {
            this.chave = chave == null ? "" : chave;
            this.id = id;
            this.somentePrimeiraChave = somentePrimeiraChave;
        }

        @Override
        public short size() {
            return (short) (TAMANHO_TEXTO * 2 + 4);
        }

        @Override
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            escreverTextoFixo(out, chave);
            out.writeInt(id);
            return ba.toByteArray();
        }

        @Override
        public void fromByteArray(byte[] ba) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ba));
            chave = lerTextoFixo(in);
            id = in.readInt();
            somentePrimeiraChave = false;
        }

        @Override
        public int compareTo(RegistroStringInt obj) {
            int comparacao = String.CASE_INSENSITIVE_ORDER.compare(chave, obj.chave);

            if (comparacao != 0 || somentePrimeiraChave || obj.somentePrimeiraChave) {
                return comparacao;
            }

            return Integer.compare(id, obj.id);
        }

        @Override
        public RegistroStringInt clone() {
            return new RegistroStringInt(chave, id, somentePrimeiraChave);
        }
    }
}
