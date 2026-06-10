package controle;

import arquivos.ArquivoCursoUsuario;
import arquivos.ArquivoCursos;
import arquivos.ArquivoUsuarios;
import entidades.Curso;
import entidades.CursoUsuario;
import entidades.Usuario;
import indice.ResultadoBuscaCurso;
import utils.Entrada;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ControleCursoUsuario {

    private static final int TAMANHO_PAGINA = 10;
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ArquivoCursos arqCursos;
    private final ArquivoUsuarios arqUsuarios;
    private ArquivoCursoUsuario arqCursoUsuario;

    public ControleCursoUsuario(
        ArquivoCursos arqCursos,
        ArquivoUsuarios arqUsuarios
    ) {

        this.arqCursos = arqCursos;
        this.arqUsuarios = arqUsuarios;

        try {
            this.arqCursoUsuario = new ArquivoCursoUsuario();
        } catch (Exception e) {
            System.out.println(
                    "Erro ao abrir arquivo de inscricoes\n"
                    + e.getMessage()
            );
        }
    }

    public void menuInscricoes(Usuario logado) {
        while (true) {
            List<InscricaoCurso> inscricoes =
                    listarInscricoesDoUsuario(logado.getId());

            System.out.println("\nEntrePares 1.0");
            System.out.println("--------------");
            System.out.println("> Inicio > Minhas inscricoes");
            System.out.println("\nINSCRICOES");

            if (inscricoes.isEmpty()) {
                System.out.println("(nenhuma inscricao)");
            } else {
                for (int i = 0; i < inscricoes.size(); i++) {
                    Curso curso = inscricoes.get(i).curso;

                    System.out.println(
                            "(" + (i + 1) + ") "
                            + curso.getNome()
                            + " - "
                            + curso.getDataInicio()
                            + sufixoEstado(curso)
                    );
                }
            }

            System.out.println("\n(A) Buscar curso por codigo");
            System.out.println("(B) Buscar curso por palavras-chave");
            System.out.println("(C) Listar todos os cursos");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = Entrada.SCANNER.nextLine().trim();

            Integer indice = parseInteiro(op);

            if (indice != null) {
                if (indice >= 1 && indice <= inscricoes.size()) {
                    visualizarMinhaInscricao(
                            logado,
                            inscricoes.get(indice - 1).curso
                    );
                } else {
                    System.out.println("\nOpcao invalida.");
                }

                continue;
            }

            if (op.equalsIgnoreCase("A")) {
                buscarCursoPorCodigo(logado);
                continue;
            }

            if (op.equalsIgnoreCase("B")) {
                buscarCursoPorPalavras(logado);
                continue;
            }

            if (op.equalsIgnoreCase("C")) {
                listarTodosCursos(logado);
                continue;
            }

            if (op.equalsIgnoreCase("R")) {
                return;
            }

            System.out.println("\nOpcao invalida.");
        }
    }

    private void buscarCursoPorCodigo(Usuario logado) {
        System.out.println("\nEntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> Inicio > Minhas inscricoes > Buscar curso por codigo");
        System.out.print("\nDigite o codigo do curso: ");

        String codigo = Entrada.SCANNER.nextLine().trim();

        try {
            Curso cursoEncontrado = arqCursos.buscarPorCodigo(codigo);

            if (cursoEncontrado == null) {
                System.out.println("\nCurso nao encontrado.");
                return;
            }

            visualizarCursoParaInscricao(
                    logado,
                    cursoEncontrado,
                    "> Inicio > Minhas inscricoes > "
                    + cursoEncontrado.getNome()
            );

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao buscar curso!\n"
                    + e.getMessage()
            );
        }
    }

    // Busca cursos por palavras-chave usando o indice invertido (TP3).
    // Mostra os resultados ordenados por relevancia (TF*IDF) e permite
    // abrir um curso para inscricao, igual a busca por codigo.
    private void buscarCursoPorPalavras(Usuario logado) {
        System.out.println("\nEntrePares 1.0");
        System.out.println("--------------");
        System.out.println("> Inicio > Minhas inscricoes > Buscar curso por palavras-chave");
        System.out.print("\nDigite uma ou mais palavras do nome do curso: ");

        String consulta = Entrada.SCANNER.nextLine().trim();

        if (consulta.isEmpty()) {
            System.out.println("\nDigite ao menos uma palavra para buscar.");
            return;
        }

        try {
            List<ResultadoBuscaCurso> resultados =
                    arqCursos.buscarPorPalavras(consulta);

            if (resultados.isEmpty()) {
                System.out.println("\nNenhum curso encontrado para essa busca.");
                return;
            }

            while (true) {
                System.out.println("\nEntrePares 1.0");
                System.out.println("--------------");
                System.out.println(
                        "> Inicio > Minhas inscricoes > Resultados da busca"
                );
                System.out.println("\nRESULTADOS (ordenados por relevancia)\n");

                for (int i = 0; i < resultados.size(); i++) {
                    Curso curso = resultados.get(i).getCurso();

                    System.out.println(
                            "(" + (i + 1) + ") "
                            + curso.getNome()
                            + " - "
                            + curso.getDataInicio()
                            + sufixoEstado(curso)
                    );
                }

                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpcao: ");

                String op = Entrada.SCANNER.nextLine().trim();

                if (op.equalsIgnoreCase("R")) {
                    return;
                }

                Integer indice = parseInteiro(op);

                if (indice != null
                        && indice >= 1
                        && indice <= resultados.size()) {
                    Curso escolhido = resultados.get(indice - 1).getCurso();

                    visualizarCursoParaInscricao(
                            logado,
                            escolhido,
                            "> Inicio > Minhas inscricoes > "
                            + escolhido.getNome()
                    );
                    continue;
                }

                System.out.println("\nOpcao invalida.");
            }

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao buscar curso por palavras-chave!\n"
                    + e.getMessage()
            );
        }
    }

    private void listarTodosCursos(Usuario logado) {
        try {
            List<Curso> cursos = new ArrayList<>(arqCursos.readAll());

            cursos.sort(
                    Comparator
                            .comparing(this::dataInicioOrdenacao)
                            .thenComparing(
                                    Curso::getNome,
                                    String.CASE_INSENSITIVE_ORDER
                            )
            );

            if (cursos.isEmpty()) {
                System.out.println("\nNenhum curso cadastrado.");
                return;
            }

            int pagina = 0;

            while (true) {
                int inicio = pagina * TAMANHO_PAGINA;
                int fim = Math.min(inicio + TAMANHO_PAGINA, cursos.size());
                int totalPaginas =
                        (int) Math.ceil((double) cursos.size() / TAMANHO_PAGINA);

                System.out.println("\nEntrePares 1.0");
                System.out.println("--------------");
                System.out.println("> Inicio > Minhas inscricoes > Lista de cursos");
                System.out.println(
                        "\nPagina "
                        + (pagina + 1)
                        + " de "
                        + totalPaginas
                );
                System.out.println();

                for (int i = inicio; i < fim; i++) {
                    int numeroTela = i - inicio + 1;
                    String opcao = numeroTela == TAMANHO_PAGINA
                            ? "0"
                            : Integer.toString(numeroTela);
                    Curso curso = cursos.get(i);

                    System.out.println(
                            "(" + opcao + ") "
                            + curso.getNome()
                            + " - "
                            + curso.getDataInicio()
                            + sufixoEstado(curso)
                    );
                }

                System.out.println("\n(A) Pagina anterior");
                System.out.println("(B) Proxima pagina");
                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpcao: ");

                String op = Entrada.SCANNER.nextLine().trim();
                int indiceReal = indiceOpcaoPagina(op, inicio, fim);

                if (indiceReal >= 0) {
                    visualizarCursoParaInscricao(
                            logado,
                            cursos.get(indiceReal),
                            "> Inicio > Minhas inscricoes > Lista de cursos > "
                            + cursos.get(indiceReal).getNome()
                    );
                    continue;
                }

                if (op.equalsIgnoreCase("A")) {
                    if (pagina > 0) {
                        pagina--;
                    }
                    continue;
                }

                if (op.equalsIgnoreCase("B")) {
                    if (fim < cursos.size()) {
                        pagina++;
                    }
                    continue;
                }

                if (op.equalsIgnoreCase("R")) {
                    return;
                }

                System.out.println("\nOpcao invalida.");
            }

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao listar cursos!\n"
                    + e.getMessage()
            );
        }
    }

    private void visualizarCursoParaInscricao(
        Usuario logado,
        Curso curso,
        String caminho
    ) {

        try {
            while (true) {
                mostrarDadosCurso(curso, caminho);

                // So cursos abertos (ATIVO) aceitam inscricao; nos demais
                // estados a opcao de inscricao nem aparece no menu.
                boolean aberto = curso.getEstado() == Curso.ATIVO;

                if (aberto) {
                    System.out.println("\n(A) Fazer minha inscricao no curso");
                } else {
                    System.out.println(
                            "\nEste curso nao esta aberto para inscricoes."
                    );
                }

                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpcao: ");

                String op = Entrada.SCANNER.nextLine().trim();

                if (aberto && op.equalsIgnoreCase("A")) {
                    boolean ok =
                            inscreverUsuario(curso.getId(), logado.getId());

                    if (ok) {
                        System.out.println("\nInscricao realizada!");
                    } else {
                        System.out.println(
                                "\nNao foi possivel realizar inscricao."
                        );
                    }
                    return;
                }

                if (op.equalsIgnoreCase("R")) {
                    return;
                }

                System.out.println("\nOpcao invalida.");
            }

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao visualizar curso!\n"
                    + e.getMessage()
            );
        }
    }

    private void visualizarMinhaInscricao(
        Usuario logado,
        Curso curso
    ) {

        try {
            while (true) {
                mostrarDadosCurso(
                        curso,
                        "> Inicio > Minhas inscricoes > "
                        + curso.getNome()
                );

                System.out.println("\n(A) Cancelar minha inscricao no curso");
                System.out.println("\n(R) Retornar ao menu anterior");
                System.out.print("\nOpcao: ");

                String op = Entrada.SCANNER.nextLine().trim();

                if (op.equalsIgnoreCase("A")) {
                    boolean ok =
                            cancelarInscricao(
                                    curso.getId(),
                                    logado.getId()
                            );

                    if (ok) {
                        System.out.println(
                                "\nInscricao cancelada com sucesso!"
                        );
                    } else {
                        System.out.println(
                                "\nNao foi possivel cancelar a inscricao."
                        );
                    }
                    return;
                }

                if (op.equalsIgnoreCase("R")) {
                    return;
                }

                System.out.println("\nOpcao invalida.");
            }

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao visualizar inscricao!\n"
                    + e.getMessage()
            );
        }
    }

    public void abrirGerenciamentoInscritos(Curso curso) {
        while (true) {
            List<InscricaoUsuario> inscricoes =
                    listarInscricoesDoCurso(curso.getId());

            System.out.println("\nEntrePares 1.0");
            System.out.println("--------------");
            System.out.println(
                    "> Inicio > Meus cursos > "
                    + curso.getNome()
                    + " > Inscricoes"
            );
            System.out.println();

            if (inscricoes.isEmpty()) {
                System.out.println("(nenhum usuario inscrito)");
            } else {
                for (int i = 0; i < inscricoes.size(); i++) {
                    InscricaoUsuario inscricao = inscricoes.get(i);

                    System.out.println(
                            "(" + (i + 1) + ") "
                            + inscricao.usuario.getNome()
                            + " ("
                            + formatarDataInscricao(
                                    inscricao.inscricao.getDataInscricao()
                            )
                            + ")"
                    );
                }
            }

            System.out.println("\n(A) Exportar lista");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = Entrada.SCANNER.nextLine().trim();
            Integer indice = parseInteiro(op);

            if (indice != null) {
                if (indice >= 1 && indice <= inscricoes.size()) {
                    visualizarUsuarioInscrito(
                            curso,
                            inscricoes.get(indice - 1)
                    );
                } else {
                    System.out.println("\nOpcao invalida.");
                }
                continue;
            }

            if (op.equalsIgnoreCase("A")) {
                exportarCSV(curso);
                continue;
            }

            if (op.equalsIgnoreCase("R")) {
                return;
            }

            System.out.println("\nOpcao invalida.");
        }
    }

    private void visualizarUsuarioInscrito(
        Curso curso,
        InscricaoUsuario inscricao
    ) {

        while (true) {
            Usuario usuario = inscricao.usuario;

            System.out.println("\nEntrePares 1.0");
            System.out.println("--------------");
            System.out.println(
                    "> Inicio > Meus cursos > "
                    + curso.getNome()
                    + " > Inscricoes > "
                    + usuario.getNome()
            );
            System.out.println("\nNOME..............: " + usuario.getNome());
            System.out.println("E-MAIL............: " + usuario.getEmail());
            System.out.println(
                    "DATA DA INSCRICAO.: "
                    + formatarDataInscricao(
                            inscricao.inscricao.getDataInscricao()
                    )
            );

            System.out.println("\n(A) Cancelar inscricao deste usuario");
            System.out.println("\n(R) Retornar ao menu anterior");
            System.out.print("\nOpcao: ");

            String op = Entrada.SCANNER.nextLine().trim();

            if (op.equalsIgnoreCase("A")) {
                boolean ok =
                        arqCursoUsuario.delete(
                                inscricao.inscricao.getId()
                        );

                if (ok) {
                    System.out.println("\nInscricao cancelada com sucesso!");
                } else {
                    System.out.println(
                            "\nNao foi possivel cancelar a inscricao."
                    );
                }
                return;
            }

            if (op.equalsIgnoreCase("R")) {
                return;
            }

            System.out.println("\nOpcao invalida.");
        }
    }

    private boolean inscreverUsuario(
        int idCurso,
        int idUsuario
    ) {

        try {
            Curso curso = arqCursos.read(idCurso);

            if (curso == null) {
                System.out.println("\nCurso inexistente.");
                return false;
            }

            if (curso.getIdUsuario() == idUsuario) {
                System.out.println(
                        "\nVoce nao pode se inscrever no proprio curso."
                );
                return false;
            }

            if (curso.getEstado() != Curso.ATIVO) {
                System.out.println(
                        "\nEste curso nao esta aberto para inscricoes."
                );
                return false;
            }

            Usuario usuario = arqUsuarios.read(idUsuario);

            if (usuario == null) {
                System.out.println("\nUsuario inexistente.");
                return false;
            }

            if (buscarInscricao(idCurso, idUsuario) != null) {
                System.out.println(
                        "\nVoce ja esta inscrito neste curso."
                );
                return false;
            }

            CursoUsuario novaInscricao =
                    new CursoUsuario(idCurso, idUsuario);

            int idInscricao = arqCursoUsuario.create(novaInscricao);

            return idInscricao > 0;

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao realizar inscricao!\n"
                    + e.getMessage()
            );

            return false;
        }
    }

    private boolean cancelarInscricao(
        int idCurso,
        int idUsuario
    ) {

        try {
            CursoUsuario inscricao =
                    buscarInscricao(idCurso, idUsuario);

            return inscricao != null
                    && arqCursoUsuario.delete(inscricao.getId());

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao cancelar inscricao!\n"
                    + e.getMessage()
            );
        }

        return false;
    }

    public boolean cursoTemInscritos(int idCurso) {
        try {
            return !arqCursoUsuario.readByCurso(idCurso).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void removerInscricoesDoCurso(int idCurso) {
        try {
            for (CursoUsuario inscricao
                    : new ArrayList<>(arqCursoUsuario.readByCurso(idCurso))) {
                arqCursoUsuario.delete(inscricao.getId());
            }
        } catch (Exception e) {
            System.out.println(
                    "\nErro ao remover inscricoes do curso!\n"
                    + e.getMessage()
            );
        }
    }

    public void removerInscricoesDoUsuario(int idUsuario) {
        try {
            for (CursoUsuario inscricao
                    : new ArrayList<>(arqCursoUsuario.readByUsuario(idUsuario))) {
                arqCursoUsuario.delete(inscricao.getId());
            }
        } catch (Exception e) {
            System.out.println(
                    "\nErro ao remover inscricoes do usuario!\n"
                    + e.getMessage()
            );
        }
    }

    private CursoUsuario buscarInscricao(
        int idCurso,
        int idUsuario
    ) {

        ArrayList<CursoUsuario> inscricoes =
                arqCursoUsuario.readByUsuario(idUsuario);

        for (CursoUsuario inscricao : inscricoes) {
            if (inscricao.getIdCurso() == idCurso) {
                return inscricao;
            }
        }

        return null;
    }

    private List<InscricaoCurso> listarInscricoesDoUsuario(
        int idUsuario
    ) {

        ArrayList<InscricaoCurso> lista = new ArrayList<>();

        for (CursoUsuario inscricao
                : arqCursoUsuario.readByUsuario(idUsuario)) {
            Curso curso = arqCursos.read(inscricao.getIdCurso());

            if (curso == null) {
                arqCursoUsuario.delete(inscricao.getId());
                continue;
            }

            lista.add(new InscricaoCurso(inscricao, curso));
        }

        lista.sort(
                Comparator
                        .comparing(
                                (InscricaoCurso item) ->
                                        dataInicioOrdenacao(item.curso)
                        )
                        .thenComparing(
                                item -> item.curso.getNome(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        return lista;
    }

    private List<InscricaoUsuario> listarInscricoesDoCurso(
        int idCurso
    ) {

        ArrayList<InscricaoUsuario> lista = new ArrayList<>();

        for (CursoUsuario inscricao
                : arqCursoUsuario.readByCurso(idCurso)) {
            Usuario usuario = arqUsuarios.read(inscricao.getIdUsuario());

            if (usuario == null) {
                arqCursoUsuario.delete(inscricao.getId());
                continue;
            }

            lista.add(new InscricaoUsuario(inscricao, usuario));
        }

        lista.sort(
                Comparator.comparing(
                        item -> item.usuario.getNome(),
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        return lista;
    }

    private void exportarCSV(Curso curso) {
        try {
            List<InscricaoUsuario> inscricoes =
                    listarInscricoesDoCurso(curso.getId());

            if (inscricoes.isEmpty()) {
                System.out.println("\nNao ha inscritos neste curso.");
                return;
            }

            String nomeArquivo =
                    "inscritos_" + curso.getCodigo() + ".csv";

            try (PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(nomeArquivo),
                            StandardCharsets.UTF_8
                    )
            )) {
                pw.println("nome,email,data_inscricao");

                for (InscricaoUsuario inscricao : inscricoes) {
                    pw.println(
                            csv(inscricao.usuario.getNome())
                            + ","
                            + csv(inscricao.usuario.getEmail())
                            + ","
                            + csv(
                                    formatarDataInscricao(
                                            inscricao.inscricao
                                                    .getDataInscricao()
                                    )
                            )
                    );
                }
            }

            System.out.println("\nArquivo CSV exportado com sucesso!");
            System.out.println("Arquivo: " + nomeArquivo);

        } catch (Exception e) {
            System.out.println(
                    "\nErro ao exportar CSV!\n"
                    + e.getMessage()
            );
        }
    }

    private void mostrarDadosCurso(
        Curso curso,
        String caminho
    ) {

        Usuario autorCurso = arqUsuarios.read(curso.getIdUsuario());
        String autor =
                autorCurso != null
                ? autorCurso.getNome()
                : "Usuario desconhecido";

        System.out.println("\nEntrePares 1.0");
        System.out.println("--------------");
        System.out.println(caminho);
        System.out.println("\nCODIGO........: " + curso.getCodigo());
        System.out.println("CURSO.........: " + curso.getNome());
        System.out.println("AUTOR.........: " + autor);
        System.out.println("DESCRICAO.....: " + curso.getDescricao());
        System.out.println("DATA DE INICIO: " + curso.getDataInicio());
        System.out.println("SITUACAO......: " + estadoCurso(curso));
    }

    private int indiceOpcaoPagina(
        String op,
        int inicio,
        int fim
    ) {

        if (op.length() != 1 || !Character.isDigit(op.charAt(0))) {
            return -1;
        }

        int numero = Integer.parseInt(op);
        int posicao = numero == 0 ? TAMANHO_PAGINA - 1 : numero - 1;
        int indiceReal = inicio + posicao;

        return indiceReal >= inicio && indiceReal < fim
                ? indiceReal
                : -1;
    }

    private Integer parseInteiro(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate dataInicioOrdenacao(Curso curso) {
        try {
            return LocalDate.parse(curso.getDataInicio(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            return LocalDate.MAX;
        }
    }

    private String formatarDataInscricao(long dataInscricao) {
        if (dataInscricao <= 0) {
            return "";
        }

        return Instant
                .ofEpochMilli(dataInscricao)
                .atZone(ZoneId.systemDefault())
                .format(FORMATO_DATA);
    }

    private String sufixoEstado(Curso curso) {
        if (curso.getEstado() == Curso.ATIVO) {
            return "";
        }

        return " (" + estadoCurso(curso) + ")";
    }

    private String estadoCurso(Curso curso) {
        switch (curso.getEstado()) {
            case Curso.ATIVO:
                return "ABERTO";
            case Curso.INSCRICOES_ENCERRADAS:
                return "INSCRICOES ENCERRADAS";
            case Curso.CONCLUIDO:
                return "CURSO CONCLUIDO";
            case Curso.CANCELADO:
                return "CURSO CANCELADO";
            default:
                return "DESCONHECIDO";
        }
    }

    private String csv(String valor) {
        String texto = valor == null ? "" : valor;

        if (
            texto.contains(",")
            || texto.contains("\"")
            || texto.contains("\n")
            || texto.contains("\r")
        ) {
            return "\""
                    + texto.replace("\"", "\"\"")
                    + "\"";
        }

        return texto;
    }

    private static class InscricaoCurso {
        private final CursoUsuario inscricao;
        private final Curso curso;

        private InscricaoCurso(
            CursoUsuario inscricao,
            Curso curso
        ) {
            this.inscricao = inscricao;
            this.curso = curso;
        }
    }

    private static class InscricaoUsuario {
        private final CursoUsuario inscricao;
        private final Usuario usuario;

        private InscricaoUsuario(
            CursoUsuario inscricao,
            Usuario usuario
        ) {
            this.inscricao = inscricao;
            this.usuario = usuario;
        }
    }
}
