package io.github.nasso.urmusic.json;

import java.util.Map;

import io.github.nasso.urmusic.core.PrimitiveProperties;
import io.github.nasso.urmusic.core.Section;
import io.github.nasso.urmusic.core.Settings;

public class JSONLoader {
	private JSONLoader() {
	}
	
	private static PrimitiveProperties getProperties(JSONObject jsonObj) {
		PrimitiveProperties props = new PrimitiveProperties();
		
		Map<String, Object> m = jsonObj.map();
		
		for(String k : m.keySet()) {
			Object o = m.get(k);
			
			if(o instanceof String) {
				props.setString(k, o.toString());
			} else if(o instanceof Float) {
				props.setNumber(k, (Float) o);
			} else if(o instanceof Boolean) {
				props.setBool(k, (Boolean) o);
			}
		}
		
		return props;
	}
	
	public static Settings loadSettings(Settings s, String srcjson) {
		JSONObject o = JSONEngine.ENGINE.parse(srcjson);
		
		PrimitiveProperties p = getProperties(o);
		PrimitiveProperties ap = getProperties(o.getObject("advanced"));
		s.set(p, ap);
		
		JSONArray sections = o.getArray("sections");
		
		for(int i = 0, l = sections.getLength(); i < l; i++) {
			JSONObject secObj = sections.getObject(i);
			
			s.sections.add(new Section(getProperties(secObj), getProperties(secObj.getObject("target"))));
		}
		
		return s;
	}
	
	public static Settings loadSettings(String srcjson) {
		return loadSettings(new Settings(), srcjson);
	}
}
