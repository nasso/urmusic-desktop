package io.github.nasso.urmusic.expression;

import io.github.nasso.urmusic.ApplicationPreferences;
import io.github.nasso.urmusic.core.FrameProperties;

public interface ExpressionEngine {
	public static final ExpressionEngine ENGINE = ExpressionEngines.get(ApplicationPreferences.expressionEngineName);
	
	public void update(FrameProperties prop);
	
	public ExpressionGetter compile(String expr);
	
	public void refreshDirty();
}
