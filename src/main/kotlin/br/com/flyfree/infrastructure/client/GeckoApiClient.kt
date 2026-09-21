package br.com.flyfree.infrastructure.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate

@Component
class GeckoApiClient(
    @Value("\${geckoapi.base-url}") private val baseUrl: String,
    @Value("\${geckoapi.api-key}") private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer $apiKey")
        .defaultHeader("Content-Type", "application/json")
        .build()

    fun fetchGolFlights(
        from: String,
        to: String,
        departureDate: LocalDate,
        adults: Int = 1
    ): List<GeckoFlightDto> {
        log.info("Buscando voos GOL: $from → $to em $departureDate")

        val request = GeckoRequest(
            target = "voegol.com.br:plp",
            from = from,
            to = to,
            departureDate = departureDate.toString(),
            adults = adults
        )

        return webClient.post()
            .uri("/v1/extract")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<GeckoResponse>()
            .onErrorResume { e ->
                log.error("Erro ao chamar GeckoAPI: ${e.message}")
                Mono.empty()
            }
            .block()
            ?.results
            ?: emptyList()
    }
}

data class GeckoRequest(
    val target: String,
    val from: String,
    val to: String,
    @JsonProperty("departure_date")
    val departureDate: String,
    val adults: Int = 1
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoResponse(
    val results: List<GeckoFlightDto> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoFlightDto(
    @JsonProperty("flight_number")
    val flightNumber: String? = null,

    @JsonProperty("departure_time")
    val departureTime: String? = null,

    @JsonProperty("arrival_time")
    val arrivalTime: String? = null,

    val price: Double? = null,

    val stops: Int? = 0,

    @JsonProperty("fare_class")
    val fareClass: String? = null
)
