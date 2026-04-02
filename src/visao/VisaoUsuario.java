package visao;

import entidades.Usuario;
import utils.Entrada;

public class VisaoUsuario {

    public Usuario leUsuario() {
        System.out.println("\nNOVO USUARIO");

        System.out.print("Nome: ");
        String nome = lerLinha();

        System.out.print("E-mail: ");
        String email = lerLinha().toLowerCase();

        System.out.print("Senha: ");
        String senha = lerLinha();

        System.out.print("Pergunta secreta: ");
        String pergunta = lerLinha();

        System.out.print("Resposta secreta: ");
        String resposta = lerLinha();

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setHashSenha(senha);
        usuario.setPerguntaSecreta(pergunta);
        usuario.setHashRespostaSecreta(resposta);

        return usuario;
    }

    public String[] leLogin() {
        System.out.println("\nLOGIN");

        System.out.print("E-mail: ");
        String email = lerLinha().toLowerCase();

        System.out.print("Senha: ");
        String senha = lerLinha();

        return new String[] {email, senha};
    }

    public Usuario leUsuarioParaAtualizacao(Usuario atual) {
        System.out.println("\nATUALIZAR DADOS (deixe em branco para manter)");

        Usuario usuario = new Usuario();
        usuario.setId(atual.getId());

        System.out.print("Nome (" + atual.getNome() + "): ");
        String nome = lerLinha();
        usuario.setNome(nome.isBlank() ? atual.getNome() : nome);

        System.out.print("E-mail (" + atual.getEmail() + "): ");
        String email = lerLinha().toLowerCase();
        usuario.setEmail(email.isBlank() ? atual.getEmail() : email);

        System.out.print("Nova senha: ");
        String senha = lerLinha();
        usuario.setHashSenha(senha.isBlank() ? null : senha);

        System.out.print("Pergunta secreta (" + atual.getPerguntaSecreta() + "): ");
        String pergunta = lerLinha();
        usuario.setPerguntaSecreta(pergunta.isBlank() ? atual.getPerguntaSecreta() : pergunta);

        System.out.print("Nova resposta secreta: ");
        String resposta = lerLinha();
        usuario.setHashRespostaSecreta(resposta.isBlank() ? null : resposta);

        return usuario;
    }

    public void mostraUsuario(Usuario usuario) {
        System.out.println("\nMEUS DADOS");
        System.out.println("ID..............: " + usuario.getId());
        System.out.println("NOME............: " + usuario.getNome());
        System.out.println("E-MAIL..........: " + usuario.getEmail());
        System.out.println("PERGUNTA SECRETA: " + usuario.getPerguntaSecreta());
    }

    private String lerLinha() {
        return Entrada.SCANNER.nextLine().trim();
    }
}
