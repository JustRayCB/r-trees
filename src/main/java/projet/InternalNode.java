
package projet;

import java.util.ArrayList;
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
        return null;
    }

    public Node linearSplit() {
        return null;
    }
}
