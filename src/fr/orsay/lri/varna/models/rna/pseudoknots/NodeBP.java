package fr.orsay.lri.varna.models.rna.pseudoknots;

public class NodeBP extends Node{
	private int inf;
	private int sup;
	
	public NodeBP(int inf, int sup) {
		super();
		this.inf = inf;
		this.sup = sup;		
	}
	
	public int getInf() {
		return inf;
	}

	public void setInf(int inf) {
		this.inf = inf;
	}

	public int getSup() {
		return sup;
	}

	public void setSup(int sup) {
		this.sup = sup;
	}

	public boolean isNeighbourWith(Node node) {
		NodeBP nodeBP = (NodeBP) node;
		System.out.println("Neighbour "+this.getInf()+" "+this.getSup()+" "+nodeBP.getInf()+" "+nodeBP.getSup());
		if(this.containsBase(nodeBP.getInf()) && !this.containsBase(nodeBP.getSup())){
			return true;
		}
		else if(nodeBP.containsBase(this.inf) && !nodeBP.containsBase(this.sup)){
			return true;
		}
		else return false;
	}
	
	private boolean containsBase(int base){
		if((base >= this.inf) && (base <= this.sup)){
			return true;
		}
		return false;
	}

	public boolean isGreaterThan(Node node) {
		boolean ret = false;
		NodeBP nodeBP = (NodeBP) node;
		if(this.inf > nodeBP.getInf() || this.sup > nodeBP.getSup()) {
			ret = true;
		}
		return ret;
	}

}
