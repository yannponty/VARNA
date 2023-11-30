package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;

public class ConnectedComponentArea extends ConnectedComponent{
	private int position;
	private ArrayList<GeneralPath> bounding_boxes;

	public ConnectedComponentArea() {
		super();
		this.position = Integer.MAX_VALUE;
		this.bounding_boxes = new ArrayList<GeneralPath>();
	}
	
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	public ArrayList<GeneralPath> getBounding_boxes() {
		return bounding_boxes;
	}

	public void setBounding_boxes(ArrayList<GeneralPath> bounding_boxes) {
		this.bounding_boxes = bounding_boxes;
	}

	public void addNode(Node n) {
		NodeArea na = (NodeArea) n;
		this.nodes.add(na);
		if(na.getPosition() < this.position) {
			this.position = na.getPosition();
		}
	}

	public boolean isGreaterThan(ConnectedComponent cc) {
		ConnectedComponentArea cca = (ConnectedComponentArea) cc;
		if(this.getPosition() > cca.getPosition()) {
			return true;
		}
		return false;
	}

	public void removeNode(Node node) {
		// TODO Auto-generated method stub
		
	}

	public boolean isIncluded(ConnectedComponent cc) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void retrieveBounding_boxes() {
		System.out.println("AZAZAZ "+this.nodes.size());
		for(Node n : this.nodes) {
			System.out.println(this.bounding_boxes.size());
			NodeArea na = (NodeArea) n;
			this.bounding_boxes.add(na.getBounding_box());
		}
	}

}
