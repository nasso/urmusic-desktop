package io.github.nasso.urmusic.json;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import io.github.nasso.urmusic.Project;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.core.PrimitiveProperties;
import io.github.nasso.urmusic.core.Section;
import io.github.nasso.urmusic.core.Settings;

public class JSONProjectLoader {
	public static enum JSONProjectVersion {
		V1_0
	}
	
	private JSONProjectLoader() {
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
	
	private static Project loadProject10(Project proj, JSONObject root) {
		Settings s = proj.getSettings();
		
		PrimitiveProperties p = getProperties(root);
		PrimitiveProperties ap = getProperties(root.getObject("advanced"));
		s.set(p, ap);
		
		s.rootGroup.scaleX.setExpr(p.getString("globalScale", "1.0"));
		s.rootGroup.scaleY.bind(s.rootGroup.scaleX);
		s.rootGroup.posX.setExpr(p.getString("globalOffsetX", "0.0"));
		s.rootGroup.posY.setExpr(p.getString("globalOffsetY", "0.0"));
		s.rootGroup.rotation.setExpr(p.getString("globalRotation", "0.0"));
		
		JSONArray sections = root.getArray("sections");
		
		for(int i = 0, l = sections.getLength(); i < l; i++) {
			JSONObject secObj = sections.getObject(i);
			
			s.rootGroup.addChildren(new Section(getProperties(secObj), getProperties(secObj.getObject("target"))));
		}
		
		return proj;
	}
	
	public static Project loadProject(File file) throws IOException {
		if(Urmusic.DEBUG) System.out.println("[ProjectLoader] Loading: " + file.getAbsolutePath());
		
		String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
		
		JSONObject o = JSONEngine.ENGINE.parse(Utils.readFile(file.getAbsolutePath(), false));
		
		JSONProjectVersion ver = o.has("version") ? JSONProjectVersion.valueOf(o.getString("version")) : JSONProjectVersion.V1_0;
		
		if(ver == null) ver = JSONProjectVersion.V1_0;
		
		switch(ver) {
			default: // case V1_0:
				return loadProject10(new Project(fileName), o);
		}
	}
}
