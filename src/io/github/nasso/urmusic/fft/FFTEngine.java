package io.github.nasso.urmusic.fft;

import io.github.nasso.urmusic.ApplicationPreferences;

public interface FFTEngine {
	public static final FFTEngine ENGINE = FFTEngines.get(ApplicationPreferences.fftEngineName);
	
	public float[] fft(float[] in, float[] out);
}
