package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public abstract class RecursiveElement extends Element{
	protected ArrayList<BPConstitutingRE> bpcre;
	protected int span_inf;
	protected int span_sup;
	protected ArrayList<RecursiveElement> children;
	protected GeneralPath bounding_box;
	protected ArrayList<Couple<Integer,Point2D.Double>> points;
	protected ArrayList<Couple<Integer,Point2D.Double>> centers;
	protected int strand;
	protected ArrayList<Element> element;
	public ArrayList<GeneralPath> debugShape = new ArrayList<GeneralPath>();
	
	public RecursiveElement(int inf, int sup, ArrayList<BPConstitutingRE> bpcre) {
		this.span_inf = inf;
		this.span_sup = sup;
		this.bpcre = bpcre;
		this.children = new ArrayList<RecursiveElement>();
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
	}
	
	public RecursiveElement() {
		this.children = new ArrayList<RecursiveElement>();
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
	}

	public ArrayList<BPConstitutingRE> getBpcre() {
		return bpcre;
	}

	public void setBpcre(ArrayList<BPConstitutingRE> bpcre) {
		this.bpcre = bpcre;
	}

	public int getSpan_inf() {
		return span_inf;
	}

	public void setSpan_inf(int span_inf) {
		this.span_inf = span_inf;
	}

	public int getSpan_sup() {
		return span_sup;
	}

	public void setSpan_sup(int span_sup) {
		this.span_sup = span_sup;
	}

	public ArrayList<RecursiveElement> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<RecursiveElement> children) {
		this.children = children;
	}

	public GeneralPath getBounding_box() {
		return bounding_box;
	}

	public void setBounding_box(GeneralPath bounding_box) {
		this.bounding_box = bounding_box;
	}

	public ArrayList<Couple<Integer, Point2D.Double>> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<Couple<Integer, Point2D.Double>> points) {
		this.points = points;
	}

	public ArrayList<Couple<Integer, Point2D.Double>> getCenters() {
		return centers;
	}

	public void setCenters(ArrayList<Couple<Integer, Point2D.Double>> centers) {
		this.centers = centers;
	}

	public int getStrand() {
		return strand;
	}

	public void setStrand(int strand) {
		this.strand = strand;
	}

	public ArrayList<Element> getElement() {
		return element;
	}

	public void setElement(ArrayList<Element> element) {
		this.element = element;
	}

	public ArrayList<GeneralPath> getDebugShape() {
		return debugShape;
	}

	public void setDebugShape(ArrayList<GeneralPath> debugShape) {
		this.debugShape = debugShape;
	}
	
	public int getElementInf(){
		return this.getSpan_inf();
	}
	
	public int getElementSup(){
		return this.getSpan_sup();
	} 
	
	protected void initializeDistances(){
		for(RecursiveElement re : this.children) {
			Rectangle2D rectangle = this.bounding_box.getBounds2D();
			double distance_above = 0;
			double distance_beneath;
			if(re.getStrand()%2 == 0){
				distance_above = Math.max(re.getDraw_inf().getX() - rectangle.getX(), Element.SPACE_BETWEEN_BASES);			
				re.setDistance_above(distance_above);
				distance_beneath = (rectangle.getWidth() + rectangle.getX()) - this.draw_inf.getX();
				re.setDistance_beneath(distance_beneath);
			}
			else {
				distance_above = Math.max((rectangle.getWidth() + rectangle.getX()) - this.draw_inf.getX(), Element.SPACE_BETWEEN_BASES);
				re.setDistance_above(distance_above);
				distance_beneath = this.draw_inf.getX() - rectangle.getX();
				re.setDistance_beneath(distance_beneath);
			}			
		}
	}

	protected RecursiveElement getCorrespondingRe(int i) {
		for(RecursiveElement re : this.children){
			if(i < re.getSpan_inf()){
				return null;
			}
			else if(i == re.getSpan_inf()){
				return re;
			}
		}
		return null;
	}
	
	public boolean isPseudoKnot(){
		if(this.bpcre.size() > 1){
			return true;
		}
		return false;
	}

	/*public void assignStrands(){
		ArrayList<Couple<Integer, Node>> l = new ArrayList<Couple<Integer, Node>>();
		for(Node n : this.nodes){
			if(n.getInf() == n.getSup()){
				l.add(new Couple<Integer, Node>(n.getInf(), n));
			}
			else {
				l.add(new Couple<Integer, Node>(n.getInf(), n));
				l.add(new Couple<Integer, Node>(n.getSup(), n));
			}
		}
		Collections.sort(l, new Comparator<Couple<Integer, Node>>(){
			public int compare(Couple<Integer, Node> c1, Couple<Integer, Node> c2) {
				if(c1.first < c2.first){
					return -1;
				}
				else return 1;
			}
			
		});
		int cur_strand = 0;
		for(Couple<Integer, Node> c : l){
			if(c.first == c.second.getInf()){
				if(c.second.getColor() == 0){
					if(cur_strand%2 == 0){
						cur_strand++;
					}
				}
				else{
					if(cur_strand%2 == 1){
						cur_strand++;
					}
				}
				c.second.setStrand_inf(cur_strand);
			}
			else {
				if(c.second.getColor() == 0){
					if(cur_strand%2 == 1){
						cur_strand++;
					}
				}
				else{
					if(cur_strand%2 == 0){
						cur_strand++;
					}
				}
				c.second.setStrand_sup(cur_strand);
			}
			Strand s = getCorrespondingStrand(cur_strand);
			if(s != null){
				
				s.getNodes().add(c.second);
			}
			else{
				s = new Strand(cur_strand);
				this.strands.add(s);
				s.getNodes().add(c.second);
			}
		}
		for(Node n : this.nodes){
			System.out.println("("+n.getInf()+","+n.getSup()+") "+n.getStrand_inf()+"-"+n.getStrand_sup()+"  "+strands.size());
		}
		for(Strand s : this.strands){
			s.sortNodes();
			s.buildRelationBetweenNodeInStrand();
			System.out.println();
		}
		for(Node n : this.nodes){
			System.out.print("("+n.getInf()+","+n.getSup()+") / ");
			for(Node n1 : n.getDraw_fathers()){
				System.out.print("("+n1.getInf()+","+n1.getSup()+") ");
			}
			System.out.print(" / ");
			for(Node n2 : n.getDraw_children()){
				System.out.print("("+n2.getInf()+","+n2.getSup()+") ");
			}
			System.out.println();
		}
		this.assignAbsoluteCoords();
	}*/
	
	/*private Strand getCorrespondingStrand(int n){
		for(Strand s : this.strands){
			if(s.getStrand_num() == n){
				return s;
			}
		}
		return null;
	}*/
	
	
	
	/*	public void setDrawYs(){
		ArrayList<Element> visited = new ArrayList<Element>();
		ArrayList<Element> queue = new ArrayList<Element>();
		for(Element n : this.Element){
			if(n.getDraw_fathers().isEmpty()){
				if(n instanceof Node){
					n.setDraw_infY(0.0);
					n.setDraw_supY(0.0);					
				}
				else {
					if(((RecursiveElement) n).getStrand()%2 == 0){
						n.setDraw_infY(((RecursiveElement) n).getDistanceBetweenExtrema());
						n.setDraw_supY(0.0);
					}
					else {
						n.setDraw_infY(0.0);
						n.setDraw_supY(((RecursiveElement) n).getDistanceBetweenExtrema());						
					}
				}
				for(Element children : n.getDraw_children()){
					if(!queue.contains(children)){
						queue.add(children);
					}
				}
				visited.add(n);
			}
		}
		while(!queue.isEmpty()){
			Element n = queue.remove(0);
			if(!visited.containsAll(n.getDraw_fathers())){
				queue.add(n);
			}
			else{
				double max = 0;
				if(n instanceof Node){
					for(Element father : n.getDraw_fathers()){
						double minimum_height = 0;
						if(father instanceof Node){
							double y_father;
							if(((Node) father).getStrand_inf() == ((Node)n).getStrand_inf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((Node) n).updateDistanceAbove(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else if(((Node) father).getStrand_sup() == ((Node)n).getStrand_inf()){
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((Node) n).updateDistanceAbove(Math.abs(father.getDraw_sup().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else if(((Node) father).getStrand_inf() == ((Node)n).getStrand_sup()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((Node) n).updateDistanceAboveSup(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_supY(minimum_height);
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((Node) n).updateDistanceAboveSup(Math.abs(father.getDraw_sup().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_supY(minimum_height);
							}
						}
						else {
							int f_strand = ((RecursiveElement) father).getStrand();
							double y_father;
							if(f_strand%2 == 0){
								y_father = father.getDraw_inf().getY();
							}
							else {
								y_father = father.getDraw_sup().getY();
							}
							minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
							if(n.getBoundOnStrand(f_strand) == n.getElementInf()){
								((Node) n).updateDistanceAbove(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else{
								((Node) n).updateDistanceAboveSup(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_supY(minimum_height);
							}
						}
						max = Math.max(max, minimum_height);
					}
					if(max == n.getDraw_inf().getY()){
						((Node) n).updateDistanceAboveSup(n.getDistance_above() - (max - n.getDraw_sup().getY()));
						n.setDraw_supY(max);				
					}
					else {
						((Node) n).updateDistanceAbove(n.getDistance_above() - (max - n.getDraw_inf().getY()));
						n.setDraw_infY(max);
					}
				}
				else {
					for(Element father : n.getDraw_fathers()){
						double minimum_height = 0;
						if(father instanceof Node){
							int n_strand = ((RecursiveElement) n).getStrand();
							double y_father;
							if(father.getBoundOnStrand(n_strand) == father.getElementInf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + father.getDistance_above() + n.getDistance_beneath();
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + ((Node) father).getDistance_above_sup() + n.getDistance_beneath();								
							}
						}
						else {
							minimum_height = father.getDraw_inf().getY() + father.getDistance_above() + n.getDistance_beneath();
						}
						max = Math.max(max, minimum_height);
					}
					if(((RecursiveElement) n).getStrand()%2 == 0){
						n.setDraw_infY(max); 
						n.setDraw_supY(max - ((RecursiveElement) n).getDistanceBetweenExtrema());
					}
					else {
						n.setDraw_infY(max); 
						n.setDraw_supY(max + ((RecursiveElement) n).getDistanceBetweenExtrema());
					}
				}
				for(Element children : n.getDraw_children()){
					if(!queue.contains(children)){
						queue.add(children);
					}
				}
				visited.add(n);
			}
		}		
	}*/
	
/*	public void setDrawYs(){
		ArrayList<Node> visited = new ArrayList<Node>();
		ArrayList<Node> queue = new ArrayList<Node>();
		for(Node n : this.nodes){
			if(n.getDraw_fathers().isEmpty()){
				n.setDraw_infY(0.0);
				n.setDraw_supY(0.0);
				queue.addAll(n.getDraw_children());
				visited.add(n);
			}
		}
		while(!queue.isEmpty()){
			Node n = queue.remove(0);
			if(!visited.containsAll(n.getDraw_fathers())){
				queue.add(n);
			}
			else{
				double max = 0;
				for(Node father : n.getDraw_fathers()){
					int inf, sup;
					if(father.getStrand_inf() == n.getStrand_inf()){
						inf = father.getInf();
						sup = n.getInf();
					}
					else if(father.getStrand_inf() == n.getStrand_sup()){
						inf = father.getInf();
						sup = n.getSup();
					}
					else if(father.getStrand_sup() == n.getStrand_inf()){
						inf = father.getSup();
						sup = n.getInf();
					}
					else{
						inf = father.getSup();
						sup = n.getSup();
					}
					double minimum_height = getMinimumY(inf,sup) + father.getDraw_inf().getY();
					max = Math.max(max, minimum_height);
				}
				n.setDraw_infY(max); 
				n.setDraw_supY(max); 
				queue.addAll(n.getDraw_children());
				visited.add(n);
			}
		}
		for(Node n : this.nodes){
			System.out.println("("+n.getInf()+","+n.getSup()+") "+n.getDraw_inf().getY());
		}
		
	}*/

	protected double getMinimumY(int inf, int sup) {
		return Math.abs((sup-inf))*SPACE_BETWEEN_BASES;
	}
	
	
	
	protected void buildBoundingBox(){
		Point2D.Double first_point = this.points.get(0).second;
		this.bounding_box.moveTo(first_point.getX(), first_point.getY());
		for(Couple<Integer,Point2D.Double> c : this.points){
			this.bounding_box.lineTo(c.second.getX(), c.second.getY());
		}
		this.bounding_box.closePath();
	}

	protected void initializeListPoints() {
		int nb_bases = 0;
		if(this.isRoot()){
			nb_bases = this.span_sup - 1;
		}
		else {
			nb_bases = this.span_sup - this.span_inf;
		}
		for(int i = 0; i <= nb_bases; i++){
			Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(i+this.span_inf, new Point2D.Double());
			this.points.add(c);
		}
	}
	
	protected void initializeListCenters() {
		int nb_bases = 0;
		if(this.isRoot()){
			nb_bases = this.span_sup - 1;
		}
		else {
			nb_bases = this.span_sup - this.span_inf;
		}
		for(int i = 0; i <= nb_bases; i++){
			Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(i+this.span_inf, new Point2D.Double());
			this.centers.add(c);
		}
	}

	protected void sortPoints() {
		Collections.sort(this.points, new Comparator<Couple<Integer,Point2D.Double>>(){
			public int compare(Couple<Integer, Point2D.Double> c1,
					Couple<Integer, Point2D.Double> c2) {
				if(c1.first < c2.first){
					return -1;
				}
				else {
					return 1;
				}
			}
		});
	}
	
	protected void sortCenters() {
		Collections.sort(this.centers, new Comparator<Couple<Integer,Point2D.Double>>(){
			public int compare(Couple<Integer, Point2D.Double> c1,
					Couple<Integer, Point2D.Double> c2) {
				if(c1.first < c2.first){
					return -1;
				}
				else {
					return 1;
				}
			}
		});
	}
	
	protected double getDistanceBetweenExtrema(){
		double distance = 0;
		if(this.isPseudoKnot()){
			Couple<Point2D.Double, Point2D.Double> c = this.getFirstAndLastPoints();
			distance = Math.sqrt((c.second.getX()-c.first.getX())*(c.second.getX()-c.first.getX()) + (c.second.getY()-c.first.getY())*(c.second.getY()-c.first.getY()));
		}
		else {
			distance = Element.BASE_PAIR_DISTANCE;
		}
		return distance;
	}

	public int getBoundOnStrand(int strand_num) {
		if(strand_num != strand){
			return -1;
		}
		else return this.span_inf;
	}
	
	public void rotate(Point2D.Double center, double angle){
		double x = 0;
		double y = 0;
		for(Couple<Integer,Point2D.Double> p : this.points){
			x = Math.cos(angle)*(p.second.getX() - center.getX()) - Math.sin(angle)*(p.second.getY() - center.getY()) + center.getX();
			y = Math.sin(angle)*(p.second.getX() - center.getX()) + Math.cos(angle)*(p.second.getY() - center.getY()) + center.getY();
			p.second.setLocation(x, y);
		}
		for(Couple<Integer,Point2D.Double> c : this.centers){
			x = Math.cos(angle)*(c.second.getX() - center.getX()) - Math.sin(angle)*(c.second.getY() - center.getY()) + center.getX();
			y = Math.sin(angle)*(c.second.getX() - center.getX()) + Math.cos(angle)*(c.second.getY() - center.getY()) + center.getY();
			c.second.setLocation(x, y);
		}
		AffineTransform  at = new AffineTransform();
		at.rotate(angle, center.getX(), center.getY());
		this.bounding_box.transform(at);
	}
	
	public void translate(Point2D.Double vector){
		for(Couple<Integer,Point2D.Double> p : this.points){
			double x = p.second.getX() + vector.getX();
			double y = p.second.getY() + vector.getY();
			p.second.setLocation(x, y);
		}
		for(Couple<Integer,Point2D.Double> c : this.centers){
			double x = c.second.getX() + vector.getX();
			double y = c.second.getY() + vector.getY();
			c.second.setLocation(x, y);
		}
		AffineTransform at = new AffineTransform();
		at.translate(vector.getX(), vector.getY());
		this.bounding_box.transform(at);
	}
	
	public void verticalSymmetry(double axis){
		for(Couple<Integer,Point2D.Double> p : this.points){
			double x = p.second.getX();
			double y = axis - (p.second.getY() - axis);
			p.second.setLocation(x, y);
		}
		for(Couple<Integer,Point2D.Double> c : this.centers){
			double x = c.second.getX();
			double y = axis - (c.second.getY() - axis);
			c.second.setLocation(x, y);
		}
		AffineTransform  at = new AffineTransform(1.0, 0, 0, -1.0, 0, 2.0*axis);
		this.bounding_box.transform(at);
	}
	
	public Couple<Point2D.Double, Point2D.Double> getFirstAndLastPoints(){
		Point2D.Double p1 = null;
		Point2D.Double p2 = null;
		for(Couple<Integer,Point2D.Double> c : this.points){
			if(c.first == this.span_inf){
				p1 = c.second;
			}
			else if(c.first == this.span_sup){
				p2 = c.second;
			}
		}
		Couple<Point2D.Double, Point2D.Double> c = new Couple<Point2D.Double, Point2D.Double>(p1, p2);
		return c;
	}

	public boolean isRoot() {
		if(this.span_inf == -1){
			return true;
		}
		else {
			return false;
		}
	}

	public void verticalFlip(){
		System.out.println("ICI 1 " + this.points);
		for(Couple<Integer,Point2D.Double> p : this.points){
			double x = p.second.getX();
			double y = -p.second.getY();
			p.second.setLocation(x, y);
		}
		System.out.println("ICI 1 " + this.centers);
		for(Couple<Integer,Point2D.Double> c : this.centers){
			double x = c.second.getX();
			double y = -c.second.getY();
			c.second.setLocation(x, y);
		}
		System.out.println("ICI 2 " + this.centers);
		AffineTransform  at = new AffineTransform(1.0, 0, 0, -1.0, 0, 0);
		this.bounding_box.transform(at);		
	}
	
	public abstract void assignCoords();
	
}
