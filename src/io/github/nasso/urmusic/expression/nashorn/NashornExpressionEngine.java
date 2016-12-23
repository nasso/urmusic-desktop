package io.github.nasso.urmusic.expression.nashorn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.expression.ExpressionEngine;
import io.github.nasso.urmusic.expression.ExpressionGetter;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class NashornExpressionEngine implements ExpressionEngine {
	private NashornScriptEngine scriptEngine;
	private ScriptObjectMirror func_compileExpr;
	private ScriptObjectMirror func_disposeExpr;
	private ScriptObjectMirror func_setExpr;
	private ScriptObjectMirror func_recalcAllExpr;
	private ScriptObjectMirror func_recalcExpr;
	
	ScriptObjectMirror exprResults;
	
	private Map<String, FrameProperties> refreshRequests = new HashMap<String, FrameProperties>();
	
	public NashornExpressionEngine() {
		scriptEngine = (NashornScriptEngine) new ScriptEngineManager().getEngineByName("nashorn");
		
		try {
			scriptEngine.eval(new BufferedReader(new InputStreamReader(NashornExpressionEngine.class.getClassLoader().getResourceAsStream("res/mainlib.min.js"))));
			scriptEngine.eval(new BufferedReader(new InputStreamReader(NashornExpressionEngine.class.getClassLoader().getResourceAsStream("res/nashorn/nsmirror.min.js"))));
		} catch(ScriptException e) {
			e.printStackTrace();
		}
		
		this.exprResults = (ScriptObjectMirror) scriptEngine.get("exprResults");
		this.func_compileExpr = (ScriptObjectMirror) scriptEngine.get("compileExpr");
		this.func_disposeExpr = (ScriptObjectMirror) scriptEngine.get("disposeExpr");
		this.func_setExpr = (ScriptObjectMirror) scriptEngine.get("setExpr");
		this.func_recalcAllExpr = (ScriptObjectMirror) scriptEngine.get("recalcAllExpr");
		this.func_recalcExpr = (ScriptObjectMirror) scriptEngine.get("recalcExpr");
	}
	
	public ExpressionGetter compile(String expr) {
		try {
			return new NashornExpressionGetter(this, Float.valueOf(expr));
		} catch(Exception e) {
			
		}
		
		NashornExpressionGetter getter = new NashornExpressionGetter(this);
		this.func_compileExpr.call(null, getter.hashCodeStr, expr);
		
		return getter;
	}
	
	public void disposeGetter(NashornExpressionGetter getter) {
		this.func_disposeExpr.call(null, getter.hashCodeStr);
	}
	
	public void setGetterExpr(NashornExpressionGetter getter, String expr) {
		this.func_setExpr.call(null, getter.hashCodeStr, expr);
	}
	
	public void update(FrameProperties props) {
		this.func_recalcAllExpr.call(null, props);
	}
	
	public void refreshExpr(NashornExpressionGetter getter, FrameProperties props) {
		refreshRequests.put(getter.hashCodeStr, props);
	}
	
	public void refreshDirty() {
		this.func_recalcExpr.call(null, this.refreshRequests);
		refreshRequests.clear();
	}
}
