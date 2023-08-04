# Guião de Demonstração

## Demonstração da primeira parte:

## 1. Preparação do Sistema

Para testar a aplicação e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Executar o servidor do ZooKeeper

Nesta versão do projeto é necessário executar primeiro o ZooKeeper que é onde o servidor irá fazer bind.
Para isso, terá de se ir à diretoria apache-zookeeper-3.6.0-bin/bin/ e executar o seguinte comando:

Se for em uma maquina Windows:

```
$ ./zkServer.cmd
```

Se for em uma maquina Linux:

```
$ ./zkServer.sh
```

Seguidamente o servidor do ZooKeeper irá inicializar e poderemos seguir para os passos seguintes

### 1.2. Compilar o Projeto

Agora, é necessário instalar as dependências necessárias para o *silo* e os clientes (*eye* e *spotter*) e compilar estes componentes.
Para isso, basta ir à diretoria *root* do projeto e correr o seguinte comando:

```
$ mvn clean install -DskipTests
```

Com este comando já é possível analisar se o projeto compila na íntegra.

### 1.3. *Silo-Server*

Para proceder aos testes, é preciso o servidor *silo* estar a correr. 
Para isso basta ir à diretoria *silo-server* e executar:

```
$ mvn exec:java
```

Este comando vai colocar o *silo* no endereço *localhost* e na porta *8081*, fazendo bind com o ZooKeeper e será acedido pelos clientes através da porta 2181.

### 1.4. *Silo-Client*

Para executar os testes de integração, é preciso ir à diretoria *silo-client* e executar:

```
$ mvn verify
```

Este comando irá validar os testes de integração, poderá demorar algum tempo.

### 1.5. *Spotter*

Para inicialização adidional é necessario correr o comando init para isso precisamos instanciar um cliente *spotter*, presente na diretoria com o mesmo nome e executar:

```
$ ./target/appassembler/bin/spotter localhost 2181 1
```

De seguida irá aparecer uma mensagem a informar que o cliente se conectou com a replica 1. Posteriormente a isto o programa irá ficar à espera de comandos infinitamente.

1.4.1. Execução do comando *init*

Agora podemos executar o comando init:

```
> init 1
Parameters initialized on this server
```

Este comando irá definir a 1 o máximo de replicas que irão existir durante a execução e irá inicializar o timestamp da Replica e o do frontend (com todas as entradas a 0).
O primeiro cliente *spotter* a conectar-se com a replica deverá sempre executar este comando com o terceiro argumento (considerando que o init é o primeiro argumento) igual a 1(neste caso o valor por omissão é igual a 1)
Deverá devolver uma mensagem como a seguinte: "Parameters initialized on this server" 

### 1.6. *Eye*

Vamos registar 3 câmeras e as respetivas observações. 
Cada câmera vai ter o seu ficheiro de entrada próprio com observações já definidas.
Para isso basta ir à diretoria *eye* e correr os seguintes comandos:

```
$ ./target/appassembler/bin/eye localhost 2181 Tagus 38.737613 -9.303164 1 < ../demo/eye1.txt
$ ./target/appassembler/bin/eye localhost 2181 Alameda 30.303164 -10.737613 1 < ../demo/eye2.txt
$ ./target/appassembler/bin/eye localhost 2181 Lisboa 32.737613 -15.303164 1 < ../demo/eye3.txt
```
Depois de executar os comandos acima já temos o que é necessário para testar o sistema. 

## 2. Teste das Operações

Nesta secção vamos correr os comandos necessários para testar todas as operações. 
Cada subsecção é respetiva a cada operação presente no *silo*.

### 2.1. *cam_join*

Esta operação já foi testada na preparação do ambiente, no entanto ainda é necessário testar algumas restrições.

2.1.1. Teste das câmeras com nome duplicado e coordenadas diferentes.  
O servidor deve rejeitar esta operação. 
Para isso basta executar um *eye* com o seguinte comando:

```
$ ./target/appassembler/bin/eye localhost 2181 Tagus 10.0 10.0 1
```

2.1.2. Teste do tamanho do nome.  
O servidor deve rejeitar esta operação. 
Para isso basta executar um *eye* com o seguinte comando:

```
$ ./target/appassembler/bin/eye localhost 2181 ab 10.0 10.0 1
$ ./target/appassembler/bin/eye localhost 2181 abcdefghijklmnop 10.0 10.0 1
```

### 2.2. *cam_info*

Esta operação não tem nenhum comando específico associado e para isso é necessário ver qual o nome do comando associado a esta operação. 
Para isso basta executar os seguintes comandos demonstrados em baixo com a sessão do cliente *spotter* inicializado posteriormente (passo 1.4).

Corremos o comando *help* e executa-se o comando *info* recebendo um nome:

2.2.1. Execução do comando *help*.
```
> help
```

Este comando irá devolver uma lista com uma descrição de comandos possiveis e os seus respetivos argumentos.

2.2.1. Teste para uma câmera existente.  
O servidor deve responder com as coordenadas de localização da câmera *Tagus* (38.737613 -9.303164):

```
> info Tagus
*Tagus* (38.737613 -9.303164)
```

2.2.2. Teste para câmera inexistente.  
O servidor deve rejeitar esta operação e terminar a sessão do cliente *spotter*:

```
> info Inexistente
```

### 2.3. *report*

Esta operação já foi testada acima na preparação do ambiente.

No entanto falta testar o sucesso do comando *zzz*. 
Na preparação foi adicionada informação que permite testar este comando.
Para testar basta abrir uma nova sessão do cliente *spotter* na diretoria com o mesmo nome da seguinte forma:

```
$ ./target/appassembler/bin/spotter localhost 2181 1
```

Apos a sessão ter sido inicializada e aparecer a mensagem de "Connected to Replica 1" teremos de realizar novamente o comando *init*, como demonstrado em baixo:

```
> init 1 0
```

O terceiro argumento deste comando (o valor 0) trata-se de uma flag que informa se é necessário voltar a inicializar o timestamp da replica, caso seja 1 irá inicializar(como no passo 1.4.1.), caso seja 0 não será necessário voltar a inicializar o timestamp.
Como este cliente *spotter* é o segundo cliente a ligar-se à replica 1, não é necessário voltar a inicializar o timestamp da replica.

Após esta inicialização já é possivel realizar o comando: 


```
> trail car 00AA00
```

O resultado desta operação deve ser duas observações pela câmera *Tagus* com intervalo de mais ou menos 5 segundos.

### 2.4. *track*

Esta operação vai ser testada utilizando o comando *spot* com um identificador.

2.4.1. Teste com uma pessoa (deve devolver vazio):

```
> spot person 14388236
```

2.4.2. Teste com uma pessoa:

```
> spot person 123456789
person,123456789,<timestamp>,Alameda,30.303164,-10.737613
```

2.4.3. Teste com um carro:

```
> spot car 20SD21
car,20SD21,<timestamp>,Alameda,30.303164,-10.737613
```

### 2.5. *trackMatch*

Esta operação vai ser testada utilizando o comando *spot* com um fragmento de identificador.

2.5.1. Teste com uma pessoa (deve devolver vazio):

```
> spot person 143882*
```

2.5.2. Testes com uma pessoa:

```
> spot person 111*
person,111111000,<timestamp>,Tagus,38.737613,-9.303164

> spot person *000
person,111111000,<timestamp>,Tagus,38.737613,-9.303164

> spot person 111*000
person,111111000,<timestamp>,Tagus,38.737613,-9.303164
```

2.5.3. Testes com duas ou mais pessoas:

```
> spot person 123*
person,123111789,<timestamp>,Alameda,30.303164,-10.737613
person,123222789,<timestamp>,Alameda,30.303164,-10.737613
person,123456789,<timestamp>,Alameda,30.303164,-10.737613

> spot person *789
person,123111789,<timestamp>,Alameda,30.303164,-10.737613
person,123222789,<timestamp>,Alameda,30.303164,-10.737613
person,123456789,<timestamp>,Alameda,30.303164,-10.737613

> spot person 123*789
person,123111789,<timestamp>,Alameda,30.303164,-10.737613
person,123222789,<timestamp>,Alameda,30.303164,-10.737613
person,123456789,<timestamp>,Alameda,30.303164,-10.737613
```

2.5.4. Testes com um carro:

```
> spot car 00A*
car,00AA00,<timestamp>,Tagus,38.737613,-9.303164

> spot car *A00
car,00AA00,<timestamp>,Tagus,38.737613,-9.303164

> spot car 00*00
car,00AA00,<timestamp>,Tagus,38.737613,-9.303164
```

2.5.5. Testes com dois ou mais carros:

```
> spot car 20SD*
car,20SD20,<timestamp>,Alameda,30.303164,-10.737613
car,20SD21,<timestamp>,Alameda,30.303164,-10.737613
car,20SD22,<timestamp>,Alameda,30.303164,-10.737613

> spot car *XY20
car,66XY20,<timestamp>,Lisboa,32.737613,-15.303164
car,67XY20,<timestamp>,Alameda,30.303164,-10.737613
car,68XY20,<timestamp>,Tagus,38.737613,-9.303164

> spot car 19SD*9
car,19SD19,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD29,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD39,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD49,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD59,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD69,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD79,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD89,<timestamp>,Lisboa,32.737613,-15.303164
car,19SD99,<timestamp>,Lisboa,32.737613,-15.303164
```

### 2.6. *trace*

Esta operação vai ser testada utilizando o comando *trail* com um identificador.

2.6.1. Teste com uma pessoa (deve devolver vazio):

```
> trail person 14388236
```

2.6.2. Teste com uma pessoa:

```
> trail person 123456789
person,123456789,<timestamp>,Alameda,30.303164,-10.737613
person,123456789,<timestamp>,Alameda,30.303164,-10.737613
person,123456789,<timestamp>,Tagus,38.737613,-9.303164

```

2.6.3. Teste com um carro (deve devolver vazio):

```
> trail car 12XD34
```

2.6.4. Teste com um carro:

```
> trail car 00AA00
car,00AA00,<timestamp>,Tagus,38.737613,-9.303164
car,00AA00,<timestamp>,Tagus,38.737613,-9.303164
```

## 3. Finalização da primeira demonstração

Para finalizar esta demonstração iremos terminar a sessão do cliente *spotter* e fechar o servidor, para isso basta executar os comandos seguintes:

3.1. Execução do comando *quit*

Na sessão do cliente (iniciado em 2.3.) basta executar este comando da seguinte forma:
```
> quit
```
Na sessão do servidor (iniciado em 1.3.) basta clicar na tecla "Enter" para o fechar, este comando poderá demorar algum tempo.

Correr o comando mvn clean na diretoria *root* do projeto:

```
$ mvn clean
```

Por fim fechar a sessão do ZooKeeper (inizializada em 1.1.) com o comando de ctrl-C.

Assim termina a demonstração da primeira parte.

## Demonstração da segunda parte:

## 4. Preparação do Sistema

### 4.1. Executar o servidor do ZooKeeper

Nesta versão do projeto é necessário executar primeiro o ZooKeeper que é onde o servidor irá fazer bind.
Para isso, terá de se ir à diretoria apache-zookeeper-3.6.0-bin/bin/ e executar o seguinte comando:

Se for em uma maquina Windows:

```
$ ./zkServer.cmd
```

Se for em uma maquina Linux:

```
$ ./zkServer.sh
```

Seguidamente o servidor do ZooKeeper irá inicializar e poderemos seguir para os passos seguintes 

### 4.2 Compilar o Projeto

É novamente necessário instalar as dependências necessárias para o *silo* e os clientes (*eye* e *spotter*) e compilar estes componentes.
Para isso, basta ir à diretoria *root* do projeto e correr o seguinte comando:

```
$ mvn install -DskipTests
```

## 5. Protocolo de replicação

Agora iremos lançar 2 replicas do servidor.
Para isso basta aceder à diretoria do *silo-server* e lançar as replicas da seguinte forma:

5.1 Lançamento da primeira replica:

```
$ mvn exec:java
```

Este comando vai lançar a primeira replica do *silo* no endereço *localhost* e na porta *8081*, fazendo bind com o ZooKeeper e será acedido pelos clientes através da porta 2181.

5.2 Lançamento da segunda replica:

```
$ mvn exec:java -Dinstance=2
```

Este comando vai lançar a segunda replica do *silo* no endereço *localhost* e na porta *8082*, fazendo bind com o ZooKeeper e será acedido pelos clientes através da porta 2182.

### 5.3. *Spotter*

Agora iremos conectar 3 clientes *spotter* com as replicas.
Para isto temos de correr os seguintes comandos na diretoria *spotter*:

5.3.1 Lançamento do primeiro *spotter*

Com este comando vamos ligar o cliente à replica 1.

```
$ ./target/appassembler/bin/spotter localhost 2181 1
```

Após a receção da mensagem "Connected to Replica: 1" vamos correr o seguinte comando:

```
> init 2
Parameters initialized on this server
```

Com este comando estamos a establecer um maximo de 2 replicas e a inicializar o timestamp da replica (o valor por omissão do terceiro argumento é 1).


5.3.2 Lançamento do segundo *spotter*

Com este comando vamos ligar o cliente à replica 2.

```
$ ./target/appassembler/bin/spotter localhost 2181 2
```

Após a receção da mensagem "Connected to Replica: 2" vamos correr o seguinte comando:


```
> init 2
Parameters initialized on this server
```

5.3.3 Lançamento do terceiro *spotter*

Como não é atribuido o numero de replica, o Zookeeper irá ligar o cliente a uma das replicas disponiveis.

```
$ ./target/appassembler/bin/spotter localhost 2181
```

Após a receção da mensagem "Connected to Replica: n" (n = (1|2)) vamos correr o seguinte comando de modo a fechar fechar o terceiro cliente *spotter*:


```
> quit
```

### 5.4. *Eye*

Iremos agora adicionar observações à replica 1 e à replica 2.
Para isto teremos de aceder à diretoria do eye e correr os seguintes comandos:

```
$ ./target/appassembler/bin/eye localhost 2181 Tagus 38.737613 -9.303164 1 < ../demo/eye4.txt
$ ./target/appassembler/bin/eye localhost 2181 Alameda 30.303164 -10.737613 2 < ../demo/eye5.txt
$ ./target/appassembler/bin/eye localhost 2181 Tagus 38.737613 -9.303164 1 < ../demo/eye5.txt
```

De seguida iremos confirmar que as observações foram registadas nas respetivas replicas com os seguintes comandos:


No primeiro cliente *spotter* (inicializado no passo 5.3.1):

```
> spot person 123
person,123,<timestamp>,Tagus,38.737613,-9.303164
```

```
> trail car 20SD24
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
```

No segundo cliente *spotter* (inicializado no passo 5.3.2.):
```
> spot person 123
person,123,<timestamp>,Alameda,30.303164,-10.737613
```

```
> trail car 20SD24
car,20SD24,<timestamp>,Alameda,30.303164,-10.737613
```

```
> quit
```

## 6. Modelo de tolerancia a faltas

Agora iremos correr o comando clear com o primeiro cliente *spotter* (inicializado em 5.3.1.) após ter-se fechado a replica 1 (inicializada em 5.1.) , pelo que o ZooKeeper deverá reconhecer que não é uma replica disponivel e reconectar à replica 2 (inicializada em 5.2.).

### 6.1 Leituras coerentes por cliente

Agora iremos fazer com que a replica 1 falhe clicando "Enter" na sessão da replica 1.

Este comando poderá demorar algum tempo.

Agora iremos realizar com o comando clear com o primeiro cliente *spotter* (inicializado em 5.3.1.) na sua respetiva sessão com o seguinte comando:

```
> clear
Replica: 1 not available
Reconnecting to another available replica...
Connected to Replica: 2
```

Como a replica 1 deixou de estar disponivel o ZooKeeper conectou o cliente com a replica 2 e o comando clear irá ser executado na replica 2.

Agora iremos realizar uma nova query com o primeiro cliente *spotter*(inicializado em 5.3.1) na sua respetiva sessão com o seguinte comando:

```
> spot person 1*
person,123,<timestamp>,Tagus,38.737613,-9.303164
```

A replica 1 (inicializada em 5.1) deixou de estar disponivel e o ZooKeeper conectou o cliente com a replica 2 (inicializada em 5.2.), no entanto devolve o mesmo resultado que a replica 1 (inicializada em 5.1) iria devolver se continuasse ativa (como no passo 5.4) mantendo a coerência com o mesmo cliente.

Tentemos agora com outro comando:

```
> trail car 20SD24
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
car,20SD24,<timestamp>,Tagus,38.737613,-9.303164
```

Novamente voltou a devolver o mesmo resultado que a replica 1 (inicializada em 5.1) iria devolver se continuasse ativa (como no passo 5.4).


## 7. Finalização da segunda demonstração

Agora iremos fechar todas as sessões ainda ativas.

Para o primeiro cliente *spotter* (inicializado em 5.3) realizar o seguinte comando:

```
> quit
```

Para fechar a segunda replica (lançada em 5.2) basta clicar "Enter", esta operação poderá demorar algum tempo.

Agora na diretoria /*root*/ correr o seguinte comando:

```
$ mvn clean
```

Por fim fechar a sessão do ZooKeeper (inicializada em 4.1.) com o comando de ctrl-C

Assim terminamos a demonstração do projeto.