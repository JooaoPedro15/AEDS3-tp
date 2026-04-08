import java.util.Scanner;

import entidades.livros.MenuLivros;
import entidades.pessoas.MenuPessoas;

public class Principal {
    
    public static void main(String[] args) {

        Scanner console = new Scanner(System.in);

        int opcao;
        do {
            System.out.println("\n\nAEDs III");
            System.out.println(    "--------");
            System.out.println("\n> Início\n");
            System.out.println("1 - Pessoas");
            System.out.println("2 - Livros");
            System.out.println("0 - Sair");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: 
                    (new MenuPessoas()).menu();
                    break;
                case 2:
                    (new MenuLivros()).menu();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }

}
