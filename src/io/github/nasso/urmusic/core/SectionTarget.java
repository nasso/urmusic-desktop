package io.github.nasso.urmusic.core;

public interface SectionTarget {
	public void dispose();
	
	public void set(PrimitiveProperties props);
	
	public void refreshOwnProperties(FrameProperties props);
}
