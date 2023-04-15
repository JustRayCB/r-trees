package projet;

// import java.util.ArrayList;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class Leaf extends Node {
    private Polygon polygon;
    private String name;

    public Leaf(Polygon polygon, String name) {
        this.polygon = polygon;
        this.name = name;
        this.mbr = polygon.getEnvelopeInternal();
        this.isLeaf = true;
    }
    public Node insert(Polygon polygon, String name) {
        if (this.polygon.equals(polygon)) {
            return this;
        }
        InternalNode newNode = new InternalNode();
        newNode.insert(this.polygon, this.name);
        newNode.insert(polygon, name);
        return newNode;
    }

    public Node search(Point p) {
        System.out.println("Searching inside " + name);
        if (polygon.contains(p)) { // if the point in inside the polygon, it's inside the mbr too
            System.out.println("Found " + name);
            return this;
        } else {
            System.out.println("Not found");
            return null;
        }
    }
    public void chooseNode() {}
    public void addLeaf() {}
}
