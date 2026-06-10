# TP3 - EntrePares 1.0

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
- buscar cursos por palavras-chave do nome, com resultados ordenados por relevancia (TF-IDF), usando indice invertido;
- listar todos os cursos com paginacao de 10 itens por pagina;
- visualizar os dados completos de um curso antes da inscricao;
- realizar inscricoes em cursos de outros usuarios;
- listar e cancelar as proprias inscricoes;
- gerenciar os inscritos nos cursos do usuario proponente;
- visualizar nome, e-mail e data de inscricao de um usuario inscrito;
- cancelar a inscricao de um usuario em um curso proprio;
- exportar a lista de inscritos em CSV.

Neste TP3, foi implementado o indice invertido dos nomes dos cursos, permitindo a busca de cursos por palavras-chave. A estrutura principal do indice e a classe `ListaInvertida`, gravada em arquivo, fornecida pelo professor e reaproveitada no projeto. A busca por codigo NanoID, ja existente, continua funcionando.

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
- Busca de curso por palavras-chave do nome
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

### Busca por palavras-chave (TP3)

- Indexacao dos termos do nome de cada curso na `ListaInvertida`
- Tratamento dos termos: minusculas, remocao de acentos e remocao de stop words em portugues
- Calculo de TF na indexacao e de IDF no momento da busca
- Pontuacao de cada curso pela soma de TF*IDF dos termos da consulta
- Ordenacao dos resultados por relevancia (maior pontuacao primeiro)
- Atualizacao automatica do indice na inclusao, alteracao e exclusao de cursos

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

### ElementoLista (indice invertido)

- `id` -> id do curso
- `frequencia` -> TF do termo naquele curso

### Pacotes

- `entidades` -> classes `Usuario`, `Curso`, `CursoUsuario` e interface `Registro`
- `arquivos` -> classes de CRUD e persistencia
- `controle` -> logica do sistema e menus
- `visao` -> interacao com o usuario
- `utils` -> funcoes auxiliares
- `estruturas` -> classes de hash extensivel, arvore B+ e arquivo indexado
- `aed3` -> estruturas fornecidas pelo professor (`ListaInvertida`, `ElementoLista`, `ArvoreBMais`, `HashExtensivel`)
- `indice` -> classes do indice invertido dos cursos (`IndiceInvertidoCursos`, `TratadorDeTermos`, `ResultadoBuscaCurso`)

## 3. Modelagem de Dados

Relacionamentos implementados:

```text
Usuario (1) -------- (N) Curso
Usuario (N) -------- (N) Curso
Termo   (1) -------- (N) Curso   (indice invertido)
```

O relacionamento 1:N entre `Usuario` e `Curso` indica que um usuario pode criar varios cursos.

O relacionamento N:N entre `Usuario` e `Curso` indica que um usuario pode se inscrever em varios cursos e que um curso pode possuir varios usuarios inscritos.

Para implementar o relacionamento N:N, foi criada a entidade de associacao `CursoUsuario`.

No indice invertido, cada termo do nome de um curso aponta para a lista de cursos em que ele aparece, junto com a frequencia (TF) do termo em cada nome.

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
- indice relacional `idUsuario -> idCurso`;
- indice invertido dos termos do nome (TP3).

### Indices de inscricoes

A classe `ArquivoCursoUsuario` estende `ArquivoIndexado<CursoUsuario>` e mantem:

- indice direto por ID da inscricao;
- arvore B+ com os pares `(idCurso, idCursoUsuario)`;
- arvore B+ com os pares `(idUsuario, idCursoUsuario)`.

Essas duas arvores B+ permitem recuperar eficientemente:

- todos os usuarios inscritos em um curso;
- todos os cursos em que um usuario esta inscrito.

### Indice invertido dos nomes dos cursos (TP3)

A classe `IndiceInvertidoCursos` usa a `ListaInvertida` do professor (pacote `aed3`) como estrutura principal, gravada em arquivo, em dois arquivos proprios:

- `dados/cursos.dicionario.listainv.db` -> dicionario de termos com o numero de cursos indexados;
- `dados/cursos.blocos.listainv.db` -> blocos encadeados com os pares `(idCurso, TF)`.

Para cada termo do nome de um curso e gravado um `ElementoLista (idCurso, TF)`, em que TF e a frequencia do termo naquele nome. Os termos sao tratados por `TratadorDeTermos` (minusculas, sem acento e sem stop words).

Calculos:

- **TF (na indexacao):** `vezes que o termo aparece no nome / total de termos validos do nome`.
- **IDF (na busca):** `log10(totalDeCursos / quantidadeDeCursosComEsseTermo) + 1`, onde `totalDeCursos` vem de `numeroEntidades()` e `quantidadeDeCursosComEsseTermo` e o tamanho da lista do termo.
- **Relevancia:** soma de `TF * IDF` de cada termo da consulta; o mesmo curso em termos diferentes tem os scores somados.

O indice e reconstruido a partir dos cursos cadastrados na inicializacao e atualizado de forma incremental quando um curso e incluido, alterado ou excluido.

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
- busca de curso por palavras-chave usando indice invertido, com resultados ordenados por relevancia;
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
- a lista de inscritos pode ser exportada em CSV;
- na indexacao e na busca, os termos do nome sao normalizados (minusculas e sem acento) e as stop words em portugues sao removidas;
- o indice invertido e atualizado automaticamente sempre que um curso e incluido, alterado ou excluido.

## 7. Compilacao e execucao

Exemplo de compilacao:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
```

Exemplo de execucao:

```powershell
java -cp out Main
```

Execucao do teste automatizado do indice invertido (cursos do enunciado):

```powershell
java -cp out TesteIndiceInvertido
```

## 8. Evidencias que devem aparecer no video

- login;
- criacao de curso, mostrando que o indice invertido e atualizado;
- busca de curso por codigo NanoID (continua funcionando);
- busca de curso por palavras-chave, com os resultados ordenados por relevancia (TF-IDF);
- demonstracao de que um curso sem nenhum dos termos buscados nao aparece nos resultados;
- inscricao em um curso a partir do resultado da busca;
- execucao do teste automatizado do indice invertido.

## 9. Checklist

O indice invertido com os termos dos nomes dos cursos foi criado usando a classe ListaInvertida?  
Resposta: Sim. A `ListaInvertida` do professor (pacote `aed3`) e a estrutura principal, gravada em arquivo, usada pela classe `IndiceInvertidoCursos` para armazenar, para cada termo, a lista de cursos com a frequencia TF correspondente.

E possivel buscar cursos por palavras no menu de inscricao/busca?  
Resposta: Sim. No menu "Minhas inscricoes", a opcao "Buscar curso por palavras-chave" aplica o mesmo tratamento de termos (minusculas, sem acento, sem stop words), calcula TF*IDF e mostra os cursos ordenados por relevancia. A busca por codigo continua disponivel.

O trabalho compila corretamente?  
Resposta: Sim. Compila com `javac` sem erros nem avisos.

O trabalho esta completo e funcionando sem erros de execucao?  
Resposta: Sim. O sistema executa normalmente e o teste automatizado (`TesteIndiceInvertido`) passa em todas as verificacoes, reproduzindo o exemplo do enunciado.

O trabalho e original e nao a copia de um trabalho de outro grupo?  
Resposta: Sim. Apenas as classes `ListaInvertida` e `ElementoLista` sao do professor, conforme previsto no enunciado; o restante foi desenvolvido pelo grupo.

## 10. Video

[Assistir video](./video/tp3.mp4)
