package projet;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;

abstract class Node {
    protected Envelope mbr;
    protected boolean isLeaf;
    protected Node father;
    protected final int id = name++;
    protected static int MAX_CHILDREN = 50;
    protected static int MIN_CHILDREN = 25;
    protected static String SPLIT_METHOD = "linear";
    protected static int name = 0;

    public static void setMaxChildren(int max) {
        MAX_CHILDREN = max;
    }

    public static void setMinChildren(int min) {
        MIN_CHILDREN = min;
    }

    public static void setSplitMethod(String method) {
        SPLIT_METHOD = method;
    }

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
    protected abstract Node chooseNode(Polygon p);

    /**
     * @brief: Add a new leaf for the current node if the node is full
     *         (i.e. if it has MAX_CHILDREN children)
     */
    public abstract Node addLeaf(Polygon polygon, String label);

    public int getId() {
        return id;
    }

    public abstract void print(StringBuilder buffer, String prefix, String childrenPrefix);

    public abstract String toString();

    public abstract void parseTree(ListFeatureCollection collection, SimpleFeatureBuilder featureBuilder,
            GeometryBuilder gb);

    public String getSplitMethod() {
        return SPLIT_METHOD;
    }
}
