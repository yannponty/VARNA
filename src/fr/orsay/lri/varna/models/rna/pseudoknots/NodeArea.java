package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

public class NodeArea extends Node{
	private GeneralPath bounding_box;
	private int position;

	public NodeArea(GeneralPath bounding_box, int position) {
		super();
		this.bounding_box = bounding_box;
		this.position = position;
	}

	public GeneralPath getBounding_box() {
		return bounding_box;
	}

	public void setBounding_box(GeneralPath bounding_box) {
		this.bounding_box = bounding_box;
	}

	public int getPosition() {
		return position;
	}

	public void setFirst_base(int first_base) {
		this.position = first_base;
	}

	public boolean isNeighbourWith(Node node) {
		NodeArea na = (NodeArea) node;
		Area a1 = new Area(this.bounding_box);
		Area a2 = new Area(na.getBounding_box());
		a1.intersect(a2);
		if(!a1.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean isGreaterThan(Node node) {
		NodeArea na = (NodeArea) node;
		if(this.position > na.getPosition()) {
			return true;
		}
		return false;
	}

}
