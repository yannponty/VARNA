package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public abstract class Element {
	protected static final double SPACE_BETWEEN_BASES = 50.0;
	protected static final double BASE_PAIR_DISTANCE = 65.0;
	
	protected ArrayList<Element> draw_children;
	protected ArrayList<Element> draw_fathers;
	protected Point2D.Double draw_inf;
	protected Point2D.Double draw_sup;
	protected double distance_above;
	protected double distance_beneath;
	
	public Element(){
		this.draw_children = new ArrayList<Element>();
		this.draw_fathers = new ArrayList<Element>();
		this.draw_inf = new Point2D.Double(-1, -1);
		this.draw_sup = new Point2D.Double(-1, -1);
	}
	
	public ArrayList<Element> getDraw_children() {
		return draw_children;
	}

	public void setDraw_children(ArrayList<Element> draw_children) {
		this.draw_children = draw_children;
	}

	public ArrayList<Element> getDraw_fathers() {
		return draw_fathers;
	}

	public void setDraw_fathers(ArrayList<Element> draw_fathers) {
		this.draw_fathers = draw_fathers;
	}
	
	public Point2D.Double getDraw_inf() {
		return draw_inf;
	}

	public void setDraw_inf(Point2D.Double draw_inf) {
		this.draw_inf = draw_inf;
	}
	
	public Point2D.Double getDraw_sup() {
		return draw_sup;
	}

	public void setDraw_sup(Point2D.Double draw_sup) {
		this.draw_sup = draw_sup;
	}
	
	public void setDraw_infX(Double x) {
		this.draw_inf.x = x;
	}
	
	public void setDraw_infY(Double y) {
		this.draw_inf.y = y;
	}
	
	public void setDraw_supX(Double x) {
		this.draw_sup.x = x;
	}
	
	public void setDraw_supY(Double y) {
		this.draw_sup.y = y;
	}
	
	public double getDistance_above() {
		return distance_above;
	}

	public void setDistance_above(double distance_above) {
		this.distance_above = distance_above;
	}

	public double getDistance_beneath() {
		return distance_beneath;
	}

	public void setDistance_beneath(double distance_beneath) {
		this.distance_beneath = distance_beneath;
	}

	public abstract int getBoundOnStrand(int strand_num);
	public abstract int getElementInf();
	public abstract int getElementSup();
}
