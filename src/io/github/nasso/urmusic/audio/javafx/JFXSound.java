package io.github.nasso.urmusic.audio.javafx;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import io.github.nasso.urmusic.UrAnimationTimer;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.audio.AnalyseData;
import io.github.nasso.urmusic.audio.BiquadFilter;
import io.github.nasso.urmusic.audio.Sound;
import io.github.nasso.urmusic.fft.FFTEngine;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class JFXSound implements Sound {
	// WARNING: There's a bug in JavaFX, it doesn't support mp3 with image covers. So yeah, JSound is the best here.
	
	private File sourceFile;
	private MediaPlayer med = null;
	
	// Java Sound API stuff, for analysis
	private AudioInputStream in;
	private AudioInputStream din;
	private AudioFormat baseFormat;
	
	private float[] audioData;
	
	private float nyquist;
	private double duration;
	
	private BiquadFilter lowpassfilter;
	private BiquadFilter highpassfilter;
	
	private double smoothingTimeConstant;
	private double highSmoothingTimeConstant;
	private double lowSmoothingTimeConstant;
	
	private boolean lowpassEnabled = false;
	private boolean highpassEnabled = false;
	
	private UrAnimationTimer analyseTimer;
	
	public JFXSound(File f) {
		this.sourceFile = f;
		this.med = new MediaPlayer(new Media(f.toURI().toString()));
		this.med.setOnReady(() -> {
			this.med.setCycleCount(MediaPlayer.INDEFINITE);
			this.duration = (float) this.med.getCycleDuration().toSeconds();
		});
		
		try {
			// Reset Input streams for PCM stuff
			this.in = AudioSystem.getAudioInputStream(f);
			this.baseFormat = in.getFormat();
			
			this.nyquist = baseFormat.getSampleRate() / 2.0f;
			this.lowpassfilter = new BiquadFilter(0.05, 0.0, BiquadFilter.Type.LOWPASS);
			this.highpassfilter = new BiquadFilter(0.1, 0.0, BiquadFilter.Type.HIGHPASS);
			
			// @format:off
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(),
					16,
					baseFormat.getChannels(),
					baseFormat.getChannels() * 2,
					baseFormat.getSampleRate(),
					true);
			// @format:on
			
			int channelCount = baseFormat.getChannels();
			
			this.din = AudioSystem.getAudioInputStream(decodedFormat, in);
			
			// Read raw PCM data (byte array of shorts)
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int nBytesRead = 0;
			while((nBytesRead = this.din.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, nBytesRead);
			}
			buffer = null;
			
			byte[] byteData = baos.toByteArray();
			baos = null;
			
			// Convert the byte data to short data to floating point PCM
			this.audioData = new float[byteData.length / channelCount / 2];
			for(int i = 0, l = this.audioData.length; i < l; i++) {
				this.audioData[i] = Utils.clamp((short) (((byteData[i * channelCount * 2] & 0xFF) << 8) | (byteData[i * channelCount * 2 + 1] & 0xFF)) / (float) Short.MAX_VALUE, -1.0f, 1.0f);
			}
			
			byteData = null;
			
			this.in.close();
			this.din.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	
	public File getSourceFile() {
		return this.sourceFile;
	}
	
	public void play() {
		med.play();
	}
	
	public void stop() {
		med.stop();
	}
	
	public void dispose() {
		med.dispose();
		this.stopAnalysis();
	}
	
	public void pause() {
		med.pause();
	}
	
	public double getPosition() {
		return med.getCurrentTime().toSeconds();
	}
	
	public void setPosition(double seconds) {
		med.seek(Duration.seconds(seconds));
	}
	
	public double getVolume() {
		return med.getVolume();
	}
	
	public void setVolume(double volume) {
		med.setVolume(volume);
	}
	
	public MediaPlayer getMediaPlayer() {
		return med;
	}
	
	public void setMediaPlayer(MediaPlayer med) {
		this.med = med;
	}
	
	public double getDuration() {
		return this.duration;
	}
	
	private float[] timeDomData = new float[Urmusic.FFTSIZE];
	
	private float[] fftBuffer = new float[Urmusic.FFTSIZE];
	private float[] prevSmoothData = new float[Urmusic.FFTSIZE];
	
	private float[] lowFFTBuffer = new float[Urmusic.FFTSIZE];
	private float[] lowPrevSmoothData = new float[Urmusic.FFTSIZE];
	
	private float[] highFFTBuffer = new float[Urmusic.FFTSIZE];
	private float[] highPrevSmoothData = new float[Urmusic.FFTSIZE];
	
	public float maxval = -Float.MAX_VALUE;
	public float minval = -Float.MAX_VALUE;
	
	public float maxlowval = -Float.MAX_VALUE;
	public float minlowval = -Float.MAX_VALUE;
	
	public float maxhighval = -Float.MAX_VALUE;
	public float minhighval = -Float.MAX_VALUE;
	
	private synchronized void computeData(double seconds) {
		if(seconds == Sound.CURRENT_TIME) seconds = this.getPosition();
		
		// Position in the buffer
		int bufferPos = Math.min(this.audioData.length - Urmusic.FFTSIZE, (int) (seconds / this.getDuration() * this.audioData.length) - Urmusic.FFTSIZE);
		
		// Follow the Web Audio API spec
		float s;
		// Get the lasts fftSize samples (0 if k < 0)
		for(int i = 0, j = bufferPos, l = Urmusic.FFTSIZE; i < l; i++, j++) {
			s = (j < 0 ? 0 : this.audioData[j]);
			this.fftBuffer[i] = timeDomData[i] = s;
			if(lowpassEnabled) this.lowFFTBuffer[i] = (float) this.lowpassfilter.process(s);
			if(highpassEnabled) this.highFFTBuffer[i] = (float) this.highpassfilter.process(s);
		}
		
		Utils.applyBlackmanWindow(this.fftBuffer, this.fftBuffer.length);
		FFTEngine.ENGINE.fft(this.fftBuffer, this.fftBuffer);
		
		if(lowpassEnabled) {
			Utils.applyBlackmanWindow(this.lowFFTBuffer, this.lowFFTBuffer.length);
			FFTEngine.ENGINE.fft(this.lowFFTBuffer, this.lowFFTBuffer);
		}
		
		if(highpassEnabled) {
			Utils.applyBlackmanWindow(this.highFFTBuffer, this.highFFTBuffer.length);
			FFTEngine.ENGINE.fft(this.highFFTBuffer, this.highFFTBuffer);
		}
		
		// Needed to undo the FFT scale
		float magnitudeScalar = 1.0f / Urmusic.FFTSIZE;
		float lerped;
		
		maxval = -Float.MAX_VALUE;
		minval = -Float.MAX_VALUE;
		
		maxlowval = -Float.MAX_VALUE;
		minlowval = -Float.MAX_VALUE;
		
		maxhighval = -Float.MAX_VALUE;
		minhighval = -Float.MAX_VALUE;
		
		// Smooth over time and converts to DB
		for(int i = 0, l = Urmusic.FFTSIZE_HALF; i < l; i++) {
			// Smoothing (also, undo FFT scale)
			lerped = (float) Utils.lerp(Math.abs(this.fftBuffer[i] * magnitudeScalar), this.prevSmoothData[i], this.smoothingTimeConstant);
			this.prevSmoothData[i] = lerped;
			
			// Convert to DB
			this.fftBuffer[i] = lerped != 0 ? (float) (20 * Math.log10(lerped)) : -Float.MAX_VALUE;
			maxval = Math.max(maxval, this.fftBuffer[i]);
			minval = Math.min(minval, this.fftBuffer[i]);
			
			if(lowpassEnabled) {
				lerped = (float) Utils.lerp(Math.abs(this.lowFFTBuffer[i] * magnitudeScalar), this.lowPrevSmoothData[i], this.lowSmoothingTimeConstant);
				this.lowPrevSmoothData[i] = lerped;
				
				this.lowFFTBuffer[i] = lerped != 0 ? (float) (20 * Math.log10(lerped)) : -Float.MAX_VALUE;
				maxlowval = Math.max(maxlowval, this.lowFFTBuffer[i]);
				minlowval = Math.min(minlowval, this.lowFFTBuffer[i]);
			}
			
			if(highpassEnabled) {
				lerped = (float) Utils.lerp(Math.abs(this.highFFTBuffer[i] * magnitudeScalar), this.highPrevSmoothData[i], this.highSmoothingTimeConstant);
				this.highPrevSmoothData[i] = lerped;
				
				this.highFFTBuffer[i] = lerped != 0 ? (float) (20 * Math.log10(lerped)) : -Float.MAX_VALUE;
				maxhighval = Math.max(maxhighval, this.highFFTBuffer[i]);
				minhighval = Math.min(minhighval, this.highFFTBuffer[i]);
			}
		}
	}
	
	public synchronized void getAnalysedData(AnalyseData dest) {
		System.arraycopy(this.timeDomData, 0, dest.timeDomData, 0, Urmusic.FFTSIZE);
		System.arraycopy(this.fftBuffer, 0, dest.freqData, 0, Urmusic.FFTSIZE_HALF);
		
		dest.maxval = this.maxval;
		dest.minval = this.minval;
		
		dest.maxlowval = this.maxlowval;
		dest.minlowval = this.minlowval;
		
		dest.maxhighval = this.maxhighval;
		dest.minhighval = this.minhighval;
	}
	
	public void setLowpassFreq(double freq) {
		this.lowpassfilter.set(freq / nyquist, 0.0);
	}
	
	public void setHighpassFreq(double freq) {
		this.highpassfilter.set(freq / nyquist, 0.0);
	}
	
	public void startAnalysis(double framerate) {
		this.analyseTimer = new UrAnimationTimer("JFXSoundAnalyser", framerate) {
			protected void handle(long now) {
				JFXSound.this.computeData(JFXSound.this.getPosition());
			}
		};
		
		analyseTimer.start(true);
	}
	
	public void setSmoothingTimeConstant(double c) {
		this.smoothingTimeConstant = Utils.clamp(c, 0.0, 1.0);
	}
	
	public void setHighSmoothingTimeConstant(double c) {
		this.highSmoothingTimeConstant = Utils.clamp(c, 0.0, 1.0);
	}
	
	public void setLowSmoothingTimeConstant(double c) {
		this.lowSmoothingTimeConstant = Utils.clamp(c, 0.0, 1.0);
	}
	
	public void setHighpassEnabled(boolean enabled) {
		this.highpassEnabled = enabled;
	}
	
	public void setLowpassEnabled(boolean enabled) {
		this.lowpassEnabled = enabled;
	}
	
	public void stopAnalysis() {
		if(analyseTimer != null) analyseTimer.stop();
	}
	
	public void getAnalysedData(AnalyseData dest, double timeSeconds) {
		if(timeSeconds != Sound.CURRENT_TIME || !analyseTimer.isRunning()) this.computeData(timeSeconds);
		this.getAnalysedData(dest);
	}
}
