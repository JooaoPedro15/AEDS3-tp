package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Curso implements Registro {

    private int id;
    private int idUsuario;
    private String nome;
    private String descricao;
    private String dataInicio;
    private String codigo;
    private int estado;

    public Curso() {
        this(-1, -1, "", "", "", "", 0);
    }

    public Curso(int idUsuario, String nome, String descricao, String dataInicio, String codigo, int estado) {
        this(-1, idUsuario, nome, descricao, dataInicio, codigo, estado);
    }

    public Curso(int id, int idUsuario, String nome, String descricao, String dataInicio, String codigo, int estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.codigo = codigo;
        this.estado = estado;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream da = new DataOutputStream(ba);

        da.writeInt(id);
        da.writeInt(idUsuario);
        da.writeUTF(valorOuVazio(nome));
        da.writeUTF(valorOuVazio(descricao));
        da.writeUTF(valorOuVazio(dataInicio));
        da.writeUTF(valorOuVazio(codigo));
        da.writeInt(estado);

        return ba.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bi = new ByteArrayInputStream(ba);
        DataInputStream di = new DataInputStream(bi);

        id = di.readInt();
        idUsuario = di.readInt();
        nome = di.readUTF();
        descricao = di.readUTF();
        dataInicio = di.readUTF();
        codigo = di.readUTF();
        estado = di.readInt();
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
