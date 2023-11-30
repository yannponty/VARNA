package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.orsay.lri.varna.applications.templateEditor.Couple;
import fr.orsay.lri.varna.models.rna.ModeleBP;

public class Graph {
	private ArrayList<Node> nodes;
	private ArrayList<ConnectedComponent> cc;
	private ArrayList<Node> exceptions;
	private AbstractFactoryCC factoryCC;

	
	
	public Graph(ArrayList<Node> nodes, AbstractFactoryCC factoryCC) {
		this.nodes = nodes;
		this.cc = new ArrayList<ConnectedComponent>();
		this.exceptions = new ArrayList<Node>();
		this.factoryCC = factoryCC;
	}
	public Graph(ArrayList<ModeleBP> bps, int nb_bases) {
		this.nodes = new ArrayList<Node>();
		this.cc = new ArrayList<ConnectedComponent>();
		this.exceptions = new ArrayList<Node>();
		
		/*HashSet<Couple<Integer,Integer>> stackedPairs = new HashSet<Couple<Integer,Integer>>(); 

	for(ModeleBP bp : bps){
		int i = bp.getIndex5();
		int j = bp.getIndex3();
		stackedPairs.add(new Couple<Integer,Integer>(i+1,j-1));
		stackedPairs.add(new Couple<Integer,Integer>(i-1,j+1));
	}
	
	for(ModeleBP bp : bps){
		int i = bp.getIndex5();
		int j = bp.getIndex3();
		Node n = new Node(i, j);
		//if (stackedPairs.contains(new Couple<Integer,Integer>(i,j))){
			this.nodes.add(n);
		}
		else{
			this.exceptions.add(n);
		}
	}
	
	for(Node i : nodes){
		for(Node j : nodes){
			if(!i.equals(j) && i.intersection(j) && !i.containsChild(j)){
				i.addChild(j);
				j.addChild(i);
			}
		}
	}
	
	
	
	this.sortNodes();
	
	for(Node i : nodes){
		System.out.print("("+i.getInf()+","+i.getSup()+") : ");
		for(Node j : i.getChildren()){
			System.out.print("("+j.getInf()+","+j.getSup()+") ");
		}
		System.out.println();
	}
	
	this.cc.addAll(getStronglyConnectedComponents(nodes));
	ArrayList<ConnectedComponent> temp = new ArrayList<ConnectedComponent>();
	for(int i = 0; i < this.cc.size(); i++){
		System.out.print("-> ");
		for(Node n : this.cc.get(i).getNodes()){
			System.out.print("("+n.getInf()+","+n.getSup()+") ");
		}
		if(this.cc.get(i).hasOddCycle()){
			for(Node n : this.cc.get(i).getNodes()){
				System.out.print("("+n.getInf()+","+n.getSup()+") "+n.getColor());
			}
			temp.addAll(getStronglyConnectedComponents(this.cc.get(i).getNodes()));
			System.out.println(true);
		}
		else{
			temp.add(this.cc.get(i));
			System.out.println(false);
		}
	}
	this.cc = temp;
	
	for(int i = 0; i < this.cc.size(); i++){
		System.out.print("--> ");
		for(Node n : temp.get(i).getNodes()){
			System.out.print("("+n.getInf()+","+n.getSup()+") "+n.getColor());
		}
		System.out.println();
	}
	
	for(Node n : exceptions){
		System.out.print("---> ");
		System.out.println("("+n.getInf()+","+n.getSup()+") ");
	}		
	
	for(ConnectedComponent s : this.cc){
		s.sortNodes();
	}
	
	for(Node i : nodes){
		System.out.print("("+i.getInf()+","+i.getSup()+") : ");
		for(Node j : i.getChildren()){
			System.out.print("("+j.getInf()+","+j.getSup()+") "+j.getColor()+" ");
		}
		System.out.println();
	}
	
	this.cc.add(new Root(-1, nb_bases));
	sortCc(this.cc);
	
	Couple<Integer, ArrayList<ConnectedComponent>> couple = new Couple<Integer, ArrayList<ConnectedComponent>>(1, this.cc);
	ConnectedComponent root = this.cc.get(0);
	while(couple.first < this.cc.size()){
		root.getChildren().add(buildTree(couple).second);
	}		
	
	for(ConnectedComponent i : this.cc){
		if(i.getSpan_inf() != -1){
			System.out.print("("+i.getSpan_inf()+","+i.getSpan_sup()+") : children :");
			for(ConnectedComponent j : i.getChildren()){
				System.out.print("("+j.getSpan_inf()+","+j.getSpan_sup()+") ");
			}
			System.out.print(" | nodes : ");
			for(Node n : i.getNodes()){
				System.out.print("("+n.getInf()+","+n.getSup()+") ");
			}
			System.out.println();
		}
	}*/
	}
	
	public ArrayList<ConnectedComponent> getCc() {
		return cc;
	}
	
	public void setCc(ArrayList<ConnectedComponent> cc) {
		this.cc = cc;
	}
	
	public ArrayList<Node> getExceptions() {
		return exceptions;
	}
	
	public void setExceptions(ArrayList<Node> exceptions) {
		this.exceptions = exceptions;
	}
	
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void buildRelationshipNodes() {
		for(Node i : this.nodes){
			for(Node j : this.nodes){
				if(!i.equals(j) && i.isNeighbourWith(j) && !i.containsChild(j)){
					System.out.println("TRUE");
					i.addChild(j);
					j.addChild(i);
				}
			}
		}
	}
	
	public void sortNodes() {
		Collections.sort(this.nodes, new Comparator<Node>() {
			public int compare(Node n1, Node n2){
				int ret = 0;
				if(n1.isGreaterThan(n2)) {
					ret = 1;
				}
				else {
					ret = -1;
				}
				/*if(n1.getInf() < n2.getInf()){
			ret = -1;
		}
		else if(n1.getInf() > n2.getInf()){
			ret = 1;
		}
		else if(n1.getSup() < n2.getSup()){
			ret = -1;
		}
		else if(n1.getSup() > n2.getSup()){
			ret = 1;
		}*/
				return ret;
			}
		});
	}
	
	/*public ArrayList<ConnectedComponent> getStronglyConnectedComponents(ArrayList<Node> nodes){
		ArrayList<ConnectedComponent> l = new ArrayList<ConnectedComponent>();
		for(Node n : nodes){
			n.setVisited(false);
		}
		for(Node n : nodes){
			if(!n.isVisited()){
				ConnectedComponent s;
				if(n.getChildren().isEmpty()) {
					s = new BasePair();
				}
				else {
					s = new PseudoKnot();
				}
				visitChildren(n,s);
				l.add(s);
			}
		}
		return l;
	}*/
	
	public ArrayList<ConnectedComponent> getStronglyConnectedComponents(ArrayList<Node> nodes){
		ArrayList<ConnectedComponent> l = new ArrayList<ConnectedComponent>();
		for(Node n : nodes){
			n.setVisited(false);
		}
		for(Node n : nodes){
			if(!n.isVisited()){
				ConnectedComponent s = this.factoryCC.buildCC();
				visitChildren(n,s);
				l.add(s);
			}
		}
		return l;
	}
	
	/*private ConnectedComponent getWellTypedCC(String type) {
		if(type == "BP") {
			return new ConnectedComponentBP();
		}
		else if(type == "Area") {
			return null;
		}
		else {
			return null;
		}
	}*/
	
	private void visitChildren(Node n, ConnectedComponent s) {
		n.setVisited(true);
		s.addNode(n);
		for(Node child : n.getChildren()){
			if(!child.isVisited()){
				visitChildren(child,s);
			}
		}
	}
	
	/*private boolean ccIsCyclic(ConnectedComponent s) {
		if(s.getNodes().isEmpty()){
			return false;
		}
		ArrayList<Node> queue = new ArrayList<Node>();
		for(Node n : s.getNodes()){
			n.setColor(-1);
		}
		boolean ret = false;
		ArrayList<Node> toDelete = new ArrayList<Node>();
		s.getNodes().get(0).setColor(0);
		for(Node n : s.getNodes().get(0).getChildren()){
			queue.add(n);
		}
		while(!queue.isEmpty()){
			Node n = queue.remove(0);
			n.setColor(n.determineColor());
			if(n.getColor() == 2){
				toDelete.add(n);
				this.exceptions.add(n);
				ret = true;
			}
			for(Node child : n.getChildren()){
				if(!queue.contains(child) && child.getColor() == -1){
					queue.add(child);
				}
			}
		}
		for(Node n : toDelete){
			s.removeNode(n);
		}
		return ret;
	}*/
	
	public void removeOddCycles() {
		ArrayList<ConnectedComponent> temp = new ArrayList<ConnectedComponent>();
		for(ConnectedComponent cc : this.cc) {
			if(cc.hasOddCycle()) {
				this.exceptions.addAll(cc.removeProblematicNodes());
				temp.addAll(getStronglyConnectedComponents(cc.getNodes()));
			}
			else {
				temp.add(cc);
			}
		}
		this.cc = temp;
	}
	
	public void sortCc(ArrayList<ConnectedComponent> cc){
		Collections.sort(this.cc, new Comparator<ConnectedComponent>() {
			public int compare(ConnectedComponent cc1, ConnectedComponent cc2){
				int ret = 0;
				if(cc1.isGreaterThan(cc2)){
					ret = 1;
				}
				else {
					ret = -1;
				}
				return ret;
			}
		});
	}
	
	private Couple<Integer,ConnectedComponent> buildTree(Couple<Integer, ArrayList<ConnectedComponent>>couple) {
		ConnectedComponent temp = couple.second.get(couple.first);
		boolean over = false;
		couple.first++;
		while(!over){
			if(couple.first >= couple.second.size()){
				over = true;
			}
			else if(couple.second.get(couple.first).isIncluded(temp)){
				temp.getChildren().add(buildTree(couple).second);
			}
			else{
				over = true;
			}
		}
		Couple<Integer,ConnectedComponent> ret = new Couple<Integer,ConnectedComponent>(couple.first, temp);
		return ret;
	}
	
	public void calculateConnectedComponents() {
		ArrayList<ConnectedComponent> l = getStronglyConnectedComponents(this.nodes);
		this.sortCc(l);
		this.cc = l;
	}
	
	public void buildCCTree() {
		sortCc(this.cc);
		/*for(ConnectedComponent cc : this.getCc()) {
			ConnectedComponentBP ccbp = (ConnectedComponentBP) cc;
			System.out.println("CC "+ccbp.getSpan_inf());
		}*/
		Couple<Integer, ArrayList<ConnectedComponent>> couple = new Couple<Integer, ArrayList<ConnectedComponent>>(1, this.cc);
		ConnectedComponent root = this.cc.get(0);
		while(couple.first < this.cc.size()){
			root.getChildren().add(buildTree(couple).second);
		}		
		
	}
}
