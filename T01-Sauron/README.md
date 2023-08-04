# Sauron

Distributed Systems 2019-2020, 2nd semester project


## Authors
  
**Group T01**

### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.  
This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.

### Team members  

| Number | Name              | User                                 | Email                                                 |
| -------|-------------------|--------------------------------------|-------------------------------------------------------|
| 87668  | João Antunes      | <https://github.com/jcja>            | <mailto:joao.c.jeronimo.antunes@tecnico.ulisboa.pt>   |
| 83609  | Maria Inês Morais | <https://github.com/inesqmorais>     | <mailto:ines.q.morais@tecnico.ulisboa.pt>             |


### Task leaders
  
| Task set | To-Do                         | Leader              |
| ---------|-------------------------------|---------------------|
| core     | protocol buffers, silo-client |  Group T01          |
| T1       | cam_join, cam_info, eye       |  Maria Inês Morais  |
| T2       | report                        |  João Antunes       |
| T2       | spotter                       |  Maria Inês Morais  |
| T3       | track, trackMatch, trace      |  João Antunes       |
| T4       | test T1                       |  João Antunes       |
| T5       | test T2                       |  Maria Inês Morais  |
| T6       | test T3 track, trackMatch     |  Maria Inês Morais  |
| T7       | test T3 trace                 |  João Antunes       |
| T8       | test control operation ping   |  João Antunes       |
| T9       | test control operation clear  |  João Antunes       |
| T10      | test control operation init   |  Maria Inês Morais  |


## Getting Started

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](https://github.com/tecnico-distsys/Sauron/blob/master/README.md) for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require the servers to be running.


## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
