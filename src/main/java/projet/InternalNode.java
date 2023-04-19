
package projet;

import java.util.ArrayList;
import org.locationtech.jts.geom.Envelope;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class InternalNode extends Node {
    private ArrayList<Node> children;

    public Node insert(Polygon polygon, String name) {
        Node newNode = null;
        for (Node child : children) {
            if (child.getMbr().contains(polygon.getEnvelopeInternal())) {
                newNode = child.insert(polygon, name);
                break;
            }
        }
        if (newNode == null) {
            newNode = new Leaf(polygon, name);
        }
        if (children.size() < MAX_CHILDREN) {
            children.add(newNode);
            return this;
        } else {
            // return split(newNode);
            return null;
        }
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

    public Node chooseNode(Node n, Polygon p) {
        for (Node child : children) {
            Envelope extandedMbr = mbr.copy();
            extandedMbr.expandToInclude(child.getMbr());
        }
        return null;
    }

    public void addLeaf() {}

    public void split() {}
}
