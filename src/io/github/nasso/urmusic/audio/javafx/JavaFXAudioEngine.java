package io.github.nasso.urmusic.audio.javafx;

import java.io.File;

import io.github.nasso.urmusic.TaskProperties;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.audio.AudioEngine;
import io.github.nasso.urmusic.audio.Sound;

public class JavaFXAudioEngine implements AudioEngine {
	public Sound loadSound(File f, TaskProperties props) {
		// TODO: implement TaskProperties for JFX audio engine
		try {
			JFXSound sound = new JFXSound(f);
			return sound;
		} catch(Exception e) {
			if(Urmusic.DEBUG) e.printStackTrace();
			return null;
		}
	}
}
