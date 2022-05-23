package de.connectedcars.backend.messaging.util;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.connectedcars.backend.cars.Location;

/**
 * A normal Gson object would have problems with some java classes. For example {@linkplain LocalDate}.<br>
 * This class provides an modified Gson object via {@linkplain #get()}
 * @author David
 *
 */
public class ConfigeredGson {

	/**
	 * Creates a modified Gson object.
	 * @return modified gson object
	 */
	public static Gson get() {
		
		return new GsonBuilder().registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {

			@Override
			public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
				
				JsonObject result = new JsonObject();
				result.addProperty("day", localDate.getDayOfMonth());
				result.addProperty("month", localDate.getMonthValue());
				result.addProperty("year", localDate.getYear());
				
				return result;
			}
			
		}).registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
		    @Override
		    public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		        JsonObject obj = json.getAsJsonObject();
		        return LocalDate.of(obj.get("year").getAsInt(), obj.get("month").getAsInt(), obj.get("day").getAsInt());
		    }
		}).registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {

			@Override
			public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
				
				JsonObject result = new JsonObject();
				result.addProperty("day", localDateTime.getDayOfMonth());
				result.addProperty("month", localDateTime.getMonthValue());
				result.addProperty("year", localDateTime.getYear());
				result.addProperty("hour", localDateTime.getHour());
				result.addProperty("minute", localDateTime.getMinute());
				result.addProperty("second", localDateTime.getSecond());
				
				return result;
			}
			
		}).registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
		    @Override
		    public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		        JsonObject obj = json.getAsJsonObject();
		        return LocalDateTime.of(obj.get("year").getAsInt(), obj.get("month").getAsInt(), obj.get("day").getAsInt(), obj.get("hour").getAsInt(), obj.get("minute").getAsInt(), obj.get("second").getAsInt());
		    }
		}).registerTypeAdapter(Location.class, new JsonDeserializer<Location>() {
		    @Override
		    public Location deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		        JsonObject obj = json.getAsJsonObject();
		        return new Location(obj.get("lat").getAsDouble(), obj.get("lng").getAsDouble());
		    }
		}).create();
		
	}
	
}
