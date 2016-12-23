package io.github.nasso.urmusic.json.gson;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import io.github.nasso.urmusic.json.JSONArray;
import io.github.nasso.urmusic.json.JSONObject;

public class GSONJSONArray implements JSONArray {
	private static Map<JsonArray, GSONJSONArray> bindings = new HashMap<JsonArray, GSONJSONArray>();
	
	public static GSONJSONArray get(JsonArray obj) {
		if(bindings.containsKey(obj)) { return bindings.get(obj); }
		
		GSONJSONArray o = new GSONJSONArray(obj);
		bindings.put(obj, o);
		
		return o;
	}
	
	JsonArray gsonArr;
	
	private GSONJSONArray(JsonArray arr) {
		this.gsonArr = arr;
	}
	
	public JSONObject getObject(int index) {
		return GSONJSONObject.get(this.gsonArr.get(index).getAsJsonObject());
	}
	
	public JSONArray getArray(int index) {
		return get(this.gsonArr.get(index).getAsJsonArray());
	}
	
	public boolean getBool(int index) {
		return this.gsonArr.get(index).getAsBoolean();
	}
	
	public float getNumber(int index) {
		return this.gsonArr.get(index).getAsFloat();
	}
	
	public String getString(int index) {
		return this.gsonArr.get(index).getAsString();
	}
	
	public int getLength() {
		return this.gsonArr.size();
	}
	
	public void set(int i, JSONObject v) {
		if(v instanceof GSONJSONObject) this.gsonArr.set(i, ((GSONJSONObject) v).gsonObj);
	}
	
	public void set(int i, JSONArray v) {
		if(v instanceof GSONJSONArray) this.gsonArr.set(i, ((GSONJSONArray) v).gsonArr);
	}
	
	public void set(int i, boolean v) {
		this.gsonArr.set(i, new JsonPrimitive(v));
	}
	
	public void set(int i, float v) {
		this.gsonArr.set(i, new JsonPrimitive(v));
	}
	
	public void set(int i, String v) {
		this.gsonArr.set(i, new JsonPrimitive(v));
	}
	
	public String toString() {
		return this.gsonArr.toString();
	}
}
