package controle;

import arquivos.ArquivoCursos;
import arquivos.ArquivoUsuarios;
import entidades.Usuario;
import utils.Entrada;
import utils.HashSenha;
import visao.VisaoUsuario;

public class ControleUsuario {

    private final ArquivoUsuarios arqUsuarios;
    private final ArquivoCursos arqCursos;
    private final VisaoUsuario visao;

    public ControleUsuario(ArquivoCursos arqCursos) {
        this.arqUsuarios = new ArquivoUsuarios();
        this.arqCursos = arqCursos;
        this.visao = new VisaoUsuario();
    }

    public void cadastrar() {
        Usuario usuario = visao.leUsuario();

        if (!dadosObrigatoriosPreenchidos(usuario)) {
            System.out.println("Cadastro invalido: preencha todos os campos.");
            return;
        }

        usuario.setHashSenha(HashSenha.hash(usuario.getHashSenha()));
        usuario.setHashRespostaSecreta(HashSenha.hash(usuario.getHashRespostaSecreta().trim().toLowerCase()));

        int id = arqUsuarios.create(usuario);

        if (id < 0) {
            System.out.println("E-mail ja cadastrado.");
            return;
        }

        System.out.println("Usuario cadastrado com sucesso. ID: " + id);
    }

    public Usuario login() {
        String[] dados = visao.leLogin();
        Usuario usuario = arqUsuarios.buscarPorEmail(dados[0]);

        if (usuario == null) {
            System.out.println("Login invalido.");
            return null;
        }

        String hashInformado = HashSenha.hash(dados[1]);

        if (!hashInformado.equals(usuario.getHashSenha())) {
            System.out.println("Login invalido.");
            return null;
        }

        System.out.println("Login realizado com sucesso.");
        return usuario;
    }

    public void recuperarSenha(){
        String email = visao.leEmailRecuperacao();
        Usuario usuario = arqUsuarios.buscarPorEmail(email);

        if(usuario == null){ //se nao existe
            System.out.println("Erro: o e-mail nao foi encontrado no sistema.");
            return;
        }

        String resposta = visao.leRespostaSecreta(usuario.getPerguntaSecreta());

        String hashResposta = HashSenha.hash(resposta.trim().toLowerCase()); //padroniza a resposta antes de transformar em hash

        //se resposta digitada nao for equialente a armazenada
        if(!hashResposta.equals(usuario.getHashRespostaSecreta())){
            System.out.println("A resposta esta incorreta!");
            System.out.println("A senha nao pode ser recuperada.");
            return;
        }

        //caso esteja correta, altera a senha
        String novaSenha = visao.leNovaSenha();
        if(novaSenha.isBlank()){ //se vazia
            System.out.println("A senha nao pode ficar em branco!");
            return;
        }

        usuario.setHashSenha(HashSenha.hash(novaSenha)); //atualiza o hash da senha

        if(arqUsuarios.update(usuario)){
            System.out.println("Senha atualizada com sucesso!");
        }else{
            System.out.println("Ocorreu um erro ao atualizar a nova senha.");
        }

    }

    public Usuario menuMeusDados(Usuario logado) {
        while (true) {
            System.out.println("\n> Inicio > Meus dados");
            visao.mostraUsuario(logado);

            System.out.println("\n(A) Corrigir meus dados");
            System.out.println("(B) Excluir meu usuario");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String opcao = Entrada.SCANNER.nextLine().trim();

            if (opcao.equalsIgnoreCase("A")) {
                logado = atualizarDados(logado);
                continue;
            }

            if (opcao.equalsIgnoreCase("B")) {
                boolean excluiu = excluirUsuario(logado);

                if (excluiu) {
                    return null;
                }

                continue;
            }

            if (opcao.equalsIgnoreCase("R")) {
                return logado;
            }

            System.out.println("Opcao invalida.");
        }
    }

    private Usuario atualizarDados(Usuario atual) {
        Usuario editado = visao.leUsuarioParaAtualizacao(atual);

        if (editado.getHashSenha() == null || editado.getHashSenha().isBlank()) {
            editado.setHashSenha(atual.getHashSenha());
        } else {
            editado.setHashSenha(HashSenha.hash(editado.getHashSenha()));
        }

        if (editado.getHashRespostaSecreta() == null || editado.getHashRespostaSecreta().isBlank()) {
            editado.setHashRespostaSecreta(atual.getHashRespostaSecreta());
        } else {
            editado.setHashRespostaSecreta(HashSenha.hash(editado.getHashRespostaSecreta()));
        }

        if (!arqUsuarios.update(editado)) {
            System.out.println("Nao foi possivel atualizar. Verifique se o e-mail ja existe.");
            return atual;
        }

        Usuario atualizado = arqUsuarios.read(atual.getId());
        System.out.println("Dados atualizados com sucesso.");

        if (atualizado == null) {
            return atual;
        }

        return atualizado;
    }

    private boolean excluirUsuario(Usuario usuario) {
        if (arqCursos.temCursosAtivosPorUsuario(usuario.getId())) {
            System.out.println("Nao e possivel excluir: ha cursos ativos vinculados ao usuario.");
            return false;
        }

        arqCursos.removerInativosDoUsuario(usuario.getId());

        if (!arqUsuarios.delete(usuario.getId())) {
            System.out.println("Falha ao excluir usuario.");
            return false;
        }

        System.out.println("Usuario excluido com sucesso.");
        return true;
    }

    private boolean dadosObrigatoriosPreenchidos(Usuario usuario) {
        return usuario.getNome() != null
            && !usuario.getNome().isBlank()
            && usuario.getEmail() != null
            && !usuario.getEmail().isBlank()
            && usuario.getHashSenha() != null
            && !usuario.getHashSenha().isBlank()
            && usuario.getPerguntaSecreta() != null
            && !usuario.getPerguntaSecreta().isBlank()
            && usuario.getHashRespostaSecreta() != null
            && !usuario.getHashRespostaSecreta().isBlank();
    }
}
