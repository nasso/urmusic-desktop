package io.github.nasso.urmusic;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.core.Settings;
import io.github.nasso.urmusic.json.JSONLoader;
import javafx.scene.canvas.Canvas;

public class Project {
	private String name = "Untitled";
	
	private Settings settings;
	private Canvas cvs = new Canvas();
	private FrameProperties frameProps = new FrameProperties();
	
	public Project() {
		this.settings = new Settings();
	}
	
	public Project(String name) {
		this.setName(name);
		this.settings = new Settings();
	}
	
	public Project(String name, String src) {
		this.setName(name);
		this.settings = JSONLoader.loadSettings(src);
	}
	
	public void dispose() {
		this.settings.dispose();
	}
	
	public FrameProperties getFrameProperties() {
		return frameProps;
	}
	
	public void setFrameProperties(FrameProperties frameProps) {
		this.frameProps = frameProps;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public Canvas getCanvas() {
		return cvs;
	}
	
	public void setCanvas(Canvas cvs) {
		this.cvs = cvs;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
