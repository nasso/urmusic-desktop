package io.github.nasso.urmusic;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
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
import io.github.nasso.urmusic.json.JSONProjectCodec;
import io.github.nasso.urmusic.log.LoggingOutputStream;
import io.github.nasso.urmusic.log.StdOutErrLevel;
import io.github.nasso.urmusic.ui.ProjectPane;
import io.github.nasso.urmusic.ui.SizeDialog;
import io.github.nasso.urmusic.ui.ThePlayer;
import io.github.nasso.urmusic.ui.UrExportingVideoStatusPane;
import io.github.nasso.urmusic.ui.UrLoadingPane;
import io.github.nasso.urmusic.ui.UrVideoExportSettingsPane;
import io.github.nasso.urmusic.video.VideoExportSettings;
import io.github.nasso.urmusic.video.VideoExportTask;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
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
	public static final Background PANES_BACKGROUND_FLAT = new Background(new BackgroundFill(Color.web("#111"), null, null));
	public static final Effect PANES_EFFECT = new DropShadow(16, Color.BLACK);
	
	public static final boolean DEBUG = false;
	public static final int FFTSIZE = 2048;
	public static final int FFTSIZE_HALF = FFTSIZE / 2;
	
	public static final double EXPORT_VID_PANES_OFFSET_TOP = 32;
	public static final double EXPORT_VID_PANES_OFFSET_BOTTOM = 32;
	
	public static final File APPLICATION_PREFERENCES_FILE = new File("urmusic.pref");
	
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
	private static SizeDialog sizeDialog;
	
	private static AnalyseData analyseData;
	
	private static long now_nano;
	
	// Vid stuff
	private static int currentAnalysedFrame = -1;
	private static int currentRenderedFrame = 0;
	private static int lastAnalysedFrame = currentAnalysedFrame - 1;
	private static double vidFrameLength = 0;
	private static double audioAnalysisFrameLength = 1.0 / ApplicationPreferences.audioAnalysisFramerate;
	private static double exportingTime = 0.0;
	private static boolean requestVideoStop = false;
	
	private static VideoExportTask vidExportTask;
	private static WritableImage snapImg;
	private static SnapshotParameters snapParams = new SnapshotParameters();
	private static Canvas renderingCanvas;
	
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
		Urmusic.sizeDialog = new SizeDialog("Size", 1280, 720);
		
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
		Urmusic.tabPane.prefHeightProperty().bind(sce.heightProperty().subtract(30).subtract(ThePlayer.PLAYER_HEIGHT));
		
		Urmusic.tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
		
		Tab addTabTab = new Tab("+");
		addTabTab.setClosable(false);
		Urmusic.tabPane.getTabs().add(addTabTab);
		
		// TODO: choose a default preset (or add an ApplicationPreference)
		loadProject(new File("presets/dubstepgutter.urm"));
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
		Urmusic.exportingVidPane.prefWidthProperty().bind(sce.widthProperty().multiply(0.8));
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
			
			if(Urmusic.vidExportTask != null) {
				Urmusic.vidExportTask.requestStop();
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
				if(now - this.lastFPSUpdate > 1_000_000_000) {
					this.fps = this.frameCounter;
					this.frameCounter = 0;
					this.lastFPSUpdate = now;
					
					if(Urmusic.DEBUG) System.out.println("FPS: " + this.fps + " | Frame time: " + ((now - this.lastFrameTime) / 1_000_000.0) + "ms");
				}
				
				Urmusic.loop(now);
				
				this.frameCounter++;
				this.lastFrameTime = now;
			}
		};
		
		timer.start();
	}
	
	private static void recordVideoFrame(Project proj, float per) {
		Urmusic.renderer.render(proj, renderingCanvas);
		
		renderingCanvas.snapshot(Urmusic.snapParams, Urmusic.snapImg);
		
		Urmusic.vidExportTask.setNextImage(snapImg);
		Urmusic.exportingVidPane.update(snapImg, per, Urmusic.now_nano);
	}
	
	private static void analysePulse(Project proj) {
		FrameProperties props = proj.getFrameProperties();
		
		if(Urmusic.thePlayer.getCurrentSound() != null) {
			if(Urmusic.vidExportTask != null) {
				// Recording
				props.duration = (float) Urmusic.thePlayer.getCurrentSound().getDuration();
				props.time = (float) exportingTime;
			} else {
				props.time = (float) Urmusic.thePlayer.getCurrentSound().getPosition();
				props.duration = (float) Urmusic.thePlayer.getCurrentSound().getDuration();
			}
			
			Urmusic.thePlayer.getCurrentSound().setSmoothingTimeConstant(proj.getSettings().smoothingTimeConstant.getValueAsFloat());
			Urmusic.thePlayer.getCurrentSound().setLowSmoothingTimeConstant(proj.getSettings().advanced.lowpassSmooth);
			Urmusic.thePlayer.getCurrentSound().setHighSmoothingTimeConstant(proj.getSettings().advanced.highpassSmooth);
			Urmusic.thePlayer.getCurrentSound().setLowpassEnabled(proj.getSettings().advanced.enableLowpass);
			Urmusic.thePlayer.getCurrentSound().setHighpassEnabled(proj.getSettings().advanced.enableHighpass);
			Urmusic.thePlayer.getCurrentSound().setLowpassFreq(proj.getSettings().advanced.lowpassFreq);
			Urmusic.thePlayer.getCurrentSound().setHighpassFreq(proj.getSettings().advanced.highpassFreq);
			
			if(Urmusic.vidExportTask == null || lastAnalysedFrame < currentAnalysedFrame) {
				Urmusic.thePlayer.getCurrentSound().getAnalysedData(Urmusic.analyseData, currentAnalysedFrame * Urmusic.audioAnalysisFrameLength);
				lastAnalysedFrame = currentAnalysedFrame;
			}
			
			props.maxval = Urmusic.analyseData.maxval;
			props.minval = Urmusic.analyseData.minval;
			
			props.maxlowval = Urmusic.analyseData.maxlowval;
			props.minlowval = Urmusic.analyseData.minlowval;
			
			props.maxhighval = Urmusic.analyseData.maxhighval;
			props.minhighval = Urmusic.analyseData.minhighval;
			
			props.prettytime = Utils.prettyTime(props.time);
			props.prettyduration = Utils.prettyTime(props.duration);
		}
		
		ExpressionEngine.ENGINE.update(props);
	}
	
	private static void loop(long now) {
		Urmusic.now_nano = now;
		
		int projI = Urmusic.tabPane.getSelectionModel().getSelectedIndex();
		if(projI >= Urmusic.openedProjects.size()) return;
		
		ProjectPane pane = (ProjectPane) Urmusic.tabPane.getSelectionModel().getSelectedItem().getContent();
		Project proj = Urmusic.openedProjects.get(projI);
		
		thePlayer.refresh();
		
		// If recording...
		if(vidExportTask != null) {
			if(!vidExportTask.needsANewImage()) return;
			
			analysePulse(proj);
			
			if(audioAnalysisFrameLength > vidFrameLength) {
				// FIXME
				// Here: time to next audio analysis frame > time to next video frame
				// Higher framerate: VIDEO
				//
				// Then, increment by the smallest time: VIDEO FRAME LENGTH
				//
				currentRenderedFrame++;
				exportingTime = currentRenderedFrame * vidFrameLength;
				
				if(Math.floor(exportingTime / audioAnalysisFrameLength) >= currentAnalysedFrame) {
					recordVideoFrame(proj, (float) (exportingTime / vidExportTask.vidSettings().durationSec));
					currentAnalysedFrame++;
				}
			} else if(vidFrameLength > audioAnalysisFrameLength) {
				//
				// Here: time to next video frame > time to next audio analysis frame
				// Higher framerate: AUDIO
				//
				// Then, increment by the smallest time: AUDIO FRAME LENGTH
				//
				currentAnalysedFrame++;
				exportingTime = currentAnalysedFrame * audioAnalysisFrameLength;
				
				//
				// > time / unit
				// gives how many "unit"s there's in "time"
				//
				// > floor(time / unit)
				// gives how many full "unit"s there's in "time"
				// 
				// Here: convert the current analysed position and the current rendered position in full video frames, and check if the analyse is on the next frame
				// 
				if(Math.floor(exportingTime / vidFrameLength) >= currentRenderedFrame) {
					recordVideoFrame(proj, (float) (exportingTime / vidExportTask.vidSettings().durationSec));
					currentRenderedFrame++;
				}
			} else {
				//
				// Here: video and audio sync
				// Higher framerate: BOTH
				//
				// Then, increment by the smallest time: BOTH
				//
				currentRenderedFrame++;
				currentAnalysedFrame++;
				exportingTime = currentRenderedFrame * vidFrameLength;
				
				recordVideoFrame(proj, (float) (exportingTime / vidExportTask.vidSettings().durationSec));
			}
			
			requestVideoStop |= exportingTime >= vidExportTask.vidSettings().durationSec;
			
			if(requestVideoStop) {
				exportingVidPane.finishExport();
				loadPane.startLoading();
				renderer.setMotionBlur(Urmusic.motionBlurCheckboxItem.isSelected());
				currentAnalysedFrame = -1;
				currentRenderedFrame = 0;
				lastAnalysedFrame = currentAnalysedFrame - 1;
				vidExportTask.requestStop();
				vidExportTask = null;
				exportingTime = 0;
				thePlayer.getCurrentSound().startAnalysis(ApplicationPreferences.audioAnalysisFramerate);
			}
		} else {
			// if not recording, render as usual
			analysePulse(proj);
			renderer.render(proj, pane.getCanvas());
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
		
		Tab tab = new Tab(p.name);
		tab.setContent(new ProjectPane(p));
		tab.setOnCloseRequest((e) -> {
			p.dispose();
			Urmusic.openedProjects.remove(p);
		});
		p.setOnNameChanged(() -> {
			tab.setText(p.name);
		});
		
		Urmusic.tabPane.getTabs().add(i, tab);
		Urmusic.tabPane.getSelectionModel().select(i);
	}
	
	private static Project loadProject(File f) throws IOException {
		if(!f.isFile()) return null;
		
		Project p = JSONProjectCodec.loadProject(f);
		if(p != null) createProjectTab(p);
		
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
	
	private static Project getCurrentTabProject() {
		int projI = Urmusic.tabPane.getSelectionModel().getSelectedIndex();
		if(projI >= Urmusic.openedProjects.size()) return null;
		
		return Urmusic.openedProjects.get(projI);
	}
	
	private static void openSound() {
		File f = Urmusic.audioFileChooser.showOpenDialog(Urmusic.stg);
		
		if(f == null) return;
		
		Urmusic.loadSound(f);
	}
	
	private static void newProject() {
		Optional<int[]> size = Urmusic.sizeDialog.showAndWait();
		
		if(size.isPresent()) {
			int[] isize = size.get();
			createProjectTab(new Project(isize[0], isize[1]));
		} else {
			tabPane.getSelectionModel().selectPrevious();
		}
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
	
	private static void saveProject(Project proj) {
		if(proj == null) return;
		
		JSONProjectCodec.saveProject(proj);
	}
	
	private static void saveProjectAs(Project proj) {
		File f = Urmusic.fileChooser.showSaveDialog(Urmusic.stg);
		if(f != null) proj.setProjectFile(f);
		else return;
		
		saveProject(proj);
	}
	
	private static void saveProject() {
		Project proj = getCurrentTabProject();
		if(proj == null) return;
		
		if(proj.getProjectFile() == null) saveProjectAs(proj);
		else saveProject(proj);
	}
	
	private static void saveProjectAs() {
		saveProjectAs(getCurrentTabProject());
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
		Project proj = getCurrentTabProject();
		if(proj == null) return;
		
		s.width = proj.getSettings().framewidth;
		s.height = proj.getSettings().frameheight;
		
		vidExportTask = null;
		
		try {
			vidExportTask = new VideoExportTask(s);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(vidExportTask == null) {
			System.err.println("Couldn't create the video stream");
			return; // rip
		}
		
		vidFrameLength = 1.0 / s.framerate;
		lastAnalysedFrame = currentAnalysedFrame - 1;
		
		if(renderingCanvas == null) renderingCanvas = new Canvas();
		renderingCanvas.setWidth(s.width);
		renderingCanvas.setHeight(s.height);
		
		if(snapImg == null || snapImg.getWidth() != s.width || snapImg.getHeight() != s.height) snapImg = new WritableImage(s.width, s.height);
		
		renderer.setMotionBlur(s.motionBlur);
		
		thePlayer.getCurrentSound().resetSmoothingBuffer();
		thePlayer.getCurrentSound().stopAnalysis();
		
		requestVideoStop = false;
		new Thread(() -> {
			vidExportTask.run();
			
			Platform.runLater(() -> {
				loadPane.stopLoading();
			});
		}).start();
		exportingVidPane.beginExport(s, now_nano);
	}
	
	public static void showError(String err) {
		Platform.runLater(() -> {
			Urmusic.errorAlert.setContentText(err);
			Urmusic.errorAlert.show();
		});
	}
	
	public static Optional<int[]> showSizeDialog() {
		return Urmusic.sizeDialog.showAndWait();
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
