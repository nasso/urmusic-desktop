package io.github.nasso.urmusic.expression;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.json.JSONEngine;
import io.github.nasso.urmusic.json.JSONSerializable;

public class ExpressionProperty implements JSONSerializable {
	private String expr;
	private ExpressionGetter getter;
	
	public ExpressionProperty(ExpressionProperty prop) {
		this(prop.expr);
	}
	
	public ExpressionProperty(float v) {
		this(String.valueOf(v));
	}
	
	public ExpressionProperty(String v) {
		this.setExpr(v);
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
		return this.getter.get();
	}
	
	public float getValueAsFloat() {
		return this.getter.getAsFloat();
	}
	
	public int getValueAsInt() {
		return this.getter.getAsInt();
	}
	
	public void refresh(FrameProperties props) {
		this.getter.refresh(props);
	}
	
	public Object toJSON(JSONEngine e) {
		return this.expr;
	}
}
