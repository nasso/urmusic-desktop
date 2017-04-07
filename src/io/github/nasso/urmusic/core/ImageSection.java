package io.github.nasso.urmusic.core;

import java.io.ByteArrayInputStream;
import java.util.Base64;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ImageSection implements SectionTarget {
	public static final SectionType THIS_TYPE = SectionType.IMAGE;
	
	private String imageURL = null;
	public ExpressionProperty imageBorderRadius = new ExpressionProperty("0.0");
	public boolean opaque = false;
	public ExpressionProperty borderSize = new ExpressionProperty("0.0");
	
	public Color borderColor = Color.web("#ffffff");
	public boolean borderVisible = false;
	
	public Image image = null;
	
	public ImageSection() {
		
	}
	
	public ImageSection(PrimitiveProperties p) {
		this.set(p);
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public void dispose() {
		this.imageBorderRadius.dispose();
		this.borderSize.dispose();
	}
	
	public void refreshOwnProperties(FrameProperties props) {
		this.imageBorderRadius.refresh(props);
		this.borderSize.refresh(props);
	}
	
	public String getImageURL() {
		return this.imageURL;
	}
	
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
		
		if(imageURL == null) {
			this.image = null;
		} else if(!this.imageURL.equals("")) {
			if(this.imageURL.startsWith("data:")) {
				this.image = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(this.imageURL.substring(this.imageURL.indexOf(",") + 1))));
			}
			
			// @format:off
			else if(
				this.imageURL.startsWith("file:") ||
				this.imageURL.startsWith("http:") ||
				this.imageURL.startsWith("https:")) this.image = new Image(this.imageURL);
			// @format:on
			
			else this.image = new Image("file:" + this.imageURL, true);
		}
	}
	
	public void set(PrimitiveProperties p) {
		this.setImageURL(p.getString("imageURL", ""));
		this.imageBorderRadius.setExpr(p.getString("imageBorderRadius", "0.0"));
		this.opaque = p.getBool("opaque", false);
		this.borderSize.setExpr(p.getString("borderSize", "0.0"));
		
		this.borderColor = Color.web(p.getString("borderColor", "#ffffff"));
		this.borderVisible = p.getBool("borderVisible", false);
	}
	
	public void set(SectionTarget other) {
		if(!(other instanceof ImageSection)) return;
		
		ImageSection is = (ImageSection) other;
		this.imageURL = is.imageURL;
		this.imageBorderRadius.setExpr(is.imageBorderRadius.getExpr());
		this.opaque = is.opaque;
		this.borderSize.setExpr(is.borderSize.getExpr());
		this.borderColor = is.borderColor;
		this.borderVisible = is.borderVisible;
		this.image = is.image;
	}
}
