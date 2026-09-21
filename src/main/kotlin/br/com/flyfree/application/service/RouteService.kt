package br.com.flyfree.application.service

import br.com.flyfree.domain.entity.Route
import br.com.flyfree.domain.repository.RouteRepository
import br.com.flyfree.infrastructure.exception.RouteAlreadyExistsException
import br.com.flyfree.infrastructure.exception.RouteNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class RouteService(private val routeRepository: RouteRepository) {

    @Transactional
    fun register(from: String, to: String, date: LocalDate, returnDate: LocalDate? = null): Route {
        routeRepository.findByOriginAndDestinationAndTravelDate(
            from.uppercase(), to.uppercase(), date
        ).ifPresent {
            throw RouteAlreadyExistsException("Rota $from → $to em $date já está sendo monitorada.")
        }

        return routeRepository.save(
            Route(
                origin = from.uppercase(),
                destination = to.uppercase(),
                travelDate = date,
                returnDate = returnDate
            )
        )
    }

    @Transactional(readOnly = true)
    fun listAll(): List<Route> = routeRepository.findAll()

    @Transactional
    fun deactivate(id: Long): Route {
        val route = routeRepository.findById(id)
            .orElseThrow { RouteNotFoundException("Rota com id $id não encontrada.") }
        return routeRepository.save(route.copy(active = false))
    }

    @Transactional(readOnly = true)
    fun findActiveRoutes(): List<Route> = routeRepository.findAllByActiveTrue()
}
