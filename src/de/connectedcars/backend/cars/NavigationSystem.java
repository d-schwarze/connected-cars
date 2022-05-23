package de.connectedcars.backend.cars;

import java.util.ArrayList;
import java.util.List;

import de.connectedcars.backend.cars.Route.RouteJSON;
import de.connectedcars.backend.id.IdGenerator;

/**
 * Navigation system of a {@linkplain Car}.
 * @author David Schwarze
 *
 */
public class NavigationSystem {

	//All routes that are send to the navigation system.
	private List<Route> routes = new ArrayList<>();
	
	//Current selected route.
	private Route currentRoute;
	
	/* Routes get a own id that is linked to this navigationsystem. 
	 * This id is not unique, but in combination with the unique navigation system the routes are unique.
	 * Transient because it is not saved into json.
	 */
	private transient IdGenerator<Route> idGenerator;
	
	public NavigationSystem() {
		
		idGenerator = new IdGenerator<>(routes);
		
	}
	
	public NavigationSystem(Route currentRoute, List<Route> routes) {
		
		this.routes = routes;
		
		if(this.routes.contains(currentRoute))
			this.currentRoute = currentRoute;
		
	}
	
	/**
	 * Add a new route. Format is {@linkplain RouteJSON} because it is directly send from the client-fronted in a json format.
	 * @param route the new route
	 */
	public void addRoute(RouteJSON route) {
		
		this.routes.add(new Route(idGenerator.generateNewID(), route));
		
	}
	
	/**
	 * Removes a route.
	 * @param routeId id of the route, that should be removed
	 */
	public void removeRoute(long routeId) {
		
		for(Route route : routes) {
			
			if(route.getID() == routeId) {
				
				routes.remove(route);
				
				//If the route is the selected one, the selected route is set to null -> no route selected.
				if((currentRoute != null && currentRoute.getID() == routeId)) {
					
					currentRoute = null;
					
				}
				
				break;
				
			}
			
		}
		
	}
	
	/**
	 * Set the current route of the navigation system.
	 * @param routeId id of the route, route has to be added before ({@linkplain #addRoute(RouteJSON)}).
	 */
	public void setCurrentRoute(long routeId) {
		
		for(Route route : routes) {
			
			if(route.getID() == routeId) {
				this.currentRoute = route;
				break;
			}
			
		}
		
	}

	public List<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public Route getCurrentRoute() {
		return currentRoute;
	}

	public void setCurrentRoute(Route currentRoute) {
		this.currentRoute = currentRoute;
	}
}
