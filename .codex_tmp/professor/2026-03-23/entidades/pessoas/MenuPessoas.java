package entidades.pessoas;
import entidades.livros.ArquivoLivro;
import entidades.livros.Livro;

import java.io.File;
import java.time.LocalDate;
import java.util.Scanner;

public class MenuPessoas {

    ArquivoPessoa arqPessoas;
    ArquivoLivro arqLivros;
    Scanner console;

    public void menu() {

        try {
            console = new Scanner(System.in);
            arqPessoas = new ArquivoPessoa();
            arqLivros = new ArquivoLivro();

            int opcao;
            do {
                System.out.println("\n\nAEDs III");
                System.out.println(    "--------");
                System.out.println("\n> Início > Pessoas\n");
                System.out.println("1 - Inserir");
                System.out.println("2 - Buscar por CPF");
                System.out.println("3 - Buscar por Nome");
                System.out.println("4 - Alterar");
                System.out.println("5 - Excluir");
                System.out.println("8 - Listar todas as pessoas");
                System.out.println("9 - Popular BD");
                System.out.println("0 - Retornar ao menu anterior");
                System.out.print("\nOpção: ");
                try {
                    opcao = Integer.parseInt(console.nextLine());
                } catch (NumberFormatException e) {
                    opcao = -1;
                }

                switch (opcao) {
                    case 1: 
                        inserir();
                        break;
                    case 2: 
                        buscarCPF();
                        break;
                    case 3: 
                        buscarNome();
                        break;
                    case 4: 
                        alterar();
                        break;
                    case 5: 
                        excluir();
                        break;
                    case 8:
                        listarTodas();
                        break;
                    case 9: 
                        popular();
                        break;
                    case 0: break;
                    default:
                        System.out.println("Opção inválida");
                }
            } while (opcao != 0);

            // fecha todos os arquivos
            arqPessoas.close();
            arqLivros.close();
            
        } catch(Exception e) {
            System.err.println("Não foi possível criar o menu de pessoas!");
            e.printStackTrace();
        }
    }

    private void inserir() throws Exception {
        String nome;
        String cpf;
        LocalDate dataNascimento;
        boolean dadosValidos;                  
        System.out.println("INCLUSÃO");
        System.out.print("Nome: ");

        nome = console.nextLine();
        if(nome.length()==0)
            return;

        // CPF
        dadosValidos = false;
        do {
            System.out.print("CPF: ");
            cpf = console.nextLine();
            if(cpf.length()==0)
                return;
            if(!cpf.matches("\\d{11}")) {
                System.out.println("CPF inválido!");
            } else {
                Pessoa p1 = arqPessoas.readCPF(cpf);
                if(p1!=null)
                    System.out.println("CPF já cadastrado!");
                else if(cpf.length()==11)
                    dadosValidos = true;
            }
        } while(!dadosValidos);

        // Data de nascimento
        dadosValidos = false;
        dataNascimento = LocalDate.now();
        do {
            System.out.print("Data de nascimento (dd/mm/aaaa): ");
            String aux = console.nextLine();
            try {
                String[] dadosData = aux.split("/");
                dataNascimento = LocalDate.of(
                    Integer.parseInt(dadosData[2]),
                    Integer.parseInt(dadosData[1]),
                    Integer.parseInt(dadosData[0]));
                dadosValidos = true;
            } catch(Exception e) {
                System.out.println("Data inválida!");
            }
        } while(!dadosValidos);

        System.out.print("Confirmar inclusão (S/N) ?");
        String confirma = console.nextLine();
        if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
            Pessoa p = new Pessoa(nome, cpf, dataNascimento);
            arqPessoas.create(p);
            System.out.println("Pessoa incluída!");
        }
    }

    private void buscarCPF() throws Exception {     
        System.out.println("BUSCA");
        System.out.print("CPF: ");
        String cpf = console.nextLine();
        if(cpf.length()==0)
            return;
        if(!cpf.matches("\\d{11}")) {
            System.out.println("CPF inválido!");
            return;
        }
        Pessoa p = arqPessoas.readCPF(cpf);
        if(p!=null)
            System.out.println(p);
        else
            System.out.println("Pessoa não encontrada!");
    }

    private void buscarNome() throws Exception {     
        System.out.println("BUSCA");
        System.out.print("Nome: ");
        String nome = console.nextLine();
        if(nome.length()==0)
            return;
        Pessoa[] pessoas = arqPessoas.readNome(nome);
        if(pessoas.length>0) {
            for(Pessoa p : pessoas)
                System.out.println(p);
        }
        else
            System.out.println("Nenhuma pessoa encontrada!");
    }

    private void excluir() throws Exception {
        System.out.println("EXCLUSÃO");
        System.out.print("CPF: ");
        String cpf = console.nextLine();
        if(cpf.length()==0)
            return;
        if(!cpf.matches("\\d{11}")) {
            System.out.println("CPF inválido!");
            return;
        }
        Pessoa p = arqPessoas.readCPF(cpf);
        if(p!=null) {
            System.out.println(p);

            Livro[] livros = arqLivros.readAutor(p.getID());
            if(livros.length>0) {
                System.out.println("Não é possível excluir esta pessoa, pois ela é autora de um ou mais livros:");
                for(Livro l : livros)
                    System.out.println(l);
                return;
            }

            System.out.print("\nConfirma exclusão (S/N) ?");
            String confirma = console.nextLine();
            if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
                if(arqPessoas.delete(p.getID()))
                    System.out.println("Pessoa excluída!");
                else
                    System.out.println("Erro na exclusão!");
            }
        }
        else
            System.out.println("Pessoa não encontrada!");
    }

    private void alterar() throws Exception {
        System.out.println("ALTERAÇÃO");
        System.out.print("CPF: ");
        String cpf = console.nextLine();
        if(cpf.length()==0)
            return;
        if(!cpf.matches("\\d{11}")) {
            System.out.println("CPF inválido!");
            return;
        }
        Pessoa p = arqPessoas.readCPF(cpf);

        if(p!=null) {
            System.out.println(p);

            System.out.println("\nAltere os dados a seguir. Deixe o campo em branco quando não quiser alterar.");
            String novoNome;
            String novoCPF;
            LocalDate novaDN;

            // Alteração do nome
            System.out.print("Nome: ");
            novoNome = console.nextLine();
            if(novoNome.length()>0)
                p.setNome(novoNome);

            // Alteração do CPF
            boolean dadosValidos = false;
            do {
                System.out.print("CPF: ");
                novoCPF = console.nextLine();
                if(novoCPF.length()==0) {
                    dadosValidos = true;
                } else {
                    if(!novoCPF.matches("\\d{11}")) {
                        System.out.println("CPF inválido!");
                    } else {
                        Pessoa p1 = arqPessoas.readCPF(novoCPF);
                        if(p1!=null)
                            System.out.println("CPF já cadastrado!");
                        else 
                            dadosValidos = true;
                    }
                }
            } while(!dadosValidos);
            if(novoCPF.length()>0)
                p.setCpf(novoCPF);

            // Alteração da data de nascimento
            dadosValidos = false;
            novaDN = LocalDate.now();
            String aux = "";
            do {
                System.out.print("Data de nascimento (dd/mm/aaaa): ");
                aux = console.nextLine();
                if(aux.length()==0) {
                    dadosValidos = true;
                }
                else {
                    try {
                        String[] dadosData = aux.split("/");
                        novaDN = LocalDate.of(
                            Integer.parseInt(dadosData[2]),
                            Integer.parseInt(dadosData[1]),
                            Integer.parseInt(dadosData[0]));
                        dadosValidos = true;
                    } catch(Exception e) {
                        System.out.println("Data inválida!");
                    }
                }
            } while(!dadosValidos);
            if(aux.length()>0)
                p.setDataNascimento(novaDN);

            System.out.print("\nConfirma alteração (S/N) ?");
            String confirma = console.nextLine();
            if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
                if(arqPessoas.update(p))
                    System.out.println("Pessoa atualizada!");
                else
                    System.out.println("Erro na alteração!");
            }
        }
        else
            System.out.println("Pessoa não encontrada!");
        
    }

    public void listarTodas() throws Exception {
        System.out.println("LISTAGEM COMPLETA");
        Pessoa[] pessoas = arqPessoas.readAll();
        if(pessoas.length>0) {
            for(Pessoa p : pessoas)
                System.out.println(p);
        }
        else
            System.out.println("Nenhuma pessoa encontrada!");
    }

    public  void popular() throws Exception {
        arqPessoas.close();
        arqPessoas = null;

        (new File("./dados/pessoa/dados.db")).delete();
        (new File("./dados/pessoa/indiceDireto.d.db")).delete();
        (new File("./dados/pessoa/indiceDireto.c.db")).delete();
        (new File("./dados/pessoa/indiceCPF.d.db")).delete();
        (new File("./dados/pessoa/indiceCPF.c.db")).delete();
        (new File("./dados/pessoa/indiceNome.db")).delete();


        arqPessoas = new ArquivoPessoa();

        arqPessoas.create(new Pessoa("Johann Hari", "11111111111", LocalDate.of(1979, 1, 21)));
        arqPessoas.create(new Pessoa( "Brian Traci", "22222222222", LocalDate.of(1944, 1, 5)));
        arqPessoas.create(new Pessoa( "James Clear", "33333333333", LocalDate.of(1986, 1, 22)));
        arqPessoas.create(new Pessoa( "Morgan Housel", "44444444444", LocalDate.of(1986, 7, 20)));
    }

    
}
