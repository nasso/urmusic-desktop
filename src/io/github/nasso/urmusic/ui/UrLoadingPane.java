package io.github.nasso.urmusic.ui;

import io.github.nasso.urmusic.TaskProperties;
import javafx.scene.layout.Pane;

public class UrLoadingPane extends Pane {
	private TaskProperties taskProps = new TaskProperties();
	
	public UrLoadingPane() {
		// TODO: implement UrLoadingPane
		
		this.setVisible(false);
	}
	
	public void startLoading() {
		this.setVisible(true);
	}
	
	public TaskProperties getBoundTaskProperties() {
		return this.taskProps;
	}
}
