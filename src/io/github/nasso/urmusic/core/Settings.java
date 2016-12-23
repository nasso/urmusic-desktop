package io.github.nasso.urmusic.core;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.paint.Color;

public class Settings {
	public ExpressionProperty smoothingTimeConstant = new ExpressionProperty("0.65");
	
	public List<Section> sections = new ArrayList<Section>();
	
	public ExpressionProperty globalScale = new ExpressionProperty("1.0");
	public ExpressionProperty globalOffsetX = new ExpressionProperty("0.0");
	public ExpressionProperty globalOffsetY = new ExpressionProperty("0.0");
	public ExpressionProperty globalRotation = new ExpressionProperty("0.0");
	
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
		
		this.globalScale.setExpr(props.getString("globalScale", "1.0"));
		this.globalOffsetX.setExpr(props.getString("globalOffsetX", "0.0"));
		this.globalOffsetY.setExpr(props.getString("globalOffsetY", "0.0"));
		this.globalRotation.setExpr(props.getString("globalRotation", "0.0"));
		
		this.backgroundColor = Color.web(props.getString("backgroundColor", "#3b3b3b"));
		
		if(advanced != null) this.advanced.set(advanced);
	}
	
	public void dispose() {
		for(Section s : this.sections) {
			s.dispose();
		}
		
		this.globalScale.dispose();
		this.globalOffsetX.dispose();
		this.globalOffsetY.dispose();
		this.globalRotation.dispose();
	}
}
