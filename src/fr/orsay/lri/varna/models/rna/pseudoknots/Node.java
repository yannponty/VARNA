package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.util.ArrayList;

public abstract class Node {
	private ArrayList<Node> children;
	private int color;
	private boolean visited;
	
	public Node() {
		this.children = new ArrayList<Node>();
		this.color = -1;
		this.visited = false;		
	}
	
	public Node(int inf, int sup){
		super();
		this.children = new ArrayList<Node>();
		this.color = -1;
		this.visited = false;
	}
	
	public Node(int inf, int sup, int color) {
		super();
	}

	public ArrayList<Node> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Node> children) {
		this.children = children;
	}
	
	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public boolean containsChild(Node node){
		for(Node n : this.children){
			if(n.equals(node)) return true;
		}
		return false;
	}
	
	public void addChild(Node node){
		this.children.add(node);
	}

	public int determineColor() {
		boolean gotFather0 = false;
		boolean gotFather1 = false;
		boolean gotFather2 = false;
		int color = -1;
		for(Node n : children){
			if(n.getColor() == 0){
				gotFather0 = true;
			}
			else if(n.getColor() == 1){
				gotFather1 = true;
			}
			else if(n.getColor() == 2){
				gotFather2 = true;
			}
		}
		if(gotFather0 && gotFather1){
			color = 2;
		}
		else if(gotFather0){
			color = 1;
		}
		else if(gotFather1){
			color = 0;
		}
		else if(gotFather2){
			color = 0;
		}
		return color;
	}
	
	public abstract boolean isNeighbourWith(Node node);
	public abstract boolean isGreaterThan(Node node);
}
