package io.github.nasso.urmusic;

public abstract class UrAnimationTimer {
	public static final int UNLIMITED_FPS = -1;
	
	private Runnable loopRunner;
	
	private boolean stopRequest = false;
	private boolean running = false;
	private long fps = 0;
	
	public UrAnimationTimer(String name, double requestedFPS) {
		loopRunner = () -> {
			this.running = true;
			if(Urmusic.DEBUG) System.out.println("[" + name + "] UrAnimationTimer started");
			
			long startTime = System.nanoTime();
			
			long lastFPSUpdate = 0;
			long frameCounter = 0;
			fps = 0;
			
			long lastFrameTime = 0;
			long now;
			
			long targetFrameTime = requestedFPS > 0 ? (long) (1_000_000_000.0 / requestedFPS) : 0;
			long frameTime = 0;
			
			while(!this.stopRequest) {
				now = System.nanoTime();
				
				if(now - lastFPSUpdate > 1_000_000_000) {
					fps = frameCounter;
					frameCounter = -1;
					lastFPSUpdate = now;
					
					if(Urmusic.DEBUG) System.out.println("[" + name + "] FPS: " + fps + " | Frame time: " + ((now - lastFrameTime) / 1_000_000.0) + "ms");
				}
				
				this.handle(now - startTime);
				
				lastFrameTime = now;
				frameCounter++;
				
				frameTime = System.nanoTime() - now;
				if(frameTime < targetFrameTime) {
					try {
						Thread.sleep((targetFrameTime - frameTime) / 1_000_000);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			this.running = false;
			if(Urmusic.DEBUG) System.out.println("[" + name + "] UrAnimationTimer stopped");
		};
	}
	
	protected abstract void handle(long now);
	
	public boolean isRunning() {
		return this.running;
	}
	
	public long getFPS() {
		return this.fps;
	}
	
	public void stop() {
		this.stopRequest = true;
	}
	
	public void start(boolean async) {
		if(async) {
			Thread t = new Thread(loopRunner);
			t.setDaemon(true);
			t.start();
		} else {
			loopRunner.run();
		}
	}
	
	public void start() {
		this.start(true);
	}
}
