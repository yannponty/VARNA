package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import fr.orsay.lri.varna.applications.templateEditor.Couple;
import fr.orsay.lri.varna.models.rna.ModeleBP;

public class Graph {
	private ArrayList<Node> nodes;
	private ArrayList<StronglyConnectedComponent> scc;
	private ArrayList<Node> exceptions;

	public Graph(ArrayList<ModeleBP> bps, int nb_bases) {
		this.nodes = new ArrayList<Node>();
		this.scc = new ArrayList<StronglyConnectedComponent>();
		this.exceptions = new ArrayList<Node>();
		
		HashSet<Couple<Integer,Integer>> stackedPairs = new HashSet<Couple<Integer,Integer>>(); 
		
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
			/*}
			else{
				this.exceptions.add(n);
			}*/
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
		
		this.scc.addAll(getStronglyConnectedComponents(nodes));
		ArrayList<StronglyConnectedComponent> temp = new ArrayList<StronglyConnectedComponent>();
		for(int i = 0; i < this.scc.size(); i++){
			System.out.print("-> ");
			for(Node n : this.scc.get(i).getNodes()){
				System.out.print("("+n.getInf()+","+n.getSup()+") ");
			}
			if(sccIsCyclic(this.scc.get(i))){
				for(Node n : this.scc.get(i).getNodes()){
					System.out.print("("+n.getInf()+","+n.getSup()+") "+n.getColor());
				}
				temp.addAll(getStronglyConnectedComponents(this.scc.get(i).getNodes()));
				System.out.println(true);
			}
			else{
				temp.add(this.scc.get(i));
				System.out.println(false);
			}
		}
		this.scc = temp;
		
		for(int i = 0; i < this.scc.size(); i++){
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
		
		for(StronglyConnectedComponent s : this.scc){
			s.sortNodes();
		}
		
		for(Node i : nodes){
			System.out.print("("+i.getInf()+","+i.getSup()+") : ");
			for(Node j : i.getChildren()){
				System.out.print("("+j.getInf()+","+j.getSup()+") "+j.getColor()+" ");
			}
			System.out.println();
		}
		
		this.scc.add(new Root(-1, nb_bases));
		sortScc(this.scc);
		
		Couple<Integer, ArrayList<StronglyConnectedComponent>> couple = new Couple<Integer, ArrayList<StronglyConnectedComponent>>(1, this.scc);
		StronglyConnectedComponent root = this.scc.get(0);
		while(couple.first < this.scc.size()){
			root.getChildren().add(buildTree(couple).second);
		}		
		
		for(StronglyConnectedComponent i : this.scc){
			if(i.getSpan_inf() != -1){
				System.out.print("("+i.getSpan_inf()+","+i.getSpan_sup()+") : father :");
				if(i.getFather() != null){
					System.out.print("("+i.getFather().getSpan_inf()+","+i.getFather().getSpan_sup()+")");
				}
				System.out.print(" | children : ");
				for(StronglyConnectedComponent j : i.getChildren()){
					System.out.print("("+j.getSpan_inf()+","+j.getSpan_sup()+") ");
				}
				System.out.print(" | nodes : ");
				for(Node n : i.getNodes()){
					System.out.print("("+n.getInf()+","+n.getSup()+") ");
				}
				System.out.println();
			}
		}
	}

	public ArrayList<StronglyConnectedComponent> getScc() {
		return scc;
	}

	public void setScc(ArrayList<StronglyConnectedComponent> scc) {
		this.scc = scc;
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
	
	public void sortNodes() {
		Collections.sort(this.nodes, new Comparator<Node>() {
			public int compare(Node n1, Node n2){
				int ret = 0;
				if(n1.getInf() < n2.getInf()){
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
				}
				return ret;
			}
		});
	}

	public ArrayList<StronglyConnectedComponent> getStronglyConnectedComponents(ArrayList<Node> nodes){
		ArrayList<StronglyConnectedComponent> l = new ArrayList<StronglyConnectedComponent>();
		for(Node n : nodes){
			n.setVisited(false);
		}
		for(Node n : nodes){
			if(!n.isVisited()){
				StronglyConnectedComponent s;
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
	}

	private void visitChildren(Node n, StronglyConnectedComponent s) {
		n.setVisited(true);
		s.addNode(n);
		for(Node child : n.getChildren()){
			if(!child.isVisited()){
				visitChildren(child,s);
			}
		}
	}
	
	//TODO : Gerer le cas ou le noeud a un pere de couleur 2 et tous les autres a -1
	//Solution -> Pas de solution optimale, probleme NP-Complet	
	private boolean sccIsCyclic(StronglyConnectedComponent s) {
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
	}
	
	public void sortScc(ArrayList<StronglyConnectedComponent> scc){
		Collections.sort(this.scc, new Comparator<StronglyConnectedComponent>() {
			public int compare(StronglyConnectedComponent scc1, StronglyConnectedComponent scc2){
				int ret = 0;
				if(scc1.getSpan_inf() < scc2.getSpan_inf()){
					ret = -1;
				}
				else if(scc1.getSpan_inf() > scc2.getSpan_inf()){
					ret = 1;
				}
				else if(scc1.getSpan_sup() < scc2.getSpan_sup()){
					ret = 1;
				}
				else if(scc1.getSpan_sup() > scc2.getSpan_sup()){
					ret = -1;
				}
				return ret;
			}
		});
	}
	
	private Couple<Integer,StronglyConnectedComponent> buildTree(Couple<Integer, ArrayList<StronglyConnectedComponent>>couple) {
		StronglyConnectedComponent temp = couple.second.get(couple.first);
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
		Couple<Integer,StronglyConnectedComponent> ret = new Couple<Integer,StronglyConnectedComponent>(couple.first, temp);
		return ret;
	}
}