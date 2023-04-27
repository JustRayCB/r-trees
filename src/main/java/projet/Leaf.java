package projet;

// import java.util.ArrayList;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;

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
            System.out.println("Not found in leaf");
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
        buffer.append(name + ':' + id);
        buffer.append('\n');
    }

    public void parseTree(ListFeatureCollection collection, SimpleFeatureBuilder featureBuilder,
            GeometryBuilder gb) {
        // if (name.e"Chili") {
        if (name.equals("Chili")) {
            featureBuilder.add(
                    gb.box(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY()));
            collection.add(featureBuilder.buildFeature(null));
            featureBuilder.add(polygon);
            collection.add(featureBuilder.buildFeature(null));
        }

    }

}
