package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class Root extends StronglyConnectedComponent{
	private Strand strand;
	private Configuration configuration;
	
	public Root(int inf, int sup){
		super();
		this.span_inf = inf;
		this.span_sup = sup;
		this.children = new ArrayList<StronglyConnectedComponent>();
		this.father = null;
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
	}	

	/*public void assignCoords(){
		this.initializeListPoints();
		this.initializeListCenters();
		this.buildRootStrand();
		this.initializeDistances();
		this.setDrawYs();
		for(Element n : this.element){
			n.setDraw_infX(n.getDraw_inf().getY());
			n.setDraw_supX(n.getDraw_sup().getY());
			n.setDraw_infY(0.0);
			n.setDraw_supY(0.0);
		}
		this.assignPointsAndCentersElementsFromStrand();
		this.positionChildrenAroundStrand();
		this.buildBoundingBox();
	}*/
	
	public void assignCoords(){
		this.initializeListPoints();
		this.initializeListCenters();
		this.buildRootStrand();
		this.initializeDistances();
		this.setDrawYs();
		this.setDrawXs();
		/*for(Element n : this.element){
			n.setDraw_infX(n.getDraw_inf().getY());
			n.setDraw_supX(n.getDraw_sup().getY());
			n.setDraw_infY(0.0);
			n.setDraw_supY(0.0);
		}*/
		this.positionChildrenAroundStrand();
		this.findBestConfiguration();
		this.applyBestConfiguration();
		this.assignPointsAndCentersElementsFromStrand();
		this.buildBoundingBox();
	}

	private void buildRootStrand(){
		Strand s = new Strand(0);
		for(int i = 0; i < this.span_sup; i++){
			StronglyConnectedComponent scc = this.getCorrespondingScc(i);
			if(scc == null){
				Node n = new Node(i,i,1,1);
				s.getElements().add(n);
				this.element.add(n);
			}
			else {
				s.getElements().add(scc);
				i = scc.span_sup;
				this.element.add(scc);
				scc.setStrand(1);
				//scc.initializeDistances();
			}
		}
		s.sortNodes();
		s.buildRelationBetweenNodeInStrand();
		this.strand = s;
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
	
	private void assignPointsAndCentersElementsFromStrand() {
		ArrayList<Element> visited = new ArrayList<Element>();
		for(Element n : this.element){
			if(!visited.contains(n)){
				if(n.getElementInf() == n.getElementSup()){
					int index = n.getElementInf();
					Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(n.getElementInf(),n.getDraw_inf());
					this.points.set(index, c);
					Point2D.Double center = new Point2D.Double(n.getDraw_inf().getX(), n.getDraw_inf().getY() + 5);
					c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center);
					this.centers.set(index, c);
					visited.add(n);
				}
				else {
					int index1 = n.getElementInf();
					int index2 = n.getElementSup();
					Couple<Integer,Point2D.Double> c = new Couple<Integer,Point2D.Double>(n.getElementInf(),n.getDraw_inf());		
					this.points.set(index1, c);
					c = new Couple<Integer,Point2D.Double>(n.getElementSup(),n.getDraw_sup());
					this.points.set(index2, c);
					Point2D.Double center1;
					Point2D.Double center2;
					if(n instanceof PseudoKnot) {
						center1 = new Point2D.Double(n.getDraw_inf().getX(), n.getDraw_inf().getY() + 5);
						center2 = new Point2D.Double(n.getDraw_sup().getX() - 5, n.getDraw_sup().getY() + 5);
					}
					else {
						center1 = new Point2D.Double(n.getDraw_inf().getX() + 5, n.getDraw_inf().getY());
						center2 = new Point2D.Double(n.getDraw_sup().getX() - 5, n.getDraw_sup().getY());
					}
					c = new Couple<Integer,Point2D.Double>(n.getElementInf(),center1);
					this.centers.set(index1, c);
					c = new Couple<Integer,Point2D.Double>(n.getElementSup(),center2);
					this.centers.set(index2, c);
					visited.add(n);
				}
			}
		}
		
		for(StronglyConnectedComponent scc : this.children){
			for(int i = 1; i <= scc.getPoints().size()-1; i++){
				int index = scc.span_inf + i;
				this.points.set(index,scc.getPoints().get(i));
				this.centers.set(index,scc.getCenters().get(i));
			}
		}
		this.sortPoints();
		this.sortCenters();
	}
	
	private void positionChildrenAroundStrand() {
		for(StronglyConnectedComponent scc : this.children){
			Couple<Point2D.Double, Point2D.Double> c = scc.getFirstAndLastPoints();
			ArrayList<Element> strand_elements = this.strand.getElements();
			for(int i = 0; i < strand_elements.size(); i++){
				if(scc.getSpan_inf() == strand_elements.get(i).getElementInf()){	
					double x = strand_elements.get(i).getDraw_inf().getX() - c.first.getX();
					double y = strand_elements.get(i).getDraw_inf().getY() - c.first.getY();
					Point2D.Double translation_vector = new Point2D.Double(x, y);
					scc.translate(translation_vector);				
				}
			}
			/*for(int j = 1; j <= scc.getPoints().size()-1; j++){
				int index = scc.span_inf + j;
				this.points.set(index,scc.getPoints().get(j));
				this.centers.set(index,scc.getCenters().get(j));
			}*/
		}
	}
	
	private void setDrawYs() {
		for(Element n : this.element){
			n.setDraw_infY(0.0);
			n.setDraw_supY(0.0);
		}
	}
	
	private void setDrawXs() {
		Element e = this.element.get(0);
		if(e instanceof Node) {
			e.setDraw_infX(0.);
			e.setDraw_supX(0.);			
		}
		else {
			e.setDraw_infX(0.);
			e.setDraw_supX(((StronglyConnectedComponent) e).getDistanceBetweenExtrema());	
		}		
		for(int i = 1; i < this.element.size(); i++) {
			e = this.element.get(i);
			double x_father = e.getDraw_fathers().get(0).getDraw_sup().getX();
			e.setDraw_infX(x_father + Element.SPACE_BETWEEN_BASES);
			if(e instanceof Node){
				e.setDraw_supX(x_father + Element.SPACE_BETWEEN_BASES);
			}
			else {
				e.setDraw_supX(x_father + Element.SPACE_BETWEEN_BASES + ((StronglyConnectedComponent) e).getDistanceBetweenExtrema());
			}
		}
	}
	
	private void findBestConfiguration() {
		Configuration best_conf = new Configuration();
		ArrayList<GeneralPath> bounding_boxes = new ArrayList<GeneralPath>();
		for(StronglyConnectedComponent scc : this.children) {
			bounding_boxes.add(scc.getBounding_box());
		}
		int max_configuration = (int) Math.pow(2, this.children.size());
		for(int i = 0; i <= max_configuration; i++) {
			String i_to_binary = Integer.toBinaryString(i);
			Configuration conf = new Configuration(new ArrayList<GeneralPath>(bounding_boxes), i_to_binary);
			conf.positionBoundingBoxes();
			conf.evalEntropy();
			if(conf.getEntropy() < best_conf.getEntropy()) {
				best_conf = conf;
			}
		}
		this.configuration = best_conf;
	}
	
	private void applyBestConfiguration() {
		String repartition = this.configuration.getRepartition();
		for(int i = 0; i < this.children.size(); i++) {
			if(repartition.charAt(i) == '1') {
				this.children.get(i).verticalFlip();
			}
		}
	}
}
