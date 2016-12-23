package io.github.nasso.urmusic.core;

import java.lang.reflect.Field;
import java.util.Map;

public class FrameProperties {
	private static final Field[] FIELDS = FrameProperties.class.getFields();
	
	public float maxval = -Float.MAX_VALUE;
	public float minval = -Float.MAX_VALUE;
	
	public float maxlowval = -Float.MAX_VALUE;
	public float minlowval = -Float.MAX_VALUE;
	
	public float maxhighval = -Float.MAX_VALUE;
	public float minhighval = -Float.MAX_VALUE;
	
	public float csize = 0;
	
	public float imgw = 0;
	public float imgh = 0;
	public float imgr = 0;
	
	public float time = 0;
	public float duration = 0;
	
	public String songtitle = "Silence";
	public String prettytime = "";
	public String prettyduration = "";
	
	public Map<String, Object> get(Map<String, Object> map) {
		try {
			for(int i = 0, l = FIELDS.length; i < l; i++) {
				Field f = FIELDS[i];
				map.put(f.getName(), f.get(this));
			}
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return map;
	}
}
