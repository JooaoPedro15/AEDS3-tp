import arquivos.ArquivoCursos;
import controle.ControleCurso;
import controle.ControleUsuario;
import entidades.Usuario;
import utils.Entrada;

public class Main {

    private static final ArquivoCursos arqCursos = new ArquivoCursos();
    private static final ControleUsuario controleUsuario = new ControleUsuario(arqCursos);
    private static final ControleCurso controleCurso = new ControleCurso(arqCursos);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\nEntrePares 1.0");
            System.out.println("--------------");
            System.out.println("\n(A) Login");
            System.out.println("(B) Novo usuario");
            System.out.println("(C) Esqueci minha senha");
            System.out.println("(S) Sair");
            System.out.print("\nOpcao: ");

            String opcao = Entrada.SCANNER.nextLine().trim();

            if (opcao.equalsIgnoreCase("A")) {
                Usuario logado = controleUsuario.login();

                if (logado != null) {
                    menuPrincipal(logado);
                }

                continue;
            }

            if (opcao.equalsIgnoreCase("B")) {
                controleUsuario.cadastrar();
                continue;
            }

            if(opcao.equalsIgnoreCase("C")){
                controleUsuario.recuperarSenha();
                continue;
            }

            if (opcao.equalsIgnoreCase("S")) {
                System.out.println("Saindo...");
                return;
            }

            System.out.println("Opcao invalida.");
        }
    }

    private static void menuPrincipal(Usuario logado) {
        while (logado != null) {
            System.out.println("\nEntrePares 1.0");
            System.out.println("--------------");
            System.out.println("> Inicio");
            System.out.println("\n(A) Meus dados");
            System.out.println("(B) Meus cursos");
            System.out.println("(C) Minhas inscricoes");
            System.out.println("(S) Sair");
            System.out.print("\nOpcao: ");

            String opcao = Entrada.SCANNER.nextLine().trim();

            if (opcao.equalsIgnoreCase("A")) {
                logado = controleUsuario.menuMeusDados(logado);
                continue;
            }

            if (opcao.equalsIgnoreCase("B")) {
                controleCurso.menu(logado);
                continue;
            }

            if (opcao.equalsIgnoreCase("C")) {
                System.out.println("Modulo de inscricoes sera implementado no TP2.");
                continue;
            }

            if (opcao.equalsIgnoreCase("S")) {
                return;
            }

            System.out.println("Opcao invalida.");
        }
    }
}
