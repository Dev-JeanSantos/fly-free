package br.com.flyfree.presentation.controller

import br.com.flyfree.application.service.RouteService
import br.com.flyfree.presentation.dto.RouteResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/routes")
class RouteController(private val routeService: RouteService) {

    @PostMapping
    fun register(
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<RouteResponse> {
        val route = routeService.register(from, to, date)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            RouteResponse(
                id = route.id,
                from = route.origin,
                to = route.destination,
                date = route.travelDate,
                active = route.active,
                createdAt = route.createdAt
            )
        )
    }

    @GetMapping
    fun listAll(): ResponseEntity<List<RouteResponse>> {
        val routes = routeService.listAll().map {
            RouteResponse(
                id = it.id,
                from = it.origin,
                to = it.destination,
                date = it.travelDate,
                active = it.active,
                createdAt = it.createdAt
            )
        }
        return ResponseEntity.ok(routes)
    }

    @DeleteMapping("/{id}")
    fun deactivate(@PathVariable id: Long): ResponseEntity<RouteResponse> {
        val route = routeService.deactivate(id)
        return ResponseEntity.ok(
            RouteResponse(
                id = route.id,
                from = route.origin,
                to = route.destination,
                date = route.travelDate,
                active = route.active,
                createdAt = route.createdAt
            )
        )
    }
}
