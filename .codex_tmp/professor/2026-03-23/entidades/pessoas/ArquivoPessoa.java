package entidades.pessoas;
import aed3.*;

import java.util.ArrayList;

public class ArquivoPessoa extends Arquivo<Pessoa> {
    
    HashExtensivel<ParCpfId> indiceCPF;
    ArvoreBMais<ParNomeId> indiceNome;

    public ArquivoPessoa() throws Exception {
        super("pessoa", Pessoa.class.getConstructor());
        indiceCPF = new HashExtensivel<>(
            ParCpfId.class.getConstructor(),
            4, 
            "./dados/pessoa/indiceCPF.d.db", 
            "./dados/pessoa/indiceCPF.c.db");
        indiceNome = new ArvoreBMais<>(
            ParNomeId.class.getConstructor(),
            4,
           "./dados/pessoa/indiceNome.db");
    }

    @Override
    public int create(Pessoa p) throws Exception {
        int id = super.create(p);
        indiceCPF.create(new ParCpfId(p.getCpf(), id));
        indiceNome.create(new ParNomeId(p.getNome(), id));
        return id;
    }

    public Pessoa readCPF(String cpf) throws Exception {
        ParCpfId pci = indiceCPF.read(Math.abs(cpf.hashCode()));
        if(pci == null)
            return null;
        Pessoa p = read(pci.getId());
        return p;
    }

    public Pessoa[] readNome(String nome) throws Exception {
        ArrayList<ParNomeId> pnis = indiceNome.read(new ParNomeId(nome,-1));  // O -1 indica que a comparação só deve verificar o nome (e não o ID)
        if(pnis.isEmpty())
            return new Pessoa[0];

        Pessoa[] pessoas = new Pessoa[pnis.size()];
        int i=0;
        for (ParNomeId pni : pnis) {
            pessoas[i++] = super.read(pni.getId());            
        }
        return pessoas;
    }

    public Pessoa[] readAll() throws Exception {
        ArrayList<ParNomeId> pnis = indiceNome.read(null);
        if(pnis.isEmpty())
            return new Pessoa[0];

        Pessoa[] pessoas = new Pessoa[pnis.size()];
        int i=0;
        for (ParNomeId pni : pnis) {
            pessoas[i++] = super.read(pni.getId());            
        }
        return pessoas;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Pessoa p = read(id);
        if(p!=null)
            if(super.delete(id)) {
                indiceCPF.delete(Math.abs(p.getCpf().hashCode()));
                indiceNome.delete(new ParNomeId(p.getNome(), p.getID()));
                return true;
            }
        return false;
    }

    @Override
    public boolean update(Pessoa novaPessoa) throws Exception {
        Pessoa p = read(novaPessoa.getID());
        if(p==null)
            return false;
        if(super.update(novaPessoa)) {
            if(p.getCpf().compareTo(novaPessoa.getCpf())!=0) {
                indiceCPF.delete(Math.abs(p.getCpf().hashCode()));
                indiceCPF.create(new ParCpfId(novaPessoa.getCpf(), novaPessoa.getID()));
            }
            if(p.getNome().compareTo(novaPessoa.getNome())!=0) {
                indiceNome.delete(new ParNomeId(p.getNome(), p.getID()));
                indiceNome.create(new ParNomeId( novaPessoa.getNome(), novaPessoa.getID()));
            }
            return true;
        }
        return false;
    }


    public void close() throws Exception {
        super.close();
        indiceCPF.close();
        indiceNome.close();
    }
}
