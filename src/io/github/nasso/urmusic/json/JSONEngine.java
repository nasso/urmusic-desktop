package io.github.nasso.urmusic.json;

import io.github.nasso.urmusic.ApplicationPreferences;

public interface JSONEngine {
	public static final JSONEngine ENGINE = JSONEngines.get(ApplicationPreferences.jsonEngineName);
	
	public String stringify(Object obj);
	
	public JSONObject parse(String str);
	
	public JSONObject createObject();
	
	public JSONArray createArray();
}
