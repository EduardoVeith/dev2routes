package com.exemplo.app;

// Importação do microframework Spark para criação da API REST
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Microserviço Dev2: Implementação do algoritmo de fluxo máximo
 * utilizando Edmonds-Karp (variação do Ford-Fulkerson com BFS).
 * A API expõe um endpoint HTTP onde o grafo é enviado como JSON
 * e a resposta traz o fluxo máximo e os fluxos por aresta.
 */
public class App {

    // Classe interna que representa uma aresta do grafo
    public static class Edge {
        int from, to, capacity, flow;
        Edge reverse;

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }

        // Capacidade residual = capacidade original - fluxo atual
        public int residualCapacity() {
            return capacity - flow;
        }
    }

    // Classe principal de estrutura do grafo e implementação do algoritmo
    public static class Graph {
        private final List<Edge>[] adj; // Lista de adjacência
        public final List<Edge> allEdges = new ArrayList<>(); // Arestas para output
        private final int nodeCount;

        @SuppressWarnings("unchecked")
        public Graph(int n) {
            nodeCount = n;
            adj = new ArrayList[n];
            for (int i = 0; i < n; i++) {
                adj[i] = new ArrayList<>();
            }
        }

        // Adiciona uma aresta direta e sua reversa (grafo residual)
        public void addEdge(int from, int to, int capacity) {
            Edge fwd = new Edge(from, to, capacity);
            Edge rev = new Edge(to, from, 0); // aresta reversa com 0 capacidade inicial
            fwd.reverse = rev;
            rev.reverse = fwd;
            adj[from].add(fwd);
            adj[to].add(rev);
            allEdges.add(fwd); // apenas a original será exportada
        }

        // Implementação de Edmonds-Karp com busca em largura (BFS)
        public int edmondsKarp(int source, int sink) {
            int maxFlow = 0;
            while (true) {
                Edge[] parent = new Edge[nodeCount];
                boolean[] visited = new boolean[nodeCount];
                Queue<Integer> queue = new LinkedList<>();
                queue.add(source);
                visited[source] = true;

                // Busca em largura para encontrar caminho aumentante
                while (!queue.isEmpty()) {
                    int u = queue.poll();
                    for (Edge edge : adj[u]) {
                        if (!visited[edge.to] && edge.residualCapacity() > 0) {
                            visited[edge.to] = true;
                            parent[edge.to] = edge;
                            queue.add(edge.to);
                        }
                    }
                }

                // Se não chegou ao destino, não há mais caminho aumentante
                if (!visited[sink])
                    break;

                // Determina o gargalo (mínimo da capacidade residual)
                int flow = Integer.MAX_VALUE;
                for (int v = sink; v != source; v = parent[v].from) {
                    flow = Math.min(flow, parent[v].residualCapacity());
                }

                // Atualiza os fluxos ao longo do caminho
                for (int v = sink; v != source; v = parent[v].from) {
                    parent[v].flow += flow;
                    parent[v].reverse.flow -= flow;
                }

                maxFlow += flow;
            }
            return maxFlow;
        }
    }

    // Método principal: inicializa o servidor HTTP e define o endpoint
    public static void main(String[] args) {
        port(8080); // Define a porta da API

        // Define o endpoint POST para cálculo do fluxo máximo
        post("/fluxo-maximo", (req, res) -> {
            res.type("application/json");

            // Converte o corpo da requisição JSON em objeto
            JSONObject input = new JSONObject(req.body());

            int nodeCount = input.getInt("nodeCount");
            JSONArray sources = input.getJSONArray("sources");
            JSONArray sinks = input.getJSONArray("sinks");
            JSONArray edges = input.getJSONArray("edges");

            // Cria super-source e super-sink fictícios
            int superSource = nodeCount;
            int superSink = nodeCount + 1;
            Graph graph = new Graph(nodeCount + 2);

            // Adiciona arestas reais ao grafo
            for (int i = 0; i < edges.length(); i++) {
                JSONObject e = edges.getJSONObject(i);
                graph.addEdge(e.getInt("from"), e.getInt("to"), e.getInt("capacity"));
            }

            // Liga superSource a cada source com capacidade infinita
            for (int i = 0; i < sources.length(); i++) {
                graph.addEdge(superSource, sources.getInt(i), Integer.MAX_VALUE);
            }

            // Liga cada sink ao superSink
            for (int i = 0; i < sinks.length(); i++) {
                graph.addEdge(sinks.getInt(i), superSink, Integer.MAX_VALUE);
            }

            // Executa o algoritmo de fluxo máximo
            int maxFlow = graph.edmondsKarp(superSource, superSink);

            // Constrói a resposta JSON
            JSONObject output = new JSONObject();
            output.put("maxFlow", maxFlow);

            JSONArray flowDetails = new JSONArray();
            for (Edge e : graph.allEdges) {
                JSONObject edgeJson = new JSONObject();
                edgeJson.put("from", e.from);
                edgeJson.put("to", e.to);
                edgeJson.put("capacity", e.capacity);
                edgeJson.put("flow", e.flow);
                flowDetails.put(edgeJson);
            }
            output.put("edges", flowDetails);

            return output.toString(2); // retorna JSON formatado
        });
    }
}
