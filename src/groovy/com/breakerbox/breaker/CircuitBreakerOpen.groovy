package com.breakerbox.breaker

import org.aopalliance.intercept.MethodInvocation
import org.apache.log4j.Logger

class CircuitBreakerOpen {

    Logger log = Logger.getLogger(getClass())

    def action(MethodInvocation method, CircuitBreaker breaker) {
        if (breaker.hasWaitTimeExceeded()) {
           breaker.halfOpen()
           breaker.action(method)
        } else {
            def message = "OPEN [${breaker?.name}]: Trying to execute opperation: ${method.getMethod()?.toGenericString()}. Wait Time remaining: [${breaker?.getWaitTime()} ms]"
            log.warn message
            throw new CircuitBreakerOpenException(message)
        }

    }

    def getName(){
        "Open"
    }

    def toggle(CircuitBreaker breaker) {
        log.warn "OPEN [${breaker.name}]: Manually closing the breaker"
        breaker.close()
    }

}
