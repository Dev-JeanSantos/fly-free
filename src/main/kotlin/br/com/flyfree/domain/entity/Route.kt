package br.com.flyfree.domain.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "routes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["origin", "destination", "travel_date"])]
)
data class Route(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 3)
    val origin: String,

    @Column(nullable = false, length = 3)
    val destination: String,

    @Column(name = "travel_date", nullable = false)
    val travelDate: LocalDate,

    @Column(name = "return_date")
    val returnDate: LocalDate? = null,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
