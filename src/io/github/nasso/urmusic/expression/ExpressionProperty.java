package io.github.nasso.urmusic.expression;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.json.JSONEngine;
import io.github.nasso.urmusic.json.JSONSerializable;

public class ExpressionProperty implements JSONSerializable {
	private String expr;
	private ExpressionGetter getter;
	
	private ExpressionProperty binding;
	
	public ExpressionProperty(ExpressionProperty prop) {
		this(prop.expr);
	}
	
	public ExpressionProperty(float v) {
		this(String.valueOf(v));
	}
	
	public ExpressionProperty(String v) {
		this.setExpr(v);
	}
	
	public void bind(ExpressionProperty prop) {
		if(!prop.searchFor(this)) this.binding = prop;
	}
	
	private boolean searchFor(ExpressionProperty prop) {
		if(this == prop) return true;
		if(this.binding != null) return this.binding.searchFor(prop);
		
		return false;
	}
	
	public void dispose() {
		this.getter.dispose();
	}
	
	public String getExpr() {
		return expr;
	}
	
	public void setExpr(String expr) {
		if(expr.equals(this.expr)) return;
		
		this.expr = expr;
		
		if(this.getter != null) this.getter.setExpr(expr);
		else this.getter = ExpressionEngine.ENGINE.compile(this.expr);
	}
	
	public Object getValue() {
		if(this.binding != null) return this.binding.getValue();
		
		return this.getter.get();
	}
	
	public float getValueAsFloat() {
		if(this.binding != null) return this.binding.getValueAsFloat();
		
		return this.getter.getAsFloat();
	}
	
	public int getValueAsInt() {
		if(this.binding != null) return this.binding.getValueAsInt();
		
		return this.getter.getAsInt();
	}
	
	public void refresh(FrameProperties props) {
		this.getter.refresh(props);
	}
	
	public String toString() {
		return this.expr;
	}
	
	public Object toJSON(JSONEngine e) {
		return this.expr;
	}
}
