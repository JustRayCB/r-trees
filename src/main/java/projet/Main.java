package projet;

import org.javatuples.Pair;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;
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
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello");
        System.out.println("World");
        RTree rtree = new RTree();
        String filename = "data/WB_countries_Admin0_10m/WB_countries_Admin0_10m.shp";

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
        Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int) global_bounds.getMaxX()),
                r.nextInt((int) global_bounds.getMinY(), (int) global_bounds.getMaxY()));
        // Point p = gb.point(-70.9, -33.4); // Santiago
        // Point p = gb.point(-118.24, 28.98); // Madrid
        // Point p = gb.point(-66.54, -55.24); // Madrid

        int i = 0;
        try (SimpleFeatureIterator itr = all_features.features()) {
            while (itr.hasNext()) {
                SimpleFeature f = itr.next();
                MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
                rtree.addLeaf(mp, f.getAttribute("NAME_FR").toString());
            }
        }

        rtree.print();
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
        Node n = rtree.search(p);
        if (n != null) {
            System.out.println("Found " + n.toString());
        } else {
            System.out.println("Pas trouvé");
        }

        Style style2 = SLD.createLineStyle(Color.red, 2.0f);
        Style style3 = SLD.createLineStyle(Color.blue, 2.0f);
        Layer layer2 = new FeatureLayer(collection, style2);
        Layer layer3 = new FeatureLayer(collection2, style3);
        map.addLayer(layer2);
        map.addLayer(layer3);

        // Now display the map
        JMapFrame.showMap(map);

    }
}
