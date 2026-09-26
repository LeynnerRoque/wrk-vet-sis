package org.vet.wrk.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.vet.wrk.response.AnaliseResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GroqService {

    @ConfigProperty(name = "groq.api.key")
    String groqApiKey;

    @ConfigProperty(name = "groq.model", defaultValue = "llama-3.1-8b-instant")
    String groqModel;

    @Inject
    ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AnaliseResponse polirMensagemComGroq(String servico, String relatoCliente) {
        String systemPrompt = """
            Você é um assistente virtual acolhedor e profissional de uma clínica veterinária humanizada da Dra. Aline Werneck.
            O usuário vai fornecer o serviço desejado e um relato simples sobre o pet.
            Reescreva esse relato de forma educada, clara e profissional para ser enviado via WhatsApp à veterinária.
            Seja direto, mantenha o tom de acolhimento e não inclua explicações extras, apenas o texto pronto para envio.
            
            Responda OBRIGATORIAMENTE em formato JSON válido seguindo exatamente este schema:
            {
              "mensagem_polida": "string"
            }
            """;

        String userPrompt = """
            --- SERVIÇO DESEJADO ---
            %s

            --- RELATO DO TUTOR ---
            %s
            """.formatted(servico, relatoCliente);

        try {
            Map<String, Object> requestBodyMap = Map.of(
                    "model", groqModel.trim(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "temperature", 0.7
            );

            String jsonBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("ERRO GROQ API [Status " + response.statusCode() + "]: " + response.body());
                // Fallback seguro encapsulado no DTO
                return new AnaliseResponse(relatoCliente);
            }

            Map<String, Object> rootResponse = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) rootResponse.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String contentJson = (String) message.get("content");

            return objectMapper.readValue(contentJson, AnaliseResponse.class);

        } catch (Exception e) {
            System.err.println("EXCEÇÃO INTERNA NO GROQ SERVICE: " + e.getMessage());
            e.printStackTrace();
            // Fallback seguro em caso de falha
            return new AnaliseResponse(relatoCliente);
        }
    }
}