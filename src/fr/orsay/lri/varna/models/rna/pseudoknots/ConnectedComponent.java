package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class ConnectedComponent{	
	protected ArrayList<Node> nodes;
	protected ArrayList<ConnectedComponent> children;
	
	public ConnectedComponent(){
		this.nodes = new ArrayList<Node>();
		this.children = new ArrayList<ConnectedComponent>();
	}
	
	public ConnectedComponent(int inf, int sup){
		this.children = new ArrayList<ConnectedComponent>();
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<ConnectedComponent> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<ConnectedComponent> children) {
		this.children = children;
	}

	public boolean containsNode(Node node){
		for(Node n : this.nodes){
			if(n.equals(node)){
				return true;
			}
		}
		return false;
	}

	public abstract void addNode(Node n);
	
	public abstract void removeNode(Node node);
	
	/*public void colorNodes(){
		ArrayList<Node> q = new ArrayList<Node>();
		for(Node n : nodes){
			n.setColor(-1);
		}
		nodes.get(0).setColor(0);
		q.add(nodes.get(0));
		while(!q.isEmpty()){
			Node n = q.remove(0);
			for(Node child : n.getChildren()){
				if(child.getColor() == -1){
					child.setColor((n.getColor()+1)%2);
					q.add(child);
				}
			}
		}
	}*/
	
	public abstract boolean isIncluded(ConnectedComponent cc);
	
	public abstract boolean isGreaterThan(ConnectedComponent cc);

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
	
	public boolean hasOddCycle() {
		ArrayList<Node> queue = new ArrayList<Node>();
		for(Node n : this.nodes){
			n.setColor(-1);
		}
		boolean ret = false;
		this.nodes.get(0).setColor(0);
		for(Node n : this.nodes.get(0).getChildren()){
			queue.add(n);
		}
		while(!queue.isEmpty()){
			Node n = queue.remove(0);
			n.setColor(n.determineColor());
			if(n.getColor() == 2){
				ret = true;
			}
			for(Node child : n.getChildren()){
				if(!queue.contains(child) && child.getColor() == -1){
					queue.add(child);
				}
			}
		}
		return ret;
	}

	public ArrayList<Node> removeProblematicNodes() {
		for(Node n : this.nodes){
			n.setVisited(false);
		}
		ArrayList<Node> queue = new ArrayList<Node>();
		ArrayList<Node> toDelete = new ArrayList<Node>();
		for(Node n : this.nodes.get(0).getChildren()){
			queue.add(n);
		}
		while(!queue.isEmpty()){
			Node n = queue.remove(0);
			if(n.getColor() == 2){
				toDelete.add(n);
			}
			for(Node child : n.getChildren()){
				if(!queue.contains(child) && !n.isVisited()){
					queue.add(child);
				}
			}
		}
		for(Node n : toDelete){
			this.removeNode(n);
		}
		return toDelete;
	}
}