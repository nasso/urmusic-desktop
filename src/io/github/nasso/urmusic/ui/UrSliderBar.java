package io.github.nasso.urmusic.ui;

import java.util.function.Consumer;

import io.github.nasso.urmusic.Utils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class UrSliderBar extends Pane {
	private Line progressPast;
	private Line progressFuture;
	private Circle progressDot;
	
	private DoubleProperty sliderPosition = new DoublePropertyBase() {
		public Object getBean() {
			return UrSliderBar.this;
		}
		
		public String getName() {
			return "sliderPosition";
		}
	};
	
	private Consumer<Double> onPositionChangedByUser;
	private boolean changingPos = false;
	
	public UrSliderBar() {
		
		this.setOnMousePressed((e) -> {
			this.changingPos = true;
			this.sliderPosition.set(Utils.clamp(e.getX() / this.getWidth(), 0.0, 1.0));
		});
		this.setOnMouseDragged(this.getOnMousePressed());
		this.setOnMouseReleased((e) -> {
			this.changingPos = false;
			double newVal = Utils.clamp(e.getX() / this.getWidth(), 0.0, 1.0);
			this.sliderPosition.set(newVal);
			if(this.onPositionChangedByUser != null) this.onPositionChangedByUser.accept(newVal);
		});
		
		this.progressPast = new Line();
		this.progressPast.setStartX(0);
		this.progressPast.setStartY(0);
		this.progressPast.endXProperty().bind(this.sliderPosition.multiply(this.widthProperty()));
		this.progressPast.setEndY(0);
		this.progressPast.setStroke(Color.WHITE);
		this.progressPast.setStrokeWidth(2);
		
		this.progressFuture = new Line();
		this.progressFuture.startXProperty().bind(this.progressPast.endXProperty());
		this.progressFuture.setStartY(0);
		this.progressFuture.endXProperty().bind(this.widthProperty());
		this.progressFuture.setEndY(0);
		this.progressFuture.setStroke(Color.web("#222"));
		this.progressFuture.setStrokeWidth(2);
		
		this.progressDot = new Circle(6, Color.WHITE);
		this.progressDot.centerXProperty().bind(this.progressPast.endXProperty());
		this.progressDot.setCenterY(0);
		
		this.getChildren().addAll(this.progressPast, this.progressFuture, this.progressDot);
	}
	
	public double getSliderPosition() {
		return this.sliderPosition.get();
	}
	
	public void setSliderPosition(double pos) {
		if(!this.changingPos) this.sliderPosition.set(pos);
	}
	
	public Consumer<Double> getOnPositionChangedByUser() {
		return onPositionChangedByUser;
	}
	
	public void setOnPositionChangedByUser(Consumer<Double> onPositionChangedByUser) {
		this.onPositionChangedByUser = onPositionChangedByUser;
	}
}
