
package projet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.javatuples.Pair;
import org.locationtech.jts.geom.Envelope;
// import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;

class InternalNode extends Node {
    private ArrayList<Node> children;

    public InternalNode(Envelope mbr, Node father) {
        super(mbr, false, father);
        children = new ArrayList<Node>();
    }

    public Node search(Point p) {
        if (mbr.contains(p.getEnvelopeInternal())) { // if the point is inside the mbr
            System.out.println("Coordinates of the point : " + p.getCoordinate());
            System.out.println("Coordinates of the mbr : " + mbr.toString());
            System.out.println("searching in : " + this.toString());

            for (Node child : children) {
                var result = child.search(p);
                if (result != null) {
                    return result;
                } else {
                    System.out.println("Not found in node");
                }
            }
        } // if the point is not inside the mbr, it's not inside the node or his children
        return null;
    }

    public Node chooseNode(Polygon p) {
        double minIncrease = Double.MAX_VALUE;
        Node minNode = null;
        for (Node child : children) {
            Envelope childMbr = child.mbr;
            double areaMbr = childMbr.getArea(); // area(mbr)
            Envelope insertionMbr = new Envelope(childMbr);
            insertionMbr.expandToInclude(p.getCoordinate());
            double areaInsertionMbr = insertionMbr.getArea(); // area(mbr U p)
            if (areaInsertionMbr - areaMbr < minIncrease) {
                minIncrease = areaInsertionMbr - areaMbr;
                minNode = child;
            }
        }
        return minNode;
    }

    public Node addLeaf(Polygon polygon, String label) {
        if (children.size() == 0 || children.get(0).isLeaf()) { // bottom level is reached -> Create Leaf
            children.add(new Leaf(polygon, label, this));
        } else {// still need to go deeper
            Node n = this.chooseNode(polygon);
            Node newNode = n.addLeaf(polygon, label);
            if (newNode != null) {
                // a split occured in addLeaf
                // a new node is added at this level
                children.add(newNode);
            }
        }
        mbr.expandToInclude(polygon.getEnvelopeInternal());
        if (children.size() > MAX_CHILDREN) {
            return split();
        }
        return null;
    }

    private void addToA(Node nodeToPlace, Envelope mbrA, Envelope mbrAWithNode, ArrayList<Node> groupA) {
        groupA.add(nodeToPlace);
        mbrA = mbrAWithNode;
    }

    private void addToB(Node nodeToPlace, Envelope mbrB, Envelope mbrBWithNode, ArrayList<Node> groupB) {
        groupB.add(nodeToPlace);
        mbrB = mbrBWithNode;
    }

    public Node split() {
        Pair<Node, Node> groups = new Pair<Node, Node>(null, null);
        if (SPLIT_METHOD == "quadratic") {
            groups = pickSeedsQuadratic();
        } else {
            groups = pickSeedsLinear();
        }
        ArrayList<Node> groupA = new ArrayList<Node>();
        ArrayList<Node> groupB = new ArrayList<Node>();
        Envelope mbrA = new Envelope(groups.getValue0().getMbr());
        Envelope mbrB = new Envelope(groups.getValue1().getMbr());
        groupA.add(groups.getValue0());
        groupB.add(groups.getValue1());

        children.remove(groups.getValue0());
        children.remove(groups.getValue1());

        int nodeToIntegrate = children.size(); // n-2 children remainings to place in the right group
        while (nodeToIntegrate > 0) {
            // if one group has so many nodes that all the rest must be assigned to it in
            // order for it to have the minimum number m, assign them and stop
            if (groupA.size() + nodeToIntegrate == MIN_CHILDREN) {
                for (int i = 0; i < children.size(); i++) {
                    groupA.add(children.get(i));
                    mbrA.expandToInclude(children.get(i).getMbr());
                    nodeToIntegrate--;
                }
                break;
            } else if (groupB.size() + nodeToIntegrate == MIN_CHILDREN) {
                for (int i = 0; i < children.size(); i++) {
                    groupB.add(children.get(i));
                    mbrB.expandToInclude(children.get(i).getMbr());
                    nodeToIntegrate--;
                }
                break;
            } else {
                // else, choose the node that will increase the area of the mbr the least
                // if the area increase is the same for both groups, choose the one with the
                // smallest area, then the one with the fewest nodes, then randomly choosing
                Node nodeToPlace;
                if (SPLIT_METHOD == "quadratic") {
                    nodeToPlace = pickNextQuadratic(mbrA, mbrB);
                } else {
                    nodeToPlace = pickNextLinear();
                }
                children.remove(nodeToPlace);
                Envelope mbrAWithNode = new Envelope(mbrA);
                Envelope mbrBWithNode = new Envelope(mbrB);
                GeometryBuilder gb = new GeometryBuilder();
                Point p = gb.point(-66.57, -55.22);
                if (nodeToPlace.mbr.contains(p.getCoordinate())) {
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    System.out.println("J'ai trouvé le node pas dans le mbr : " + nodeToPlace.getId());
                }
                mbrAWithNode.expandToInclude(nodeToPlace.getMbr());
                mbrBWithNode.expandToInclude(nodeToPlace.getMbr());
                double areaIncreaseA = mbrAWithNode.getArea() - mbrA.getArea();
                double areaIncreaseB = mbrBWithNode.getArea() - mbrB.getArea();
                if (areaIncreaseA < areaIncreaseB) {
                    groupA.add(nodeToPlace);
                    // mbrA.expandToInclude(mbrAWithNode);
                    mbrA = mbrAWithNode;
                } else if (areaIncreaseA > areaIncreaseB) {
                    groupB.add(nodeToPlace);
                    // mbrB.expandToInclude(mbrBWithNode);
                    mbrB = mbrBWithNode;
                } else {
                    if (mbrA.getArea() < mbrB.getArea()) {
                        groupA.add(nodeToPlace);
                        // mbrA.expandToInclude(mbrAWithNode);
                        mbrA = mbrAWithNode;
                    } else if (mbrA.getArea() > mbrB.getArea()) {
                        groupB.add(nodeToPlace);
                        mbrB = mbrBWithNode;
                        // mbrB.expandToInclude(mbrBWithNode);
                    } else if (groupA.size() < groupB.size()) {
                        groupA.add(nodeToPlace);
                        // mbrA.expandToInclude(mbrAWithNode);
                        mbrA = mbrAWithNode;
                    } else if (groupA.size() > groupB.size()) {
                        groupB.add(nodeToPlace);
                        // mbrB.expandToInclude(mbrBWithNode);
                        mbrB = mbrBWithNode;
                    } else {
                        groupA.add(nodeToPlace);
                        // mbrA.expandToInclude(mbrAWithNode);
                        mbrA = mbrAWithNode;
                    }

                }
            }
            nodeToIntegrate--;
        }
        if (father == null) {
            // we need to create a father
            InternalNode childA = new InternalNode(mbrA, this);
            InternalNode childB = new InternalNode(mbrB, this);
            childA.children = groupA;
            childB.children = groupB;
            mbr = new Envelope(mbrA);
            mbr.expandToInclude(mbrB);
            children.clear();
            children.add(childA);
            children.add(childB);
            return null;
        } else {
            children.clear();
            children.addAll(groupA);
            mbr = mbrA;
            InternalNode newNode = new InternalNode(mbrB, father);
            newNode.children = groupB;
            father.mbr.expandToInclude(mbr);
            return newNode;

        }
    }

    private Pair<Node, Node> pickSeedsQuadratic() {
        double maxArea = 0;
        // Pair<Node, Node> bestPair = new Pair<Node, Node>(children.get(0),
        // children.get(1));
        Node bestNode1 = null;
        Node bestNode2 = null;
        for (int i = 0; i < children.size(); i++) {
            for (int j = i + 1; j < children.size(); j++) {
                Node node1 = children.get(i);
                Node node2 = children.get(j);
                Envelope mbr1 = node1.getMbr();
                Envelope mbr2 = node2.getMbr();
                Envelope bigArea = new Envelope(mbr1);
                bigArea.expandToInclude(mbr2);
                double area = bigArea.getArea() - mbr1.getArea() - mbr2.getArea();
                if (area > maxArea) {
                    maxArea = area;
                    // bestPair.addAt0(node1);
                    // bestPair.addAt1(node2);
                    bestNode1 = node1;
                    bestNode2 = node2;
                }
            }
        }
        return new Pair<Node, Node>(bestNode1, bestNode2);
    }

    private Node pickNextLinear() {
        Node node = children.get(0);
        return node;
    }

    private Node pickNextQuadratic(final Envelope mbrA, final Envelope mbrB) {
        // needs to have the index of the next node to place in the children list
        // Choose any entry
        // with the maximum difference -> maybe we could use abs
        // between d1 and d2

        double biggestDiff = 0;
        Node bestNode = null;

        // index is the index where we need to begin from because
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            Envelope mbr = node.getMbr();

            // we'll test adding the node to the two groups
            Envelope augmentedMbrA = new Envelope(mbrA);
            Envelope augmentedMbrB = new Envelope(mbrB);
            augmentedMbrA.expandToInclude(mbr);
            augmentedMbrB.expandToInclude(mbr);

            // we check the difference between the two area augmentation
            double d1 = Math.abs(augmentedMbrA.getArea() - mbrA.getArea());
            double d2 = Math.abs(augmentedMbrB.getArea() - mbrB.getArea());
            double d = Math.abs(d1 - d2);

            if (d > biggestDiff) {
                biggestDiff = d;
                bestNode = node;
            }
        }
        return bestNode;
    }

    public Pair<Node, Node> pickSeedsLinear() {
        // find the entry whose rectangle has
        // the highest low side, and the one
        // with the lowest high side in each dimension
        double lowestHightSide = Double.MAX_VALUE;
        double highestLowSide = -Double.MAX_VALUE;
        Node bestLowNode = null;
        Node bestHighNode = null;

        for (var child : children) {
            // low side of children
            double lowSide = (child.getMbr().getMinY() / child.getMbr().getHeight()) +
                    (child.getMbr().getMinX() / child.getMbr().getWidth());
            double highSide = (child.getMbr().getMaxY() / child.getMbr().getHeight()) +
                    (child.getMbr().getMaxX() / child.getMbr().getWidth());
            if (lowSide > highestLowSide) {
                highestLowSide = lowSide;
                bestLowNode = child;
            } else if (highSide < lowestHightSide) {
                lowestHightSide = highSide;
                bestHighNode = child;
            }
        }
        // useless to normalise the values ?

        return new Pair<Node, Node>(bestLowNode, bestHighNode);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(id);
        buffer.append('\n');
        for (Iterator<Node> it = children.iterator(); it.hasNext();) {
            Node child = it.next();
            if (it.hasNext()) {
                child.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                child.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }

    public void parseTree(ListFeatureCollection collection, SimpleFeatureBuilder featureBuilder,
            GeometryBuilder gb) {
        // uncomment these line if you want to see all the mbr of the different INTERNAL
        // NODE
        // featureBuilder.add(
        // gb.box(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY()));
        // collection.add(featureBuilder.buildFeature(null));
        for (Iterator<Node> it = children.iterator(); it.hasNext();) {
            Node child = it.next();
            if (it.hasNext()) {
                child.parseTree(collection, featureBuilder, gb);
            } else {
                child.parseTree(collection, featureBuilder, gb);
            }
        }

    }

    public String toString() {
        return "Node " + id;
    }
}
