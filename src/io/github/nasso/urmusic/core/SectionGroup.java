package io.github.nasso.urmusic.core;

import java.util.HashSet;
import java.util.Set;

public class SectionGroup extends SectionGroupElement {
	private Set<SectionGroupElement> sectionList = new HashSet<SectionGroupElement>();
	
	public SectionGroupElement addChildren(SectionGroupElement e) {
		if(e != this && !(e instanceof SectionGroup && ((SectionGroup) e).contains(this))) this.sectionList.add(e);
		return e;
	}
	
	public boolean contains(SectionGroupElement el) {
		if(this.sectionList.contains(el)) return true;
		
		for(SectionGroupElement e : sectionList) {
			if(e instanceof SectionGroup && ((SectionGroup) e).contains(el)) return true;
		}
		
		return false;
	}
	
	public void clearChildren() {
		this.sectionList.clear();
	}
	
	public SectionGroupElement removeChildren(SectionGroupElement e) {
		this.sectionList.remove(e);
		return e;
	}
}
