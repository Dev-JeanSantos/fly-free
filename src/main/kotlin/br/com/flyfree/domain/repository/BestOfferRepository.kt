package br.com.flyfree.domain.repository

import br.com.flyfree.domain.entity.BestOffer
import br.com.flyfree.domain.entity.Route
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface BestOfferRepository : JpaRepository<BestOffer, Long> {

    fun findByRoute(route: Route): Optional<BestOffer>

    fun findByRouteOriginAndRouteDestinationAndRouteTravelDate(
        origin: String,
        destination: String,
        travelDate: java.time.LocalDate
    ): Optional<BestOffer>
}
