package io.github.nasso.urmusic.audio;

import java.util.Arrays;

import io.github.nasso.urmusic.Urmusic;

public class AnalyseData {
	public float[] freqData = new float[Urmusic.FFTSIZE_HALF];
	public float[] timeDomData = new float[Urmusic.FFTSIZE];
	
	public float maxval = -Float.MAX_VALUE;
	public float minval = -Float.MAX_VALUE;
	
	public float maxlowval = -Float.MAX_VALUE;
	public float minlowval = -Float.MAX_VALUE;
	
	public float maxhighval = -Float.MAX_VALUE;
	public float minhighval = -Float.MAX_VALUE;
	
	public AnalyseData() {
		Arrays.fill(this.freqData, -Float.MAX_VALUE);
		Arrays.fill(this.timeDomData, 0.0f);
	}
}
