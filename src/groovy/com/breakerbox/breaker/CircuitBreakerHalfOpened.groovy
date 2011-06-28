package com.breakerbox.breaker

import org.aopalliance.intercept.MethodInvocation
import org.apache.log4j.Logger

class CircuitBreakerHalfOpened {

    Logger log = Logger.getLogger(getClass())

    def action(MethodInvocation method, CircuitBreaker breaker) {
        def result
        try {
            result = method.proceed()
            breaker.close()
        } catch (Exception ex ){
            def message = "HALF OPEN [${breaker.name}]: Failure of opperation: ${method.getMethod()?.toGenericString()}."
            if(breaker.isNonTrippingException(ex)){
                message += " Caught a nonTrippingException."
                log.info message, ex
                throw ex
            } else {
                breaker.open()
                message += " Swiching to OPEN state"
                throw new CircuitBreakerOpenException(message, ex)
            }
        }

        return result
    }

    def getName(){
        "Half-Opened"
    }

    def toggle(CircuitBreaker breaker) {
        log.warn "HALF OPEN [${breaker.name}]: Manually closing the breaker"
        breaker.close()
    }

}
