package projet;

import java.util.ArrayList;
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
    public abstract void search(Point p);
    public abstract void chooseNode();
    public abstract void addLeaf();
}

class Leaf extends Node {
    private Polygon polygon;
    private String name;

    public Leaf(Polygon polygon, String name) {
        this.polygon = polygon;
        this.name = name;
        this.mbr = polygon.getEnvelopeInternal();
        this.isLeaf = true;
    }
    public Node insert(Polygon polygon, String name) {
        if (this.polygon.equals(polygon)) {
            return this;
        }
        InternalNode newNode = new InternalNode();
        newNode.insert(this.polygon, this.name);
        newNode.insert(polygon, name);
        return newNode;
    }

    public void search(Point p) {}
    public void chooseNode() {}
    public void addLeaf() {}
}

class InternalNode extends Node {
    private ArrayList<Node> children;

    // public Node insert() { return null; }
    public Node insert(Polygon polygon, String name) {
        Node newNode = null;
        for (Node child : children) {
            if (child.getMbr().contains(polygon.getEnvelopeInternal())) {
                newNode = child.insert(polygon, name);
                break;
            }
        }
        if (newNode == null) {
            newNode = new Leaf(polygon, name);
        }
        if (children.size() < MAX_CHILDREN) {
            children.add(newNode);
            return this;
        } else {
            // return split(newNode);
            return null;
        }
    }

    public void search(Point p) {}
    public void chooseNode() {}
    public void addLeaf() {}
}
