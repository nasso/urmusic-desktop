package io.github.nasso.urmusic.audio;

import java.io.File;

import io.github.nasso.urmusic.ApplicationPreferences;

public interface AudioEngine {
	public static final AudioEngine ENGINE = AudioEngines.get(ApplicationPreferences.audioEngineName);
	
	public Sound loadSound(File f);
}
