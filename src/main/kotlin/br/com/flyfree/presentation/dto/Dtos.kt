package br.com.flyfree.presentation.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// --- Request DTOs ---

data class RegisterRouteRequest(
    val from: String,
    val to: String,
    val date: LocalDate
)

// --- Response DTOs ---

data class RouteResponse(
    val id: Long,
    val from: String,
    val to: String,
    val date: LocalDate,
    val active: Boolean,
    val createdAt: LocalDateTime
)

data class BestOfferResponse(
    val from: String,
    val to: String,
    val date: LocalDate,
    val flightNumber: String,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    val price: BigDecimal,
    val stops: Int,
    val fareClass: String?,
    val lastUpdated: LocalDateTime
)

data class SyncResult(
    val synced: Int,
    val failed: Int,
    val durationMs: Long
)

data class SyncLogResponse(
    val id: Long,
    val status: String,
    val routesSynced: Int,
    val routesFailed: Int,
    val durationMs: Long?,
    val errorMessage: String?,
    val startedAt: LocalDateTime
)

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
