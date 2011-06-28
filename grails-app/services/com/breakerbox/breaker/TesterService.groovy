package com.breakerbox.breaker

class TesterService {

    static transactional = true

    @WithCircuitBreaker("GOOD")
    def good() {
        return "good"
    }

    @WithCircuitBreaker(value = "FAIL_TO_OPEN_STATE",
        failureThreshold = 1,
        timeUntilRetry = 10,
        nonTrippingExceptions = [java.lang.IllegalAccessError, java.lang.ArithmeticException]
    )
    def failToOpenState(tryWithSuccess) {
        if (tryWithSuccess) {
            return "SUCCESS"
        } else {
           throw new IllegalStateException()
        }
    }
}
