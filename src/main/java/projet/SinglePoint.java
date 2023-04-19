/*
 *    Annexe pour l'énoncé du projet d'INFOF203 (2022-2023)
 *
 */

// package be.ulb.infof203.projet;
package projet;

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

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 *
 * <p>This is the GeoTools Quickstart application used in documentation a and tutorials. *
 */
public class SinglePoint {

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        // String
        // filename="../projetinfof203/data/sh_statbel_statistical_sectors_31370_20220101.shp/sh_statbel_statistical_sectors_31370_20220101.shp";

        // String
        // filename="../projetinfof203/data/WB_countries_Admin0_10m/WB_countries_Admin0_10m.shp";
        // String filename="/home/cbr/50m_cultural/WB_countries_Admin0_10m.shp";
        // String filename="/home/cbr/Unif2/50m_cultural/WB_countries_Admin0_10m.shp";
        String filename = "/home/cbr/Unif2/WB_countries_Admin0_10m/WB_countries_Admin0_10m.shp";
        // String filename="../projetinfof203/data/communes-20220101-shp/communes-20220101.shp";
        // String filename="/home/cbr/50m_cultural/ne_10m_admin_1_sel.shp";

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
        // Point p = gb.point(152183, 167679);// Plaine
        // Point p = gb.point(4.4, 50.8);//
        // Point p = gb.point(58.0, 47.0);
        // Point p = gb.point(10.6,59.9);// Oslo

        Point p = gb.point(-70.9, -33.4); // Santiago
                                          // Point p = gb.point(169.2, -52.5);//NZ

        // Point p = gb.point(172.97365198326708, 1.8869725782923172);

        // Point p = gb.point(r.nextInt((int) global_bounds.getMinX(), (int)
        // global_bounds.getMaxX()), r.nextInt((int) global_bounds.getMinY(), (int)
        // global_bounds.getMaxY()));

        SimpleFeature target = null;

        System.out.println(all_features.size() + " features");
        System.out.println("Je print les coordonnées du point: " + p.getX() + " " + p.getY());

        Envelope env = null;
        try (SimpleFeatureIterator iterator = all_features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                //
                MultiPolygon polygon = (MultiPolygon)feature.getDefaultGeometry();
                System.out.println("Name of the polygone: " + feature.getAttribute("NAME_FR"));
                String s = feature.getAttribute("NAME_FR").toString();
                System.out.println("This is the string I got : " + s);
                // if the name of the feature is France then break
                if (s.equals("France")) {
                    System.out.println("I found France");
                    target = feature;
                    env = polygon.getEnvelopeInternal();
                    break;
                }
                // System.out.println("Name of polygone: " + feature.getAttribute("NAME_FR"));
                // // var tesst = feature.getNbGeometries();
                // int test = polygon.getNumGeometries();
                // System.out.println("Nombre de polygones: " + test);
                // for (int i = 0; i < test; i++) {
                //     Polygon poly = (Polygon)polygon.getGeometryN(i);
                //     Envelope env2 = poly.getEnvelopeInternal();
                // }
                //
                // // System.out.println(env.expandToInclude());
                // if (polygon != null && polygon.contains(p)) {
                //     target = feature;
                //     env = polygon.getEnvelopeInternal();
                //     System.out.println("Envelope: minX = " + env.getMinX() +
                //                        ", minY =  " + env.getMinY() + ", maxX =  " +
                //                        env.getMaxX() +
                //                        ", max Y =  " + env.getMaxY());
                //     System.out.println("coordonnées du mbr : " + env.toString());
                //     break;
                // }
            }
        }

        if (target == null)
            System.out.println("Point not in any polygon!");

        else {
            for (Property prop : target.getProperties()) {
                if (prop.getName().toString() != "the_geom") {
                    System.out.println(prop.getName() + ": " + prop.getValue());
                }
            }
        }

        MapContent map = new MapContent();
        map.setTitle("Projet INFO-F203");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        ListFeatureCollection collection = new ListFeatureCollection(featureSource.getSchema());
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureSource.getSchema());

        // Add target polygon
        collection.add(target); // pays

        // Add Point
        Polygon c = gb.circle(p.getX(), p.getY(), all_features.getBounds().getWidth() / 200, 10);
        featureBuilder.add(c);
        collection.add(featureBuilder.buildFeature(null));

        // Add MBR
        // Envelope test = new Envelope(5, -5, 5, -5);
        // Envelope test1 = new Envelope(10, 15, 10, 15);
        // Envelope test = new Envelope(-5, 5, -5, 5);
        // make an Envelope 100x50 centered in the map
        // Envelope test = new Envelope(-100, 100, -50, 50);
        // Envelope test2 = test.copy();
        // Envelope test2 = new Envelope(test);
        // test2.expandToInclude(test1);
        if (target != null) {
            // featureBuilder.add(gb.box(env.getMinX(), env.getMinY(), env.getMaxX(),
            // env.getMaxY())); collection.add(featureBuilder.buildFeature(null));
            MultiPolygon multiPolygon = (MultiPolygon)target.getDefaultGeometry();
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                Polygon poly = (Polygon)multiPolygon.getGeometryN(i);
                Envelope env2 = poly.getEnvelopeInternal();
                featureBuilder.add(
                    gb.box(env2.getMinX(), env2.getMinY(), env2.getMaxX(), env2.getMaxY()));
                collection.add(featureBuilder.buildFeature(null));
                // print the name of poly
                System.out.println("Name of the polygone: " + target.getAttribute("NAME_FR"));
            }
        }

        Style style2 = SLD.createLineStyle(Color.red, 2.0f);
        Layer layer2 = new FeatureLayer(collection, style2);
        map.addLayer(layer2);

        // Now display the map
        JMapFrame.showMap(map);
    }
}
