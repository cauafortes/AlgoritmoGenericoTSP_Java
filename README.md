# AlgoritmoGenericoTSP
# Algoritmo Genético para o Problema do Caixeiro Viajante (TSP)

> Atividade Prática — Inteligência Artificial  
> Baseado no repositório de referência: [TSP-Modelo-Alunos](https://github.com/matheuefranco/TSP-Modelo-Alunos)

---

## 📌 Descrição

Este projeto implementa um **Algoritmo Genético (AG)** para resolver o **Problema do Caixeiro Viajante (TSP — Travelling Salesman Problem)**.

O objetivo é encontrar a **rota de menor distância total** que visita todas as cidades exatamente uma vez e retorna à cidade de origem, utilizando operadores genéticos adaptados para representações de **permutação**.

---

## 📁 Estrutura do Projeto

```
TSP-AG/
├── Cidade.java       # Classe que representa uma cidade (nome, x, y, distância)
├── AGtsp.java        # Algoritmo Genético completo para o TSP
├── cidades.csv       # Base de dados com cidades brasileiras e coordenadas
└── README.md         # Este arquivo
```

## Como Compilar e Executar

### Pré-requisitos
- Java JDK 8 ou superior instalado
- Arquivo `cidades.csv` no mesmo diretório

### Compilação

```bash
javac Cidade.java AGtsp.java
```

### Execução

```bash
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" AGtsp
```

> A flag `-Dfile.encoding=UTF-8` garante que os acentos e caracteres especiais dos nomes das cidades sejam exibidos corretamente no terminal Windows.

---

Atividade desenvolvida com base na estrutura fornecida pelo professor, adaptando o algoritmo genético do Problema da Mochila para o Problema do Caixeiro Viajante.
