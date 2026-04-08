package entidades.livros;
import entidades.pessoas.ArquivoPessoa;
import entidades.pessoas.Pessoa;

import java.io.File;
import java.time.LocalDate;
import java.util.Scanner;

public class MenuLivros {

    ArquivoLivro arqLivros;
    ArquivoPessoa arqPessoas;
    Scanner console;

    public void menu() {

        try {
            console = new Scanner(System.in);
            arqLivros = new ArquivoLivro();
            arqPessoas = new ArquivoPessoa();

            int opcao;
            do {
                System.out.println("\n\nAEDs III");
                System.out.println(    "--------");
                System.out.println("\n> Início > Livros\n");
                System.out.println("1 - Inserir");
                System.out.println("2 - Buscar por ISBN");
                System.out.println("3 - Buscar por Título");
                System.out.println("4 - Buscar por Autor");
                System.out.println("5 - Alterar");
                System.out.println("6 - Excluir");
                System.out.println("8 - Listar todos os livros");
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
                        buscarIsbn();
                        break;
                    case 3: 
                        buscarTitulo();
                        break;
                    case 4: 
                        buscarAutor();
                        break;
                    case 5: 
                        alterar();
                        break;
                    case 6: 
                        excluir();
                        break;
                    case 8:
                        listarTodos();
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
            arqLivros.close();
            arqPessoas.close();

        } catch(Exception e) {
            System.err.println("Não foi possível criar o menu de livros!");
            e.printStackTrace();
        }
    }

    private void inserir() throws Exception {
        String titulo;
        String cpfAutor;
        String isbn;
        byte edicao;
        LocalDate dataPublicacao;
        float preco;  
        boolean dadosValidos;                  
        System.out.println("INCLUSÃO");

        // ISBN
        dadosValidos = false;
        do {
            System.out.print("ISBN: ");
            isbn = console.nextLine();
            if(isbn.length()==0)
                return;
            if(!isbn.matches("\\d{13}")) {
                System.out.println("ISBN inválido!");
            } else {
                Livro l1 = arqLivros.readIsbn(isbn);
                if(l1!=null)
                    System.out.println("ISBN já cadastrado!");
                else if(isbn.length()==13)
                    dadosValidos = true;
            }
        } while(!dadosValidos);

        // Título
        System.out.print("Título: ");
        titulo = console.nextLine();
        if(titulo.length()==0)
            return;

        // Autor
        dadosValidos = false;
        Pessoa autor = null;
        int idAutor = -1;
        do {
            System.out.print("CPF Autor: ");
            cpfAutor = console.nextLine();
            if(cpfAutor.length()==0)
                return;

            if(!cpfAutor.matches("\\d{11}")) {
                System.out.println("CPF inválido!");
            } else {
                autor = arqPessoas.readCPF(cpfAutor);
                if(autor==null)
                    System.out.println("Autor não encontrado!");
                else {
                    System.out.println("Autor: " + autor.getNome());
                    idAutor = autor.getID();
                    dadosValidos = true;
                }
            }
        } while(!dadosValidos);

        // Edição
        dadosValidos = false;
        edicao = (byte)0;
        do {
            System.out.print("Edição: ");
            String r = console.nextLine();
            try {
                edicao = Byte.parseByte(r);
                if(edicao>0)
                    dadosValidos = true;
                else
                    System.out.println("Edição precisa ser maior que 1!");
            } catch(Exception e) {
                System.out.println("Edição inválida!");
            }
        } while(!dadosValidos);

        // Data de publicação
        dadosValidos = false;
        dataPublicacao = LocalDate.now();
        do {
            System.out.print("Data de publicação (dd/mm/aaaa): ");
            String data = console.nextLine();
            try {
                String[] dadosData = data.split("/");
                dataPublicacao = LocalDate.of(
                    Integer.parseInt(dadosData[2]),
                    Integer.parseInt(dadosData[1]),
                    Integer.parseInt(dadosData[0]));
                dadosValidos = true;
            } catch(Exception e) {
                System.out.println("Data inválida!");
            }
        } while(!dadosValidos);

        // Preço
        dadosValidos = false;
        preco = 0F;
        do {
            System.out.print("Preço: R$ ");
            String p = console.nextLine();
            try {
                preco = Float.parseFloat(p);
                dadosValidos = true;
            } catch(Exception e) {
                System.out.println("Valor inválido!");
            }
        } while(!dadosValidos);

        System.out.print("Confirmar inclusão (S/N) ?");
        String confirma = console.nextLine();
        if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
            Livro l = new Livro(titulo, idAutor, isbn, edicao, dataPublicacao, preco);
            arqLivros.create(l);
            System.out.println("Livro incluído!");
        }
    }

    private void buscarIsbn() throws Exception {     
        System.out.println("BUSCA POR ISBN");
        System.out.print("ISBN: ");
        String isbn = console.nextLine();
        if(isbn.length()==0)
            return;
        if(!isbn.matches("\\d{13}")) {
            System.out.println("ISBN inválido!");
            return;
        }
        Livro l = arqLivros.readIsbn(isbn);
        if(l!=null)
            mostraLivro(l);
        else
            System.out.println("Livro não encontrado!");
    }

    private void buscarTitulo() throws Exception {     
        System.out.println("BUSCA POR TÍTULO");
        System.out.print("Nome: ");
        String nome = console.nextLine();
        if(nome.length()==0)
            return;
        Livro[] livros = arqLivros.readTitulo(nome);
        if(livros.length>0) {
            for(Livro l : livros)
                mostraLivro(l);
        }
        else
            System.out.println("Nenhum livro encontrado!");
    }

    private void buscarAutor() throws Exception {     
        System.out.println("BUSCA POR AUTOR");
        System.out.print("CPF do autor: ");
        String cpf = console.nextLine();
        if(cpf.length()==0)
            return;
        if(!cpf.matches("\\d{11}")) {
            System.out.println("CPF inválido!");
            return;
        }
        Pessoa autor = arqPessoas.readCPF(cpf);
        if(autor==null) {
            System.out.println("Autor não encontrado!");
            return;
        }
        Livro[] livros = arqLivros.readAutor(autor.getID());
        if(livros.length>0) {
            for(Livro l : livros)
                mostraLivro(l);
        }
        else
            System.out.println("Nenhum livro encontrado para o autor " + autor.getNome() + "!");
    }

    private void excluir() throws Exception {
        System.out.println("EXCLUSÃO");
        System.out.print("ISBN: ");
        String isbn = console.nextLine();
        if(isbn.length()==0)
            return;
        if(!isbn.matches("\\d{13}")) {
            System.out.println("ISBN inválido!");
            return;
        }
        Livro l = arqLivros.readIsbn(isbn);

        if(l!=null) {
            mostraLivro(l);
            System.out.print("\nConfirma exclusão (S/N) ?");
            String confirma = console.nextLine();
            if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
                if(arqLivros.delete(l.getID()))
                    System.out.println("Livro excluído!");
                else
                    System.out.println("Erro na exclusão!");
            }
        }
        else
            System.out.println("Livro não encontrado!");
    }

    private void alterar() throws Exception {
        System.out.println("ALTERAÇÃO");
        System.out.print("ISBN: ");
        String isbn = console.nextLine();
        if(isbn.length()==0)
            return;
        if(!isbn.matches("\\d{13}")) {
            System.out.println("ISBN inválido!");
            return;
        }
        Livro l = arqLivros.readIsbn(isbn);

        if(l!=null) {
            mostraLivro(l);

            System.out.println("\nAltere os dados a seguir. Deixe o campo em branco quando não quiser alterar.");
            String novoTitulo;
            String novoCpfAutor;
            String novoIsbn;
            byte novaEdicao;
            LocalDate novaDataPublicacao;
            float novoPreco;

            // Alteração do ISBN
            boolean dadosValidos = false;
            do {
                System.out.print("ISBN: ");
                novoIsbn = console.nextLine();
                if(novoIsbn.length()==0) {
                    dadosValidos = true;
                } else {
                    if(!novoIsbn.matches("\\d{13}")) {
                        System.out.println("ISBN inválido!");
                    } else {
                        Livro l1 = arqLivros.readIsbn(novoIsbn);
                        if(l1!=null)
                            System.out.println("ISBN já cadastrado!");
                        else 
                            dadosValidos = true;
                    }
                }
            } while(!dadosValidos);
            if(novoIsbn.length()>0)
                l.setIsbn(novoIsbn);

            // Alteração do título
            System.out.print("Título: ");
            novoTitulo = console.nextLine();
            if(novoTitulo.length()>0)
                l.setTitulo(novoTitulo);

            // Alteração do autor
            Pessoa autor = null;
            int idAutor = -1;
            dadosValidos = false;
            do {
                System.out.print("Autor: ");
                novoCpfAutor = console.nextLine();
                if(novoCpfAutor.length()>0) {
                    if(!novoCpfAutor.matches("\\d{11}")) {
                        System.out.println("CPF inválido!");
                    } else {
                        autor = arqPessoas.readCPF(novoCpfAutor);
                        if(autor==null)
                            System.out.println("Autor não encontrado!");
                        else {
                            System.out.println("Autor: " + autor.getNome());
                            idAutor = autor.getID();
                            dadosValidos = true;
                        }
                    }
                } else {
                    dadosValidos = true;
                }
            } while(!dadosValidos);
            if(novoCpfAutor.length()>0)
                l.setAutor(idAutor);

            // Alteração da edição
            dadosValidos = false;
            novaEdicao = (byte)0;
            String aux = "";
            do {
                System.out.print("Edição: ");
                aux = console.nextLine();
                if(aux.length()==0) {
                    dadosValidos = true;
                } else {
                    try {
                        novaEdicao = Byte.parseByte(aux);
                        dadosValidos = true;
                    } catch(Exception e) {
                        System.out.println("Valor inválido!");
                    }
                }
            } while(!dadosValidos);
            if(aux.length()>0)
                l.setEdicao(novaEdicao);

            // Alteração da data de publicação
            dadosValidos = false;
            novaDataPublicacao = LocalDate.now();
            aux = "";
            do {
                System.out.print("Data de publicação (dd/mm/aaaa): ");
                aux = console.nextLine();
                if(aux.length()==0) {
                    dadosValidos = true;
                }
                else {
                    try {
                        String[] dadosData = aux.split("/");
                        novaDataPublicacao = LocalDate.of(
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
                l.setDataPublicacao(novaDataPublicacao);

            // Alteração do preço
            dadosValidos = false;
            novoPreco = 0F;
            aux = "";
            do {
                System.out.print("Preço: R$ ");
                aux = console.nextLine();
                if(aux.length()==0) {
                    dadosValidos = true;
                } else {
                    try {
                        novoPreco = Float.parseFloat(aux);
                        dadosValidos = true;
                    } catch(Exception e) {
                        System.out.println("Valor inválido!");
                    }
                }
            } while(!dadosValidos);
            if(aux.length()>0)
                l.setPreco(novoPreco);

            System.out.print("\nConfirma alteração (S/N) ?");
            String confirma = console.nextLine();
            if(confirma.charAt(0)=='S' || confirma.charAt(0)=='s') {
                if(arqLivros.update(l))
                    System.out.println("Livro atualizado!");
                else
                    System.out.println("Erro na alteração!");
            }
        }
        else
            System.out.println("Livro não encontrado!");
        
    }

    public void mostraLivro(Livro l) throws Exception {
        Pessoa autor = arqPessoas.read(l.getIdAutor());
        if(autor==null)
            throw new Exception("Autor do livro não encontrado!");
        System.out.println(  
                 "ISBN......: " + l.getIsbn() +
               "\nTítulo....: " + l.getTitulo() +
               "\nAutor.....: " + autor.getNome() +
               "\nEdição....: " + l.getEdicao() +
               "\nDt.Public.: " + l.getDataPublicacao() +
               "\nPreço.....: R$ " + l.getPreco() + "\n"
        );
    }

    public void listarTodos() throws Exception {
        System.out.println("LISTAGEM COMPLETA");
        Livro[] lista = arqLivros.readAll();
        if(lista.length == 0) {
            System.out.println("Nenhum livro cadastrado!");
            return;
        }
        for (Livro l : lista) {
            mostraLivro(l);
        }
    }

    public  void popular() throws Exception {
        arqLivros.close();
        arqLivros = null;

        (new File("./dados/livro/dados.db")).delete();
        (new File("./dados/livro/indiceDireto.d.db")).delete();
        (new File("./dados/livro/indiceDireto.c.db")).delete();
        (new File("./dados/livro/indiceISBN.d.db")).delete();
        (new File("./dados/livro/indiceISBN.c.db")).delete();
        (new File("./dados/livro/indiceTitulo.db")).delete();
        (new File("./dados/livro/indiceAutor.db")).delete();

        arqLivros = new ArquivoLivro();

        arqLivros.create(new Livro("Foco Roubado", 1, "9786560020047", 1, LocalDate.of(2023,6,30),  54.01F));
        arqLivros.create(new Livro("O Poder da Autodisciplina", 2, "9786556092829", 1, LocalDate.of(2024, 2, 20), 33.25F));
        arqLivros.create(new Livro("Hábitos Atômicos", 3, "9788550807560", 1, LocalDate.of(2019, 8, 8), 32.81F));
        arqLivros.create(new Livro("A Psicologia Financeira", 4, "9786555111101", 1, LocalDate.of(2021, 3, 15), 31.38F));
        arqLivros.create(new Livro("Ligações Perdidas", 1, "9789895773770", 1, LocalDate.of(2025, 4, 17), 74.90F));
    }

    
}
