var Webservice = (function() {
    
    var url = "http://localhost:8080/rest/";
    
    //All status codes from the java code (MessageType.class)
    var Status = {
        
        OK: 0,
        AUTHENTICATION_FAILED: 1,
        FATAL_ERROR: 2,
        FAILED: 3,
        UP_TO_DATE: 4,
        DATABASE: 5,
        NOT_FOUND: 6,
        ALREADY_DONE: 7,
        INVAILD_DATA: 8,
        AUTHORIZATION_FAILED: 9
        
    };
    
    /*
     * Status listener to listen everytime for a specific statustype
     * status: statusnumber
     * method: called on the status
     * target: "GET", "POST", "BOTH" -> webservice call
     */
    function StatusListener(status, method, target) {
        
        this.status = status;
        
        this.method = method;
        
        this.target = target;
        
    }
    
    var statusListener = [];
    
    /*
     * Get data from the webservice.
     * path: path to the webservice method (for example: "cars/all/db?sessionid=...")
     * response: is called whene data was received
     */
    function get(path, response) {
        
        $.get(url + path, function(value) {
            
            //Parse the ResponseWS.class
            var json = JSON.parse(value);
                    
            if(Status.FATAL_ERROR == json.status) {
                
                onException(json.data);
                
            } else if(Status.AUTHENTICATION_FAILED == json.status) {
                
                onAuthenticationFailed();
                
            } else {
                
                var statusListenerCalled = false;
                
                //Check for statuslisteners
                for(var i in statusListener) {

                    if(statusListener[i].status == json.status && (statusListener[i].target == "GET" || statusListener[i] == "BOTH")) {

                        statusListener[i].method(json.data);

                        statusListenerCalled = true;

                        break;

                    }

                }
                
                //Only response if no status listener was called
                if(!statusListenerCalled) {

                    if(response !== undefined)
                        response(json);

                }
                    
            }
            
        });
        
    }
    
    /*
     * Get data to the webservice.
     * path: path to the webservice method (for example: "route/add?carid=..&sessionid=...")
     * data: which is send to the webservice
     * response: is called whene data was received
     */
    function post(path, data, response) {
        
        $.post(url + path, JSON.stringify(data), function(value) {
            
            //Parse the ResponseWS.class -> only status for "POST"
            var json = JSON.parse(value);
            
            if(Status.FATAL_ERROR == json.status) {
                
                onException(json.data);
                
            } else if(Status.AUTHENTICATION_FAILED == json.status) {
                
                onAuthenticationFailed();
                
            } else {
                
                var statusListenerCalled = false;

                //Check for statuslisteners
                for(var i in statusListener) {

                    if(statusListener[i].status == json.status && (statusListener[i].target == "POST" || statusListener[i] == "BOTH")) {

                        statusListener[i].method(json.data);

                        statusListenerCalled = true;

                        break;

                    }

                }

                //Only response if no status listener was called
                if(!statusListenerCalled) {

                    if(response !== undefined)
                        response(json);

                }
                    
            }
            
            
        });
        
    }
    
    this.onException = function(exceptionMsg) {
        
        alert("Exception: " + exceptionMsg);
        
    };
    
    this.onAuthenticationFailed = function() {
        
        alert("Your are not to allowed to access that data.");
        
    };
    
    function setOnException(method) {
        
        onException = method;
        
    }
    
    function setOnAuthenticationFailed(method) {
        
        onAuthenticationFailed = method;
        
    }
    
    function addStatusListener(status, method, target) {
        
        for(var i in statusListener) {
            
            if(statusListener[i].status == status) {
                
                statusListener.splice(i, 1);
                
            }
            
        }
        
        statusListener.push(new StatusListener(status, method, target));
        
        
    }
    
    return {
        
        get: get,
        post: post,
        setOnException: setOnException,
        setOnAuthenticationFailed: setOnAuthenticationFailed,
        Status: Status,
        addStatusListener: addStatusListener
        
    };
    
})();