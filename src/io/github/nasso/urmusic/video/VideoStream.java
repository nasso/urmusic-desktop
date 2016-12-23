package io.github.nasso.urmusic.video;

import java.io.IOException;

import javafx.scene.image.WritableImage;

public interface VideoStream {
	public void writeImage(WritableImage img) throws IOException;
	
	public boolean done() throws IOException;
	
	public void cancel() throws IOException;
}
