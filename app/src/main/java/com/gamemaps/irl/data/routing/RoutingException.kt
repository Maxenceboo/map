package com.gamemaps.irl.data.routing

/** Le moteur de routage a répondu mais sans itinéraire exploitable. */
class RoutingException(message: String) : Exception(message)
