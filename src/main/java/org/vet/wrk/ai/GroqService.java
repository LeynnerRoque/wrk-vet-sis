package org.vet.wrk.ai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ApplicationScoped
public class GroqService {

    @ConfigProperty(name = "groq.api.url")
    String apiUrl;

    @ConfigProperty(name = "groq.api.key")
    String apiKey;

    @ConfigProperty(name = "groq.model")
    String model;

    @Inject
    ObjectMapper objectMapper;

    public String polirMensagemComIA(String servico, String relatoCliente) {
        try {
            Client client = ClientBuilder.newClient();

            // Montando o payload JSON no formato da API da Groq/OpenAI
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", model);
            rootNode.put("temperature", 0.7);

            ArrayNode messagesArray = objectMapper.createArrayNode();

            // Mensagem de Sistema (Instrução para a IA)
            ObjectNode systemMessage = objectMapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Você é um assistente virtual de uma clínica veterinária humanizada. " +
                    "O usuário vai te dar um relato simples sobre o pet e o serviço desejado (" + servico + "). " +
                    "Reescreva esse texto de forma educada, clara e profissional para ser enviado via WhatsApp à veterinária. " +
                    "Seja direto, mantenha o tom de acolhimento e não inclua explicações extras, apenas o texto pronto para envio.");
            messagesArray.add(systemMessage);

            // Mensagem do Utilizador
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", "Serviço: " + servico + ". Relato do tutor: " + relatoCliente);
            messagesArray.add(userMessage);

            rootNode.set("messages", messagesArray);

            // Executando a requisição POST
            Response response = client.target(apiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(Entity.json(rootNode.toString()));

            if (response.getStatus() == 200) {
                String responseBody = response.readEntity(String.class);
                // Extraindo o texto da resposta JSON da IA
                var jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("choices").get(0).get("message").get("content").asText().trim();
            } else {
                return "Erro ao comunicar com a IA. Tente novamente mais tarde.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro interno ao processar a IA: " + e.getMessage();
        }
    }
}
