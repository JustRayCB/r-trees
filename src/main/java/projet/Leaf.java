package projet;

// import java.util.ArrayList;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class Leaf extends Node {
    private Polygon polygon;
    private String name;

    public Leaf(Polygon polygon, String name) {
        super(polygon.getEnvelopeInternal(), true);
        this.polygon = polygon;
        this.name = name;
    }

    public Node search(Point p) {
        System.out.println("Searching inside " + name);
        if (polygon.contains(p)) { // if the point is inside the polygon, it's inside the mbr too
            System.out.println("Found " + name);
            return this;
        } else {
            System.out.println("Not found");
            return null;
        }
    }

    public Node chooseNode(Polygon p) {
        System.out.println("Choosing node inside " + name + " which is a leaf");
        return this;
    }

    public void addLeaf() {
    }

    public void quadraticSplit() {
    }

    public void linearSplit() {
    }
}
