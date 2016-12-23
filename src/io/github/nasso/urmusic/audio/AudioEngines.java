package io.github.nasso.urmusic.audio;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.audio.javafx.JavaFXAudioEngine;
import io.github.nasso.urmusic.audio.javasound.JavaSoundAudioEngine;

public class AudioEngines {
	private AudioEngines() {
	}
	
	private static Map<String, AudioEngine> engines = new HashMap<String, AudioEngine>();
	
	static {
		engines.put("javafx", new JavaFXAudioEngine());
		engines.put("javaSound", new JavaSoundAudioEngine());
	}
	
	public static AudioEngine get(String name) {
		return engines.get(name);
	}
}
