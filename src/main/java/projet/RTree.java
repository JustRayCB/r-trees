package projet;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
// import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygon;

public class RTree {

    private int nbrNodes;
    private Node root; // maybe we need to make a default root

    public RTree() {
        this.nbrNodes = 0;
        this.root = new InternalNode(new Envelope()); // default root
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

    /**
     * @brief : Add a leaf to the R-Tree
     * @param polygon : Polygon we want to add the the R-Tree
     * @param label   : Name of the polygon
     */
    public void addLeaf(Polygon polygon, String label) {
        root.addLeaf(polygon, label);
        nbrNodes++;
    }

    /**
     * @brief : Same as above with a MultiPolygon which is a polygon that contains
     *        other polygons
     *        Ex: France, Spain, ...
     * @param multi : MultiPolygon we want to add to the R-Tree
     * @param label : Name of the MultiPolygon
     */
    public void addLeaf(MultiPolygon multi, String label) {
        // Méthode proposée par Ziyad Haltout Rhouni
        for (int polygon = 0; polygon < multi.getNumGeometries(); polygon++) {
            addLeaf((Polygon) multi.getGeometryN(polygon), label);
        }
    }

    public int getNbrNodes() {
        return nbrNodes;
    }
}
