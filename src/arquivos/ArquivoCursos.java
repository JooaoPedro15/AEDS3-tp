package arquivos;

import entidades.Curso;
import estruturas.ArquivoIndexado;
import estruturas.ArvoreBMais;
import estruturas.TabelaHashExtensivel;
import indice.IndiceInvertidoCursos;
import indice.ResultadoBuscaCurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArquivoCursos extends ArquivoIndexado<Curso> {

    private static final String ARQ_DADOS = "dados/cursos.db";
    private static final String ARQ_INDICE_DIRETO = "dados/cursosId.hash";
    private static final String ARQ_REL_USUARIO_CURSO = "dados/usuarioCurso.idx";
    private static final String ARQ_INDICE_CODIGO = "dados/cursoCodigo.hash";
    private static final String ARQ_INDICE_NOME = "dados/cursosNome.idx";

    // Arquivos do indice invertido dos nomes dos cursos (TP3).
    private static final String ARQ_INV_DICIONARIO = "dados/cursos.dicionario.listainv.db";
    private static final String ARQ_INV_BLOCOS = "dados/cursos.blocos.listainv.db";

    private final TabelaHashExtensivel<String, Integer> indiceCodigo;
    private final ArvoreBMais<Integer, Integer> indiceUsuarioCurso;
    private final ArvoreBMais<String, Integer> indiceNome;
    private final IndiceInvertidoCursos indiceInvertido;

    public ArquivoCursos() {
        super(ARQ_DADOS, ARQ_INDICE_DIRETO, Curso::new);
        indiceCodigo = new TabelaHashExtensivel<>(ARQ_INDICE_CODIGO, TabelaHashExtensivel.Tipo.STRING_INT);
        indiceUsuarioCurso = new ArvoreBMais<>(ARQ_REL_USUARIO_CURSO, ArvoreBMais.Tipo.INT_INT);
        indiceNome = new ArvoreBMais<>(ARQ_INDICE_NOME, ArvoreBMais.Tipo.STRING_INT);
        indiceInvertido = new IndiceInvertidoCursos(ARQ_INV_DICIONARIO, ARQ_INV_BLOCOS);
        reconstruirIndices();
    }

    @Override
    public int create(Curso curso) {
        String codigo = normalizarCodigo(curso.getCodigo());

        if (!codigo.isEmpty() && indiceCodigo.read(codigo) != null) {
            return -1;
        }

        curso.setCodigo(codigo);

        int id = super.create(curso);

        indiceUsuarioCurso.create(curso.getIdUsuario(), id);
        indiceNome.create(curso.getNome(), id);
        indiceInvertido.inserir(curso);

        if (!codigo.isEmpty()) {
            indiceCodigo.upsert(codigo, id);
        }

        return id;
    }

    @Override
    public boolean update(Curso curso) {
        Curso antigo = super.read(curso.getId());

        if (antigo == null) {
            return false;
        }

        String novoCodigo = normalizarCodigo(curso.getCodigo());
        Integer idDonoCodigo = indiceCodigo.read(novoCodigo);

        if (!novoCodigo.isEmpty() && idDonoCodigo != null && idDonoCodigo != curso.getId()) {
            return false;
        }

        curso.setCodigo(novoCodigo);

        boolean ok = super.update(curso);

        if (!ok) {
            return false;
        }

        if (antigo.getIdUsuario() != curso.getIdUsuario()) {
            indiceUsuarioCurso.delete(antigo.getIdUsuario(), curso.getId());
            indiceUsuarioCurso.create(curso.getIdUsuario(), curso.getId());
        }

        if (!antigo.getNome().equals(curso.getNome())) {
            indiceNome.delete(antigo.getNome(), antigo.getId());
            indiceNome.create(curso.getNome(), curso.getId());
            indiceInvertido.atualizar(antigo, curso);
        }

        String codigoAntigo = normalizarCodigo(antigo.getCodigo());

        if (!codigoAntigo.equals(novoCodigo)) {
            if (!codigoAntigo.isEmpty()) {
                indiceCodigo.delete(codigoAntigo);
            }

            if (!novoCodigo.isEmpty()) {
                indiceCodigo.upsert(novoCodigo, curso.getId());
            }
        }

        return true;
    }

    @Override
    public boolean delete(int id) {
        Curso curso = read(id);

        if (curso == null) {
            return false;
        }

        boolean ok = super.delete(id);

        if (!ok) {
            return false;
        }

        indiceUsuarioCurso.delete(curso.getIdUsuario(), id);
        indiceNome.delete(curso.getNome(), id);
        indiceInvertido.remover(curso);

        String codigo = normalizarCodigo(curso.getCodigo());
        if (!codigo.isEmpty()) {
            indiceCodigo.delete(codigo);
        }

        return true;
    }

    public List<Curso> listarPorUsuario(int idUsuario) {
        List<Curso> cursos = new ArrayList<>();
        ArrayList<Integer> idsDoUsuario = indiceUsuarioCurso.read(idUsuario);

        if (idsDoUsuario.isEmpty()) {
            return cursos;
        }

        Map<String, ArrayList<Integer>> nomesOrdenados = indiceNome.snapshot();

        for (ArrayList<Integer> idsPorNome : nomesOrdenados.values()) {
            for (Integer id : idsPorNome) {
                if (!idsDoUsuario.contains(id)) {
                    continue;
                }

                Curso curso = read(id);

                if (curso != null) {
                    cursos.add(curso);
                }
            }
        }
        return cursos;
    }

    public boolean temCursosAtivosPorUsuario(int idUsuario) {
        for (Curso curso : listarPorUsuario(idUsuario)) {
            if (curso.getEstado() == 0 || curso.getEstado() == 1) {
                return true;
            }
        }

        return false;
    }

    public void removerInativosDoUsuario(int idUsuario) {
        List<Curso> cursos = listarPorUsuario(idUsuario);

        for (Curso curso : cursos) {
            if (curso.getEstado() == 2 || curso.getEstado() == 3) {
                delete(curso.getId());
            }
        }
    }

    public List<Curso> listarTodos() {
        List<Curso> cursos = new ArrayList<>();

        for (ArrayList<Integer> ids : indiceNome.snapshot().values()) {
            for (Integer id : ids) {
                Curso curso = read(id);

                if (curso != null) {
                    cursos.add(curso);
                }
            }
        }

        return cursos;
    }

    public Curso buscarPorCodigo(String codigo) {
        Integer id = indiceCodigo.read(normalizarCodigo(codigo));
        return id == null ? null : read(id);
    }

    /**
     * Busca cursos por palavras-chave usando o indice invertido.
     * Aplica o mesmo tratamento de termos (minusculas, sem acento,
     * sem stop words), calcula TF*IDF e retorna os cursos ordenados
     * por relevancia. Cada resultado ja vem com o curso carregado.
     */
    public List<ResultadoBuscaCurso> buscarPorPalavras(String consulta) {
        List<ResultadoBuscaCurso> resultados = indiceInvertido.buscar(consulta);
        List<ResultadoBuscaCurso> comCurso = new ArrayList<>();

        for (ResultadoBuscaCurso resultado : resultados) {
            Curso curso = read(resultado.getIdCurso());

            // Ignora ids que por algum motivo nao existam mais.
            if (curso != null) {
                resultado.setCurso(curso);
                comCurso.add(resultado);
            }
        }

        return comCurso;
    }

    private void reconstruirIndices() {
        indiceCodigo.clear();
        indiceUsuarioCurso.clear();
        indiceNome.clear();

        for (Curso curso : super.readAll()) {
            indiceUsuarioCurso.create(curso.getIdUsuario(), curso.getId());
            indiceNome.create(curso.getNome(), curso.getId());
            indiceInvertido.inserir(curso);

            String codigo = normalizarCodigo(curso.getCodigo());
            if (!codigo.isEmpty()) {
                indiceCodigo.upsert(codigo, curso.getId());
            }
        }
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }

        return codigo.trim();
    }
}
