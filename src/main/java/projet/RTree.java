package projet;

import org.locationtech.jts.geom.Point;
// import org.locationtech.jts.geom.Polygon;

public class RTree {

    private int nbrNodes;
    private Node root;

    public RTree() {
        this.nbrNodes = 0;
        this.root = null;
    }

    public Node search(Point p) {
        if (root != null) { // we just call the search method of the root
            return root.search(p);
        } else {
            System.out.println("The tree is empty");
            return null;
        }
    }

    public Node chooseNode() {
        return root.chooseNode(null);
    }

    public void addLeaf() {
        root.addLeaf();
        nbrNodes++;
    }

    public int getNbrNodes() {
        return nbrNodes;
    }
}
