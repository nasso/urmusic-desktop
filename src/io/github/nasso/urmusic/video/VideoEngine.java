package io.github.nasso.urmusic.video;

import io.github.nasso.urmusic.ApplicationPreferences;

public interface VideoEngine {
	public static final VideoEngine ENGINE = VideoEngines.get(ApplicationPreferences.videoEngineName);
	
	public VideoStream createStream(VideoExportSettings settings) throws Exception;
}
