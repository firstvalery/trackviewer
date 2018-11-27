package ru.smartsarov.trackviewer;
import static ru.smartsarov.trackviewer.postgres.tables.TrackingData.*;
import static ru.smartsarov.trackviewer.postgres.tables.VehicleData.*;
import org.jooq.DSLContext;
import org.jooq.JSONFormat;
import org.jooq.Record8;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.JSONFormat.RecordFormat;
import org.jooq.Record;
import org.jooq.impl.DSL;

import ru.smartsarov.trackviewer.postgres.tables.records.TrackingDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.VehicleDataRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import ru.smartsarov.trackviewer.gpxschema.ExtensionsTypeSeg;
import ru.smartsarov.trackviewer.gpxschema.ExtensionsTypeWpt;
import ru.smartsarov.trackviewer.gpxschema.GpxType;
import ru.smartsarov.trackviewer.gpxschema.MetadataType;
import ru.smartsarov.trackviewer.gpxschema.TrkType;
import ru.smartsarov.trackviewer.gpxschema.TrksegType;
import ru.smartsarov.trackviewer.gpxschema.WptType;


public class Trackviewer {
	/** 
	 *Getting conect to postgresql DB. Look for Properies file
	 *return Connection conn  
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Class.forName("org.postgresql.Driver");		
		
		   conn = DriverManager.getConnection("jdbc:postgresql://"+
				   			Props.get().getProperty("host","localhost")+":"+
				   				Props.get().getProperty("port", "5432")+"/"+
				   					Props.get().getProperty("db","transport"), Props.get());
		   conn.setAutoCommit(false);
		   return conn;
	} 
		
	/**
	 *This method retrieves the data from the file, checks for the existence of the vehicle, and inserts the data into the tracking table.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static int InsertFileInto(String fileName) throws IOException, ClassNotFoundException, SQLException   {

		
		List<TrackingDataRecord>fileData = new ArrayList<>();
		Set<VehicleDataRecord> vehicleDataRecordSet = new HashSet<>();
		
		//reading file to stream and building set of transport data
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {	
			stream.skip(1).forEach(line->{ // skip the first definition string of CSV file	
				String splitLine[] = line.split(",");
				if (splitLine.length > 9) {
					vehicleDataRecordSet.add(new VehicleDataRecord(0,splitLine[7], splitLine[8], splitLine[6]));
					
					TrackingDataRecord tdr = new TrackingDataRecord(new Timestamp(Long.valueOf(splitLine[1])*1000), 
																	new BigDecimal(splitLine[2]),
																	new BigDecimal(splitLine[3]),
																	Short.valueOf(splitLine[4]),
																	Short.valueOf(splitLine[5]),
																	(splitLine.length>=10 ? splitLine[9] : ""),
																	Arrays.asList(splitLine[7], splitLine[8], splitLine[6]).hashCode(),
																	null);
					tdr.changed(TRACKING_DATA.ID, false);
					fileData.add(tdr);
				}else {
					//TODO
					//add into the logger info
				}
			});	
		}
		
		//working with DB
		try (Connection conn = getConnection()) {//remove from set vehicle_data existing data in table VehicleData
			 DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
			 Result<VehicleDataRecord> vehicleDataRecordResult = dsl
			    		 									.selectFrom(VEHICLE_DATA)
			    		 										.fetch();
			 vehicleDataRecordResult.stream().forEach(j->{
			    vehicleDataRecordSet.remove(j.value1(0));
			 });
			    		 														
			//Inserting new records in table VehicleData	   
			 if(!vehicleDataRecordSet.isEmpty()) {		    	 
			    vehicleDataRecordSet.stream().forEach(j->{	 
			    	j.changed(VEHICLE_DATA.ID, false); //set default value to field ID 
			    });
			    dsl.batchInsert(vehicleDataRecordSet).execute();
			    conn.commit();
			     } 
			 //Selecting Records from vehicleData table for finding ID as foreign key for tracking data record. 
			 vehicleDataRecordResult = dsl
						.selectFrom(VEHICLE_DATA)
							.fetch();

			 Map<Integer, Integer> vehicleDataMap = new HashMap<>();
			 vehicleDataRecordResult.stream().forEach(j->{
				 vehicleDataMap.put(Arrays.asList(j.getUid(), j.getNumber(), j.getType()).hashCode(),j.getId());
			});
			

			//Preparing data for batch insert into TrackingData table
			fileData.stream().forEach(j->{
				j.setVehicleUid(vehicleDataMap.get(j.getVehicleUid()));
			});
				
			int rs[] = dsl.batchInsert(fileData).execute();
			conn.commit();
				
			return rs.length;			
		}		
	}
	
	
	/**
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 * 
	 */	
	public static List<Record8<Timestamp, Short, BigDecimal, BigDecimal, Short, String, String, String>> 
		selectTrackData(long min_ts, long max_ts, int vehicleUid)	
										throws ClassNotFoundException, SQLException{
			
			try (Connection conn = getConnection()) {
		         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
		         List<Record8<Timestamp, Short, BigDecimal, BigDecimal, Short, String, String, String>> result = 
		        		 				dsl.select(TRACKING_DATA.TIMESTAMP, 
		         					  		TRACKING_DATA.VELOCITY, 
		         					  		TRACKING_DATA.LATITUDE, 
		         					  		TRACKING_DATA.LONGITUDE, 
		         					  		TRACKING_DATA.DIRECTION, 
		         					  		TRACKING_DATA.ADDITIONAL,
		         					  		VEHICLE_DATA.NUMBER,
		         					  		VEHICLE_DATA.UID)
				         							.from(TRACKING_DATA)
				         								.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(TRACKING_DATA.VEHICLE_UID))
				         									.where(TRACKING_DATA.TIMESTAMP.between(new Timestamp(min_ts), new Timestamp(max_ts))
				         										.and(VEHICLE_DATA.ID.eq(vehicleUid)))
		         													.orderBy(TRACKING_DATA.TIMESTAMP)
		         														.fetch();       
		            
		         	return result;
		         	
			}  
	}	

	/**
	 * Marshaling the data
	 * @throws JAXBException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws DatatypeConfigurationException 
	 */	
	public static String marshalTrackData(long min_ts, long max_ts, int vehicleUid) throws SQLException, JAXBException, ClassNotFoundException, DatatypeConfigurationException {
		List<Record8<Timestamp, Short, BigDecimal, BigDecimal, Short, String, String, String>> result = selectTrackData(min_ts, max_ts, vehicleUid);
		   	
		GpxType gpx = initGpx();
		gpx.getTrk().get(0).setName(result.get(0).getValue(VEHICLE_DATA.NUMBER));
		gpx.getTrk().get(0).setDesc("The vehicle track");
		
	    int segmentIndex = 0;    
	    WptType tmpWpt = new WptType();  
	    tmpWpt.setLat(new BigDecimal(0));
	    tmpWpt.setLon(new BigDecimal(0));
	    tmpWpt.setExtensions(new ExtensionsTypeWpt());
	    tmpWpt.getExtensions().setUnixtimestamp(0L);
	    
	    GregorianCalendar cal = new GregorianCalendar();
	    //Building of track segments     	
	   for (int i=0; i<result.size(); i++) {
	       if (!result.get(i).get(TRACKING_DATA.LATITUDE).equals( tmpWpt.getLat()) ||
	         				!result.get(i).get(TRACKING_DATA.LONGITUDE).equals(tmpWpt.getLon())) {// check for new coordinates
	         	
	    	   //Craeating wpt by result[i]
	    	   WptType wpt = wptBuild(result.get(i));
	    	   
	    	   //Set TimeStamp newXMLGregorianCalendar Format.
	    	   //For mem economy use one GregorianCalendar Instance defining early outside of cycle "for"
	           cal.setTimeInMillis(result.get(i).getValue(TRACKING_DATA.TIMESTAMP).getTime());	
	         	wpt.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
	           
	         	long delta = 0;
	         	if((delta = (wpt.getExtensions().getUnixtimestamp() - tmpWpt.getExtensions().getUnixtimestamp())/1000) > Integer.valueOf(Props.get().getProperty("suspense","300"))){
	         		segmentIndex++;
	         		gpx.getTrk().get(0).getTrkseg().add(new TrksegType());//new segment building
	         		if (segmentIndex>1) {// from second segment begins to set waiting in sec
	         			gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).setExtensions(new ExtensionsTypeSeg());
	         			gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).getExtensions().setWaiting(delta);
	         		}	
	         	}
	         		gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).getTrkpt().add(wpt);
	         		tmpWpt = wpt;
	         }
	    }
	   return jaxbTrackMarshal(gpx);
	}
	
	/**
	 * jaxb marshaler
	 * returns XML GPX document as String
	 * @throws JAXBException 
	 */
	static private String jaxbTrackMarshal(GpxType gpx) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance( GpxType.class );
		   Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		   jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		    		
		   JAXBElement<GpxType> jaxbElement = new JAXBElement<GpxType>(new QName(TrackviewerConstants.XSD_TYPE_PATH,
				   										TrackviewerConstants.GPX_ROOT_NAME), GpxType.class, gpx);
		   StringWriter sw = new StringWriter();
		   jaxbMarshaller.marshal(jaxbElement, sw);
		   return sw.getBuffer().toString();
	}
	
	/**
	 * Creates wpt Instance from result data
	 */
	static private WptType wptBuild(Record result) {
 	    WptType wpt = new WptType();
 	   
    	wpt.setLat(result.getValue(TRACKING_DATA.LATITUDE));
    	wpt.setLon(result.getValue(TRACKING_DATA.LONGITUDE));
    	wpt.setMagvar(BigDecimal.valueOf(result.getValue(TRACKING_DATA.DIRECTION)));
    	wpt.setExtensions(new ExtensionsTypeWpt());
    	wpt.getExtensions().setUnixtimestamp(result.getValue(TRACKING_DATA.TIMESTAMP).getTime());
    	wpt.getExtensions().setVelocity(BigInteger.valueOf(result.getValue(TRACKING_DATA.VELOCITY)));
    //	wpt.setTime(value);
		
		return wpt;	
	}
	
	/**
	 *Prepare GpxType for marshaling  
	 * 
	 */
	private static GpxType initGpx() {
		GpxType gpx = new GpxType();
		gpx.setCreator("smartsarov.ru");
		gpx.setVersion("1.0");
		gpx.setMetadata(new MetadataType());
		gpx.getTrk().add(new TrkType());
		
		return gpx;	
	}
	
	
	/**
	 * Returns JSON of all vehicles registered in DB
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static String getVehicleList() throws ClassNotFoundException, SQLException  {
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	         Result<VehicleDataRecord> result = dsl.selectFrom(VEHICLE_DATA).fetch(); 
	        
	         return result.formatJSON(new JSONFormat().header(false).recordFormat(RecordFormat.OBJECT));
		}
	}

}
