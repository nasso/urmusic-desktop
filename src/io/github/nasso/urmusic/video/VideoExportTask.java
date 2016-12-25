package io.github.nasso.urmusic.video;

import io.github.nasso.urmusic.ApplicationPreferences;
import io.github.nasso.urmusic.audio.Sound;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class VideoExportTask extends Task<Boolean> {
	private VideoExportSettings vidSettings = null;
	private VideoStream vidStream = null;
	
	private double currentAnalysedPosition = Sound.CURRENT_TIME;
	private double currentRenderedPosition = 0;
	private double vidFrameLength = 0;
	private double audioAnalysisFrameLength = 1.0 / ApplicationPreferences.audioAnalysisFramerate;
	private Image snapImg;
	
	private boolean requestVideoStop = false;
	private boolean needANewImage = true;
	
	protected Boolean call() throws Exception {
		
		return true;
	}
	
	private void loop() {
		if(this.snapImg != null) {
			this.needANewImage = false;
			
			this.snapImg = null;
		}
	}
	
	public void setNextImage(Image img) {
		this.snapImg = img;
	}
	
	public boolean needsANewImage() {
		return needANewImage;
	}
}
