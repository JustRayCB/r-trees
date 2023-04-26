
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
        if (mbr.contains(p.getCoordinate())) { // if the point is inside the mbr
            for (Node child : children) {
                var result = child.search(p);
                if (result != null) {
                    return result;
                } else {
                    System.out.println("Not found");
                }
            }
        } // if the point is not inside the mbr, it's not inside the node or his children
        return null;
    }

    public Node chooseNode(Polygon p) {
        System.out.println("In choose Node");
        // we need to stop to the node that have leaves as children
        if (children.size() >= 1 && children.get(0).isLeaf()) {
            return this;// if childisLeaf is true, we will insert the new node inside the current one
        }
        // we need to find the child that will minimize the increase of the MBR on
        // insertion
        ArrayList<Double> insertionArea = new ArrayList<Double>();
        for (Node child : children) {
            Envelope childMbr = child.mbr;
            double areaMbr = childMbr.getArea(); // area(mbr)
            Envelope insertionMbr = new Envelope(childMbr);
            insertionMbr.expandToInclude(p.getCoordinate());
            double areaInsertionMbr = insertionMbr.getArea(); // area(mbr U p)
            insertionArea.add(areaInsertionMbr - areaMbr); // area(mbr U p) - area(mbr)
        }
        System.out.println("End of chooseNode");
        return children.get(insertionArea.indexOf(insertionArea.stream().min(Double::compare).get()));
    }

    public Node addLeaf(Polygon polygon, String label) {
        System.out.println("In addLeaf");
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
        System.out.println("End of addLeaf");
        if (children.size() > MAX_CHILDREN) {
            return split();
        }
        System.out.println("I will return null");
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
        System.out.println("quadratic split");
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
        System.out.println(nodeToIntegrate);
        while (nodeToIntegrate > 0) {
            System.out.println();
            // if one group has so many nodes that all the rest must be assigned to it in
            // order for it to have the minimum number m, assign them and stop
            if (groupA.size() + nodeToIntegrate == MIN_CHILDREN) {
                System.out.println("IF");
                for (int i = children.size() - nodeToIntegrate; i < children.size(); i++) {
                    groupA.add(children.get(i));
                    mbrA.expandToInclude(children.get(i).getMbr());
                }
                break;
            } else if (groupB.size() + nodeToIntegrate == MIN_CHILDREN) {
                System.out.println("ELSE IF");
                for (int i = children.size() - nodeToIntegrate; i < children.size(); i++) {
                    groupB.add(children.get(i));
                    mbrB.expandToInclude(children.get(i).getMbr());
                }
                break;
            } else {
                System.out.println("ELSE");
                // else, choose the node that will increase the area of the mbr the least
                // if the area increase is the same for both groups, choose the one with the
                // smallest area, then the one with the fewest nodes, then randomly choosing
                Node nodeToPlace;
                if (SPLIT_METHOD == "quadratic") {
                    nodeToPlace = pickNextQuadratic(mbrA, mbrB);
                } else {
                    nodeToPlace = pickNextLinear();
                }
                System.out.println(nodeToPlace.getId());
                children.remove(nodeToPlace);
                Envelope mbrAWithNode = new Envelope(mbrA);
                Envelope mbrBWithNode = new Envelope(mbrB);
                mbrAWithNode.expandToInclude(nodeToPlace.getMbr());
                mbrBWithNode.expandToInclude(nodeToPlace.getMbr());
                double areaIncreaseA = mbrAWithNode.getArea() - mbrA.getArea();
                double areaIncreaseB = mbrBWithNode.getArea() - mbrB.getArea();
                if (areaIncreaseA < areaIncreaseB) {
                    addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                } else if (areaIncreaseA > areaIncreaseB) {
                    addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                } else {
                    if (mbrA.getArea() < mbrB.getArea()) {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                    } else if (mbrA.getArea() > mbrB.getArea()) {
                        addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                    } else if (groupA.size() < groupB.size()) {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                    } else if (groupA.size() > groupB.size()) {
                        addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                    } else {
                        addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                        // Random rand = new Random();
                        // if (rand.nextInt(2) == 0) {
                        // addToA(nodeToPlace, mbrA, mbrAWithNode, groupA);
                        // } else {
                        // addToB(nodeToPlace, mbrB, mbrBWithNode, groupB);
                        // }
                    }

                }
            }
            nodeToIntegrate--;
        }
        if (father == null) {
            System.out.println("father is null");
            // we need to create a father
            InternalNode childA = new InternalNode(mbrA, this);
            InternalNode childB = new InternalNode(mbrB, this);
            System.out.println("Group A");
            for (Node a : groupA) {
                System.out.println(a.getId());
            }

            System.out.println("Group B");
            for (Node a : groupB) {
                System.out.println(a.getId());
            }
            childA.children = groupA;
            childB.children = groupB;
            mbr = new Envelope(mbrA);
            mbr.expandToInclude(mbrB);
            children.clear();
            children.add(childA);
            children.add(childB);
            return null;
        } else {
            System.out.println("father is not null");
            children.clear();
            children.addAll(groupA);
            mbr = mbrA;
            InternalNode newNode = new InternalNode(mbrB, father);
            newNode.children = groupB;
            return newNode;

        }
    }

    private Pair<Node, Node> pickSeedsQuadratic() {
        System.out.println("pick seeds quadratic");
        double maxArea = 0;
        // Pair<Node, Node> bestPair = new Pair<Node, Node>(children.get(0),
        // children.get(1));
        Node bestNode1 = children.get(0);
        Node bestNode2 = children.get(1);
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
                    System.out.println("found a best pair");
                    maxArea = area;
                    // bestPair.addAt0(node1);
                    // bestPair.addAt1(node2);
                    bestNode1 = node1;
                    bestNode2 = node2;
                    System.out.println(node1.isLeaf());
                    System.out.println(bestNode1.isLeaf());
                    System.out.println(bestNode2.isLeaf());
                }
            }
        }
        System.out.println("end pick seeds quadratic");
        System.out.println("Returning best pair");
        return new Pair<Node, Node>(bestNode1, bestNode2);
    }

    private Node pickNextLinear() {
        System.out.println("pick next linear");
        // Random rand = new Random();
        // int index = rand.nextInt(children.size());
        Node node = children.get(0);
        System.out.println("end pick next linear");
        return node;
    }

    private Node pickNextQuadratic(final Envelope mbrA, final Envelope mbrB) {
        System.out.println("pick next quadratic");
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
        System.out.println("end pick next quadratic");
        return bestNode;
    }

    public Pair<Node, Node> pickSeedsLinear() {
        // find the entry whose rectangle has
        // the highest low side, and the one
        // with the lowest high side in each dimension
        System.out.println("pick seeds linear");
        double lowestHightSide = Double.MAX_VALUE;
        double highestLowSide = -Double.MAX_VALUE;
        Node bestLowNode = null;
        Node bestHighNode = null;

        for (var child : children) {
            System.out.println(child.id);
            // low side of children
            double lowSide = (child.getMbr().getMinY() / child.getMbr().getHeight()) +
                    (child.getMbr().getMinX() / child.getMbr().getWidth());
            double highSide = (child.getMbr().getMaxY() / child.getMbr().getHeight()) +
                    (child.getMbr().getMaxX() / child.getMbr().getWidth());
            System.out.println(lowSide + " versus " + highestLowSide);
            if (lowSide > highestLowSide) {
                System.out.println("Entering if lowSide");
                highestLowSide = lowSide;
                bestLowNode = child;
            } else if (highSide < lowestHightSide) {
                System.out.println("Entering if highSide");
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
}
