package indice;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Trata o texto dos nomes dos cursos e das buscas.
 *
 * Coloca tudo em minusculas, remove acentos, quebra o texto em
 * palavras e descarta as stop words em portugues. O mesmo tratamento
 * e aplicado no momento de indexar um curso e no momento da busca,
 * garantindo que os termos comparados sejam equivalentes.
 */
public class TratadorDeTermos
{
    // Stop words ja normalizadas (minusculas e sem acento).
    // Os termos acentuados do enunciado (a, as) caem aqui apos a
    // remocao de acentos -> "a" e "as".
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "o", "os", "as", "um", "uma", "uns", "umas",
        "de", "da", "do", "das", "dos",
        "em", "no", "na", "nos", "nas",
        "para", "por", "com",
        "e", "ou",
        "ao", "aos"
    ));

    /**
     * Normaliza um texto: remove acentos e converte para minusculas.
     */
    public String normalizar(String texto)
    {
        if (texto == null)
        {
            return "";
        }

        // NFD separa a letra do acento; \p{M} remove as marcas de acento.
        String semAcento = Normalizer
                .normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return semAcento.toLowerCase();
    }

    /**
     * Quebra o texto em termos validos: minusculo, sem acento e sem
     * stop words. As repeticoes sao mantidas, pois sao necessarias
     * para o calculo do TF (frequencia do termo no nome).
     */
    public List<String> extrairTermos(String texto)
    {
        List<String> termos = new ArrayList<>();
        String normalizado = normalizar(texto);

        // Quebra em qualquer caractere que nao seja letra de a-z.
        for (String palavra : normalizado.split("[^a-z]+"))
        {
            if (palavra.isEmpty())
            {
                continue;
            }

            if (STOP_WORDS.contains(palavra))
            {
                continue;
            }

            termos.add(palavra);
        }

        return termos;
    }
}
