package entidades.livros;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;

import aed3.InterfaceEntidade;



public class Livro implements InterfaceEntidade {
    
    private int idLivro;
    private String titulo;
    private int idAutor;
    private String isbn;
    private byte edicao;
    private LocalDate dataPublicacao;
    private float preco;

    public Livro() {
        this(-1, "", -1, "", 0, LocalDate.now(), 0F);
    }

    public Livro(String n, int a, String c, int e, LocalDate d, float r) {
        this(-1, n, a, c, e, d, r);
    }

    public Livro(int i, String n, int a, String c, int e, LocalDate d, float r) {
        idLivro = i;
        titulo = n;
        idAutor = a;
        isbn = c;
        edicao = (byte)e;
        dataPublicacao = d;
        preco = r;
    }

    public int getID() {
        return idLivro;
    }

    public void setID(int idLivro) {
        this.idLivro = idLivro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getIdAutor() {
        return idAutor;
    }

    public void setAutor(int autor) {
        this.idAutor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public byte getEdicao() {
        return edicao;
    }

    public void setEdicao(byte edicao) {
        this.edicao = edicao;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    @Override
    public String toString() {
        return   "ID........: " + idLivro + 
               "\nTítulo....: " + titulo +
               "\nAutor.....: " + idAutor +
               "\nISBN......: " + isbn +
               "\nEdição....: " + edicao +
               "\nDt.Public.: " + dataPublicacao +
               "\nPreço.....: R$ " + preco + "\n";
    }
    
    public byte[] toByteArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(idLivro);
        dos.writeUTF(titulo);
        dos.writeInt(idAutor);
        dos.write(isbn.getBytes());
        dos.writeByte(edicao);
        dos.writeInt((int)dataPublicacao.toEpochDay());
        dos.writeFloat(preco);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] vb) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(vb);
        DataInputStream dis = new DataInputStream(bais);
        idLivro = dis.readInt();
        titulo = dis.readUTF();
        idAutor = dis.readInt();
        byte[] aux = new byte[13];
        dis.read(aux);
        isbn = new String(aux);
        edicao = dis.readByte();
        dataPublicacao = LocalDate.ofEpochDay(dis.readInt());
        preco = dis.readFloat();
    }

}
