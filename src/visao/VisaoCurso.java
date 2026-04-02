package visao;

import entidades.Curso;
import utils.Entrada;

import java.util.List;

public class VisaoCurso {

    public Curso leCurso() {
        System.out.println("\nNOVO CURSO");

        System.out.print("Nome: ");
        String nome = lerLinha();

        System.out.print("Data de inicio (dd/MM/yyyy): ");
        String dataInicio = lerLinha();

        System.out.print("Descricao: ");
        String descricao = Entrada.SCANNER.nextLine().trim();

        Curso curso = new Curso();
        curso.setNome(nome);
        curso.setDataInicio(dataInicio);
        curso.setDescricao(descricao);

        return curso;
    }

    public Curso leCursoParaAtualizacao(Curso atual) {
        System.out.println("\nATUALIZAR CURSO (deixe em branco para manter)");

        Curso curso = new Curso();
        curso.setId(atual.getId());
        curso.setIdUsuario(atual.getIdUsuario());
        curso.setCodigo(atual.getCodigo());
        curso.setEstado(atual.getEstado());

        System.out.print("Nome (" + atual.getNome() + "): ");
        String nome = lerLinha();
        curso.setNome(nome.isBlank() ? atual.getNome() : nome);

        System.out.print("Data de inicio (" + atual.getDataInicio() + "): ");
        String dataInicio = lerLinha();
        curso.setDataInicio(dataInicio.isBlank() ? atual.getDataInicio() : dataInicio);

        System.out.print("Descricao (deixe em branco para manter): ");
        String descricao = Entrada.SCANNER.nextLine().trim();
        curso.setDescricao(descricao.isBlank() ? atual.getDescricao() : descricao);

        return curso;
    }

    public void listarCursos(List<Curso> cursos) {
        System.out.println("\nCURSOS");

        if (cursos.isEmpty()) {
            System.out.println("(nenhum curso cadastrado)");
            return;
        }

        for (int i = 0; i < cursos.size(); i++) {
            Curso curso = cursos.get(i);
            System.out.printf("(%d) %s - %s%n", i + 1, curso.getNome(), curso.getDataInicio());
        }
    }

    public void mostrarCurso(Curso curso) {
        System.out.println("\nCODIGO........: " + curso.getCodigo());
        System.out.println("NOME..........: " + curso.getNome());
        System.out.println("DESCRICAO.....: " + curso.getDescricao());
        System.out.println("DATA DE INICIO: " + curso.getDataInicio());
        System.out.println();
        System.out.println(estadoComoTexto(curso.getEstado()));
    }

    private String estadoComoTexto(int estado) {
        if (estado == 0) {
            return "Este curso esta aberto para inscricoes.";
        }
        if (estado == 1) {
            return "Este curso esta ativo, mas sem novas inscricoes.";
        }
        if (estado == 2) {
            return "Este curso foi concluido.";
        }
        return "Este curso foi cancelado.";
    }

    private String lerLinha() {
        return Entrada.SCANNER.nextLine().trim();
    }
}
