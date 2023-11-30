package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class BasePair extends RecursiveElement {
	private Loop loop;
	
	public BasePair(){
		super();
		this.bpcre = new ArrayList<BPConstitutingRE>();
		this.span_inf = -1;
		this.span_sup = -1;
		this.children = new ArrayList<RecursiveElement>();
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
		this.loop = new Loop();
	}
	
	public BasePair(int span_inf, int span_sup, ArrayList<BPConstitutingRE> bpcres) {
		super();
		this.bpcre = new ArrayList<BPConstitutingRE>(bpcres);
		this.span_inf = span_inf;
		this.span_sup = span_sup;
		this.children = new ArrayList<RecursiveElement>();
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();		
	}
	
	public void assignCoords(){
		this.initializeListPoints();
		this.initializeListCenters();
		this.initializeBPConstitutingRE();
		if(this.span_sup - this.span_inf > 1) {
			this.buildLoop();
			this.positionChildrenAroundLoop();
			this.assignCentersCoords();	
		}
		this.buildBoundingBox();
	}
	
	private void initializeBPConstitutingRE(){
		Point2D.Double p_inf = new Point2D.Double(0, 0);
		Point2D.Double p_sup = new Point2D.Double(Element.BASE_PAIR_DISTANCE, 0);
		this.bpcre.get(0).setDraw_inf(p_inf);
		this.bpcre.get(0).setDraw_sup(p_sup);
		if(this.span_sup - this.span_inf == 1) {
			Point2D.Double c_inf = new Point2D.Double(5.0, 0);
			Point2D.Double c_sup = new Point2D.Double(Element.BASE_PAIR_DISTANCE - 5.0, 0);
			Couple<Integer,Point2D.Double> c =new Couple<Integer,Point2D.Double>(this.span_inf, p_inf);
			this.points.set(0, c);
			c =new Couple<Integer,Point2D.Double>(this.span_inf, c_inf);
			this.centers.set(0, c);
			c =new Couple<Integer,Point2D.Double>(this.span_sup, p_sup);
			this.points.set(1, c);
			c =new Couple<Integer,Point2D.Double>(this.span_sup, c_sup);
			this.centers.set(1, c);
		}
	}
	protected void buildLoop() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Couple<Boolean,Double>> distances = new ArrayList<Couple<Boolean,Double>>(this.span_sup - this.span_inf + 1);
		distances.add(new Couple<Boolean,Double>(true, Element.BASE_PAIR_DISTANCE));
		indexes.add(this.span_inf);
		if(this.span_sup - this.span_inf > 1){
			for(int i = this.span_inf+1; i <= this.span_sup; i++){
				indexes.add(i);
				RecursiveElement re = getCorrespondingRe(i);
				if(re == null){
					distances.add(new Couple<Boolean,Double>(false, Element.SPACE_BETWEEN_BASES));
				}
				else {
					distances.add(new Couple<Boolean,Double>(false, Element.SPACE_BETWEEN_BASES));
					distances.add(new Couple<Boolean,Double>(true, re.getDistanceBetweenExtrema()));
					i = re.getSpan_sup();
					indexes.add(i);
				}
			}
		}
		Point2D.Double first_point = this.getBpcre().get(0).getDraw_inf();
		this.loop = new Loop(distances, first_point, indexes);
	}
	
	private void positionChildrenAroundLoop(){
		ArrayList<GeneralPath> bounding_box_children_positioned = new ArrayList<GeneralPath>();
		/*for(RecursiveElement s : this.children){
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
			RecursiveElement re = this.children.get(i);
			GeneralPath re_gp = new GeneralPath(re.getBounding_box());
			int j = 0;
			while(re.getSpan_inf() != loop_points.get(j).first){
				j++;
			}
			AffineTransform at = new AffineTransform();
			double x = 0;
			double y = 0;
			double angle1 = this.loop.getAngles().get(j).second;
			double angle2 = this.loop.getAngles().get(j+1).second;
			double rotation_angle = Math.PI - (angle1 + angle2)/2.0;
			Couple<Point2D.Double, Point2D.Double> c = re.getFirstAndLastPoints();
			Point2D.Double rotation_center = c.first;
			at.rotate(rotation_angle, rotation_center.getX(), rotation_center.getY());
			re_gp.transform(at);
			at = new AffineTransform();
			Point2D.Double translation_vector = new Point2D.Double();
			x = loop_points.get(j).second.getX() - c.first.getX();
			y = loop_points.get(j).second.getY() - c.first.getY();
			translation_vector.setLocation(x, y);
			at.translate(translation_vector.getX(), translation_vector.getY());
			re_gp.transform(at);
			Couple<Couple<Point2D.Double,Double>,Point2D.Double> rot_trans = new Couple<Couple<Point2D.Double,Double>,Point2D.Double>(
					new Couple<Point2D.Double,Double>(rotation_center,rotation_angle),translation_vector);
			list_rot_trans.set(i, rot_trans);
			
			Area re_area = new Area(re_gp);
			System.out.println("TEST "+re.getSpan_inf()+" "+re.getSpan_sup());
			for(int k = 0; k < i; k++){
				GeneralPath gp = new GeneralPath(bounding_box_children_positioned.get(k));
				Area a = new Area(gp);
				System.out.println("Bounding1 "+re_area.getBounds2D().getX()+" "+re_area.getBounds2D().getY()+" "+re_area.getBounds2D().getWidth()+" "+re_area.getBounds2D().getHeight());
				System.out.println("Bounding2 "+a.getBounds2D().getX()+" "+a.getBounds2D().getY()+" "+a.getBounds2D().getWidth()+" "+a.getBounds2D().getHeight());
				a.intersect(re_area);
				if(!a.isEmpty()){
					System.out.println("PROBLEM");
					double distance_to_increase = this.loop.getDistances().get(j).second;
					Couple<Boolean,Double> c1 = new Couple<Boolean,Double>(false, distance_to_increase + 20.0);
					this.loop.getDistances().set(j, c1);
					this.positionChildrenAroundLoop();
					return;
				}
			}
			bounding_box_children_positioned.set(i, re_gp);
		}
		for(GeneralPath gp : bounding_box_children_positioned){
			this.debugShape.add(new GeneralPath(gp));
		}
		for(int i = 0; i < this.children.size(); i++){
			RecursiveElement re = this.children.get(i);
			Couple<Couple<Point2D.Double,Double>,Point2D.Double> rot_trans = list_rot_trans.get(i);
			re.rotate(rot_trans.first.first, rot_trans.first.second);
			re.translate(rot_trans.second);
			for(int j = 1; j < re.getPoints().size()-1; j++){
				int index = re.span_inf-this.span_inf + j;
				this.points.set(index,re.getPoints().get(j));
				this.centers.set(index, re.getCenters().get(j));
			}
		}

		/*if(re.isPseudoKnot()){
			double x = 0;
			double y = 0;
			Couple<Point2D.Double, Point2D.Double> c = re.getFirstAndLastPoints();
			Point2D.Double x_axis_vector = new Point2D.Double(0, 1);
			Point2D.Double re_vector = new Point2D.Double();
			x = loop_points.get(i+1).second.getX() - loop_points.get(i).second.getX();
			y = loop_points.get(i+1).second.getY() - loop_points.get(i).second.getY();
			re_vector.setLocation(x, y);
			double scalar_product = x_axis_vector.getX()*re_vector.getY() + x_axis_vector.getY()*re_vector.getX();
			Point2D.Double rotation_center = c.first;
			double rotation_angle = - Math.acos((scalar_product)/re.getDistanceBetweenExtrema());
			re.rotate(rotation_center, rotation_angle);
			Point2D.Double translation_vector = new Point2D.Double();
			x = loop_points.get(i).second.getX() - c.first.getX();
			y = loop_points.get(i).second.getY() - c.first.getY();
			translation_vector.setLocation(x, y);
			re.translate(translation_vector);							
		}
		else {
			double x = 0;
			double y = 0;
			Point2D.Double re_middle = new Point2D.Double();
			x = (loop_points.get(i).second.getX() + loop_points.get(i+1).second.getX())/2.0;
			y = (loop_points.get(i).second.getY() + loop_points.get(i+1).second.getY())/2.0;
			re_middle.setLocation(x, y);
			double rotation_angle = this.loop.getAngles().get(i+1).second - first_angle;
			re.rotate(this.loop.getCenter(), -rotation_angle);
			re.rotate(re_middle, Math.PI);
		}*/
	}
	
	private void assignCentersCoords(){
		for(int i = this.span_inf; i <= this.span_sup; i++) {
			RecursiveElement re = this.getCorrespondingRe(i);
			Point2D.Double loop_center = new Point2D.Double(this.loop.getCenter().getX(),this.loop.getCenter().getY());
			Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(i,loop_center);
			this.centers.set(i - this.span_inf, c);
			if(re != null) {
				i = re.span_sup -1;
			}
		}
	}
}
