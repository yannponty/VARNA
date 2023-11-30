package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Strand {
	private int strand_num;
	private ArrayList<Element> elements;
	
	public Strand(int n){
		this.strand_num = n;
		this.elements = new ArrayList<Element>();
	}
	
	public int getStrand_num() {
		return strand_num;
	}
	public void setStrand_num(int strand_num) {
		this.strand_num = strand_num;
	}
	public ArrayList<Element> getElements() {
		return elements;
	}
	public void setElementss(ArrayList<Element> elements) {
		this.elements = elements;
	}
	
	public void sortBPCREs(){
		Collections.sort(this.elements, new Comparator<Element>(){
			public int compare(Element n1, Element n2){if(n1.getBoundOnStrand(strand_num) < n2.getBoundOnStrand(strand_num)){
					if(strand_num%2 == 0){
						return 1;
					}
					else{
						return -1;
					}
				}
				else if(n1.getBoundOnStrand(strand_num) > n2.getBoundOnStrand(strand_num)){
					if(strand_num%2 == 0){
						return -1;
					}
					else{
						return 1;
					}
				}
				else{
					return 0;
				}
			}
			
		});
		for(Element n : elements){
			System.out.println(n.getElementInf()+" "+n.getElementSup());
		}
	}
	
	public void buildRelationBetweenBPCREInStrand(){
		for(int i = 0; i < this.elements.size()-1; i++){
			this.elements.get(i).getDraw_children().add(this.elements.get(i+1));
			this.elements.get(i+1).getDraw_fathers().add(this.elements.get(i));
		}
	}
	
	public double getMaxHeightRe(){
		double max = 0;
		for(Element n : this.elements){
			if(n instanceof RecursiveElement){
				Rectangle2D rectangle = ((RecursiveElement) n).getBounding_box().getBounds2D();
				max = Math.max(rectangle.getHeight(), max);
			}
		}
		return max;
	}
}

/*public class Strand {
	private int strand_num;
	private ArrayList<Node> nodes;
	
	public Strand(int n){
		this.strand_num = n;
		this.nodes = new ArrayList<Node>();
	}
	
	public int getStrand_num() {
		return strand_num;
	}
	public void setStrand_num(int strand_num) {
		this.strand_num = strand_num;
	}
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void sortNodes(){
		Collections.sort(this.nodes, new Comparator<Node>(){
			public int compare(Node n1, Node n2) {
				if(n1.getBoundOnStrand(strand_num) < n2.getBoundOnStrand(strand_num)){
					if(strand_num%2 == 0){
						return 1;
					}
					else{
						return -1;
					}
				}
				else if(n1.getBoundOnStrand(strand_num) > n2.getBoundOnStrand(strand_num)){
					if(strand_num%2 == 0){
						return -1;
					}
					else{
						return 1;
					}
				}
				else{
					return 0;
				}
			}
			
		});
		for(Node n : nodes){
			System.out.println(n.getBasePair().getIndex5()+" "+n.getBasePair().getIndex3());
		}
	}
	
	public void buildRelationBetweenNodeInStrand(){
		for(int i = 0; i < this.nodes.size()-1; i++){
			this.nodes.get(i).getDraw_children().add(this.nodes.get(i+1));
			this.nodes.get(i+1).getDraw_fathers().add(this.nodes.get(i));
		}
	}
}*/