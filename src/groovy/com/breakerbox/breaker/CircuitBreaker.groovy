package com.breakerbox.breaker

import org.aopalliance.intercept.MethodInvocation
import org.apache.log4j.Logger

class CircuitBreaker {

    Logger log = Logger.getLogger(getClass())

    Integer failureThreshold
    Integer failureCount = 0
    Integer timeUntilRetry
    Long lastOpenedTime
    String name
    def nonTrippingExceptions = []

    def state = new CircuitBreakerClosed()

    def addFailure(){
        failureCount++
        nextState()
    }

    def nextState(){
        if(failureCount >= failureThreshold) {
            state = new CircuitBreakerOpen()
            lastOpenedTime = System.currentTimeMillis()
            log.error "Breaker: [${name} has reached its failure threshold of ${failureThreshold}. Switching to OPEN state. Will reset in [${timeUntilRetry} ms]"
        }
    }

    def toggle() {
        state.toggle(this)
    }

    def open() {
        state = new CircuitBreakerOpen()
        lastOpenedTime = System.currentTimeMillis()
    }

    def halfOpen() {
        state = new CircuitBreakerHalfOpened()
        lastOpenedTime = null
    }

    def close() {
        state = new CircuitBreakerClosed()
        failureCount = 0
        lastOpenedTime = null
    }

    def action(MethodInvocation method) {
        state.action(method, this)
    }

    def hasWaitTimeExceeded() {
        def currentTime = System.currentTimeMillis()
        currentTime - timeUntilRetry > lastOpenedTime
    }

    def getWaitTime() {
        timeUntilRetry - (System.currentTimeMillis() - lastOpenedTime)    
    }

    def isNonTrippingException(Throwable ex) {
        for (Class throwable : nonTrippingExceptions) {
            if(throwable.isAssignableFrom(ex.getClass())){
                return true
            }
        }
        return false
    }
}
