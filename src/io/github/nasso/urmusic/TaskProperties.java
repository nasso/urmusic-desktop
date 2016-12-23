package io.github.nasso.urmusic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.FloatPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;

public class TaskProperties {
	private BooleanProperty undefined = new BooleanPropertyBase(false) {
		public String getName() {
			return "undefined";
		}
		
		public Object getBean() {
			return this;
		}
	};
	
	private FloatProperty progress = new FloatPropertyBase(0.0f) {
		public String getName() {
			return "progress";
		}
		
		public Object getBean() {
			return this;
		}
	};
	
	private StringProperty text = new StringPropertyBase() {
		public String getName() {
			return "text";
		}
		
		public Object getBean() {
			return this;
		}
	};
	
	public boolean getUndefined() {
		return undefined.get();
	}
	
	public void setUndefined(boolean undefined) {
		this.undefined.set(undefined);
	}
	
	public BooleanProperty undefinedProperty() {
		return this.undefined;
	}
	
	public float getProgress() {
		return progress.get();
	}
	
	public void setProgress(float progress) {
		this.progress.set(progress);
	}
	
	public FloatProperty progressProperty() {
		return this.progress;
	}
	
	public String getText() {
		return text.get();
	}
	
	public void setText(String text) {
		this.text.set(text);
	}
	
	public StringProperty textProperty() {
		return this.text;
	}
}
