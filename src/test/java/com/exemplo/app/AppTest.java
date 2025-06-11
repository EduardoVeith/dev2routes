package com.exemplo.app;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testes de stress para o algoritmo Edmonds-Karp com múltiplas fontes e
 * destinos.
 */
public class AppTest {

    @Test(timeout = 10000) // limite de tempo de 10 segundos
    public void stressTestLargeGraph() throws Exception {
        int nodeCount = 1000;
        int sourceNode = 0;
        int sinkNode = 999;

        // Cria objeto JSON para input
        JSONObject input = new JSONObject();
        input.put("nodeCount", nodeCount);
        input.put("sources", new JSONArray().put(sourceNode)); // necessário para o App.java
        input.put("sinks", new JSONArray().put(sinkNode)); // necessário para o App.java

        JSONArray edges = new JSONArray();

        // Conecta source a 100 nós intermediários
        for (int i = 1; i <= 100; i++) {
            edges.put(new JSONObject()
                    .put("from", sourceNode)
                    .put("to", i)
                    .put("capacity", 1000));
        }

        // Liga nós intermediários a nós terminais
        for (int i = 1; i <= 100; i++) {
            for (int j = 101; j < nodeCount - 1; j += 100) {
                edges.put(new JSONObject()
                        .put("from", i)
                        .put("to", j)
                        .put("capacity", 10));
            }
        }

        // Liga nós terminais ao sink
        for (int j = 101; j < nodeCount - 1; j += 100) {
            edges.put(new JSONObject()
                    .put("from", j)
                    .put("to", sinkNode)
                    .put("capacity", 1000));
        }

        input.put("edges", edges);

        // Salva input.json no disco
        Files.write(Paths.get("input.json"), input.toString(2).getBytes());

        // Executa o algoritmo principal
        App.main(null);

        // Lê output.json
        byte[] bytes = Files.readAllBytes(Paths.get("output.json"));
        String result = new String(bytes);
        JSONObject output = new JSONObject(result);

        // Verificações
        assertTrue("Output JSON deve conter maxFlow", output.has("maxFlow"));
        int maxFlow = output.getInt("maxFlow");
        assertTrue("Fluxo máximo deve ser positivo", maxFlow > 0);
    }
}
