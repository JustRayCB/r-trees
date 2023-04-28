package projet;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Les arguments suivants ont été passés :");
        int MAX_CHILDREN;
        int MIN_CHILDREN;
        String SPLIT_METHOD;
        try {
            if (args.length != 3) {
                MAX_CHILDREN = 50;
                MIN_CHILDREN = 25;
                SPLIT_METHOD = "linear";
                System.err.println("Usage: java Main <MAX_CHILDREN> <MIN_CHILDREN> <SPLIT_METHOD>");
                System.out.println("Using default values : MAX_CHILDREN = " + MAX_CHILDREN
                        + ", MIN_CHILDREN = " + MIN_CHILDREN + ", SPLIT_METHOD = " + SPLIT_METHOD);
            } else {
                MAX_CHILDREN = Integer.parseInt(args[0]);
                MIN_CHILDREN = Integer.parseInt(args[1]);
                SPLIT_METHOD = args[2];
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            MAX_CHILDREN = 50;
            MIN_CHILDREN = 25;
            SPLIT_METHOD = "linear";
        }
        RTree.setMaxChildren(MAX_CHILDREN);
        RTree.setMinChildren(MIN_CHILDREN);
        RTree.setSplitMethod(SPLIT_METHOD);
        RTree rtree = new RTree();
        String filename = "data/world_countries/WB_countries_Admin0_10m.shp";

        File file = new File(filename);
        if (!file.exists())
            throw new RuntimeException("Shapefile does not exist.");

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        SimpleFeatureCollection all_features = featureSource.getFeatures();
        store.dispose();

        ReferencedEnvelope global_bounds = featureSource.getBounds();

        Random r = new Random();
        GeometryBuilder gb = new GeometryBuilder();
        // Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int)
        // global_bounds.getMaxX()),
        // r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));
        Point p = gb.point(-70.9, -33.4); // Santiago
        // Point p = gb.point(-118.24, 28.98); // Madrid
        // Point p = gb.point(-66.54, -55.24); // Mexique

        long start = System.currentTimeMillis();
        try (SimpleFeatureIterator itr = all_features.features()) {
            while (itr.hasNext()) {
                SimpleFeature f = itr.next();
                MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
                rtree.addLeaf(mp, f.getAttribute("NAME_FR").toString());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Temps d'insertion : " + (end - start) + " ms");
        long CreationTime = end - start;

        // rtree.print();
        MapContent map = new MapContent();
        map.setTitle("Projet INFO-F203");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        ListFeatureCollection collection = new ListFeatureCollection(featureSource.getSchema());
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureSource.getSchema());
        ListFeatureCollection collection2 = new ListFeatureCollection(featureSource.getSchema());
        SimpleFeatureBuilder featureBuilder2 = new SimpleFeatureBuilder(featureSource.getSchema());
        Polygon c = gb.circle(p.getX(), p.getY(), all_features.getBounds().getWidth()
                / 200, 10);
        featureBuilder2.add(c);
        collection2.add(featureBuilder2.buildFeature(null));
        rtree.parseTree(collection, featureBuilder, gb);
        start = System.currentTimeMillis();
        Node n = rtree.search(p);
        end = System.currentTimeMillis();
        long SearchTime = end - start;
        System.out.println("Temps de recherche : " + (end - start) + " ms");
        if (n != null) {
            System.out.println("Found " + n.toString());
        } else {
            System.out.println("Pas trouvé");
        }

        System.out.println("Le nombre total de node : " + rtree.getNbrNodes());
        Style style2 = SLD.createLineStyle(Color.red, 2.0f);
        Style style3 = SLD.createLineStyle(Color.blue, 2.0f);
        Layer layer2 = new FeatureLayer(collection, style2);
        Layer layer3 = new FeatureLayer(collection2, style3);
        map.addLayer(layer2);
        map.addLayer(layer3);

        try {
            FileWriter writer;
            if (rtree.getSplitMethod().equals("linear")) {
                writer = new FileWriter("performance/perfLinear.csv", true);
            } else {
                writer = new FileWriter("performance/perfQuadratic.csv", true);
            }
            writer.append(CreationTime + "," + SearchTime + "," + rtree.getNbrNodes() + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("MAX_CHILDREN = " + MAX_CHILDREN + ", MIN_CHILDREN = " + MIN_CHILDREN
                + ", SPLIT_METHOD = " + SPLIT_METHOD);
        System.out.println("methode de root " + rtree.getSplitMethod());
        // Now display the map
        JMapFrame.showMap(map);

    }
}
