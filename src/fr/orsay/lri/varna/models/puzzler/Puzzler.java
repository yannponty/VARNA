package fr.orsay.lri.varna.models.puzzler;

import fr.orsay.lri.varna.models.rna.ModeleBase;
import rnaDrawing.rnaPuzzler.Options.NumberOfChangesExceededException;
import rnaDrawing.rnaPuzzler.Options.OptimizationOptions;
import rnaDrawing.rnaPuzzler.Options.PuzzlerOptions;
import rnaDrawing.rnaPuzzler.affineCoordinates.AffineCoordinates;
import rnaDrawing.rnaPuzzler.data.*;
import rnaDrawing.rnaPuzzler.dataTypes.ApplyStrategy;
import rnaDrawing.rnaPuzzler.dataTypes.ExecutionStrategy;
import rnaDrawing.rnaPuzzler.intersections.resolve.ResolveExteriorChildIntersections;
import rnaDrawing.rnaPuzzler.intersections.resolve.ResolveIntersections;
import rnaDrawing.rnaPuzzler.optimization.OrderStrategy;
import rnaDrawing.rnaPuzzler.optimizer.computeDeltas.DistributionStrategy;
import rnaDrawing.rnaPuzzler.optimizer.computeIncreases.IncreaseStrategy;
import rnaDrawing.rnaPuzzler.optimizer.shrinkLoopRadius.SearchStrategy;
import rnaDrawing.rnaPuzzler.output.ConfigtreeDebug;
import rnaDrawing.rnaPuzzler.output.MessageHandler;
import rnaDrawing.rnaPuzzler.postscript.PostscriptArcs;
import rnaDrawing.rnaPuzzler.rnaPuzzler.RNApuzzlerException;
import rnaDrawing.rnaPuzzler.rnaTurtle.RNAturtle;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static rnaDrawing.rnaPuzzler.rnaPuzzler.RNApuzzler.*;
import static rnaDrawing.rnaPuzzler.utils.PuzzlerMath.sqrt;

public class Puzzler {

    private BaseInformation[] baseInformationArray;
    private PuzzlerOptions myOpt;
    private int[] pair_table;
    private double scaleFactor = 1.;
    private List<LoopTemplate> templates;

    public Puzzler() {
        myOpt = new PuzzlerOptions("out");
        //Needed for method layout to work
        RNADescription dummy = new RNADescription("dummy", "dummy", "", "");
        myOpt.setRNADescription(dummy);
        OptimizationOptions optimizationOptions = new OptimizationOptions(
                OrderStrategy.PACO, //BOTTOM_UP,
                ExecutionStrategy.AFTER_RESOLVE, //WHILE_RESOLVE,
                SearchStrategy.SEARCH_LINEAR,
                ApplyStrategy.IMPROVEMENT, //ALL,
                IncreaseStrategy.INCREASE_ALL_OTHER,
                DistributionStrategy.DISTRIBUTE_PROPORTIONALLY,
                myOpt
        );
        myOpt.setOptimizationOptions(optimizationOptions);
        templates = new ArrayList<>();
    }

    public Drawing puzzler_xycoords(int[] mypair_table) {


        Drawing myDrawing;
        try {
            myDrawing = layout_RNApuzzler(mypair_table, myOpt);
        } catch (RNApuzzlerException e) {
            e.printStackTrace();
            return null;
        }
        return myDrawing;
    }

    public Drawing test_puzzler_drawing(
            final int[] pair_table
    ) throws RNApuzzlerException {
        final String fnName = "layout_RNApuzzler";

        Drawing drawing = new Drawing();
        myOpt.scaleDistance(scaleFactor);
        System.out.println(myOpt.getPaired());

        if (myOpt.getPaired() / myOpt.getUnpaired() > 2.0) {
            MessageHandler.printWarning(fnName, "paired:unpaired > 2.0 -> layout might be destroyed!\n");
        }

        int length = pair_table[0];

        baseInformationArray = BaseInformation.generateBaseInformation(pair_table,
                myOpt.getUnpaired(),
                myOpt.getPaired());


        /// RNAturtle
//        System.out.printf("%s start turtle: %s\n", fnName, puzzlerOptions.getRnaDescription().getName());
        RNAturtle.computeAffineCoordinates(pair_table,
                myOpt.getPaired(),
                myOpt.getUnpaired(),
                baseInformationArray);

        /// Transform affine coordinates into cartesian coordinates
        double[] myX = new double[length];
        double[] myY = new double[length];
        AffineCoordinates.affineToCartesianCoordinates(baseInformationArray, myX, myY);

        /// Build RNApuzzler configuration tree from cartesian coordinates
        double distBulge = sqrt(0.75 * myOpt.getUnpaired() * myOpt.getUnpaired());
        TreeNode exteriorNode = new TreeNode(pair_table, baseInformationArray, myX, myY, distBulge);
        drawing.setTree(exteriorNode);

        /// reset angle coordinates
        /*
        for (int i = 0; i < length+1; i++) {
            baseInformation[i].distance = puzzler.unpaired;
            baseInformation[i].angle = 0.0;
        }
         */
        /// RNApuzzler
//        System.out.printf("%s start puzzler: %s\n", fnName, puzzlerOptions.getRnaDescription().getName());
        myOpt.setLayoutOptions(exteriorNode);
        if (myOpt.isCheckExteriorIntersections()
                || myOpt.isCheckSiblingIntersections()
                || myOpt.isCheckAncestorIntersections()) {
            /// - One execution of checkAndFixIntersections should always be sufficient
            exteriorNode.updateBoundingBoxes(myOpt);
            try {
                ResolveIntersections.checkAndFixIntersections(exteriorNode, 0, myOpt);
            } catch (NumberOfChangesExceededException numberOfChangesExceededException) {
                MessageHandler.printError(fnName,
                        "Reached maximum number of changes while resolving intersection: "
                                + numberOfChangesExceededException.getMessage());
                throw new RNApuzzlerException("RNApuzzler layout not successful");
            }

            if (myOpt.performOptimizationAfter()) {
                try {
                    myOpt.getOptimizationOptions().getOptimize().optimize(exteriorNode);
                } catch (NumberOfChangesExceededException numberOfChangesExceededException) {
                    MessageHandler.printError(fnName,
                            "Reached maximum number of changes while performing optimization: "
                                    + numberOfChangesExceededException.getMessage());
                    throw new RNApuzzlerException("RNApuzzler layout not successful");
                }
            }
        }

        determineNucleotideCoordinates(exteriorNode,
                pair_table, length,
                myOpt.getUnpaired(), myOpt.getPaired(),
                myX, myY);


        boolean checkIntersectionsOfExteriorBranches = true;
        if (checkIntersectionsOfExteriorBranches) {
            ResolveExteriorChildIntersections.resolveExteriorChildrenIntersectionXY(exteriorNode,
                    pair_table,
                    myOpt.getUnpaired(),
                    myOpt.isAllowFlipping(),
                    myX, myY);
        }

        if (myOpt.isDrawArcs()) {
            PostscriptArcs postscriptArcs = new PostscriptArcs(pair_table, myX, myY, baseInformationArray);
            drawing.setPostscriptArcs(postscriptArcs);
        }

        /// final check based on line segments and arc segments
        boolean printDetails = false;

        boolean intersect = checkRemainingIntersections(myX, myY, drawing, printDetails, baseInformationArray, length);
        drawing.initialize(myX, myY);
        return drawing;
    }

    private void getCoordinates(List<ModeleBase> bases, double[] x, double[] y) {
        for (int i = 0; i < bases.size(); i++) {
            x[i] = bases.get(i).getCoords().x;
            y[i] = bases.get(i).getCoords().y;
        }
    }

    public Point2D.Double[] resolve_new_intersections(List<ModeleBase> bases) throws RNApuzzlerException {
        double[] myX = new double[bases.size()];
        double[] myY = new double[bases.size()];
        getCoordinates(bases, myX, myY);
        double distBulge = Math.sqrt(0.75 * myOpt.getUnpaired() * myOpt.getUnpaired());
        TreeNode exteriorNode = new TreeNode(pair_table, baseInformationArray, myX, myY, distBulge);
        if (!templates.isEmpty()) {
            LoopTemplate lastTempl = templates.get(templates.size() - 1);
            findAndUpdateTreeNodeConfig(exteriorNode, lastTempl.getID(), lastTempl, myOpt);
//            recTest = findNode(exteriorNode,templates.get(templates.size()-1).getID());
        } else {
            System.err.println("PUZZLER: Templates should have at least one element, how did this happen?");
            throw new RNApuzzlerException("Well, this should not have happened!");
        }

        if (ConfigtreeDebug.FANCY_PS) {
            ConfigtreeDebug.PS_printFancyTree(exteriorNode, myOpt, true);
        }
        myOpt.setLayoutOptions(exteriorNode);
        if (myOpt.isCheckExteriorIntersections()
                || myOpt.isCheckSiblingIntersections()
                || myOpt.isCheckAncestorIntersections()) {
            exteriorNode.updateBoundingBoxes(myOpt);
            if (ConfigtreeDebug.FANCY_PS) {
                ConfigtreeDebug.PS_printFancyTree(exteriorNode, myOpt, true);
            }
//            for (int i = 0; i < 10; i++) {
//                System.out.println("Coordinates before inter at: " + (i + 1) + " x= " + myX[i] + " y= " + myY[i]);
//            }
            try {
                ResolveIntersections.checkAndFixIntersections_VARNA(exteriorNode, 0, myOpt, templates);
//                ResolveIntersections.checkAndFixIntersections(exteriorNode,0,myOpt);
            } catch (NumberOfChangesExceededException numberOfChangesExceededException) {
                numberOfChangesExceededException.printStackTrace();
                throw new RNApuzzlerException("RNApuzzler layout not successful");
            }
//            for (int i = 0; i < 10; i++) {
//                System.out.println("Coordinates after inter at: " + (i + 1) + " x= " + myX[i] + " y= " + myY[i]);
//            }
//
//            if (myOpt.performOptimizationAfter()) {
//                try {
//                    myOpt.getOptimizationOptions().getOptimize().optimize(exteriorNode);
//                } catch (NumberOfChangesExceededException numberOfChangesExceededException) {
//                    numberOfChangesExceededException.printStackTrace();
//                    throw new RNApuzzlerException("RNApuzzler layout not successful");
//                }
//            }
        }
//        for(int i=0;i<10;i++){
//            System.out.println("Coordinates before coord at: "+ (i+1) + " x= " + myX[i] + " y= "+ myY[i]);
//        }
        determineNucleotideCoordinates(exteriorNode,
                pair_table, pair_table[0],
                myOpt.getUnpaired(), myOpt.getPaired(),
                myX, myY);
//        for(int i=0;i<10;i++){
//            System.out.println("Coordinates after coord at: "+ (i+1) + " x= " + myX[i] + " y= "+ myY[i]);
//        }

        boolean checkIntersectionsOfExteriorBranches = true;
        if (checkIntersectionsOfExteriorBranches) {
            ResolveExteriorChildIntersections.resolveExteriorChildrenIntersectionXY(exteriorNode,
                    pair_table,
                    myOpt.getUnpaired(),
                    myOpt.isAllowFlipping(),
                    myX, myY);
        }
        Point2D.Double[] coords = new Point2D.Double[pair_table[0]];
        for (int i = 0; i < pair_table[0]; i++) {
            coords[i] = new Point2D.Double(myX[i], myY[i]);
//            System.out.println("Coordinates at "+(i+1)+": x= "+coords[i].x + " y= "+ coords[i].y);
        }
        fixExteriorBases(coords);
//        printConfigs();
        return coords;
    }

    private void fixExteriorBases(Point2D.Double[] coordinates){
        int firstStem = -1;
        boolean foundStem = false;
        int i = 1;
        while(!foundStem){
            if(pair_table[i] == 0){
                i++;
            } else{
                firstStem = i;
                foundStem = true;
            }
        }
        double y = coordinates[firstStem-1].y;
        for(i = firstStem;1 < i;i--){
            double x = coordinates[i-1].x - myOpt.getUnpaired();
            Point2D.Double newCoord = new Point2D.Double(x,y);
            coordinates[i-2].setLocation(newCoord);
        }
    }

    private double[] getNewAnglesForLoop(int loopBase1, double newAngle1, int loopBase2, double newAngle2) {
        int loopStart = getLoopStart(loopBase1);
//        System.out.println("Loop start: " + loopStart);
        int arcIndex1 = getArcIndex(loopBase1, loopStart);
        int arcIndex2 = getArcIndex(loopBase2, loopStart);
        double[] angles = new double[baseInformationArray[loopStart].getConfig().getNumberOfArcs()];
//        System.out.println("ArcIndexes: " + arcIndex1 + ", " + arcIndex2);
        ConfigArc cfg1 = baseInformationArray[loopStart].getConfig().getConfigArcs()[arcIndex1];
        ConfigArc cfg2 = baseInformationArray[loopStart].getConfig().getConfigArcs()[arcIndex2];
        double normalizeAngle1 = cfg1.getArcAngle() + newAngle1;
        double normalizeAngle2 = cfg2.getArcAngle() + newAngle2;
        while (normalizeAngle1 > 2 * Math.PI)
            normalizeAngle1 -= 2 * Math.PI;
        while (normalizeAngle1 < 0.0)
            normalizeAngle1 += 2 * Math.PI;
        while (normalizeAngle2 > 2 * Math.PI)
            normalizeAngle2 -= 2 * Math.PI;
        while (normalizeAngle2 < 0.0)
            normalizeAngle2 += 2 * Math.PI;
        for (int i = 0; i < angles.length; i++) {
            double value;
            if (i == arcIndex1)
                value = normalizeAngle1;
            else if (i == arcIndex2)
                value = normalizeAngle2;
            else
                value = baseInformationArray[loopStart].getConfig().getArcAngle(i);
            angles[i] = value;
        }
        return angles;
    }

    public void updateRotationLoop(int loopBase1, double newAngle1, int loopBase2, double newAngle2, List<ModeleBase> bases) {
        double[] angles = getNewAnglesForLoop(loopBase1, newAngle1, loopBase2, newAngle2);
        makeLoopTemplate(bases, getLoopStart(loopBase1), angles);

    }

    public double getArcAngle(int loopBase) {
        int loopStart = getLoopStart(loopBase);
        int arcIndex = getArcIndex(loopBase, loopStart);
        ConfigArc cfg = baseInformationArray[loopStart].getConfig().getConfigArcs()[arcIndex];
        double angleTotal = cfg.getArcAngle();
        double actualAngle = angleTotal - 2 * Math.asin(myOpt.getPaired() / (2 * baseInformationArray[loopStart].getConfig().getRadius()));
        return actualAngle;

    }

    public int getLoopStart(int loopBase) {
        int end = loopBase;
        int i = 1;
        int lastConfig = -1;
        while (i <= end) {
//            System.out.println("End: "+ loopBase);
            //found a loop start
            if (baseInformationArray[i].getConfig() != null)
                lastConfig = i;
            if (i == end) {
//                System.out.println("Loopstart found at: " + lastConfig);
                return lastConfig;
            }
            //Unpaired base: switch to next backbone base
            if (pair_table[i] == 0) {
                i++;
            }
            //Paired base, check pair value. If bigger than our end, then end must be there.
            else {
                if (pair_table[i] > i) {
                    if (pair_table[i] > end) {
                        i++;
                    } else {
                        i = pair_table[i];
                    }
                } else {
                    i++;
                }

            }
//            System.out.println("Actual base: "+ i + " and actual last config: "+ lastConfig);
        }
        return lastConfig;
    }

    private int getArcIndex(int index_base, int startLoop) {
        int end = pair_table[startLoop];
        int i = startLoop + 1;
        int arcIndex = 0;
        while (i <= end) {
//            System.out.println("Actual base: " + i);
//            System.out.println("Actual arcindex: " + arcIndex);
            if (i == index_base) {
                return arcIndex;
            }
            if (pair_table[i] == 0) {
                i++;
            } else if (pair_table[i] > i) {
                if (pair_table[i] == index_base)
                    return arcIndex;
                i = pair_table[i] + 1;
                arcIndex++;
            } else {
                break;
            }
//            System.out.println("Updated to: "+ i);
        }
        return -1;
    }

    public void setPair_table(int[] new_table) {
        pair_table = new_table;
        //debug
//        for(int i=1;i<new_table[0];i++){
//            System.out.println("For base i= "+ i + " is pair value="+pair_table[i]);
//        }
    }

    public void setScaleFactor(double scale) {
        this.scaleFactor = scale;
    }

    private TreeNode DFS_config(TreeNode start, Config conf) {
        if (start == null)
            return null;
        if (start.getConfig() != null && start.getConfig().equals(conf))
            return start;
        else {
            for (TreeNode child : start.getChildren()) {
                TreeNode rec = DFS_config(child, conf);
                if (rec != null)
                    return rec;
            }
            return null;
        }
    }

    private void makeLoopTemplate(List<ModeleBase> bases, int loopStart, double[] newAngles) {
        Config conf = baseInformationArray[loopStart].getConfig();
        if (conf == null) {
            System.err.println("PUZZLER: Loopstart is not actually a loopstart");
            return;
        }
        double[] x = new double[pair_table[0]];
        double[] y = new double[pair_table[0]];
        getCoordinates(bases, x, y);
        TreeNode tree = new TreeNode(pair_table, baseInformationArray, x, y, Math.sqrt(0.75 * myOpt.getUnpaired() * myOpt.getUnpaired()));
        TreeNode node = DFS_config(tree, conf);
        if (node == null) {
            System.err.println("PUZZLER: Node is null");
            return;
        }
//        System.out.println("Radius before: " + node.getConfig().getRadius());
        node.getConfig().setAngles(newAngles, myOpt);
//        System.out.println("Radius after: " + node.getConfig().getRadius());
        LoopTemplate templ = new LoopTemplate();
        templ.setLoopID(node.getNodeID());
        int from = node.getNodeID();
        int to = node.getChild(0).getNodeID();
        for (int i = 0; i < newAngles.length; i++) {
            templ.addArc(new LoopArc(from, to, newAngles[i]));
            from = to;
            if (i + 1 < node.getNumberOfChildren())
                to = node.getChild(i + 1).getNodeID();
            else
                to = node.getNodeID();
        }
        if (!templates.isEmpty())
            templates.removeIf(t -> t.getID() == templ.getID());
//        System.out.println("Is template valid?: " + templ.isValid(0.00001));
        templates.add(templ);
//        System.out.println("New Template:\n"+ templ.toString());

    }

    private TreeNode findNode(TreeNode node, int nodeID) {
        if (node.getNodeID() == nodeID)
            return node;
        int next = node.getChildIndex(nodeID);
        if (next < 0)
            return null;
        return findNode(node.getChild(next), nodeID);
    }

    private boolean findAndUpdateTreeNodeConfig(TreeNode root, int nodeID, LoopTemplate template, PuzzlerOptions options) {
        TreeNode foundNode = findNode(root, nodeID);
        if (foundNode == null)
            return false;
        foundNode.applyTemplate(template, options);
        return true;
    }

    private void printConfigs() {
        for (BaseInformation bi : baseInformationArray) {
            if (bi.getConfig() == null)
                continue;
            System.out.println(bi.getConfig().getNumberOfArcs());
            for (ConfigArc arc : bi.getConfig().getConfigArcs())
                System.out.println("Angle: " + Math.toDegrees(arc.getArcAngle()));
        }
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getPairedDist() {
        return myOpt.getPaired();
    }

    public double getUnpairedDist() {
        return myOpt.getUnpaired();
    }
}
