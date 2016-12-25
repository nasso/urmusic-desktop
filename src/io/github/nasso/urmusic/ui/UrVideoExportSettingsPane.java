package io.github.nasso.urmusic.ui;

import java.io.File;
import java.text.NumberFormat;
import java.util.function.Consumer;

import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.audio.Sound;
import io.github.nasso.urmusic.video.VideoExportSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class UrVideoExportSettingsPane extends Pane {
	private Label titleLabel;
	
	private VBox vbox;
	private TextField outputFileField;
	private NumberTextField constantRateFactorField;
	private NumberTextField audioSampleRateField;
	private NumberTextField widthField;
	private NumberTextField heightField;
	private NumberTextField framerateField;
	private NumberTextField durationField;
	private CheckBox motionBlurField;
	
	private Button cancelButton, exportButton;
	
	private VideoExportSettings settings = new VideoExportSettings();
	
	private Consumer<VideoExportSettings> onSettingFinished;
	
	public UrVideoExportSettingsPane() {
		this.setBackground(Urmusic.PANES_BACKGROUND);
		this.setEffect(Urmusic.PANES_EFFECT);
		
		this.titleLabel = new Label("Export to video");
		this.titleLabel.setLayoutY(16);
		this.titleLabel.setTextFill(Color.WHITE);
		this.titleLabel.setFont(Font.font(24.0));
		this.titleLabel.layoutXProperty().bind(this.widthProperty().divide(2).subtract(this.titleLabel.widthProperty().divide(2)));
		
		ColumnConstraints col0Constr = new ColumnConstraints();
		col0Constr.setPercentWidth(50);
		ColumnConstraints col1Constr = new ColumnConstraints();
		col1Constr.setPercentWidth(50);
		
		this.vbox = new VBox();
		this.vbox.setAlignment(Pos.TOP_CENTER);
		this.vbox.setPadding(new Insets(8));
		this.vbox.setSpacing(4);
		this.vbox.setLayoutX(16);
		this.vbox.layoutYProperty().bind(this.titleLabel.heightProperty().add(16));
		this.vbox.prefWidthProperty().bind(this.widthProperty().subtract(32));
		this.vbox.prefHeightProperty().bind(this.heightProperty().subtract(this.vbox.layoutYProperty()).subtract(48));
		
		this.cancelButton = new Button("Cancel");
		this.cancelButton.layoutXProperty().bind(this.vbox.layoutXProperty());
		this.cancelButton.layoutYProperty().bind(this.heightProperty().subtract(10).subtract(this.cancelButton.heightProperty()));
		this.cancelButton.setOnAction((e) -> {
			Urmusic.removeTheModality();
			this.setVisible(false);
		});
		
		this.exportButton = new Button("Export");
		this.exportButton.layoutXProperty().bind(this.widthProperty().subtract(16).subtract(this.exportButton.widthProperty()));
		this.exportButton.layoutYProperty().bind(this.heightProperty().subtract(10).subtract(this.exportButton.heightProperty()));
		this.exportButton.setOnAction((e) -> {
			if(!checkFields()) return;
			
			Urmusic.removeTheModality();
			this.setVisible(false);
			
			if(this.onSettingFinished != null) this.onSettingFinished.accept(this.settings);
		});
		
		this.getChildren().addAll(this.titleLabel, this.vbox, this.cancelButton, this.exportButton);
		
		this.outputFileField = this.addFileField("Output file", this.settings.outputFile, new ExtensionFilter("MP4 video file", "*.mp4"), true, (f) -> {
			this.settings.outputFile = f;
			if(Urmusic.DEBUG) System.out.println("Output file: " + f);
		}, "The destination video file.");
		this.constantRateFactorField = this.addIntField("Constant rate factor", this.settings.constantRateFactor, 0, 51, (v) -> {
			this.settings.constantRateFactor = v;
			if(Urmusic.DEBUG) System.out.println("Constant rate factor: " + v);
		}, "The Constant Rate Factor (CRF) is the default quality setting for the x264 encoder. You can set the values between 0 and 51, where lower values would result in better quality (at the expense of higher file sizes).\nSane values are between 18 and 28. The default for x264 is 23, so you can use this as a starting point.");
		this.audioSampleRateField = this.addIntField("Audio sample rate", this.settings.audioSampleRate, 1, Integer.MAX_VALUE, (v) -> {
			this.settings.audioSampleRate = v;
			if(Urmusic.DEBUG) System.out.println("Audio sample rate: " + v);
		}, "The audio sample rate. Default to 48k");
		this.widthField = this.addIntField("Width", this.settings.width, 1, Integer.MAX_VALUE, (v) -> {
			this.settings.width = v;
			if(Urmusic.DEBUG) System.out.println("Width: " + v);
		}, "The video width.");
		this.heightField = this.addIntField("Height", this.settings.height, 1, Integer.MAX_VALUE, (v) -> {
			this.settings.height = v;
			if(Urmusic.DEBUG) System.out.println("Height: " + v);
		}, "The video height.");
		this.framerateField = this.addDoubleField("Framerate", this.settings.framerate, 1, 60, (v) -> {
			this.settings.framerate = v;
			if(Urmusic.DEBUG) System.out.println("Framerate: " + v);
		}, "The video framerate, in FPS. (values higher than 60 aren't supported yet)");
		this.durationField = this.addDoubleField("Duration (seconds)", this.settings.durationSec, 1, Double.MAX_VALUE, (v) -> {
			this.settings.durationSec = v;
			if(Urmusic.DEBUG) System.out.println("Duration: " + v);
		}, "The video duration, in seconds. The default value is the current song duration.");
		this.motionBlurField = this.addBooleanField("Motion blur", this.settings.motionBlur, (v) -> {
			this.settings.motionBlur = v;
			if(Urmusic.DEBUG) System.out.println("Motion blur: " + v);
		}, "If enabled, there'll be motion blur. It's looks cool.");
		this.motionBlurField.setDisable(true); // TODO: Enable motion blur when it'll be fixed
		
		this.setVisible(false);
	}
	
	private boolean checkFields() {
		this.settings.outputFile = outputFileField.getText();
		this.settings.constantRateFactor = Integer.parseInt(constantRateFactorField.getText());
		this.settings.audioSampleRate = Integer.parseInt(audioSampleRateField.getText());
		this.settings.width = Integer.parseInt(widthField.getText());
		this.settings.height = Integer.parseInt(heightField.getText());
		this.settings.framerate = Double.parseDouble(framerateField.getText());
		this.settings.durationSec = Double.parseDouble(durationField.getText());
		this.settings.motionBlur = motionBlurField.isSelected();
		
		if(Urmusic.DEBUG) {
			System.out.println("outputFile: " + this.settings.outputFile);
			System.out.println("constantRateFactor: " + this.settings.constantRateFactor);
			System.out.println("audioSampleRate: " + this.settings.audioSampleRate);
			System.out.println("width: " + this.settings.width);
			System.out.println("height: " + this.settings.height);
			System.out.println("framerate: " + this.settings.framerate);
			System.out.println("duration: " + this.settings.durationSec);
		}
		
		if(this.settings.outputFile.equals("")) {
			Urmusic.showError("The output file isn't valid.");
			return false;
		}
		
		return true;
	}
	
	private TextField addFileField(String labelName, String defaultValue, ExtensionFilter filter, boolean saveMode, Consumer<String> onAction, String tooltip) {
		Pane p = new Pane();
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().setAll(filter);
		fileChooser.setTitle(labelName);
		
		TextField field = new TextField(defaultValue);
		
		Button browseBtn = new Button("...");
		browseBtn.setOnAction((e) -> {
			File f;
			if(saveMode) f = fileChooser.showSaveDialog(Urmusic.getWindow());
			else f = fileChooser.showOpenDialog(Urmusic.getWindow());
			
			if(f != null) {
				field.setText(f.getAbsolutePath());
				onAction.accept(field.getText());
			}
		});
		
		field.setLayoutX(0);
		field.setLayoutY(0);
		field.prefWidthProperty().bind(p.widthProperty().subtract(browseBtn.widthProperty()).subtract(4));
		field.setOnAction((e) -> {
			onAction.accept(field.getText());
		});
		
		browseBtn.layoutXProperty().bind(p.widthProperty().subtract(browseBtn.widthProperty()));
		browseBtn.setLayoutY(0);
		browseBtn.prefHeightProperty().bind(field.heightProperty());
		
		p.getChildren().addAll(field, browseBtn);
		
		this.addField(labelName, tooltip, p);
		
		return field;
	}
	
	private NumberTextField addIntField(String labelName, int defaultValue, int min, int max, Consumer<Integer> onAction, String tooltip) {
		NumberTextField field = new NumberTextField(NumberFormat.getIntegerInstance(), min, max);
		
		field.setLayoutX(0);
		field.setLayoutY(0);
		field.setOnAction((e) -> {
			onAction.accept(Integer.valueOf(field.getText()));
		});
		field.setText(String.valueOf(defaultValue));
		
		this.addField(labelName, tooltip, field);
		
		return field;
	}
	
	private NumberTextField addDoubleField(String labelName, double defaultValue, double min, double max, Consumer<Double> onAction, String tooltip) {
		NumberTextField field = new NumberTextField(NumberFormat.getInstance(), min, max);
		
		field.setLayoutX(0);
		field.setLayoutY(0);
		field.setOnAction((e) -> {
			onAction.accept(Double.valueOf(field.getText()));
		});
		field.setText(String.valueOf(defaultValue));
		
		this.addField(labelName, tooltip, field);
		
		return field;
	}
	
	private CheckBox addBooleanField(String labelName, boolean defaultValue, Consumer<Boolean> onAction, String tooltip) {
		CheckBox checkbox = new CheckBox();
		checkbox.setSelected(defaultValue);
		checkbox.setOnAction((e) -> {
			onAction.accept(checkbox.isSelected());
		});
		
		this.addField(labelName, tooltip, checkbox);
		
		return checkbox;
	}
	
	private void addField(String labelName, String tooltip, Region n) {
		Pane p = new Pane();
		p.prefWidthProperty().bind(this.vbox.widthProperty());
		p.setPrefHeight(32);
		
		Label l = new Label(labelName + ":");
		l.setFont(Font.font(16));
		l.setTextFill(Color.WHITE);
		l.setAlignment(Pos.CENTER_LEFT);
		
		p.getChildren().addAll(l, n);
		
		if(tooltip != null) {
			Label tt = new Label(tooltip);
			tt.setTextFill(Color.WHITE);
			tt.setFont(Font.font(14));
			tt.setBackground(new Background(new BackgroundFill(Color.web("#222"), new CornerRadii(4), null)));
			tt.setEffect(new DropShadow(4, Color.BLACK));
			tt.setPadding(new Insets(8));
			tt.setMaxWidth(400);
			tt.setWrapText(true);
			tt.setTextAlignment(TextAlignment.JUSTIFY);
			tt.setMouseTransparent(true);
			
			l.setOnMouseEntered((e) -> {
				tt.setVisible(true);
			});
			l.setOnMouseExited((e) -> {
				tt.setVisible(false);
			});
			l.setOnMouseMoved((e) -> {
				tt.setLayoutX(e.getX());
				tt.setLayoutY(e.getY() - tt.getHeight());
			});
			
			tt.setVisible(false);
			
			p.getChildren().add(tt);
		}
		
		n.layoutXProperty().bind(p.widthProperty().subtract(n.widthProperty()));
		n.prefWidthProperty().bind(p.widthProperty().divide(2));
		
		this.vbox.getChildren().add(p);
	}
	
	public void beginSetting(Sound sound) {
		if(sound == null) {
			Urmusic.showError("You must open a sound in order to export a video.");
			return;
		}
		
		this.settings.durationSec = sound.getDuration();
		this.settings.inputSoundFile = sound.getSourceFile().getAbsolutePath();
		
		this.durationField.setText(String.valueOf(this.settings.durationSec));
		this.durationField.setMax(this.settings.durationSec);
		
		Urmusic.bringTheModality();
		this.setVisible(true);
	}
	
	public void setOnSettingFinished(Consumer<VideoExportSettings> c) {
		this.onSettingFinished = c;
	}
}
