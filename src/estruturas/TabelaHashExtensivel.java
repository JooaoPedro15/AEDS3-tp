package estruturas;

import aed3.HashExtensivel;
import aed3.RegistroHashExtensivel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TabelaHashExtensivel<K extends Serializable, V extends Serializable> {

    private static final int QUANTIDADE_DADOS_POR_CESTO = 4;
    private static final int TAMANHO_TEXTO = 160;

    public enum Tipo {
        INT_LONG,
        STRING_INT
    }

    private final Path arquivoDiretorio;
    private final Path arquivoCestos;
    private final Path arquivoEspelho;
    private final Tipo tipo;

    private HashExtensivel<RegistroIntLong> indiceIntLong;
    private HashExtensivel<RegistroStringInt> indiceStringInt;
    private Map<K, V> espelho;

    public TabelaHashExtensivel(String caminhoArquivo) {
        this(caminhoArquivo, inferirTipo(caminhoArquivo));
    }

    public TabelaHashExtensivel(String caminhoArquivo, Tipo tipo) {
        this.arquivoDiretorio = Paths.get(caminhoArquivo);
        this.arquivoCestos = Paths.get(caminhoArquivo + ".cestos");
        this.arquivoEspelho = Paths.get(caminhoArquivo + ".espelho");
        this.tipo = tipo;
        this.espelho = carregarEspelho();
        abrirIndice();
    }

    public synchronized boolean create(K chave, V valor) {
        try {
            boolean criou = criarRegistro(chave, valor);

            if (criou) {
                espelho.put(chave, valor);
                salvarEspelho();
            }

            return criou;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized V read(K chave) {
        try {
            if (tipo == Tipo.INT_LONG) {
                RegistroIntLong registro = indiceIntLong.read(((Integer) chave).intValue());
                return registro == null ? null : (V) Long.valueOf(registro.valor);
            }

            RegistroStringInt registro = indiceStringInt.read(chave.hashCode());
            if (registro == null || !registro.chave.equals(chave)) {
                return null;
            }

            return (V) Integer.valueOf(registro.valor);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler hash extensivel", e);
        }
    }

    public synchronized boolean update(K chave, V valor) {
        try {
            boolean atualizou = atualizarRegistro(chave, valor);

            if (atualizou) {
                espelho.put(chave, valor);
                salvarEspelho();
            }

            return atualizou;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar hash extensivel", e);
        }
    }

    public synchronized void upsert(K chave, V valor) {
        if (!update(chave, valor)) {
            if (!create(chave, valor)) {
                throw new RuntimeException("Nao foi possivel inserir chave no hash extensivel");
            }
        }
    }

    public synchronized boolean delete(K chave) {
        try {
            boolean removeu = removerRegistro(chave);

            if (removeu) {
                espelho.remove(chave);
                salvarEspelho();
            }

            return removeu;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover hash extensivel", e);
        }
    }

    public synchronized boolean containsKey(K chave) {
        return read(chave) != null;
    }

    public synchronized void clear() {
        fecharIndice();

        try {
            reiniciarArquivo(arquivoDiretorio);
            reiniciarArquivo(arquivoCestos);
            Files.deleteIfExists(arquivoEspelho);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao limpar arquivos do hash extensivel", e);
        }

        espelho = new HashMap<>();
        abrirIndice();
        salvarEspelho();
    }

    public synchronized Map<K, V> snapshot() {
        return new HashMap<>(espelho);
    }

    private boolean criarRegistro(K chave, V valor) throws Exception {
        if (tipo == Tipo.INT_LONG) {
            return indiceIntLong.create(new RegistroIntLong((Integer) chave, (Long) valor));
        }

        return indiceStringInt.create(new RegistroStringInt((String) chave, (Integer) valor));
    }

    private boolean atualizarRegistro(K chave, V valor) throws Exception {
        if (tipo == Tipo.INT_LONG) {
            return indiceIntLong.update(new RegistroIntLong((Integer) chave, (Long) valor));
        }

        return indiceStringInt.update(new RegistroStringInt((String) chave, (Integer) valor));
    }

    private boolean removerRegistro(K chave) throws Exception {
        if (tipo == Tipo.INT_LONG) {
            return indiceIntLong.delete(((Integer) chave).intValue());
        }

        RegistroStringInt atual = indiceStringInt.read(chave.hashCode());
        if (atual == null || !atual.chave.equals(chave)) {
            return false;
        }

        return indiceStringInt.delete(chave.hashCode());
    }

    private void abrirIndice() {
        prepararArquivos();

        try {
            if (tipo == Tipo.INT_LONG) {
                indiceIntLong = new HashExtensivel<>(
                    RegistroIntLong.class.getConstructor(),
                    QUANTIDADE_DADOS_POR_CESTO,
                    arquivoDiretorio.toString(),
                    arquivoCestos.toString()
                );
                indiceStringInt = null;
                return;
            }

            indiceStringInt = new HashExtensivel<>(
                RegistroStringInt.class.getConstructor(),
                QUANTIDADE_DADOS_POR_CESTO,
                arquivoDiretorio.toString(),
                arquivoCestos.toString()
            );
            indiceIntLong = null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao abrir hash extensivel", e);
        }
    }

    private void fecharIndice() {
        try {
            if (indiceIntLong != null) {
                indiceIntLong.close();
            }
            if (indiceStringInt != null) {
                indiceStringInt.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fechar hash extensivel", e);
        }
    }

    private void prepararArquivos() {
        try {
            Path pasta = arquivoDiretorio.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            if (arquivoEhMapaLegado(arquivoDiretorio)) {
                Files.deleteIfExists(arquivoDiretorio);
                Files.deleteIfExists(arquivoCestos);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao preparar arquivos do hash extensivel", e);
        }
    }

    private boolean arquivoEhMapaLegado(Path arquivo) {
        if (!Files.exists(arquivo)) {
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo.toFile()))) {
            return ois.readObject() instanceof Map<?, ?>;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<K, V> carregarEspelho() {
        if (!Files.exists(arquivoEspelho)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivoEspelho.toFile()))) {
            Object objeto = ois.readObject();

            if (objeto instanceof Map<?, ?> mapa) {
                return (Map<K, V>) mapa;
            }
        } catch (Exception ignored) {
            // Espelho ausente ou em formato invalido.
        }

        return new HashMap<>();
    }

    private void salvarEspelho() {
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivoEspelho.toFile()))) {
                oos.writeObject(espelho);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar espelho do hash extensivel", e);
        }
    }

    private void reiniciarArquivo(Path arquivo) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.toFile(), "rw")) {
            raf.setLength(0);
        }
    }

    private static Tipo inferirTipo(String caminhoArquivo) {
        String caminho = caminhoArquivo.toLowerCase();

        if (caminho.contains("email") || caminho.contains("codigo")) {
            return Tipo.STRING_INT;
        }

        return Tipo.INT_LONG;
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

    public static class RegistroIntLong implements RegistroHashExtensivel<RegistroIntLong> {

        private int chave;
        private long valor;

        public RegistroIntLong() {
            this(0, -1L);
        }

        public RegistroIntLong(int chave, long valor) {
            this.chave = chave;
            this.valor = valor;
        }

        @Override
        public int hashCode() {
            return chave;
        }

        @Override
        public short size() {
            return 12;
        }

        @Override
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            out.writeInt(chave);
            out.writeLong(valor);
            return ba.toByteArray();
        }

        @Override
        public void fromByteArray(byte[] ba) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ba));
            chave = in.readInt();
            valor = in.readLong();
        }
    }

    public static class RegistroStringInt implements RegistroHashExtensivel<RegistroStringInt> {

        private String chave;
        private int valor;

        public RegistroStringInt() {
            this("", -1);
        }

        public RegistroStringInt(String chave, int valor) {
            this.chave = chave == null ? "" : chave;
            this.valor = valor;
        }

        @Override
        public int hashCode() {
            return chave.hashCode();
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
            out.writeInt(valor);
            return ba.toByteArray();
        }

        @Override
        public void fromByteArray(byte[] ba) throws IOException {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(ba));
            chave = lerTextoFixo(in);
            valor = in.readInt();
        }
    }
}
