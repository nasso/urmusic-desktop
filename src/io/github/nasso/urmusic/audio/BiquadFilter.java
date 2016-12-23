package io.github.nasso.urmusic.audio;

import io.github.nasso.urmusic.Utils;

public class BiquadFilter {
	// http://blog.bjornroche.com/2012/08/basic-audio-eqs.html
	// http://www.musicdsp.org/files/Audio-EQ-Cookbook.txt
	public static enum Type {
		LOWPASS, HIGHPASS
	}
	
	private Type type;
	private double cutoff, res;
	private double b0, b1, b2, a1, a2;
	private double x1, x2, y1, y2;
	
	public BiquadFilter(double cutoff, double res, Type type) {
		this.type = type;
		this.set(cutoff, res);
	}
	
	public void set(double cutoff, double res) {
		if(this.cutoff == cutoff && this.res == res) return;
		
		this.cutoff = Utils.clamp(cutoff, 0.0, 1.0);
		this.res = Math.max(0.0, res);
		
		switch(this.type) {
			case LOWPASS:
				this.setLowpass(this.cutoff, this.res);
				break;
			case HIGHPASS:
				this.setHighpass(this.cutoff, this.res);
				break;
		}
	}
	
	private void setLowpass(double cutoff, double res) {
		if(cutoff == 1.0) {
			this.setNormalizedCoefficients(1, 0, 0, 1, 0, 0);
		} else if(cutoff > 0.0) {
			double g = Math.pow(10.0, -0.05 * res);
			double w0 = Math.PI * cutoff;
			double c = Math.cos(w0);
			double alpha = 0.5 * Math.sin(w0) * g;
			
			double b1 = 1.0 - c;
			double b0 = 0.5 * b1;
			double b2 = b0;
			double a0 = 1.0 + alpha;
			double a1 = -2.0 * c;
			double a2 = 1.0 - alpha;
			
			this.setNormalizedCoefficients(b0, b1, b2, a0, a1, a2);
		} else {
			this.setNormalizedCoefficients(0, 0, 0, 1, 0, 0);
		}
	}
	
	private void setHighpass(double cutoff, double res) {
		if(cutoff == 1.0) {
			this.setNormalizedCoefficients(0, 0, 0, 1, 0, 0);
		} else if(cutoff > 0.0) {
			double g = Math.pow(10.0, -0.05 * res);
			double w0 = Math.PI * cutoff;
			double c = Math.cos(w0);
			double alpha = 0.5 * Math.sin(w0) * g;
			
			double b1 = -1.0 - c;
			double b0 = -0.5 * b1;
			double b2 = b0;
			double a0 = 1.0 + alpha;
			double a1 = -2.0 * c;
			double a2 = 1.0 - alpha;
			
			this.setNormalizedCoefficients(b0, b1, b2, a0, a1, a2);
		} else {
			this.setNormalizedCoefficients(1, 0, 0, 1, 0, 0);
		}
	}
	
	private void setNormalizedCoefficients(double b0, double b1, double b2, double a0, double a1, double a2) {
		this.b0 = b0 / a0;
		this.b1 = b1 / a0;
		this.b2 = b2 / a0;
		this.a1 = a1 / a0;
		this.a2 = a2 / a0;
	}
	
	public double process(double x) {
		double y = this.b0 * x + this.b1 * this.x1 + this.b2 * this.x2 - this.a1 * this.y1 - this.a2 * this.y2;
		
		this.x2 = this.x1;
		this.x1 = x;
		this.y2 = this.y1;
		this.y1 = y;
		
		if(this.x1 == 0.0 && this.x2 == 0.0 && (this.y1 != 0.0 || this.y2 != 0.0) && Math.abs(this.y1) < Float.MIN_VALUE && Math.abs(this.y2) < Float.MIN_VALUE) {
			this.y1 = this.y2 = 0.0;
			
			if(Math.abs(y) < Float.MIN_VALUE) y = 0.0;
		}
		
		return y;
	}
}
