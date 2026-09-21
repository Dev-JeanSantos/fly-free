package br.com.flyfree.domain.repository

import br.com.flyfree.domain.entity.FlightResult
import br.com.flyfree.domain.entity.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlightResultRepository : JpaRepository<FlightResult, Long> {

    fun findAllByRoute(route: Route): List<FlightResult>

    fun findTopByRouteOrderByPriceAsc(route: Route): FlightResult?
}
