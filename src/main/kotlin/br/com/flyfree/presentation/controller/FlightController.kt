package br.com.flyfree.presentation.controller

import br.com.flyfree.application.service.FlightService
import br.com.flyfree.application.service.FlightSyncService
import br.com.flyfree.presentation.dto.BestOfferResponse
import br.com.flyfree.presentation.dto.SyncLogResponse
import br.com.flyfree.presentation.dto.SyncResult
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1")
class FlightController(
    private val flightService: FlightService,
    private val flightSyncService: FlightSyncService
) {

    @GetMapping("/flights")
    fun getBestOffer(
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<BestOfferResponse> {
        return ResponseEntity.ok(flightService.getBestOffer(from, to, date))
    }

    @PostMapping("/sync/trigger")
    fun triggerSync(): ResponseEntity<SyncResult> {
        return ResponseEntity.ok(flightSyncService.sync())
    }

    @GetMapping("/sync/logs")
    fun getSyncLogs(
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<SyncLogResponse>> {
        val logs = flightSyncService.getRecentLogs(limit).map {
            SyncLogResponse(
                id = it.id,
                status = it.status.name,
                routesSynced = it.routesSynced,
                routesFailed = it.routesFailed,
                durationMs = it.durationMs,
                errorMessage = it.errorMessage,
                startedAt = it.startedAt
            )
        }
        return ResponseEntity.ok(logs)
    }
}
