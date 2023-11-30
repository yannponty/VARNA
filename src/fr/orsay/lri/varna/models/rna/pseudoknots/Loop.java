package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class Loop {
	public static double TOL = 0.000000001;
	
	private ArrayList<Couple<Boolean,Double>> distances;
	private Point2D.Double center;
	private ArrayList<Couple<Integer,Point2D.Double>> points;
	private ArrayList<Couple<Integer,Double>> angles;
	private Point2D.Double first_point;
	private ArrayList<Integer> indexes;
	private double radius;
	
	public Loop(){
		this.distances = new ArrayList<Couple<Boolean,Double>>();
		this.center = new Point2D.Double();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.angles = new ArrayList<Couple<Integer,Double>>();
		this.first_point = new Point2D.Double();
		this.indexes = new ArrayList<Integer>();
		this.radius = 0;
	}
	
	public Loop(ArrayList<Couple<Boolean,Double>> distances, Point2D.Double first_point, ArrayList<Integer> indexes){
		this.distances = distances;
		this.center = new Point2D.Double();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.angles = new ArrayList<Couple<Integer,Double>>();
		this.first_point = first_point;
		this.indexes = indexes;
		this.radius = 0;
		for(int i = 0; i < this.distances.size(); i ++){
			this.points.add(null);
			this.angles.add(null);
		}
	}
	
	public ArrayList<Couple<Boolean,Double>> getDistances(){
		return distances;
	}

	public void setDistances(ArrayList<Couple<Boolean,Double>> distances) {
		this.distances = distances;
	}

	public Point2D.Double getCenter() {
		return center;
	}

	public void setCenter(Point2D.Double center) {
		this.center = center;
	}

	public ArrayList<Couple<Integer, Point2D.Double>> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<Couple<Integer, Point2D.Double>> points) {
		this.points = points;
	}

	public ArrayList<Couple<Integer, Double>> getAngles() {
		return angles;
	}

	public void setAngles(ArrayList<Couple<Integer, Double>> angles) {
		this.angles = angles;
	}

	public Point2D.Double getFirst_point() {
		return first_point;
	}

	public void setFirst_point(Point2D.Double first_point) {
		this.first_point = first_point;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public ArrayList<Couple<Integer,Point2D.Double>> getPointsCoords(){
		double distances_sum = this.getDistancesSum();
		double minimum_radius = distances_sum/(2.0*Math.PI);
		double absolute_minimum_radius = this.getDistanceMax()/2.0;
		this.radius = minimum_radius;
		int iteration = 0;
		ArrayList<Double> list_angles = this.setRadiusAndAngularIncrements(iteration, absolute_minimum_radius, minimum_radius, Double.MAX_VALUE);
		double angular_increment_base = (2*Math.PI - this.getAnglesSum(list_angles))/(double)this.getNbUnpaired();
		double angular_increment_current = 0;
		System.out.println("--- "+angular_increment_base+" "+this.getNbUnpaired()+" "+this.getAnglesSum(list_angles));
		for(int i = 0; i < this.distances.size(); i++){
			double angle = list_angles.get(i);
			if(this.isDistanceUnpaired(i)){
				angular_increment_current += angular_increment_base;
			}
			list_angles.set(i, angle + angular_increment_current);
		}
		System.out.println(this.getAnglesSum(list_angles));
		this.determineCenter();
		for(int i = 0; i < list_angles.size(); i++){
			Couple<Integer,Double> c1 = new Couple<Integer,Double>(this.indexes.get(i), list_angles.get(i));
			this.angles.set(i, c1);
			Point2D.Double p = new Point2D.Double(this.first_point.getX(), this.first_point.getY());
			double angle = -(list_angles.get(i) - list_angles.get(0));
			p = this.rotatePoint(p, angle, this.center);
			Couple<Integer,Point2D.Double> c2 = new Couple<Integer,Point2D.Double>(this.indexes.get(i), p);
			this.points.set(i, c2);
		}
		return this.points;
	}
	
	public double getDistancesSum(){
		double sum = 0;
		for(Couple<Boolean,Double> c : this.distances){
			sum += c.second;
		}
		return sum;
	}
	
	public double getDistanceMax(){
		double max = 0;
		for(Couple<Boolean,Double> c : this.distances){
			max = Math.max(max, c.second);
		}
		return max;
	}
	
	private Point2D.Double rotatePoint(Point2D.Double point, double angle, Point2D.Double c){
		Point2D.Double p = new Point2D.Double();
		double x = 0;
		double y = 0;
		x = Math.cos(angle)*(point.getX() - c.getX()) - Math.sin(angle)*(point.getY() - c.getY()) + c.getX();
		y = Math.sin(angle)*(point.getX() - c.getX()) + Math.cos(angle)*(point.getY() - c.getY()) + c.getY();
		p.setLocation(x, y);
		return p;
	}

	private void determineCenter(){
		double x = 0;
		double y = 0;
		double triangle_height = 0;
		x = this.first_point.getX() + this.distances.get(0).second/2.0;
		triangle_height = Math.sqrt(Math.pow(this.radius, 2) - Math.pow((this.distances.get(0).second/2.0),2));
		y = this.first_point.getY() + triangle_height;
		this.center.setLocation(x, y);
	}

	public ArrayList<Double> setRadiusAndAngularIncrements(int iteration, double absolute_minimum_radius, double minimum_radius, double maximum_radius){
		/*if(iteration > 100){
			System.out.println("PROBLEM");
			return null;
		}*/
		ArrayList<Double> list_angles = this.getAngularIncrements();
		double angles_sum = this.getAnglesSum(list_angles);
		if(angles_sum >= 2*Math.PI*(1-TOL) && angles_sum <= 2*Math.PI*(1+TOL)){
			return list_angles;
		}
		else if(angles_sum < 2*Math.PI*(1-TOL)){
			if(this.radius <= absolute_minimum_radius){
				return list_angles;
			}
			else {
				maximum_radius = this.radius;
				this.radius = Math.max(absolute_minimum_radius, (this.radius+minimum_radius)/2.0);
			}
		}
		else {
			minimum_radius = this.radius;
			if(2*this.radius >= maximum_radius){
				this.radius = (maximum_radius+this.radius)/2.0;
			}
			else {
				this.radius = 2*this.radius;
			}			
		}
		return this.setRadiusAndAngularIncrements(iteration+1, absolute_minimum_radius, minimum_radius, maximum_radius);
	}
	
	public ArrayList<Double> getAngularIncrements(){
		ArrayList<Double> list_angles = new ArrayList<Double>(this.distances.size());
		double last_angle = 0;
		for(int i = 0; i < this.distances.size(); i++){
			double cur_angle = Math.asin((this.distances.get(i).second/2.0)/this.radius);
			if(i == 0){
				last_angle += cur_angle;
				list_angles.add(last_angle);
			}
			else {
				last_angle += cur_angle*2.0;
				list_angles.add(last_angle);
			}
		}
		return list_angles;
	}
	
	public double getAnglesSum(ArrayList<Double> angles){
		double sum = angles.get(0) + angles.get(angles.size()-1);
		return sum;
	}
	
	public int getNbUnpaired(){
		int nb = 0;
		for(int i = 0; i < this.distances.size(); i++){
			if(this.isDistanceUnpaired(i)){
				nb++;
			}
		}
		return nb;
	}
	
	public boolean isDistanceUnpaired(int i){
		Couple<Boolean,Double> c = this.distances.get(i);
		return !c.first;
	}
}
