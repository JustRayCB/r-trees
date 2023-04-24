
package projet;

import java.util.ArrayList;
import java.util.Random;

import org.javatuples.Pair;
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
        // quadratic split for r-tree node
        // we need to find the two nodes that will maximize the area of the mbr
        // of the two nodes
        // we will use the same method as chooseNode
        // but we will return the two nodes that maximize the area of the mbr
        // of the two nodes

        // select the first two nodes to make a good split
        Pair<Node, Node> groups = pickSeeds();
        ArrayList<Node> groupA = new ArrayList<Node>(null);
        ArrayList<Node> groupB = new ArrayList<Node>(null);
        Envelope mbrA = new Envelope(groups.getValue0().getMbr());
        Envelope mbrB = new Envelope(groups.getValue1().getMbr());
        // remove the two nodes from the children list
        children.remove(groups.getValue0());
        children.remove(groups.getValue1());

        groupA.add(groups.getValue0());
        groupB.add(groups.getValue1());

        int nodeToIntegrate = children.size() - 2; // n-2 children remainings to place in the right group
        while (nodeToIntegrate > 0) {
            // if one group has so many nodes that all the rest must be assigned to it in
            // order for it to have the minimum number m, assign them and stop
            if (groupA.size() + nodeToIntegrate == MIN_CHILDREN) {
                groupA.addAll(children.subList(children.size() - nodeToIntegrate, children.size()));
                break;
            } else if (groupB.size() + nodeToIntegrate == MIN_CHILDREN) {
                groupB.addAll(children.subList(children.size() - nodeToIntegrate, children.size()));
                break;
            }

            // else, choose the node that will increase the area of the mbr the least
            // if the area increase is the same for both groups, choose the one with the
            // smallest area
            Node nodeToPlace = pickNext();
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
                // if equals, choose the group with the smallest area
                if (mbrA.getArea() < mbrB.getArea()) {
                    addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                } else if (mbrA.getArea() > mbrB.getArea()) {
                    addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                } else if (groupA.size() < groupB.size()) {
                    addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                } else if (groupA.size() > groupB.size()) {
                    addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                } else {
                    // pick a random int number
                    Random rand = new Random();
                    if (rand.nextInt(2) == 0) {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                    } else {
                        addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                    }
                }

            }
            nodeToIntegrate--;
        }

        return null;
    }

    private Pair<Node, Node> pickSeeds() {
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

    private Node pickNext() {
        return null;
    }

    public Node linearSplit() {
        return null;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }
}
