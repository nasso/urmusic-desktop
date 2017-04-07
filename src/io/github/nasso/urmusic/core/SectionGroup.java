package io.github.nasso.urmusic.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionGroup extends SectionGroupElement {
	private List<SectionGroupElement> sectionList = new ArrayList<SectionGroupElement>();
	
	public SectionGroup(PrimitiveProperties props) {
		super(props);
	}
	
	public SectionGroup(SectionGroup other) {
		super(other);
		
		for(SectionGroupElement e : other.sectionList) {
			if(e instanceof SectionGroup) {
				this.addChild(new SectionGroup((SectionGroup) e));
			} else if(e instanceof Section) {
				this.addChild(new Section((Section) e));
			}
		}
	}
	
	public SectionGroup() {
		
	}
	
	public SectionGroupElement addChild(SectionGroupElement e) {
		if(e != this && !(e instanceof SectionGroup && ((SectionGroup) e).contains(this))) this.sectionList.add(e);
		return e;
	}
	
	public SectionGroupElement addChild(int index, SectionGroupElement e) {
		if(e != this && !(e instanceof SectionGroup && ((SectionGroup) e).contains(this))) this.sectionList.add(index, e);
		return e;
	}
	
	public List<SectionGroupElement> getUnmodifiableChildren() {
		return Collections.unmodifiableList(this.sectionList);
	}
	
	public boolean contains(SectionGroupElement el) {
		if(this.sectionList.contains(el)) return true;
		
		for(SectionGroupElement e : this.sectionList) {
			if(e instanceof SectionGroup && ((SectionGroup) e).contains(el)) return true;
		}
		
		return false;
	}
	
	public void dispose() {
		super.dispose();
		
		for(SectionGroupElement e : this.sectionList) {
			e.dispose();
		}
	}
	
	public void clearChildren() {
		this.sectionList.clear();
	}
	
	public SectionGroupElement removeChild(SectionGroupElement e) {
		this.sectionList.remove(e);
		return e;
	}
}
