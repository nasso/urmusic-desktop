package io.github.nasso.urmusic.expression;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.expression.nashorn.NashornExpressionEngine;

public class ExpressionEngines {
	private ExpressionEngines() {
	}
	
	private static Map<String, ExpressionEngine> engines = new HashMap<String, ExpressionEngine>();
	
	static {
		engines.put("nashorn", new NashornExpressionEngine());
	}
	
	public static ExpressionEngine get(String name) {
		return engines.get(name);
	}
}
