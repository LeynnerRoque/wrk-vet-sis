package org.vet.wrk.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.vet.wrk.ai.GroqService;
import org.vet.wrk.response.AnaliseResponse;

import java.util.Map;

@Path("/api/ia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiResource {

    private final GroqService groqService;

    public AiResource(GroqService groqService) {
        this.groqService = groqService;
    }

    @POST
    @Path("/polir") // ou o caminho que você estiver a utilizar
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> polirTexto(Map<String, String> payload) {
        String servico = payload.getOrDefault("servico", "Consulta");
        String relato = payload.getOrDefault("relato", "");

        AnaliseResponse response = groqService.polirMensagemComGroq(servico, relato);

        String textoPolido = response != null && response.mensagemPolida() != null
                ? response.mensagemPolida()
                : relato;

        return Map.of("textoPolido", textoPolido);
    }
}