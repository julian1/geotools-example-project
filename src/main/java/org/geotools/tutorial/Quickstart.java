package org.geotools.tutorial.quickstart;

/*package org.geotools.jdbc;  */


import java.sql.Connection;
 
import java.util.HashMap;


import org.geotools.jdbc.JDBCDataStore;
import org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory;

import org.opengis.feature.type.FeatureType;


import org.geotools.factory.GeoTools;


import org.opengis.filter.Filter;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;



import org.postgresql.ds.PGPoolingDataSource;

import org.geotools.data.simple.SimpleFeatureSource;

import org.geotools.feature.visitor.UniqueVisitor;




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

        FeatureType schema = source.getSchema();
//        String name = schema.getGeometryDescriptor().getLocalName();
		// "title|station";
        String name = "title";

		//Filter filter = null;//CQL.toFilter("title != 100" );
//        Query query = new Query(typeName, filter, new String[] { "stationxxx" });

        Query query = new Query( null, null, new String[] { } );


		UniqueVisitor visitor = new UniqueVisitor( name);

		storeGetAggregateValueMethod.invoke(store, visitor, schema, query, conn );

        System.out.println( visitor.getResult().toList() .size() );

		//store.closeSafe(conn);
        //store.dispose();


    }

}
