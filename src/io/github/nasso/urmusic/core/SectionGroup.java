package io.github.nasso.urmusic.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SectionGroup extends SectionGroupElement {
	private List<SectionGroupElement> sectionList = new ArrayList<SectionGroupElement>();
	
	public SectionGroup(PrimitiveProperties props) {
		super(props);
	}
	
	public SectionGroup() {
		
	}
	
	public SectionGroupElement addChildren(SectionGroupElement e) {
		if(e != this && !(e instanceof SectionGroup && ((SectionGroup) e).contains(this))) this.sectionList.add(e);
		return e;
	}
	
	public List<SectionGroupElement> getUnmodifiableChildren() {
		return Collections.unmodifiableList(this.sectionList);
	}
	
	public boolean contains(SectionGroupElement el) {
		if(this.sectionList.contains(el)) return true;
		
		for(SectionGroupElement e : sectionList) {
			if(e instanceof SectionGroup && ((SectionGroup) e).contains(el)) return true;
		}
		
		return false;
	}
	
	public void dispose() {
		super.dispose();
		
		for(SectionGroupElement e : sectionList) {
			e.dispose();
		}
	}
	
	public void clearChildren() {
		this.sectionList.clear();
	}
	
	public SectionGroupElement removeChildren(SectionGroupElement e) {
		this.sectionList.remove(e);
		return e;
	}
	
	private void generateDescription(StringBuilder builder, int tabs) {
		builder.append("+ " + this.getClass().getSimpleName() + ": \"" + this.name + "\"\n");
		
		String prefix = new String(new char[tabs + 1]).replace("\0", "|\t"); // prefix
																				// =
																				// rep("|\t",
																				// tabs
																				// +
																				// 1)
		for(SectionGroupElement elem : this.sectionList) {
			builder.append(prefix + "|\n" + prefix);
			
			if(elem instanceof SectionGroup) {
				((SectionGroup) elem).generateDescription(builder, tabs + 1);
			} else {
				builder.append("+ " + elem.getClass().getSimpleName() + ": \"" + elem.name + "\"\n");
			}
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		this.generateDescription(builder, 0);
		builder.append("o"); // Final "o" (makes it prettier :D)
		
		return builder.toString();
	}
}
