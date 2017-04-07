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
	
	private float[] freqData;
	private float[] timeDomData;
	
	private boolean motionBlur = false;
	private float motionBlurAmount = 1.0f;
	
	private DropShadow glowEffect = new DropShadow();
	private MotionBlur motionEffect = new MotionBlur();
	
	private FrameProperties frameProps;
	private Settings settings;
	
	// Rendering globals
	private float cwidth = 0;
	private float cheight = 0;
	private float csize = 0;
	private float glowness = 0;
	private float currentScaleX = 1;
	private float currentScaleY = 1;
	private int dataCount = 32;
	private Section section;
	
	public Renderer() {
	}
	
	public void render(Project proj, Canvas targetCvs) {
		if(targetCvs.getWidth() != proj.getSettings().framewidth) targetCvs.setWidth(proj.getSettings().framewidth);
		if(targetCvs.getHeight() != proj.getSettings().frameheight) targetCvs.setHeight(proj.getSettings().frameheight);
		
		this.freqData = Urmusic.getFrequencyData();
		this.timeDomData = Urmusic.getTimeDomData();
		
		this.cvs = targetCvs;
		this.gtx = this.cvs.getGraphicsContext2D();
		
		this.frameProps = proj.getFrameProperties();
		this.settings = proj.getSettings();
		
		this.cwidth = (float) this.cvs.getWidth();
		this.cheight = (float) this.cvs.getHeight();
		
		this.csize = Math.min(this.cwidth, this.cheight);
		
		this.gtx.clearRect(0, 0, this.cwidth, this.cheight);
		
		this.gtx.setFill(this.settings.backgroundColor);
		this.gtx.fillRect(0, 0, this.cwidth, this.cheight);
		
		this.gtx.save();
		{
			this.gtx.translate(this.cwidth / 2, this.cheight / 2);
			this.gtx.scale(0.5, -0.5);
			
			this.renderGroup(this.settings.rootGroup);
		}
		this.gtx.restore();
	}
	
	private void renderGroup(SectionGroup group) {
		if(!group.visible) return;
		
		float mem_scaleX = this.currentScaleX;
		float mem_scaleY = this.currentScaleY;
		
		this.currentScaleX *= group.scaleX.getValueAsFloat();
		this.currentScaleY *= group.scaleY.getValueAsFloat();
		
		this.gtx.save();
		{
			this.gtx.translate(group.posX.getValueAsFloat() * this.csize, group.posY.getValueAsFloat() * this.csize);
			this.gtx.rotate(group.rotation.getValueAsFloat());
			this.gtx.scale(group.scaleX.getValueAsFloat(), group.scaleY.getValueAsFloat());
			
			for(SectionGroupElement elem : group.getUnmodifiableChildren()) {
				if(elem instanceof SectionGroup) {
					this.renderGroup((SectionGroup) elem);
				} else if(elem instanceof Section) {
					this.section = (Section) elem;
					
					if(!this.section.visible || this.section.target == null) continue;
					
					if(this.section.type != SectionType.IMAGE) {
						this.frameProps.imgw = this.frameProps.imgh = this.frameProps.imgr = 0;
					} else {
						ImageSection target = (ImageSection) this.section.target;
						
						if(target.image != null) {
							this.frameProps.imgw = (float) target.image.getWidth();
							this.frameProps.imgh = (float) target.image.getHeight();
							this.frameProps.imgr = this.frameProps.imgw / this.frameProps.imgh;
							
							this.section.refreshProperties(this.frameProps);
						}
					}
					
					this.glowness = this.section.glowness.getValueAsFloat();
					
					this.gtx.save();
					{
						this.gtx.translate(this.section.posX.getValueAsFloat() * this.csize, this.section.posY.getValueAsFloat() * this.csize);
						this.gtx.rotate(this.section.rotation.getValueAsFloat());
						this.gtx.scale(this.section.scaleX.getValueAsFloat(), this.section.scaleY.getValueAsFloat());
						
						this.gtx.setGlobalAlpha(this.section.opacity.getValueAsFloat());
						
						if(this.section.type == SectionType.FREQ) {
							this.renderFreqSection();
						} else if(this.section.type == SectionType.TIME_DOM) {
							this.renderTimeDomSection();
						} else if(this.section.type == SectionType.IMAGE) {
							this.renderImageSection();
						} else if(this.section.type == SectionType.TEXT) {
							this.renderTextSection();
						}
					}
					this.gtx.restore();
				}
			}
		}
		this.gtx.restore();
		
		this.currentScaleX = mem_scaleX;
		this.currentScaleY = mem_scaleY;
		
		// Set the temp section slot to null, this way it can be garbage collected
		this.section = null;
	}
	
	private float freqValue(float nind, FreqSection section) {
		float minDec = section.minDecibels.getValueAsFloat();
		float maxDec = section.maxDecibels.getValueAsFloat();
		
		// @format:off
		float val = Math.max(
			Utils.getValue(
				this.freqData,
				Utils.lerp(section.freqStart.getValueAsFloat(), section.freqEnd.getValueAsFloat(), nind) * this.freqData.length,
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
			
			x = Utils.lerp(x * this.csize, xp * this.csize, polar);
			y = Utils.lerp(y * this.csize, yp * this.csize, polar);
		} else {
			x *= this.csize;
			y *= this.csize;
		}
		
		this.sectionPerProps[0] = x;
		this.sectionPerProps[1] = y;
		
		return this.sectionPerProps;
	}
	
	private float[] getProps(FreqSection section, float per) {
		float height = (float) (Math.pow(this.freqValue(per, section), section.exponent.getValueAsFloat()) * section.height.getValueAsFloat());
		
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
			
			x = Utils.lerp(x * this.csize, xp * this.csize, polar);
			y = Utils.lerp(y * this.csize, yp * this.csize, polar);
			ex = Utils.lerp(ex * this.csize, exp * this.csize, polar);
			ey = Utils.lerp(ey * this.csize, eyp * this.csize, polar);
		} else {
			x *= this.csize;
			y *= this.csize;
			ex *= this.csize;
			ey *= this.csize;
		}
		
		this.sectionPerProps[0] = x;
		this.sectionPerProps[1] = y;
		this.sectionPerProps[2] = ex;
		this.sectionPerProps[3] = ey;
		
		return this.sectionPerProps;
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
			
			x = Utils.lerp(x * this.csize, xp * this.csize, polar);
			y = Utils.lerp(y * this.csize, yp * this.csize, polar);
		} else {
			x *= this.csize;
			y *= this.csize;
		}
		
		this.sectionPerProps[0] = x;
		this.sectionPerProps[1] = y;
		
		return this.sectionPerProps;
	}
	
	private float[] getTimeProps(TimeDomSection section, float per) {
		float height = Utils.getValue(this.timeDomData, per * this.timeDomData.length, section.quadratic, 0);
		
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
			
			x = Utils.lerp(x * this.csize, xp * this.csize, polar);
			y = Utils.lerp(y * this.csize, yp * this.csize, polar);
			ex = Utils.lerp(ex * this.csize, exp * this.csize, polar);
			ey = Utils.lerp(ey * this.csize, eyp * this.csize, polar);
		} else {
			x *= this.csize;
			y *= this.csize;
			ex *= this.csize;
			ey *= this.csize;
		}
		
		this.sectionPerProps[0] = x;
		this.sectionPerProps[1] = y;
		this.sectionPerProps[2] = ex;
		this.sectionPerProps[3] = ey;
		
		return this.sectionPerProps;
	}
	
	private Map<Section, float[]> lastPositions = new HashMap<Section, float[]>();
	
	private void setupEffect() {
		Effect finalEffect = null;
		
		if(this.glowness > 0.0f) {
			this.glowEffect.setColor(this.section.color);
			this.glowEffect.setWidth(this.glowness * this.currentScaleX);
			this.glowEffect.setHeight(this.glowness * this.currentScaleY);
			
			finalEffect = this.glowEffect;
		}
		
		if(this.motionBlur) {
			float posX = 0; // (this.section.posX.getValueAsFloat() + this.settings.globalOffsetX.getValueAsFloat()) * this.csize * this.glblscl;
			float posY = 0; // (this.section.posY.getValueAsFloat() + this.settings.globalOffsetY.getValueAsFloat()) * this.csize * this.glblscl;
			
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
		
		this.gtx.setEffect(finalEffect);
	}
	
	private void renderFreqSection() {
		FreqSection sectarg = (FreqSection) this.section.target;
		
		this.dataCount = sectarg.dataCount.getValueAsInt();
		DrawMode mode = sectarg.mode;
		
		this.gtx.setStroke(this.section.color);
		this.gtx.setFill(this.section.color);
		this.gtx.setLineWidth((sectarg.lineWidth.getValueAsFloat() / 100f) * this.csize);
		
		this.setupEffect();
		
		this.gtx.setLineCap(sectarg.lineCap);
		
		this.gtx.beginPath();
		for(int i = 0; i < this.dataCount; i++) {
			if(!sectarg.drawLast && i == this.dataCount - 1) {
				break;
			}
			
			float per = i / (this.dataCount - 1.0f);
			
			float[] p = this.getProps(sectarg, per);
			
			if(mode == DrawMode.LINES || (i == 0 && sectarg.clampShapeToZero)) {
				this.gtx.moveTo(p[0], p[1]);
			}
			
			this.gtx.lineTo(p[2], p[3]);
			
			if(i == this.dataCount - 1 && sectarg.clampShapeToZero) {
				this.gtx.lineTo(p[0], p[1]);
			}
		}
		
		if(sectarg.closeShape && mode != DrawMode.FILL) {
			this.gtx.closePath();
		}
		
		if(mode == DrawMode.FILL) {
			if(sectarg.smartFill) {
				for(float i = this.dataCount - 1; i >= 0; i--) {
					if(!sectarg.drawLast && i == this.dataCount - 1) {
						continue;
					}
					
					float per = i / (this.dataCount - 1);
					float[] fp = this.getFootProps(sectarg, per);
					
					this.gtx.lineTo(fp[0], fp[1]);
				}
			}
			
			this.gtx.fill();
		} else {
			this.gtx.moveTo(0, 0);
			this.gtx.stroke();
		}
	}
	
	private void renderTimeDomSection() {
		TimeDomSection sectarg = (TimeDomSection) this.section.target;
		
		this.dataCount = sectarg.dataCount.getValueAsInt();
		DrawMode mode = sectarg.mode;
		
		this.gtx.setStroke(this.section.color);
		this.gtx.setFill(this.section.color);
		this.gtx.setLineWidth(sectarg.lineWidth.getValueAsFloat() / 100f * this.csize);
		
		this.setupEffect();
		
		this.gtx.setLineCap(sectarg.lineCap);
		this.gtx.setLineJoin(sectarg.lineJoin);
		
		this.gtx.beginPath();
		for(int i = 0; i < this.dataCount; i++) {
			if(!sectarg.drawLast && i == this.dataCount - 1) {
				break;
			}
			
			float per = i / (this.dataCount - 1.0f);
			float[] p = this.getTimeProps(sectarg, per);
			
			if(mode == DrawMode.LINES || (i == 0 && sectarg.clampShapeToZero)) {
				this.gtx.moveTo(p[0], p[1]);
			}
			
			this.gtx.lineTo(p[2], p[3]);
			
			if(i == this.dataCount - 1 && sectarg.clampShapeToZero) {
				this.gtx.lineTo(p[0], p[1]);
			}
		}
		
		if(sectarg.closeShape && mode != DrawMode.FILL) {
			this.gtx.closePath();
		}
		
		if(mode == DrawMode.FILL) {
			if(sectarg.smartFill) {
				for(int i = this.dataCount - 1; i >= 0; i--) {
					if(!sectarg.drawLast && i == this.dataCount - 1) {
						continue;
					}
					
					float per = i / (this.dataCount - 1.0f);
					float[] fp = this.getTimeFootProps(sectarg, per);
					
					this.gtx.lineTo(fp[0], fp[1]);
				}
			}
			
			this.gtx.fill();
		} else {
			this.gtx.moveTo(0, 0);
			this.gtx.stroke();
		}
	}
	
	private void renderImageSection() {
		ImageSection sectarg = (ImageSection) this.section.target;
		
		this.gtx.setLineWidth((sectarg.borderSize.getValueAsFloat() / 100f) * this.csize);
		
		this.gtx.setFill(this.section.color);
		this.gtx.setStroke(sectarg.borderColor);
		
		this.setupEffect();
		
		this.gtx.scale(1, -1);
		
		float imgBorderRad = sectarg.imageBorderRadius.getValueAsFloat() * this.csize;
		if(imgBorderRad != 0.0f) {
			Utils.roundRect(this.gtx, -this.csize / 2f, -this.csize / 2f, this.csize, this.csize, imgBorderRad);
			this.gtx.clip();
		} else {
			this.gtx.beginPath();
			this.gtx.rect(-this.csize / 2f, -this.csize / 2f, this.csize, this.csize);
		}
		
		if(sectarg.opaque) this.gtx.fill();
		if(sectarg.borderVisible) this.gtx.stroke();
		
		if(sectarg.image != null) {
			this.gtx.drawImage(sectarg.image, -this.csize / 2f, -this.csize / 2f, this.csize, this.csize);
		}
	}
	
	private void renderTextSection() {
		TextSection sectarg = (TextSection) this.section.target;
		
		Object otxt = sectarg.text.getValue();
		String txt;
		
		if(otxt == null || "".equals(txt = otxt.toString())) return;
		
		this.setupEffect();
		
		this.gtx.setFill(this.section.color);
		this.gtx.setFont(Font.font(sectarg.fontFamily, FontWeight.findByName(sectarg.fontStyle), FontPosture.findByName(sectarg.fontStyle), sectarg.fontSize.getValueAsFloat() * this.csize));
		this.gtx.setTextAlign(sectarg.textAlign);
		this.gtx.setTextBaseline(sectarg.textBaseline);
		
		this.gtx.scale(1, -1);
		
		this.gtx.fillText(txt, 0, 0);
	}
	
	public Canvas getCanvas() {
		return this.cvs;
	}
	
	public boolean isMotionBlur() {
		return this.motionBlur;
	}
	
	public void setMotionBlur(boolean motionBlur) {
		this.motionBlur = motionBlur;
	}
	
	public float getMotionBlurAmount() {
		return this.motionBlurAmount;
	}
	
	public void setMotionBlurAmount(float motionBlurAmount) {
		this.motionBlurAmount = motionBlurAmount;
	}
}
