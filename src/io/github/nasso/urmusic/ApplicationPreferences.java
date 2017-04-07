package io.github.nasso.urmusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ApplicationPreferences {
	public static String expressionEngineName = "nashorn";
	public static String fftEngineName = "jtransforms";
	public static String jsonEngineName = "gson";
	public static String audioEngineName = "javaSound";
	public static String videoEngineName = "ffmpeg";
	public static String ffmpegLocation = "lib/ffmpeg.exe";
	
	public static boolean playSoundOnLoad = false;
	
	public static double treePanelSize = 200;
	public static double audioVolume = 0.8;
	public static double audioAnalysisFramerate = 60;
	
	public static void load(File file) throws IOException {
		if(!file.exists()) return;
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		Map<String, String> values = new HashMap<String, String>();
		String line;
		while((line = in.readLine()) != null) {
			if(line.matches(".+=.*")) {
				String[] splitted = line.split("=", 2);
				values.put(splitted[0], splitted[1]);
			}
		}
		
		in.close();
		
		expressionEngineName = values.getOrDefault("expressionEngineName", "nashorn");
		fftEngineName = values.getOrDefault("fftEngineName", "jtransforms");
		jsonEngineName = values.getOrDefault("jsonEngineName", "gson");
		audioEngineName = values.getOrDefault("audioEngineName", "javaSound");
		videoEngineName = values.getOrDefault("videoEngineName", "ffmpeg");
		ffmpegLocation = values.getOrDefault("ffmpegLocation", "lib/ffmpeg.exe");
		
		playSoundOnLoad = Boolean.parseBoolean(values.getOrDefault("playSoundOnLoad", "false"));
		
		treePanelSize = Double.parseDouble(values.getOrDefault("treePanelSize", "300"));
		audioVolume = Double.parseDouble(values.getOrDefault("audioVolume", "0.8"));
		audioAnalysisFramerate = Double.parseDouble(values.getOrDefault("audioAnalysisFramerate", "60"));
	}
	
	public static void save(File file) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		
		Field[] fields = ApplicationPreferences.class.getFields();
		try {
			for(Field f : fields) {
				out.write(f.getName() + "=" + f.get(null) + System.lineSeparator());
			}
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		
		out.close();
	}
}
