package io.github.nasso.urmusic.video;

import java.io.IOException;

import javafx.scene.image.Image;

public class VideoExportTask {
	private VideoExportSettings vidSettings = null;
	private VideoStream vidStream = null;
	
	private Image snapImg;
	
	private boolean requestVideoStop = false;
	private boolean needANewImage = true;
	
	public VideoExportTask(VideoExportSettings s) throws Exception {
		this.vidSettings = s;
		
		this.vidStream = VideoEngine.ENGINE.createStream(this.vidSettings);
	}
	
	public VideoExportSettings vidSettings() {
		return vidSettings;
	}
	
	public void run() {
		while(!requestVideoStop) {
			loop();
		}
		
		try {
			this.vidStream.done();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void loop() {
		if(this.snapImg == null) {
			// Wait until snapImg is equal to something
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			// Maybe it has been canceled
			if(requestVideoStop) return;
		}
		
		this.needANewImage = false;
		
		try {
			this.vidStream.writeImage(this.snapImg);
			this.snapImg = null;
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.needANewImage = true;
	}
	
	public synchronized void requestStop() {
		requestVideoStop = true;
		notifyAll();
	}
	
	public synchronized void setNextImage(Image img) {
		if(this.needANewImage) {
			this.snapImg = img;
			this.needANewImage = false;
			notifyAll();
		}
	}
	
	public boolean needsANewImage() {
		return needANewImage;
	}
	
	public boolean hasFinished() {
		return this.requestVideoStop;
	}
}
