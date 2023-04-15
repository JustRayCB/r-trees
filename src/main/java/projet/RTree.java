package projet;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class RTree {

    private int nbrNodes;
    private Node root;

    public RTree() {
        nbrNodes = 0;
        root = null;
    }

    // public int getNbrNodes() { return nbrNodes; }

    public void insert(Polygon polygon, String name) {
        System.out.println("The number of nodes is " + nbrNodes);
        if (root == null) {
            root = new Leaf(polygon, name);
        } else {
            root = root.insert(polygon, name);
        }
        nbrNodes++;
    }

    public Node search(Point p) {
        if (root != null) { // we just call the search method of the root
            return root.search(p);
        } else {
            System.out.println("The tree is empty");
            return null;
        }
    }
    public void chooseNode() {}
    public void addLeaf() {}
}
