package io.github.nasso.urmusic.core;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.paint.Color;

public class Section extends SectionGroupElement {
	public SectionType type = SectionType.FREQ;
	public Color color = Color.web("#ffffff");
	public ExpressionProperty glowness = new ExpressionProperty("0.0");
	public SectionTarget target = null;
	
	public Section(PrimitiveProperties props, PrimitiveProperties targetProps) {
		super(props);
		
		this.type = SectionType.valueOf(props.getString("type", "FREQ").toUpperCase());
		this.color = Color.web(props.getString("color", "#ffffff"));
		this.glowness.setExpr(props.getString("glowness", "0.0"));
		
		this.updateType();
		
		if(this.target != null) {
			this.target.set(targetProps);
		}
	}
	
	public void refreshProperties(FrameProperties props) {
		this.glowness.refresh(props);
		
		if(this.target != null) this.target.refreshOwnProperties(props);
		
		super.refreshProperties(props);
	}
	
	public void updateType() {
		switch(this.type) {
			case FREQ:
				this.target = new FreqSection();
				break;
			case TIME_DOM:
				this.target = new TimeDomSection();
				break;
			case IMAGE:
				this.target = new ImageSection();
				break;
			case TEXT:
				this.target = new TextSection();
				break;
		}
	}
	
	public void dispose() {
		super.dispose();
		
		this.glowness.dispose();
		
		if(this.target != null) this.target.dispose();
	}
}
