package arquivos;

import entidades.Usuario;
import estruturas.ArquivoIndexado;
import estruturas.TabelaHashExtensivel;

import java.util.List;

public class ArquivoUsuarios extends ArquivoIndexado<Usuario> {

    private static final String ARQ_DADOS = "dados/usuarios.db";
    private static final String ARQ_INDICE_DIRETO = "dados/usuariosId.hash";
    private static final String ARQ_INDICE_EMAIL = "dados/email.hash";

    private final TabelaHashExtensivel<Integer, Integer> indiceDireto;
    private final TabelaHashExtensivel<String, Integer> indiceEmail;

    public ArquivoUsuarios() {
        super(ARQ_DADOS, Usuario::new);
        indiceDireto = new TabelaHashExtensivel<>(ARQ_INDICE_DIRETO);
        indiceEmail = new TabelaHashExtensivel<>(ARQ_INDICE_EMAIL);
        reconstruirIndices();
    }

    @Override
    public int create(Usuario usuario) {
        String email = normalizarEmail(usuario.getEmail());

        if (indiceEmail.read(email) != null) {
            return -1;
        }

        usuario.setEmail(email);
        int id = super.create(usuario);

        indiceDireto.upsert(id, id);
        indiceEmail.upsert(email, id);

        return id;
    }

    @Override
    public Usuario read(int id) {
        Integer idArmazenado = indiceDireto.read(id);

        if (idArmazenado == null) {
            return null;
        }

        return super.read(idArmazenado);
    }

    public Usuario buscarPorEmail(String email) {
        Integer id = indiceEmail.read(normalizarEmail(email));

        if (id == null) {
            return null;
        }

        return super.read(id);
    }

    // Compatibilidade com chamadas antigas.
    public Usuario buscarEmail(String email) {
        return buscarPorEmail(email);
    }

    @Override
    public boolean update(Usuario usuario) {
        Usuario antigo = super.read(usuario.getId());

        if (antigo == null) {
            return false;
        }

        String novoEmail = normalizarEmail(usuario.getEmail());
        Integer idDonoEmail = indiceEmail.read(novoEmail);

        if (idDonoEmail != null && idDonoEmail != usuario.getId()) {
            return false;
        }

        usuario.setEmail(novoEmail);

        boolean ok = super.update(usuario);

        if (!ok) {
            return false;
        }

        indiceDireto.upsert(usuario.getId(), usuario.getId());

        String emailAntigo = normalizarEmail(antigo.getEmail());
        if (!emailAntigo.equals(novoEmail)) {
            indiceEmail.delete(emailAntigo);
        }

        indiceEmail.upsert(novoEmail, usuario.getId());
        return true;
    }

    @Override
    public boolean delete(int id) {
        Usuario usuario = read(id);

        if (usuario == null) {
            return false;
        }

        boolean ok = super.delete(id);

        if (!ok) {
            return false;
        }

        indiceDireto.delete(id);
        indiceEmail.delete(normalizarEmail(usuario.getEmail()));
        return true;
    }

    public List<Usuario> listarTodos() {
        return super.readAll();
    }

    private void reconstruirIndices() {
        indiceDireto.clear();
        indiceEmail.clear();

        for (Usuario usuario : super.readAll()) {
            indiceDireto.upsert(usuario.getId(), usuario.getId());
            indiceEmail.upsert(normalizarEmail(usuario.getEmail()), usuario.getId());
        }
    }

    private String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase();
    }
}
