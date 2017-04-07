package io.github.nasso.urmusic.ui;

import java.text.NumberFormat;
import java.text.ParsePosition;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class NumberTextField extends TextField {
	private double min, max;
	
	public NumberTextField(NumberFormat format, double min, double max) {
		super();
		
		this.min = min;
		this.max = max;
		
		this.setTextFormatter(new TextFormatter<>((c) -> {
			if(c.getControlNewText().isEmpty()) return c;
			
			ParsePosition parsePosition = new ParsePosition(0);
			Number num = format.parse(c.getControlNewText(), parsePosition);
			
			if(num == null || parsePosition.getIndex() < c.getControlNewText().length() || num.doubleValue() < this.min || num.doubleValue() > this.max) return null;
			
			return c;
		}));
	}
	
	public double getMin() {
		return this.min;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public double getMax() {
		return this.max;
	}
	
	public void setMax(double max) {
		this.max = max;
	}
	
	public void setValue(double value) {
		this.setText(String.valueOf(value));
	}
	
	public void setValue(int value) {
		this.setText(String.valueOf(value));
	}
}
