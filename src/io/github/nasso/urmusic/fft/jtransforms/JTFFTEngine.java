package io.github.nasso.urmusic.fft.jtransforms;

import org.jtransforms.fft.FloatFFT_1D;

import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.fft.FFTEngine;

public class JTFFTEngine implements FFTEngine {
	private FloatFFT_1D fft;
	private float[] altBuffer = new float[Urmusic.FFTSIZE];
	
	public JTFFTEngine() {
		this.fft = new FloatFFT_1D(Urmusic.FFTSIZE);
	}
	
	public float[] fft(float[] in, float[] out) {
		System.arraycopy(in, 0, altBuffer, 0, in.length);
		this.fft.realForward(altBuffer);
		
		for(int i = 0, l = Urmusic.FFTSIZE_HALF; i < l; i++) {
			out[i] = (float) Math.hypot(altBuffer[i * 2], altBuffer[i * 2 + 1]);
		}
		
		return out;
	}
}
