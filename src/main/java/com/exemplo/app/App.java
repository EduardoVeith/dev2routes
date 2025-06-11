package com.exemplo.app;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implementação de Edmonds-Karp com suporte a múltiplas fontes e destinos.
 */
public class App {

    public static class Edge {
        int from, to, capacity, flow;
        Edge reverse;

        public Edge(int from, int to, int capacity) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
        }

        public int residualCapacity() {
            return capacity - flow;
        }
    }

    public static class Graph {
        private final List<Edge>[] adj;
        private final List<Edge> allEdges = new ArrayList<>();
        private final int nodeCount;

        @SuppressWarnings("unchecked")
        public Graph(int n) {
            nodeCount = n;
            adj = new ArrayList[n];
            for (int i = 0; i < n; i++) {
                adj[i] = new ArrayList<>();
            }
        }

        public void addEdge(int from, int to, int capacity) {
            Edge fwd = new Edge(from, to, capacity);
            Edge rev = new Edge(to, from, 0);
            fwd.reverse = rev;
            rev.reverse = fwd;
            adj[from].add(fwd);
            adj[to].add(rev);
            allEdges.add(fwd);
        }

        public int edmondsKarp(int source, int sink) {
            int maxFlow = 0;
            while (true) {
                Edge[] parent = new Edge[nodeCount];
                boolean[] visited = new boolean[nodeCount];
                Queue<Integer> queue = new LinkedList<>();
                queue.add(source);
                visited[source] = true;

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

                if (!visited[sink])
                    break;

                int flow = Integer.MAX_VALUE;
                for (int v = sink; v != source; v = parent[v].from) {
                    flow = Math.min(flow, parent[v].residualCapacity());
                }

                for (int v = sink; v != source; v = parent[v].from) {
                    parent[v].flow += flow;
                    parent[v].reverse.flow -= flow;
                }

                maxFlow += flow;
            }
            return maxFlow;
        }

        public void exportToJSON(String filename, int maxFlow) throws Exception {
            JSONObject output = new JSONObject();
            output.put("maxFlow", maxFlow);

            JSONArray flows = new JSONArray();
            for (Edge e : allEdges) {
                JSONObject edgeJson = new JSONObject();
                edgeJson.put("from", e.from);
                edgeJson.put("to", e.to);
                edgeJson.put("capacity", e.capacity);
                edgeJson.put("flow", e.flow);
                flows.put(edgeJson);
            }

            output.put("edges", flows);
            Files.write(Paths.get(filename), output.toString(2).getBytes());
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("input.json"));
        String input = new String(bytes);

        JSONObject json = new JSONObject(input);

        int nodeCount = json.getInt("nodeCount");
        JSONArray sources = json.getJSONArray("sources");
        JSONArray sinks = json.getJSONArray("sinks");
        JSONArray edges = json.getJSONArray("edges");

        // Cria super-source e super-sink
        int superSource = nodeCount;
        int superSink = nodeCount + 1;
        Graph graph = new Graph(nodeCount + 2); // inclui superSource e superSink

        // Adiciona arestas reais
        for (int i = 0; i < edges.length(); i++) {
            JSONObject e = edges.getJSONObject(i);
            graph.addEdge(e.getInt("from"), e.getInt("to"), e.getInt("capacity"));
        }

        // Conecta super-source a cada source
        for (int i = 0; i < sources.length(); i++) {
            int s = sources.getInt(i);
            graph.addEdge(superSource, s, Integer.MAX_VALUE);
        }

        // Conecta cada sink ao super-sink
        for (int i = 0; i < sinks.length(); i++) {
            int t = sinks.getInt(i);
            graph.addEdge(t, superSink, Integer.MAX_VALUE);
        }

        // Roda Edmonds-Karp
        int maxFlow = graph.edmondsKarp(superSource, superSink);
        graph.exportToJSON("output.json", maxFlow);
        System.out.println("Fluxo máximo: " + maxFlow);
    }
}
