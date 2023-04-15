
package projet;

import java.util.ArrayList;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class InternalNode extends Node {
    private ArrayList<Node> children;

    // public Node insert() { return null; }
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

    public void search(Point p) {}
    public void chooseNode() {}
    public void addLeaf() {}
}
