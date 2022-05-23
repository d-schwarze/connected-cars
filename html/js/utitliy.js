var UTIL = (function() {
    
    /*
     * Check if the input value matches the email pattern.
     * input: jquery object
     */
    function matchesEmailPattern(input) {
        
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        
        var matches = re.test(input.val());
        
        if(!matches) {
            
            $(input).prev(".validation_message").html("Falsches Format");
            
        } else {
            
            $(input).prev(".validation_message").html("");
            
        }
        
        return matches;
        
    }
    
    /*
     * Check if the input value matches the password requirements.
     * input: jquery object
     */
    function isPasswordValid(input) {
        
        if(input.val() != undefined) {
            
            var pwWithoutSpaces = input.val().replace(/ /g,"");
            
            if(pwWithoutSpaces.length > 0) {
                
                $(input).prev(".validation_message").html("");
                
                return true;
                
            }
            
        }
        
        $(input).prev(".validation_message").html("Das Passwort darf nicht leer sein.");
        
        return false;
        
    }
    
    /*
     * Check if the input value matches the normal requirements.
     * input: jquery object
     */
    function isInputValid(input) {
        
        if(input.val() != undefined) {
            
            var pwWithoutSpaces = input.val().replace(/ /g,"");
            
            if(pwWithoutSpaces.length > 0) {
                
                $(input).prev(".validation_message").html("");
                
                return true;
                
            }
            
        }
        
        $(input).prev(".validation_message").html("Das Passwort darf nicht leer sein.");
        
        return false;
        
    }
    
    /*
     * Show a message as overlay dialog.
     * title: of the message
     * msg: displayed message
     * duration: time until the car is hided (in millis)
     */
    function showMessage(title, msg, duration) {
        
        var msg = $('<div><img src="img/menu.svg"><h3>' + title + '</h3><p>' + msg + '</p><div>').addClass("msg_dialog").appendTo("body");
        
        msg.delay(duration).queue(function() { $(this).remove(); });
        msg.first("img").click(function() {
            
            msg.remove();
            
        });
        
    }
    
    /*
     * Get sessionid of the current user from the coockie.
     */
    function getSessionID() {
        
        return Cookies.get("sessionID");
        
    }
    
    /*
     * Get carid of the current car from the coockie.
     */
    function getCarID() {
        
        return Cookies.get("carID");
        
    }
    
    /*
     * JSON date from Java-Response to a string.
     */
    function dateToString(date) {
        
        return date.day + "." + date.month + "." + date.year;
        
    }
    
    /*
     * JSON dateTime from Java-Response to a string.
     */
    function dateTimeToString(dateTime) {
        
        return dateTime.day + "." + dateTime.month + "." + dateTime.year + " " + dateTime.hour + ":" + dateTime.minute;
        
    }
    
    /*
     * Get the place name of a location.
     * loation: { lat: .., lng: ... }
     * reponse(place): called when results are received
     */
    function getPlace(location, response) {
        
        var geocoder = new google.maps.Geocoder;
        
        geocoder.geocode({'location': location}, function(results, status) {
            
            if (status === 'OK') {

                if (results[0]) {
                    
                    response(results[0].formatted_address);

                } else {

                    response("");

                }
            } else {

                response("");

            }
        });
        
    }
           
    /*
     * Crate a new timestamp.
     */
    function getTimeStamp() {
            
        var timestamp = new Date();
            
        return {

            day: timestamp.getDate(),
            month: timestamp.getMonth(),
            year: timestamp.getFullYear(),
            hour: timestamp.getHours(),
            minute: timestamp.getMinutes(),
            second: timestamp.getSeconds()

        }
            
    }
        
    /*
     * Get LatLng from a place.
     * place: placename
     * response(location): called on results received
     */
    function placeToLatLng(place, response) {
        
        var geocoder = new google.maps.Geocoder;
        
        geocoder.geocode( { address : place }, function( results, status ) {
           
            if( status === "OK" ) {

                //In this case it creates a marker, but you can get the lat and lng from the location.LatLng
                response(results[0].geometry.location);
                
                
            } else {
                
                response();
            }
            
        });

    }
    
    /*
     * Get a google maps route from to locations.
     * response(direction): called on results received
     */
    function getDirection(start, destination, response) {
        
        var directionsService = new google.maps.DirectionsService();
        
        directionsService.route({
            origin: start,
            destination: destination,
            travelMode: 'DRIVING'
        }, function(result, status) {
            
            if (status == 'OK') {
                
                response(result);
                
            } else {
                
                response();
                
            }
            
        });
        
    }
    
    /*
     * Get gas stations in the radius of a loation.
     * map: GoogleMaps object
     */
    function getGasStations(location, radius, map, response) {
        
        var service = new google.maps.places.PlacesService(map);
        
        service.nearbySearch({
            location: location,
            radius: radius,
            type: ['gas_station']
        }, function(results, status) {
            
            if (status == google.maps.places.PlacesServiceStatus.OK) {

                response(results);

            } else {

                response();

            }
        });
        
    }
    
    /*
     * Get charging stations in the radius of a loation.
     */
    function getChargingStations(location, radius, response) {
        
        $.get("https://api.openchargemap.io/v2/poi/?output=json&latitude=" + location.lat + "&longitude=" + location.lng + "&distance=" + radius + "&distanceunit=KM&maxresults=20&opendata=true", function(json) {
           
            response(json);
            
        });
        
    }
    
    return {
        
        matchesEmailPattern: matchesEmailPattern,
        isPasswordValid: isPasswordValid,
        showMessage: showMessage,
        isInputValid: isInputValid,
        getSessionID: getSessionID,
        dateToString: dateToString,
        getPlace: getPlace,
        placeToLatLng: placeToLatLng,
        dateTimeToString: dateTimeToString,
        getTimeStamp: getTimeStamp,
        getCarID: getCarID,
        getCharginStations: getChargingStations,
        getDirection: getDirection,
        getGasStations: getGasStations
        
    };
    
})();