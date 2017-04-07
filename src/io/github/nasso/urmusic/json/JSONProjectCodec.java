package io.github.nasso.urmusic.json;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import io.github.nasso.urmusic.Project;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.core.AdvancedSettings;
import io.github.nasso.urmusic.core.ImageSection;
import io.github.nasso.urmusic.core.PrimitiveProperties;
import io.github.nasso.urmusic.core.Section;
import io.github.nasso.urmusic.core.SectionGroup;
import io.github.nasso.urmusic.core.SectionGroupElement;
import io.github.nasso.urmusic.core.SectionTarget;
import io.github.nasso.urmusic.core.Settings;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class JSONProjectCodec {
	public static enum JSONProjectVersion {
		V1_0, V1_1, V2_0
	}
	
	private JSONProjectCodec() {
	}
	
	// Load
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
	
	private static SectionGroupElement getGroupElement20(JSONObject root) {
		String type = root.getString("OBJECT_TYPE");
		
		if(type.equals("SectionGroup")) {
			SectionGroup group = new SectionGroup(getProperties(root));
			
			JSONArray childrenArray = root.getArray("children");
			for(int i = 0; i < childrenArray.getLength(); i++) {
				group.addChild(getGroupElement20(childrenArray.getObject(i)));
			}
			
			return group;
		} else if(type.equals("Section")) return new Section(getProperties(root), getProperties(root.getObject("target")));
		
		return null;
	}
	
	private static Project loadProject10(File file, JSONObject root) {
		String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
		
		int width = 0;
		int height = 0;
		
		Optional<int[]> result = Urmusic.showSizeDialog();
		
		if(result.isPresent()) {
			int[] size = result.get();
			width = size[0];
			height = size[1];
		} else {
			return null;
		}
		
		Settings s = new Settings(width, height);
		
		PrimitiveProperties p = getProperties(root);
		PrimitiveProperties ap = getProperties(root.getObject("advanced"));
		s.set(p, ap);
		
		s.rootGroup.scaleX.setExpr(p.getString("globalScale", "1.0"));
		s.rootGroup.scaleY.setExpr(p.getString("globalScale", "1.0"));
		s.rootGroup.posX.setExpr(p.getString("globalOffsetX", "0.0"));
		s.rootGroup.posY.setExpr(p.getString("globalOffsetY", "0.0"));
		s.rootGroup.rotation.setExpr(p.getString("globalRotation", "0.0"));
		
		JSONArray sections = root.getArray("sections");
		
		for(int i = 0, l = sections.getLength(); i < l; i++) {
			JSONObject secObj = sections.getObject(i);
			
			s.rootGroup.addChild(new Section(getProperties(secObj), getProperties(secObj.getObject("target"))));
		}
		
		return new Project(file, fileName, s);
	}
	
	private static Project loadProject11(File file, JSONObject root) {
		Settings s = new Settings((int) root.getNumber("width"), (int) root.getNumber("height"));
		
		PrimitiveProperties p = getProperties(root);
		PrimitiveProperties ap = getProperties(root.getObject("advanced"));
		s.set(p, ap);
		
		s.rootGroup.scaleX.setExpr(p.getString("globalScale", "1.0"));
		s.rootGroup.scaleY.setExpr(p.getString("globalScale", "1.0"));
		s.rootGroup.posX.setExpr(p.getString("globalOffsetX", "0.0"));
		s.rootGroup.posY.setExpr(p.getString("globalOffsetY", "0.0"));
		s.rootGroup.rotation.setExpr(p.getString("globalRotation", "0.0"));
		
		JSONArray sections = root.getArray("sections");
		
		for(int i = 0, l = sections.getLength(); i < l; i++) {
			JSONObject secObj = sections.getObject(i);
			
			s.rootGroup.addChild(new Section(getProperties(secObj), getProperties(secObj.getObject("target"))));
		}
		
		return new Project(file, root.getString("name"), s);
	}
	
	private static Project loadProject20(File file, JSONObject root) {
		Settings s = new Settings((int) root.getNumber("framewidth"), (int) root.getNumber("frameheight"));
		
		PrimitiveProperties props = getProperties(root);
		PrimitiveProperties advancedProps = getProperties(root.getObject("advanced"));
		s.set(props, advancedProps);
		
		s.rootGroup = (SectionGroup) getGroupElement20(root.getObject("root"));
		
		return new Project(file, root.getString("name"), s);
	}
	
	public static Project loadProject(File file) throws IOException {
		if(Urmusic.DEBUG) System.out.println("[ProjectLoader] Loading: " + file.getAbsolutePath());
		
		JSONObject o = JSONEngine.ENGINE.parse(Utils.readFile(file.getAbsolutePath(), false));
		
		JSONProjectVersion ver = o.has("version") ? JSONProjectVersion.valueOf(o.getString("version")) : JSONProjectVersion.V1_0;
		
		if(ver == null) ver = JSONProjectVersion.V1_0;
		
		switch(ver) {
			case V2_0:
				return loadProject20(file, o);
			case V1_1:
				return loadProject11(file, o);
			default:
				return loadProject10(file, o);
		}
	}
	
	// Save
	private static JSONObject createSectionGroupElement(SectionGroupElement sge) {
		if(sge instanceof Section) {
			JSONObject obj = JSONEngine.ENGINE.createObject();
			
			obj.set("OBJECT_TYPE", "Section");
			
			Field[] fields = Section.class.getFields();
			for(Field f : fields) {
				if(Modifier.isStatic(f.getModifiers()) || f.getType() == ArrayList.class) continue;
				
				try {
					if(f.getType() == Color.class) {
						Color c = ((Color) f.get(sge));
						
						int r = (int) (c.getRed() * 255);
						int g = (int) (c.getGreen() * 255);
						int b = (int) (c.getBlue() * 255);
						int a = (int) (c.getOpacity() * 255);
						
						obj.set(f.getName(), "#" + Integer.toString(r, 16) + Integer.toString(g, 16) + Integer.toString(b, 16) + Integer.toString(a, 16));
					} else if(f.getType() == Boolean.TYPE) obj.set(f.getName(), (boolean) f.get(sge));
					else if(f.getType().isEnum()) obj.set(f.getName(), f.get(sge).toString());
					else if(SectionTarget.class.isAssignableFrom(f.getType())) {
						SectionTarget target = (SectionTarget) f.get(sge);
						
						Field[] targetFields = target.getClass().getFields();
						
						JSONObject targetObject = JSONEngine.ENGINE.createObject();
						for(Field targetF : targetFields) {
							if(Modifier.isStatic(targetF.getModifiers()) || targetF.getType() == ArrayList.class || targetF.getType() == Image.class) continue;
							
							if(targetF.getType() == Color.class) {
								Color c = ((Color) targetF.get(target));
								
								int r = (int) (c.getRed() * 255);
								int g = (int) (c.getGreen() * 255);
								int b = (int) (c.getBlue() * 255);
								int a = (int) (c.getOpacity() * 255);
								
								targetObject.set(targetF.getName(), "#" + Integer.toString(r, 16) + Integer.toString(g, 16) + Integer.toString(b, 16) + Integer.toString(a, 16));
							} else if(targetF.getType() == Boolean.TYPE) targetObject.set(targetF.getName(), (boolean) targetF.get(target));
							else if(targetF.getType().isEnum()) targetObject.set(targetF.getName(), targetF.get(target).toString());
							else targetObject.set(targetF.getName(), targetF.get(target).toString());
						}
						
						if(target instanceof ImageSection) {
							ImageSection is = (ImageSection) target;
							
							targetObject.set("imageURL", (is.getImageURL() == null ? null : is.getImageURL()));
						}
						
						obj.set("target", targetObject);
					} else obj.set(f.getName(), f.get(sge).toString());
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
				} catch(IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			return obj;
		} else if(sge instanceof SectionGroup) {
			JSONObject obj = JSONEngine.ENGINE.createObject();
			
			obj.set("OBJECT_TYPE", "SectionGroup");
			
			Field[] fields = SectionGroup.class.getFields();
			for(Field f : fields) {
				if(Modifier.isStatic(f.getModifiers()) || f.getType() == ArrayList.class) continue;
				
				try {
					if(f.getType() == Boolean.TYPE) obj.set(f.getName(), (boolean) f.get(sge));
					else obj.set(f.getName(), f.get(sge).toString());
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
				} catch(IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			SectionGroup sg = (SectionGroup) sge;
			
			JSONArray groupChildren = JSONEngine.ENGINE.createArray();
			for(SectionGroupElement sgeChild : sg.getUnmodifiableChildren()) {
				groupChildren.add(createSectionGroupElement(sgeChild));
			}
			
			obj.set("children", groupChildren);
			
			return obj;
		}
		
		return null;
	}
	
	public static void saveProject(Project proj) {
		Settings settings = proj.getSettings();
		SectionGroup rootGroup = settings.rootGroup;
		AdvancedSettings advancedSettings = settings.advanced;
		
		JSONObject root = JSONEngine.ENGINE.createObject();
		
		root.set("version", JSONProjectVersion.V2_0.toString());
		root.set("name", proj.name);
		
		Field[] fields = Settings.class.getFields();
		for(Field f : fields) {
			if(Modifier.isStatic(f.getModifiers()) || f.getType() == AdvancedSettings.class || f.getType() == SectionGroup.class) continue;
			
			try {
				if(f.getType() == Color.class) {
					Color c = ((Color) f.get(settings));
					
					int r = (int) (c.getRed() * 255);
					int g = (int) (c.getGreen() * 255);
					int b = (int) (c.getBlue() * 255);
					int a = (int) (c.getOpacity() * 255);
					
					root.set(f.getName(), "#" + Integer.toString(r, 16) + Integer.toString(g, 16) + Integer.toString(b, 16) + Integer.toString(a, 16));
				} else root.set(f.getName(), f.get(settings).toString());
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		root.set("root", createSectionGroupElement(rootGroup));
		
		JSONObject advancedObject = JSONEngine.ENGINE.createObject();
		
		fields = AdvancedSettings.class.getFields();
		for(Field f : fields) {
			if(Modifier.isStatic(f.getModifiers())) continue;
			
			try {
				if(f.getType() == Boolean.TYPE) advancedObject.set(f.getName(), (boolean) f.get(advancedSettings));
				else if(f.getType() == Float.TYPE) advancedObject.set(f.getName(), (Float) f.get(advancedSettings));
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		root.set("advanced", advancedObject);
		
		try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(proj.getProjectFile()))) {
			JSONEngine.ENGINE.write(root, out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
