package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ConnectedComponentBP extends ConnectedComponent{
	private int span_inf;
	private int span_sup;
	
	public ConnectedComponentBP() {
		super();
		this.span_inf = -1;
		this.span_sup = -1;
	}
	
	public ConnectedComponentBP(int inf, int sup) {
		super();
		this.span_inf = inf;
		this.span_sup = sup;
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
	
	public void addNode(Node node) {
		NodeBP nodebp = (NodeBP) node;
		this.nodes.add(nodebp);
		if(this.span_inf == -1){
			this.span_inf = nodebp.getInf();
			this.span_sup = nodebp.getSup();
		}
		if(nodebp.getInf() < this.span_inf){
			this.span_inf = nodebp.getInf();
		}
		if(nodebp.getSup() > this.span_sup){
			this.span_sup = nodebp.getSup();
		}
	}
	
	public void updateSpan(){
		int min = Integer.MAX_VALUE;
		int max = 0;
		for(Node node : nodes){
			NodeBP nodebp = (NodeBP) node;
			if(nodebp.getInf() < min){
				min = nodebp.getInf();
			}
			if(nodebp.getSup() > max) {
				max = nodebp.getSup();
			}
		}
		this.span_inf = min;
		this.span_sup = max;
	}
	
	public void removeNode(Node node){
		for(Node child : node.getChildren()){
			child.getChildren().remove(node);
		}
		nodes.remove(node);
		updateSpan();
		node.setChildren(new ArrayList<Node>());
	}
	
	public boolean isIncluded(ConnectedComponent cc){
		ConnectedComponentBP ccbp = (ConnectedComponentBP) cc;
		if((this.getSpan_inf() > ccbp.getSpan_inf()) && (this.getSpan_sup() < ccbp.getSpan_sup())){
			return true;
		}
		return false;
	}
	
	public boolean isGreaterThan(ConnectedComponent cc) {
		ConnectedComponentBP ccbp = (ConnectedComponentBP) cc;
		boolean ret = false;
		if(this.getSpan_inf() > ccbp.getSpan_inf()){
			ret = true;
		}
		return ret;
	}
}
