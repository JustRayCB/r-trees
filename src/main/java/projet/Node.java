package projet;

// import java.util.ArrayList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

abstract class Node {
    protected Envelope mbr;
    protected boolean isLeaf;
    protected static final int MAX_CHILDREN = 4;

    public Envelope getMbr() { return mbr; }
    public boolean isLeaf() { return isLeaf; }
    public abstract Node insert(Polygon polygon, String name);

    /**
     * Search for a point in the tree
     * @param p the point to search
     * @return the node containing the point, or null if it's not found
     */
    public abstract Node search(Point p);
    public abstract Node chooseNode(Node n, Polygon p);
    public abstract void addLeaf();
    public abstract void split();
}
