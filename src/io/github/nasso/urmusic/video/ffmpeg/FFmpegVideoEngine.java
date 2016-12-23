package io.github.nasso.urmusic.video.ffmpeg;

import java.io.File;
import java.io.IOException;

import io.github.nasso.urmusic.video.VideoEngine;
import io.github.nasso.urmusic.video.VideoExportSettings;
import io.github.nasso.urmusic.video.VideoStream;

public class FFmpegVideoEngine implements VideoEngine {
	public VideoStream createStream(VideoExportSettings settings) throws IOException {
		// @format:off
		if(
				settings == null ||
				settings.width <= 0 ||
				settings.height <= 0 ||
				settings.durationSec <= 0.0f ||
				settings.audioSampleRate <= 0.0 ||
				settings.constantRateFactor < 0 || settings.constantRateFactor > 51 ||
				settings.framerate <= 0 ||
				settings.inputSoundFile.equals("") ||
				settings.outputFile.equals("") ||
				!(new File(settings.inputSoundFile)).exists())
			return null;
		// @format:on
		
		return new FFmpegVideoStream(settings);
	}
}
