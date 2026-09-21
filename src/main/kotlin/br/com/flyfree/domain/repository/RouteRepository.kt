package br.com.flyfree.domain.repository

import br.com.flyfree.domain.entity.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface RouteRepository : JpaRepository<Route, Long> {

    fun findByOriginAndDestinationAndTravelDate(
        origin: String,
        destination: String,
        travelDate: LocalDate
    ): Optional<Route>

    fun findAllByActiveTrue(): List<Route>
}
