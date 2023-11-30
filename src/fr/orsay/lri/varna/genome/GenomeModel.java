package fr.orsay.lri.varna.genome;

import fr.orsay.lri.varna.applications.templateEditor.Couple;
import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.models.export.SwingGraphics;
import fr.orsay.lri.varna.models.export.VueVARNAGraphics;
import fr.orsay.lri.varna.models.rna.RNA;
import fr.orsay.lri.varna.models.rna.VARNAPoint;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GenomeModel {
	private char[] _seq;
	private int[] _str;
	private ArrayList<BasePair> _aux;
	private ArrayList<BasePair> _pks;
	private double[] _values;
	private VARNAPoint[] _pos;
	private VARNAPoint[] _centers;
	private VARNAPoint[] _backupPos;
	private VARNAPoint[] _backupCenters;
	private int _maxBPSpan = 400;
	private HashMap<BasePair, Couple<Boolean, Double>> _auxBPLayout = new HashMap<BasePair, Couple<Boolean, Double>>();
	private String _description = "";

	private static double MIN_LOOP_HEIGHT = 4 * RNA.LOOP_DISTANCE;

	public GenomeModel() {
		_seq = new char[0];
		_pos = new VARNAPoint[0];
		_backupPos = new VARNAPoint[0];
		_backupCenters = new VARNAPoint[0];
		_centers = new VARNAPoint[0];
		_str = new int[0];
		_aux = new ArrayList<BasePair>();
		_pks = new ArrayList<BasePair>();
		_values = new double[0];
		doInitialLayout();
		adaptToMaxBPSpan();
	}

	public GenomeModel(String seq, String dbn) {
		this(seq, dbn, "");
	}

	public GenomeModel(String seq, String dbn, String description) {
		_seq = seq.toCharArray();
		Couple<int[], ArrayList<BasePair>> ss = parseDBN(dbn);
		_description = description;
		_str = ss.first;
		_aux = new ArrayList<BasePair>();
		_values = new double[_seq.length];
		_pks = ss.second;
		for (int i = 0; i < _values.length; i++) {
			_values[i] = -999.;
		}
		doInitialLayout();
		adaptToMaxBPSpan();
	}

	public GenomeModel(String seq, int[] ss, ArrayList<BasePair> pkbps) {
		this(seq, ss, pkbps, "");
	}

	public GenomeModel(String seq, int[] ss, ArrayList<BasePair> bpaux, String description) {
		_seq = seq.toCharArray();
		_str = ss;
		_description = description;
		_aux = new ArrayList<BasePair>();
		_values = new double[_seq.length];
		_pks = bpaux;
		for (int i = 0; i < _values.length; i++) {
			_values[i] = -999.;
		}
		doInitialLayout();
		adaptToMaxBPSpan();
	}

	public void exportRNA(String path) {
		File fout = new File(path);
		try {
			FileWriter out = new FileWriter(fout);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getDescription() {
		return _description;
	}

	public void setMaxBPSpan(int nm) {
		this._maxBPSpan = nm;
		this.adaptToMaxBPSpan();
	}

	public int getMaxBPSpan() {
		return _maxBPSpan;
	}

	public int getLength() {
		return _pos.length;
	}

	public void setValue(int index, double value) {
		_values[index] = value;
	}

	public double getValue(int index) {
		return _values[index];
	}

	public void setValues(double[] values) {
		for (int i = 0; i < values.length; i++) {
			_values[i] = values[i];
		}
	}

	public double[] getValues() {
		double[] res = new double[this._seq.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = _values[i];
		}
		return res;
	}

	public void setSequence(String s) {
		while (s.length() < _seq.length) {
			s += '-';
		}
		if (s.length() > _seq.length) {
			s = s.substring(0, _seq.length);
		}
		this._seq = s.toCharArray();
	}

	private RNA exportRange(int i, int j) throws ExceptionUnmatchedClosingParentheses, ExceptionFileFormatOrSyntax {
		String seq = "";
		String dbn = "";
		for (int a = i; a <= j; a++) {
			seq += _seq[a];
			int k = _str[a];
			if (k == -1) {
				dbn += ".";
			} else if (k > a) {
				dbn += "(";
			} else {
				dbn += ")";
			}
		}
		RNA result = new RNA();
		result.setRNA(seq, dbn);
		return result;
	}

	private void doInitialLayout() {
		int i = 0;
		double x = 0.;
		_pos = new VARNAPoint[_str.length];
		_centers = new VARNAPoint[_str.length];
		_backupPos = new VARNAPoint[_str.length];
		_backupCenters = new VARNAPoint[_str.length];
		boolean flip = false;

		while (i < _str.length) {
			_pos[i] = new VARNAPoint(x, 0.);
			_backupPos[i] = new VARNAPoint(x, 0.);
			_centers[i] = new VARNAPoint(x, -10.);
			_backupCenters[i] = new VARNAPoint(x, -10.);
			int j = _str[i];
			if (j > i) {
				try {
					RNA r = exportRange(i, j);
					r.drawRNARadiate();
					if (flip) {
						r.flipHelix(new Point(0, r.getSize() - 1));
					}

					Point2D.Double base = new Point2D.Double(r.getBaseAt(0).getCoords().x,
							r.getBaseAt(0).getCoords().y);
					for (int a = i; a <= j; a++) {
						Point2D.Double p = r.getBaseAt(a - i).getCoords();
						Point2D.Double c = r.getBaseAt(a - i).getCenter();
						_pos[a] = new VARNAPoint(x + p.x - base.x, p.y - base.y);
						_backupPos[a] = new VARNAPoint(x + p.x - base.x, p.y - base.y);
						_centers[a] = new VARNAPoint(x + c.x - base.x, c.y - base.y);
						_backupCenters[a] = new VARNAPoint(x + c.x - base.x, c.y - base.y);
					}
				} catch (ExceptionUnmatchedClosingParentheses | ExceptionFileFormatOrSyntax e) {
					e.printStackTrace();
				}
				flip = !flip;
				x += RNA.BASE_PAIR_DISTANCE;
				i = j;
			} else {
				x += RNA.LOOP_DISTANCE;
				i += 1;
			}
		}
	}

	private VARNAPoint rotate(VARNAPoint p, VARNAPoint center, double theta) {
		double newX = center.x + (p.x - center.x) * Math.cos(theta) + (p.y - center.y) * Math.sin(theta);
		double newY = center.y + (p.x - center.x) * Math.sin(theta) - (p.y - center.y) * Math.cos(theta);
		return new VARNAPoint(newX, newY);
	}

	public void fixHeights() {
		ArrayList<BasePair> bps = this.getAllNonSSBPs();
		Collections.sort(bps, new Comparator<BasePair>() {
			public int compare(BasePair o1, BasePair o2) {
				int span1 = o1.j - o1.i + 1;
				int span2 = o2.j - o2.i + 1;
				return (span1 - span2);
			}

		});
		_auxBPLayout.clear();
		double[] heights = new double[_pos.length];
		for (int i = 0; i < heights.length; i++) {
			heights[i] = 0.;
		}

		for (BasePair c : bps) {
			int a = c.i;
			int b = c.j;
			VARNAPoint pa = _pos[a];
			VARNAPoint pb = _pos[b];
			double minx = 0.;
			double maxx = 0.;
			double miny = 0.;
			double maxy = 0.;
			for (int k = a + 1; k < b; k++) {
				VARNAPoint p = _pos[k];
				minx = Math.min(minx, p.x);
				maxx = Math.max(maxx, p.x);
				miny = Math.min(miny, p.y);
				maxy = Math.max(maxy, p.y);
				miny = Math.min(miny, heights[k]);
				maxy = Math.max(maxy, heights[k]);
			}
			double dUp = Math.abs(maxy - pa.y);
			double dDown = Math.abs(miny - pa.y);
			BasePair cStack = new BasePair(a + 1, b - 1);
			if (_auxBPLayout.containsKey(cStack)) {
				Couple<Boolean, Double> lStack = _auxBPLayout.get(cStack);
				double fy;
				if (lStack.first) {
					fy = heights[a + 1] + RNA.LOOP_DISTANCE;
				} else {
					fy = heights[a + 1] - RNA.LOOP_DISTANCE;
				}
				_auxBPLayout.put(c, new Couple<Boolean, Double>(lStack.first, fy));
				heights[a] = fy;
				heights[b] = fy;
			} else {
				boolean dup = (dUp <= dDown);
				//dup = false;
				if (dup) {
					double fy = Math.max(MIN_LOOP_HEIGHT, maxy + RNA.LOOP_DISTANCE);
					_auxBPLayout.put(c, new Couple<Boolean, Double>(true, fy));
					heights[a] = fy;
					heights[b] = fy;
				} else {
					double fy = Math.min(-MIN_LOOP_HEIGHT, miny - RNA.LOOP_DISTANCE);
					_auxBPLayout.put(c, new Couple<Boolean, Double>(false, fy));
					heights[a] = fy;
					heights[b] = fy;
				}
			}
		}
	}
	
	private void adaptToMaxBPSpan() {

		for (int i = 0; i < _str.length; i++) {
			int j = _str[i];
			if (j > i) {
				int span = j - i + 1;
				if (span > _maxBPSpan) {
					_aux.add(new BasePair(i, j));
					_str[i] = -1;
					_str[j] = -1;
				}
			}
		}

		ArrayList<BasePair> naux = new ArrayList<BasePair>();
		for (BasePair c : _aux) {
			int i = c.i;
			int j = c.j;
			int span = j - i + 1;
			if (span <= _maxBPSpan) {
				_str[i] = j;
				_str[j] = i;
			} else {
				naux.add(c);
			}
		}
		_aux.clear();
		_aux.addAll(naux);

		{
			int i = 0;
			double x = 0.;
			while (i < _str.length) {
				_pos[i] = new VARNAPoint(x, 0.);
				int j = _str[i];
				if (j > i) {
					VARNAPoint base = _backupPos[i];
					double theta = -Math.atan2(_backupPos[j].y - base.y, _backupPos[j].x - base.x);
					for (int a = i; a <= j; a++) {
						VARNAPoint p = _backupPos[a];
						VARNAPoint rp = rotate(p, base, -theta);
						VARNAPoint c = _backupCenters[a];
						VARNAPoint rc = rotate(c, base, -theta);

						_pos[a] = new VARNAPoint(x + rp.x - base.x, rp.y - base.y);
						_centers[a] = new VARNAPoint(x + rc.x - base.x, rc.y - base.y);
					}
					x += RNA.BASE_PAIR_DISTANCE;
					i = j;
				} else {
					x += RNA.LOOP_DISTANCE;
					i += 1;
				}
			}
		}

		fixHeights();

	}

	public Rectangle2D.Double getBoundingBox() {
		return getBoundingBox(0, this._seq.length - 1);
	}

	public Rectangle2D.Double getBoundingBox(int from, int to) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (int i = Math.max(0, from); i <= Math.min(_pos.length - 1, to); i++) {
			VARNAPoint p = _pos[i];
			minX = Math.min(minX, p.x);
			maxX = Math.max(maxX, p.x);

			minY = Math.min(minY, p.y);
			maxY = Math.max(maxY, p.y);
		}
		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	public static ArrayList<Character> openPar = new ArrayList<>(Arrays.asList('(', '[', '{', '<'));
	public static ArrayList<Character> closePar = new ArrayList<>(Arrays.asList(')', ']', '}', '>'));
	static Hashtable<Character, Character> close2open = new Hashtable<Character, Character>();
	{
		for (int j = 0; j < openPar.size(); j++) {
			close2open.put(GenomeModel.closePar.get(j), GenomeModel.openPar.get(j));
		}
	}

	public static GenomeModel loadDBN(File path) throws IOException {
		String fn = path.getName();
		int j = fn.lastIndexOf('.');
		if (j != -1) {
			fn = fn.substring(0, j);
		}
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;

		String seq = "";
		String title = "";
		String dbn = "";
		while ((line = br.readLine()) != null) {
			String[] data = line.split(" ");
			if (data[0].startsWith(">")) {
				title = data[0].substring(1);
			} else if (!title.isEmpty()) {
				if (seq.isEmpty()) {
					seq = data[0].trim();
				} else {
					if (dbn.isEmpty()) {
						dbn = data[0].trim();
					}
				}
			}
		}
		br.close();

		return new GenomeModel(seq, dbn);
	}
	



	public static Couple<int[], ArrayList<BasePair>> parseDBN(String dbn) {

		int[] mainSS = new int[dbn.length()];
		ArrayList<BasePair> secSS = new ArrayList<BasePair>();
		for (int i = 0; i < dbn.length(); i++) {
			mainSS[i] = -1;
		}

		Hashtable<Character, Stack<Integer>> stacks = new Hashtable<Character, Stack<Integer>>();
		for (int j = 0; j < openPar.size(); j++) {
			stacks.put(openPar.get(j), new Stack<Integer>());
		}

		for (int i = 0; i < dbn.length(); i++) {
			char c = dbn.charAt(i);
			if (stacks.containsKey(c)) {
				stacks.get(c).push(i);
			} else {
				if (close2open.containsKey(c)) {
					char op = close2open.get(c);
					int j = stacks.get(op).pop();
					if (op == '(') {
						mainSS[i] = j;
						mainSS[j] = i;
					} else {
						secSS.add(new BasePair(j, i));
					}
				}
			}
		}
		return new Couple<int[], ArrayList<BasePair>>(mainSS, secSS);
	}

	public static GenomeModel loadCT(File f) throws NumberFormatException, IOException {
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String seq = "";
		String line;
		ArrayList<Couple<Integer, Integer>> struct = new ArrayList<Couple<Integer, Integer>>();
		while (((line = br.readLine()) != null)) {
			String[] data = line.trim().split("\\s+");
			System.err.println(Arrays.toString(data));
			if (data.length >= 6) {
				int i = Integer.parseInt(data[0]) - 1;
				int j = Integer.parseInt(data[4]) - 1;
				seq += data[1];
				if (i < j) {
					struct.add(new Couple<Integer, Integer>(i, j));
				}
			}
		}
		fr.close();

		int[] ss = new int[seq.length()];
		ArrayList<BasePair> secSS = new ArrayList<BasePair>();

		Stack<Integer> p = new Stack<Integer>();
		for (int i = 0; i < ss.length; i++) {
			ss[i] = -1;
		}

		for (Couple<Integer, Integer> c : struct) {
			int i = c.first;
			int j = c.second;
			if (i < j) {
				ss[i] = j;
				ss[j] = i;
			}
		}
		System.err.println(Arrays.toString(ss));

		for (int i = 0; i < ss.length; i++) {
			int j = ss[i];
			int jj = -1;
			if (!p.empty()) {
				jj = p.lastElement();
				if (jj == i) {
					p.pop();
				} else if ((i < j) && (jj < j)) {
					ss[i] = -1;
					ss[j] = -1;
					secSS.add(new BasePair(i, j));
				} else {
					p.push(j);
				}
			} else {
				p.push(j);
			}
		}
		System.err.println(Arrays.toString(ss));
		GenomeModel g = new GenomeModel(seq, ss, secSS);
		System.err.println(g.getMainStructAsString());
		return g;
	}
	

	public static GenomeModel loadRNA2D(File path) throws NumberFormatException, IOException  {
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String seq = "";
		String line;
		ArrayList<Couple<Integer, Integer>> struct = new ArrayList<Couple<Integer, Integer>>();
		ArrayList<VARNAPoint> centers = new ArrayList<VARNAPoint>(); 
		ArrayList<VARNAPoint> coords = new ArrayList<VARNAPoint>(); 

		while (((line = br.readLine()) != null)) {
			String[] data = line.trim().split("\\s+");
			if (data.length == 6) {
				int i = Integer.parseInt(data[0]) - 1;
				seq += data[1];
				double x = Double.parseDouble(data[2]);
				double y = Double.parseDouble(data[3]);
				coords.add(new VARNAPoint(x,y)); 
				double xc = Double.parseDouble(data[4]);
				double yc = Double.parseDouble(data[5]);
				centers.add(new VARNAPoint(xc,yc)); 
			}
			else if (data.length >= 2) {
				int i = Integer.parseInt(data[0]) - 1;
				int j = Integer.parseInt(data[1]) - 1;
				if (i < j) {
					struct.add(new Couple<Integer, Integer>(i, j));
				}
			}
		}
		fr.close();

		int[] ss = new int[seq.length()];
		ArrayList<BasePair> secSS = new ArrayList<BasePair>();

		Stack<Integer> p = new Stack<Integer>();
		for (int i = 0; i < ss.length; i++) {
			ss[i] = -1;
		}

		for (Couple<Integer, Integer> c : struct) {
			int i = c.first;
			int j = c.second;
			if (i < j) {
				ss[i] = j;
				ss[j] = i;
			}
		}

		for (int i = 0; i < ss.length; i++) {
			int j = ss[i];
			int jj = -1;
			if (!p.empty()) {
				jj = p.lastElement();
				if (jj == i) {
					p.pop();
				} else if ((i < j) && (jj < j)) {
					ss[i] = -1;
					ss[j] = -1;
					secSS.add(new BasePair(i, j));
				} else {
					p.push(j);
				}
			} else {
				p.push(j);
			}
		}
		GenomeModel g = new GenomeModel(seq, ss, secSS);
		g.setCenters(centers);
		g.setCoords(coords);
		
		return g;
	}
	
	public void saveRNA2D(File path) throws IOException {
		FileWriter fr = new FileWriter(path);
		BufferedWriter br = new BufferedWriter(fr);
		for(int i=0;i<_seq.length;i++) {
			String s = (i+1)+"\t"+_seq[i]+"\t"+_backupPos[i].getX()+"\t"+_backupPos[i].getY()+"\t"+_backupCenters[i].getX()+"\t"+_backupCenters[i].getY()+"\n";
			br.write(s);
		}
		int[] full = getFullStructure();
		for(int i=0;i<_seq.length;i++) {
			int j = _str[i];
			if (j>i) {
				String s = (i+1)+"\t"+(j+1)+"\n";
				br.write(s);
			}
		}
	}


	public char[] getSequence() {
		return _seq;
	}

	public int[] getMainStructure() {
		return this._str;
	}

	public int[] getFullStructure() {
		int[] full = new int[_str.length];
		for (int i=0;i<full.length;i++) {
			full[i] = _str[i];
		}
		for(BasePair bp: _aux) {
			if (full[bp.i] ==-1) {
				full[bp.i] = bp.j;
				full[bp.j] = bp.i;
			}
		}
		for(BasePair bp: this._pks) {
			if (full[bp.i] ==-1) {
				full[bp.i] = bp.j;
				full[bp.j] = bp.i;
			}
		}
		return full;
	}

	public String getMainStructAsString() {
		String res = "";
		for (int i = 0; i < _str.length; i++) {
			if (_str[i] == -1) {
				res += ".";
			} else if (_str[i] > i) {
				res += "(";
			} else {
				res += ")";
			}
		}
		return res;
	}
	
	public ArrayList<BasePair> getAllNonSSBPs() {
		ArrayList<BasePair> result = new ArrayList<BasePair>();
		result.addAll(_aux);
		result.addAll(_pks);
		return result;
	}


	public ArrayList<BasePair> getAuxBPs() {
		return _aux;
	}

	public ArrayList<BasePair> getPKBPs() {
		return this._pks;
	}

	public boolean isAuxBPUp(int i, int j) {
		return isAuxBPUp(new Couple<Integer, Integer>(i, j));
	}

	public boolean isAuxBPUp(Couple<Integer, Integer> c) {
		return _auxBPLayout.get(c).first;
	}

	public double getHeight(int i, int j) {
		return getHeight(new BasePair(i, j));
	}

	public double getHeight(BasePair c) {
		return _auxBPLayout.get(c).second;
	}

	public VARNAPoint[] getPositions() {
		return this._pos;
	}

	public VARNAPoint[] getCenters() {
		return this._centers;
	}
	
	public void setCoord(int index, double x, double y) {
		this._pos[index].x = x;
		this._pos[index].y = y;
		this._backupPos[index].x = x;
		this._backupPos[index].y = -y;
	}

	public void setCenter(int index, double x, double y) {
		this._centers[index].x = x;
		this._centers[index].y = y;
		this._backupCenters[index].x = x;
		this._backupCenters[index].y = -y;
	}

	public void setCoords(ArrayList<VARNAPoint> coords)  {
		for (int i=0;i<coords.size();i++) {
			VARNAPoint p = coords.get(i);
			this._backupPos[i].x = p.x;
			this._backupPos[i].y = p.y;
			this._pos[i].x = p.x;
			this._pos[i].y = p.y;
		}
	}

	public void setCenters(ArrayList<VARNAPoint> centers)  {
		for (int i=0;i<centers.size();i++) {
			VARNAPoint p = centers.get(i);
			this._backupCenters[i].x = p.x;
			this._backupCenters[i].y = p.y;
			this._centers[i].x = p.x;
			this._centers[i].y = p.y;
		}
	}

	public int locateBase(VARNAPoint p) {
		int best = -1;
		double bestDistance = Double.MAX_VALUE;
		for (int i = 0; i < _pos.length; i++) {
			double d = p.distanceTo(_pos[i]);
			if (d < bestDistance) {
				best = i;
				bestDistance = d;
			}
		}
		return best;
	}

	public ArrayList<Integer> getDomain(int index) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		boolean paired = (this._str[index] != -1);
		int i = index;
		int last = -1;
		while (i >= 0) {
			int j = _str[i];
			if (j != -1) {
				if (j > index) {
					last = i;
				}
			}
			i--;
		}
		int k = 0;
		int l = getLength() - 1;
		if (last != -1) {
			k = last;
			l = _str[k];
		}
		for (int a = k; a <= l; a++) {
			result.add(a);
		}
		return result;
	}

}
