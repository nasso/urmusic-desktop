package io.github.nasso.urmusic.ui;

import java.util.function.Consumer;

import io.github.nasso.urmusic.ApplicationPreferences;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.audio.Sound;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;

public class ThePlayer extends Pane {
	public static final double PLAYER_HEIGHT = 54;
	public static final double VOLUME_CONTROL_WIDTH = 200;
	public static final double VOLUME_ICON_SIZE = 24;
	
	private Runnable onPlay;
	private Runnable onPause;
	private Consumer<Double> onTimeChange;
	private Consumer<Double> onVolumeChange;
	
	private boolean playing = false;
	
	private Shape playBtnShape;
	private Shape pauseBtnShape;
	
	private UrSliderBar timeSliderBar;
	private UrSliderBar volumeSliderBar;
	
	private Sound currentSound;
	
	private StringProperty prettytime = new StringPropertyBase() {
		public String getName() {
			return "prettytime";
		}
		
		public Object getBean() {
			return ThePlayer.this;
		}
	};
	
	private StringProperty prettyduration = new StringPropertyBase() {
		public String getName() {
			return "prettyduration";
		}
		
		public Object getBean() {
			return ThePlayer.this;
		}
	};
	
	public ThePlayer() {
		this.setBackground(new Background(new BackgroundFill(Color.web("#111"), CornerRadii.EMPTY, Insets.EMPTY)));
		this.setPrefHeight(PLAYER_HEIGHT);
		
		Pane playPauseBtnPane = new Pane();
		
		this.playBtnShape = new Polygon(0.0, 7.5, 15.0, 0.0, 0.0, -7.5);
		this.playBtnShape.setFill(Color.WHITE);
		
		this.pauseBtnShape = Shape.union(new Rectangle(0.0, -7.5, 6.5, 15.0), new Rectangle(8.0, -7.5, 6.5, 15.0));
		this.pauseBtnShape.setFill(Color.WHITE);
		this.pauseBtnShape.visibleProperty().bind(this.playBtnShape.visibleProperty().not());
		
		playPauseBtnPane.setLayoutX(10);
		playPauseBtnPane.layoutYProperty().bind(this.heightProperty().subtract(20));
		playPauseBtnPane.getChildren().addAll(this.playBtnShape, this.pauseBtnShape);
		playPauseBtnPane.setOnMouseClicked((e) -> this.setPlaying(!this.playing));
		
		this.timeSliderBar = new UrSliderBar();
		this.timeSliderBar.setLayoutX(40);
		this.timeSliderBar.layoutYProperty().bind(playPauseBtnPane.layoutYProperty());
		this.timeSliderBar.prefWidthProperty().bind(this.widthProperty().subtract(VOLUME_CONTROL_WIDTH + VOLUME_ICON_SIZE + 70));
		this.timeSliderBar.setOnPositionChangedByUser((pos) -> {
			if(this.currentSound != null) this.currentSound.setPosition((float) (pos * this.currentSound.getDuration()));
		});
		
		this.volumeSliderBar = new UrSliderBar();
		this.volumeSliderBar.layoutXProperty().bind(this.widthProperty().subtract(VOLUME_CONTROL_WIDTH + 10));
		this.volumeSliderBar.layoutYProperty().bind(this.timeSliderBar.layoutYProperty());
		this.volumeSliderBar.setPrefWidth(VOLUME_CONTROL_WIDTH);
		this.volumeSliderBar.setSliderPosition(ApplicationPreferences.audioVolume);
		this.volumeSliderBar.setOnPositionChangedByUser((pos) -> {
			if(this.currentSound != null) this.currentSound.setVolume(pos.floatValue());
			ApplicationPreferences.audioVolume = pos;
		});
		
		ImageView volumeImg = new ImageView(new Image("/res/img/volume.png"));
		volumeImg.layoutXProperty().bind(this.volumeSliderBar.layoutXProperty().subtract(VOLUME_ICON_SIZE + 10.0));
		volumeImg.layoutYProperty().bind(playPauseBtnPane.layoutYProperty().subtract(VOLUME_ICON_SIZE / 2));
		volumeImg.setFitWidth(VOLUME_ICON_SIZE);
		volumeImg.setFitHeight(VOLUME_ICON_SIZE);
		
		Pane timeStuff = new Pane();
		timeStuff.layoutXProperty().bind(this.widthProperty().divide(2));
		timeStuff.setLayoutY(2);
		
		Font laBelleFonte = Font.font(16.0);
		
		Label currentTimeLabel = new Label();
		currentTimeLabel.layoutXProperty().bind(currentTimeLabel.widthProperty().divide(-2).subtract(48));
		currentTimeLabel.setLayoutY(0);
		currentTimeLabel.textProperty().bind(this.prettytime);
		currentTimeLabel.setTextFill(Color.WHITE);
		currentTimeLabel.setFont(laBelleFonte);
		
		Label slashLabel = new Label("/");
		slashLabel.layoutXProperty().bind(slashLabel.widthProperty().divide(-2));
		slashLabel.setLayoutY(0);
		slashLabel.setTextFill(Color.WHITE);
		slashLabel.setFont(laBelleFonte);
		
		Label durationLabel = new Label();
		durationLabel.layoutXProperty().bind(durationLabel.widthProperty().divide(-2).add(48));
		durationLabel.setLayoutY(0);
		durationLabel.textProperty().bind(this.prettyduration);
		durationLabel.setTextFill(Color.WHITE);
		durationLabel.setFont(laBelleFonte);
		
		timeStuff.getChildren().addAll(currentTimeLabel, durationLabel, slashLabel);
		
		this.getChildren().addAll(playPauseBtnPane, this.timeSliderBar, volumeImg, this.volumeSliderBar, timeStuff);
		
		this.prettytime.set(Utils.prettyTime(0));
		this.prettyduration.set(Utils.prettyTime(0));
	}
	
	public void refresh() {
		if(this.currentSound != null) {
			double currentTime = this.currentSound.getPosition();
			double duration = this.currentSound.getDuration();
			double progPer = currentTime / duration;
			
			this.timeSliderBar.setSliderPosition(progPer);
			
			this.prettytime.set(Utils.prettyTime((float) currentTime));
			this.prettyduration.set(Utils.prettyTime((float) this.currentSound.getDuration()));
		} else {
			this.prettyduration.set(Utils.prettyTime(0));
		}
	}
	
	public String getPrettyTime() {
		return this.prettytime.get();
	}
	
	public String getPrettyDuration() {
		return this.prettyduration.get();
	}
	
	public Runnable getOnPlay() {
		return onPlay;
	}
	
	public void setOnPlay(Runnable onPlay) {
		this.onPlay = onPlay;
	}
	
	public Runnable getOnPause() {
		return onPause;
	}
	
	public void setOnPause(Runnable onPause) {
		this.onPause = onPause;
	}
	
	public Consumer<Double> getOnTimeChange() {
		return onTimeChange;
	}
	
	public void setOnTimeChange(Consumer<Double> onTimeChange) {
		this.onTimeChange = onTimeChange;
	}
	
	public Consumer<Double> getOnVolumeChange() {
		return onVolumeChange;
	}
	
	public void setOnVolumeChange(Consumer<Double> onVolumeChange) {
		this.onVolumeChange = onVolumeChange;
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void setPlaying(boolean playing) {
		if(this.currentSound == null) playing = false;
		if(this.playing == playing) return;
		
		this.playBtnShape.setVisible(!(this.playing = playing));
		
		if(this.playing) {
			if(this.currentSound != null) this.currentSound.play();
			if(this.onPlay != null) this.onPlay.run();
		} else {
			if(this.currentSound != null) this.currentSound.pause();
			if(this.onPause != null) this.onPause.run();
		}
	}
	
	public Sound getCurrentSound() {
		return currentSound;
	}
	
	public void setCurrentSound(Sound currentSound) {
		if(this.currentSound != null) this.currentSound.dispose();
		
		this.currentSound = currentSound;
		
		if(this.currentSound == null) {
			this.setPlaying(false);
			return;
		} else {
			this.currentSound.setVolume((float) this.volumeSliderBar.getSliderPosition());
			if(this.playing) this.currentSound.play();
		}
	}
}
