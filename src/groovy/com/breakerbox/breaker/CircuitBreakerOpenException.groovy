package com.breakerbox.breaker

class CircuitBreakerOpenException extends RuntimeException{

    def CircuitBreakerOpenException() {
        super();
    }

    def CircuitBreakerOpenException(String s) {
        super(s);
    }

    def CircuitBreakerOpenException(String s, Throwable throwable) {
        super(s, throwable);
    }

    def CircuitBreakerOpenException(Throwable throwable) {
        super(throwable);
    }

}
