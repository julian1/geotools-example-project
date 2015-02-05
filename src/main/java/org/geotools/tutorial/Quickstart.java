package org.geotools.tutorial.quickstart;

/*package org.geotools.jdbc;  */

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.geotools.data.DataAccess;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCJNDIDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;


import org.geotools.factory.GeoTools;


import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;



import org.postgresql.ds.PGConnectionPoolDataSource; 
import org.postgresql.ds.PGPoolingDataSource; 

//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.visitor.CountVisitor;

import org.geotools.data.Query;

import java.lang.reflect.Method;


public class Quickstart {

  /*
    Example taken from
    JDBCJNDIDataSourceTest.java
  */
    public static void main(String[] args) throws Exception {


        System.out.println( "hi there\n");

        try {

            Properties props = new Properties();

// https://blogs.oracle.com/randystuph/entry/injecting_jndi_datasources_for_junit

            // set up environment 
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, 
                "org.apache.naming");            
            InitialContext ic = new InitialContext();

            // jndi name
            ic.createSubcontext("java:");
            ic.createSubcontext("java:/comp");
            ic.createSubcontext("java:/comp/env");
            ic.createSubcontext("java:/comp/env/jdbc");


            // PGConnectionPoolDataSource ds = new PGConnectionPoolDataSource();
            PGPoolingDataSource ds = new PGPoolingDataSource() ; 

            System.out.println( "here4\n");

			ds.setDataSourceName("my unique name" );

/*            ds.setServerName("dbprod.emii.org.au");
            ds.setUser("jfca");
            ds.setPassword("****");
*/

			// localhost
            //ds.setServerName("131.217.38.46");
            ds.setServerName("127.0.0.1");
            ds.setUser("meteo");
            ds.setPassword("meteo");

            ds.setPortNumber( 5432 );
            ds.setDatabaseName("harvest");
            ds.setSsl( true );
			ds.setSslfactory("org.postgresql.ssl.NonValidatingFactory");

        
            System.out.println( "here5\n");

            // jndi association
            ic.bind("java:/comp/env/jdbc/harvest_read", ds);

            System.out.println( "registered jndi\n");

 
            // set the GeoTools initial context
            GeoTools.init( ic );

            // problem is this initial context is not being read ... 
        } catch (NamingException e) {
              System.out.println("***** Exception Uggh "  + e);
        }




        HashMap params = new HashMap();

        params.put( "schema", "legacy_bluenet_aodn");
        params.put( "dbtype", "postgis");
        params.put( "Loose bbox", "true");
        params.put( "Expose primary keys", "true");
        params.put( "fetch size", "1000");
        params.put( "Max open prepared statements" , "50");
        params.put( "preparedStatements","true");
        params.put( "jndiReferenceName", "java:/comp/env/jdbc/harvest_read");
        params.put( "namespace", "aodn");



        PostgisNGJNDIDataStoreFactory factory = new PostgisNGJNDIDataStoreFactory();

        JDBCDataStore store = (JDBCDataStore) factory.createDataStore(params);

		// we need the non validating connection details ?


        System.out.println( "*******\n");

        System.out.println( store);
        System.out.println( store.getFetchSize() );

        Connection conn = store.getDataSource().getConnection();

        System.out.println( "conn");
        System.out.println( conn );


        System.out.println( "dialect");
		System.out.println( store.getSQLDialect() );


		// oohhhh
        String typeName = store.getTypeNames()[0];
        System.out.println( "typename");
		System.out.println( typeName );

		// bluenet_datasets

        SimpleFeatureSource featureSource = store.getFeatureSource(typeName);
        System.out.println( "featureSource");
		System.out.println( featureSource);


		// see http://docs.geotools.org/latest/javadocs/org/geotools/data/Query.html
		// for details on queries and filters
		Query query = new Query("countries");

        System.out.println( "count");
		System.out.println( featureSource.getCount( query ) );


		// want to pull values out, 
		
		//SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
		//tb.setAttributes( store.getSchema().getAttributeDescriptors());
		//tb.setAttributes( store.getAttributeDescriptors());


//		protected int getCount(SimpleFeatureType featureType, Query query, Connection cx)


//	error: getAggregateValue(FeatureVisitor,SimpleFeatureType,Query,Connection) has protected access in JDBCDataStore

        System.out.println( "list of methods");

		for(Method m : store.getClass().getDeclaredMethods()) { 
			if(m.getName().equals("getAggregateValue")) { 
				System.out.println( "whoot the method");
				System.out.println (m);
			}
		}

// can see the method there,
//			protected java.lang.Object org.geotools.jdbc.JDBCDataStore.getAggregateValue(org.opengis.feature.FeatureVisitor,org.opengis.feature.simple.SimpleFeatureType,org.geotools.data.Query,java.sql.Connection) throws java.io.IOException


        System.out.println( "***************");
        System.out.println( "before get method");

		Method method = store.getClass().getDeclaredMethod("getAggregateValue", 
			org.opengis.feature.FeatureVisitor.class,
			org.opengis.feature.simple.SimpleFeatureType.class,
			org.geotools.data.Query.class,
			java.sql.Connection.class
		);
		//Method method = store.getClass().getMethod("getAggregateValue" );

        System.out.println("method");
		System.out.println("method = " + method.toString());


 
 //       CountVisitor v = new CountVisitor();
//        store.getAggregateValue(v,null,query, null /*cx */);
 //       return v.getCount();



  
		store.closeSafe(conn);
        store.dispose();

		


/*
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);

        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();
        map.setTitle("Quickstart");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        // Now display the map
        JMapFrame.showMap(map);
*/
    }

}
