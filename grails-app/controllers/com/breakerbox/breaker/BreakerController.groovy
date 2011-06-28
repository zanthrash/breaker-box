package com.breakerbox.breaker

class BreakerController {

    def circuitBreakerInterceptor

    static defaultAction = 'list'

    def index = {
        redirect action: 'list'
    }

    def list = {
        def breakers = circuitBreakerInterceptor.breakers?.values()
        render(view:'breakers', model:[breakers:breakers])
    }

    def toggle = {
        def breakerName = params?.id ?: "NO_NAME"
        circuitBreakerInterceptor.toggleBreaker(breakerName)
        redirect action: 'index'
    }
}
