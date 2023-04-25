package projet;

// import java.util.ArrayList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

abstract class Node {
    protected Envelope mbr;
    protected boolean isLeaf;
    protected Node father;
    protected static final int MAX_CHILDREN = 4;
    protected static final int MIN_CHILDREN = 2;
    protected static final String SPLIT_METHOD = "quadratic";

    public Node(Envelope MBR, boolean isleaf, Node father) {
        this.mbr = MBR;
        this.isLeaf = isleaf;
        this.father = father;
    }

    public Envelope getMbr() {
        return mbr;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Search for a point in the current Node
     * 
     * @param p : the point to search
     * @return the node containing the point, or null if it's not found
     */
    public abstract Node search(Point p);

    /**
     * @brief: Search an insertion node for wich the insertion of the new polygone
     *         will minimize the increase of the MBR
     * 
     * @param p : the new polygon to insert
     */
    public abstract Node chooseNode(Polygon p);

    /**
     * @brief: Add a new leaf for the current node if the node is full
     *         (i.e. if it has MAX_CHILDREN children)
     */
    public abstract Node addLeaf(Polygon polygon, String label);

    /**
     * @brief: Function that will split the node with the quadratic or linear split
     */
    public Node split() {
        if (SPLIT_METHOD == "quadratic") {
            return this.quadraticSplit();
        } else {
            return this.linearSplit();
        }
    }

    protected abstract Node quadraticSplit();

    protected abstract Node linearSplit();
}
