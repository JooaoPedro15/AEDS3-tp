package arquivos;

import entidades.Curso;
import estruturas.ArquivoIndexado;
import estruturas.ArvoreBMais;
import estruturas.TabelaHashExtensivel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArquivoCursos extends ArquivoIndexado<Curso> {

    private static final String ARQ_DADOS = "dados/cursos.db";
    private static final String ARQ_INDICE_DIRETO = "dados/cursosId.hash";
    private static final String ARQ_REL_USUARIO_CURSO = "dados/usuarioCurso.idx";
    private static final String ARQ_INDICE_CODIGO = "dados/cursoCodigo.hash";

    private final TabelaHashExtensivel<Integer, Integer> indiceDireto;
    private final TabelaHashExtensivel<String, Integer> indiceCodigo;
    private final ArvoreBMais<Integer, Integer> indiceUsuarioCurso;

    public ArquivoCursos() {
        super(ARQ_DADOS, Curso::new);
        indiceDireto = new TabelaHashExtensivel<>(ARQ_INDICE_DIRETO);
        indiceCodigo = new TabelaHashExtensivel<>(ARQ_INDICE_CODIGO);
        indiceUsuarioCurso = new ArvoreBMais<>(ARQ_REL_USUARIO_CURSO);
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

        indiceDireto.upsert(id, id);
        indiceUsuarioCurso.create(curso.getIdUsuario(), id);

        if (!codigo.isEmpty()) {
            indiceCodigo.upsert(codigo, id);
        }

        return id;
    }

    @Override
    public Curso read(int id) {
        Integer idArmazenado = indiceDireto.read(id);

        if (idArmazenado == null) {
            return null;
        }

        return super.read(idArmazenado);
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

        indiceDireto.upsert(curso.getId(), curso.getId());

        if (antigo.getIdUsuario() != curso.getIdUsuario()) {
            indiceUsuarioCurso.delete(antigo.getIdUsuario(), curso.getId());
            indiceUsuarioCurso.create(curso.getIdUsuario(), curso.getId());
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

        indiceDireto.delete(id);
        indiceUsuarioCurso.delete(curso.getIdUsuario(), id);

        String codigo = normalizarCodigo(curso.getCodigo());
        if (!codigo.isEmpty()) {
            indiceCodigo.delete(codigo);
        }

        return true;
    }

    public List<Curso> listarPorUsuario(int idUsuario) {
        ArrayList<Integer> idsCursos = indiceUsuarioCurso.read(idUsuario);
        List<Curso> cursos = new ArrayList<>();

        for (Integer idCurso : idsCursos) {
            Curso curso = super.read(idCurso);

            if (curso != null) {
                cursos.add(curso);
            }
        }

        cursos.sort(Comparator.comparing(curso -> texto(curso.getNome()), String.CASE_INSENSITIVE_ORDER));
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
        List<Curso> cursos = super.readAll();
        cursos.sort(Comparator.comparing(curso -> texto(curso.getNome()), String.CASE_INSENSITIVE_ORDER));
        return cursos;
    }

    private void reconstruirIndices() {
        indiceDireto.clear();
        indiceCodigo.clear();
        indiceUsuarioCurso.clear();

        for (Curso curso : super.readAll()) {
            indiceDireto.upsert(curso.getId(), curso.getId());
            indiceUsuarioCurso.create(curso.getIdUsuario(), curso.getId());

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

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
