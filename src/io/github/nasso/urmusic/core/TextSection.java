package io.github.nasso.urmusic.core;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;

public class TextSection implements SectionTarget {
	public static final SectionType THIS_TYPE = SectionType.TEXT;
	
	public ExpressionProperty text = new ExpressionProperty("\"Type your text here\"");
	public String fontFamily = "sans-serif";
	public ExpressionProperty fontSize = new ExpressionProperty("0.2");
	public String fontStyle = "normal";
	public TextAlignment textAlign = TextAlignment.CENTER;
	public VPos textBaseline = VPos.BASELINE;
	
	public TextSection() {
		
	}
	
	public TextSection(PrimitiveProperties p) {
		this.set(p);
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public void dispose() {
		this.text.dispose();
		this.fontSize.dispose();
	}
	
	public void set(PrimitiveProperties p) {
		this.text.setExpr(p.getString("text", "\"Type your text here\""));
		this.fontFamily = p.getString("fontFamily", "sans-serif");
		this.fontSize.setExpr(p.getString("fontSize", "0.2"));
		this.fontStyle = p.getString("fontStyle", "regular");
		this.textAlign = TextAlignment.valueOf(p.getString("textAlign", "CENTER").toUpperCase());
		
		String baseline = p.getString("textBaseline", "BASELINE");
		if(baseline.equalsIgnoreCase("ALPHABETIC")) baseline = "BASELINE";
		else if(baseline.equalsIgnoreCase("HANGING")) baseline = "TOP";
		else if(baseline.equalsIgnoreCase("MIDDLE")) baseline = "CENTER";
		
		this.textBaseline = VPos.valueOf(baseline);
	}
	
	public void set(SectionTarget other) {
		if(!(other instanceof TextSection)) return;
		
		TextSection ts = (TextSection) other;
		this.text.setExpr(ts.text.getExpr());
		this.fontFamily = ts.fontFamily;
		this.fontSize.setExpr(ts.fontSize.getExpr());
		this.fontStyle = ts.fontStyle;
		this.textAlign = ts.textAlign;
		this.textBaseline = ts.textBaseline;
	}
	
	public void refreshOwnProperties(FrameProperties props) {
		this.text.refresh(props);
	}
}
