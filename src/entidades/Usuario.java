package entidades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Usuario implements Registro {

    private int id;
    private String nome;
    private String email;
    private String hashSenha;
    private String perguntaSecreta;
    private String hashRespostaSecreta;

    public Usuario() {
        this(-1, "", "", "", "", "");
    }

    public Usuario(
        String nome,
        String email,
        String hashSenha,
        String perguntaSecreta,
        String hashRespostaSecreta
    ) {
        this(-1, nome, email, hashSenha, perguntaSecreta, hashRespostaSecreta);
    }

    public Usuario(
        int id,
        String nome,
        String email,
        String hashSenha,
        String perguntaSecreta,
        String hashRespostaSecreta
    ) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.hashSenha = hashSenha;
        this.perguntaSecreta = perguntaSecreta;
        this.hashRespostaSecreta = hashRespostaSecreta;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashSenha() {
        return hashSenha;
    }

    public void setHashSenha(String hashSenha) {
        this.hashSenha = hashSenha;
    }

    public String getPerguntaSecreta() {
        return perguntaSecreta;
    }

    public void setPerguntaSecreta(String perguntaSecreta) {
        this.perguntaSecreta = perguntaSecreta;
    }

    public String getHashRespostaSecreta() {
        return hashRespostaSecreta;
    }

    public void setHashRespostaSecreta(String hashRespostaSecreta) {
        this.hashRespostaSecreta = hashRespostaSecreta;
    }

    // Compatibilidade com o nome antigo.
    public String getRespostaSecreta() {
        return hashRespostaSecreta;
    }

    // Compatibilidade com o nome antigo.
    public void setRespostaSecreta(String respostaSecreta) {
        this.hashRespostaSecreta = respostaSecreta;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        DataOutputStream da = new DataOutputStream(ba);

        da.writeInt(id);
        da.writeUTF(valorOuVazio(nome));
        da.writeUTF(valorOuVazio(email));
        da.writeUTF(valorOuVazio(hashSenha));
        da.writeUTF(valorOuVazio(perguntaSecreta));
        da.writeUTF(valorOuVazio(hashRespostaSecreta));

        return ba.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bi = new ByteArrayInputStream(ba);
        DataInputStream di = new DataInputStream(bi);

        id = di.readInt();
        nome = di.readUTF();
        email = di.readUTF();
        hashSenha = di.readUTF();
        perguntaSecreta = di.readUTF();
        hashRespostaSecreta = di.readUTF();
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
