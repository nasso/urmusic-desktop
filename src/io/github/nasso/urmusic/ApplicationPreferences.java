package io.github.nasso.urmusic;

public class ApplicationPreferences {
	public static String expressionEngineName = "nashorn";
	public static String fftEngineName = "jtransforms";
	public static String jsonEngineName = "gson";
	public static String audioEngineName = "javaSound";
	public static String videoEngineName = "ffmpeg";
	public static String ffmpegLocation = "lib/ffmpeg.exe";
	
	public static boolean playSoundOnLoad = false;
	
	public static double audioVolume = 0.8;
	public static double audioAnalysisFramerate = 60;
}
