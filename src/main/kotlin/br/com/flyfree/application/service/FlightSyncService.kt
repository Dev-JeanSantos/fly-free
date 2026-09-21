package br.com.flyfree.application.service

import br.com.flyfree.domain.entity.BestOffer
import br.com.flyfree.domain.entity.FlightResult
import br.com.flyfree.domain.entity.SyncLog
import br.com.flyfree.domain.entity.SyncStatus
import br.com.flyfree.domain.repository.BestOfferRepository
import br.com.flyfree.domain.repository.FlightResultRepository
import br.com.flyfree.domain.repository.SyncLogRepository
import br.com.flyfree.infrastructure.client.GeckoApiClient
import br.com.flyfree.infrastructure.client.GeckoItineraryDto
import br.com.flyfree.presentation.dto.SyncResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class FlightSyncService(
    private val routeService: RouteService,
    private val geckoApiClient: GeckoApiClient,
    private val flightResultRepository: FlightResultRepository,
    private val bestOfferRepository: BestOfferRepository,
    private val syncLogRepository: SyncLogRepository,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 6,18 * * *")
    fun scheduledSync() {
        log.info("Iniciando sincronização automática...")
        sync()
    }

    @Transactional
    fun sync(): SyncResult {
        val startedAt = System.currentTimeMillis()
        val routes = routeService.findActiveRoutes()

        if (routes.isEmpty()) {
            log.info("Nenhuma rota ativa para sincronizar.")
            syncLogRepository.save(SyncLog(status = SyncStatus.SUCCESS, routesSynced = 0, durationMs = 0))
            return SyncResult(synced = 0, failed = 0, durationMs = 0)
        }

        var synced = 0
        var failed = 0
        val errors = mutableListOf<String>()

        for (route in routes) {
            try {
                log.info("Sincronizando: ${route.origin} → ${route.destination} em ${route.travelDate}")

                val itineraries = geckoApiClient.fetchGolFlights(
                    from = route.origin,
                    to = route.destination,
                    departureDate = route.travelDate,
                    returnDate = route.returnDate
                )

                // Filtra apenas itinerários da rota de ida (origin == route.origin)
                val outbound = itineraries.filter { it.origin == route.origin }

                if (outbound.isEmpty()) {
                    log.warn("Nenhum voo de ida retornado para ${route.origin} → ${route.destination}")
                    failed++
                    continue
                }

                // Salva todos os voos de ida
                val savedFlights = outbound.mapNotNull { dto -> toFlightResult(dto, route) }

                // Determina a melhor oferta (menor preço)
                val cheapest = savedFlights.minByOrNull { it.price }

                if (cheapest != null) {
                    val existing = bestOfferRepository.findByRoute(route)
                    if (existing.isPresent) {
                        bestOfferRepository.save(
                            existing.get().copy(
                                flightResult = cheapest,
                                price = cheapest.price,
                                updatedAt = LocalDateTime.now()
                            )
                        )
                    } else {
                        bestOfferRepository.save(
                            BestOffer(route = route, flightResult = cheapest, price = cheapest.price)
                        )
                    }
                    log.info("Melhor oferta ${route.origin}→${route.destination}: R$ ${cheapest.price}")
                }

                synced++
            } catch (e: Exception) {
                log.error("Erro ao sincronizar rota ${route.origin}→${route.destination}: ${e.message}")
                errors.add("${route.origin}→${route.destination}: ${e.message}")
                failed++
            }
        }

        val durationMs = System.currentTimeMillis() - startedAt
        val status = when {
            failed == 0 -> SyncStatus.SUCCESS
            synced == 0 -> SyncStatus.FAILED
            else -> SyncStatus.PARTIAL
        }

        syncLogRepository.save(
            SyncLog(
                status = status,
                routesSynced = synced,
                routesFailed = failed,
                durationMs = durationMs,
                errorMessage = if (errors.isNotEmpty()) errors.joinToString("\n") else null
            )
        )

        log.info("Sincronização concluída: $synced ok, $failed falhas, ${durationMs}ms")
        return SyncResult(synced = synced, failed = failed, durationMs = durationMs)
    }

    fun getRecentLogs(limit: Int = 20): List<SyncLog> =
        syncLogRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, limit))

    private fun toFlightResult(dto: GeckoItineraryDto, route: br.com.flyfree.domain.entity.Route): FlightResult? {
        return try {
            val flightNumber = dto.segments.firstOrNull()?.flight?.flightNumber ?: "N/A"
            val airlineCode = dto.segments.firstOrNull()?.flight?.airlineCode ?: "G3"
            val price = dto.cheapestOffer?.total?.amount ?: BigDecimal.ZERO
            val fareClass = dto.cheapestOffer?.brandId

            flightResultRepository.save(
                FlightResult(
                    route = route,
                    flightNumber = "$airlineCode$flightNumber",
                    departureTime = LocalDateTime.parse(dto.departure ?: "${route.travelDate}T00:00:00"),
                    arrivalTime = LocalDateTime.parse(dto.arrival ?: "${route.travelDate}T00:00:00"),
                    price = price,
                    stops = dto.stopsCount ?: 0,
                    fareClass = fareClass,
                    rawJson = objectMapper.writeValueAsString(dto)
                )
            )
        } catch (e: Exception) {
            log.error("Erro ao mapear itinerário: ${e.message}")
            null
        }
    }
}
