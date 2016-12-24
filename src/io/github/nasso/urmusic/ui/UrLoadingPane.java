package io.github.nasso.urmusic.ui;

import io.github.nasso.urmusic.Urmusic;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class UrLoadingPane extends Pane {
	public static final double LOAD_PANE_SIZE = 400;
	
	public static final int LOAD_DOTS_COUNT = 8;
	public static final int LOAD_DOTS_RADIUS = 24;
	public static final double LOAD_DOTS_DIST = 100;
	public static final double LOAD_DOTS_MIN_OPACITY = 0.1;
	public static final double LOAD_DOTS_MAX_OPACITY = 0.5;
	public static final double ANIMATION_SPEED = 1.0;
	
	private Circle[] loadDots = new Circle[LOAD_DOTS_COUNT];
	
	public UrLoadingPane() {
		this.setBackground(Urmusic.PANES_BACKGROUND);
		this.setEffect(Urmusic.PANES_EFFECT);
		
		this.setPrefSize(LOAD_PANE_SIZE, LOAD_PANE_SIZE);
		
		for(int i = 0; i < LOAD_DOTS_COUNT; i++) {
			double per = (double) i / LOAD_DOTS_COUNT;
			
			Circle c = new Circle();
			c.setRadius(LOAD_DOTS_RADIUS);
			c.setLayoutX(LOAD_PANE_SIZE / 2 + Math.cos(-per * Math.PI * 2) * LOAD_DOTS_DIST);
			c.setLayoutY(LOAD_PANE_SIZE / 2 + Math.sin(-per * Math.PI * 2) * LOAD_DOTS_DIST);
			c.setFill(Color.WHITE);
			
			FadeTransition transition = new FadeTransition(Duration.seconds(ANIMATION_SPEED), c);
			transition.setCycleCount(Animation.INDEFINITE);
			transition.setFromValue(LOAD_DOTS_MAX_OPACITY);
			transition.setToValue(LOAD_DOTS_MIN_OPACITY);
			
			transition.playFrom(Duration.seconds(ANIMATION_SPEED * per));
			
			this.loadDots[i] = c;
		}
		
		this.getChildren().addAll(loadDots);
		
		this.setVisible(false);
	}
	
	public void startLoading() {
		Urmusic.bringTheModality();
		this.setVisible(true);
	}
	
	public void stopLoading() {
		Urmusic.removeTheModality();
		this.setVisible(false);
	}
}
