package io.github.nasso.urmusic.expression;

import io.github.nasso.urmusic.core.FrameProperties;

public interface ExpressionGetter {
	public Object get();
	
	public float getAsFloat();
	
	public int getAsInt();
	
	public ExpressionEngine getEngine();
	
	public void setExpr(String expr);
	
	public void refresh(FrameProperties props);
	
	public void dispose();
}
