package com.breakerbox.breaker

import grails.plugin.spock.UnitSpec
import org.aopalliance.intercept.MethodInvocation

class CircuitBreakerSpec extends UnitSpec {

    CircuitBreaker circuitBreaker
    MethodInvocation mockMethodInvocation
    def now
    int TIME_UNTIL_RETRY = 3000
    def savedMetaClass

    def setup() {
        savedMetaClass = [:]
        savedMetaClass[System] = System.metaClass

        def emc = new ExpandoMetaClass(System, true, true)
        emc.initialize()
        GroovySystem.metaClassRegistry.setMetaClass(System, emc)

        mockMethodInvocation = Mock(MethodInvocation)
        circuitBreaker = new CircuitBreaker(name:"TEST_BREAKER", failureCount:0, failureThreshold:3, timeUntilRetry:TIME_UNTIL_RETRY)
        now = new Date().getTime()
        System.metaClass.static.currentTimeMillis = { now }
    }

    def cleanup() {
        savedMetaClass.each {clazz, metaClass ->
            GroovySystem.metaClassRegistry.removeMetaClass(clazz)
            GroovySystem.metaClassRegistry.setMetaClass(clazz, metaClass) 
        }
    }

    def "create circuit breaker in default - closed - state"() {

        when: "a circuit breaker if first created with defauld closed state"
            circuitBreaker.close()

        then: "the initail state should be closed"
            assert circuitBreaker.state instanceof CircuitBreakerClosed
    }

    def "a closed circuit breaker should go into a open state when the failure threshold is met or exceeded"() {

        when: "the circuit breaker is in a closed state with a failure threshold of 3"
        and: "we add 3 failurs to the breaker"
            circuitBreaker.addFailure()
            circuitBreaker.addFailure()
            circuitBreaker.addFailure()
        then: "the failure count should be 3"
            circuitBreaker.failureCount == 3

        and: "the breaker state should be changed to 'open'"
            assert circuitBreaker.state instanceof CircuitBreakerOpen
        and: "the breaker should have a last opened timestamp"
            circuitBreaker.lastOpenedTime == now
    }

    def "a closed circuit breaker should stay in closed state when the failure count is under the threshold"() {
        when: "the circit breaker is closed and there is on failure"
            circuitBreaker.addFailure()

        then: "failure count should be 1"
            circuitBreaker.failureCount == 1

        and: "breaker state should still be closed"
            assert circuitBreaker.state instanceof CircuitBreakerClosed

    }

    def "a closed breaker should stay closed if action method is called without insident"() {
        when: "the breaker is in closed state"
            circuitBreaker.close()

        and: "call the action method with no exceptions thrown"
            circuitBreaker.action(mockMethodInvocation)

        then: "the breaker should remain in the closed state"
            assert circuitBreaker.state instanceof CircuitBreakerClosed

        and: "the last opend date should be null"
            circuitBreaker.lastOpenedTime == null

        and: "proceed method is called once"
            1 * mockMethodInvocation.proceed()
    }

    def "a closed breaker with no nonTrippingExceptions should increment failureCount if an exception is thrown"() {

        when: "the breaker is closed"
            circuitBreaker.close()

        and: "the action method is called and an exception is thrown"
            circuitBreaker.action(mockMethodInvocation)

        then: "the failureCount should be one"
            circuitBreaker.failureCount == 1

        and: "the state shold still be closed"
            assert circuitBreaker.state instanceof CircuitBreakerClosed

        and: "the exception is thron on the action method call"
            1 * mockMethodInvocation.proceed() >> {throw new Exception()}

        and: "a CircuitBreakerException should be thrown"
            CircuitBreakerClosedException e = thrown()
            e.message == "CLOSED [TEST_BREAKER]: Failure of opperation: null. Incrementing failureCount to [1 of 3]"
            e.cause.class == java.lang.Exception
    }

    def "a closed breaker should not increment failure count if a nonTripping exception is thrown"() {
        when: "the breaker is closed"
            circuitBreaker.close()
            circuitBreaker.nonTrippingExceptions = [java.lang.IllegalArgumentException, java.lang.ArithmeticException]

        and: "the action method is called and throws one of the nonTripping exceptions"
            circuitBreaker.action(mockMethodInvocation)

        then: "failure count should still be zero"
            circuitBreaker.failureCount == 0

        and: "orgiginal exception should be rethrown"
            IllegalArgumentException e = thrown()

        and: "mock the trowing of a nonTrippingException"
            1 * mockMethodInvocation.proceed() >> { throw new IllegalArgumentException()}
    }

    def "a open circuit breaker should stay open if the time until retry threshold has not been met"() {

        when: "the breaker is open"
            circuitBreaker.open()

        and: "we call the action opperation on the circuit breaker before the try again threshold has been reached"
            def twoSecondsLatter = now + 2000
            System.metaClass.static.currentTimeMillis = {twoSecondsLatter}
            circuitBreaker.action(mockMethodInvocation)

        then: "the breaker state should still be open"
            assert circuitBreaker.state instanceof CircuitBreakerOpen 
            circuitBreaker.lastOpenedTime == now
            TIME_UNTIL_RETRY - (twoSecondsLatter - circuitBreaker.lastOpenedTime) == 1000

        and: "a CircuitBreaker exception should be thrown"
            CircuitBreakerOpenException ex = thrown()
            ex.message == "OPEN [TEST_BREAKER]: Trying to execute opperation: null. Wait Time remaining: [1000 ms]"
    }


    def "a open circuit breaker should open half way if the wait time has expried"() {

        when: "the breaker is in an open state"
            circuitBreaker.open()

        and: "we call the action method after the try again threashold has been reached"
            def fiveSecondslatter = now + 5000
            System.metaClass.static.currentTimeMillis = {fiveSecondslatter}
            circuitBreaker.action(mockMethodInvocation)

        then: "with the successfull call on the action method the breaker should reset to closed"
            assert circuitBreaker.state instanceof CircuitBreakerClosed

        and: "the lastOpenTime shold be reset to null"
            circuitBreaker.lastOpenedTime == null

        and: "proceed method is called once"
            1 * mockMethodInvocation.proceed()

    }


    def "a half opened circuit breaker should move to the closed state if action executes with out exception"() {

        when: "the breaker is in a half opened state"
            circuitBreaker.halfOpen()

        and: "we call the action method and no exceptions are thrown"
            def result = circuitBreaker.action(mockMethodInvocation)

        then: "breaker should be moved to closed state"
            assert circuitBreaker.state instanceof CircuitBreakerClosed

        and: "the last opened time should be null"
            circuitBreaker.lastOpenedTime == null

        and: "proceed method is called once without an error"
            1 * mockMethodInvocation.proceed() >> "FOO"
            result == "FOO"
    }

    def "a half opened circuit breaker should switch back to open state if the action executes with an exception this is not in nonTripable list"() {
        setup:
            def systemTime = new Date().getTime()
            System.metaClass.static.currentTimeMillis = {systemTime}

        when: "the breaker is in a half opend state"
            circuitBreaker.halfOpen()

        and: "we call the action method and get an exception thrown"
            def result = circuitBreaker.action(mockMethodInvocation)

        then: "breaker should return to open state"
            assert circuitBreaker.state instanceof CircuitBreakerOpen

        and: "the last open time shold be reset"
            circuitBreaker.lastOpenedTime == systemTime

        and: "the proceed method throws and error"
            1 * mockMethodInvocation.proceed() >> { throw new IllegalArgumentException() }

        and: "we should expect the origial exception to be thrown"
            CircuitBreakerOpenException ex = thrown()
    }

    def "a half opened circuit breaker that throws exception that's in nonTrippable list should say in half open and throw original ex"(){

        when: "the breaker is half open"
            circuitBreaker.halfOpen()
        and: "has a nonTripableException list"
            circuitBreaker.nonTrippingExceptions = [java.lang.ArithmeticException, java.lang.IllegalArgumentException]

        and: "we call the action method and get a thrown exception"
            def result = circuitBreaker.action(mockMethodInvocation)

        then: "the breaker should stay in the half open state"
            assert circuitBreaker.state instanceof CircuitBreakerHalfOpened

        and: "the prossess method throws a error that is in the nonTrippingException list"
            1 * mockMethodInvocation.proceed() >> { throw new IllegalArgumentException()}

        and: "verify that we get the original exception re-thrown"
            IllegalArgumentException ex = thrown()
    }

}


