package io.github.nasso.urmusic.video;

public class VideoExportSettings {
	public String inputSoundFile = "";
	public String outputFile = "";
	
	public int constantRateFactor = 23;
	public int audioSampleRate = 48000;
	
	public int width = 1280;
	public int height = 720;
	public double framerate = 60;
	
	public double durationSec = 0.0;
	
	public boolean motionBlur = true;
}
