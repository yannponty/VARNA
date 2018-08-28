package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class PseudoKnot extends StronglyConnectedComponent{
	protected ArrayList<Trombone> trombones;
	protected ArrayList<Strand> strands;
	
	public PseudoKnot(){
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
		this.trombones = new ArrayList<Trombone>();
		this.strands = new ArrayList<Strand>();
	}	
	
	public void assignCoords() {
		this.initializeListPoints();
		this.initializeListCenters();
		this.assignStrands();
		System.out.println("[A]");
		System.out.flush();
		this.initializeDistances();
		System.out.println("[B]");
		System.out.flush();
		this.setDrawYs();
		System.out.println("[C]");
		System.out.flush();
		this.setDrawXs();
		System.out.println("[D]");
		System.out.flush();
		this.assignPointsAndCentersElementsFromStrands();
		System.out.println("[E]");
		System.out.flush();
		this.positionChildrenAroundStrands();
		System.out.println("[F]");
		System.out.flush();
		this.assignPointsAndCentersElementsFromTrombones();
		System.out.println("[G]");
		System.out.flush();
		//this.positionPseudoKnotCorrectly();
		System.out.println("[H]");
		System.out.flush();
		this.buildBoundingBox();
		System.out.println("[I]");
		System.out.flush();
	}
	
	public void assignStrands(){
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
				int i = s.getElements().get(s.getElements().size()-1).getBoundOnStrand(cur_strand) + 1;
				for(; i < c.first; i++){
					StronglyConnectedComponent scc = getCorrespondingScc(i);
					if(scc == null){
						Node n = new Node(i,i,cur_strand,cur_strand);
						s.getElements().add(n);
						this.element.add(n);
					}
					else {
						s.getElements().add(scc);
						this.element.add(scc);
						i = scc.getSpan_sup();
						scc.setStrand(cur_strand);
						//scc.initializeDistances();
					}
				}
				s.getElements().add(c.second);
				this.element.add(c.second);
			}
			else {
				if(cur_strand > 1){
					int last_scc_index = -1;
					ArrayList<Element> temp_list = new ArrayList<Element>();
					Strand last_strand = this.strands.get(this.strands.size()-1);
					int i = last_strand.getElements().get(last_strand.getElements().size()-1).getBoundOnStrand(cur_strand-1) + 1;
					for(; i < c.first; i++){
						StronglyConnectedComponent scc = getCorrespondingScc(i);
						if(scc == null){
							Node n = new Node(i,i,cur_strand-1,cur_strand-1);
							temp_list.add(n);
						}
						else {
							temp_list.add(scc);
							last_scc_index = temp_list.size() - 1;
							i = scc.getSpan_sup();
						}
					}
					for(int j = 0; j <= last_scc_index; j++){
						last_strand.getElements().add(temp_list.get(j));
						this.element.add(temp_list.get(j));
						if(temp_list.get(j) instanceof StronglyConnectedComponent){
							((StronglyConnectedComponent) temp_list.get(j)).setStrand(cur_strand-1);
							//((StronglyConnectedComponent) temp_list.get(j)).initializeDistances();
						}
					}
					for(Element n : temp_list){
						System.out.println("STRAND "+n.getElementInf()+" "+n.getElementSup());
					}
					if(!temp_list.isEmpty()){
						if(last_scc_index < temp_list.size()-1){
							Element last_element_strand = last_strand.getElements().get(last_strand.getElements().size()-1);
							Point2D.Double first_point = new Point2D.Double();
							int first_index = 0;
							if(last_element_strand instanceof Node){
								first_index = last_element_strand.getBoundOnStrand(cur_strand-1);
								if(last_element_strand.getBoundOnStrand(cur_strand-1) ==last_element_strand.getElementInf()){
									first_point = last_element_strand.getDraw_inf();
								}
								else{
									first_point = last_element_strand.getDraw_sup();
								}
							}
							else {
								first_index = last_element_strand.getElementSup();
								first_point = last_element_strand.getDraw_sup();
							}
							Point2D.Double last_point = c.second.getCoordBoundOnStrand(cur_strand);
							int nb_base = temp_list.size() - last_scc_index - 1;
							boolean even_strand = ((cur_strand-1)%2 == 0);
							Trombone t = new Trombone(first_point, last_point, nb_base, first_index, even_strand);
							trombones.add(t);
						}
					}
				}
				s = new Strand(cur_strand);
				this.strands.add(s);
				s.getElements().add(c.second);
				this.element.add(c.second);
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
		for(Element n : this.element){
			System.out.print("("+n.getElementInf()+","+n.getElementSup()+") / ");
			for(Element n1 : n.getDraw_fathers()){
				System.out.print("("+n1.getElementInf()+","+n1.getElementSup()+") ");
			}
			System.out.print(" / ");
			for(Element n2 : n.getDraw_children()){
				System.out.print("("+n2.getElementInf()+","+n2.getElementSup()+") ");
			}
			System.out.println();
		}
	}
	
	protected void setDrawXs(){
		double last_x = 0;
		ArrayList<Element> visited = new ArrayList<Element>();
		for(int i = 0; i < this.strands.size(); i++){
			if(i == 0){
				for(Element n : this.strands.get(i).getElements()){
					if(n instanceof Node){
						n.setDraw_infX(0.0);
					}
					else {
						n.setDraw_infX(0.0);
						n.setDraw_supX(0.0);
					}
					visited.add(n);
				}
			}
			else {
				double minimum_distance = 30;
				double strand_increment = this.strands.get(i).getMaxHeightScc() + minimum_distance;
				double minimum_increment = Element.BASE_PAIR_DISTANCE;
				double x_increment = Math.max(strand_increment, minimum_increment);
				for(Element n : this.strands.get(i).getElements()){
					if(!visited.contains(n)){
						if(n instanceof Node){
							n.setDraw_infX(last_x + x_increment);
						}
						else {
							n.setDraw_infX(last_x + x_increment);
							n.setDraw_supX(last_x + x_increment);
						}
						visited.add(n);
					}
				}
				last_x += x_increment;
			}
		}
		for(Node n : this.nodes){
			n.setDraw_supX(n.getDraw_inf().getX() + Element.BASE_PAIR_DISTANCE);
		}
	}
	
	/*public void setDrawYs(){
		ArrayList<Element> visited = new ArrayList<Element>();
		ArrayList<Element> queue = new ArrayList<Element>();
		for(Element n : this.element){
			if(n.getDraw_fathers().isEmpty()){
				if(n instanceof Node){
					n.setDraw_infY(0.0);
					n.setDraw_supY(0.0);					
				}
				else {
					if(((StronglyConnectedComponent) n).getStrand()%2 == 0){
						n.setDraw_infY(((StronglyConnectedComponent) n).getDistanceBetweenExtrema());
						n.setDraw_supY(0.0);
					}
					else {
						n.setDraw_infY(0.0);
						n.setDraw_supY(((StronglyConnectedComponent) n).getDistanceBetweenExtrema());						
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
		
		ArrayList<Double> maxHeightsLastSccs = new ArrayList<Double>();
		for(Strand s : this.strands){
			maxHeightsLastSccs.add(-1.0);
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
							int f_strand = ((StronglyConnectedComponent) father).getStrand();
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
							int n_strand = ((StronglyConnectedComponent) n).getStrand();
							double y_father;
							if(father.getBoundOnStrand(n_strand) == father.getElementInf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + father.getDistance_above();
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + ((Node) father).getDistance_above_sup();						
							}
							if(maxHeightsLastSccs.get(n_strand-1) != -1){
								double minHeightScc = minimum_height - n.getDistance_beneath();
								double diff_height = maxHeightsLastSccs.get(n_strand-1) - minHeightScc;
								if(diff_height > 0){
									minimum_height += diff_height + 30;
								}
							}
						}
						else {
							minimum_height = father.getDraw_inf().getY() + father.getDistance_above() + n.getDistance_beneath() + 30;
						}
						max = Math.max(max, minimum_height);
					}
					int cur_strand_num = ((StronglyConnectedComponent) n).getStrand();
					if(cur_strand_num%2 == 0){
						n.setDraw_infY(max + ((StronglyConnectedComponent) n).getDistanceBetweenExtrema()); 
						n.setDraw_supY(max);
					}
					else {
						n.setDraw_infY(max); 
						n.setDraw_supY(max + ((StronglyConnectedComponent) n).getDistanceBetweenExtrema());
					}
					maxHeightsLastSccs.set(cur_strand_num-1, n.getDraw_inf().getY() + n.getDistance_above());
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
	public void setDrawYs(){
		ArrayList<Element> visited = new ArrayList<Element>();
		ArrayList<Element> queue = new ArrayList<Element>();
		for(Element n : this.element){
			if(n.getDraw_fathers().isEmpty()){
				if(n instanceof Node){
					n.setDraw_infY(0.0);
					n.setDraw_supY(0.0);					
				}
				else {
					if(((StronglyConnectedComponent) n).getStrand()%2 == 0){
						n.setDraw_infY(((StronglyConnectedComponent) n).getDistanceBetweenExtrema());
						n.setDraw_supY(0.0);
					}
					else {
						n.setDraw_infY(0.0);
						n.setDraw_supY(((StronglyConnectedComponent) n).getDistanceBetweenExtrema());						
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
		
		ArrayList<Double> maxHeightsLastSccs = new ArrayList<Double>();
		for(Strand s : this.strands){
			maxHeightsLastSccs.add(-1.0);
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
							int f_strand = ((StronglyConnectedComponent) father).getStrand();
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
							int n_strand = ((StronglyConnectedComponent) n).getStrand();
							double y_father;
							if(father.getBoundOnStrand(n_strand) == father.getElementInf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + father.getDistance_above();
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + ((Node) father).getDistance_above_sup();						
							}
							if(maxHeightsLastSccs.get(n_strand-1) != -1){
								double minHeightScc = minimum_height - n.getDistance_beneath();
								double diff_height = maxHeightsLastSccs.get(n_strand-1) - minHeightScc;
								if(diff_height > 0){
									minimum_height += diff_height + 30;
								}
							}
						}
						else {
							minimum_height = father.getDraw_inf().getY() + father.getDistance_above() + n.getDistance_beneath() + 30;
						}
						max = Math.max(max, minimum_height);
					}
					int cur_strand_num = ((StronglyConnectedComponent) n).getStrand();
					if(cur_strand_num%2 == 0){
						n.setDraw_infY(max + ((StronglyConnectedComponent) n).getDistanceBetweenExtrema()); 
						n.setDraw_supY(max);
					}
					else {
						n.setDraw_infY(max); 
						n.setDraw_supY(max + ((StronglyConnectedComponent) n).getDistanceBetweenExtrema());
					}
					maxHeightsLastSccs.set(cur_strand_num-1, n.getDraw_inf().getY() + n.getDistance_above());
				}
				for(Element children : n.getDraw_children()){
					if(!queue.contains(children)){
						queue.add(children);
					}
				}
				visited.add(n);
			}
		}		
	}
	
	private Strand getCorrespondingStrand(int n){
		for(Strand s : this.strands){
			if(s.getStrand_num() == n){
				return s;
			}
		}
		return null;
	}
	
	private void assignPointsAndCentersElementsFromStrands(){
		ArrayList<Element> visited = new ArrayList<Element>();
		for(Element n : this.element){
			if(!visited.contains(n)){
				if(n.getElementInf() == n.getElementSup()){
					int index = n.getElementInf() - this.span_inf;
					Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(n.getElementInf(),n.getDraw_inf());
					this.points.set(index, c);
					Point2D.Double center = new Point2D.Double(n.getDraw_inf().getX() + 5, n.getDraw_inf().getY());
					c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center);
					this.centers.set(index, c);
					visited.add(n);
				}
				else {
					int index1 = n.getElementInf() - this.span_inf;
					int index2 = n.getElementSup() - this.span_inf;
					Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(n.getElementInf(),n.getDraw_inf());
					this.points.set(index1, c);
					c = new Couple<Integer,Point2D.Double>(n.getElementSup(),n.getDraw_sup());
					this.points.set(index2, c);
					Point2D.Double center1;
					Point2D.Double center2;
					if(n instanceof PseudoKnot) {
						center1 = new Point2D.Double(n.getDraw_inf().getX() - 5, n.getDraw_inf().getY());
						center2 = new Point2D.Double(n.getDraw_sup().getX() - 5, n.getDraw_sup().getY());
						c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center1);
						this.centers.set(index1, c);
						c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center2);
						this.centers.set(index2, c);
					}
					else {
						center1 = new Point2D.Double(n.getDraw_inf().getX() + 5, n.getDraw_inf().getY());
						center2 = new Point2D.Double(n.getDraw_sup().getX() - 5, n.getDraw_sup().getY());
						c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center1);
						this.centers.set(index1, c);
						c = new Couple<Integer,Point2D.Double>(n.getElementSup(),center2);
						this.centers.set(index2, c);
					}
					visited.add(n);
				}
			}
		}
		this.sortPoints();
		this.sortCenters();
	}
	
	private void positionChildrenAroundStrands() {
		for(StronglyConnectedComponent scc : this.children){
			Couple<Point2D.Double, Point2D.Double> c = scc.getFirstAndLastPoints();
			scc.rotate(c.first, Math.PI/2.0);
			ArrayList<Element> strand_elements = this.strands.get(scc.getStrand() - 1).getElements();
			for(int i = 0; i < strand_elements.size(); i++){
				if(scc.getSpan_inf() == strand_elements.get(i).getElementInf()){	
					if(scc.getStrand()%2 == 0){
						double x = strand_elements.get(i).getDraw_sup().getX() - c.first.getX();
						double y = strand_elements.get(i).getDraw_sup().getY() - c.first.getY();
						Point2D.Double translation_vector = new Point2D.Double(x, y);
						scc.translate(translation_vector);
						Point2D.Double scc_middle = new Point2D.Double();
						x = (c.first.getX() + c.second.getX())/2.0;
						y = (c.first.getY() + c.second.getY())/2.0;
						scc_middle.setLocation(x, y);
						scc.verticalSymmetry(scc_middle.getY());
					}
					else {
						double x = strand_elements.get(i).getDraw_inf().getX() - c.first.getX();
						double y = strand_elements.get(i).getDraw_inf().getY() - c.first.getY();
						Point2D.Double translation_vector = new Point2D.Double(x, y);
						scc.translate(translation_vector);
					}
				}
			}
			for(int j = 1; j < scc.getPoints().size()-1; j++){
				int index = scc.span_inf-this.span_inf + j;
				this.points.set(index,scc.getPoints().get(j));
				this.centers.set(index,scc.getCenters().get(j));
			}
		}
	}
	
	private void assignPointsAndCentersElementsFromTrombones() {
		for(int i = 0; i < this.trombones.size(); i++){
			this.trombones.get(i).assignPointsAndCentersCoords();
			int index = this.trombones.get(i).getFirst_index() - this.span_inf + 1;
			for(int j = 0; j < this.trombones.get(i).getPoints().size(); j++){
				this.points.set(index+j, this.trombones.get(i).getPoints().get(j));
				this.centers.set(index+j, this.trombones.get(i).getCenters().get(j));
			}
		}
	}
	
	private void positionPseudoKnotCorrectly() {
		double x = 0;
		double y = 0;
		Couple<Point2D.Double, Point2D.Double> c = this.getFirstAndLastPoints();
		Point2D.Double x_axis_vector = new Point2D.Double(0, 1);
		Point2D.Double scc_vector = new Point2D.Double();
		x = c.second.getX() - c.first.getX();
		y = c.second.getY() - c.first.getY();
		scc_vector.setLocation(x, y);
		double scalar_product = x_axis_vector.getX()*scc_vector.getY() + x_axis_vector.getY()*scc_vector.getX();
		double rotation_angle;
		Point2D.Double rotation_center = c.first;
		if(c.first.getY() < c.second.getY()){
			rotation_angle =  - Math.acos((scalar_product)/this.getDistanceBetweenExtrema());
		}
		else {
			rotation_angle = Math.acos((scalar_product)/this.getDistanceBetweenExtrema());
		}
		this.rotate(rotation_center, rotation_angle);
	}
}
