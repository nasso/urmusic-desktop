package io.github.nasso.urmusic.core;

import javafx.scene.shape.StrokeLineJoin;

public class TimeDomSection extends AnalyserSection {
	public StrokeLineJoin lineJoin = StrokeLineJoin.ROUND;
	
	public TimeDomSection() {
		
	}
	
	public TimeDomSection(PrimitiveProperties p) {
		super(p);
	}
	
	public void set(PrimitiveProperties p) {
		super.set(p);
		
		this.lineJoin = StrokeLineJoin.valueOf(p.getString("lineJoin", "ROUND").toUpperCase());
	}
}
