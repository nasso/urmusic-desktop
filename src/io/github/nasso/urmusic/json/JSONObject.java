package io.github.nasso.urmusic.json;

import java.util.Map;

public interface JSONObject {
	public JSONObject getObject(String key);
	
	public JSONArray getArray(String key);
	
	public boolean getBool(String key);
	
	public float getNumber(String key);
	
	public String getString(String key);
	
	public void set(String key, JSONObject v);
	
	public void set(String key, JSONArray v);
	
	public void set(String key, boolean v);
	
	public void set(String key, float v);
	
	public void set(String key, String v);
	
	public Map<String, Object> map();
}
