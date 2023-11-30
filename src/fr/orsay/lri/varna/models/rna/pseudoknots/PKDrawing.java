package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

import fr.orsay.lri.varna.applications.templateEditor.Couple;
import fr.orsay.lri.varna.models.rna.ModeleBP;

public class PKDrawing {
	private ArrayList<ModeleBP> bps;
	private int nb_bases;
	private ArrayList<Couple<Integer,Point2D.Double>> points;
	private ArrayList<Couple<Integer,Point2D.Double>> centers;
	private Graph graph;
	private RecursiveElement recursive_element;
	
	public PKDrawing(ArrayList<ModeleBP> bps, int nb_bases) {
		this.bps = bps;
		this.nb_bases = nb_bases;
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



	public Graph getGraph() {
		return graph;
	}



	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public void calculatePointsAndCenters() {
		Graph graph = this.buildGraph();
		graph.calculateConnectedComponents();
		graph.removeOddCycles();
		graph.getCc().add(new ConnectedComponentBP(-1, nb_bases));
		graph.buildCCTree();
		/*for(ConnectedComponent cc : graph.getCc()) {
			ConnectedComponentBP ccbp = (ConnectedComponentBP) cc;
			System.out.println("CC "+ccbp.getSpan_inf());
		}*/
		this.recursive_element = this.CCtoRecursiveElement((ConnectedComponentBP) graph.getCc().get(0));
		/*ArrayList<RecursiveElement> queue = new ArrayList<RecursiveElement>();
		queue.add(this.recursive_element);
		while(!queue.isEmpty()){
			RecursiveElement re = queue.remove(0);
			System.out.println("RE : "+re.getSpan_inf()+" "+re.getSpan_sup());
			System.out.print("CHILDREN : ");
			for(RecursiveElement child : re.getChildren()) {
				queue.add(child);
				System.out.print(child.getSpan_inf()+" "+child.getSpan_sup()+" ");
			}
			System.out.println();
		}*/
		this.calculateCoords(recursive_element);
		this.recursive_element.verticalFlip();
		this.points = new ArrayList<Couple<Integer,Point2D.Double>>(this.recursive_element.getPoints());
		this.centers = new ArrayList<Couple<Integer,Point2D.Double>>(this.recursive_element.getCenters());
	}

	private Graph buildGraph() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		HashSet<Couple<Integer,Integer>> stackedPairs = new HashSet<Couple<Integer,Integer>>(); 
		
		for(ModeleBP bp : this.bps){
			int i = bp.getIndex5();
			int j = bp.getIndex3();
			stackedPairs.add(new Couple<Integer,Integer>(i+1,j-1));
			stackedPairs.add(new Couple<Integer,Integer>(i-1,j+1));
		}
		
		for(ModeleBP bp : this.bps){
			int i = bp.getIndex5();
			int j = bp.getIndex3();
			NodeBP n = new NodeBP(i,j);
			//if (stackedPairs.contains(new Couple<Integer,Integer>(i,j))){
				nodes.add(n);
			/*}
			else{
				this.exceptions.add(n);
			}*/
		}
		FactoryCCBP fccbp = new FactoryCCBP();
		graph = new Graph(nodes, fccbp);
		graph.buildRelationshipNodes();
		graph.sortNodes();
		/*for(Node i : graph.getNodes()){
			NodeBP nodeBP1 = (NodeBP) i;
			System.out.print("("+nodeBP1.getInf()+","+nodeBP1.getSup()+") : ");
			for(Node j : i.getChildren()){
				NodeBP nodeBP2 = (NodeBP) j;
				System.out.print("("+nodeBP2.getInf()+","+nodeBP2.getSup()+") ");
			}
			System.out.println();
		}*/
		return graph;
	}

	private RecursiveElement CCtoRecursiveElement(ConnectedComponentBP ccBP) {
		ArrayList<BPConstitutingRE> bpcres = this.nodesToBPConstitutingRE(ccBP.getNodes());
		RecursiveElement re = null;
		if(ccBP.getSpan_inf() == -1) {
			re = new Root(ccBP.getSpan_inf(), ccBP.getSpan_sup(), bpcres);
		}
		else if(ccBP.getNodes().size() == 1) {
			re = new BasePair(ccBP.getSpan_inf(), ccBP.getSpan_sup(), bpcres);
		}
		else {
			re = new PseudoKnot(ccBP.getSpan_inf(), ccBP.getSpan_sup(), bpcres);
		}
		for(ConnectedComponent child : ccBP.getChildren()) {
			ConnectedComponentBP childBP = (ConnectedComponentBP) child;
			re.getChildren().add(CCtoRecursiveElement(childBP));
		}
		return re;
		
		/*ArrayList<RecursiveElement> recursive_elements = new ArrayList<RecursiveElement>();
		ArrayList<ConnectedComponent> queue = new ArrayList<ConnectedComponent>();
		queue.add(scc.get(0));
		while(!queue.isEmpty()) {
			ConnectedComponent scc = queue.remove(0);
			ArrayList<BPConstitutingRE> bpcres = this.nodesToBPConstitutingRE(scc.getNodes());
			RecursiveElement re =
		}
		return null;*/
	}

	private ArrayList<BPConstitutingRE> nodesToBPConstitutingRE(ArrayList<Node> nodes) {
		ArrayList<BPConstitutingRE> bpcres = new ArrayList<BPConstitutingRE>();
		for(Node node : nodes) {
			NodeBP nodeBP = (NodeBP) node;
			//System.out.println("AZ "+nodeBP.getInf()+" "+nodeBP.getSup()+" "+nodeBP.getColor());
			BPConstitutingRE bpcre = new BPConstitutingRE(nodeBP.getInf(), nodeBP.getSup(), nodeBP.getColor());
			bpcres.add(bpcre);
		}
		return bpcres;
	}

	private void calculateCoords(RecursiveElement re1) {
		for(RecursiveElement re2 : re1.getChildren()) {
			this.calculateCoords(re2);
		}
		re1.assignCoords();
	}
}
