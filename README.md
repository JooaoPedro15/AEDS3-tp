# TP2 - EntrePares 1.0

## Integrantes

- Jamille Ferreira
- Joao Pedro Costa
- Maria Clara G. Soares

## 1. Descricao do sistema

O sistema EntrePares 1.0 permite:

- cadastrar usuarios;
- autenticar usuarios por e-mail e senha;
- recuperar senha com pergunta secreta;
- cadastrar cursos vinculados ao usuario logado;
- alterar, concluir, encerrar inscricoes ou cancelar cursos;
- buscar cursos de outros usuarios por codigo NanoID;
- listar todos os cursos com paginacao de 10 itens por pagina;
- visualizar os dados completos de um curso antes da inscricao;
- realizar inscricoes em cursos de outros usuarios;
- listar e cancelar as proprias inscricoes;
- gerenciar os inscritos nos cursos do usuario proponente;
- visualizar nome, e-mail e data de inscricao de um usuario inscrito;
- cancelar a inscricao de um usuario em um curso proprio;
- exportar a lista de inscritos em CSV.

Neste TP2, foi implementado o modulo de inscricoes, incluindo o relacionamento N:N entre usuarios e cursos por meio da entidade `CursoUsuario`.

## 2. Organizacao do projeto

O projeto segue o padrao MVC, separando entidades, persistencia, controle e visao.

### Classes principais

### Usuarios

- Cadastro de usuario
- Login com e-mail e senha
- Armazenamento de senha utilizando hash
- Recuperacao de senha por pergunta secreta
- Busca de usuario por e-mail
- Exclusao de usuario com verificacao de cursos ativos e limpeza de inscricoes relacionadas

### Cursos

- Cadastro de cursos
- Associacao automatica ao usuario logado
- Geracao de codigo compartilhavel NanoID
- Busca de curso por codigo
- Listagem dos cursos do usuario ativo
- Listagem geral de cursos com paginacao
- Visualizacao completa dos dados do curso
- Alteracao, encerramento de inscricoes, conclusao e cancelamento de curso
- Gerenciamento dos usuarios inscritos no curso

### Inscricoes

- CRUD da entidade de associacao `CursoUsuario`
- Inscricao de usuario em curso
- Consulta dos cursos em que o usuario esta inscrito
- Consulta dos usuarios inscritos em um curso
- Cancelamento da propria inscricao
- Cancelamento da inscricao de um usuario pelo proponente do curso
- Exportacao da lista de inscritos em CSV

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

### CursoUsuario

- `idCursoUsuario`
- `idCurso`
- `idUsuario`
- `dataInscricao`

### Pacotes

- `entidades` -> classes `Usuario`, `Curso`, `CursoUsuario` e interface `Registro`
- `arquivos` -> classes de CRUD e persistencia
- `controle` -> logica do sistema e menus
- `visao` -> interacao com o usuario
- `utils` -> funcoes auxiliares
- `estruturas` -> classes de hash extensivel, arvore B+ e arquivo indexado

## 3. Modelagem de Dados

Relacionamentos implementados:

```text
Usuario (1) -------- (N) Curso
Usuario (N) -------- (N) Curso
```

O relacionamento 1:N entre `Usuario` e `Curso` indica que um usuario pode criar varios cursos.

O relacionamento N:N entre `Usuario` e `Curso` indica que um usuario pode se inscrever em varios cursos e que um curso pode possuir varios usuarios inscritos.

Para implementar o relacionamento N:N, foi criada a entidade de associacao `CursoUsuario`.

## 4. Persistencia e indices

### CRUD generico

A classe `ArquivoIndexado` foi implementada no modelo apresentado em sala:

- cabecalho com ultimo ID e cabeca da lista de espacos livres;
- registro com lapide, tamanho e vetor de bytes;
- indice direto por hash extensivel apontando de `id` para o endereco do registro no arquivo;
- reaproveitamento de espacos excluidos.

### Indices de usuarios

A classe `ArquivoUsuarios` estende `ArquivoIndexado<Usuario>` e mantem:

- indice direto por ID;
- indice indireto por e-mail com hash extensivel.

### Indices de cursos

A classe `ArquivoCursos` estende `ArquivoIndexado<Curso>` e mantem:

- indice direto por ID;
- indice por codigo NanoID;
- indice por nome;
- indice relacional `idUsuario -> idCurso`.

### Indices de inscricoes

A classe `ArquivoCursoUsuario` estende `ArquivoIndexado<CursoUsuario>` e mantem:

- indice direto por ID da inscricao;
- arvore B+ com os pares `(idCurso, idCursoUsuario)`;
- arvore B+ com os pares `(idUsuario, idCursoUsuario)`.

Essas duas arvores B+ permitem recuperar eficientemente:

- todos os usuarios inscritos em um curso;
- todos os cursos em que um usuario esta inscrito.

## 5. Menus implementados

### Tela inicial

- login;
- novo usuario;
- recuperacao de senha;
- sair.

### Menu principal do usuario logado

- meus dados;
- meus cursos;
- minhas inscricoes;
- sair.

### Menu de cursos

- novo curso;
- visualizar curso selecionado;
- gerenciar inscritos no curso;
- corrigir dados do curso;
- encerrar inscricoes;
- concluir curso;
- cancelar curso;
- retornar ao menu anterior.

### Menu de inscricoes

- listagem das inscricoes do usuario logado;
- busca de curso por codigo;
- busca por palavras-chave usando indice invertido (TP3);
- listagem de todos os cursos;
- visualizacao completa do curso;
- realizacao de inscricao;
- cancelamento da propria inscricao;
- retorno ao menu anterior.

### Menu de inscritos no curso

- listagem dos usuarios inscritos;
- visualizacao dos dados de um inscrito;
- cancelamento da inscricao de um usuario;
- exportacao da lista em CSV;
- retorno ao menu anterior.

## 6. Regras de negocio implementadas

- o login e feito por e-mail e senha com comparacao por hash;
- a resposta secreta tambem e armazenada em hash;
- um e-mail nao pode ser reutilizado por outro usuario;
- um codigo compartilhavel nao pode ser reutilizado por outro curso;
- todo curso novo recebe automaticamente o `idUsuario` do usuario logado;
- usuarios com cursos ativos nao podem ser excluidos;
- ao excluir um usuario, suas inscricoes sao removidas;
- ao excluir cursos inativos de um usuario, as inscricoes relacionadas tambem sao removidas;
- um usuario nao pode se inscrever no proprio curso;
- um usuario nao pode se inscrever duas vezes no mesmo curso;
- somente cursos abertos aceitam novas inscricoes;
- cursos com inscricoes nao sao excluidos diretamente, mas marcados como cancelados;
- cursos sem inscritos podem ser excluidos;
- o usuario pode cancelar a propria inscricao;
- o proponente do curso pode cancelar a inscricao de um usuario;
- a lista de inscritos pode ser exportada em CSV.

## 7. Compilacao e execucao

Exemplo de compilacao:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
```

Exemplo de execucao:

```powershell
java -cp out Main
```

## 8. Evidencias que devem aparecer no video

- cadastro de usuario;
- login;
- criacao de curso;
- visualizacao do codigo NanoID do curso;
- busca de curso por codigo;
- listagem de todos os cursos com paginacao;
- visualizacao completa dos dados do curso;
- inscricao de um usuario em um curso;
- listagem das proprias inscricoes;
- cancelamento da propria inscricao;
- gerenciamento de inscritos pelo proponente do curso;
- visualizacao dos dados de um usuario inscrito;
- cancelamento da inscricao de um usuario pelo proponente;
- exportacao da lista de inscritos em CSV.

## 9. Checklist

Ha um CRUD da entidade de associacao CursoUsuario (que estende a classe ArquivoIndexado, acrescentando Tabelas Hash Extensiveis e Arvores B+ como indices diretos e indiretos conforme necessidade) que funciona corretamente?  
Resposta: Sim. `ArquivoCursoUsuario` estende `ArquivoIndexado<CursoUsuario>`, usa o indice direto herdado da classe base e mantem duas arvores B+: uma para `(idCurso, idCursoUsuario)` e outra para `(idUsuario, idCursoUsuario)`.

A visao de inscricoes esta corretamente implementada e permite consultas aos cursos em que um usuario esta inscrito?  
Resposta: Sim. O menu "Minhas inscricoes" mostra as inscricoes do usuario logado, permite abrir os dados completos do curso e cancelar a inscricao.

A visao de cursos funciona corretamente e permite a gestao dos usuarios inscritos em um curso?  
Resposta: Sim. No menu "Meus cursos", a opcao "Gerenciar inscritos no curso" lista os inscritos, permite visualizar dados do usuario, cancelar inscricoes e exportar CSV.

Ha uma visualizacao dos cursos de outras pessoas por meio de um codigo NanoID?  
Resposta: Sim. A busca por codigo localiza o curso pelo NanoID e abre diretamente a tela de dados completos do curso.

A integridade do relacionamento entre cursos e usuarios esta mantida em todas as operacoes?  
Resposta: Sim. O sistema impede inscricoes duplicadas, impede inscricao no proprio curso, remove associacoes no cancelamento de inscricoes e limpa inscricoes relacionadas quando usuarios ou cursos sao removidos.

O trabalho compila corretamente?  
Resposta: Sim.

O trabalho esta completo e funcionando sem erros de execucao?  
Resposta: Sim para o escopo do TP2. A busca por palavras-chave permanece para o TP3, conforme o enunciado.

O trabalho e original e nao a copia de um trabalho de outro grupo?  
Resposta: Sim.

## 10. Evidencias de Execucao

As imagens abaixo registram operacoes ja demonstradas no sistema

#### Cadastro de Usuario

<img src="/public/tela_cadastro.jpg">

#### Login de Usuario

<img src="/public/tela_login.jpg">

#### Exibir dados do Usuario e Alterar dados

<img src="/public/tela_dados_do_usuario_e_alteracao.jpg">

#### Esqueci minha Senha

<img src="/public/tela_esquecer_senha.jpg">

#### Excluir Usuario

<img src="/public/tela_excluir_usuario.jpg">

#### Cadastro de Curso

<img src="/public/tela_cadastro_de_curso.jpg">

#### Exibir dados do Curso

<img src="/public/tela_dados_curso.jpg">

#### Atualizar Curso

<img src="/public/tela_atualizar_curso.jpg">

#### Encerrar inscricoes e Excluir Curso

<img src="/public/tela_encerrar_inscricoes_e_deletar_curso.jpg">

#### Telas de inscricao

<img src="/public/tela_incricao.jpg"> 

#### Busca por codigo

<img src="/public/busca_por_codigo.jpg">

#### Gerenciamento de inscritos

<img src="/public/gerenciamento_inscritos.jpg">

#### Exportacao CSV

<img src="/public/csv.jpg">

## 11. Video

[Assistir video](./video/tp2.mp4)

---

# TP3 - Indice Invertido (busca de cursos por palavras-chave)

Neste TP3 o sistema passou a permitir buscar cursos **por palavras do nome**,
alem da busca por codigo/id que ja existia. A busca usa um **indice invertido**
construido sobre os nomes dos cursos, com pontuacao **TF*IDF** para ordenar os
resultados por relevancia.

## 1. Classes criadas e alteradas

### Codigo do professor (reaproveitado)

A estrutura principal do indice e a **`ListaInvertida` do professor**, copiada
para o projeto **sem alterar o algoritmo**:

- `src/aed3/ListaInvertida.java` - lista invertida em arquivo (dicionario de
  termos + blocos encadeados). Mantida igual ao ZIP, com **uma unica adicao
  minima**: o metodo `close()`, necessario para liberar os arquivos ao
  reconstruir/remover o indice (a classe original nao fechava os arquivos).
- `src/aed3/ElementoLista.java` - par `(id do curso, frequencia)`. Aqui o campo
  `frequencia` (float) guarda o **TF** do termo naquele curso. Copiada sem
  alteracoes.

Essas classes ficam no pacote `aed3`, junto com as outras estruturas do
professor ja usadas no projeto (`ArvoreBMais`, `HashExtensivel`).

### Classes novas (pacote `indice`)

- `src/indice/TratadorDeTermos.java` - trata o texto: coloca em minusculas,
  remove acentos, quebra em palavras e remove as stop words em portugues.
- `src/indice/IndiceInvertidoCursos.java` - usa a `ListaInvertida` como
  estrutura principal. Indexa os nomes dos cursos, calcula TF e IDF e faz a
  busca por palavras-chave.
- `src/indice/ResultadoBuscaCurso.java` - representa um curso encontrado na
  busca, com a pontuacao (`score`) usada para ordenar.

### Classes integradas (alteradas)

- `src/arquivos/ArquivoCursos.java` - passou a manter o indice invertido junto
  com os outros indices. Atualiza o indice automaticamente em
  `create`, `update` e `delete`, e oferece o metodo `buscarPorPalavras`.
- `src/controle/ControleCursoUsuario.java` - a opcao **"(B) Buscar curso por
  palavras-chave"** do menu de inscricoes (antes apenas um aviso) agora chama a
  busca real e mostra os resultados ordenados.

### Teste

- `src/TesteIndiceInvertido.java` - teste automatizado com os 4 cursos do
  enunciado, validando normalizacao, stop words, TF, IDF, ordenacao e a
  atualizacao do indice.

## 2. Como a `ListaInvertida` do professor foi usada

A `ListaInvertida` e a **estrutura principal em arquivo** (nao foi usado nenhum
`HashMap`/`ArrayList` em memoria como substituto do indice). Para cada termo do
nome de um curso e gravada uma entrada:

```
termo  ->  (idDoCurso, TF)
```

Os metodos do professor usados pela classe `IndiceInvertidoCursos`:

- `create(String termo, ElementoLista e)` - insere `(idCurso, TF)` no termo;
- `read(String termo)` - retorna todos os `(idCurso, TF)` daquele termo (usado
  na busca e para descobrir em quantos cursos o termo aparece);
- `delete(String termo, int idCurso)` - remove um curso de um termo;
- `incrementaEntidades()` / `decrementaEntidades()` / `numeroEntidades()` -
  controlam o total de cursos indexados, usado no calculo do IDF.

Os arquivos do indice sao separados dos arquivos de exemplo do ZIP:

- `dados/cursos.dicionario.listainv.db`
- `dados/cursos.blocos.listainv.db`

## 3. Tratamento dos termos

Feito por `TratadorDeTermos`, igual na indexacao e na busca:

1. remove acentos (`Inteligencia`/`Inteligencia` ficam iguais);
2. converte para minusculas;
3. quebra o texto em palavras (qualquer caractere que nao seja letra separa);
4. remove as stop words em portugues:
   `a, o, os, as, um, uma, uns, umas, de, da, do, das, dos, em, no, na, nos,
   nas, para, por, com, e, ou, ao, aos, a, as`.

Exemplo:

```
"Introducao a Inteligencia Artificial"  ->  [introducao, inteligencia, artificial]
```

## 4. Calculo do TF e do IDF

**TF (na indexacao)** - para cada curso, depois de remover as stop words:

```
TF = vezes que o termo aparece no nome / total de termos validos do nome
```

Exemplo: `"Inteligencia no Trabalho por Meio da Inteligencia Artificial"`
-> `[inteligencia, trabalho, meio, inteligencia, artificial]`.
O termo `inteligencia` aparece 2 vezes em 5 termos, entao `TF = 2/5 = 0.4`.

**IDF (na busca)** - calculado na hora da busca, para cada termo:

```
IDF = log10(totalDeCursos / quantidadeDeCursosComEsseTermo) + 1
```

- `totalDeCursos` = `numeroEntidades()` da lista invertida;
- `quantidadeDeCursosComEsseTermo` = tamanho da lista retornada por `read(termo)`.

## 5. Como a busca ordena os resultados

1. a consulta passa pelo mesmo tratamento (minusculas, sem acento, sem stop words);
2. cada termo e buscado na `ListaInvertida`;
3. para cada curso encontrado: `scoreDoTermo = TF * IDF`;
4. se o mesmo curso aparece em mais de um termo, os scores sao **somados**;
5. os cursos sao ordenados por **score decrescente** (mais relevante primeiro).

Exemplo real do teste automatizado para a busca `"Inteligencia Artificial"`
(4 cursos do enunciado):

```
1) ID 1  score=0.8087  -  Introducao a Inteligencia Artificial
2) ID 3  score=0.7102  -  Inteligencia no Trabalho por Meio da Inteligencia Artificial
3) ID 2  score=0.3750  -  Inteligencia Emocional para Gestores
```

O curso ID 4 ("Introducao a Gestao de Equipes") **nao aparece**, pois nao possui
nenhum dos termos buscados.

## 6. Atualizacao automatica do indice

O indice e mantido em duas frentes:

- **Reconstrucao na inicializacao**: ao abrir o programa, `ArquivoCursos` apaga
  os arquivos do indice e reconstroi tudo a partir dos cursos cadastrados. Assim
  o indice nunca fica inconsistente entre execucoes.
- **Atualizacao incremental durante o uso**, dentro de `ArquivoCursos`:
  - **inclusao** (`create`) -> `indiceInvertido.inserir(curso)`;
  - **exclusao** (`delete`) -> `indiceInvertido.remover(curso)`;
  - **alteracao de nome** (`update`) -> `indiceInvertido.atualizar(antigo, novo)`
    (remove os termos antigos e insere os novos).

## 7. Compilacao e execucao do TP3

Compilar (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
```

Executar o sistema:

```powershell
java -cp out Main
```

Executar o teste automatizado do indice invertido:

```powershell
java -cp out TesteIndiceInvertido
```

## 8. Roteiro de teste da busca por palavras-chave (no menu)

1. faca login com um usuario;
2. cadastre (em "Meus cursos") cursos com nomes que compartilhem palavras,
   por exemplo "Introducao a Inteligencia Artificial" e
   "Inteligencia Emocional para Gestores";
3. va em "Minhas inscricoes" -> "(B) Buscar curso por palavras-chave";
4. digite `Inteligencia Artificial`;
5. confira a lista ordenada por relevancia;
6. confirme que a busca por codigo ("(A)") continua funcionando.

## 9. Checklist do TP3

1. O indice invertido com os termos dos nomes dos cursos foi criado usando a
   classe `ListaInvertida`?
   **Sim.** A `ListaInvertida` do professor (pacote `aed3`) e a estrutura
   principal, em arquivo, usada por `IndiceInvertidoCursos`.

2. E possivel buscar cursos por palavras no menu de inscricao/busca?
   **Sim.** Em "Minhas inscricoes", a opcao "(B) Buscar curso por
   palavras-chave" faz a busca por TF*IDF. A busca por codigo continua
   disponivel na opcao "(A)".

3. O trabalho compila corretamente?
   **Sim.** Compila com `javac` sem erros nem avisos.

4. O trabalho esta completo e funcionando sem erros de execucao?
   **Sim.** O sistema executa normalmente e o teste automatizado
   (`TesteIndiceInvertido`) passa em todas as verificacoes.

5. O trabalho e original e nao copia de outro grupo?
   **Sim.** Apenas a `ListaInvertida`/`ElementoLista` sao do professor (uso
   previsto no enunciado); o restante foi desenvolvido pelo grupo.
