package br.com.flyfree.application.service

import br.com.flyfree.domain.entity.BestOffer
import br.com.flyfree.domain.entity.FlightResult
import br.com.flyfree.domain.entity.SyncLog
import br.com.flyfree.domain.entity.SyncStatus
import br.com.flyfree.domain.repository.BestOfferRepository
import br.com.flyfree.domain.repository.FlightResultRepository
import br.com.flyfree.domain.repository.SyncLogRepository
import br.com.flyfree.infrastructure.client.GeckoApiClient
import br.com.flyfree.presentation.dto.SyncResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    private val dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

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
            val log = syncLogRepository.save(
                SyncLog(status = SyncStatus.SUCCESS, routesSynced = 0, durationMs = 0)
            )
            return SyncResult(synced = 0, failed = 0, durationMs = 0)
        }

        var synced = 0
        var failed = 0
        val errors = mutableListOf<String>()

        for (route in routes) {
            try {
                log.info("Sincronizando: ${route.origin} → ${route.destination} em ${route.travelDate}")
                val flights = geckoApiClient.fetchGolFlights(
                    from = route.origin,
                    to = route.destination,
                    departureDate = route.travelDate
                )

                if (flights.isEmpty()) {
                    log.warn("Nenhum voo retornado para ${route.origin} → ${route.destination}")
                    failed++
                    continue
                }

                // Salva todos os resultados brutos
                val savedFlights = flights.mapNotNull { dto ->
                    try {
                        flightResultRepository.save(
                            FlightResult(
                                route = route,
                                flightNumber = dto.flightNumber ?: "N/A",
                                departureTime = parseDateTime(dto.departureTime, route.travelDate.toString()),
                                arrivalTime = parseDateTime(dto.arrivalTime, route.travelDate.toString()),
                                price = BigDecimal.valueOf(dto.price ?: 0.0),
                                stops = dto.stops ?: 0,
                                fareClass = dto.fareClass,
                                rawJson = objectMapper.writeValueAsString(dto)
                            )
                        )
                    } catch (e: Exception) {
                        log.error("Erro ao salvar voo: ${e.message}")
                        null
                    }
                }

                // Calcula e persiste a melhor oferta (menor preço)
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
                            BestOffer(
                                route = route,
                                flightResult = cheapest,
                                price = cheapest.price
                            )
                        )
                    }
                }

                synced++
            } catch (e: Exception) {
                log.error("Erro ao sincronizar rota ${route.id}: ${e.message}")
                errors.add("Rota ${route.origin}→${route.destination}: ${e.message}")
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

    private fun parseDateTime(value: String?, date: String): LocalDateTime {
        if (value == null) return LocalDateTime.now()
        return try {
            LocalDateTime.parse(value, dtFormatter)
        } catch (e: Exception) {
            LocalDateTime.parse("${date}T${value}")
        }
    }
}
