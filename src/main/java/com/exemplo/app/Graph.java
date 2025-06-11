package com.exemplo.app;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe de estrutura de grafo com implementação do algoritmo de Edmonds-Karp
 * (fluxo máximo).
 */
public class Graph {

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

    private final List<Edge>[] adj;
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
                for (Edge e : adj[u]) {
                    if (!visited[e.to] && e.residualCapacity() > 0) {
                        visited[e.to] = true;
                        parent[e.to] = e;
                        queue.add(e.to);
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

    public JSONArray exportEdgeFlows() {
        JSONArray result = new JSONArray();
        for (List<Edge> edges : adj) {
            for (Edge e : edges) {
                if (e.capacity > 0 && e.flow > 0) {
                    JSONObject obj = new JSONObject();
                    obj.put("from", e.from);
                    obj.put("to", e.to);
                    obj.put("flow", e.flow);
                    obj.put("capacity", e.capacity);
                    result.put(obj);
                }
            }
        }
        return result;
    }
}
