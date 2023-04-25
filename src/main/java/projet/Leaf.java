package projet;

// import java.util.ArrayList;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class Leaf extends Node {
    private Polygon polygon;
    private String name;

    public Leaf(Polygon polygon, String name, Node father) {
        super(polygon.getEnvelopeInternal(), true, father);
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

    public Node addLeaf(Polygon polygon, String label) {
        System.out.println("Adding a leaf to a leaf " + name + " !!! not normal");
        return null;
    }

    public Node quadraticSplit() {
        System.out.println("Quadratic split on a leaf " + name + " !!! not normal");
        return null;
    }

    public Node linearSplit() {
        System.out.println("Linear split on a leaf " + name + " !!! not normal");
        return null;
    }

    public String toString() {
        return name;
    }

    public void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);
        buffer.append('\n');
        // for (Iterator<Node> it = children.iterator(); it.hasNext();) {
        // Node child = it.next();
        // if (it.hasNext()) {
        // child.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│ ");
        // } else {
        // child.print(buffer, childrenPrefix + "└── ", childrenPrefix + " ");
        // }
        // }
    }

}
