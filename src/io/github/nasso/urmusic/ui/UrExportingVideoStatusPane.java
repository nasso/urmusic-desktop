package io.github.nasso.urmusic.ui;

import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import io.github.nasso.urmusic.video.VideoExportSettings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.FloatPropertyBase;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class UrExportingVideoStatusPane extends Pane {
	private FloatProperty progress = new FloatPropertyBase() {
		public Object getBean() {
			return UrExportingVideoStatusPane.this;
		}
		
		public String getName() {
			return "progress";
		}
	};
	
	private Label exportProgressLabel;
	private Label currentTimeLabel;
	private Label durationLabel;
	
	private Button cancelButton;
	
	private ImageView preview;
	
	private Pane progressBarPane;
	private Rectangle doneProgressBar;
	
	private VideoExportSettings exportSettings;
	
	private Runnable onCancel;
	
	public UrExportingVideoStatusPane() {
		this.setBackground(Urmusic.PANES_BACKGROUND);
		this.setEffect(Urmusic.PANES_EFFECT);
		
		this.exportProgressLabel = new Label("Exporting nothing.");
		this.exportProgressLabel.setLayoutY(16);
		this.exportProgressLabel.setTextFill(Color.WHITE);
		this.exportProgressLabel.setFont(Font.font(24.0));
		this.exportProgressLabel.layoutXProperty().bind(this.widthProperty().divide(2).subtract(this.exportProgressLabel.widthProperty().divide(2)));
		
		this.cancelButton = new Button("Cancel");
		this.cancelButton.setLayoutX(32);
		this.cancelButton.layoutYProperty().bind(this.heightProperty().subtract(20).subtract(this.cancelButton.heightProperty()));
		this.cancelButton.setOnAction((e) -> {
			if(this.onCancel != null) this.onCancel.run();
		});
		
		this.progressBarPane = new Pane();
		this.progressBarPane.layoutXProperty().bind(this.cancelButton.layoutXProperty().add(this.cancelButton.widthProperty()).add(8));
		this.progressBarPane.layoutYProperty().bind(this.cancelButton.layoutYProperty().add(2));
		this.progressBarPane.prefWidthProperty().bind(this.widthProperty().subtract(this.progressBarPane.layoutXProperty()).subtract(32));
		this.progressBarPane.prefHeightProperty().bind(this.cancelButton.heightProperty().subtract(4));
		this.progressBarPane.setBackground(new Background(new BackgroundFill(Color.web("#222"), new CornerRadii(2), null)));
		
		this.doneProgressBar = new Rectangle();
		this.doneProgressBar.setFill(Color.DODGERBLUE);
		this.doneProgressBar.setArcWidth(4);
		this.doneProgressBar.setArcHeight(4);
		this.doneProgressBar.setX(2);
		this.doneProgressBar.setY(2);
		this.doneProgressBar.widthProperty().bind(this.progress.multiply(this.progressBarPane.widthProperty()).subtract(4));
		this.doneProgressBar.heightProperty().bind(this.progressBarPane.heightProperty().subtract(4));
		
		this.progressBarPane.getChildren().add(this.doneProgressBar);
		
		this.currentTimeLabel = new Label(Utils.prettyTime(0, true));
		this.currentTimeLabel.setTextFill(Color.WHITE);
		this.currentTimeLabel.setFont(Font.font(11.0));
		this.currentTimeLabel.layoutXProperty().bind(this.progressBarPane.layoutXProperty());
		this.currentTimeLabel.layoutYProperty().bind(this.progressBarPane.layoutYProperty().subtract(2).subtract(this.currentTimeLabel.heightProperty()));
		
		this.durationLabel = new Label(Utils.prettyTime(0, true));
		this.durationLabel.setTextFill(Color.WHITE);
		this.durationLabel.setFont(Font.font(11.0));
		this.durationLabel.layoutXProperty().bind(this.widthProperty().subtract(32).subtract(this.durationLabel.widthProperty()));
		this.durationLabel.layoutYProperty().bind(this.progressBarPane.layoutYProperty().subtract(2).subtract(this.currentTimeLabel.heightProperty()));
		
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.TOP_CENTER);
		hbox.setLayoutX(32);
		hbox.layoutYProperty().bind(this.exportProgressLabel.heightProperty().add(32));
		hbox.prefWidthProperty().bind(this.widthProperty().subtract(64));
		
		this.preview = new ImageView();
		this.preview.fitWidthProperty().bind(hbox.widthProperty());
		this.preview.fitHeightProperty().bind(this.heightProperty().subtract(this.heightProperty().subtract(this.progressBarPane.layoutYProperty()).add(hbox.layoutYProperty().add(32))));
		this.preview.setPreserveRatio(true);
		
		hbox.getChildren().add(this.preview);
		
		this.getChildren().addAll(this.exportProgressLabel, hbox, this.progressBarPane, this.currentTimeLabel, this.durationLabel, this.cancelButton);
		
		this.setVisible(false);
	}
	
	public void setOnCancel(Runnable r) {
		Urmusic.removeTheModality();
		this.setVisible(false);
		this.onCancel = r;
	}
	
	public void beginExport(VideoExportSettings settings) {
		this.exportSettings = settings;
		
		this.durationLabel.setText(Utils.prettyTime(this.exportSettings.durationSec, true));
		this.update(null, 0.0f);
		
		Urmusic.bringTheModality();
		this.setVisible(true);
	}
	
	public void finishExport() {
		Urmusic.removeTheModality();
		this.setVisible(false);
	}
	
	public void update(WritableImage img, float per) {
		this.exportProgressLabel.setText("Exporting video... " + (Math.floor(per * 100000.0) / 1000.0) + "%");
		this.currentTimeLabel.setText(Utils.prettyTime(per * this.exportSettings.durationSec, true));
		this.progress.set(per);
		
		if(img != null) {
			this.preview.setImage(img);
		}
	}
}
