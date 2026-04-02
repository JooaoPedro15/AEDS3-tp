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

public class ArquivoIndexado<T extends Registro> {

    private static final int TAMANHO_CABECALHO = 4;

    private final Supplier<T> fabrica;
    private final RandomAccessFile arquivo;

    public ArquivoIndexado(String caminhoArquivo, Supplier<T> fabrica) {
        try {
            this.fabrica = fabrica;
            Path caminho = Paths.get(caminhoArquivo);
            Path pasta = caminho.getParent();

            if (pasta != null) {
                Files.createDirectories(pasta);
            }

            this.arquivo = new RandomAccessFile(caminho.toFile(), "rw");

            if (arquivo.length() < TAMANHO_CABECALHO) {
                arquivo.seek(0);
                arquivo.writeInt(0);
            }
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

            arquivo.seek(arquivo.length());
            arquivo.writeByte(0);
            arquivo.writeShort(bytes.length);
            arquivo.write(bytes);

            return novoId;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar registro", e);
        }
    }

    public synchronized T read(int id) {
        try {
            arquivo.seek(TAMANHO_CABECALHO);

            while (arquivo.getFilePointer() < arquivo.length()) {
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readUnsignedShort();
                byte[] dados = new byte[tamanho];
                arquivo.readFully(dados);

                if (lapide == 0) {
                    T registro = fabrica.get();
                    registro.fromByteArray(dados);

                    if (registro.getId() == id) {
                        return registro;
                    }
                }
            }

            return null;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler registro id=" + id, e);
        }
    }

    public synchronized boolean update(T registroAtualizado) {
        try {
            arquivo.seek(TAMANHO_CABECALHO);

            while (arquivo.getFilePointer() < arquivo.length()) {
                long enderecoLapide = arquivo.getFilePointer();
                byte lapide = arquivo.readByte();
                int tamanhoOriginal = arquivo.readUnsignedShort();
                long enderecoDados = arquivo.getFilePointer();

                byte[] dados = new byte[tamanhoOriginal];
                arquivo.readFully(dados);

                if (lapide != 0) {
                    continue;
                }

                T registro = fabrica.get();
                registro.fromByteArray(dados);

                if (registro.getId() != registroAtualizado.getId()) {
                    continue;
                }

                byte[] novosDados = registroAtualizado.toByteArray();
                validarTamanhoRegistro(novosDados.length);

                if (novosDados.length <= tamanhoOriginal) {
                    arquivo.seek(enderecoDados);
                    arquivo.write(novosDados);

                    for (int i = novosDados.length; i < tamanhoOriginal; i++) {
                        arquivo.writeByte(0);
                    }
                } else {
                    arquivo.seek(enderecoLapide);
                    arquivo.writeByte(1);

                    arquivo.seek(arquivo.length());
                    arquivo.writeByte(0);
                    arquivo.writeShort(novosDados.length);
                    arquivo.write(novosDados);
                }

                return true;
            }

            return false;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao atualizar registro id=" + registroAtualizado.getId(), e);
        }
    }

    public synchronized boolean delete(int id) {
        try {
            arquivo.seek(TAMANHO_CABECALHO);

            while (arquivo.getFilePointer() < arquivo.length()) {
                long enderecoLapide = arquivo.getFilePointer();
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readUnsignedShort();

                byte[] dados = new byte[tamanho];
                arquivo.readFully(dados);

                if (lapide != 0) {
                    continue;
                }

                T registro = fabrica.get();
                registro.fromByteArray(dados);

                if (registro.getId() == id) {
                    arquivo.seek(enderecoLapide);
                    arquivo.writeByte(1);
                    return true;
                }
            }

            return false;
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

                if (lapide == 0) {
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
}
