package com.breakerbox.breaker

import org.aopalliance.intercept.MethodInvocation
import org.apache.log4j.Logger

class CircuitBreakerClosed {

    def log = Logger.getLogger(getClass())

    def action(MethodInvocation method, CircuitBreaker breaker) {
        def result
        try {
            result = method.proceed()
        } catch (Throwable ex) {
            def message = "CLOSED [${breaker.name}]: Failure of opperation: ${method.getMethod()?.toGenericString()}."
            if (breaker.isNonTrippingException(ex)) {
                message += " Caught a nonTrippingException."
                log.info message, ex
                throw ex
            } else {
                breaker.addFailure()
                message += " Incrementing failureCount to [${breaker.failureCount} of ${breaker.failureThreshold}]"
                log.warn  message, ex
                throw new CircuitBreakerClosedException(message, ex)
            }
        }
        result
    }

    def getName(){
        "Closed"
    }

    def toggle(CircuitBreaker breaker) {
        log.warn "CLOSED [${breaker.name}]: Manually opening the breaker"
        breaker.open()
    }
    
}
