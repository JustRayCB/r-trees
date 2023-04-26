package projet;

// import java.util.ArrayList;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;

abstract class Node {
    protected Envelope mbr;
    protected boolean isLeaf;
    protected Node father;
    protected final int id = name++;
    protected static final int MAX_CHILDREN = 50;
    protected static final int MIN_CHILDREN = 25;
    protected static final String SPLIT_METHOD = "linear";
    protected static int name = 0;

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

    public int getId() {
        return id;
    }

    public abstract void print(StringBuilder buffer, String prefix, String childrenPrefix);

    public abstract void parseTree(ListFeatureCollection collection, SimpleFeatureBuilder featureBuilder,
            GeometryBuilder gb);
}
