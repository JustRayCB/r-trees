package projet;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class RTree {
    private int nbrNodes;
    private Node root;
    // public static void main(String[] args) {
    //     // constructor
    // }
    public RTree() {
        nbrNodes = 0;
        root = null;
    }

    public void insert(Polygon polygon, String name) {
        if (root == null) {
            root = new Leaf(polygon, name);
        } else {
            root = root.insert(polygon, name);
        }
    }

    public void search(Point p) {}
    public void chooseNode() {}
    public void addLeaf() {}
}
