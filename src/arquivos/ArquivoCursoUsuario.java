package arquivos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import aed3.ArvoreBMais;
import aed3.RegistroArvoreBMais;
import entidades.Curso;
import entidades.CursoUsuario;
import estruturas.ArquivoIndexado;

public class ArquivoCursoUsuario extends ArquivoIndexado<CursoUsuario>{
    private static final String ARQ_DADOS = "dados/cursoUsuario.db";
    private static final String ARQ_INDICE_DIRETO = "dados/cursoUsuarioId.hash";
    private static final String ARQ_INDICE_CURSOS = "dados/cursoUsuario_curso.idx";
    private static final String ARQ_INDICE_USUARIOS = "dados/cursoUsuario_usuario.idx";

    private final ArvoreBMais<ParIdId> indiceCursos;
    private final ArvoreBMais<ParIdId> indiceUsuarios;

    public ArquivoCursoUsuario() throws Exception{
        super(ARQ_DADOS, ARQ_INDICE_DIRETO, CursoUsuario::new);

        indiceCursos = new ArvoreBMais<>(ParIdId.class.getConstructor(), 5, ARQ_INDICE_CURSOS);
        indiceUsuarios = new ArvoreBMais<>(ParIdId.class.getConstructor(), 5, ARQ_INDICE_USUARIOS);
    }

    @Override
    public int create(CursoUsuario cursoUsuario){
        int idInscricao = super.create(cursoUsuario);

        try{
            ParIdId parCurso = new ParIdId(cursoUsuario.getIdCurso(), idInscricao);
            ParIdId parUsuario = new ParIdId(cursoUsuario.getIdUsuario(), idInscricao);

            indiceCursos.create(parCurso);
            indiceUsuarios.create(parUsuario);
        }catch (Exception e) {
            System.out.println("Erro ao salvar\n" + e.getMessage());
        }
            return idInscricao;
    }

    @Override
    public boolean delete(int idInscricao){
        CursoUsuario cursoUsuario = read(idInscricao);

        if(cursoUsuario == null){
            return false;
        }

        boolean ok = super.delete(idInscricao);

        if(!ok){
            return false;
        }

        try {
            ParIdId parCurso = new ParIdId(cursoUsuario.getIdCurso(), idInscricao);
            ParIdId parUsuario = new ParIdId(cursoUsuario.getIdUsuario(), idInscricao);

            indiceCursos.delete(parCurso);
            indiceUsuarios.delete(parUsuario);

        } catch (Exception e) {
            System.out.println("Erro ao deletar\n" + e.getMessage());
        }

        return true;
    }

    //implementar listagem de inscricoes de curso 
    // e lista de alunos nos cursos

    public static class ParIdId implements RegistroArvoreBMais<ParIdId>{
        public int idEstrangeiro;
        public int idInscricao;
        private final short TAMANHO = 8;

        public ParIdId(){
            this(-1, -1);
        }

        public ParIdId(int idEstrangeiro, int idInscricao){
            this.idEstrangeiro = idEstrangeiro;
            this.idInscricao = idInscricao;
        }

        @Override
        public ParIdId clone() {
            return new ParIdId(this.idEstrangeiro, this.idInscricao);
        }

        @Override
        public short size(){
            return TAMANHO;
        }

        @Override
        public int compareTo(ParIdId par){
            if(this.idEstrangeiro != par.idEstrangeiro){
                return Integer.compare(this.idEstrangeiro, par.idEstrangeiro);
            }

            if(this.idInscricao == -1 || par.idInscricao == -1){
                return 0;
            }

            return Integer.compare(this.idInscricao, par.idInscricao);
        }

        @Override
        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream da = new DataOutputStream(ba);
            da.writeInt(this.idEstrangeiro);
            da.writeInt(this.idInscricao);
            return ba.toByteArray();
        }

        @Override
        public void fromByteArray(byte[] buffer) throws IOException {
            ByteArrayInputStream ba = new ByteArrayInputStream(buffer);
            DataInputStream di = new DataInputStream(ba);
            this.idEstrangeiro = di.readInt();
            this.idInscricao = di.readInt();
        }
    }
}
