# TP1 - EntrePares 1.0

## Integrantes
- Jamille Ferreira
- Joao Pedro
- Maria Clara

## 1. Descricao do sistema

O sistema EntrePares 1.0 permite:

- cadastrar usuarios;
- autenticar usuarios por e-mail e senha;
- recuperar senha com pergunta secreta;
- cadastrar cursos vinculados ao usuario logado;
- alterar, concluir, encerrar inscricoes ou cancelar cursos;
- listar os cursos do usuario ativo.

Neste TP1, o modulo de inscricoes ainda nao foi implementado. Por isso, a opcao "Minhas inscricoes" e o gerenciamento de inscritos no curso permanecem reservados para o TP2.

## 2. Organizacao do projeto

O projeto segue o padrao MVC, separando entidades, persistencia, controle e visao.

### Classes principais

- `src/Main.java`
- `src/controle/ControleUsuario.java`
- `src/controle/ControleCurso.java`
- `src/visao/VisaoUsuario.java`
- `src/visao/VisaoCurso.java`
- `src/arquivos/ArquivoUsuarios.java`
- `src/arquivos/ArquivoCursos.java`
- `src/estruturas/ArquivoIndexado.java`
- `src/estruturas/TabelaHashExtensivel.java`
- `src/estruturas/ArvoreBMais.java`
- `src/entidades/Usuario.java`
- `src/entidades/Curso.java`

## 3. Entidades

### Usuario

- `id`
- `nome`
- `email`
- `hashSenha`
- `perguntaSecreta`
- `hashRespostaSecreta`

### Curso

- `id`
- `idUsuario`
- `nome`
- `descricao`
- `dataInicio`
- `codigo`
- `estado`

Relacao implementada:

- 1 usuario possui N cursos;
- cada curso pertence a 1 unico usuario por meio de `idUsuario`.

## 4. Persistencia e indices

### CRUD generico

A classe `ArquivoIndexado` foi implementada no modelo apresentado em sala:

- cabecalho com ultimo ID e cabeca da lista de espacos livres;
- registro com lapide, tamanho e vetor de bytes;
- indice direto por hash extensivel apontando de `id` para o endereco do registro no arquivo;
- reaproveitamento de espacos excluidos.

### Indices de usuarios

A classe `ArquivoUsuarios` estende `ArquivoIndexado<Usuario>` e mantem:

- indice indireto por e-mail com hash extensivel.

Operacoes especiais:

- busca de usuario por e-mail;
- validacao de unicidade de e-mail;
- exclusao bloqueada quando o usuario possui cursos ativos.

### Indices de cursos

A classe `ArquivoCursos` estende `ArquivoIndexado<Curso>` e mantem:

- indice indireto por codigo compartilhavel com hash extensivel;
- indice indireto por nome com arvore B+;
- relacionamento 1:N `idUsuario -> idCurso` com arvore B+.

Operacoes especiais:

- listagem dos cursos do usuario ativo;
- ordenacao alfabetica dos cursos na exibicao do menu;
- geracao automatica de codigo compartilhavel de 10 caracteres;
- remocao automatica de cursos inativos quando um usuario e excluido.

## 5. Menus implementados

### Tela inicial

- login;
- novo usuario;
- recuperacao de senha;
- sair.

### Menu principal do usuario logado

- meus dados;
- meus cursos;
- minhas inscricoes (reservado para o TP2);
- sair.

### Menu de cursos

- novo curso;
- visualizar curso selecionado;
- corrigir dados do curso;
- encerrar inscricoes;
- concluir curso;
- cancelar curso;
- gerenciar inscritos (reservado para o TP2).

## 6. Regras de negocio implementadas

- o login e feito por e-mail e senha com comparacao por hash;
- a resposta secreta tambem e armazenada em hash;
- um e-mail nao pode ser reutilizado por outro usuario;
- um codigo compartilhavel nao pode ser reutilizado por outro curso;
- todo curso novo recebe automaticamente o `idUsuario` do usuario logado;
- usuarios com cursos ativos nao podem ser excluidos;
- ao excluir um usuario, os cursos inativos vinculados a ele sao removidos;
- ao cancelar um curso sem inscritos, o registro e excluido;
- como o modulo de inscricoes ainda nao existe no TP1, a verificacao de inscritos permanece preparada para o TP2.

## 7. Compilacao e execucao

Exemplo de compilacao:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

Exemplo de execucao:

```powershell
java -cp out Main
```

## 8. Evidencias que devem aparecer no video

- cadastro de usuario;
- login;
- recuperacao de senha;
- criacao de curso;
- listagem de cursos;
- alteracao de curso;
- exclusao de usuario com validacao de cursos ativos.

## 9. Checklist

Ha um CRUD de usuarios (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensiveis e Arvores B+ como indices diretos e indiretos conforme necessidade) que funciona corretamente?  
Resposta: Sim. `ArquivoUsuarios` estende `ArquivoIndexado<Usuario>`, usa persistencia em arquivo e mantem indice indireto por e-mail. O indice direto por ID para endereco e mantido na base generica.

Ha um CRUD de cursos (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensiveis e Arvores B+ como indices diretos e indiretos conforme necessidade) que funciona corretamente?  
Resposta: Sim. `ArquivoCursos` estende `ArquivoIndexado<Curso>` e mantem indice por codigo, indice por nome e indice relacional `idUsuario -> idCurso`.

Os cursos estao vinculados aos usuarios usando o idUsuario como chave estrangeira?  
Resposta: Sim.

Ha uma arvore B+ que registre o relacionamento 1:N entre usuarios e cursos?  
Resposta: Sim. O relacionamento e mantido em `dados/usuarioCurso.idx`.

Ha um CRUD de usuarios (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensiveis e Arvores B+ como indices diretos e indiretos conforme necessidade)?  
Resposta: Sim.

O trabalho compila corretamente?  
Resposta: Sim.

O trabalho esta completo e funcionando sem erros de execucao?  
Resposta: Sim para o escopo do TP1. O modulo de inscricoes permanece fora do escopo e esta sinalizado no sistema para o TP2.

O trabalho e original e nao a copia de um trabalho de outro grupo?  
Resposta: Sim.

## 10. Video

Inserir aqui o link do video de demonstracao de ate 3 minutos.
