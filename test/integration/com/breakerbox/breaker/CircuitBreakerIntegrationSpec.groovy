package com.breakerbox.breaker

import grails.plugin.spock.IntegrationSpec

class CircuitBreakerIntegrationSpec extends IntegrationSpec {
    def testerService
    def circuitBreakerInterceptor

    def cleanup(){
        circuitBreakerInterceptor.breakers = [:]
    }
    
    def "the service should not be null"(){
        expect:
            testerService != null
            circuitBreakerInterceptor != null
    }

    def "test the good method"(){
        when: "we call the method on the service"
            def result = testerService.good()

        then: "we check the result"
            result == "good"

        and: "interceptor should have one circut breaker setup"
            def breakerMap = circuitBreakerInterceptor.breakers
            breakerMap.size() == 1

            def breaker = circuitBreakerInterceptor.breakers['GOOD']
            assert breaker.state instanceof CircuitBreakerClosed
    }

    def "test the fail to open state method"() {
        when: "we call the method once"
            def result = testerService.failToOpenState(false)

        then: "we should get a CircuitBreakerClosedException thrown"
            CircuitBreakerClosedException ex = thrown()

        and: "the breaker should be in the open state now"
            def breakerMap = circuitBreakerInterceptor.breakers
            breakerMap.size() == 1

            def breaker = breakerMap['FAIL_TO_OPEN_STATE']
            assert breaker.state instanceof CircuitBreakerOpen
    }

    def "test the timeout retry"() {
        when: "we call the method once"
            def result = testerService.failToOpenState(false)
        
        then: "we should get a CircuitBreakerClosedExceptioin thrown"
            CircuitBreakerClosedException ex = thrown()

        and: "we should be in the 'open' state"
            assert circuitBreakerInterceptor.breakers['FAIL_TO_OPEN_STATE'].state instanceof CircuitBreakerOpen

        when: "we call the same method after after the try again time has expired"
            Thread.sleep 5000
            result = testerService.failToOpenState(true)

        then: "no exception is thrown"
            notThrown(Exception)

        and: "we get a valid result back"
            result == "SUCCESS"
    }
}
