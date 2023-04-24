
package projet;

import java.util.ArrayList;

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

        groupA.add(groups.getValue0());
        groupB.add(groups.getValue1());

        int nodeToIntegrate = children.size() - 2; // rest n-2 children to place in the right group

        for (Node child : children) {
            // if node is not ont of the two pre-selected node
            if (child != groups.getValue0() && child != groups.getValue1()) {
                // check if the minimum number of children is respected :
                // if you have minimum nodes to integrate in a group to maintain the minimum
                // number right, you just put them in the group
                if ((groupA.size() + nodeToIntegrate) <= child.MIN_CHILDREN) {
                    groupA.add(child);
                } else if ((groupB.size() + nodeToIntegrate) <= child.MIN_CHILDREN) {
                    groupB.add(child);
                } else {
                    
                }

            }
            nodeToIntegrate--;
        }
        // every child is in a group



        return null;
    }

    public Pair<Node, Node> pickSeeds() {
        double maxArea = 0;
        Pair<Node, Node> bestPair = new Pair<Node,Node>(null,null);
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

    public Node pickNext() {
        return null;
    }

    public 

    public Node linearSplit() {
        return null;
    }
}
