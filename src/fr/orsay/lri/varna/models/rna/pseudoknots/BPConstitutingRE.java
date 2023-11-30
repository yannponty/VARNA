package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class BPConstitutingRE extends Element{
	private int inf;
	private int sup;
	private int strand_inf;
	private int strand_sup;
	private int color;
	private double distance_above_sup;
	private double distance_beneath_sup;
	
	public BPConstitutingRE(int inf, int sup, int color) {
		super();
		this.inf = inf;
		this.sup = sup;
		this.color = color;
		this.distance_above = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath = 0.0;
		this.distance_above_sup = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath_sup = 0.0;
	}
	
	public BPConstitutingRE(int inf, int sup, int strand_inf, int strand_sup){
		super();
		this.inf = inf;
		this.sup = sup;
		this.strand_inf = strand_inf;
		this.strand_sup = strand_sup;
		this.distance_above = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath = 0.0;
		this.distance_above_sup = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath_sup = 0.0;
	}

	public int getInf() {
		return inf;
	}

	public void setInf(int inf) {
		this.inf = inf;
	}

	public int getSup() {
		return sup;
	}

	public void setSup(int sup) {
		this.sup = sup;
	}
	
	public int getElementInf(){
		return this.getInf();
	}
	
	public int getElementSup(){
		return this.getSup();
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getStrand_inf() {
		return strand_inf;
	}

	public void setStrand_inf(int strand_inf) {
		this.strand_inf = strand_inf;
	}

	public int getStrand_sup() {
		return strand_sup;
	}

	public void setStrand_sup(int strand_sup) {
		this.strand_sup = strand_sup;
	}

	public double getDistance_above_sup() {
		return distance_above_sup;
	}

	public void setDistance_above_sup(double distance_above_sup) {
		this.distance_above_sup = distance_above_sup;
	}

	public double getDistance_beneath_sup() {
		return distance_beneath_sup;
	}

	public void setDistance_beneath_sup(double distance_beneath_sup) {
		this.distance_beneath_sup = distance_beneath_sup;
	}
	
	public int getBoundOnStrand(int n){
		if(n == this.strand_inf){
			return this.inf;
		}
		else if(n == this.strand_sup){
			return this.sup;
		}
		else return -1;
	}
	
	public Point2D.Double getCoordBoundOnStrand(int n){
		if(n == this.strand_inf){
			return this.draw_inf;
		}
		else if(n == this.strand_sup){
			return this.draw_sup;
		}
		else return null;
	}

	/*public void setDrawX() {
		double BPdistance = 100.0;
		this.setDraw_infX((this.strand_inf-1)*BPdistance);
		this.setDraw_supX((this.strand_sup-1)*BPdistance);
	}*/

	public void updateDistanceAbove(double distance) {
		this.distance_above = Math.max(Element.SPACE_BETWEEN_BASES, distance);
	}
	
	public void updateDistanceAboveSup(double distance) {
		this.distance_above_sup = Math.max(Element.SPACE_BETWEEN_BASES, distance);
	}
	
	public boolean isUnpaired(){
		if(this.inf == this.sup){
			return true;
		}
		else return false;
	}

}
