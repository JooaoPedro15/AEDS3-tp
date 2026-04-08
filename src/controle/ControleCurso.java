package controle;

import arquivos.ArquivoCursos;
import entidades.Curso;
import entidades.Usuario;
import utils.Entrada;
import utils.GeradorCodigo;
import visao.VisaoCurso;

import java.util.List;

public class ControleCurso {

    private final ArquivoCursos arq;
    private final VisaoCurso visao;

    public ControleCurso() {
        this.arq = new ArquivoCursos();
        this.visao = new VisaoCurso();
    }

    public void menu(Usuario usuario) {
        while (true) {
            System.out.println("\n> Inicio > Meus cursos");

            List<Curso> lista = arq.listarPorUsuario(usuario.getId());
            visao.listarCursos(lista);

            System.out.println("\n(A) Novo curso");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String opcao = Entrada.SCANNER.nextLine().trim();

            if (opcao.equalsIgnoreCase("A")) {
                criar(usuario);
                continue;
            }

            if (opcao.equalsIgnoreCase("R")) {
                break;
            }

            Integer indiceEscolhido = parseIndice(opcao);

            if (indiceEscolhido == null || indiceEscolhido < 1 || indiceEscolhido > lista.size()) {
                System.out.println("Opcao invalida.");
                continue;
            }

            Curso cursoSelecionado = lista.get(indiceEscolhido - 1);
            menuCurso(cursoSelecionado.getId());
        }
    }

    public void criar(Usuario usuario) {
        Curso curso = visao.leCurso();

        if (curso.getNome() == null || curso.getNome().isBlank()) {
            System.out.println("Nome do curso e obrigatorio.");
            return;
        }

        if (curso.getDataInicio() == null || curso.getDataInicio().isBlank()) {
            System.out.println("Data de inicio e obrigatoria.");
            return;
        }

        curso.setIdUsuario(usuario.getId());
        curso.setCodigo(gerarCodigoUnico());
        curso.setEstado(0);

        int id = arq.create(curso);

        if (id < 0) {
            System.out.println("Falha ao criar curso.");
            return;
        }

        System.out.println("Curso criado com sucesso. ID: " + id);
    }

    private void menuCurso(int idCurso) {
        while (true) {
            Curso curso = arq.read(idCurso);

            if (curso == null) {
                System.out.println("Curso nao encontrado.");
                return;
            }

            System.out.println("\n> Inicio > Meus cursos > " + curso.getNome());
            visao.mostrarCurso(curso);

            System.out.println("\n(A) Gerenciar inscritos no curso");
            System.out.println("(B) Corrigir dados do curso");
            System.out.println("(C) Encerrar inscricoes");
            System.out.println("(D) Concluir curso");
            System.out.println("(E) Cancelar curso");
            System.out.println("(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String opcao = Entrada.SCANNER.nextLine().trim();

            if (opcao.equalsIgnoreCase("A")) {
                System.out.println("Gerenciamento de inscritos sera implementado no TP2.");
                continue;
            }

            if (opcao.equalsIgnoreCase("B")) {
                corrigirDados(curso);
                continue;
            }

            if (opcao.equalsIgnoreCase("C")) {
                encerrarInscricoes(curso);
                continue;
            }

            if (opcao.equalsIgnoreCase("D")) {
                concluirCurso(curso);
                continue;
            }

            if (opcao.equalsIgnoreCase("E")) {
                cancelarCurso(curso);
                continue;
            }

            if (opcao.equalsIgnoreCase("R")) {
                return;
            }

            System.out.println("Opcao invalida.");
        }
    }

    private void corrigirDados(Curso cursoAtual) {
        Curso editado = visao.leCursoParaAtualizacao(cursoAtual);

        if (editado.getNome() == null || editado.getNome().isBlank()) {
            System.out.println("Nome do curso nao pode ficar em branco.");
            return;
        }

        if (!arq.update(editado)) {
            System.out.println("Nao foi possivel atualizar os dados do curso.");
            return;
        }

        System.out.println("Curso atualizado com sucesso.");
    }

    private void encerrarInscricoes(Curso curso) {
        if (curso.getEstado() != 0) {
            System.out.println("Somente cursos em estado ativo (0) podem encerrar inscricoes.");
            return;
        }

        curso.setEstado(1);
        arq.update(curso);
        System.out.println("Inscricoes encerradas.");
    }

    private void concluirCurso(Curso curso) {
        if (curso.getEstado() == 3) {
            System.out.println("Curso cancelado nao pode ser concluido.");
            return;
        }

        curso.setEstado(2);
        arq.update(curso);
        System.out.println("Curso concluido.");
    }

    private void cancelarCurso(Curso curso) {
        if (curso.getEstado() == 2) {
            System.out.println("Curso concluido nao pode ser cancelado.");
            return;
        }

        boolean temInscritos = verificarInscritos(curso.getId()); //verifica se tem inscritos no curso

        if(temInscritos){
            //se sim, muda estado para cancelado
            curso.setEstado(3);
            arq.update(curso);
            System.out.println("Ha inscritos no curso, o curso foi cancelado!");
        }else{
            //caos contrario exclui o registro
            boolean excluiu = arq.delete(curso.getId());
            if(excluiu){
                System.out.println("Curso foi excluido com sucesso!");
            }else{
                System.out.println("Ocorreu um erro ao tentar excluir o curso!");
            }
        }
    }

    private boolean verificarInscritos(int id){
        return false;
    }

    private String gerarCodigoUnico() {
        String codigo;

        do {
            codigo = GeradorCodigo.gerar();
        } while (codigoExiste(codigo));

        return codigo;
    }

    private boolean codigoExiste(String codigo) {
        return arq.buscarPorCodigo(codigo) != null;
    }

    private Integer parseIndice(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
