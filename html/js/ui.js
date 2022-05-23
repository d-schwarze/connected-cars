var UI = (function() {
    
    var Cars = (function() {
        
        //Google Maps for the car location
        var position_map;
            
        //Marker for the car location
        var car_marker;

        //Google Map for the navi map
        var navigation_map;

        //Google Maps Direction Service
        var directionsService;

        //Google Maps Direction Display
        var directionsDisplay;
        
        //All display station (gas stations or charging stations) on the positin_map
        var stations = [];
        
        /* 
         * Set the first car of the user .
         */
        function setFirstCar() {
            
            Webservice.get("cars/get?sessionid=" + UTIL.getSessionID(), function(json) {
                    
                if(json.status == Webservice.Status.UP_TO_DATE) {
                    //Data is from a simulated car
                    setCarData(json.data, true);
                    

                } else if(json.status == Webservice.Status.DATABASE) {
                    //Data is from the database
                    setCarData(json.data, false);

                } else if(json.status == Webservice.Status.NOT_FOUND) {
                    //User has no cars
                    $("#std_cells").hide();

                    $("#no_cars").show();

                } else {

                    UTIL.showMessage("Unhandled status", json.status, 10000);

                }

            });
            
        }
        
        /*
         * Set a car by its id.
         */
        function setCar(carid) {
            
            Webservice.get("cars/get?carid=" + carid + "&sessionid=" + UTIL.getSessionID(), function(json) {
                
                if(json.status == Webservice.Status.UP_TO_DATE) {
                    //Data is from a simulated car
                    setCarData(json.data, true);

                } else if(json.status == Webservice.Status.DATABASE) {
                     //Data is from the database
                    setCarData(json.data, false);

                } else if(json.status == Webservice.Status.NOT_FOUND) {
                    //Car was not found
                    $("#std_cells").hide();

                    $("#no_cars").show();

                } else {

                    UTIL.showMessage("Unhandled status", json.status, 10000);

                }

            });
                
            
        }
        
        /*
         * Display the car data
         */
        function setCarData(car, upToDate) {
            
            $("#std_cells").show();

            $("#no_cars").hide();
            
            //Set current carid
            Cookies.set("carID", car.carID);
            
            //Display status
            setStatus(car.status);
            
            //Display routes
            setRoutes(car.navigationSystem.routes);
            
            //Display infos
            setInfos(car.brand, car.type, car.fuellevel, car.mileage, car.nextInspection, car.electrical);
            
            
            if(car.fuellevel > 0.1) {
                //Set car position, fuellevel: enough
                setPosition(car.location, false);
                
            } else {
                //Set car position, fuellevel: too low -> add stations
                setPosition(car.location, true, car.electrical);
            }
            
            //Display current route
            setCurrentRoute(car.navigationSystem.currentRoute);
            
            if(upToDate) {
                
                $("#new_route_div").show();
                $("#db_data").hide();
                
                
            } else {
                //Remove navi functions
                
                //Create new route -> not available
                $("#new_route_div").hide();
                //Functions of the routes -> not available
                $("#routes .std_button").remove();
                $("#db_data").show();
                
            }
            
        }
        
        //Set current route 
        function setCurrentRoute(route) {
            
            if(route != undefined) {
                
                var start = route.start;
                
                var destination = route.destination;
                
                UTIL.getPlace(route.start, function(place) {
                        
                    if(place != "") {

                        start = place;
 
                    } else {
                        //If no placename was found -> set latlng
                        start = route.start.lat + " | " + route.start.lng;

                    }

                    UTIL.getPlace(route.destination, function(place) {

                        if(place != "") {

                            destination = place;

                        } else {
                            //If no placename was found -> set latlng
                            destination = route.destination.lat + " | " + route.destination.lng;

                        }
                        
                        $("#current_route_p").html(start + " - " + destination);
                        
                    });
                });
                
                //Show the current route on the map
                UTIL.getDirection(route.start, route.destination, function(direction) {
                    
                    if(direction != undefined) {
                        
                        directionsDisplay.setDirections(direction);
                        
                    } else {
                        
                        UTIL.showMessage("Aktuelle Route", "Aktuelle Route konnt nicht eingezeichnet werden.", 10000);
                        
                    }
                    
                });
                
            } else {
                
                $("#current_route_p").html("Keine Route ausgewählt.");
                
            }
                
        }
        
        //Display routes
        function setRoutes(routes) {
                
            $("#routes tbody").html("");

            var i = 0;
            
            //Recursive pattern
            if(i < routes.length) {
                
                //Add new route
                addRoutes(routes, i);

            }

        }
        
        //Add new route
        function addRoutes(routes, i) {

            var start;

            var destination;
            
            var route = routes[i];

            UTIL.getPlace(route.start, function(place) {

                if(place != "") {

                    start = place;

                } else {

                    start = route.start.lat + " | " + route.start.lng;

                }

                UTIL.getPlace(route.destination, function(place) {

                    if(place != "") {

                        destination = place;

                    } else {

                        destination = route.destination.lat + " | " + route.destination.lng;

                    }

                    var newRoute = $('<tr><td>' + start + " - " + destination + '</td><td>' + UTIL.dateTimeToString(route.creationTime) + '</td><td class="lt_route_options"><button class="std_button send"><img src="img/send.svg"></button><button class="std_button remove"><img src="img/trash-2.svg"></button></td></tr>').appendTo("#routes tbody");

                    //Add on click functin for set current route to this button
                    newRoute.find(".std_button.send").first().click(function() {
                        
                        
                        Webservice.get("cars/route/current?carid=" + UTIL.getCarID() + "&routeid=" + route.id + "&sessionid=" + UTIL.getSessionID(), function(json) {

                            if(json.status == Webservice.Status.OK) {

                                UTIL.showMessage("Route", "Aktuelle Route wurde geändert.", 10000);

                                setCar(UTIL.getCarID());
                                
                            } else if(json.status == Webservice.Status.FAILED) {

                                UTIL.showMessage("Route", "Aktuelle Route konnte nicht geändert. Auto nicht erreichbar.", 10000);

                            } else if(json.status == Webservice.Status.NOT_FOUND) {

                                UTIL.showMessage("Route", "Auto wurde nicht gefunden.", 10000);

                            } else {

                                UTIL.showMessage("Unhandled status", json.status, 10000);

                            }

                        });

                    });

                    //Add on click function for remove route button
                    newRoute.find(".std_button.remove").first().click(function() {

                        Webservice.get("cars/route/remove?carid=" + UTIL.getCarID() + "&routeid=" + route.id + "&sessionid=" + UTIL.getSessionID(), function(json) {

                            if(json.status == Webservice.Status.OK) {

                                UTIL.showMessage("Route", "Route wurde entfernt.", 10000);

                                setCar(UTIL.getCarID());

                            } else if(json.status == Webservice.Status.FAILED) {

                                UTIL.showMessage("Route", "Route konnte nicht entfernt werden. Auto nicht erreichbar.", 10000);

                            } else if(json.status == Webservice.Status.NOT_FOUND) {

                                UTIL.showMessage("Route", "Auto wurde nicht gefunden.", 10000);

                            } else {

                                UTIL.showMessage("Unhandled status", json.status, 10000);

                            }

                        });

                    });

                    i++;

                    if(i < routes.length) {

                        addRoutes(routes, i, UTIL.getCarID());

                    }
                });

            });

        }

        //Display the status
        function setStatus(status) {

            
            $("#status td").hide();
            
            for(var domId in status) {

                setSingleStatus(domId, status[domId]);

            }

        }

        //Display the main infos
        function setInfos(brand, type, fuelLevel, mileage, nextInspection, electrical) {

            $("#car_name").html(brand + ", " + type);
            $("#mileage").html(mileage + "km");
            $("#nextInspection").html(UTIL.dateToString(nextInspection));
            $("#fuellevel").html((fuelLevel * 100) + "%");
            
            if(electrical) {
                
                $("#fuellevel").next(".imp_data_name").html("Batteriestand");
                
            } else {
                
                $("#fuellevel").next(".imp_data_name").html("Tankfüllung");
                
            }
        }

        //Set the position of the current car
        function setPosition(position, lowFuelLevel, electrical) {
            
            //Add marker and zoom to the position
            car_marker.setPosition(position);
            position_map.setCenter(position);
            
            //Remove stations
            clearStations();
            
            //Add statins if the fuellevel is too low
            if(lowFuelLevel) {
                
                if(!electrical) {
                    
                    UTIL.getGasStations(position, "10000", position_map, function(results) {

                        if(results != undefined) {

                            $("#critcal_fuellevel").html("Tank ist fast leer. Clicken sie auf eine Tankstelle um direkt eine Route zu erstellen.")

                            for(var i in results) {

                                addStation(results[i].geometry.location, position);

                            }
                        }
                    });
                    
                } else {
                    
                    UTIL.getCharginStations(position, 10, function(stations) {
                       
                        if(stations != undefined) {
                            
                            $("#critcal_fuellevel").html("Batterie ist fast leer. Clicken sie auf eine Ladestation um direkt eine Route zu erstellen.");
                            
                            for(var i in stations) {
                                
                                addStation({lat: stations[i].AddressInfo.Latitude, lng: stations[i].AddressInfo.Longitude}, position);
                                
                            }
                            
                        } else {
                            
                            UTIL.showMessage("Ladestationen", "Ladestationen konnten nicht gefunden werden.");
                            
                        }
                        
                    });
                    
                }
            } else {
                
                $("#critcal_fuellevel").html("");
                
            }

        }
        
        //Add station to position_map
        function addStation(stationLocation, carLocation) {
            
            var station = new google.maps.Marker({
                position: stationLocation,
                map: position_map,
                icon:"img/gas-station.svg"
            });


            //Staion on click -> add new route from car location to station
            station.addListener('click', function() {


                Webservice.post("cars/route/add?carid=" + UTIL.getCarID() + "&sessionid=" + UTIL.getSessionID(), { start: carLocation,  destination: this.position, creationTime: UTIL.getTimeStamp()}, function(json) {

                    if(json.status == Webservice.Status.OK) {

                        UTIL.showMessage("Route", "Route wurde hinzugefügt.", 10000);

                        setCar(UTIL.getCarID());

                    } else if(json.status == Webservice.Status.FAILED) {

                        UTIL.showMessage("Route", "Route konnte nicht hinzugefügt werden. Auto nicht erreichbar.", 10000);

                    } else if(json.status == Webservice.Status.NOT_FOUND) {

                        UTIL.showMessage("Route", "Auto wurde nicht gefunden.", 10000);

                    } else {

                        UTIL.showMessage("Unhandled status", json.status, 10000);

                    }


                });

            });

            stations.push(station);
            
        }
        
        //Remove all stations from map
        function clearStations() {
            
            for(var i in stations) {
                
                stations[i].setMap(null);
                
            }
            
            stations = [];
            
        }

        //Add on component of the status
        function setSingleStatus(domId, status) {

            switch(status) {

                case "OK":
                    $("#" + domId).show();
                    $("#" + domId).next(".status_value").show();
                    $("#" + domId).next(".status_value").removeClass("sufficient ok critical").addClass("ok").html("OK");
                    break;
                case "SUFFICIENT":
                    $("#" + domId).show();
                    $("#" + domId).next(".status_value").show();
                    $("#" + domId).next(".status_value").removeClass("sufficient ok critical").addClass("sufficient").html("AUSREICHEND");
                    break;
                case "CRITICAL":
                    $("#" + domId).show();
                    $("#" + domId).next(".status_value").show();
                    $("#" + domId).next(".status_value").removeClass("sufficient ok critical").addClass("critical").html("KRITISCH");    
                    break;
                default:
                    //Electrical
                    $("#" + domId).hide();
                    $("#" + domId).next(".status_value").hide();
                    break;


            }

        }
        
        //Initialize the Goolge Maps stuff
        function initializeMaps() {
            
            directionsService = new google.maps.DirectionsService;
                
            position_map = new google.maps.Map(document.getElementById('position_map'), {
              center: {lat: -34.397, lng: 150.644},
              zoom: 8
            });

            car_marker = new google.maps.Marker({position: {lat: -34.397, lng: 150.644},map: position_map});
            

            navigation_map = new google.maps.Map(document.getElementById('navigation_map'), {
              center: {lat: -34.397, lng: 150.644},
              zoom: 8
            });

            directionsDisplay = new google.maps.DirectionsRenderer;
            directionsDisplay.setMap(navigation_map);


            var input = document.getElementById("route_to_input");

            var route_to_input = new google.maps.places.Autocomplete(input);

            input = document.getElementById("destination_point_input");

            var destination_point_input = new google.maps.places.Autocomplete(input);

        }
        
        //Initialize the sidebar
        function initSidebar() {
                
            $("#menu_button").click(function() {



                var display = document.getElementById("sidebar").style.display;

                if(display == "block") {

                    document.getElementById("sidebar").style.display = "none";

                } else {

                    document.getElementById("sidebar").style.display = "block";

                }


            });

        }
        
        //Add the car resutls to the search-resutls div.
        function setSearchResults(cars) {
            
            $("#search_my_cars_results").click(function() {
                
                $("#search_my_cars_results").hide();
                
            });
            
            $("#search_my_cars_results").children().remove(".search_result");
            
            for(var i in cars) {
                
                var id = cars[i].carID;
                
                var item = $('<div class="search_result"><p>' + cars[i].brand + ", " + cars[i].type +  " (" + id + ")</p></div>");
                item.addClass("search_result");
                item.appendTo("#search_my_cars_results");
                
                item.click({id: id},function(e) {
                 
                   setCar(e.data.id); 
                    
                });
                
            }
            
            if(cars.length == 0) {
                
                $("#no_results").show();
                $("#no_results").click(function() {
                    $("#search_my_cars_results").hide();
                    
                });
                
            } else {
                
                $("#no_results").hide();
                
            }
            
        }
        
        //Initialize my cars search field
        function initSearchMyCars() {

            var position = $("#search_my_cars").position();
            position.top += $("#search_my_cars").height();
            $("#search_my_cars_results").css(position);
            $("#search_my_cars_results").width($("#search_my_cars").width() + 20);



            $("#search_my_cars").focusin(function(ev) {

                $("#search_my_cars_results").show();
                
                Webservice.get("cars/my?sessionid=" + UTIL.getSessionID() + "&filter=" + $("#search_my_cars").val(), function(json) {
                    
                    
                    if(json.status == Webservice.Status.OK) {
                        
                        
                        setSearchResults(json.data);
                        
                    } else {
                    
                         UTIL.showMessage("Unhandled status", json.status, 10000);
                    }
                    
                });
                

            });
            
            $("#search_my_cars").keyup(function(ev) {
                
                Webservice.get("cars/my?sessionid=" + UTIL.getSessionID() + "&filter=" + $("#search_my_cars").val(), function(json) {
                    
                    
                    if(json.status == Webservice.Status.OK) {
                        
                        
                        setSearchResults(json.data);
                        
                    } else {
                    
                         UTIL.showMessage("Unhandled status", json.status, 10000);
                    }
                    
                });
                

            });

        }
        
        //Initlaize html listeners
        function initListeners() {
            
            window.onresize = function() {
                
                var position = $("#search_my_cars").position();
                position.top += $("#search_my_cars").height();
                $("#search_my_cars_results").css(position);
                $("#search_my_cars_results").width($("#search_my_cars").width());
                
            }
            
            //Delte current user
            $("#delte_user").click(function() {
                
                Webservice.post("users/remove?sessionid=" + UTIL.getSessionID(), function(json) {
                    
                    if(json.status == Webservice.Status.OK) {
                        
                        //Success -> back to login
                        window.location.href = "login.html";
                        
                    } else {
                        
                        UTIL.showMessage("Unhandled status", json.status, 10000);
                        
                    }
                    
                });
                
            });
            
            //Update user data with the data of the user-dialog
            $("#update_user").click(function() {
               
                var url = "users/update?sessionid=" + UTIL.getSessionID();
                
                if($("#name").val().trim().length > 0) {
                    
                    url += "&name=" + $("#name").val();
                    
                }
                
                if($("#password").val().trim().length > 0) {
                    
                    url += "&pw=" + $("#password").val();
                    
                }
                
                Webservice.post(url, "", function(json) {
                    
                    if(json.status == Webservice.Status.OK) {
                        
                        UTIL.showMessage("Account", "Daten wurden erneuert.", 10000);
                        
                        Webservice.get("users/get?sessionid=" + UTIL.getSessionID(), function(json) {

                            if(json.status == Webservice.Status.OK) {

                                $("#user p").html(json.data.name);

                            } else {

                                UTIL.showMessage("Unhandled status", json.status, 10000);

                            }
                        });
                        
                    } else if(json.status == Webservice.Status.INVAILD_DATA) {
                        
                        UTIL.showMessage("Account", "Daten sind nicht valid.", 10000);
                        
                    } else {

                        UTIL.showMessage("Unhandled status", json.status, 10000);

                    }
                });

                
            });
            
            //Logout user
            $("#logout").click(function() {
                
                Webservice.get("auth/logout?sessionid=" + UTIL.getSessionID(), function(json) {
                    
                    if(json.status == Webservice.Status.OK) {
                        
                        //Back to login page
                        window.location.href = "login.html";
                        
                    }  else {

                        UTIL.showMessage("Unhandled status", json.status, 10000);

                    }
                    
                });
                
            });
            
            //if the background of the dialogs overlay is clicked the show dialog is removed
            $("#dialogs").click(function(e) {
                    
                if(e.target !== e.currentTarget) return;

                $(this).hide();

            });

            //Show user dialog
            $("#user").click(function() {

                Webservice.get("users/get?sessionid=" + UTIL.getSessionID(), function(json) {

                    if(json.status == Webservice.Status.OK) {

                        $("#name").val(json.data.name);
                        $("#email").val(json.data.email);

                    } else {

                        UTIL.showMessage("Unhandled status", json.status, 10000);

                    }
                });

                $("#dialogs .main_dialog").hide();

                $("#user_dialog").show();

                $("#dialogs").show();


            });

            //Unbind current car
            $("#remove_car").click(function() {

                Webservice.get("cars/unbind?carid=" + UTIL.getCarID() + "&sessionid=" + UTIL.getSessionID(), function(json) {

                   if(json.status == Webservice.Status.OK) {

                       UTIL.showMessage("Auto", "Auto wurde entfernt.", 10000);
                       
                       setFirstCar();


                   } else if(json.status == Webservice.Status.ALREADY_DONE) {

                       UTIL.showMessage("Auto", "Auto wurde bereits entfernt.", 10000);

                   } else if(json.status == Webservice.Status.NOT_FOUND) {

                       UTIL.showMessage("Auto", "Auto konnte nicht gefunden werden.", 10000);

                   } else {

                       UTIL.showMessage("Unhandled status", json.status, 10000);

                   }

               }); 

            });

            //Show binding dialog
            $("#add_car").click(function() {


                $("#dialogs .main_dialog").hide();

                $("#binding_dialog").show();

                $("#dialogs").show();


            });
            
            //Bind car with the entered code
            $("#bind_car").click(function() {
                
                if(UTIL.isInputValid($("#bindingCode"))) {
                  
                    Webservice.get("cars/bind?code=" + $("#bindingCode").val() + "&sessionid=" + UTIL.getSessionID(), function(json) {
                        
                        if(json.status == Webservice.Status.OK) {
                            
                            UTIL.showMessage("Auto", "Auto wurde hinzugefügt.", 10000);
                        
                        } else if(json.status == Webservice.Status.ALREADY_DONE) {
                            
                            UTIL.showMessage("Auto", "Auto wurde bereits hinzugefügt.", 10000);
                        
                        } else if(json.status == Webservice.Status.NOT_FOUND) {
                        
                            UTIL.showMessage("Auto", "Auto wurde nicht hinzugefügt.", 10000);
                        
                        } else if(json.status == Webservice.Status.AUTHORIZATION_FAILED) {
                        
                            UTIL.showMessage("Auto", "Code ist falsch.", 10000);
                        
                        } else {
                            
                            UTIL.showMessage("Unhandled status", json.status, 10000);
                            
                        }
                        
                        
                    });
                  
                }
                
            });

            //Create a new route
            $("#create_route").click(function() {

                var start = $("#route_to_input").val();

                var destination = $("#destination_point_input").val();
                
                UTIL.placeToLatLng(start, function(latlng) {

                    if(latlng != undefined) {

                        start = latlng;
                        
                        
                        UTIL.placeToLatLng(destination, function(latlng) {

                            if(latlng != undefined) {

                                destination = latlng;

                                Webservice.post("cars/route/add?carid=" + UTIL.getCarID() + "&sessionid=" + UTIL.getSessionID(), { start: start,  destination: destination, creationTime: UTIL.getTimeStamp()}, function(json) {
                                    console.log(json.status);
                                    if(json.status == Webservice.Status.OK) {

                                        UTIL.showMessage("Route", "Route wurde hinzugefügt.", 10000);

                                        setCar(UTIL.getCarID());

                                    } else if(json.status == Webservice.Status.FAILED) {

                                        UTIL.showMessage("Route", "Route konnte nicht hinzugefügt werden. Auto nicht erreichbar.", 10000);

                                    } else if(json.status == Webservice.Status.NOT_FOUND) {

                                        UTIL.showMessage("Route", "Auto wurde nicht gefunden.", 10000);

                                    } else {

                                        UTIL.showMessage("Unhandled status", json.status, 10000);

                                    }


                                });

                            } else {

                                UTIL.showMessage("Format", "Ziel konnte nicht gefunden werden.", 10000);

                            }

                        });

                    } else {

                        UTIL.showMessage("Format", "Startpunkt konnte nicht gefunden werden.", 10000);

                    }

                });

            });

        }
        
        //Intialize webservice listeners
        function initializeWebservice() {
            
            Webservice.setOnAuthenticationFailed(function() {
                    
                //UTIL.showMessage("Login-Fehler", "Sie müssen sich zuerst einloggen.", 10000);

                window.location.href = "login.html";

            });

            Webservice.setOnException(function(exceptionMsg) {

                UTIL.showMessage("Exception", "Ups, da ist wohl was ganz schön schief gegeangen.<br>Exception: " + exceptionMsg, 10000);

            });

            Webservice.get("users/get?sessionid=" + UTIL.getSessionID(), function(json) {

                if(json.status == Webservice.Status.OK) {

                    $("#user p").html(json.data.name);

                } else {

                    UTIL.showMessage("Unhandled status", json.status, 10000);

                }
            });
        }
        
        //Initialize UI
        function initialize() {
            
            initializeWebservice();
            
            initializeMaps();
            
            initSidebar();
            
            initSearchMyCars();
            
            initListeners();
            
            //Set the car for ther start
            if(UTIL.getCarID() != undefined) {
                
                Webservice.get("cars/get?carid=" + UTIL.getCarID() + "&sessionid=" + UTIL.getSessionID(), function(json) {

                    if(json.status == Webservice.Status.UP_TO_DATE) {

                        setCarData(json.data, true);

                    } else if(json.status == Webservice.Status.DATABASE) {

                        setCarData(json.data, false);

                    } else if(json.status == Webservice.Status.NOT_FOUND) {

                        setFirstCar();

                    } else {

                        UTIL.showMessage("Unhandled status", json.status, 10000);

                    }

                });
                
            } else {
                
                setFirstCar();
                
            }
            
        }
        
        return {
            
            initialize: initialize
            
        }
        
    })();
    
    return {
        
        Cars: Cars
        
    }
    
})();