package br.com.flyfree.infrastructure.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal
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
        returnDate: LocalDate? = null,
        numAdults: Int = 1,
        numChildren: Int = 0,
        numInfants: Int = 0
    ): List<GeckoItineraryDto> {
        log.info("Buscando voos GOL: $from → $to em $departureDate")

        val request = GeckoRequest(
            target = "voegol.com.br",
            type = "plp",
            from = from,
            to = to,
            departureDate = departureDate.toString(),
            returnDate = returnDate?.toString(),
            numAdults = numAdults,
            numChildren = numChildren,
            numInfants = numInfants
        )

        return webClient.post()
            .uri("/v1/extract")
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { response ->
                response.bodyToMono<String>().doOnNext { body ->
                    log.error("GeckoAPI 4xx - body: $body")
                }.flatMap { body ->
                    reactor.core.publisher.Mono.error(RuntimeException("GeckoAPI error: $body"))
                }
            }
            .bodyToMono<GeckoResponse>()
            .onErrorResume { e ->
                log.error("Erro ao chamar GeckoAPI: ${e.message}")
                Mono.empty()
            }
            .block()
            ?.data
            ?.itineraries
            ?: emptyList()
    }
}

// ── Request ──────────────────────────────────────────────

data class GeckoRequest(
    val target: String,
    val type: String,
    val from: String,
    val to: String,
    val departureDate: String,
    val returnDate: String? = null,
    val numAdults: Int = 1,
    val numChildren: Int = 0,
    val numInfants: Int = 0
)

// ── Response ─────────────────────────────────────────────

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoResponse(
    val data: GeckoData? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoData(
    val itineraries: List<GeckoItineraryDto> = emptyList(),
    val success: Boolean = false,
    val totalResults: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoItineraryDto(
    val position: Int? = null,
    val origin: String? = null,
    val destination: String? = null,
    val departure: String? = null,
    val arrival: String? = null,
    val stopsCount: Int? = 0,
    val duration: String? = null,
    val segments: List<GeckoSegmentDto> = emptyList(),
    val cheapestOffer: GeckoCheapestOfferDto? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoSegmentDto(
    val origin: String? = null,
    val destination: String? = null,
    val departure: String? = null,
    val arrival: String? = null,
    val duration: String? = null,
    val flight: GeckoFlightInfoDto? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoFlightInfoDto(
    val airlineCode: String? = null,
    val flightNumber: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoCheapestOfferDto(
    val brandId: String? = null,
    val cabinClass: String? = null,
    val total: GeckoPriceDto? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeckoPriceDto(
    val currency: String? = null,
    val amount: BigDecimal? = null
)
