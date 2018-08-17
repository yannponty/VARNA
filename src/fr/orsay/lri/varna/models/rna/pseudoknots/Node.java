package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.orsay.lri.varna.models.rna.ModeleBP;

public class Node extends Element{
	private int inf;
	private int sup;
	private int strand_inf;
	private int strand_sup;
	private ArrayList<Node> children;
	private int color;
	private boolean visited;
	//private ArrayList<Node> draw_fathers;
	//private ArrayList<Node> draw_children;
	//private Point2D.Double draw_inf;
	//private Point2D.Double draw_sup;
	private double distance_above_sup;
	private double distance_beneath_sup;
	
	public Node(int inf, int sup){
		super();
		this.inf = inf;
		this.sup = sup;
		this.children = new ArrayList<Node>();
		this.color = -1;
		this.visited = false;
		//this.draw_fathers = new ArrayList<Node>();
		//this.draw_children = new ArrayList<Node>();
		//this.draw_inf = new Point2D.Double();
		//this.draw_sup = new Point2D.Double();
		this.distance_above = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath = 0.0;
		this.distance_above_sup = Element.SPACE_BETWEEN_BASES;
		this.distance_beneath_sup = 0.0;
	}
	
	public Node(int inf, int sup, int strand_inf, int strand_sup){
		super();
		this.inf = inf;
		this.sup = sup;
		this.strand_inf = strand_inf;
		this.strand_sup = strand_sup;
		this.draw_inf = new Point2D.Double();
		this.draw_sup = new Point2D.Double();
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

	public ArrayList<Node> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
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

	/*public ArrayList<Node> getDraw_fathers() {
		return draw_fathers;
	}

	public void setDraw_fathers(ArrayList<Node> draw_father) {
		this.draw_fathers = draw_father;
	}

	public ArrayList<Node> getDraw_children() {
		return draw_children;
	}

	public void setDraw_children(ArrayList<Node> draw_children) {
		this.draw_children = draw_children;
	}*/
	
	/*public Point2D.Double getDraw_inf() {
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
	}*/

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

	public boolean intersection(Node node){
		if(containsInteger(node.getInf()) && !containsInteger(node.getSup())){
			return true;
		}
		else if(node.containsInteger(this.inf) && !node.containsInteger(this.sup)){
			return true;
		}
		else return false;
	}
	
	public boolean containsChild(Node node){
		for(Node n : this.children){
			if(n.equals(node)) return true;
		}
		return false;
	}
	
	private boolean containsInteger(int integer){
		if((integer >= this.inf) && (integer <= this.sup)){
			return true;
		}
		return false;
	}
	
	public void addChild(Node node){
		this.children.add(node);
	}

	public int determineColor() {
		boolean gotFather0 = false;
		boolean gotFather1 = false;
		boolean gotFather2 = false;
		int color = -1;
		for(Node n : children){
			if(n.getColor() == 0){
				gotFather0 = true;
			}
			else if(n.getColor() == 1){
				gotFather1 = true;
			}
			else if(n.getColor() == 2){
				gotFather2 = true;
			}
		}
		if(gotFather0 && gotFather1){
			color = 2;
		}
		else if(gotFather0){
			color = 1;
		}
		else if(gotFather1){
			color = 0;
		}
		else if(gotFather2){
			color = 0;
		}
		return color;
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

	public void setDrawX() {
		double BPdistance = 100.0;
		this.setDraw_infX((this.strand_inf-1)*BPdistance);
		this.setDraw_supX((this.strand_sup-1)*BPdistance);
	}

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
