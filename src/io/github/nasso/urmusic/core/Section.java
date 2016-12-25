package io.github.nasso.urmusic.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.nasso.urmusic.expression.ExpressionEngine;
import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.paint.Color;

public class Section extends SectionGroup {
	public String name = "A section";
	public SectionType type = SectionType.FREQ;
	public boolean visible = true;
	public ExpressionProperty opacity = new ExpressionProperty("1.0");
	public ExpressionProperty posX = new ExpressionProperty("0.0");
	public ExpressionProperty posY = new ExpressionProperty("0.0");
	public ExpressionProperty rotation = new ExpressionProperty("0.0");
	public ExpressionProperty scaleX = new ExpressionProperty("1.0");
	public ExpressionProperty scaleY = new ExpressionProperty("1.0");
	public Color color = Color.web("#ffffff");
	public ExpressionProperty glowness = new ExpressionProperty("0.0");
	public SectionTarget target = null;
	
	public Section(PrimitiveProperties props, PrimitiveProperties targetProps) {
		this.name = props.getString("name", this.name);
		this.type = SectionType.valueOf(props.getString("type", "FREQ").toUpperCase());
		this.visible = props.getBool("visible", true);
		this.opacity.setExpr(props.getString("opacity", "1.0"));
		this.posX.setExpr(props.getString("posX", "0.0"));
		this.posY.setExpr(props.getString("posY", "0.0"));
		this.rotation.setExpr(props.getString("rotation", "0.0"));
		this.scaleX.setExpr(props.getString("scaleX", "1.0"));
		this.scaleY.setExpr(props.getString("scaleY", "1.0"));
		this.color = Color.web(props.getString("color", "#ffffff"));
		this.glowness.setExpr(props.getString("glowness", "0.0"));
		
		this.updateType();
		
		if(this.target != null) {
			this.target.set(targetProps);
		}
	}
	
	public void refreshProperties(FrameProperties props) {
		this.opacity.refresh(props);
		this.posX.refresh(props);
		this.posY.refresh(props);
		this.rotation.refresh(props);
		this.scaleX.refresh(props);
		this.scaleY.refresh(props);
		this.glowness.refresh(props);
		
		if(this.target != null) this.target.refreshOwnProperties(props);
		
		ExpressionEngine.ENGINE.refreshDirty();
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
	
	private List<Consumer<Section>> disposeListeners = new ArrayList<Consumer<Section>>();
	
	public void addDisposeListener(Consumer<Section> r) {
		this.disposeListeners.add(r);
	}
	
	public void removeDisposeListener(Consumer<Section> r) {
		this.disposeListeners.remove(r);
	}
	
	public void clearDisposeListeners() {
		this.disposeListeners.clear();
	}
	
	public void dispose() {
		for(Consumer<Section> r : this.disposeListeners) {
			r.accept(this);
		}
		
		this.opacity.dispose();
		this.posX.dispose();
		this.posY.dispose();
		this.rotation.dispose();
		this.scaleX.dispose();
		this.scaleY.dispose();
		this.glowness.dispose();
		
		if(this.target != null) this.target.dispose();
		
		this.clearDisposeListeners();
	}
}
