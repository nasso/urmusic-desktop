package io.github.nasso.urmusic.core;

import io.github.nasso.urmusic.expression.ExpressionProperty;

public class FreqSection extends AnalyserSection {
	public ExpressionProperty minDecibels = new ExpressionProperty("-100");
	public ExpressionProperty maxDecibels = new ExpressionProperty("-20");
	public ExpressionProperty minHeight = new ExpressionProperty("0.01");
	public ExpressionProperty freqStart = new ExpressionProperty("0");
	public ExpressionProperty freqEnd = new ExpressionProperty("0.03");
	public boolean clampToMaxDecibels = true;
	
	public FreqSection() {
		
	}
	
	public FreqSection(PrimitiveProperties p) {
		super(p);
	}
	
	public void dispose() {
		super.dispose();
		
		this.minDecibels.dispose();
		this.maxDecibels.dispose();
		this.minHeight.dispose();
		this.freqStart.dispose();
		this.freqEnd.dispose();
	}
	
	public void set(PrimitiveProperties p) {
		super.set(p);
		
		this.minDecibels.setExpr(p.getString("minDecibels", "-100"));
		this.maxDecibels.setExpr(p.getString("maxDecibels", "-20"));
		this.minHeight.setExpr(p.getString("minHeight", "0.01"));
		this.freqStart.setExpr(p.getString("freqStart", "0"));
		this.freqEnd.setExpr(p.getString("freqEnd", "0.03"));
		this.clampToMaxDecibels = p.getBool("clampToMaxDecibels", false); // For backward compatibility
	}
	
	public void refreshOwnProperties(FrameProperties props) {
		super.refreshOwnProperties(props);
		
		this.minDecibels.refresh(props);
		this.maxDecibels.refresh(props);
		this.minHeight.refresh(props);
		this.freqStart.refresh(props);
		this.freqEnd.refresh(props);
	}
}
