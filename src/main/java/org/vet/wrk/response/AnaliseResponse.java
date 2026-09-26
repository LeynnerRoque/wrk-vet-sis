package org.vet.wrk.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnaliseResponse(
        @JsonProperty("mensagem_polida") String mensagemPolida
) {}