package estruturas;

import entidades.Registro;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArquivoIndexado<T extends Registro> implements AutoCloseable {

    private static final int TAMANHO_CABECALHO = 12;
    private static final byte LAPIDE_ATIVA = ' ';
    private static final byte LAPIDE_EXCLUIDA = '*';

    private final Supplier<T> fabrica;
    private final RandomAccessFile arquivo;
    private final TabelaHashExtensivel<Integer, Long> indiceDireto;

    public ArquivoIndexado(String caminhoArquivo, String caminhoIndiceDireto, Supplier<T> fabrica) {
        try {
            this.fabrica = fabrica;
            Path caminho = Paths.get(caminhoArquivo);
            Path pasta = caminho.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            this.arquivo = new RandomAccessFile(caminho.toFile(), "rw");
            migrarFormatoLegadoSeNecessario();

            if (arquivo.length() < TAMANHO_CABECALHO) {
                arquivo.seek(0);
                arquivo.writeInt(0);
                arquivo.writeLong(-1L);
            }

            this.indiceDireto = new TabelaHashExtensivel<>(caminhoIndiceDireto, TabelaHashExtensivel.Tipo.INT_LONG);
            reconstruirIndiceDireto();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao abrir arquivo de dados: " + caminhoArquivo, e);
        }
    }

    public synchronized int create(T registro) {
        try {
            int novoId = lerUltimoId() + 1;
            registro.setId(novoId);

            byte[] bytes = registro.toByteArray();
            validarTamanhoRegistro(bytes.length);

            arquivo.seek(0);
            arquivo.writeInt(novoId);

            long endereco = encontrarEspacoLivre((short) bytes.length);

            if (endereco == -1) {
                endereco = arquivo.length();
                arquivo.seek(endereco);
                arquivo.writeByte(LAPIDE_ATIVA);
                arquivo.writeShort(bytes.length);
            } else {
                arquivo.seek(endereco);
                arquivo.writeByte(LAPIDE_ATIVA);
                arquivo.skipBytes(2);
            }

            arquivo.write(bytes);
            indiceDireto.upsert(novoId, endereco);

            return novoId;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar registro", e);
        }
    }

    public synchronized T read(int id) {
        try {
            Long endereco = indiceDireto.read(id);

            if (endereco == null) {
                return null;
            }

            return lerRegistro(endereco, id);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler registro id=" + id, e);
        }
    }

    public synchronized boolean update(T registroAtualizado) {
        try {
            Long endereco = indiceDireto.read(registroAtualizado.getId());

            if (endereco == null) {
                return false;
            }

            arquivo.seek(endereco);
            byte lapide = arquivo.readByte();
            short tamanhoOriginal = arquivo.readShort();

            if (lapide != LAPIDE_ATIVA) {
                return false;
            }

            byte[] dadosAtuais = new byte[tamanhoOriginal];
            arquivo.readFully(dadosAtuais);

            T registro = fabrica.get();
            registro.fromByteArray(dadosAtuais);

            if (registro.getId() != registroAtualizado.getId()) {
                return false;
            }

            byte[] novosDados = registroAtualizado.toByteArray();
            validarTamanhoRegistro(novosDados.length);

            if (novosDados.length <= tamanhoOriginal) {
                arquivo.seek(endereco + 3);
                arquivo.write(novosDados);

                for (int i = novosDados.length; i < tamanhoOriginal; i++) {
                    arquivo.writeByte(0);
                }
            } else {
                marcarComoExcluido(endereco, tamanhoOriginal);
                inserirEspacoLivre(endereco, tamanhoOriginal);

                long novoEndereco = encontrarEspacoLivre((short) novosDados.length);

                if (novoEndereco == -1) {
                    novoEndereco = arquivo.length();
                    arquivo.seek(novoEndereco);
                    arquivo.writeByte(LAPIDE_ATIVA);
                    arquivo.writeShort(novosDados.length);
                } else {
                    arquivo.seek(novoEndereco);
                    arquivo.writeByte(LAPIDE_ATIVA);
                    arquivo.skipBytes(2);
                }

                arquivo.write(novosDados);
                indiceDireto.upsert(registroAtualizado.getId(), novoEndereco);
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao atualizar registro id=" + registroAtualizado.getId(), e);
        }
    }

    public synchronized boolean delete(int id) {
        try {
            Long endereco = indiceDireto.read(id);

            if (endereco == null) {
                return false;
            }

            arquivo.seek(endereco);
            byte lapide = arquivo.readByte();
            short tamanho = arquivo.readShort();

            if (lapide != LAPIDE_ATIVA) {
                return false;
            }

            byte[] dados = new byte[tamanho];
            arquivo.readFully(dados);

            T registro = fabrica.get();
            registro.fromByteArray(dados);

            if (registro.getId() != id) {
                return false;
            }

            marcarComoExcluido(endereco, tamanho);
            inserirEspacoLivre(endereco, tamanho);
            indiceDireto.delete(id);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao excluir registro id=" + id, e);
        }
    }

    public synchronized List<T> readAll() {
        try {
            List<T> registros = new ArrayList<>();
            arquivo.seek(TAMANHO_CABECALHO);

            while (arquivo.getFilePointer() < arquivo.length()) {
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readUnsignedShort();

                byte[] dados = new byte[tamanho];
                arquivo.readFully(dados);

                if (lapide == LAPIDE_ATIVA) {
                    T registro = fabrica.get();
                    registro.fromByteArray(dados);
                    registros.add(registro);
                }
            }

            return registros;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao listar registros", e);
        }
    }

    private int lerUltimoId() throws IOException {
        arquivo.seek(0);
        return arquivo.readInt();
    }

    private void validarTamanhoRegistro(int tamanho) {
        if (tamanho > 0xFFFF) {
            throw new IllegalArgumentException("Registro maior que o limite de 65535 bytes");
        }
    }

    @Override
    public synchronized void close() {
        try {
            arquivo.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fechar arquivo de dados", e);
        }
    }

    private T lerRegistro(long endereco, int idEsperado) throws IOException {
        arquivo.seek(endereco);
        byte lapide = arquivo.readByte();
        short tamanho = arquivo.readShort();

        if (lapide != LAPIDE_ATIVA) {
            return null;
        }

        byte[] dados = new byte[tamanho];
        arquivo.readFully(dados);

        T registro = fabrica.get();
        registro.fromByteArray(dados);
        return registro.getId() == idEsperado ? registro : null;
    }

    private void marcarComoExcluido(long endereco, short tamanho) throws IOException {
        arquivo.seek(endereco);
        arquivo.writeByte(LAPIDE_EXCLUIDA);
        arquivo.skipBytes(2);

        for (int i = 0; i < tamanho; i++) {
            arquivo.writeByte(0);
        }
    }

    private void inserirEspacoLivre(long endereco, short tamanho) throws IOException {
        arquivo.seek(4);
        long atual = arquivo.readLong();

        if (atual == -1) {
            arquivo.seek(4);
            arquivo.writeLong(endereco);
            arquivo.seek(endereco + 3);
            arquivo.writeLong(-1L);
            return;
        }

        long anterior = 4;

        while (atual != -1) {
            arquivo.seek(atual + 1);
            short tamanhoAtual = arquivo.readShort();

            if (tamanho < tamanhoAtual) {
                break;
            }

            anterior = atual;
            atual = arquivo.readLong();
        }

        arquivo.seek(endereco + 3);
        arquivo.writeLong(atual);

        if (anterior == 4) {
            arquivo.seek(4);
        } else {
            arquivo.seek(anterior + 3);
        }

        arquivo.writeLong(endereco);
    }

    private long encontrarEspacoLivre(short tamanho) throws IOException {
        arquivo.seek(4);
        long atual = arquivo.readLong();

        if (atual == -1) {
            return -1;
        }

        long anterior = 4;

        while (atual != -1) {
            arquivo.seek(atual + 1);
            short tamanhoAtual = arquivo.readShort();
            long proximo = arquivo.readLong();

            if (tamanho <= tamanhoAtual) {
                if (anterior == 4) {
                    arquivo.seek(4);
                } else {
                    arquivo.seek(anterior + 3);
                }

                arquivo.writeLong(proximo);
                return atual;
            }

            anterior = atual;
            atual = proximo;
        }

        return -1;
    }

    private void reconstruirIndiceDireto() throws IOException {
        indiceDireto.clear();
        arquivo.seek(TAMANHO_CABECALHO);

        while (arquivo.getFilePointer() < arquivo.length()) {
            long endereco = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            int tamanho = arquivo.readUnsignedShort();
            byte[] dados = new byte[tamanho];
            arquivo.readFully(dados);

            if (lapide != LAPIDE_ATIVA) {
                continue;
            }

            T registro = fabrica.get();
            registro.fromByteArray(dados);
            indiceDireto.upsert(registro.getId(), endereco);
        }
    }

    private void migrarFormatoLegadoSeNecessario() throws IOException {
        if (arquivo.length() == 0 || estaNoFormatoNovo()) {
            return;
        }

        int ultimoId = lerUltimoId();
        List<byte[]> registrosAtivos = new ArrayList<>();
        arquivo.seek(4);

        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            int tamanho = arquivo.readUnsignedShort();
            byte[] dados = new byte[tamanho];
            arquivo.readFully(dados);

            if (lapide == 0) {
                registrosAtivos.add(dados);
            }
        }

        arquivo.setLength(0);
        arquivo.seek(0);
        arquivo.writeInt(ultimoId);
        arquivo.writeLong(-1L);

        for (byte[] dados : registrosAtivos) {
            validarTamanhoRegistro(dados.length);
            arquivo.writeByte(LAPIDE_ATIVA);
            arquivo.writeShort(dados.length);
            arquivo.write(dados);
        }
    }

    private boolean estaNoFormatoNovo() throws IOException {
        if (arquivo.length() < TAMANHO_CABECALHO) {
            return false;
        }

        arquivo.seek(4);
        long cabecaLivres = arquivo.readLong();

        if (cabecaLivres == -1) {
            return true;
        }

        if (cabecaLivres >= TAMANHO_CABECALHO && cabecaLivres < arquivo.length()) {
            arquivo.seek(cabecaLivres);
            return arquivo.readByte() == LAPIDE_EXCLUIDA;
        }

        return false;
    }
}
