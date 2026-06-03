package indice;

import aed3.ElementoLista;
import aed3.ListaInvertida;
import entidades.Curso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Indice invertido dos nomes dos cursos.
 *
 * A estrutura principal e a classe ListaInvertida do professor
 * (pacote aed3), gravada em arquivo. Cada termo do nome de um curso
 * aponta para uma lista de ElementoLista (idDoCurso, TF), onde TF e
 * a frequencia do termo dentro daquele nome.
 *
 * Estrategia de atualizacao: o indice e reconstruido do zero a cada
 * inicializacao (os arquivos sao apagados no construtor) e mantido de
 * forma incremental quando um curso e incluido, alterado ou excluido.
 * Assim o indice nunca fica inconsistente com os cursos cadastrados.
 */
public class IndiceInvertidoCursos
{
    // Quantidade de dados (ids de cursos) por bloco da lista invertida.
    private static final int DADOS_POR_BLOCO = 8;

    private final ListaInvertida lista;
    private final TratadorDeTermos tratador;

    /**
     * Abre o indice invertido nos arquivos informados. Os arquivos
     * antigos sao apagados para garantir uma reconstrucao limpa a
     * cada execucao do programa.
     */
    public IndiceInvertidoCursos(String arquivoDicionario, String arquivoBlocos)
    {
        try
        {
            new File(arquivoDicionario).delete();
            new File(arquivoBlocos).delete();

            this.lista = new ListaInvertida(DADOS_POR_BLOCO, arquivoDicionario, arquivoBlocos);
            this.tratador = new TratadorDeTermos();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao abrir indice invertido de cursos", e);
        }
    }

    /**
     * Reconstroi o indice inteiro a partir de uma lista de cursos.
     * Usado na inicializacao, depois que os arquivos foram zerados.
     */
    public void reconstruir(List<Curso> cursos)
    {
        for (Curso curso : cursos)
        {
            inserir(curso);
        }
    }

    /**
     * Fecha os arquivos do indice invertido, liberando os arquivos no
     * disco. Usado principalmente em testes.
     */
    public void fechar()
    {
        try
        {
            lista.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao fechar indice invertido de cursos", e);
        }
    }

    /**
     * Indexa um curso novo: grava os termos do nome com seus TFs e
     * conta o curso no total de entidades (usado no IDF).
     */
    public void inserir(Curso curso)
    {
        try
        {
            adicionarTermos(curso);
            lista.incrementaEntidades();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao indexar curso no indice invertido", e);
        }
    }

    /**
     * Remove um curso do indice: apaga os termos do nome e desconta o
     * curso do total de entidades.
     */
    public void remover(Curso curso)
    {
        try
        {
            removerTermos(curso);
            lista.decrementaEntidades();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao remover curso do indice invertido", e);
        }
    }

    /**
     * Atualiza o indice quando o nome de um curso muda: remove os
     * termos antigos e insere os novos. O total de cursos nao muda.
     */
    public void atualizar(Curso antigo, Curso novo)
    {
        try
        {
            // Se o nome normalizado nao mudou, nao ha o que atualizar.
            if (tratador.normalizar(antigo.getNome())
                    .equals(tratador.normalizar(novo.getNome())))
            {
                return;
            }

            removerTermos(antigo);
            adicionarTermos(novo);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao atualizar curso no indice invertido", e);
        }
    }

    /**
     * Busca cursos por palavras-chave.
     *
     * Para cada termo da consulta:
     *   - le a lista invertida do termo;
     *   - IDF = log10(totalDeCursos / cursosComEsseTermo) + 1;
     *   - scoreDoTermo = TF * IDF para cada curso encontrado.
     *
     * Os scores de um mesmo curso (em termos diferentes) sao somados.
     * O resultado vem ordenado por score decrescente (mais relevante
     * primeiro).
     */
    public List<ResultadoBuscaCurso> buscar(String consulta)
    {
        try
        {
            int totalDeCursos = lista.numeroEntidades();

            // Acumula, em memoria apenas durante esta busca, o score de
            // cada curso. Isto nao substitui o indice: e so o resultado.
            Map<Integer, Double> scorePorCurso = new LinkedHashMap<>();

            for (String termo : termosDistintos(tratador.extrairTermos(consulta)))
            {
                ElementoLista[] ocorrencias = lista.read(termo);
                int cursosComEsseTermo = ocorrencias.length;

                if (cursosComEsseTermo == 0)
                {
                    continue;
                }

                double idf = Math.log10((double) totalDeCursos / cursosComEsseTermo) + 1;

                for (ElementoLista ocorrencia : ocorrencias)
                {
                    double scoreDoTermo = ocorrencia.getFrequencia() * idf;
                    scorePorCurso.merge(ocorrencia.getId(), scoreDoTermo, Double::sum);
                }
            }

            List<ResultadoBuscaCurso> resultados = new ArrayList<>();

            for (Map.Entry<Integer, Double> entrada : scorePorCurso.entrySet())
            {
                resultados.add(new ResultadoBuscaCurso(entrada.getKey(), entrada.getValue()));
            }

            // Ordena por relevancia (ver ResultadoBuscaCurso.compareTo).
            Collections.sort(resultados);
            return resultados;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Erro ao buscar cursos por palavras-chave", e);
        }
    }

    // Indexa os termos do nome do curso, calculando o TF de cada termo.
    // TF = vezes que o termo aparece / total de termos validos do nome.
    private void adicionarTermos(Curso curso) throws Exception
    {
        List<String> termos = tratador.extrairTermos(curso.getNome());

        if (termos.isEmpty())
        {
            return;
        }

        int totalTermos = termos.size();

        // Conta quantas vezes cada termo aparece no nome (uso local,
        // so para calcular o TF deste curso).
        Map<String, Integer> contagem = new LinkedHashMap<>();

        for (String termo : termos)
        {
            contagem.merge(termo, 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entrada : contagem.entrySet())
        {
            float tf = (float) entrada.getValue() / totalTermos;
            lista.create(entrada.getKey(), new ElementoLista(curso.getId(), tf));
        }
    }

    // Remove do indice todos os termos (distintos) do nome do curso.
    private void removerTermos(Curso curso) throws Exception
    {
        for (String termo : termosDistintos(tratador.extrairTermos(curso.getNome())))
        {
            lista.delete(termo, curso.getId());
        }
    }

    // Remove termos repetidos, mantendo a ordem de aparicao.
    private List<String> termosDistintos(List<String> termos)
    {
        List<String> distintos = new ArrayList<>();

        for (String termo : termos)
        {
            if (!distintos.contains(termo))
            {
                distintos.add(termo);
            }
        }

        return distintos;
    }
}
