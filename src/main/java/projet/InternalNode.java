
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
        boolean childIsLeaf = false;
        if (children.size() >= 1 && children.get(0).isLeaf()) {
            childIsLeaf = true; // if childisLeaf is true, we will insert the new node inside the current one
        }
        if (childIsLeaf) {
            return this;
        } // else
          // we need to find the child that will minimize the increase of the MBR on
          // insertion
        ArrayList<Double> insertionArea = new ArrayList<Double>();
        for (Node child : children) {
            Envelope childMbr = child.mbr;
            double areaMbr = childMbr.getArea();
            Envelope insertionMbr = new Envelope(childMbr);
            insertionMbr.expandToInclude(p.getCoordinate());
            double areaInsertionMbr = insertionMbr.getArea();
            insertionArea.add(areaInsertionMbr - areaMbr);
        }
        return children.get(insertionArea.indexOf(insertionArea.stream().min(Double::compare).get()))
                .chooseNode(p);
    }

    public void addLeaf() {

    }

    public void quadraticSplit() {
    }

    public void linearSplit() {
    }
}
