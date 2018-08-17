package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class BasePair extends StronglyConnectedComponent {
	private Loop loop;
	
	public BasePair(){
		super();
		this.nodes = new ArrayList<Node>();
		this.span_inf = -1;
		this.span_sup = -1;
		this.children = new ArrayList<StronglyConnectedComponent>();
		this.father = null;
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
		this.loop = new Loop();
	}
	
	public void assignCoords(){
		this.initializeListPoints();
		this.initializeListCenters();
		this.initializeNode();
		this.buildLoop();
		this.positionChildrenAroundLoop();	
		this.buildBoundingBox();
		this.assignCentersCoords();
	}
	
	private void initializeNode(){
		Point2D.Double p_inf = new Point2D.Double(0, 0);
		Point2D.Double p_sup = new Point2D.Double(Element.BASE_PAIR_DISTANCE, 0);
		this.nodes.get(0).setDraw_inf(p_inf);
		this.nodes.get(0).setDraw_sup(p_sup);
	}
	protected void buildLoop() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Couple<Boolean,Double>> distances = new ArrayList<Couple<Boolean,Double>>(this.span_sup - this.span_inf + 1);
		distances.add(new Couple<Boolean,Double>(true, Element.BASE_PAIR_DISTANCE));
		indexes.add(this.span_inf);
		if(this.span_sup - this.span_inf > 1){
			for(int i = this.span_inf+1; i <= this.span_sup; i++){
				indexes.add(i);
				StronglyConnectedComponent scc = getCorrespondingScc(i);
				if(scc == null){
					distances.add(new Couple<Boolean,Double>(false, Element.SPACE_BETWEEN_BASES));
				}
				else {
					distances.add(new Couple<Boolean,Double>(false, Element.SPACE_BETWEEN_BASES));
					distances.add(new Couple<Boolean,Double>(true, scc.getDistanceBetweenExtrema()));
					i = scc.getSpan_sup();
					indexes.add(i);
				}
			}
		}
		Point2D.Double first_point = this.getNodes().get(0).getDraw_inf();
		this.loop = new Loop(distances, first_point, indexes);
	}
	
	private void positionChildrenAroundLoop(){
		ArrayList<GeneralPath> bounding_box_children_positioned = new ArrayList<GeneralPath>();
		/*for(StronglyConnectedComponent s : this.children){
			debugShape.add(new GeneralPath(s.getBounding_box()));
		}*/
		ArrayList<Couple<Couple<Point2D.Double,Double>,Point2D.Double>> list_rot_trans = new ArrayList<Couple<Couple<Point2D.Double,Double>,Point2D.Double>>();
		for(int i = 0; i < this.children.size(); i++){
			bounding_box_children_positioned.add(null);
			list_rot_trans.add(null);
		}
		ArrayList<Couple<Integer,Point2D.Double>> loop_points = this.loop.getPointsCoords();
		for(Couple<Integer,Point2D.Double> c : loop_points){
			this.points.set(c.first - this.span_inf, c);
		}
		//double first_angle = this.loop.getAngles().get(0).second;
		for(int i = 0; i < this.children.size(); i ++){
			StronglyConnectedComponent scc = this.children.get(i);
			GeneralPath scc_gp = new GeneralPath(scc.getBounding_box());
			int j = 0;
			while(scc.getSpan_inf() != loop_points.get(j).first){
				j++;
			}
			AffineTransform at = new AffineTransform();
			double x = 0;
			double y = 0;
			double angle1 = this.loop.getAngles().get(j).second;
			double angle2 = this.loop.getAngles().get(j+1).second;
			double rotation_angle = Math.PI - (angle1 + angle2)/2.0;
			Couple<Point2D.Double, Point2D.Double> c = scc.getFirstAndLastPoints();
			Point2D.Double rotation_center = c.first;
			at.rotate(rotation_angle, rotation_center.getX(), rotation_center.getY());
			scc_gp.transform(at);
			at = new AffineTransform();
			Point2D.Double translation_vector = new Point2D.Double();
			x = loop_points.get(j).second.getX() - c.first.getX();
			y = loop_points.get(j).second.getY() - c.first.getY();
			translation_vector.setLocation(x, y);
			at.translate(translation_vector.getX(), translation_vector.getY());
			scc_gp.transform(at);
			Couple<Couple<Point2D.Double,Double>,Point2D.Double> rot_trans = new Couple<Couple<Point2D.Double,Double>,Point2D.Double>(
					new Couple<Point2D.Double,Double>(rotation_center,rotation_angle),translation_vector);
			list_rot_trans.set(i, rot_trans);
			
			Area scc_area = new Area(scc_gp);
			System.out.println("TEST "+scc.getSpan_inf()+" "+scc.getSpan_sup());
			for(int k = 0; k < i; k++){
				GeneralPath gp = new GeneralPath(bounding_box_children_positioned.get(k));
				Area a = new Area(gp);
				System.out.println("Bounding1 "+scc_area.getBounds2D().getX()+" "+scc_area.getBounds2D().getY()+" "+scc_area.getBounds2D().getWidth()+" "+scc_area.getBounds2D().getHeight());
				System.out.println("Bounding2 "+a.getBounds2D().getX()+" "+a.getBounds2D().getY()+" "+a.getBounds2D().getWidth()+" "+a.getBounds2D().getHeight());
				a.intersect(scc_area);
				if(!a.isEmpty()){
					System.out.println("PROBLEM");
					double distance_to_increase = this.loop.getDistances().get(j).second;
					Couple<Boolean,Double> c1 = new Couple<Boolean,Double>(false, distance_to_increase + 20.0);
					this.loop.getDistances().set(j, c1);
					this.positionChildrenAroundLoop();
					return;
				}
			}
			bounding_box_children_positioned.set(i, scc_gp);
		}
		for(GeneralPath gp : bounding_box_children_positioned){
			this.debugShape.add(new GeneralPath(gp));
		}
		for(int i = 0; i < this.children.size(); i++){
			StronglyConnectedComponent scc = this.children.get(i);
			Couple<Couple<Point2D.Double,Double>,Point2D.Double> rot_trans = list_rot_trans.get(i);
			scc.rotate(rot_trans.first.first, rot_trans.first.second);
			scc.translate(rot_trans.second);
			for(int j = 1; j < scc.getPoints().size()-1; j++){
				int index = scc.span_inf-this.span_inf + j;
				this.points.set(index,scc.getPoints().get(j));
				this.centers.set(index, scc.getCenters().get(j));
			}
		}

		/*if(scc.isPseudoKnot()){
			double x = 0;
			double y = 0;
			Couple<Point2D.Double, Point2D.Double> c = scc.getFirstAndLastPoints();
			Point2D.Double x_axis_vector = new Point2D.Double(0, 1);
			Point2D.Double scc_vector = new Point2D.Double();
			x = loop_points.get(i+1).second.getX() - loop_points.get(i).second.getX();
			y = loop_points.get(i+1).second.getY() - loop_points.get(i).second.getY();
			scc_vector.setLocation(x, y);
			double scalar_product = x_axis_vector.getX()*scc_vector.getY() + x_axis_vector.getY()*scc_vector.getX();
			Point2D.Double rotation_center = c.first;
			double rotation_angle = - Math.acos((scalar_product)/scc.getDistanceBetweenExtrema());
			scc.rotate(rotation_center, rotation_angle);
			Point2D.Double translation_vector = new Point2D.Double();
			x = loop_points.get(i).second.getX() - c.first.getX();
			y = loop_points.get(i).second.getY() - c.first.getY();
			translation_vector.setLocation(x, y);
			scc.translate(translation_vector);							
		}
		else {
			double x = 0;
			double y = 0;
			Point2D.Double scc_middle = new Point2D.Double();
			x = (loop_points.get(i).second.getX() + loop_points.get(i+1).second.getX())/2.0;
			y = (loop_points.get(i).second.getY() + loop_points.get(i+1).second.getY())/2.0;
			scc_middle.setLocation(x, y);
			double rotation_angle = this.loop.getAngles().get(i+1).second - first_angle;
			scc.rotate(this.loop.getCenter(), -rotation_angle);
			scc.rotate(scc_middle, Math.PI);
		}*/
	}
	
	private void assignCentersCoords(){
		for(int i = this.span_inf; i <= this.span_sup; i++) {
			StronglyConnectedComponent scc = this.getCorrespondingScc(i);
			Point2D.Double loop_center = new Point2D.Double(this.loop.getCenter().getX(),this.loop.getCenter().getY());
			Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(i,loop_center);
			this.centers.set(i - this.span_inf, c);
			if(scc != null) {
				i = scc.span_sup -1;
			}
		}
	}
}
