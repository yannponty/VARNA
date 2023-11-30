package fr.orsay.lri.varna.genome;

import fr.orsay.lri.varna.applications.templateEditor.Couple;

public class BasePair {
	public int i;
	public int j;
	private static final int HASH_PRIME = 1000003;
	
	public BasePair(int p5, int p3) {
		i = p5;
		j = p3;
	}
	
	public boolean equals( Object c)
	{
		if (!(c instanceof BasePair))
		{
			return false;
		}
		BasePair cc = (BasePair) c; 
		return ((cc.i == i) && (cc.j == j));
	}

	public int hashCode()
	{
		return HASH_PRIME*i+j;
	}
	
	public String toString()
	{
		return "("+i+","+j+")";
	}
}
