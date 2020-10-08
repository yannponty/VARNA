package fr.orsay.lri.varna.models.puzzler;

import fr.orsay.lri.varna.models.export.VueVARNAGraphics;
import fr.orsay.lri.varna.models.rna.ModeleBase;
import rnaDrawing.rnaPuzzler.data.BaseInformation;
import rnaDrawing.rnaPuzzler.data.Circle;
import rnaDrawing.rnaPuzzler.postscript.PostscriptArcs;
import rnaDrawing.rnaPuzzler.utils.Vector2D;
import rnaDrawing.rnaPuzzler.utils.VectorMath;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class ArcHandler {


    private PostscriptArcs _newArcs;        //PS arcs
    private ArrayList<VarnaPuzzlerArc> arcs;    //Arcs for varna
    private List<Loop> loops;


    public ArcHandler() {
    }


    public void handle(int[] pair_table, double[] x, double[] y) {
        BaseInformation[] dummies = BaseInformation.generateBaseInformation(pair_table, 25., 35.);
        _newArcs = new PostscriptArcs(pair_table, x, y, dummies);
        loops = new ArrayList<>();
        for (int i = 0; i < dummies.length; i++) {
            if (dummies[i].getConfig() != null)
                foundLoop(i - 1, pair_table[i], _newArcs.getArcs()[i].getCenter(), merge(x, y), pair_table);
        }
        arcs = new ArrayList<>();
        recalculateArcs(pair_table, x, y);

    }

    private static Point2D.Double[] merge(double[] x, double[] y) {
        Point2D.Double[] res = new Point2D.Double[x.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Point2D.Double(x[i], y[i]);
        }
        return res;
    }

    public void handle(int[] pair_table, List<ModeleBase> bases) {
        double[] myx = new double[bases.size()];
        double[] myy = new double[bases.size()];
        for (int i = 0; i < bases.size(); i++) {
            myx[i] = bases.get(i).getCoords().x;
            myy[i] = bases.get(i).getCoords().y;
        }
        handle(pair_table, myx, myy);
    }

    public void handle(int[] pair_table, Point2D[] newCoords) {
        double[] myx = new double[newCoords.length];
        double[] myy = new double[newCoords.length];
        for (int i = 0; i < newCoords.length; i++) {
            myx[i] = newCoords[i].getX();
            myy[i] = newCoords[i].getY();
        }
        handle(pair_table, myx, myy);
    }

    public void handle(List<ModeleBase> bases){
        int[] table = createPairTable(bases);
        handle(table,bases);
    }


    public void drawBackbone(VueVARNAGraphics g2D, List<ModeleBase> bases,Point2D.Double[] newCoords) {
        for (int i = 0; i < bases.size()-1; i++) {
           if(isBaseInLoop(i) == -1 || isBaseInLoop(i+1) == -1 || !areBasesInSameLoop(i,i+1)){
               Point2D.Double p1 = newCoords[i];
               Point2D.Double p2 = newCoords[i+1];
               g2D.drawLine(p1.x,p1.y,p2.x,p2.y);
           }
        }
        drawArcsPuzzler(g2D);
    }

    private boolean areBasesInSameLoop(int i,int j){
        for(Loop loop : loops){
            if(loop.isBaseInLoop(i) && loop.isBaseInLoop(j))
                return true;
        }
        return false;
    }

    //TODO Here are the arcs drawn. A workaround for the parameters is rounding to the next integer.
    //The VARNA method for the graphics uses doubles as parameters, but this are all casted to integers(next smallest integer)
    //More in SwingGraphics Class.
    public void drawArcsPuzzler(VueVARNAGraphics g2D) {
        for (VarnaPuzzlerArc arc : arcs) {
            if (arc != null) {
                g2D.drawArc(arc.centre.x, arc.centre.y, arc.radius, arc.radius, Math.round(arc.angleFrom), Math.floor(arc.angleTotal));
                //Debug
                //((SwingGraphics)g2D).fillArc(arc.centre.x, arc.centre.y, arc.radius, arc.radius, Math.round(arc.angleFrom), Math.round(arc.angleTotal));
            }
        }

    }

    public boolean isLoopFlipped(int loopIndex){
        return loops.get(loopIndex).isFlipped;
    }

    public static int[] createPairTable(List<ModeleBase> bases) {
        int mypair_table[] = new int[bases.size() + 1];
        mypair_table[0] = bases.size();
        for (int i = 0; i < bases.size(); i++) {
            mypair_table[i + 1] = bases.get(i).getElementStructure() + 1;
        }
        return mypair_table;
    }

    private void foundLoop(int start, int end, Vector2D center, Point2D.Double[] coords, int[] pairtable) {
        ArrayList<Integer> bases = new ArrayList<>();
        double radius = new Vector2D(center, new Vector2D(coords[start].x, coords[start].y)).vectorLength2D();

        bases.add(start);
        int i = start + 1;
        boolean goClockwise = _newArcs.getArcs()[i].isGoClockwise();
        while (i < end) {
            bases.add(i);
            if (pairtable[i + 1] == 0 || pairtable[i + 1] < i) {
                //Without pairing or end of stem
                i++;
            } else if (pairtable[i + 1] > i) {
                //Stem -> jump to partner base
                i = pairtable[i + 1] - 1;
            }
        }
        loops.add(new Loop(new Circle(center, radius), bases, goClockwise));
    }

    private void recalculateArcs(int[] pair_table, double[] x, double[] y) {
        for (Loop loop : loops) {
            ArrayList<Integer> bases = loop.basesIndex;
            //System.out.println(bases);
            for (int i=0;i<bases.size()-1;i++) {      //Last one does not need to be drawn
                Vector2D center = loop.circle.getCenter();
                double radius = loop.circle.getRadius();
                if (bases.get(i) == bases.get(i+1)-1) {     //Not a basepair and both in loop
                    //Lets do some vector math!
                    //Pretty much everything already in puzzler, but needed again for custom arcs
                    Vector2D pointFrom = new Vector2D(x[bases.get(i)], y[bases.get(i)]);
                    Vector2D pointTo = new Vector2D(x[bases.get(i+1)], y[bases.get(i+1)]);
                    Vector2D reference = new Vector2D(1., 0.);       //In Java graphics this vector is the 0 grad vector
                    Vector2D vecCenterFrom = new Vector2D(center, pointFrom);
                    Vector2D vecCenterTo = new Vector2D(center, pointTo);
                    double angleRefFrom = Math.toDegrees(VectorMath.angleBetweenVectors2D(reference, vecCenterFrom));
                    double angleRefTo = Math.toDegrees(VectorMath.angleBetweenVectors2D(reference, vecCenterTo));
                    if (pointFrom.values[1] > center.values[1] && !isDoubleZero(angleRefFrom)) {
                        angleRefFrom = 360.0 - angleRefFrom;
                    }
                    if (pointTo.values[1] > center.values[1] && !isDoubleZero(angleRefTo)) {
                        angleRefTo = 360.0 - angleRefTo;
                    }
                    double angleDif = angleRefTo - angleRefFrom;
                    if (angleDif > 0.0000000000){ //Temporary solution
                        angleDif -= 360.;
                    }
                    if(loop.isFlipped)
                        angleDif += 360.;
                   
                    arcs.add(new VarnaPuzzlerArc(vector2DToPoint2D(center), radius * 2, angleRefFrom, angleDif, bases.get(i)));
                }
            }
        }
    }

    private static boolean isDoubleZero(double num) {
        return Math.abs(num) < .0000000000000001;
    }

    private static Point2D.Double vector2DToPoint2D(Vector2D vector) {
        return new Point2D.Double(vector.values[0], vector.values[1]);
    }
    private static Vector2D point2DToVector2D(Point2D.Double vector) {
        return new Vector2D(vector.x,vector.y);
    }

    public int isBaseInLoop(int index) {
        for (int i = 0; i < loops.size(); i++) {
            if (loops.get(i).isBaseInLoop(index))
                return i;
        }
        return -1;
    }

    //TODO Test if this centre is valid
    public Point2D.Double getLoopCentre(int index) {
        double x = loops.get(index).circle.getCenter().values[0];
        double y = loops.get(index).circle.getCenter().values[1];
        return new Point2D.Double(x, y);
    }


    public PostscriptArcs getNewArcs() {
        return _newArcs;
    }

    private static class Loop {
        private Circle circle;
        private ArrayList<Integer> basesIndex;
        private boolean isFlipped;

        public Loop(Circle circ, ArrayList<Integer> bases,boolean isFlipped) {
            circle = circ;
            basesIndex = bases;
            this.isFlipped = isFlipped;
        }

        public boolean isBaseInLoop(int index) {
            return basesIndex.contains(index);
        }

    }

    private static class VarnaPuzzlerArc {
        private Point2D.Double centre;
        private double radius, angleFrom, angleTotal;
        private int indexFrom;

        public VarnaPuzzlerArc(Point2D.Double center, double radius, double angleFrom, double angleTotal, int index) {
            this.centre = center;
            this.radius = radius;
            this.angleFrom = angleFrom;
            this.angleTotal = angleTotal;
            this.indexFrom = index;
        }
    }


}
