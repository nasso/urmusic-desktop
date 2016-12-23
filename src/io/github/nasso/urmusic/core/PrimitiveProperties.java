package io.github.nasso.urmusic.core;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.json.JSONEngine;
import io.github.nasso.urmusic.json.JSONObject;
import io.github.nasso.urmusic.json.JSONSerializable;

public class PrimitiveProperties implements JSONSerializable {
	private Map<String, Boolean> booleans = new HashMap<String, Boolean>();
	private Map<String, Float> numbers = new HashMap<String, Float>();
	private Map<String, String> strings = new HashMap<String, String>();
	
	public void setBool(String name, boolean value) {
		this.booleans.put(name, value);
	}
	
	public void setNumber(String name, float value) {
		this.numbers.put(name, value);
	}
	
	public void setString(String name, String value) {
		this.strings.put(name, value);
	}
	
	public boolean getBool(String name) {
		return this.booleans.get(name);
	}
	
	public float getNumber(String name) {
		return this.numbers.get(name);
	}
	
	public String getString(String name) {
		return this.strings.get(name);
	}
	
	public boolean getBool(String name, boolean defaultValue) {
		return this.booleans.getOrDefault(name, defaultValue);
	}
	
	public float getNumber(String name, float defaultValue) {
		return this.numbers.getOrDefault(name, defaultValue);
	}
	
	public String getString(String name, String defaultValue) {
		return this.strings.getOrDefault(name, defaultValue);
	}
	
	public Object toJSON(JSONEngine engine) {
		JSONObject o = engine.createObject();
		
		for(String k : this.booleans.keySet()) {
			o.set(k, this.booleans.get(k));
		}
		
		for(String k : this.numbers.keySet()) {
			o.set(k, this.numbers.get(k));
		}
		
		for(String k : this.strings.keySet()) {
			o.set(k, this.strings.get(k));
		}
		
		return o;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("{\n");
		for(String k : this.booleans.keySet()) {
			builder.append("\t" + k + ": " + this.booleans.get(k) + "\n");
		}
		
		for(String k : this.numbers.keySet()) {
			builder.append("\t" + k + ": " + this.numbers.get(k) + "\n");
		}
		
		for(String k : this.strings.keySet()) {
			builder.append("\t" + k + ": \"" + this.strings.get(k) + "\"\n");
		}
		builder.append("}\n");
		
		return builder.toString();
	}
}
