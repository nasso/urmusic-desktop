package io.github.nasso.urmusic.core;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.shape.StrokeLineCap;

public abstract class AnalyserSection implements SectionTarget {
	public ExpressionProperty dataCount = new ExpressionProperty("128");
	public ExpressionProperty lineWidth = new ExpressionProperty("1");
	public StrokeLineCap lineCap = StrokeLineCap.BUTT;
	public ExpressionProperty startX = new ExpressionProperty("-1");
	public ExpressionProperty endX = new ExpressionProperty("1");
	public ExpressionProperty offsetY = new ExpressionProperty("0");
	public ExpressionProperty exponent = new ExpressionProperty("1");
	public ExpressionProperty height = new ExpressionProperty("0.5");
	public DrawMode mode = DrawMode.LINES;
	public ExpressionProperty polar = new ExpressionProperty("0");
	public boolean clampShapeToZero = true;
	public boolean closeShape = true;
	public boolean drawLast = true;
	public boolean quadratic = true;
	public boolean smartFill = false;
	
	public AnalyserSection() {
		
	}
	
	public AnalyserSection(PrimitiveProperties p) {
		this.set(p);
	}
	
	public void dispose() {
		this.dataCount.dispose();
		this.lineWidth.dispose();
		this.startX.dispose();
		this.endX.dispose();
		this.offsetY.dispose();
		this.exponent.dispose();
		this.height.dispose();
		this.polar.dispose();
	}
	
	public void set(PrimitiveProperties p) {
		this.dataCount.setExpr(p.getString("dataCount", "128"));
		this.lineWidth.setExpr(p.getString("lineWidth", "1"));
		this.lineCap = StrokeLineCap.valueOf(p.getString("lineCap", "BUTT").toUpperCase());
		this.startX.setExpr(p.getString("startX", "-1"));
		this.endX.setExpr(p.getString("endX", "1"));
		this.offsetY.setExpr(p.getString("offsetY", p.getString("yPos", "0"))); // offsetY was yPos back in the times
		this.exponent.setExpr(p.getString("exponent", "1"));
		this.height.setExpr(p.getString("height", "0.5"));
		this.mode = DrawMode.valueOf(p.getString("mode", "LINES").toUpperCase());
		this.polar.setExpr(p.getString("polar", "0"));
		this.clampShapeToZero = p.getBool("clampShapeToZero", true);
		this.closeShape = p.getBool("closeShape", true);
		this.drawLast = p.getBool("drawLast", true);
		this.quadratic = p.getBool("quadratic", true);
		this.smartFill = p.getBool("smartFill", false);
	}
	
	public void refreshOwnProperties(FrameProperties props) {
		this.dataCount.refresh(props);
		this.lineWidth.refresh(props);
		this.startX.refresh(props);
		this.endX.refresh(props);
		this.offsetY.refresh(props);
		this.exponent.refresh(props);
		this.height.refresh(props);
		this.polar.refresh(props);
	}
}
