package io.github.nasso.urmusic.json;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.json.gson.GSONJSONEngine;

public class JSONEngines {
	private JSONEngines() {
	}
	
	private static Map<String, JSONEngine> engines = new HashMap<String, JSONEngine>();
	
	static {
		engines.put("gson", new GSONJSONEngine());
	}
	
	public static JSONEngine get(String name) {
		return engines.get(name);
	}
}
