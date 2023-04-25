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

        GeometryBuilder gb = new GeometryBuilder();
        Point p = gb.point(-70.9, -33.4); // Santiago

        try (SimpleFeatureIterator itr = all_features.features()) {
            while (itr.hasNext()) {
                SimpleFeature f = itr.next();
                MultiPolygon mp = (MultiPolygon) f.getDefaultGeometry();
                System.out.println("Adding " + f.getAttribute("NAME_FR").toString());
                rtree.addLeaf(mp, f.getAttribute("NAME_FR").toString());
            }
        }

        System.out.println(rtree.toString());
        // MapContent map = new MapContent();
        // map.setTitle("Projet INFO-F203");
        //
        // Style style = SLD.createSimpleStyle(featureSource.getSchema());
        // Layer layer = new FeatureLayer(featureSource, style);
        // map.addLayer(layer);
        //
        // ListFeatureCollection collection = new
        // ListFeatureCollection(featureSource.getSchema());
        // SimpleFeatureBuilder featureBuilder = new
        // SimpleFeatureBuilder(featureSource.getSchema());

    }
}
