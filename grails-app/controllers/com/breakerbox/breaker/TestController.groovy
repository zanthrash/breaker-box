package com.breakerbox.breaker

class TestController {

    def testerService

    def good = {
        render testerService.good() 
    }

    def fail = {
        def success = params?.success ?: false
        try{
           render testerService.failToOpenState(success)
        } catch (ex) {
           redirect(controller:"breaker", action:"index")
        }

    }
}
