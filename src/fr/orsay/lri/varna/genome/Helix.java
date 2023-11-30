package fr.orsay.lri.varna.genome;

import java.awt.Color;

public class Helix {

	public int i;
	public int j;
	public int length;
	public Color color;
	
	public Helix(int ival,int jval,int lval, Color c ) {
		i = ival;
		j = jval;
		length = lval;
		color = c;
	}
	
	public BasePair getBase() {
		return new BasePair(i,j);
	}

	public BasePair getApex() {
		return new BasePair(i+length-1,j-(length-1));
	}
}
