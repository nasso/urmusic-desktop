package io.github.nasso.urmusic;

import java.io.File;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.core.Settings;
import javafx.scene.canvas.Canvas;

public class Project {
	private File projectFile;
	private String name = "Untitled";
	
	private Settings settings;
	private Canvas cvs = new Canvas();
	private FrameProperties frameProps = new FrameProperties();
	
	public Project() {
		this("Untitled");
	}
	
	public Project(String name) {
		this(name, new Settings());
	}
	
	public Project(String name, Settings s) {
		this.name = name;
		this.settings = s;
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
	
	public File getProjectFile() {
		return projectFile;
	}
	
	public void setProjectFile(File projectFile) {
		this.projectFile = projectFile;
	}
}
