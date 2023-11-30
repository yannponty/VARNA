package fr.orsay.lri.varna.models.rna.pseudoknots;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class Configuration {
	private double entropy;
	private String repartition;
	private ArrayList<GeneralPath> bounding_boxes;
	
	DecimalFormat df = new DecimalFormat(".##");
	
	public Configuration() {
		this.entropy = Double.MAX_VALUE;
	}
	
	public Configuration(ArrayList<GeneralPath> bounding_boxes, String repartition) {
		this.bounding_boxes = bounding_boxes;
		this.repartition = repartition;
		int nb_char_left = this.bounding_boxes.size() - this.repartition.length();
		for(int i = 0; i < nb_char_left; i++) {		
			this.repartition = "0".concat(this.repartition);
		}
	}	
	
	public double getEntropy() {
		return entropy;
	}
	
	public String getRepartition() {
		return repartition;
	}

	public void positionBoundingBoxes() {
		AffineTransform  at = new AffineTransform(1.0, 0, 0, -1.0, 0, 0);
		for(int i = 0; i < this.bounding_boxes.size(); i++) {
			if(this.repartition.charAt(i) == '1') {
				this.bounding_boxes.get(i).transform(at);
			}
		}
	}
	
	public void evalEntropy() {
		double entropy = 0;
		for(int i = 0; i < this.bounding_boxes.size() - 1; i++) {
			for(int j = i + 1; j < this.bounding_boxes.size(); j++) {
				Area a1 = new Area(this.bounding_boxes.get(i));
				Area a2 = new Area(this.bounding_boxes.get(j));
				a1.intersect(a2);
				Rectangle2D rect = a1.getBounds2D();
				double rectArea =  rect.getWidth() * rect.getHeight();
				entropy += Double.parseDouble(df.format(rectArea).replace(",", "."));
			}
		}
		this.entropy = entropy;
	}
}