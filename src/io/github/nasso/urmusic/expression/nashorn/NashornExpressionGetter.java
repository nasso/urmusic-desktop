package io.github.nasso.urmusic.expression.nashorn;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.expression.ExpressionEngine;
import io.github.nasso.urmusic.expression.ExpressionGetter;

public class NashornExpressionGetter implements ExpressionGetter {
	private NashornExpressionEngine engine;
	
	private float constant = Float.NaN;
	
	public NashornExpressionGetter(NashornExpressionEngine engine) {
		this.engine = engine;
	}
	
	public NashornExpressionGetter(NashornExpressionEngine engine, float f) {
		this.engine = engine;
		this.constant = f;
	}
	
	public Object get() {
		return this.engine.exprResults.get(this.hashCodeStr);
	}
	
	public float getAsFloat() {
		if(!Float.isNaN(this.constant)) return this.constant;
		
		Object o = this.get();
		
		if(o instanceof Number) return ((Number) o).floatValue();
		
		return 0.123456f;
	}
	
	public int getAsInt() {
		if(!Float.isNaN(this.constant)) return (int) this.constant;
		
		Object o = this.get();
		
		if(o instanceof Double) return (int) (double) o;
		
		return 0;
	}
	
	public void dispose() {
		this.engine.disposeGetter(this);
	}
	
	public ExpressionEngine getEngine() {
		return this.engine;
	}
	
	private static int hashCodeCounter = -Integer.MAX_VALUE;
	public final String hashCodeStr = String.valueOf(hashCodeCounter++);
	
	public void setExpr(String expr) {
		try {
			this.constant = Float.valueOf(expr);
			
			// Dispose the JS side getter, because it won't be used (the expression is constant)
			this.engine.disposeGetter(this);
		} catch(Exception e) {
			this.constant = Float.NaN;
			this.engine.setGetterExpr(this, expr);
		}
	}
	
	public void refresh(FrameProperties props) {
		if(!Float.isNaN(this.constant)) return;
		
		this.engine.refreshExpr(this, props);
	}
}
