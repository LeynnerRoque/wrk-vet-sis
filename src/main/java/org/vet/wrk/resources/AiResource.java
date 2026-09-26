package org.vet.wrk.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.vet.wrk.ai.GroqService;

import java.util.Map;

@Path("/api/ia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiResource {

    @Inject
    GroqService groqService;

    @POST
    @Path("/polir")
    public Map<String, String> polirTexto(Map<String, String> payload) {
        String servico = payload.getOrDefault("servico", "Consulta");
        String relato = payload.getOrDefault("relato", "");

        String textoPolido = groqService.polirMensagemComIA(servico, relato);

        return Map.of("textoPolido", textoPolido);
    }
}