package io.github.nasso.urmusic.audio;

import java.io.File;

public interface Sound {
	public static final double CURRENT_TIME = -1;
	
	public File getSourceFile();
	
	public void play();
	
	public void stop();
	
	public void pause();
	
	public void dispose();
	
	public double getPosition();
	
	public void setPosition(double seconds);
	
	public double getVolume();
	
	public void setVolume(double volume);
	
	public double getDuration();
	
	public void startAnalysis(double framerate);
	
	public void stopAnalysis();
	
	public void setLowpassFreq(double freq);
	
	public void setHighpassFreq(double freq);
	
	public void setSmoothingTimeConstant(double c);
	
	public void setHighSmoothingTimeConstant(double c);
	
	public void setLowSmoothingTimeConstant(double c);
	
	public void setHighpassEnabled(boolean enabled);
	
	public void setLowpassEnabled(boolean enabled);
	
	public void resetSmoothingBuffer();
	
	public void getAnalysedData(AnalyseData dest, double timeSeconds);
}
