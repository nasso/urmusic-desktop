package io.github.nasso.urmusic.core;

public class AdvancedSettings {
	public boolean enableLowpass = false;
	public boolean enableHighpass = false;
	
	public float lowpassFreq = 120;
	public float highpassFreq = 480;
	
	public float lowpassSmooth = 0.65f;
	public float highpassSmooth = 0.65f;
	
	public AdvancedSettings() {
		
	}
	
	public AdvancedSettings(PrimitiveProperties props) {
		this.set(props);
	}
	
	public void set(PrimitiveProperties props) {
		this.enableLowpass = props.getBool("enableLowpass", false);
		this.enableHighpass = props.getBool("enableHighpass", false);
		
		this.lowpassFreq = props.getNumber("lowpassFreq", 120);
		this.highpassFreq = props.getNumber("highpassFreq", 480);
		
		this.lowpassSmooth = props.getNumber("lowpassSmooth", 0.65f);
		this.highpassSmooth = props.getNumber("highpassSmooth", 0.65f);
	}
}
