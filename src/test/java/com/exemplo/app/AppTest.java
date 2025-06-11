
package com.exemplo.app;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Teste automatizado de estresse para o microserviço de fluxo máximo.
 * Envia um grafo via POST e verifica se o fluxo máximo retornado é o esperado.
 */
public class AppTest {

    @BeforeAll
    public static void verificarAPI() {
        try {
            URL url = new URL("http://localhost:8080/fluxo-maximo");
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setConnectTimeout(1000);
            conexao.connect();
            assumeTrue(conexao.getResponseCode() >= 200);
        } catch (Exception e) {
            assumeTrue(false, "API não está rodando em http://localhost:8080/fluxo-maximo");
        }
    }

    @Test
    public void stressTestLargeGraph() throws Exception {
        // Grafo com múltiplas fontes e múltiplos destinos
        JSONObject input = new JSONObject();
        input.put("nodeCount", 6);
        input.put("sources", new JSONArray(new int[] { 0, 1 }));
        input.put("sinks", new JSONArray(new int[] { 4, 5 }));

        JSONArray edges = new JSONArray();
        edges.put(new JSONObject().put("from", 0).put("to", 2).put("capacity", 10));
        edges.put(new JSONObject().put("from", 1).put("to", 2).put("capacity", 15));
        edges.put(new JSONObject().put("from", 2).put("to", 3).put("capacity", 20));
        edges.put(new JSONObject().put("from", 3).put("to", 4).put("capacity", 10));
        edges.put(new JSONObject().put("from", 3).put("to", 5).put("capacity", 5));
        input.put("edges", edges);

        // Envia o JSON para o endpoint da API
        URL url = new URL("http://localhost:8080/fluxo-maximo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(input.toString().getBytes());
        }

        // Lê o resultado retornado
        byte[] responseBytes = conn.getInputStream().readAllBytes();
        String response = new String(responseBytes);
        JSONObject result = new JSONObject(response);

        // Verifica se o fluxo máximo corresponde ao esperado
        assertEquals(15, result.getInt("maxFlow"));
    }
}