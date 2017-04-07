package io.github.nasso.urmusic.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.nasso.urmusic.expression.ExpressionEngine;
import io.github.nasso.urmusic.expression.ExpressionProperty;

public abstract class SectionGroupElement {
	public String name = "Unnamed";
	public boolean visible = true;
	public ExpressionProperty opacity = new ExpressionProperty("1.0");
	public ExpressionProperty posX = new ExpressionProperty("0.0");
	public ExpressionProperty posY = new ExpressionProperty("0.0");
	public ExpressionProperty rotation = new ExpressionProperty("0.0");
	public ExpressionProperty scaleX = new ExpressionProperty("1.0");
	public ExpressionProperty scaleY = new ExpressionProperty("1.0");
	
	public SectionGroupElement() {
		
	}
	
	public SectionGroupElement(SectionGroupElement other) {
		this.name = other.name + " - Clone";
		this.visible = other.visible;
		this.opacity.setExpr(other.opacity.getExpr());
		this.posX.setExpr(other.posX.getExpr());
		this.posY.setExpr(other.posY.getExpr());
		this.rotation.setExpr(other.rotation.getExpr());
		this.scaleX.setExpr(other.scaleX.getExpr());
		this.scaleY.setExpr(other.scaleY.getExpr());
	}
	
	public SectionGroupElement(PrimitiveProperties props) {
		if(props != null) {
			this.name = props.getString("name", this.name);
			this.visible = props.getBool("visible", true);
			this.opacity.setExpr(props.getString("opacity", "1.0"));
			this.posX.setExpr(props.getString("posX", "0.0"));
			this.posY.setExpr(props.getString("posY", "0.0"));
			this.rotation.setExpr(props.getString("rotation", "0.0"));
			this.scaleX.setExpr(props.getString("scaleX", "1.0"));
			this.scaleY.setExpr(props.getString("scaleY", "1.0"));
		}
	}
	
	public void refreshProperties(FrameProperties props) {
		this.opacity.refresh(props);
		this.posX.refresh(props);
		this.posY.refresh(props);
		this.rotation.refresh(props);
		this.scaleX.refresh(props);
		this.scaleY.refresh(props);
		
		ExpressionEngine.ENGINE.refreshDirty();
	}
	
	private List<Consumer<SectionGroupElement>> disposeListeners = new ArrayList<Consumer<SectionGroupElement>>();
	
	public void addDisposeListener(Consumer<SectionGroupElement> r) {
		this.disposeListeners.add(r);
	}
	
	public void removeDisposeListener(Consumer<SectionGroupElement> r) {
		this.disposeListeners.remove(r);
	}
	
	public void clearDisposeListeners() {
		this.disposeListeners.clear();
	}
	
	public void dispose() {
		for(Consumer<SectionGroupElement> r : this.disposeListeners) {
			r.accept(this);
		}
		
		this.clearDisposeListeners();
		
		this.opacity.dispose();
		this.posX.dispose();
		this.posY.dispose();
		this.rotation.dispose();
		this.scaleX.dispose();
		this.scaleY.dispose();
	}
	
	public String toString() {
		return this.name;
	}
}
