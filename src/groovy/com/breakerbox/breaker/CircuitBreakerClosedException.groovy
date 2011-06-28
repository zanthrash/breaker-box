package com.breakerbox.breaker
class CircuitBreakerClosedException extends RuntimeException {

    def CircuitBreakerClosedException() {
        super()
    }

    def CircuitBreakerClosedException(String s) {
        super(s)
    }

    def CircuitBreakerClosedException(String s, Throwable throwable) {
        super(s, throwable)
    }

    def CircuitBreakerClosedException(Throwable throwable) {
        super(throwable)
    }

}
