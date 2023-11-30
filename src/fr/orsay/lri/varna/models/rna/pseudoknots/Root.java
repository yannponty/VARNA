package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class Root extends RecursiveElement{
	private Strand strand;
	private String repartition;
	
	public Root(int span_inf, int span_sup){
		super();
		this.span_inf = span_inf;
		this.span_sup = span_sup;
		this.children = new ArrayList<RecursiveElement>();
		this.bounding_box = new GeneralPath();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>();
		this.element = new ArrayList<Element>();
	}
	
	public Root(int span_inf, int span_sup, ArrayList<BPConstitutingRE> bpcres) {
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
		this.findBestRepartition();
		this.applyBestRepartition();
		this.assignPointsAndCentersElementsFromStrand();
		this.buildBoundingBox();
	}

	private void buildRootStrand(){
		Strand s = new Strand(0);
		for(int i = 0; i < this.span_sup; i++){
			RecursiveElement re = this.getCorrespondingRe(i);
			if(re == null){
				BPConstitutingRE n = new BPConstitutingRE(i,i,1,1);
				s.getElements().add(n);
				this.element.add(n);
			}
			else {
				s.getElements().add(re);
				i = re.span_sup;
				this.element.add(re);
				re.setStrand(1);
				//re.initializeDistances();
			}
		}
		s.sortBPCREs();
		s.buildRelationBetweenBPCREInStrand();
		this.strand = s;
	}
	
	/*public void setDrawYs(){
		ArrayList<Element> visited = new ArrayList<Element>();
		ArrayList<Element> queue = new ArrayList<Element>();
		for(Element n : this.element){
			if(n.getDraw_fathers().isEmpty()){
				if(n instanceof BPConstitutingRE){
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
		
		ArrayList<Double> maxHeightsLastRes = new ArrayList<Double>();
		for(Strand s : this.strands){
			maxHeightsLastRes.add(-1.0);
		}
		
		while(!queue.isEmpty()){
			Element n = queue.remove(0);
			if(!visited.containsAll(n.getDraw_fathers())){
				queue.add(n);
			}
			else{
				double max = 0;
				if(n instanceof BPConstitutingRE){
					for(Element father : n.getDraw_fathers()){
						double minimum_height = 0;
						if(father instanceof BPConstitutingRE){
							double y_father;
							if(((BPConstitutingRE) father).getStrand_inf() == ((BPConstitutingRE)n).getStrand_inf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((BPConstitutingRE) n).updateDistanceAbove(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else if(((BPConstitutingRE) father).getStrand_sup() == ((BPConstitutingRE)n).getStrand_inf()){
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((BPConstitutingRE) n).updateDistanceAbove(Math.abs(father.getDraw_sup().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else if(((BPConstitutingRE) father).getStrand_inf() == ((BPConstitutingRE)n).getStrand_sup()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((BPConstitutingRE) n).updateDistanceAboveSup(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_supY(minimum_height);
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + Element.SPACE_BETWEEN_BASES;
								((BPConstitutingRE) n).updateDistanceAboveSup(Math.abs(father.getDraw_sup().getY() + father.getDistance_above()) - minimum_height);
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
								((BPConstitutingRE) n).updateDistanceAbove(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_infY(minimum_height);
							}
							else{
								((BPConstitutingRE) n).updateDistanceAboveSup(Math.abs(father.getDraw_inf().getY() + father.getDistance_above()) - minimum_height);
								n.setDraw_supY(minimum_height);
							}
						}
						max = Math.max(max, minimum_height);
					}
					if(max == n.getDraw_inf().getY()){
						((BPConstitutingRE) n).updateDistanceAboveSup(n.getDistance_above() - (max - n.getDraw_sup().getY()));
						n.setDraw_supY(max);				
					}
					else {
						((BPConstitutingRE) n).updateDistanceAbove(n.getDistance_above() - (max - n.getDraw_inf().getY()));
						n.setDraw_infY(max);
					}
				}
				else {
					for(Element father : n.getDraw_fathers()){
						double minimum_height = 0;
						if(father instanceof BPConstitutingRE){
							int n_strand = ((RecursiveElement) n).getStrand();
							double y_father;
							if(father.getBoundOnStrand(n_strand) == father.getElementInf()){
								y_father = father.getDraw_inf().getY();
								minimum_height = y_father + father.getDistance_above();
							}
							else {
								y_father = father.getDraw_sup().getY();
								minimum_height = y_father + ((BPConstitutingRE) father).getDistance_above_sup();						
							}
							if(maxHeightsLastRes.get(n_strand-1) != -1){
								double minHeightRe = minimum_height - n.getDistance_beneath();
								double diff_height = maxHeightsLastRes.get(n_strand-1) - minHeightRe;
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
					int cur_strand_num = ((RecursiveElement) n).getStrand();
					if(cur_strand_num%2 == 0){
						n.setDraw_infY(max + ((RecursiveElement) n).getDistanceBetweenExtrema()); 
						n.setDraw_supY(max);
					}
					else {
						n.setDraw_infY(max); 
						n.setDraw_supY(max + ((RecursiveElement) n).getDistanceBetweenExtrema());
					}
					maxHeightsLastRes.set(cur_strand_num-1, n.getDraw_inf().getY() + n.getDistance_above());
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
		
		for(RecursiveElement re : this.children){
			for(int i = 1; i <= re.getPoints().size()-1; i++){
				int index = re.span_inf + i;
				this.points.set(index,re.getPoints().get(i));
				this.centers.set(index,re.getCenters().get(i));
			}
		}
		this.sortPoints();
		this.sortCenters();
	}
	
	private void positionChildrenAroundStrand() {
		for(RecursiveElement re : this.children){
			Couple<Point2D.Double, Point2D.Double> c = re.getFirstAndLastPoints();
			ArrayList<Element> strand_elements = this.strand.getElements();
			for(int i = 0; i < strand_elements.size(); i++){
				if(re.getSpan_inf() == strand_elements.get(i).getElementInf()){	
					double x = strand_elements.get(i).getDraw_inf().getX() - c.first.getX();
					double y = strand_elements.get(i).getDraw_inf().getY() - c.first.getY();
					Point2D.Double translation_vector = new Point2D.Double(x, y);
					re.translate(translation_vector);				
				}
			}
			/*for(int j = 1; j <= re.getPoints().size()-1; j++){
				int index = re.span_inf + j;
				this.points.set(index,re.getPoints().get(j));
				this.centers.set(index,re.getCenters().get(j));
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
		if(e instanceof BPConstitutingRE) {
			e.setDraw_infX(0.);
			e.setDraw_supX(0.);			
		}
		else {
			e.setDraw_infX(0.);
			e.setDraw_supX(((RecursiveElement) e).getDistanceBetweenExtrema());	
		}		
		for(int i = 1; i < this.element.size(); i++) {
			e = this.element.get(i);
			double x_father = e.getDraw_fathers().get(0).getDraw_sup().getX();
			e.setDraw_infX(x_father + Element.SPACE_BETWEEN_BASES);
			if(e instanceof BPConstitutingRE){
				e.setDraw_supX(x_father + Element.SPACE_BETWEEN_BASES);
			}
			else {
				e.setDraw_supX(x_father + Element.SPACE_BETWEEN_BASES + ((RecursiveElement) e).getDistanceBetweenExtrema());
			}
		}
	}
	
	private void findBestRepartition() {
		Graph graph = this.buildGraph();
		graph.calculateConnectedComponents();
		char repartition[] = new char[this.children.size()];
		for(ConnectedComponent cc : graph.getCc()) {
			ConnectedComponentArea cca = (ConnectedComponentArea) cc;
			if(cc.hasOddCycle()) {
				cca.sortNodes();
				cca.retrieveBounding_boxes();
				Configuration best_conf = this.findBestConfiguration(cca);
				for(int i = 0; i < best_conf.getRepartition().length(); i++) {
					int position = ((NodeArea)cca.getNodes().get(i)).getPosition();
					repartition[position] = best_conf.getRepartition().charAt(i);
				}
			}
			else {
				for(Node n : cc.getNodes()) {
					NodeArea na = (NodeArea) n;
					repartition[na.getPosition()] = Character.forDigit(na.getColor(), 10);
				}
			}
		}
		this.repartition = new String(repartition);
	}
	
	private Configuration findBestConfiguration(ConnectedComponentArea cca) {
		Configuration best_conf = new Configuration();
		int max_configuration = (int) Math.pow(2, cca.getNodes().size());
		for(int i = 0; i < max_configuration; i++) {
			ArrayList<GeneralPath> bounding_boxes = new ArrayList<GeneralPath>();
			for(GeneralPath bb : cca.getBounding_boxes()) {
				GeneralPath gp = new GeneralPath(bb);
				bounding_boxes.add(gp);
			}
			String i_to_binary = Integer.toBinaryString(i);
			Configuration conf = new Configuration(bounding_boxes, i_to_binary);
			conf.positionBoundingBoxes();
			conf.evalEntropy();
			System.out.println("ENTROPY "+conf.getRepartition()+" "+conf.getEntropy());
			if(conf.getEntropy() < best_conf.getEntropy()) {
				best_conf = conf;
			}
		}
		return best_conf;
	}
	
	private void applyBestRepartition() {
		System.out.println(repartition);
		for(int i = 0; i < this.children.size(); i++) {
			if(this.repartition.charAt(i) == '1') {
				this.children.get(i).verticalFlip();
			}
		}
	}
	
/*	private void findBestConfiguration() {
		Configuration best_conf = new Configuration();
		ArrayList<GeneralPath> bounding_boxes = new ArrayList<GeneralPath>();
		for(RecursiveElement re : this.children) {
			bounding_boxes.add(re.getBounding_box());
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
	}*/

	private Graph buildGraph() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for(int i = 0; i < this.children.size(); i++) {
			RecursiveElement re = this.children.get(i);
			NodeArea na = new NodeArea(re.getBounding_box(), i);
			nodes.add(na);
		}
		FactoryCCArea fcca = new FactoryCCArea();
		Graph graph = new Graph(nodes, fcca);
		graph.buildRelationshipNodes();
		graph.sortNodes();
		return graph;
	}
}
