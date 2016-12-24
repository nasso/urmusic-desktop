package io.github.nasso.urmusic.audio.javafx;

import java.io.File;

import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.audio.AudioEngine;
import io.github.nasso.urmusic.audio.Sound;

public class JavaFXAudioEngine implements AudioEngine {
	public Sound loadSound(File f) {
		try {
			JFXSound sound = new JFXSound(f);
			return sound;
		} catch(OutOfMemoryError e) {
			Urmusic.showError("Out of memory! The audio file is to big, and cannot be loaded!");
		} catch(Exception e) {
			if(Urmusic.DEBUG) e.printStackTrace();
		}
		
		return null;
	}
}
