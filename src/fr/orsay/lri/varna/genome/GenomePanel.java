package fr.orsay.lri.varna.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import fr.orsay.lri.varna.VARNAPanel;
import fr.orsay.lri.varna.applications.templateEditor.Couple;
import fr.orsay.lri.varna.exceptions.ExceptionFileFormatOrSyntax;
import fr.orsay.lri.varna.exceptions.ExceptionUnmatchedClosingParentheses;
import fr.orsay.lri.varna.exceptions.ExceptionWritingForbidden;
import fr.orsay.lri.varna.factories.RNAFactory;
import fr.orsay.lri.varna.models.VARNAConfig;
import fr.orsay.lri.varna.models.export.SVGExport;
import fr.orsay.lri.varna.models.export.SecStrProducerGraphics;
import fr.orsay.lri.varna.models.export.SwingGraphics;
import fr.orsay.lri.varna.models.export.VueVARNAGraphics;
import fr.orsay.lri.varna.models.rna.ModeleColorMap;
import fr.orsay.lri.varna.models.rna.RNA;
import fr.orsay.lri.varna.models.rna.VARNAPoint;

public class GenomePanel extends JPanel implements MouseWheelListener,MouseMotionListener, MouseListener, ComponentListener{
	
	
	Color _backgroundColor = VARNAConfig.DEFAULT_BACKGROUND_COLOR;
	
	double _zoomLevel = 1;
	VARNAPoint _shift = new VARNAPoint(0,0);
	GenomeModel _rna = new GenomeModel();
	boolean _useColorMap = false;
	ModeleColorMap _cm = new ModeleColorMap();
	
	String _structureDescription = "N/A";
	String _profileDescription = "N/A";
	
	private HashSet<Integer> _selection = new HashSet<Integer>();
	
	private JFrame _parent;

	
	public GenomePanel(JFrame parent, String seq, String str) {
		_parent = parent;
		setSHAPEColorMap();
		setRNA(new GenomeModel(seq,str));
		addMouseWheelListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		addComponentListener(this);
	}
	
	public void setSequence(String s) {
		this.getRNA().setSequence(s);
		this.repaint();
	}
	
	
	
	public void setSHAPEColorMap() {
		_cm = new ModeleColorMap();
		_cm.addColor(-800, Color.gray, true);
		_cm.addColor(-5.01, Color.gray, true);
		_cm.addColor(-5, Color.yellow, true);
		_cm.addColor(-0.5, Color.yellow, true);
		_cm.addColor(0.8, Color.orange, true);
		_cm.addColor(1.5, Color.red, true);
		_cm.addColor(2.95, Color.red.darker().darker(), true);

	}

	public void setBoltzmannProbsColorMap() {
		_cm = new ModeleColorMap();
		_cm.addColor(-800, Color.gray, true);
		_cm.addColor(-0.01, Color.gray, true);
		_cm.addColor(0.0, Color.gray, true);
		_cm.addColor(0.5, Color.orange, true);
		_cm.addColor(0.8, Color.red, true);
		_cm.addColor(1, Color.red.darker().darker(), true);

	}
	
	private ArrayList<Annotation> _annotations = new ArrayList<Annotation>();
	
	public void setAnnotations(ArrayList<Annotation> ann) {
		_annotations.clear();
		_annotations.addAll(ann);
		this.repaint();
	}


	public void setStructureDescription(String s) {
		this._structureDescription = s;
	}

	public void setProfileDescription(String s) {
		this._profileDescription = s;
	}

	public String getStructureDescription() {
		return this._structureDescription;
	}

	public String getProfileDescription() {
		return this._profileDescription;
	}

	public void setRNA(GenomeModel rna) {
		_rna = rna; 
		showFull();
	}

	public GenomeModel  getRNA() {
		return _rna; 
	}
	
	public void setUseColorMap(boolean b) {
		_useColorMap = b;
		repaint();
	}
	
	public void setMaxBPSpan(int nm) {
		_rna.setMaxBPSpan(nm);
		this.repaint();
	}

	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Stroke dflt = g2.getStroke();
		VueVARNAGraphics g2D = new SwingGraphics(g2);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		this.removeAll();
		super.paintComponent(g2);
		renderComponent(g2D);
		super.paintBorder(g2);
	}
	


	private VARNAPoint invmap(VARNAPoint p) {
		VARNAPoint np = new VARNAPoint((p.x-_shift.x)/_zoomLevel,(p.y-_shift.y)/_zoomLevel);
		return np;
	}

	
	private VARNAPoint map(VARNAPoint p) {
		return map(p,_zoomLevel);
	}
	
	private VARNAPoint map(VARNAPoint p, double zoomLevel) {
		VARNAPoint np = new VARNAPoint(p.x*zoomLevel+_shift.x,p.y*zoomLevel+_shift.y);
		return np;
	}
	
	
	private boolean pointVisible(VARNAPoint p) {
		return (0<=p.x)&&(p.x<getWidth())&&(0<=p.y)&&(p.y<getHeight());
	}
	
	public  Couple<Integer,Integer> getVisibleInterval(){
		int i=Integer.MAX_VALUE;
		int j=Integer.MIN_VALUE;
		VARNAPoint[] pos = _rna.getPositions();
		for(int k=0;k<_rna.getLength();k++) {
			VARNAPoint p = map(pos[k]);
			if (pointVisible(p))
			{
				i = Math.min(i, k);
				j = Math.max(j, k);
			}
		}
		return new Couple<Integer,Integer>(i,j);
	}
	
	private long getOffset(int from, int to) {
		int n = to-from+1;
		double mult[] = {2, 2.5, 2};
		double offset = 5;
		int k = 0;
		while((n/offset)>(0.25*getWidth()/14.))
		{
			offset =  mult[k%mult.length]*offset;
			k++;
		}
		return (long) offset;
	}

	private void drawStringVertical(VueVARNAGraphics g2D, String s, double x, double y, double dy, boolean down) {
		//g2D.drawStringCentered(s,x,y);
		double sign = 1.;
		if (!down) {
			sign = -1.;
		}
		for(int i=0;i<s.length();i++) {
			char c;
			if (down) {
				c = s.charAt(i);
			}
			else {
				c = s.charAt(s.length()-1-i);
			}
			g2D.drawStringCentered(""+c,x,y+sign*i*dy);
		}
		
	}
	
	public static Color SELECTION_BACKBONE_COLOR = Color.BLACK;
	public static Color SELECTION_FILL_COLOR = Color.BLACK;
	public static Color SELECTION_TEXT_COLOR = Color.WHITE;
	
	
	
	private Color getBackboneColor(int index, double zoomLevel) {
		if (_selection.contains(index) && _selection.contains(index+1) ) {
			return SELECTION_BACKBONE_COLOR;
		}
		return VARNAConfig.DEFAULT_BACKBONE_COLOR;
	}

	private double getBackboneThickness(int index, double zoomLevel) {
		if (_selection.contains(index) && _selection.contains(index+1) ) {
			return Math.max(2.5,4.* zoomLevel);
		}
		return Math.max(1.5,3.* zoomLevel);
	}

	private Color getTextColor(int index, double zoomLevel) {
		if (_selection.contains(index)) {
			return SELECTION_TEXT_COLOR;
		}
		return VARNAConfig.BASE_NAME_COLOR_DEFAULT;
	}
	
	private Color getFillColor(int index, double zoomLevel) {
		Color c= Color.lightGray;
		if (_selection.contains(index)) {
			return SELECTION_FILL_COLOR;
		}
		if (this._useColorMap) {
			double val = _rna.getValue(index); 
			return _cm.getColorForValue(val);
		}
		else
		{
			if (zoomLevel>.7) {
					return VARNAConfig.BASE_INNER_COLOR_DEFAULT;
				}
			else {
				return VARNAConfig.DEFAULT_BACKGROUND_COLOR;
			}
		}

	}
	
	public static Color COLOR_LONG_RANGE_BPS = Color.blue.darker();
	public static Color COLOR_PK_BPS = Color.red;
	
	
	private ArrayList<Helix> asHelicesAux(ArrayList<BasePair> bps,Color c){
		ArrayList<Helix> result = new ArrayList<Helix>();
		HashSet<BasePair> bpSet = new HashSet<BasePair>();
		for(BasePair bp: bps) {
			bpSet.add(bp);
		}
		Hashtable<BasePair, Helix> bp2helix = new Hashtable<BasePair, Helix>();
		for(BasePair bp: bps) {
			if (!bp2helix.containsKey(bp)) {
				Helix h = new Helix(-1,-1,-1,c);
				bp2helix.put(bp, h);
				int mini = bp.i;
				int maxi = bp.i;
				int minj = bp.j;
				int maxj = bp.j;
				int curri = bp.i+1;
				int currj = bp.j-1;
				while(bpSet.contains(new BasePair(curri,currj))) {
					bp2helix.put(new BasePair(curri,currj), h);					
					maxi = curri;
					minj = currj;
					curri++;
					currj--;
				}
				curri = bp.i-1;
				currj = bp.j+1;
				while(bpSet.contains(new BasePair(curri,currj))) {
					bp2helix.put(new BasePair(curri,currj), h);					
					mini = curri;
					maxj = currj;
					curri--;
					currj++;
				}
				h.i = mini;
				h.j = maxj;
				h.length = maxi-mini+1;
				result.add(h);
			}
		}
		
		return result;		

	}
	
	private ArrayList<Helix> getAllHelices(){
		ArrayList<Helix> result = asHelicesAux(_rna.getAuxBPs(),COLOR_LONG_RANGE_BPS);
		result.addAll(asHelicesAux(_rna.getPKBPs(),COLOR_PK_BPS));
		return result;

	}
	
	public Color fadeToWhite(Color c, double mix) {
		return new Color(
				(int) (c.getRed() + (255-c.getRed())*(1.-mix)),
				(int) (c.getGreen() + (255.-c.getGreen())*(1.-mix)),
				(int) (c.getBlue() + (255.-c.getBlue())*(1.-mix)));
	}

	public synchronized void renderComponent(VueVARNAGraphics g2D) {
		renderComponent(g2D, _zoomLevel, false);
	}

	public Color DEFAULT_ANNOTATION_COLOR = Color.decode("#88E088");
	public double BASE_ANNOTATION_ABSOLUTE_MARGIN = 5.;
	
	public synchronized void renderComponent(VueVARNAGraphics g2D, double zoomLevel, boolean showAll) {
		super.setBackground(_backgroundColor);
		char[] seq = _rna.getSequence();
		VARNAPoint[] pos =_rna.getPositions();
		double newRadius = 12.*zoomLevel;
		int fontSize = (int) Math.round(18.*zoomLevel);
		if (zoomLevel<.7) 
		{ 
			fontSize *= 1.7;
			newRadius *= 1.7;
		}
		g2D.setStrokeThickness(Math.max(1.5,3.* VARNAConfig.DEFAULT_BP_THICKNESS * _zoomLevel));
		
		g2D.setColor(DEFAULT_ANNOTATION_COLOR);
		for(Annotation ann: _annotations) {
			double minx = Double.MAX_VALUE;
			double maxx = Double.MIN_VALUE;
			double miny = Double.MAX_VALUE;
			double maxy = Double.MIN_VALUE;
			for(int i=(int)ann.from;i<=(int)ann.to;i++) {
				VARNAPoint p =  pos[i];
				VARNAPoint pp = map(p,zoomLevel);
				minx = Math.min(minx,pp.x);
				maxx = Math.max(maxx,pp.x);
				miny = Math.min(miny,pp.y);
				maxy = Math.max(maxy,pp.y);
			}
			g2D.fillRect(minx-BASE_ANNOTATION_ABSOLUTE_MARGIN-2.*newRadius, miny-BASE_ANNOTATION_ABSOLUTE_MARGIN-2.*newRadius, maxx-minx + 4.*newRadius +2.*BASE_ANNOTATION_ABSOLUTE_MARGIN, maxy-miny + 4.*newRadius+2.*BASE_ANNOTATION_ABSOLUTE_MARGIN);
		}

		ArrayList<Helix> helices = getAllHelices();
		for(Helix he: helices) {
			BasePair base = he.getBase();
			VARNAPoint q =  pos[base.i];
			VARNAPoint qn =  pos[base.j];
			VARNAPoint p = map(q,zoomLevel);
			VARNAPoint pn = map(qn,zoomLevel);
			double h = _rna.getHeight(base);
			
			BasePair apex = he.getApex();
			double hh = _rna.getHeight(apex);
			VARNAPoint qq =  pos[apex.i];
			VARNAPoint qqn =  pos[apex.j];
			VARNAPoint pp = map(qq,zoomLevel);
			VARNAPoint ppn = map(qqn,zoomLevel);
			
			
			VARNAPoint p1 = map(new VARNAPoint(q.x,h),zoomLevel);
			VARNAPoint p2 = map(new VARNAPoint(qn.x,h),zoomLevel);

			VARNAPoint pp1 = map(new VARNAPoint(qq.x,hh),zoomLevel);
			VARNAPoint pp2 = map(new VARNAPoint(qqn.x,hh),zoomLevel);

			double tint = Math.max(.01,Math.min(1.-(zoomLevel*2. -.2),.35));
			
			g2D.setColor(fadeToWhite(he.color,tint));

			double miny = Math.min(p.y, p1.y);
			double maxy = Math.max(p.y, p1.y);
			g2D.fillRect(Math.min(p.x,pp.x), miny, Math.abs(pp.x-p.x), maxy-miny);
			
			miny = Math.min(pn.y, p1.y);
			maxy = Math.max(pn.y, p1.y);
			g2D.fillRect(Math.min(ppn.x,pn.x), miny, Math.abs(pn.x-ppn.x), maxy-miny);

			miny = Math.min(p1.y, pp1.y);
			maxy = Math.max(p1.y, pp1.y);

			g2D.fillRect(p1.x, miny, p2.x-p1.x, maxy-miny);

			
			g2D.setColor(he.color);
			if (zoomLevel>.1) {
				tint = Math.max(.01,Math.min(2*zoomLevel,1.));
				g2D.setColor(fadeToWhite(he.color,tint));

				g2D.drawLine(p.x, p.y, p1.x, p1.y);							
				g2D.drawLine(p1.x, p1.y, p2.x, p2.y);							
				g2D.drawLine(p2.x, p2.y, pn.x, pn.y);

				g2D.drawLine(pp.x, pp.y, pp1.x, pp1.y);							
				g2D.drawLine(pp1.x, pp1.y, pp2.x, pp2.y);							
				g2D.drawLine(pp2.x, pp2.y, ppn.x, ppn.y);
			}
		}

		if ((zoomLevel>.5)) {
			double tint = 1.;
			g2D.setColor(fadeToWhite(COLOR_LONG_RANGE_BPS,tint));
			for(BasePair c: _rna.getAuxBPs()) {
				int i = c.i;
				int j = c.j;
				VARNAPoint q =  pos[i];
				VARNAPoint qn =  pos[j];
				VARNAPoint p = map(q,zoomLevel);
				VARNAPoint pn = map(qn,zoomLevel);
				//double span = pn.x - p.x;
				//double delta = -0.5 * span;
				double h = _rna.getHeight(c);
				VARNAPoint p1 = map(new VARNAPoint(q.x,h),zoomLevel);
				VARNAPoint p2 = map(new VARNAPoint(qn.x,h),zoomLevel);
				g2D.drawLine(p.x, p.y, p1.x, p1.y);							
				g2D.drawLine(p1.x, p1.y, p2.x, p2.y);							
				g2D.drawLine(p2.x, p2.y, pn.x, pn.y);
			}
			g2D.setColor(fadeToWhite(COLOR_PK_BPS,tint));
			for(BasePair c: _rna.getPKBPs()) {
				int i = c.i;
				int j = c.j;
				VARNAPoint q =  pos[i];
				VARNAPoint qn =  pos[j];
				VARNAPoint p = map(q,zoomLevel);
				VARNAPoint pn = map(qn,zoomLevel);
				//double span = pn.x - p.x;
				//double delta = -0.5 * span;
				double h = _rna.getHeight(c);
				VARNAPoint p1 = map(new VARNAPoint(q.x,h),zoomLevel);
				VARNAPoint p2 = map(new VARNAPoint(qn.x,h),zoomLevel);
				g2D.drawLine(p.x, p.y, p1.x, p1.y);							
				g2D.drawLine(p1.x, p1.y, p2.x, p2.y);							
				g2D.drawLine(p2.x, p2.y, pn.x, pn.y);
			}
		}

		int[] ss = _rna.getMainStructure();
		if (zoomLevel>.03) {
			for(int i=0;i<seq.length;i++) {
				int k = ss[i];
				if (k>i)
				{
					VARNAPoint p = map(pos[i],zoomLevel);
					VARNAPoint pn = map(pos[k],zoomLevel);
					if (showAll || pointVisible(p)||pointVisible(pn))
					{
						g2D.setColor(VARNAConfig.DEFAULT_BOND_COLOR);
						g2D.drawLine(p.x, p.y, pn.x, pn.y);
					}
				}
			}
		}
		for(int i=1;i<seq.length;i++) {
			VARNAPoint p = map(pos[i-1],zoomLevel);
			VARNAPoint pn = map(pos[i],zoomLevel);
			if (showAll || pointVisible(p) || pointVisible(pn))
			{
				g2D.setStrokeThickness(getBackboneThickness(i-1,zoomLevel));
				Color backboneColor = getBackboneColor(i-1,zoomLevel);
				g2D.setColor(backboneColor);
				g2D.drawLine(p.x, p.y, pn.x, pn.y);
			}
		}
		
		g2D.setFont(Font.decode("Dialog-BOLD-"+fontSize));
		g2D.setStrokeThickness(VARNAConfig.DEFAULT_BASE_OUTLINE_THICKNESS* _zoomLevel);
		int minVisible = Integer.MAX_VALUE;
		int maxVisible = -1;
		for(int i=0;i<seq.length;i++) {
			VARNAPoint p = map(pos[i],zoomLevel);
			if (showAll || pointVisible(p))
			{
				minVisible = Math.min(minVisible,i);
				maxVisible = Math.max(maxVisible,i);
				Color fillColor = getFillColor(i,zoomLevel);
				Color textColor = getTextColor(i,zoomLevel);
				if (zoomLevel>.3) {
					g2D.setColor(fillColor);
					g2D.fillCircle(p.getX() - newRadius, p.getY() - newRadius,
							2.0 * newRadius);
				}
				else{
					if (_useColorMap) {
						g2D.setColor(fillColor);
						g2D.fillRect(p.getX() - newRadius, p.getY() - newRadius,
								2.0 * newRadius,2.0 * newRadius);
					}
				}
				if (zoomLevel>.7) {	
					g2D.setColor(VARNAConfig.BASE_OUTLINE_COLOR_DEFAULT);
					g2D.drawCircle(p.getX() - newRadius, p.getY() - newRadius,
							2.0 * newRadius);
				}
				if (zoomLevel>.3) {
					g2D.setColor(textColor);
					g2D.drawStringCentered(""+seq[i], p.getX(), p.getY());
				}
			}
		}

		long offset = (long) getOffset(minVisible,maxVisible);
		g2D.setColor(VARNAConfig.BASE_NAME_COLOR_DEFAULT);
		int numbersSize = (int)Math.max(14.*zoomLevel, 14.);
		g2D.setStrokeThickness(1.5);
		g2D.setFont(Font.decode("Monospace-BOLD-"+numbersSize));
		double lastBaseX = 0.;
		for(int i=0;i<seq.length;i++) {
			int num = i+1;
			VARNAPoint p = pos[i];
			boolean down = true;
			double x = p.x;
			if (p.y==0.) {
				lastBaseX = p.x;
			}
			else {
				x = lastBaseX + RNA.BASE_PAIR_DISTANCE/2.;
			}
				
			if  (((num==1)||(num%offset==0)||(num==seq.length) ) ) {
				
				if (lastBaseX != -1) {
					if (p.y>0.) {
						down=false;
					}
					VARNAPoint np = map(new VARNAPoint(x,0.),zoomLevel);
					if (down) {
						g2D.drawLine(np.getX(), np.getY()+newRadius+Math.max(4., zoomLevel*8),np.getX(), np.getY()+newRadius+23.);
						drawStringVertical(g2D,""+num, np.getX(), np.getY()+newRadius+35.,numbersSize,down);
					}
					else {
						g2D.drawLine(np.getX(), np.getY()-newRadius-Math.max(4., zoomLevel*8),np.getX(), np.getY()-newRadius-23.);
						drawStringVertical(g2D,""+num, np.getX(), np.getY()-newRadius-35.,numbersSize,down);						
					}
					lastBaseX = -1.;
					if (p.y==0.) {
						lastBaseX = p.x;
					}
				}
			}
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double delta = e.getPreciseWheelRotation();
		double factor = Math.pow(0.93, delta);
		Point p = e.getPoint();
		setShiftX(_shift.x*factor - (factor-1.)*getWidth()/2.);
		setZoomLevel(_zoomLevel*factor);
		this.repaint();
	}
	
	public static double HYSTERESIS = 3.;

	public void mouseDragged(MouseEvent e) {
		VARNAPoint current = new VARNAPoint(e.getX(),e.getY());
		if (!_moving) {
			double x = e.getX();
			double y = e.getY();
			if (current.distanceTo(_init)>HYSTERESIS)
			{
				_moving = true;
			}
		}

		if (_moving)
		{
			double  dx = current.x-_init.x;
			double  dy = current.y-_init.y;
			setShift(_shift.x+dx,_shift.y+dy);
			
			_init = current;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
	private RNA getStructure(ArrayList<Integer> domain) {
		RNA rna = new RNA();
		String seq = "";
		String dbn = "";
		char[] fullseq = _rna.getSequence();
		String fulldbn = _rna.getMainStructAsString();
		for (int p: domain) {
			seq += fullseq[p];
			dbn += fulldbn.charAt(p);
		}
		int mindomain = Collections.min(domain);
		try {
		rna.setRNA(seq,dbn);
		VARNAPoint all_pos[] = _rna.getPositions();
		VARNAPoint all_cent[] = _rna.getCenters();

		for (int p: domain) {
			VARNAPoint pos = all_pos[p];
			VARNAPoint cent = all_cent[p];
			rna.setCoord(p-mindomain, pos.x, pos.y);
			rna.setCenter(p-mindomain, cent.x, cent.y);
		}		
		} catch (ExceptionUnmatchedClosingParentheses e1) {
			e1.printStackTrace();
		} catch (ExceptionFileFormatOrSyntax e1) {
			e1.printStackTrace();
		}
		return rna;
	}

	VARNAPoint _init = new VARNAPoint(0,0);
	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getButton()==e.BUTTON1)&&(e.getClickCount() == 2))
		{
			VARNAPoint p = new VARNAPoint(e.getX(),e.getY());
			VARNAPoint pr = invmap(p);
			int i = _rna.locateBase(pr);
			ArrayList<Integer> domain = _rna.getDomain(i);
			_selection.clear();
			for(int j: domain) {
				_selection.add(j);
			}
			repaint();
			RNA rna = getStructure(domain);
			VARNAPanel vp = new VARNAPanel(rna);
			vp.setPreferredSize(new Dimension(600,600));
			
		    int result = JOptionPane.showOptionDialog(this, 
		    		vp,
	                "Edit local structure",
	                JOptionPane.OK_CANCEL_OPTION, 
	                JOptionPane.DEFAULT_OPTION, 
	                null, null, null);
		    if (result == JOptionPane.OK_OPTION) {
		    	int mindomain = Collections.min(domain);
				for (int nt: domain) {
					Point2D.Double coord = rna.getCoords(nt-mindomain);
					Point2D.Double center = rna.getCenter(nt-mindomain);
					_rna.setCoord(nt, coord.x, coord.y);
					_rna.setCenter(nt, center.x, center.y);
				}
				_rna.fixHeights();
				repaint();
		    }
		}
	}
	
	boolean _moving = false;
	@Override
	public void mousePressed(MouseEvent e) {
		this.requestFocus();
		if (e.getButton()==e.BUTTON1)
		{
			_init = new VARNAPoint(e.getX(),e.getY());
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton()==e.BUTTON1)
		{
			if (_moving) {
				_moving = false;
			}
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void exportSVG(File f) {
		SVGExport svg = new SVGExport();
		SecStrProducerGraphics g2d = new SecStrProducerGraphics(svg);
		renderComponent(g2d,5.0,true);
		try {
			g2d.saveToDisk(f);
		} catch (ExceptionWritingForbidden e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public
	void setVisible(boolean b)
	{
		super.setVisible(b);
	}

	public void showFull() {
		showPart(0, _rna.getLength()-1);
	}
	
	public void setZoomLevel(double newZoom) {
		if (newZoom !=_zoomLevel) {
			_zoomLevel = newZoom;
			fireZoomChanged();
		}
	}
	
	public double getZoomLevel() {
		return _zoomLevel;
	}
	
	
	public void setShiftY(double newY) {
		setShift(_shift.x, newY);
	}
	
	public void setShiftX(double newX) {
		setShift(newX, _shift.y);
	}
	
	public void setShift(double newX, double newY) {
		if ((_shift.x != newX) || (_shift.y != newY)) {
			_shift.x = newX;
			_shift.y = newY;
			fireShiftChanged();			
		}
	}

	public void setShift(VARNAPoint p) {
		if ((_shift.x != p.x) || (_shift.y != p.y)) {
			_shift = p;
			fireShiftChanged();			
		}
	}
	
	public void showPart(int from, int to) {
		double margin = 50.;
		Rectangle2D.Double bb = getRNA().getBoundingBox(from, to);
		setZoomLevel(Math.max(0.,((double)this.getWidth()-2.*margin)/bb.width));
		setShift(bb.x + margin,getHeight()/2.);
		repaint();
	}
	
	public void componentResized(ComponentEvent e) {
		showFull();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public interface GenomePanelChangeListener{
		public void onGenomePanelChanged(GenomePanelChange tt, GenomePanel source);
	}
	
	private ArrayList<GenomePanelChangeListener>  _genomePanelChangeListener = new ArrayList<GenomePanelChangeListener>();

	public enum GenomePanelChange {
		SHIFT_CHANGED,
		ZOOM_CHANGED
	}; 
	public void addGenomePanelChangeListener(GenomePanelChangeListener gl) {
		_genomePanelChangeListener.add(gl);
	}
	
	private void fireGenomePanelChanged(GenomePanelChange type) {
		for(GenomePanelChangeListener gl: _genomePanelChangeListener)
		{
			gl.onGenomePanelChanged(type, this);
		}
	}

	private void fireZoomChanged() {
		for(GenomePanelChangeListener gl: _genomePanelChangeListener)
		{
			gl.onGenomePanelChanged(GenomePanelChange.ZOOM_CHANGED, this);
		}
	}

	private void fireShiftChanged() {
		for(GenomePanelChangeListener gl: _genomePanelChangeListener)
		{
			gl.onGenomePanelChanged(GenomePanelChange.SHIFT_CHANGED, this);
		}
	}


	public void focusGained(FocusEvent e) {
		repaint();
	}

	public void focusLost(FocusEvent e) {
		repaint();		
	}
	
}
