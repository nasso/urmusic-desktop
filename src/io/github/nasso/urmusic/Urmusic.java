package io.github.nasso.urmusic;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.github.nasso.urmusic.audio.AnalyseData;
import io.github.nasso.urmusic.audio.AudioEngine;
import io.github.nasso.urmusic.audio.Sound;
import io.github.nasso.urmusic.core.FrameProperties;
import io.github.nasso.urmusic.core.Renderer;
import io.github.nasso.urmusic.expression.ExpressionEngine;
import io.github.nasso.urmusic.log.LoggingOutputStream;
import io.github.nasso.urmusic.log.StdOutErrLevel;
import io.github.nasso.urmusic.ui.ThePlayer;
import io.github.nasso.urmusic.ui.UrExportingVideoStatusPane;
import io.github.nasso.urmusic.ui.UrLoadingPane;
import io.github.nasso.urmusic.ui.UrVideoExportSettingsPane;
import io.github.nasso.urmusic.video.VideoEngine;
import io.github.nasso.urmusic.video.VideoExportSettings;
import io.github.nasso.urmusic.video.VideoStream;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Urmusic extends Application {
	public static final Background PANES_BACKGROUND = new Background(new BackgroundFill(Color.web("#111"), new CornerRadii(4), null));
	public static final Effect PANES_EFFECT = new DropShadow(16, Color.BLACK);
	
	public static final boolean DEBUG = true;
	public static final int FFTSIZE = 2048;
	public static final int FFTSIZE_HALF = FFTSIZE / 2;
	
	public static final double EXPORT_VID_PANES_OFFSET_TOP = 32;
	public static final double EXPORT_VID_PANES_OFFSET_BOTTOM = 32;
	
	public static final File APPLICATION_PREFERENCES_FILE = new File("urmusic.pref");
	
	@SuppressWarnings("resource")
	public static void main(String[] argv) {
		if(!DEBUG) {
			try {
				// initialize logging to go to rolling log file
				LogManager logManager = LogManager.getLogManager();
				logManager.reset();
				
				// log file max size 10K, 3 rolling files, append-on-open
				Handler fileHandler = new FileHandler("urmusic.err.log", 10000, 1, true);
				fileHandler.setFormatter(new Formatter() {
					public String format(LogRecord record) {
						return record.getMessage() + System.lineSeparator();
					}
				});
				Logger.getLogger("").addHandler(fileHandler);
				
				System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger("stderr"), StdOutErrLevel.STDERR), true));
				System.err.println("################################################################");
				System.err.println("### " + Calendar.getInstance().getTime().toString());
				System.err.println("################################################################");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			ApplicationPreferences.load(APPLICATION_PREFERENCES_FILE);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Application.launch(argv);
	}
	
	private static List<Project> openedProjects = new ArrayList<Project>();
	
	private static FadeTransition modalityTransitionOn;
	private static FadeTransition modalityTransitionOff;
	private static Pane modalityPane;
	
	private static UrVideoExportSettingsPane videoExportSettingsPane;
	private static UrExportingVideoStatusPane exportingVidPane;
	private static UrLoadingPane loadPane;
	
	private static ThePlayer thePlayer;
	
	private static MenuBar menuBar;
	private static CheckMenuItem motionBlurCheckboxItem;
	private static TabPane tabPane;
	
	private static Renderer renderer;
	
	private static Stage stg;
	private static FileChooser fileChooser = new FileChooser();
	private static FileChooser audioFileChooser = new FileChooser();
	private static Alert aboutDialog;
	private static Alert errorAlert;
	
	private static AnalyseData analyseData;
	
	// Vid stuff
	// private static Task<Boolean> vidExportTask;
	
	private static VideoExportSettings vidSettings = null;
	private static VideoStream vidStream = null;
	
	private static double currentAnalysedPosition = Sound.CURRENT_TIME;
	private static double currentRenderedPosition = 0;
	private static double vidFrameLength = 0;
	private static double audioAnalysisFrameLength = 1.0 / ApplicationPreferences.audioAnalysisFramerate;
	private static WritableImage snapImg;
	private static SnapshotParameters snapParams;
	private static Canvas renderingCanvas;
	
	private static boolean requestVideoStop = false;
	
	// The app instance
	private static Urmusic appInstance;
	
	public void start(Stage stg) throws Exception {
		appInstance = this;
		
		startApp(stg);
	}
	
	public static Stage getWindow() {
		return Urmusic.stg;
	}
	
	public static void startApp(Stage stg) throws Exception {
		Urmusic.analyseData = new AnalyseData();
		
		Urmusic.stg = stg;
		Urmusic.fileChooser.getExtensionFilters().add(new ExtensionFilter("Urmusic projects", "*.urm"));
		Urmusic.audioFileChooser.getExtensionFilters().add(new ExtensionFilter("Audio files", "*.wav", "*.mp3"));
		
		Urmusic.modalityPane = new Pane();
		Urmusic.tabPane = new TabPane();
		Urmusic.menuBar = new MenuBar();
		Urmusic.thePlayer = new ThePlayer();
		Urmusic.aboutDialog = new Alert(AlertType.INFORMATION);
		
		Urmusic.modalityPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
		Urmusic.modalityPane.setOpacity(0.0);
		Urmusic.modalityPane.setMouseTransparent(true);
		Urmusic.modalityPane.setLayoutX(0);
		Urmusic.modalityPane.setLayoutY(0);
		Urmusic.modalityPane.prefWidthProperty().bind(Urmusic.stg.widthProperty());
		Urmusic.modalityPane.prefHeightProperty().bind(Urmusic.stg.heightProperty());
		
		Urmusic.modalityTransitionOn = new FadeTransition(Duration.seconds(0.2), Urmusic.modalityPane);
		Urmusic.modalityTransitionOn.setToValue(0.2);
		
		Urmusic.modalityTransitionOff = new FadeTransition(Duration.seconds(0.2), Urmusic.modalityPane);
		Urmusic.modalityTransitionOff.setToValue(0.0);
		
		Group group = new Group();
		Scene sce = new Scene(group);
		sce.getStylesheets().add("/res/style.min.css");
		sce.setOnDragOver((e) -> {
			Dragboard db = e.getDragboard();
			
			if(!db.hasFiles()) {
				e.consume();
				return;
			}
			
			boolean valid = true;
			
			for(File f : db.getFiles()) {
				valid &= (f.getName().endsWith(".urm") || f.getName().endsWith(".mp3") || f.getName().endsWith(".wav"));
			}
			
			if(valid) {
				e.acceptTransferModes(TransferMode.COPY);
			} else {
				e.consume();
			}
		});
		sce.setOnDragDropped((e) -> {
			Dragboard db = e.getDragboard();
			
			boolean success = false;
			if(db.hasFiles()) {
				File audioFile = null;
				
				for(File f : db.getFiles()) {
					if(f.getName().endsWith(".urm")) {
						try {
							Urmusic.loadProject(f);
						} catch(IOException ex) {
							ex.printStackTrace();
						}
					} else {
						audioFile = f;
					}
				}
				
				if(audioFile != null) {
					Urmusic.loadSound(audioFile);
				}
				
				success = true;
			}
			
			e.setDropCompleted(success);
			e.consume();
		});
		
		Menu fileMenu = new Menu("File");
		MenuItem newProjectItem = new MenuItem("New");
		MenuItem openProjectItem = new MenuItem("Open");
		MenuItem saveProjectItem = new MenuItem("Save");
		MenuItem saveAsProjectItem = new MenuItem("Save As...");
		MenuItem openSoundItem = new MenuItem("Open sound");
		MenuItem exportVideoItem = new MenuItem("Export to video...");
		
		newProjectItem.setOnAction((e) -> Urmusic.newProject());
		openProjectItem.setOnAction((e) -> Urmusic.openProject());
		saveProjectItem.setOnAction((e) -> Urmusic.saveProject());
		saveAsProjectItem.setOnAction((e) -> Urmusic.saveProjectAs());
		openSoundItem.setOnAction((e) -> Urmusic.openSound());
		exportVideoItem.setOnAction((e) -> Urmusic.exportVideo());
		
		fileMenu.getItems().addAll(newProjectItem, openProjectItem, saveProjectItem, saveAsProjectItem, new SeparatorMenuItem());
		fileMenu.getItems().addAll(openSoundItem, new SeparatorMenuItem());
		fileMenu.getItems().addAll(exportVideoItem);
		
		Menu viewMenu = new Menu("View");
		Urmusic.motionBlurCheckboxItem = new CheckMenuItem("Motion blur");
		Urmusic.motionBlurCheckboxItem.setDisable(true); // TODO: Motion blur crashes the thing
		
		Urmusic.motionBlurCheckboxItem.setOnAction((e) -> {
			Urmusic.renderer.setMotionBlur(Urmusic.motionBlurCheckboxItem.isSelected());
		});
		
		viewMenu.getItems().addAll(Urmusic.motionBlurCheckboxItem);
		
		Menu helpMenu = new Menu("Help");
		MenuItem wikiItem = new MenuItem("Wiki");
		MenuItem aboutItem = new MenuItem("About Urmusic");
		
		wikiItem.setOnAction((e) -> Urmusic.openWiki());
		aboutItem.setOnAction((e) -> Urmusic.openAbout());
		
		helpMenu.getItems().addAll(wikiItem, new SeparatorMenuItem(), aboutItem);
		
		menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
		
		Urmusic.thePlayer.layoutYProperty().bind(sce.heightProperty().subtract(ThePlayer.PLAYER_HEIGHT));
		Urmusic.thePlayer.prefWidthProperty().bind(sce.widthProperty());
		
		Urmusic.menuBar.prefWidthProperty().bind(sce.widthProperty());
		Urmusic.menuBar.setPrefHeight(30);
		
		Urmusic.tabPane.setLayoutY(30);
		Urmusic.tabPane.prefWidthProperty().bind(sce.widthProperty());
		Urmusic.tabPane.prefHeightProperty().bind(sce.heightProperty().subtract(30));
		
		Urmusic.tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		
		Tab addTabTab = new Tab("+");
		addTabTab.setClosable(false);
		Urmusic.tabPane.getTabs().add(addTabTab);
		
		// TODO: choose a default preset (or add an ApplicationPreference)
		loadProject(new File("presets/drop-the-bassline.urm"));
		addTabTab.setOnSelectionChanged((e) -> Urmusic.newProject());
		
		Urmusic.videoExportSettingsPane = new UrVideoExportSettingsPane();
		Urmusic.videoExportSettingsPane.layoutXProperty().bind(sce.widthProperty().divide(2).subtract(Urmusic.videoExportSettingsPane.widthProperty().divide(2)));
		Urmusic.videoExportSettingsPane.layoutYProperty().bind(Urmusic.menuBar.heightProperty().add(Urmusic.tabPane.tabMaxHeightProperty()).add(9 + EXPORT_VID_PANES_OFFSET_TOP));
		Urmusic.videoExportSettingsPane.prefWidthProperty().bind(sce.widthProperty().multiply(0.5));
		Urmusic.videoExportSettingsPane.prefHeightProperty().bind(sce.heightProperty().subtract(Urmusic.videoExportSettingsPane.layoutYProperty()).subtract(ThePlayer.PLAYER_HEIGHT + EXPORT_VID_PANES_OFFSET_BOTTOM));
		Urmusic.videoExportSettingsPane.setOnSettingFinished((s) -> {
			Urmusic.startVideoExport(s);
		});
		
		Urmusic.loadPane = new UrLoadingPane();
		Urmusic.loadPane.layoutXProperty().bind(sce.widthProperty().divide(2).subtract(Urmusic.loadPane.widthProperty().divide(2)));
		Urmusic.loadPane.layoutYProperty().bind(sce.heightProperty().divide(2).subtract(Urmusic.loadPane.heightProperty().divide(2)));
		
		Urmusic.exportingVidPane = new UrExportingVideoStatusPane();
		Urmusic.exportingVidPane.layoutXProperty().bind(sce.widthProperty().divide(2).subtract(Urmusic.exportingVidPane.widthProperty().divide(2)));
		Urmusic.exportingVidPane.layoutYProperty().bind(Urmusic.menuBar.heightProperty().add(Urmusic.tabPane.tabMaxHeightProperty()).add(9 + EXPORT_VID_PANES_OFFSET_TOP));
		Urmusic.exportingVidPane.prefWidthProperty().bind(sce.widthProperty().multiply(0.7));
		Urmusic.exportingVidPane.prefHeightProperty().bind(sce.heightProperty().subtract(Urmusic.exportingVidPane.layoutYProperty()).subtract(ThePlayer.PLAYER_HEIGHT + EXPORT_VID_PANES_OFFSET_BOTTOM));
		Urmusic.exportingVidPane.setOnCancel(() -> {
			Urmusic.requestVideoStop = true;
		});
		
		// @format:off
		group.getChildren().addAll(
				Urmusic.menuBar,
				Urmusic.tabPane,
				Urmusic.thePlayer,
				Urmusic.modalityPane,
				Urmusic.loadPane,
				Urmusic.videoExportSettingsPane,
				Urmusic.exportingVidPane);
		// @format:on
		Urmusic.menuBar.toFront();
		
		stg.setScene(sce);
		stg.setMinWidth(800);
		stg.setMinHeight(600);
		stg.setWidth(800);
		stg.setHeight(600);
		stg.setMaximized(true);
		stg.setTitle("Urmusic");
		
		Urmusic.aboutDialog.setTitle("About Urmusic");
		Urmusic.aboutDialog.setHeaderText(null);
		Urmusic.aboutDialog.setContentText(Utils.readFile("res/about.txt", true));
		Urmusic.aboutDialog.initOwner(Urmusic.stg);
		Urmusic.aboutDialog.initModality(Modality.WINDOW_MODAL);
		
		Urmusic.errorAlert = new Alert(Alert.AlertType.ERROR);
		Urmusic.errorAlert.setTitle("Error");
		Urmusic.errorAlert.setHeaderText(null);
		Urmusic.errorAlert.initOwner(Urmusic.stg);
		Urmusic.errorAlert.initModality(Modality.WINDOW_MODAL);
		
		stg.setOnCloseRequest((e) -> {
			Urmusic.thePlayer.setCurrentSound(null);
			
			for(Project p : Urmusic.openedProjects) {
				p.dispose();
			}
			
			if(Urmusic.vidStream != null) {
				try {
					Urmusic.vidStream.done();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
			
			try {
				ApplicationPreferences.save(APPLICATION_PREFERENCES_FILE);
			} catch(IOException e1) {
				e1.printStackTrace();
			}
		});
		
		stg.show();
		
		Urmusic.renderer = new Renderer();
		AnimationTimer timer = new AnimationTimer() {
			private long lastFPSUpdate = 0;
			private long frameCounter = 0;
			private long fps = 0;
			
			private long lastFrameTime = 0;
			
			public void handle(long now) {
				if(now - lastFPSUpdate > 1_000_000_000) {
					fps = frameCounter;
					frameCounter = 0;
					lastFPSUpdate = now;
					
					if(Urmusic.DEBUG) System.out.println("FPS: " + fps + " | Frame time: " + ((now - lastFrameTime) / 1_000_000.0) + "ms");
				}
				
				Urmusic.loop(now);
				
				frameCounter++;
				lastFrameTime = now;
			}
		};
		
		timer.start();
	}
	
	private static void recordVideoFrame(Project proj, float per) {
		Urmusic.renderer.render(proj, renderingCanvas);
		snapImg = renderingCanvas.snapshot(snapParams, snapImg);
		try {
			Urmusic.vidStream.writeImage(snapImg);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Urmusic.exportingVidPane.update(snapImg, per);
	}
	
	private static void analysePulse(Project proj) {
		FrameProperties props = proj.getFrameProperties();
		
		if(Urmusic.thePlayer.getCurrentSound() != null) {
			props.time = (float) Urmusic.thePlayer.getCurrentSound().getPosition();
			props.duration = (float) Urmusic.thePlayer.getCurrentSound().getDuration();
			
			Urmusic.thePlayer.getCurrentSound().setSmoothingTimeConstant(proj.getSettings().smoothingTimeConstant.getValueAsFloat());
			Urmusic.thePlayer.getCurrentSound().setLowSmoothingTimeConstant(proj.getSettings().advanced.lowpassSmooth);
			Urmusic.thePlayer.getCurrentSound().setHighSmoothingTimeConstant(proj.getSettings().advanced.highpassSmooth);
			Urmusic.thePlayer.getCurrentSound().setLowpassEnabled(proj.getSettings().advanced.enableLowpass);
			Urmusic.thePlayer.getCurrentSound().setHighpassEnabled(proj.getSettings().advanced.enableHighpass);
			Urmusic.thePlayer.getCurrentSound().setLowpassFreq(proj.getSettings().advanced.lowpassFreq);
			Urmusic.thePlayer.getCurrentSound().setHighpassFreq(proj.getSettings().advanced.highpassFreq);
			
			Urmusic.thePlayer.getCurrentSound().getAnalysedData(Urmusic.analyseData, currentAnalysedPosition);
			
			props.maxval = Urmusic.analyseData.maxval;
			props.minval = Urmusic.analyseData.minval;
			
			props.maxlowval = Urmusic.analyseData.maxlowval;
			props.minlowval = Urmusic.analyseData.minlowval;
			
			props.maxhighval = Urmusic.analyseData.maxhighval;
			props.minhighval = Urmusic.analyseData.minhighval;
		}
		
		ExpressionEngine.ENGINE.update(props);
	}
	
	private static void loop(long now) {
		int projI = Urmusic.tabPane.getSelectionModel().getSelectedIndex();
		if(projI >= Urmusic.openedProjects.size()) return;
		
		Project proj = Urmusic.openedProjects.get(projI);
		
		Urmusic.thePlayer.refresh();
		analysePulse(proj);
		
		// If recording...
		if(Urmusic.vidStream != null) {
			if(audioAnalysisFrameLength > vidFrameLength) {
				// FIXME
				// Here: time to next audio analysis frame > time to next video frame
				// Higher framerate: VIDEO
				//
				// Then, increment by the smallest time: VIDEO FRAME LENGTH
				//
				currentRenderedPosition += vidFrameLength;
				
				if(Math.floor(currentRenderedPosition / audioAnalysisFrameLength) >= Math.floor(currentAnalysedPosition / audioAnalysisFrameLength)) {
					recordVideoFrame(proj, (float) (currentRenderedPosition / Urmusic.vidSettings.durationSec));
					currentAnalysedPosition += audioAnalysisFrameLength;
				}
				
				requestVideoStop |= currentRenderedPosition >= Urmusic.vidSettings.durationSec;
			} else if(vidFrameLength > audioAnalysisFrameLength) {
				//
				// Here: time to next video frame > time to next audio analysis frame
				// Higher framerate: AUDIO
				//
				// Then, increment by the smallest time: AUDIO FRAME LENGTH
				//
				currentAnalysedPosition += audioAnalysisFrameLength;
				
				//
				// > time / unit
				// gives how many "unit"s there's in "time"
				//
				// > floor(time / unit)
				// gives how many full "unit"s there's in "time"
				// 
				// Here: convert the current analysed position and the current rendered position in full video frames, and check if the analyse is on the next frame
				// 
				if(Math.floor(currentAnalysedPosition / vidFrameLength) >= Math.floor(currentRenderedPosition / vidFrameLength)) {
					recordVideoFrame(proj, (float) (currentAnalysedPosition / Urmusic.vidSettings.durationSec));
					currentRenderedPosition += vidFrameLength;
				}
				
				requestVideoStop |= currentAnalysedPosition >= Urmusic.vidSettings.durationSec;
			} else {
				//
				// Here: video and audio sync
				// Higher framerate: BOTH
				//
				// Then, increment by the smallest time: BOTH
				//
				currentRenderedPosition += vidFrameLength;
				currentAnalysedPosition += audioAnalysisFrameLength;
				
				recordVideoFrame(proj, (float) (currentRenderedPosition / Urmusic.vidSettings.durationSec));
				
				// Should be the same, but still.
				requestVideoStop |= currentRenderedPosition >= Urmusic.vidSettings.durationSec || currentAnalysedPosition >= Urmusic.vidSettings.durationSec;
			}
			
			if(requestVideoStop) {
				try {
					Urmusic.vidStream.done();
				} catch(IOException e) {
					e.printStackTrace();
				}
				
				Urmusic.vidStream = null;
				Urmusic.renderer.setMotionBlur(Urmusic.motionBlurCheckboxItem.isSelected());
				Urmusic.currentAnalysedPosition = Sound.CURRENT_TIME;
				Urmusic.currentRenderedPosition = Sound.CURRENT_TIME;
				Urmusic.requestVideoStop = false;
				Urmusic.exportingVidPane.finishExport();
			}
		} else {
			// if not recording, render as usual
			Urmusic.renderer.render(proj);
		}
	}
	
	public static float[] getFrequencyData() {
		return Urmusic.analyseData.freqData;
	}
	
	public static float[] getTimeDomData() {
		return Urmusic.analyseData.timeDomData;
	}
	
	private static void createProjectTab(Project p) {
		if(p == null) return;
		
		final int i = Urmusic.openedProjects.size();
		
		Urmusic.openedProjects.add(p);
		
		p.getCanvas().widthProperty().bind(Urmusic.tabPane.widthProperty());
		p.getCanvas().heightProperty().bind(Urmusic.tabPane.heightProperty().subtract(Urmusic.tabPane.tabMaxHeightProperty()));
		
		Tab tab = new Tab(p.getName());
		tab.setContent(p.getCanvas());
		tab.setOnCloseRequest((e) -> {
			p.dispose();
			Urmusic.openedProjects.remove(p);
		});
		
		Urmusic.tabPane.getTabs().add(i, tab);
		Urmusic.tabPane.getSelectionModel().select(i);
	}
	
	private static Project loadProject(File f) throws IOException {
		if(!f.isFile()) return null;
		
		String fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
		
		Project p = new Project(fileName, Utils.readFile(f.getAbsolutePath(), false));
		createProjectTab(p);
		
		return p;
	}
	
	private static void loadSound(File f) {
		Urmusic.loadPane.startLoading();
		Thread t = new Thread(() -> {
			Sound loadedSound = AudioEngine.ENGINE.loadSound(f);
			
			if(loadedSound != null) {
				Urmusic.thePlayer.setCurrentSound(loadedSound);
				loadedSound.startAnalysis(ApplicationPreferences.audioAnalysisFramerate);
			}
			
			Urmusic.loadPane.stopLoading();
		});
		t.setDaemon(true);
		t.start();
	}
	
	private static void openSound() {
		File f = Urmusic.audioFileChooser.showOpenDialog(Urmusic.stg);
		
		if(f == null) return;
		
		Urmusic.loadSound(f);
	}
	
	private static void newProject() {
		createProjectTab(new Project());
	}
	
	private static void openProject() {
		List<File> files = Urmusic.fileChooser.showOpenMultipleDialog(Urmusic.stg);
		
		if(files == null) return;
		
		for(File f : files) {
			try {
				loadProject(f);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void saveProject() {
		// TODO: saveProject()
	}
	
	private static void saveProjectAs() {
		// TODO: saveProjectAs()
	}
	
	private static void openWiki() {
		// TODO: Move the wiki url for the desktop only?
		Urmusic.appInstance.getHostServices().showDocument("https://github.com/Nasso/urmusic/wiki");
	}
	
	private static void openAbout() {
		Urmusic.aboutDialog.show();
	}
	
	private static void exportVideo() {
		Sound currentSound = Urmusic.thePlayer.getCurrentSound();
		if(currentSound != null) {
			currentSound.stop();
		}
		
		Urmusic.videoExportSettingsPane.beginSetting(currentSound);
	}
	
	private static void startVideoExport(VideoExportSettings s) {
		Urmusic.vidSettings = s;
		
		try {
			Urmusic.vidStream = VideoEngine.ENGINE.createStream(Urmusic.vidSettings);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(Urmusic.vidStream == null) {
			System.err.println("Couldn't create Video Stream");
			return; // rip
		}
		currentAnalysedPosition = 0;
		currentRenderedPosition = 0;
		Urmusic.vidFrameLength = 1.0 / Urmusic.vidSettings.framerate;
		
		if(Urmusic.renderingCanvas == null) Urmusic.renderingCanvas = new Canvas();
		Urmusic.renderingCanvas.setWidth(Urmusic.vidSettings.width);
		Urmusic.renderingCanvas.setHeight(Urmusic.vidSettings.height);
		
		if(Urmusic.snapParams == null) Urmusic.snapParams = new SnapshotParameters();
		Urmusic.snapParams.setViewport(new Rectangle2D(0, 0, Urmusic.vidSettings.width, Urmusic.vidSettings.height));
		
		Urmusic.renderer.setMotionBlur(Urmusic.vidSettings.motionBlur);
		
		Urmusic.exportingVidPane.beginExport(Urmusic.vidSettings);
		/*
		Urmusic.vidExportTask = new Task<Boolean>() {
			protected Boolean call() throws Exception {
				return null;
			}
		};*/
	}
	
	public static void showError(String err) {
		Platform.runLater(() -> {
			Urmusic.errorAlert.setContentText(err);
			Urmusic.errorAlert.show();
		});
	}
	
	public static void bringTheModality() {
		Urmusic.modalityPane.setMouseTransparent(false);
		
		Urmusic.modalityTransitionOff.stop();
		Urmusic.modalityTransitionOn.playFromStart();
	}
	
	public static void removeTheModality() {
		Urmusic.modalityPane.setMouseTransparent(true);
		
		Urmusic.modalityTransitionOn.stop();
		Urmusic.modalityTransitionOff.playFromStart();
	}
}
