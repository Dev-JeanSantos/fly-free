package br.com.flyfree.application.service

import br.com.flyfree.domain.repository.BestOfferRepository
import br.com.flyfree.domain.repository.RouteRepository
import br.com.flyfree.infrastructure.exception.OfferNotFoundException
import br.com.flyfree.infrastructure.exception.RouteNotFoundException
import br.com.flyfree.presentation.dto.BestOfferResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class FlightService(
    private val bestOfferRepository: BestOfferRepository,
    private val routeRepository: RouteRepository
) {

    @Transactional(readOnly = true)
    fun getBestOffer(from: String, to: String, date: LocalDate): BestOfferResponse {
        routeRepository.findByOriginAndDestinationAndTravelDate(
            from.uppercase(), to.uppercase(), date
        ).orElseThrow {
            RouteNotFoundException("Rota $from → $to em $date não está sendo monitorada. Cadastre primeiro via POST /api/v1/routes.")
        }

        val bestOffer = bestOfferRepository.findByRouteOriginAndRouteDestinationAndRouteTravelDate(
            from.uppercase(), to.uppercase(), date
        ).orElseThrow {
            OfferNotFoundException("Ainda não há ofertas para $from → $to em $date. Aguarde a próxima sincronização ou use POST /api/v1/sync/trigger.")
        }

        val flight = bestOffer.flightResult
        return BestOfferResponse(
            from = from.uppercase(),
            to = to.uppercase(),
            date = date,
            flightNumber = flight.flightNumber,
            departureTime = flight.departureTime,
            arrivalTime = flight.arrivalTime,
            price = bestOffer.price,
            stops = flight.stops,
            fareClass = flight.fareClass,
            lastUpdated = bestOffer.updatedAt
        )
    }
}
