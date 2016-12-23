package io.github.nasso.urmusic.fft;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.fft.jtransforms.JTFFTEngine;

public class FFTEngines {
	private FFTEngines() {
	}
	
	private static Map<String, FFTEngine> engines = new HashMap<String, FFTEngine>();
	
	static {
		engines.put("jtransforms", new JTFFTEngine());
	}
	
	public static FFTEngine get(String name) {
		return engines.get(name);
	}
}
