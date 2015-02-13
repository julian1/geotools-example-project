package org.geotools.tutorial.quickstart;

/*package org.geotools.jdbc;  */


import java.sql.Connection;
import java.util.HashMap;
import java.util.List; 
import java.util.LinkedList; 
import java.io.File; 
import java.io.ByteArrayOutputStream; 
import java.io.FileOutputStream; 
import java.net.URL;


import org.geotools.jdbc.JDBCDataStore;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;
import org.geotools.factory.GeoTools;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.data.Query;
import java.lang.reflect.Method;

import org.postgresql.ds.PGPoolingDataSource;
import org.geotools.data.simple.SimpleFeatureSource;




import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.GML;
import org.geotools.GML.Version;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.WKTReader2;

import org.opengis.feature.simple.SimpleFeatureType; 
import org.opengis.feature.simple.SimpleFeature; 




public class Quickstart {


	public static void myfunc() {

		try { 

			// Ok, is it possible to have a simpleFeatureType without Geometry?.

			SimpleFeatureType TYPE = DataUtilities.createType("location", "geom:Point,name:String");
			// SimpleFeatureType TYPE = DataUtilities.createType("location", "name:String");

			File locationFile = new File("location.xsd");
			locationFile = locationFile.getCanonicalFile();
			locationFile.createNewFile();

			URL locationURL = locationFile.toURI().toURL();
			URL baseURL = locationFile.getParentFile().toURI().toURL();

			FileOutputStream xsd = new FileOutputStream(locationFile);

			GML encode = new GML(Version.GML2);
			encode.setBaseURL(baseURL);
			encode.setNamespace("location", locationURL.toExternalForm());
			encode.encode(xsd, TYPE);

			xsd.close();


			
			WKTReader2 wkt = new WKTReader2();
			List<SimpleFeature> collection = new LinkedList<SimpleFeature>();
			collection.add(SimpleFeatureBuilder.build(TYPE, new Object[] { wkt.read("POINT (1 2)"),"name1" }, null));
			collection.add(SimpleFeatureBuilder.build(TYPE, new Object[] { wkt.read("POINT (4 4)"),"name2" }, null));

			ByteArrayOutputStream xml = new ByteArrayOutputStream();

			GML encode2 = new GML(Version.GML2);
			encode2.setLegacy(true);
			encode2.setBaseURL(baseURL);
			encode2.setNamespace("location", "location.xsd");
			encode2.encode(xml,  new ListFeatureCollection(TYPE, collection));

			xml.close();

			String gml = xml.toString();



/*			GML encode = new GML(Version.GML2);				
			
			System.out.println( "GML object " + encode + "\n");

			SimpleFeatureType TYPE = DataUtilities.createType("location", "geom:Point,name:String");

			File locationFile = new File("location.xsd");
			locationFile = locationFile.getCanonicalFile();
			locationFile.createNewFile();
*/
        // System.out.println( encode);
		} catch( Exception  e) {
			System.out.println("Exception " + e);
		}	
	}


  /*
    Example taken from
    JDBCJNDIDataSourceTest.java
  */
    public static void main(String[] args) throws Exception {


        System.out.println( "hi there\n");

		myfunc();
		if( true ) return;

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

        Connection conn = store.getDataSource().getConnection();


        System.out.println( "dialect");
		System.out.println( store.getSQLDialect() );


		// oohhhh
        //System.out.println( "typename");
		//System.out.println( typeName );

		// bluenet_datasets


		// get our aggregate storeGetAggregateValueMethod
		Method storeGetAggregateValueMethod = store.getClass().getDeclaredMethod("getAggregateValue",
			org.opengis.feature.FeatureVisitor.class,
			org.opengis.feature.simple.SimpleFeatureType.class,
			org.geotools.data.Query.class,
			java.sql.Connection.class
		);
		storeGetAggregateValueMethod.setAccessible(true);


        String [] typeNames = store.getTypeNames();
        String typeName = typeNames[0];

        SimpleFeatureSource source = store.getFeatureSource(typeName);

		System.out.println( "typeName is " + typeName );


        FeatureType schema = source.getSchema();
//        String name = schema.getGeometryDescriptor().getLocalName();
		// "title|station";
        String name = "title";

		//Filter filter = null;//CQL.toFilter("title != 100" );
		//        Query  = new Query(typeName, filter, new String[] { "stationxxx" });

        Query query = new Query( null, null, new String[] { } );


		UniqueVisitor visitor = new UniqueVisitor( name);

		storeGetAggregateValueMethod.invoke(store, visitor, schema, query, conn );

        System.out.println( visitor.getResult().toList() .size() );

		//store.closeSafe(conn);
        //store.dispose();


    }

}
