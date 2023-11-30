package fr.orsay.lri.varna.genome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Annotation {
	
	String chr="N/A";
	long from=-1; 
	long to=-1; 
	String title=""; 
	double score=0.;
	
	public Annotation(String line) {
		String [] data = line.split("\t");
		if (data.length>2) {
			chr = data[0];
			from = Integer.parseInt(data[1]);
			to = Integer.parseInt(data[2]);
		}
		if (data.length>3) {
			title = data[3];
		}
		if (data.length>4) {
			score = Double.parseDouble(data[4]);
		}
		
	}
	
	public String toString() {
		return ""+chr+"\t"+from+"\t"+to+"\t"+title+"\t"+score;
	}
	
	
	public static final String BED_HEADER = "track type";
	
	public static ArrayList<Annotation> loadBED(File f) {
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					String s = line.trim();
					if (!s.startsWith(BED_HEADER)) {
						result.add(new Annotation(s));
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return result;

	}

}
