package com.breakerbox.breaker

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD])
public @interface WithCircuitBreaker {

    int failureThreshold() default -1
    int timeUntilRetry() default -1
    String value() //This is the name of the circuit breaker
    Class[] nonTrippingExceptions()  default []

}
