package indice;

import entidades.Curso;

/**
 * Representa um curso encontrado em uma busca por palavras-chave,
 * junto com a pontuacao (soma de TF*IDF dos termos) usada para
 * ordenar os resultados por relevancia.
 */
public class ResultadoBuscaCurso implements Comparable<ResultadoBuscaCurso>
{
    private final int idCurso;
    private final double score;
    private Curso curso;

    public ResultadoBuscaCurso(int idCurso, double score)
    {
        this.idCurso = idCurso;
        this.score = score;
    }

    public int getIdCurso()
    {
        return idCurso;
    }

    public double getScore()
    {
        return score;
    }

    public Curso getCurso()
    {
        return curso;
    }

    public void setCurso(Curso curso)
    {
        this.curso = curso;
    }

    /**
     * Ordena do maior score para o menor (mais relevante primeiro).
     * Em caso de empate, usa o id do curso para manter ordem estavel.
     */
    @Override
    public int compareTo(ResultadoBuscaCurso outro)
    {
        int comparacao = Double.compare(outro.score, this.score);

        if (comparacao != 0)
        {
            return comparacao;
        }

        return Integer.compare(this.idCurso, outro.idCurso);
    }
}
