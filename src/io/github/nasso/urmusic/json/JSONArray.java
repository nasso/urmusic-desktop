package io.github.nasso.urmusic.json;

public interface JSONArray {
	public JSONObject getObject(int index);
	
	public JSONArray getArray(int index);
	
	public boolean getBool(int index);
	
	public float getNumber(int index);
	
	public String getString(int index);
	
	public int getLength();
	
	public void add(JSONObject v);
	
	public void add(JSONArray v);
	
	public void add(boolean v);
	
	public void add(float v);
	
	public void add(String v);
	
	public void set(int i, JSONObject v);
	
	public void set(int i, JSONArray v);
	
	public void set(int i, boolean v);
	
	public void set(int i, float v);
	
	public void set(int i, String v);
}
