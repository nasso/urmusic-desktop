package io.github.nasso.urmusic.video.ffmpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import io.github.nasso.urmusic.ApplicationPreferences;
import io.github.nasso.urmusic.video.VideoExportSettings;
import io.github.nasso.urmusic.video.VideoStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

public class FFmpegVideoStream implements VideoStream {
	private Process p;
	private OutputStream ffmpegInput;
	private BufferedImage awtImg;
	
	public FFmpegVideoStream(VideoExportSettings settings) throws IOException {
		this.awtImg = new BufferedImage(settings.width, settings.height, BufferedImage.TYPE_INT_RGB);
		
		File ffmpeg_output_log = new File("ffmpeg_output.log");
		
		// @format:off
		ProcessBuilder pb = new ProcessBuilder(
				ApplicationPreferences.ffmpegLocation, "-y",
				"-f", "image2pipe", "-vcodec", "png", "-r", String.valueOf(settings.framerate),
				"-i", "-",
				"-t", String.valueOf(settings.durationSec),
				"-i", settings.inputSoundFile,
				"-c:v", "h264", "-crf", String.valueOf(settings.constantRateFactor), "-r", String.valueOf(settings.framerate),
				"-c:a", "aac", "-ar", String.valueOf(settings.audioSampleRate),
				settings.outputFile);
		// @format:on
		
		pb.redirectErrorStream(true);
		pb.redirectOutput(ffmpeg_output_log);
		pb.redirectInput(ProcessBuilder.Redirect.PIPE);
		
		this.p = pb.start();
		this.ffmpegInput = p.getOutputStream();
	}
	
	public void writeImage(WritableImage img) throws IOException {
		awtImg = SwingFXUtils.fromFXImage(img, awtImg);
		ImageIO.write(awtImg, "png", this.ffmpegInput);
	}
	
	public boolean done() throws IOException {
		this.ffmpegInput.flush();
		this.ffmpegInput.close();
		
		try {
			this.p.waitFor();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void cancel() throws IOException {
		this.ffmpegInput.close();
		
		try {
			this.p.waitFor();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
