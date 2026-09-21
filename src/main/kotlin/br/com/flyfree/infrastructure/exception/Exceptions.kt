package br.com.flyfree.infrastructure.exception

class RouteAlreadyExistsException(message: String) : RuntimeException(message)
class RouteNotFoundException(message: String) : RuntimeException(message)
class OfferNotFoundException(message: String) : RuntimeException(message)
