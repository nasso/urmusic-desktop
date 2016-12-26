package io.github.nasso.urmusic.core;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.paint.Color;

public class Settings {
	public ExpressionProperty smoothingTimeConstant = new ExpressionProperty("0.65");
	
	public SectionGroup rootGroup = new SectionGroup();
	
	public Color backgroundColor = Color.web("#3b3b3b");
	
	public AdvancedSettings advanced = new AdvancedSettings();
	
	public Settings() {
		
	}
	
	public Settings(PrimitiveProperties props) {
		this(props, null);
	}
	
	public Settings(PrimitiveProperties props, PrimitiveProperties advanced) {
		this.set(props, advanced);
	}
	
	public void set(PrimitiveProperties props, PrimitiveProperties advanced) {
		this.smoothingTimeConstant.setExpr(props.getString("smoothingTimeConstant", "0.65"));
		
		this.backgroundColor = Color.web(props.getString("backgroundColor", "#3b3b3b"));
		
		if(advanced != null) this.advanced.set(advanced);
	}
	
	public void dispose() {
		rootGroup.dispose();
	}
}
