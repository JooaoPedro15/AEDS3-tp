# TP1 - EntrePares 1.0

## Integrantes

- Jamille Ferreira
- João Pedro
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

### Usuários

- Cadastro de usuário
- Login com email e senha
- Armazenamento de senha utilizando hash
- Busca de usuário por email

### Cursos

- Cadastro de cursos
- Associação automática ao usuário logado
- Geração de código compartilhável
- Listagem de cursos do usuário

### Usuario

- `id`
- `nome`
- `email`
- `hashSenha`
- `perguntaSecreta`
- `hashRespostaSecreta`

### Curso

- entidades → classes Usuario e Curso
- arquivos → simulação de persistência e CRUD
- controle → lógica do sistema
- visao → interação com o usuário
- utils → funções auxiliares
- estruturas → classes fornecidas

Relacao implementada:

## 4. Modelagem de Dados

Usuario (1) -------- (N) Curso

## 4. Persistencia e indices

### CRUD generico

A classe `ArquivoIndexado` foi implementada no modelo apresentado em sala:

- cabecalho com ultimo ID e cabeca da lista de espacos livres;
- registro com lapide, tamanho e vetor de bytes;
- indice direto por hash extensivel apontando de `id` para o endereco do registro no arquivo;
- reaproveitamento de espacos excluidos.

Cada curso possui um atributo `idUsuario`, que representa o dono do curso.

- indice indireto por e-mail com hash extensivel.

Operacoes especiais:

1. Executar a classe `Main.java`
2. Selecionar login ou cadastro
3. Após login, acessar o menu de cursos

### Indices de cursos

A classe `ArquivoCursos` estende `ArquivoIndexado<Curso>` e mantem:

### Índice por email

Foi utilizado um índice em memória (HashMap) para permitir a busca rápida de usuários por email durante o login.

### Relacionamento usuário → cursos

O relacionamento 1:N foi implementado utilizando uma estrutura de associação em memória entre idUsuario e lista de cursos.

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

## 10. Evidências de Execução

#### Cadastro de Usuário

<img src="/public/tela_cadastro.jpg">

#### Login de Usuário

<img src="/public/tela_login.jpg">

#### Exibir dados do Usuário e Alterar dados

<img src="/public/tela_dados_do_usuario_e_alteracao.jpg">

#### Esqueci minha Senha

<img src="/public/tela_esquecer_senha.jpg">

#### Excluir Usuário

<img src="/public/tela_excluir_usuario.jpg">

#### Cadastro de Curso

<img src="/public/tela_cadastro_de_curso.jpg">

#### Exibir dados do Curso

<img src="/public/tela_dados_curso.jpg">

#### Atualizar Curso

<img src="/public/tela_atualizar_curso.jpg">

#### Encerrar inscrições e Excluir Curso

<img src="/public/tela_encerrar_inscricoes_e_deletar_curso.jpg">

## 11. Video

[Link ou Video]
