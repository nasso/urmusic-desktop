package io.github.nasso.urmusic.json.gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.github.nasso.urmusic.json.JSONArray;
import io.github.nasso.urmusic.json.JSONObject;

public class GSONJSONObject implements JSONObject {
	private static Map<JsonObject, GSONJSONObject> bindings = new HashMap<JsonObject, GSONJSONObject>();
	
	public static GSONJSONObject get(JsonObject obj) {
		if(bindings.containsKey(obj)) { return bindings.get(obj); }
		
		GSONJSONObject o = new GSONJSONObject(obj);
		bindings.put(obj, o);
		
		return o;
	}
	
	JsonObject gsonObj;
	
	private GSONJSONObject(JsonObject obj) {
		this.gsonObj = obj;
	}
	
	public JSONObject getObject(String key) {
		return get(this.gsonObj.getAsJsonObject(key));
	}
	
	public JSONArray getArray(String key) {
		return GSONJSONArray.get(this.gsonObj.getAsJsonArray(key));
	}
	
	public boolean getBool(String key) {
		return this.gsonObj.get(key).getAsBoolean();
	}
	
	public float getNumber(String key) {
		return this.gsonObj.get(key).getAsFloat();
	}
	
	public String getString(String key) {
		return this.gsonObj.get(key).getAsString();
	}
	
	public boolean has(String key) {
		return this.gsonObj.has(key);
	}
	
	public void set(String key, JSONObject v) {
		if(v instanceof GSONJSONObject) this.gsonObj.add(key, ((GSONJSONObject) v).gsonObj);
	}
	
	public void set(String key, JSONArray v) {
		if(v instanceof GSONJSONArray) this.gsonObj.add(key, ((GSONJSONArray) v).gsonArr);
	}
	
	public void set(String key, boolean v) {
		this.gsonObj.addProperty(key, v);
	}
	
	public void set(String key, float v) {
		this.gsonObj.addProperty(key, v);
	}
	
	public void set(String key, String v) {
		this.gsonObj.addProperty(key, v);
	}
	
	public Map<String, Object> map() {
		Set<Map.Entry<String, JsonElement>> e = this.gsonObj.entrySet();
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		for(Map.Entry<String, JsonElement> entry : e) {
			JsonElement je = entry.getValue();
			
			if(je.isJsonObject()) {
				map.put(entry.getKey(), GSONJSONObject.get(je.getAsJsonObject()));
			} else if(je.isJsonArray()) {
				map.put(entry.getKey(), GSONJSONArray.get(je.getAsJsonArray()));
			} else if(je.isJsonPrimitive()) {
				JsonPrimitive prim = je.getAsJsonPrimitive();
				
				if(prim.isString()) {
					map.put(entry.getKey(), prim.getAsString());
				} else if(prim.isNumber()) {
					map.put(entry.getKey(), prim.getAsFloat());
				} else if(prim.isBoolean()) {
					map.put(entry.getKey(), prim.getAsBoolean());
				}
			}
		}
		
		return map;
	}
	
	public String toString() {
		return this.gsonObj.toString();
	}
}
