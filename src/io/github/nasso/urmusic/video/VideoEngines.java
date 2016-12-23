package io.github.nasso.urmusic.video;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.video.ffmpeg.FFmpegVideoEngine;

public class VideoEngines {
	private VideoEngines() {
	}
	
	private static Map<String, VideoEngine> engines = new HashMap<String, VideoEngine>();
	
	static {
		engines.put("ffmpeg", new FFmpegVideoEngine());
	}
	
	public static VideoEngine get(String name) {
		return engines.get(name);
	}
}
