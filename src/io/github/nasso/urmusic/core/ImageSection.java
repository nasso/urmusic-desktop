package io.github.nasso.urmusic.core;

import java.io.ByteArrayInputStream;
import java.util.Base64;

import io.github.nasso.urmusic.expression.ExpressionProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ImageSection implements SectionTarget {
	private String imageURL = "";
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
	
	public void dispose() {
		this.imageBorderRadius.dispose();
		this.borderSize.dispose();
	}
	
	public void refreshOwnProperties(FrameProperties props) {
		this.imageBorderRadius.refresh(props);
		this.borderSize.refresh(props);
	}
	
	public String getImageURL() {
		return imageURL;
	}
	
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
		
		if(!this.imageURL.equals("")) {
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
}
