import aed3.ElementoLista;
import aed3.ListaInvertida;
import entidades.Curso;
import indice.IndiceInvertidoCursos;
import indice.ResultadoBuscaCurso;
import indice.TratadorDeTermos;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Teste automatizado do indice invertido (TP3).
 *
 * Usa os 4 cursos do enunciado e a busca "Inteligencia Artificial"
 * para validar: normalizacao dos termos, remocao de stop words,
 * calculo de TF, calculo de IDF, soma TF*IDF e ordenacao.
 *
 * Grava em arquivos temporarios proprios, sem tocar nos dados reais
 * do programa.
 */
public class TesteIndiceInvertido {

    private static final String DIC = "dados/teste.cursos.dicionario.listainv.db";
    private static final String BLO = "dados/teste.cursos.blocos.listainv.db";

    private static int falhas = 0;

    public static void main(String[] args) {
        new File("dados").mkdir();

        TratadorDeTermos tratador = new TratadorDeTermos();

        // Cursos do enunciado.
        Curso c1 = new Curso(1, 0, "Introducao a Inteligencia Artificial", "", "01/01/2026", "C1", 0);
        Curso c2 = new Curso(2, 0, "Inteligencia Emocional para Gestores", "", "01/01/2026", "C2", 0);
        Curso c3 = new Curso(3, 0, "Inteligencia no Trabalho por Meio da Inteligencia Artificial", "", "01/01/2026", "C3", 0);
        Curso c4 = new Curso(4, 0, "Introducao a Gestao de Equipes", "", "01/01/2026", "C4", 0);
        Curso[] cursos = { c1, c2, c3, c4 };

        System.out.println("==================================================");
        System.out.println("  TESTE DO INDICE INVERTIDO - TP3");
        System.out.println("==================================================");

        // 1) Normalizacao + remocao de stop words.
        System.out.println("\n1) TERMOS APOS NORMALIZACAO E REMOCAO DE STOP WORDS\n");
        for (Curso c : cursos) {
            System.out.println("ID " + c.getId() + " - \"" + c.getNome() + "\"");
            System.out.println("    -> " + tratador.extrairTermos(c.getNome()));
        }

        // Confere o exemplo do enunciado.
        verificar(
                "ID 1 gera [introducao, inteligencia, artificial]",
                tratador.extrairTermos("Introducao a Inteligencia Artificial")
                        .toString().equals("[introducao, inteligencia, artificial]")
        );

        // Mostra que o tratamento tambem remove acentos de verdade.
        System.out.println("\n   (com acento) \"Introducao a Inteligencia Artificial\" =");
        System.out.println("    -> " + tratador.extrairTermos("Introdução à Inteligência Artificial"));
        verificar(
                "Versao acentuada gera os mesmos termos",
                tratador.extrairTermos("Introdução à Inteligência Artificial")
                        .toString().equals("[introducao, inteligencia, artificial]")
        );

        // 2) TF por curso.
        System.out.println("\n2) TF (frequencia do termo no nome) POR CURSO\n");
        for (Curso c : cursos) {
            System.out.println("ID " + c.getId() + ":");
            for (Map.Entry<String, Double> e : calcularTf(tratador, c.getNome()).entrySet()) {
                System.out.printf("    %-14s TF = %.4f%n", e.getKey(), e.getValue());
            }
        }
        verificar(
                "TF de 'inteligencia' no ID 3 = 0.4 (2 em 5 termos)",
                Math.abs(calcularTf(tratador, c3.getNome()).get("inteligencia") - 0.4) < 1e-6
        );

        // 3) Monta o indice e busca.
        IndiceInvertidoCursos indice = new IndiceInvertidoCursos(DIC, BLO);
        for (Curso c : cursos) {
            indice.inserir(c);
        }

        System.out.println("\n3) BUSCA: \"Inteligencia Artificial\"");
        System.out.println("   Termos da busca: " + tratador.extrairTermos("Inteligencia Artificial"));
        System.out.println("\n   RESULTADOS (ordenados por TF*IDF):\n");

        List<ResultadoBuscaCurso> resultados = indice.buscar("Inteligencia Artificial");

        for (int i = 0; i < resultados.size(); i++) {
            ResultadoBuscaCurso r = resultados.get(i);
            System.out.printf(
                    "    %d) ID %d  score=%.4f  -  %s%n",
                    i + 1, r.getIdCurso(), r.getScore(),
                    cursoPorId(cursos, r.getIdCurso()).getNome()
            );
        }

        // 4) Verificacoes do enunciado.
        System.out.println("\n4) VERIFICACOES\n");

        verificar("Curso ID 4 NAO aparece na busca", !contemId(resultados, 4));
        verificar("Foram encontrados 3 cursos (IDs 1, 2 e 3)", resultados.size() == 3);
        verificar("Resultados em ordem decrescente de score", estaOrdenado(resultados));
        verificar("Curso ID 1 e o mais relevante (1o lugar)",
                !resultados.isEmpty() && resultados.get(0).getIdCurso() == 1);

        // 5) Atualizacao do indice: remover um curso.
        indice.remover(c1);
        List<ResultadoBuscaCurso> aposRemocao = indice.buscar("Inteligencia Artificial");
        verificar("Apos remover o ID 1, ele some da busca",
                !contemId(aposRemocao, 1));

        // 6) Atualizacao do indice: alterar o nome de um curso.
        Curso c2novo = new Curso(2, 0, "Lideranca e Inteligencia Artificial Aplicada", "", "01/01/2026", "C2", 0);
        indice.atualizar(c2, c2novo);
        List<ResultadoBuscaCurso> aposEdicao = indice.buscar("artificial");
        verificar("Apos editar o nome do ID 2, ele passa a casar com 'artificial'",
                contemId(aposEdicao, 2));

        // Resultado final.
        System.out.println("\n==================================================");
        if (falhas == 0) {
            System.out.println("  RESULTADO: TODOS OS TESTES PASSARAM");
        } else {
            System.out.println("  RESULTADO: " + falhas + " TESTE(S) FALHARAM");
        }
        System.out.println("==================================================");

        // Fecha e limpa os arquivos temporarios do teste.
        indice.fechar();
        new File(DIC).delete();
        new File(BLO).delete();
    }

    // TF de cada termo distinto = ocorrencias / total de termos do nome.
    private static Map<String, Double> calcularTf(TratadorDeTermos tratador, String nome) {
        List<String> termos = tratador.extrairTermos(nome);
        Map<String, Integer> contagem = new LinkedHashMap<>();

        for (String t : termos) {
            contagem.merge(t, 1, Integer::sum);
        }

        Map<String, Double> tf = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : contagem.entrySet()) {
            tf.put(e.getKey(), (double) e.getValue() / termos.size());
        }

        return tf;
    }

    private static Curso cursoPorId(Curso[] cursos, int id) {
        for (Curso c : cursos) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private static boolean contemId(List<ResultadoBuscaCurso> lista, int id) {
        for (ResultadoBuscaCurso r : lista) {
            if (r.getIdCurso() == id) {
                return true;
            }
        }
        return false;
    }

    private static boolean estaOrdenado(List<ResultadoBuscaCurso> lista) {
        for (int i = 1; i < lista.size(); i++) {
            if (lista.get(i - 1).getScore() < lista.get(i).getScore()) {
                return false;
            }
        }
        return true;
    }

    private static void verificar(String descricao, boolean condicao) {
        System.out.println("    [" + (condicao ? "OK " : "FALHOU") + "] " + descricao);
        if (!condicao) {
            falhas++;
        }
    }
}
