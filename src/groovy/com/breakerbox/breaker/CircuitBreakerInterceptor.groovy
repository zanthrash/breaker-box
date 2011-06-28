package com.breakerbox.breaker

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.apache.log4j.Logger
import java.util.concurrent.ConcurrentHashMap
import grails.util.GrailsConfig

class CircuitBreakerInterceptor implements MethodInterceptor{

    def log = Logger.getLogger(getClass())
    ConcurrentHashMap breakers = [:]

    def getBreaker(WithCircuitBreaker annotation){
        def cachedBreaker = breakers[annotation.value()]
        if (cachedBreaker){
            return cachedBreaker
        } else {
            def breaker = new CircuitBreaker(
                            name: annotation?.value(),
                            failureThreshold: getFailureThreshold(annotation),
                            timeUntilRetry: getTimeUntilRetry(annotation),
                            nonTrippingExceptions: annotation.nonTrippingExceptions().toList()
                        )
            breakers[breaker.name] = breaker
            return breaker
        }
    }

    def getFailureThreshold(annotation) {
        if (annotation.failureThreshold() == -1) {
            return GrailsConfig.config.breakerbox.failureThreshold ?: 9
        }
        return annotation.failureThreshold()
    }

    def getTimeUntilRetry(annotation) {
        if( annotation.timeUntilRetry() == -1) {
            return GrailsConfig.config.breakerbox.timeUntilRetry ?: 1000
        }
        return annotation.timeUntilRetry()
    }

    def toggleBreaker(breakerName) {
        def breaker = breakers[breakerName]
        breaker?.toggle()
    }

    Object invoke(MethodInvocation methodInvocation) throws Throwable{
        WithCircuitBreaker breakerAnnotation = methodInvocation.getMethod().getAnnotation(WithCircuitBreaker.class)
        if (breakerAnnotation){
           log.debug "breakerAnnotation: ${breakerAnnotation?.value()}"
           CircuitBreaker breaker = getBreaker(breakerAnnotation)
           return breaker.action(methodInvocation)
        } else {
           return methodInvocation.proceed() 
        }
    }

}
