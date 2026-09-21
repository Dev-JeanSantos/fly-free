package br.com.flyfree.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "flight_results")
data class FlightResult(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    val route: Route,

    @Column(nullable = false, length = 10)
    val flightNumber: String,

    @Column(nullable = false)
    val departureTime: LocalDateTime,

    @Column(nullable = false)
    val arrivalTime: LocalDateTime,

    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,

    @Column(nullable = false)
    val stops: Int = 0,

    @Column(length = 50)
    val fareClass: String? = null,

    @Column(name = "raw_json", columnDefinition = "TEXT")
    val rawJson: String? = null,

    @Column(name = "synced_at", nullable = false)
    val syncedAt: LocalDateTime = LocalDateTime.now()
)
