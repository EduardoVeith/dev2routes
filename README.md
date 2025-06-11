# API de Fluxo Máximo - Dev2

Este microserviço foi desenvolvido como parte do projeto **Sistema de Otimização de Rede de Entregas**, responsável pela **Missão do Dev2**. Seu objetivo é calcular o fluxo máximo de pacotes entre depósitos e zonas de entrega usando o algoritmo **Edmonds-Karp** (variação de Ford-Fulkerson com busca em largura).

A API foi desenvolvida em **Java** utilizando o microframework **Spark Java**, com entrada e saída em formato **JSON**, e está preparada para lidar com múltiplos nós e rotas.

## Funcionalidade

- Recebe uma rede de entrega com depósitos, hubs e zonas de entrega.
- Calcula o fluxo máximo possível entre as fontes (depósitos) e os destinos (zonas de entrega).
- Retorna o valor do fluxo máximo e os fluxos percorridos em cada aresta da rede.

## Como usar

### Opção 1: Localmente com Maven

1. Clone o repositório e navegue até a pasta raiz.
2. Execute:

```bash
mvn clean install
java -jar target/app.jar
```

A API estará disponível em: `http://localhost:8080/fluxo-maximo`

### Opção 2: Via Docker

```bash
docker build -t fluxo-maximo-api .
docker run -p 8080:8080 fluxo-maximo-api
```

## Exemplo de Requisição (Insomnia/Postman)

**POST** `http://localhost:8080/fluxo-maximo`

```json
{
  "nodeCount": 5,
  "sources": [0],
  "sinks": [4],
  "edges": [
    { "from": 0, "to": 1, "capacity": 100 },
    { "from": 1, "to": 2, "capacity": 1 },
    { "from": 2, "to": 3, "capacity": 100 },
    { "from": 3, "to": 4, "capacity": 100 }
  ]
}
```

## Resposta Esperada

```json
{
  "maxFlow": 1,
  "edges": [
    { "from": 0, "to": 1, "capacity": 100, "flow": 1 },
    { "from": 1, "to": 2, "capacity": 1, "flow": 1 },
    { "from": 2, "to": 3, "capacity": 100, "flow": 1 },
    { "from": 3, "to": 4, "capacity": 100, "flow": 1 }
  ]
}
```

## Observações

- As capacidades ilimitadas são representadas com o valor `2147483647` (máximo de um `int`).
- A API trata múltiplos depósitos e múltiplas zonas de entrega automaticamente.
- Está pronta para ser integrada com os demais módulos (visualização, simulação e backend geral).
