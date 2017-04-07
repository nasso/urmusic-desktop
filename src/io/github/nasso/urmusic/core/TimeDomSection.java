package io.github.nasso.urmusic.core;

import javafx.scene.shape.StrokeLineJoin;

public class TimeDomSection extends AnalyserSection {
	public static final SectionType THIS_TYPE = SectionType.TIME_DOM;
	
	public StrokeLineJoin lineJoin = StrokeLineJoin.ROUND;
	
	public TimeDomSection() {
		
	}
	
	public TimeDomSection(PrimitiveProperties p) {
		super(p);
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public void set(PrimitiveProperties p) {
		super.set(p);
		
		this.lineJoin = StrokeLineJoin.valueOf(p.getString("lineJoin", "ROUND").toUpperCase());
	}
	
	public void set(SectionTarget other) {
		if(!(other instanceof TimeDomSection)) return;
		
		super.set(other);
		TimeDomSection ts = (TimeDomSection) other;
		this.lineJoin = ts.lineJoin;
	}
}
