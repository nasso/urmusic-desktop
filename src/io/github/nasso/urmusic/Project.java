package io.github.nasso.urmusic;

import java.io.File;

import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.core.Settings;

public class Project {
	private File projectFile;
	public String name = "Untitled";
	
	private Settings settings;
	private FrameProperties frameProps = new FrameProperties();
	
	private Runnable onnamechanged;
	
	public Project(int w, int h) {
		this("Untitled", w, h);
	}
	
	public Project(String name, int w, int h) {
		this(null, name, new Settings(w, h));
	}
	
	public Project(File file, String name, Settings s) {
		this.projectFile = file;
		this.name = name;
		this.settings = s;
	}
	
	public void dispose() {
		this.settings.dispose();
	}
	
	public FrameProperties getFrameProperties() {
		return this.frameProps;
	}
	
	public void setFrameProperties(FrameProperties frameProps) {
		this.frameProps = frameProps;
	}
	
	public Settings getSettings() {
		return this.settings;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public File getProjectFile() {
		return this.projectFile;
	}
	
	public void setProjectFile(File projectFile) {
		this.projectFile = projectFile;
	}

	public Runnable getOnNameChanged() {
		return onnamechanged;
	}

	public void setOnNameChanged(Runnable onnamechanged) {
		this.onnamechanged = onnamechanged;
	}
}
