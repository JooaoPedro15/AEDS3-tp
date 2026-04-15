package utils;

import java.security.SecureRandom;

public final class GeradorCodigo {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALFABETO = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TAMANHO_PADRAO = 10;

    private GeradorCodigo() {
    }

    public static String gerar() {
        return gerar(TAMANHO_PADRAO);
    }

    public static String gerar(int tamanho) {
        if (tamanho <= 0) {
            throw new IllegalArgumentException("Tamanho do codigo deve ser maior que zero");
        }

        StringBuilder codigo = new StringBuilder(tamanho);

        for (int i = 0; i < tamanho; i++) {
            int indice = RANDOM.nextInt(ALFABETO.length());
            codigo.append(ALFABETO.charAt(indice));
        }

        return codigo.toString();
    }
}