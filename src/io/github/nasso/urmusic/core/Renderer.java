package io.github.nasso.urmusic.core;

import java.util.HashMap;
import java.util.Map;

import io.github.nasso.urmusic.Project;
import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.Utils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.MotionBlur;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class Renderer {
	private Canvas cvs;
	private GraphicsContext gtx;
	
	private float cwidth = 0;
	private float cheight = 0;
	
	private float csize = 0;
	private float glblscl = 0;
	private float glowness = 0;
	
	private int dataCount = 32;
	
	private float[] freqData;
	private float[] timeDomData;
	
	private boolean motionBlur = false;
	private float motionBlurAmount = 1.0f;
	
	private DropShadow glowEffect = new DropShadow();
	private MotionBlur motionEffect = new MotionBlur();
	
	private Section section;
	
	private FrameProperties frameProps;
	private Settings settings;
	
	public Renderer() {
	}
	
	public void render(Project proj) {
		this.render(proj, proj.getCanvas());
	}
	
	public void render(Project proj, Canvas targetCvs) {
		this.freqData = Urmusic.getFrequencyData();
		this.timeDomData = Urmusic.getTimeDomData();
		
		this.cvs = targetCvs;
		this.gtx = this.cvs.getGraphicsContext2D();
		
		this.frameProps = proj.getFrameProperties();
		this.settings = proj.getSettings();
		
		this.cwidth = (float) cvs.getWidth();
		this.cheight = (float) cvs.getHeight();
		
		this.csize = Math.min(this.cwidth, this.cheight);
		
		gtx.clearRect(0, 0, this.cwidth, this.cheight);
		
		gtx.setFill(settings.backgroundColor);
		gtx.fillRect(0, 0, this.cwidth, this.cheight);
		
		gtx.save();
		{
			gtx.translate(this.cwidth / 2, this.cheight / 2);
			gtx.scale(0.5, -0.5);
			
			this.glblscl = settings.globalScale.getValueAsFloat();
			gtx.scale(this.glblscl, this.glblscl);
			
			gtx.translate(settings.globalOffsetX.getValueAsFloat() * this.csize, settings.globalOffsetY.getValueAsFloat() * this.csize);
			gtx.rotate(settings.globalRotation.getValueAsFloat());
			
			for(int is = 0; is < settings.sections.size(); is++) {
				this.section = settings.sections.get(is);
				
				if(!section.visible || section.target == null) continue;
				
				if(section.type != SectionType.IMAGE) {
					frameProps.imgw = frameProps.imgh = frameProps.imgr = 0;
				} else {
					ImageSection target = (ImageSection) section.target;
					
					if(target.image != null) {
						frameProps.imgw = (float) target.image.getWidth();
						frameProps.imgh = (float) target.image.getHeight();
						frameProps.imgr = frameProps.imgw / frameProps.imgh;
						
						section.refreshProperties(frameProps);
					}
				}
				
				this.glowness = section.glowness.getValueAsFloat();
				
				gtx.save();
				{
					gtx.translate(section.posX.getValueAsFloat() * this.csize, section.posY.getValueAsFloat() * this.csize);
					gtx.rotate(section.rotation.getValueAsFloat());
					gtx.scale(section.scaleX.getValueAsFloat(), section.scaleY.getValueAsFloat());
					
					gtx.setGlobalAlpha(section.opacity.getValueAsFloat());
					
					if(section.type == SectionType.FREQ) {
						renderFreqSection();
					} else if(section.type == SectionType.TIME_DOM) {
						renderTimeDomSection();
					} else if(section.type == SectionType.IMAGE) {
						renderImageSection();
					} else if(section.type == SectionType.TEXT) {
						renderTextSection();
					}
				}
				gtx.restore();
			}
		}
		gtx.restore();
	}
	
	private float freqValue(float nind, FreqSection section) {
		float minDec = section.minDecibels.getValueAsFloat();
		float maxDec = section.maxDecibels.getValueAsFloat();
		
		// @format:off
		float val = Math.max(
			Utils.getValue(
				freqData,
				Utils.lerp(section.freqStart.getValueAsFloat(), section.freqEnd.getValueAsFloat(), nind) * freqData.length,
				section.quadratic,
				minDec) - minDec,
			0) / (maxDec - minDec);
		// @format:on
		
		if(!section.clampToMaxDecibels) return val;
		return Utils.clamp(val, 0.0f, 1.0f);
	}
	
	private float[] sectionPerProps = new float[4];
	
	private float[] getFootProps(FreqSection section, float per) {
		float x = Utils.lerp(section.startX.getValueAsFloat(), section.endX.getValueAsFloat(), per);
		float y = section.offsetY.getValueAsFloat();
		
		float polar = section.polar.getValueAsFloat();
		if(polar > 0.0) {
			float cosx = (float) Math.cos((x * 0.5 + 0.5) * Math.PI * 2);
			float sinx = (float) Math.sin((x * 0.5 + 0.5) * Math.PI * 2);
			
			float xp = cosx * y;
			float yp = sinx * y;
			
			x = Utils.lerp(x * csize, xp * csize, polar);
			y = Utils.lerp(y * csize, yp * csize, polar);
		} else {
			x *= csize;
			y *= csize;
		}
		
		sectionPerProps[0] = x;
		sectionPerProps[1] = y;
		
		return sectionPerProps;
	}
	
	private float[] getProps(FreqSection section, float per) {
		float height = (float) (Math.pow(freqValue(per, section), section.exponent.getValueAsFloat()) * section.height.getValueAsFloat());
		
		float x = Utils.lerp(section.startX.getValueAsFloat(), section.endX.getValueAsFloat(), per);
		float y = section.offsetY.getValueAsFloat();
		
		float ey = y + section.minHeight.getValueAsFloat() + height;
		float ex = x;
		
		float polar = section.polar.getValueAsFloat();
		if(polar > 0.0f) {
			float cosx = (float) Math.cos((x * 0.5 + 0.5) * Math.PI * 2);
			float sinx = (float) Math.sin((x * 0.5 + 0.5) * Math.PI * 2);
			
			float xp = cosx * y;
			float yp = sinx * y;
			float exp = cosx * ey;
			float eyp = sinx * ey;
			
			x = Utils.lerp(x * csize, xp * csize, polar);
			y = Utils.lerp(y * csize, yp * csize, polar);
			ex = Utils.lerp(ex * csize, exp * csize, polar);
			ey = Utils.lerp(ey * csize, eyp * csize, polar);
		} else {
			x *= csize;
			y *= csize;
			ex *= csize;
			ey *= csize;
		}
		
		sectionPerProps[0] = x;
		sectionPerProps[1] = y;
		sectionPerProps[2] = ex;
		sectionPerProps[3] = ey;
		
		return sectionPerProps;
	}
	
	private float[] getTimeFootProps(TimeDomSection section, float per) {
		float x = Utils.lerp(section.startX.getValueAsFloat(), section.endX.getValueAsFloat(), per);
		float y = section.offsetY.getValueAsFloat();
		
		float polar = section.polar.getValueAsFloat();
		if(polar > 0.0f) {
			float cosx = (float) Math.cos((x * 0.5 + 0.5) * Math.PI * 2);
			float sinx = (float) Math.sin((x * 0.5 + 0.5) * Math.PI * 2);
			
			float xp = cosx * y;
			float yp = sinx * y;
			
			x = Utils.lerp(x * csize, xp * csize, polar);
			y = Utils.lerp(y * csize, yp * csize, polar);
		} else {
			x *= csize;
			y *= csize;
		}
		
		sectionPerProps[0] = x;
		sectionPerProps[1] = y;
		
		return sectionPerProps;
	}
	
	private float[] getTimeProps(TimeDomSection section, float per) {
		float height = Utils.getValue(timeDomData, per * timeDomData.length, section.quadratic, 0);
		
		float powered = (float) Math.abs(Math.pow(height, section.exponent.getValueAsFloat()));
		height = (height >= 0 ? powered : -powered) * section.height.getValueAsFloat();
		
		float x = Utils.lerp(section.startX.getValueAsFloat(), section.endX.getValueAsFloat(), per);
		float y = section.offsetY.getValueAsFloat();
		
		float ey = y + height;
		float ex = x;
		
		float polar = section.polar.getValueAsFloat();
		if(polar > 0.0f) {
			float cosx = (float) Math.cos((x * 0.5 + 0.5) * Math.PI * 2);
			float sinx = (float) Math.sin((x * 0.5 + 0.5) * Math.PI * 2);
			
			float xp = cosx * y;
			float yp = sinx * y;
			float exp = cosx * ey;
			float eyp = sinx * ey;
			
			x = Utils.lerp(x * csize, xp * csize, polar);
			y = Utils.lerp(y * csize, yp * csize, polar);
			ex = Utils.lerp(ex * csize, exp * csize, polar);
			ey = Utils.lerp(ey * csize, eyp * csize, polar);
		} else {
			x *= csize;
			y *= csize;
			ex *= csize;
			ey *= csize;
		}
		
		sectionPerProps[0] = x;
		sectionPerProps[1] = y;
		sectionPerProps[2] = ex;
		sectionPerProps[3] = ey;
		
		return sectionPerProps;
	}
	
	private Map<Section, float[]> lastPositions = new HashMap<Section, float[]>();
	
	private void setupEffect() {
		Effect finalEffect = null;
		
		if(this.glowness > 0.0f) {
			glowEffect.setColor(section.color);
			glowEffect.setRadius(this.glowness * this.glblscl);
			
			finalEffect = glowEffect;
		}
		
		if(this.motionBlur) {
			float posX = (this.section.posX.getValueAsFloat() + this.settings.globalOffsetX.getValueAsFloat()) * this.csize * this.glblscl;
			float posY = (this.section.posY.getValueAsFloat() + this.settings.globalOffsetY.getValueAsFloat()) * this.csize * this.glblscl;
			
			float[] lastPos;
			if(this.lastPositions.containsKey(this.section)) {
				lastPos = this.lastPositions.get(this.section);
				
				float diffX = posX - lastPos[0];
				float diffY = posY - lastPos[1];
				
				float angle = (float) Math.toDegrees(Math.atan2(diffX, diffY));
				float dist = (float) Math.sqrt(diffX * diffX + diffY * diffY);
				
				lastPos[0] = posX;
				lastPos[1] = posY;
				
				this.motionEffect.setInput(finalEffect);
				this.motionEffect.setAngle(angle);
				this.motionEffect.setRadius(Utils.clamp(dist * this.motionBlurAmount, 0.0, 63.0));
				
				finalEffect = this.motionEffect;
			} else {
				lastPos = new float[] { posX, posY };
				this.lastPositions.put(this.section, lastPos);
				this.section.addDisposeListener((s) -> {
					this.lastPositions.remove(s);
				});
			}
		}
		
		gtx.setEffect(finalEffect);
	}
	
	private void renderFreqSection() {
		FreqSection sectarg = (FreqSection) section.target;
		
		this.dataCount = sectarg.dataCount.getValueAsInt();
		DrawMode mode = sectarg.mode;
		
		gtx.setStroke(section.color);
		gtx.setFill(section.color);
		gtx.setLineWidth((sectarg.lineWidth.getValueAsFloat() / 100f) * this.csize);
		
		setupEffect();
		
		gtx.setLineCap(sectarg.lineCap);
		
		gtx.beginPath();
		for(int i = 0; i < dataCount; i++) {
			if(!sectarg.drawLast && i == dataCount - 1) {
				break;
			}
			
			float per = i / (dataCount - 1.0f);
			
			float[] p = getProps(sectarg, per);
			
			if(mode == DrawMode.LINES || (i == 0 && sectarg.clampShapeToZero)) {
				gtx.moveTo(p[0], p[1]);
			}
			
			gtx.lineTo(p[2], p[3]);
			
			if(i == dataCount - 1 && sectarg.clampShapeToZero) {
				gtx.lineTo(p[0], p[1]);
			}
		}
		
		if(sectarg.closeShape && mode != DrawMode.FILL) {
			gtx.closePath();
		}
		
		if(mode == DrawMode.FILL) {
			if(sectarg.smartFill) {
				for(float i = dataCount - 1; i >= 0; i--) {
					if(!sectarg.drawLast && i == dataCount - 1) {
						continue;
					}
					
					float per = i / (dataCount - 1);
					float[] fp = getFootProps(sectarg, per);
					
					gtx.lineTo(fp[0], fp[1]);
				}
			}
			
			gtx.fill();
		} else {
			gtx.moveTo(0, 0);
			gtx.stroke();
		}
	}
	
	private void renderTimeDomSection() {
		TimeDomSection sectarg = (TimeDomSection) section.target;
		
		this.dataCount = sectarg.dataCount.getValueAsInt();
		DrawMode mode = sectarg.mode;
		
		gtx.setStroke(section.color);
		gtx.setFill(section.color);
		gtx.setLineWidth(sectarg.lineWidth.getValueAsFloat() / 100f * csize);
		
		setupEffect();
		
		gtx.setLineCap(sectarg.lineCap);
		gtx.setLineJoin(sectarg.lineJoin);
		
		gtx.beginPath();
		for(int i = 0; i < dataCount; i++) {
			if(!sectarg.drawLast && i == dataCount - 1) {
				break;
			}
			
			float per = i / (dataCount - 1.0f);
			float[] p = getTimeProps(sectarg, per);
			
			if(mode == DrawMode.LINES || (i == 0 && sectarg.clampShapeToZero)) {
				gtx.moveTo(p[0], p[1]);
			}
			
			gtx.lineTo(p[2], p[3]);
			
			if(i == dataCount - 1 && sectarg.clampShapeToZero) {
				gtx.lineTo(p[0], p[1]);
			}
		}
		
		if(sectarg.closeShape && mode != DrawMode.FILL) {
			gtx.closePath();
		}
		
		if(mode == DrawMode.FILL) {
			if(sectarg.smartFill) {
				for(int i = dataCount - 1; i >= 0; i--) {
					if(!sectarg.drawLast && i == dataCount - 1) {
						continue;
					}
					
					float per = i / (dataCount - 1.0f);
					float[] fp = getTimeFootProps(sectarg, per);
					
					gtx.lineTo(fp[0], fp[1]);
				}
			}
			
			gtx.fill();
		} else {
			gtx.moveTo(0, 0);
			gtx.stroke();
		}
	}
	
	private void renderImageSection() {
		ImageSection sectarg = (ImageSection) section.target;
		
		gtx.setLineWidth((sectarg.borderSize.getValueAsFloat() / 100f) * this.csize);
		
		gtx.setFill(section.color);
		gtx.setStroke(sectarg.borderColor);
		
		setupEffect();
		
		gtx.scale(1, -1);
		
		float imgBorderRad = sectarg.imageBorderRadius.getValueAsFloat() * this.csize;
		if(imgBorderRad != 0.0f) {
			Utils.roundRect(gtx, -this.csize / 2f, -this.csize / 2f, this.csize, this.csize, imgBorderRad);
			gtx.clip();
		} else {
			gtx.beginPath();
			gtx.rect(-this.csize / 2f, -this.csize / 2f, this.csize, this.csize);
		}
		
		if(sectarg.opaque) gtx.fill();
		if(sectarg.borderVisible) gtx.stroke();
		
		if(sectarg.image != null) {
			gtx.drawImage(sectarg.image, -this.csize / 2f, -this.csize / 2f, this.csize, this.csize);
		}
	}
	
	private void renderTextSection() {
		TextSection sectarg = (TextSection) section.target;
		
		Object otxt = sectarg.text.getValue();
		String txt;
		
		if(otxt == null || "".equals(txt = otxt.toString())) return;
		
		setupEffect();
		
		gtx.setFill(section.color);
		gtx.setFont(Font.font(sectarg.fontFamily, FontWeight.findByName(sectarg.fontStyle), FontPosture.findByName(sectarg.fontStyle), sectarg.fontSize.getValueAsFloat() * csize));
		gtx.setTextAlign(sectarg.textAlign);
		gtx.setTextBaseline(sectarg.textBaseline);
		
		gtx.scale(1, -1);
		
		gtx.fillText(txt, 0, 0);
	}
	
	public Canvas getCanvas() {
		return this.cvs;
	}
	
	public boolean isMotionBlur() {
		return motionBlur;
	}
	
	public void setMotionBlur(boolean motionBlur) {
		this.motionBlur = motionBlur;
	}
	
	public float getMotionBlurAmount() {
		return motionBlurAmount;
	}
	
	public void setMotionBlurAmount(float motionBlurAmount) {
		this.motionBlurAmount = motionBlurAmount;
	}
}
