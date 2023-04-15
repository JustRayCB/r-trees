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
    public abstract Node search(Point p);
    public abstract void chooseNode();
    public abstract void addLeaf();
}
