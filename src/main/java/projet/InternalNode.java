
package projet;

import java.util.ArrayList;
import java.util.Random;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.locationtech.jts.geom.Envelope;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class InternalNode extends Node {
    private ArrayList<Node> children;

    public InternalNode(Envelope mbr) {
        super(mbr, false);
        children = new ArrayList<Node>();
    }

    public Node search(Point p) {
        if (mbr.contains(p.getCoordinate())) { // if the point is inside the mbr
            for (Node child : children) {
                var result = child.search(p);
                if (result != null) {
                    return result;
                } else {
                    System.out.println("Not found");
                }
            }
        } // if the point is not inside the mbr, it's not inside the node or his children
        return null;
    }

    public Node chooseNode(Polygon p) {
        // we need to stop to the node that have leaves as children
        if (children.size() >= 1 && children.get(0).isLeaf()) {
            return this;// if childisLeaf is true, we will insert the new node inside the current one
        }
        // we need to find the child that will minimize the increase of the MBR on
        // insertion
        ArrayList<Double> insertionArea = new ArrayList<Double>();
        for (Node child : children) {
            Envelope childMbr = child.mbr;
            double areaMbr = childMbr.getArea(); // area(mbr)
            Envelope insertionMbr = new Envelope(childMbr);
            insertionMbr.expandToInclude(p.getCoordinate());
            double areaInsertionMbr = insertionMbr.getArea(); // area(mbr U p)
            insertionArea.add(areaInsertionMbr - areaMbr); // area(mbr U p) - area(mbr)
        }
        // return
        // children.get(insertionArea.indexOf(insertionArea.stream().min(Double::compare).get()))
        // .chooseNode(p);
        return children.get(insertionArea.indexOf(insertionArea.stream().min(Double::compare).get()));
    }

    public Node addLeaf(Polygon polygon, String label) {
        if (children.size() == 0 || children.get(0).isLeaf()) { // bottom level is reached -> Create Leaf
            children.add(new Leaf(polygon, label));
        } else {// still need to go deeper
            Node n = this.chooseNode(polygon);
            Node newNode = n.addLeaf(polygon, label);
            if (newNode == null) {
                // a split occured in addLeaf
                // a new node is added at this level
                children.add(newNode);
            }
        }
        mbr.expandToInclude(polygon.getCoordinate());
        if (children.size() >= MAX_CHILDREN) {
            return split();
        }
        return null;

    }

    private void addToA(Node nodeToPlace, Envelope mbrA, Envelope mbrAWithNode, ArrayList<Node> groupA) {
        groupA.add(nodeToPlace);
        mbrA = mbrAWithNode;
    }

    private void addToB(Node nodeToPlace, Envelope mbrB, Envelope mbrBWithNode, ArrayList<Node> groupB) {
        groupB.add(nodeToPlace);
        mbrB = mbrBWithNode;
    }

    public Node quadraticSplit() {
        Pair<Node, Node> groups = pickSeedsQuadratic();
        ArrayList<Node> groupA = new ArrayList<Node>(null);
        ArrayList<Node> groupB = new ArrayList<Node>(null);
        Envelope mbrA = new Envelope(groups.getValue0().getMbr());
        Envelope mbrB = new Envelope(groups.getValue1().getMbr());
        children.remove(groups.getValue0());
        children.remove(groups.getValue1());

        groupA.add(groups.getValue0());
        groupB.add(groups.getValue1());

        int nodeToIntegrate = children.size() - 2; // n-2 children remainings to place in the right group
        while (nodeToIntegrate > 0) {
            // if one group has so many nodes that all the rest must be assigned to it in
            // order for it to have the minimum number m, assign them and stop
            if (groupA.size() + nodeToIntegrate == MIN_CHILDREN) {
                for (int i = children.size() - nodeToIntegrate; i < children.size(); i++) {
                    groupA.add(children.get(i));
                    mbrA.expandToInclude(children.get(i).getMbr());
                }
                break;
            } else if (groupB.size() + nodeToIntegrate == MIN_CHILDREN) {
                for (int i = children.size() - nodeToIntegrate; i < children.size(); i++) {
                    groupB.add(children.get(i));
                    mbrB.expandToInclude(children.get(i).getMbr());
                }
                break;
            } else {

                // else, choose the node that will increase the area of the mbr the least
                // if the area increase is the same for both groups, choose the one with the
                // smallest area, then the one with the fewest nodes, then randomly choosing
                Node nodeToPlace = pickNextQuadratic(children.size() - nodeToIntegrate, mbrA, mbrB);
                children.remove(nodeToPlace);
                Envelope mbrAWithNode = new Envelope(mbrA);
                Envelope mbrBWithNode = new Envelope(mbrB);
                mbrAWithNode.expandToInclude(nodeToPlace.getMbr());
                mbrBWithNode.expandToInclude(nodeToPlace.getMbr());
                double areaIncreaseA = mbrAWithNode.getArea() - mbrA.getArea();
                double areaIncreaseB = mbrBWithNode.getArea() - mbrB.getArea();
                if (areaIncreaseA < areaIncreaseB) {
                    addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                } else if (areaIncreaseA > areaIncreaseB) {
                    addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                } else {
                    if (mbrA.getArea() < mbrB.getArea()) {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                    } else if (mbrA.getArea() > mbrB.getArea()) {
                        addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                    } else if (groupA.size() < groupB.size()) {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                    } else if (groupA.size() > groupB.size()) {
                        addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                    } else {
                        Random rand = new Random();
                        if (rand.nextInt(2) == 0) {
                            addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                        } else {
                            addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                        }
                    }

                }
            }
            nodeToIntegrate--;
        }
        // creation of the two new nodes
        children = groupA;
        mbr = mbrA;
        InternalNode newNode = new InternalNode(mbrB);
        newNode.children = groupB;
        newNode.mbr = mbrB;
        return newNode;
    }

    private Pair<Node, Node> pickSeedsQuadratic() {
        double maxArea = 0;
        Pair<Node, Node> bestPair = new Pair<Node, Node>(null, null);
        for (int i = 0; i < children.size(); i++) {
            for (int j = i + 1; j < children.size(); j++) {
                Node node1 = children.get(i);
                Node node2 = children.get(j);
                Envelope mbr1 = node1.getMbr();
                Envelope mbr2 = node2.getMbr();
                Envelope bigArea = new Envelope(mbr1);
                bigArea.expandToInclude(mbr2);
                double area = bigArea.getArea() - mbr1.getArea() - mbr2.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    bestPair.addAt0(node1);
                    bestPair.addAt1(node1);
                }
            }
        }
        return bestPair;
    }

    private Node pickNextQuadratic(final int index, final Envelope mbrA, final Envelope mbrB) {
        // needs to have the index of the next node to place in the children list
        // Choose any entry
        // with the maximum difference -> maybe we could use abs
        // between d1 and d2

        double biggestDiff = 0;
        Node bestNode = null;

        // index is the index where we need to begin from because
        for (int i = index; i < children.size(); i++) {
            Node node = children.get(i);
            Envelope mbr = node.getMbr();

            // we'll test adding the node to the two groups
            Envelope augmentedMbrA = new Envelope(mbrA);
            Envelope augmentedMbrB = new Envelope(mbrB);
            augmentedMbrA.expandToInclude(mbr);
            augmentedMbrB.expandToInclude(mbr);

            // we check the difference between the two area augmentation
            double d1 = Math.abs(augmentedMbrA.getArea() - mbrA.getArea());
            double d2 = Math.abs(augmentedMbrB.getArea() - mbrB.getArea());
            double d = Math.abs(d1 - d2);

            if (d > biggestDiff) {
                biggestDiff = d;
                bestNode = node;
            }   
        }
        return bestNode;
    }

    public Node linearSplit() {
        // picknext choose any of the remainings entries

        return null;
    }

    public Node pickSeedsLinear() {
        // find the entry whose rectangle has
        // the highest low side, and the one
        // with the lowest high side in each dimension
        ArrayList<Triplet<Double, Double, Node>> lowHigh = new ArrayList<Triplet<Double, Double, Node>>();
        Node bestLow = null;
        Node bestHigh = null;
        for (var child : children) {
            // low side of children
            double lowSide = child.getMbr().getMinY() + child.getMbr().getMinX();
            double highSide = child.getMbr().getMaxY() + child.getMbr().getMaxX();
            lowHigh.add(new Triplet<Double, Double, Node>(lowSide, highSide, child));
        }
        // useless to normalise the values ?

        return null;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }
}
