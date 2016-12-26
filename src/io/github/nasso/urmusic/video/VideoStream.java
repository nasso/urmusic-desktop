package io.github.nasso.urmusic.video;

import java.io.IOException;

import javafx.scene.image.Image;

public interface VideoStream {
	public void writeImage(Image img) throws IOException;
	
	public boolean done() throws IOException;
}
