package ru.smartsarov.trackviewer;
import static ru.smartsarov.trackviewer.postgres.tables.TrackingData.*;
import static ru.smartsarov.trackviewer.postgres.tables.VehicleData.*;
import static ru.smartsarov.trackviewer.postgres.tables.RegionRb.*;
import ru.smartsarov.trackviewer.postgres.tables.records.TrackingDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.VehicleDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.RegionRbRecord;
import org.jooq.DSLContext;
import org.jooq.JSONFormat;
import org.jooq.Record8;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.JSONFormat.RecordFormat;
import org.jooq.Record;
import org.jooq.Record12;
import org.jooq.impl.DSL;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;





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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import ru.smartsarov.trackviewer.JsonTrack.JsonTrack;
import ru.smartsarov.trackviewer.JsonTrack.ReportForVehicle;
import ru.smartsarov.trackviewer.JsonTrack.Segment;
import ru.smartsarov.trackviewer.JsonTrack.TrackPoint;
import ru.smartsarov.trackviewer.JsonTrack.WaitTrackPoint;
import ru.smartsarov.trackviewer.gpxschema.ExtensionsTypeSeg;
import ru.smartsarov.trackviewer.gpxschema.ExtensionsTypeTrk;
import ru.smartsarov.trackviewer.gpxschema.ExtensionsTypeWpt;
import ru.smartsarov.trackviewer.gpxschema.GpxType;
import ru.smartsarov.trackviewer.gpxschema.MetadataType;
import ru.smartsarov.trackviewer.gpxschema.TrkType;
import ru.smartsarov.trackviewer.gpxschema.TrksegType;
import ru.smartsarov.trackviewer.gpxschema.WptType;
import ru.smartsarov.trackviewer.jsoninsert.JsonInsert;
import ru.smartsarov.trackviewer.jsoninsert.Vehicle;


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
	 * Method for reading data From file
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static String InsertFileInto(String fileName) throws IOException, ClassNotFoundException, SQLException {
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {	
			return getJsonMessage("was inserted records: ");// skip the first definition string of CSV file	
		}
	}
	
	
	/**
	 * returns message in json format {"message": "message text"}
	 * 
	 */
	 public static String getJsonMessage(String str) {
		  return "{\"message\":\"" + str + "\"}";
	 }
	
		
	/**
	 *This method retrieves the data by the List, checks for the existence of the vehicle, region, and inserts the data into the tracking table.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static int InsertListInto(List<JsonInsert>incomingDataList) throws ClassNotFoundException, SQLException  {
		List<TrackingDataRecord>fileData = new ArrayList<>();
		Set<VehicleDataRecord> vehicleDataRecordSet = new HashSet<>();
		Set<String> numberSet= new HashSet<>(); 
		Set<RegionRbRecord> regionRbRecordSet = new HashSet<>();
		
		boolean commitFl = false;
		incomingDataList.stream()
				.filter(j->j.getVehicle().getUid()!=null && j.getVehicle().getNumber()!=null )//отсеиваем записи без указания номера треккера и госномера
				.forEach(incData->{
			//build number set
			
					
			//build set of vehicle
			vehicleDataRecordSet.add(new VehicleDataRecord(0, 
															incData.getVehicle().getUid(),
															incData.getVehicle().getNumber().toLowerCase().replaceAll("\\s", ""),			
															incData.getVehicle().getType(),
															incData.getVehicle().getOwner(),
															incData.getVehicle().getModel(),
															incData.getVehicle().getDescription()));
			
			//build set of region
			regionRbRecordSet.add(new RegionRbRecord(0,incData.getRegion()));
			
			
			//Creating collection for batch Insert with  Hashcodes of region and vehicle
			
			TrackingDataRecord tdr = new TrackingDataRecord( 
										new Timestamp(1000*incData.getTime()),
										incData.getLongitude(),
										incData.getLatitude(),
										incData.getVelocity(),
										incData.getDirection(),
										Arrays.asList(incData.getVehicle().getNumber()
												.toLowerCase().replaceAll("\\s", "")).hashCode(),
										null,
										Arrays.asList(incData.getRegion()).hashCode(),
										incData.getExtra().get16());
			tdr.changed(TRACKING_DATA.ID, false);
			fileData.add(tdr);	
		});
					
		//working with DB
			try (Connection conn = getConnection()){
				DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
				Result<VehicleDataRecord> vehicleDataRecordResult = dsl
					    		 									.selectFrom(VEHICLE_DATA)
					    		 										.fetch();
				vehicleDataRecordResult.stream().forEach(j->{//remove from set existing vehicle
					  vehicleDataRecordSet.remove(j.value1(0));
					  numberSet.add(j.getNumber());
				});
				//Inserting new records in table VehicleData	 
				//TODO
				// Пересмотреть добавление нового транспортного средства
				if(!vehicleDataRecordSet.isEmpty()) {		    	 
				    vehicleDataRecordSet.stream().forEach(j->{	  	
				    	if (numberSet.contains(j.getNumber())) {//update record		
				    		dsl.update(VEHICLE_DATA)
				    			.set(VEHICLE_DATA.MODEL, j.getModel())
				    			.set(VEHICLE_DATA.OWNER, j.getOwner())
				    			.set(VEHICLE_DATA.TYPE, j.getType())
				    			.set(VEHICLE_DATA.UID, j.getUid())
				    			.set(VEHICLE_DATA.DESCRIPTION, j.getDescription())
				    				.where(VEHICLE_DATA.NUMBER.equal(j.getNumber()))
				    				.execute();
				    	}else {
				    		j.changed(VEHICLE_DATA.ID, false);
				    		dsl.insertInto(VEHICLE_DATA)
				    		.set(j).execute();
				    	}	    	
				    });
				    commitFl=true;
				}
				Result<RegionRbRecord> regionRbRecordResult = dsl
																.selectFrom(REGION_RB)
																	.fetch();
				regionRbRecordResult.stream().forEach(j->{//remove from set existing regions
					regionRbRecordSet.remove(j.value1(0));
				});
				//Inserting new records in table Region	   
				if(!regionRbRecordSet.isEmpty()) {		    	 
					regionRbRecordSet.stream().forEach(j->{	 
				    	j.changed(REGION_RB.ID, false); //set default value to field ID 
				    });
				    dsl.batchInsert(regionRbRecordSet).execute();
				    commitFl=true;
				} 
				if (commitFl) conn.commit();
				
				
				 //Selecting Records from vehicleData table for finding ID as foreign key for tracking data record. 
				 vehicleDataRecordResult = dsl
							.selectFrom(VEHICLE_DATA)
								.fetch();

				 Map<Integer, Integer> vehicleDataMap = new HashMap<>();
				 vehicleDataRecordResult.stream().forEach(j->{
					 vehicleDataMap.put(Arrays.asList(j.getNumber()).hashCode(),j.getId());
				});
				//Selecting Records from regionRb table for finding ID as foreign key for tracking data record. 
				 regionRbRecordResult = dsl
							.selectFrom(REGION_RB)
								.fetch();

				 Map<Integer, Integer> regionRbMap = new HashMap<>();
				 regionRbRecordResult.stream().forEach(j->{
					 regionRbMap.put(Arrays.asList(j.getRegion()).hashCode(),j.getId());
				});
				

					//Preparing data for batch insert into TrackingData table
				fileData.stream().forEach(j->{
					j.setVehicleUid(vehicleDataMap.get(j.getVehicleUid()));
					j.setRegion(regionRbMap.get(j.getRegion()));
				});

			int rs[] = dsl.batchInsert(fileData).execute();
			conn.commit();
				
			return rs.length;
		}
	}
	

	
	/**
	 * insert new data from Json
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */	
	public static String InsertJsonInto(String jsonData) throws  SQLException, IOException, ClassNotFoundException {
		
		try{
			List<JsonInsert>jiList = new Gson().fromJson(jsonData, new TypeToken<List<JsonInsert>>(){}.getType()); 
			int rc = InsertListInto(jiList);
			System.out.println("Incoming json: " + jsonData);
			System.out.println("total records: "+ String.valueOf(jiList.size()));
			System.out.println("Was inserted records: "+String.valueOf(rc)+"  "+Instant.now().atOffset(ZoneOffset.ofHours(3)).toString());
			return getJsonMessage("Was received records: "+String.valueOf(rc));
		}catch(JsonSyntaxException e) {
			System.out.println(e.toString()+"  "+Instant.now().toString());
			return getJsonMessage("Json syntax error! 0 records was added!");
		}
	}
	
	
	
	
	
	/**Select from base track data
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 * 
	 */	
	public static List<Record8<Timestamp,Short,BigDecimal,BigDecimal,Short,Integer,String,String>> 
		selectTrackData(long min_ts, long max_ts, String vehicleNumber)	
										throws ClassNotFoundException, SQLException{
			
			try (Connection conn = getConnection()) {
		         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
		         List<Record8<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String>> result = 
		        		 				dsl.select(TRACKING_DATA.TIMESTAMP, 
		         					  		TRACKING_DATA.VELOCITY, 
		         					  		TRACKING_DATA.LATITUDE, 
		         					  		TRACKING_DATA.LONGITUDE, 
		         					  		TRACKING_DATA.DIRECTION, 
		         					  		TRACKING_DATA.ODOMETER,
		         					  		VEHICLE_DATA.NUMBER,
		         					  		VEHICLE_DATA.UID)
				         							.from(TRACKING_DATA)
				         								.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(TRACKING_DATA.VEHICLE_UID))
				         									.where(TRACKING_DATA.TIMESTAMP.between(new Timestamp(min_ts*1000), new Timestamp(max_ts*1000))
				         										.and(VEHICLE_DATA.NUMBER.eq(vehicleNumber)))
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
	public static String marshalTrackData(long min_ts, long max_ts, String vehicleNumber) throws SQLException, JAXBException, ClassNotFoundException, DatatypeConfigurationException {
		List<Record8<Timestamp,Short,BigDecimal,BigDecimal,Short,Integer,String,String>> result = selectTrackData(min_ts, max_ts, vehicleNumber);

		GpxType gpx = initGpx();
		
		if(!result.isEmpty()) {
			gpx.getTrk().get(0).setName(result.get(0).getValue(VEHICLE_DATA.NUMBER));
			gpx.getTrk().get(0).setDesc("The vehicle track");
			

		}else return jaxbTrackMarshal(gpx);
		
		//define temporary instance for saving data outside of cycle
	    int segmentIndex = 0;    
	    WptType tmpWpt = new WptType();  
	    tmpWpt.setLat(new BigDecimal(0));
	    tmpWpt.setLon(new BigDecimal(0));
	    tmpWpt.setExtensions(new ExtensionsTypeWpt());
	    tmpWpt.getExtensions().setUnixtimestamp(0L);
	    
	    //define GregorianCalendar instance for building XMLGregorianCalendar from it
	    GregorianCalendar cal = new GregorianCalendar();
	    
	    Integer odometer=null;
	    //Building of track segments     	
	   for (int i=0; i<result.size(); i++) {
	       if (!result.get(i).get(TRACKING_DATA.LATITUDE).equals( tmpWpt.getLat()) ||
	         				!result.get(i).get(TRACKING_DATA.LONGITUDE).equals(tmpWpt.getLon())) {// check for new coordinates
	         	
	    	   
	    	   //Creating wpt by result[i]
	    	   WptType wpt = wptBuild(result.get(i));
	    	   
	    	   //Set TimeStamp newXMLGregorianCalendar Format.
	    	   //For mem economy use one GregorianCalendar Instance defining early outside of cycle "for"
	           cal.setTimeInMillis(result.get(i).getValue(TRACKING_DATA.TIMESTAMP).getTime());	
	         	wpt.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
	           
	         	//creating new segment
	         	long delta = 0;
	         	if((delta = (wpt.getExtensions().getUnixtimestamp() - tmpWpt.getExtensions().getUnixtimestamp())/1000) 
	         			> Integer.valueOf(Props.get().getProperty("suspense","300"))){//suspense time constant
	         		segmentIndex++;
	         		gpx.getTrk().get(0).getTrkseg().add(new TrksegType());//new segment building
	         		gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).setExtensions(new ExtensionsTypeSeg());
	         		if (segmentIndex>1) {// from second segment begins to set waiting in sec
	         			gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).getExtensions().setWaiting(delta);
	         		}
	         		//distance of segment for Teltonika trackers
	         		if (result.get(i).getValue(TRACKING_DATA.ODOMETER)!=null) { 
	         			gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).getExtensions().setOdometer(
	         															result.get(i).getValue(TRACKING_DATA.ODOMETER));
	         			if (odometer!=null && segmentIndex>1) {
	         				gpx.getTrk().get(0).getTrkseg().get(segmentIndex-2).getExtensions().setDistance(
	         					 odometer - gpx.getTrk().get(0).getTrkseg().get(segmentIndex-2).getExtensions().getOdometer());			
	         			}
	         		}
	         	}
	         	odometer = result.get(i).getValue(TRACKING_DATA.ODOMETER);
	         	gpx.getTrk().get(0).getTrkseg().get(segmentIndex-1).getTrkpt().add(wpt);
	         	tmpWpt = wpt;
	         }
	    }
	 //last segment distance  for Teltonika trackers  
	   if (result.get(result.size()-1).getValue(TRACKING_DATA.ODOMETER)!=null) { 
		   gpx.getTrk().get(0).getTrkseg().get(gpx.getTrk().get(0).getTrkseg().size()-1).getExtensions().setDistance(
				result.get(result.size()-1).getValue(TRACKING_DATA.ODOMETER)
				- gpx.getTrk().get(0).getTrkseg().get(gpx.getTrk().get(0).getTrkseg().size()-1).getExtensions().getOdometer());
			if(result.get(0).getValue(TRACKING_DATA.ODOMETER)!=null) {
				//Calculate distance of Track for Teltonika 
				gpx.getTrk().get(0).setExtensions(new ExtensionsTypeTrk());
				gpx.getTrk().get(0).getExtensions().setDistance(
						result.get(result.size()-1).getValue(TRACKING_DATA.ODOMETER)
															- result.get(0).getValue(TRACKING_DATA.ODOMETER));
			}	
	   }
	   
	   
	   
	   return jaxbTrackMarshal(gpx);
	}
/*	
	*//**
	 * Returns JsonBase64 instance from base64
	 * 
	 * 
	 *//*
	private static Extra getAdditionalParams(String base64String) {
		try {
			byte[] valueDecoded= Base64.getDecoder().decode(base64String);
			return new Gson().fromJson(new String(valueDecoded, "UTF-8"), Extra.class);
			
		} catch ( JsonSyntaxException | UnsupportedEncodingException e) {
			return new Extra();
		}
	}
	*/
	
	
	
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
    	wpt.getExtensions().setUnixtimestamp(result.getValue(TRACKING_DATA.TIMESTAMP).getTime()/1000);
    	wpt.getExtensions().setVelocity(BigInteger.valueOf(result.getValue(TRACKING_DATA.VELOCITY)));

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
	
	
	
	
	
	/** Returns JsonTrack object
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws DatatypeConfigurationException 
	 */
	private static JsonTrack getTrackData(long min_ts, long max_ts, String vehicleNumber) throws ClassNotFoundException, SQLException {
		List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> result=null;
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
	         result = 
	        		 				dsl.select(TRACKING_DATA.TIMESTAMP, 
	         					  		TRACKING_DATA.VELOCITY, 
	         					  		TRACKING_DATA.LATITUDE, 
	         					  		TRACKING_DATA.LONGITUDE, 
	         					  		TRACKING_DATA.DIRECTION, 
	         					  		TRACKING_DATA.ODOMETER,
	         					  		VEHICLE_DATA.NUMBER,
	         					  		VEHICLE_DATA.UID,
	         					  		VEHICLE_DATA.OWNER,
	         					  		VEHICLE_DATA.TYPE,
	         					  		VEHICLE_DATA.MODEL,
	         					  		VEHICLE_DATA.DESCRIPTION)
			         							.from(TRACKING_DATA)
			         								.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(TRACKING_DATA.VEHICLE_UID))
			         									.where(TRACKING_DATA.TIMESTAMP.between(new Timestamp(min_ts*1000), new Timestamp(max_ts*1000))
			         										.and(VEHICLE_DATA.NUMBER.eq(vehicleNumber)))
	         													.orderBy(TRACKING_DATA.TIMESTAMP)
	         														.fetch();       
		}       
	        	
		  

		return dataAnalasing(result);
		
	}
	
	/**
	 * dataAnalasing
	 * Returns JsonTrack Object by 
	 * 
	 */
	private static JsonTrack dataAnalasing(List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> result) {
		JsonTrack jt = new JsonTrack();
		int filterVelocity = Integer.valueOf(Props.get().getProperty("filterVelocity", "50"));
		int waitPointDuration = Integer.valueOf(Props.get().getProperty("suspense","300"));
		boolean odometerFlag=false;
		double tmpOdometer=0.0;
		if(!result.isEmpty()) {
			jt.setSegments(new ArrayList<Segment>());
			jt.setWaitTrackPoints(new ArrayList<WaitTrackPoint>());
			jt.setVehicle(new Vehicle(result.get(0).getValue(VEHICLE_DATA.TYPE),
										result.get(0).getValue(VEHICLE_DATA.UID),
											result.get(0).getValue(VEHICLE_DATA.NUMBER),
												result.get(0).getValue(VEHICLE_DATA.OWNER),
													result.get(0).getValue(VEHICLE_DATA.MODEL),
														result.get(0).getValue(VEHICLE_DATA.DESCRIPTION)));
			//TODO
			odometerFlag = result.get(0).getValue(TRACKING_DATA.ODOMETER)!=null;
		}else return jt;
		
		//define temporary instance for saving data outside of cycle
	    int segmentIndex = 0;    
	    TrackPoint tmpPt = new TrackPoint((short)0, (short)0, new BigDecimal(0), new BigDecimal(0), 0L, (short)0);  

	    
	    Double odometer=0.0;
	    //Building of track segments     	
	   for (int i=0; i < result.size(); i++) {
	       if (!result.get(i).get(TRACKING_DATA.LATITUDE).equals( tmpPt.getLatitude()) ||
	         				!result.get(i).get(TRACKING_DATA.LONGITUDE).equals(tmpPt.getLongitude())) {// check for new coordinates

	    	   //Creating wpt by result[i]
	    	   TrackPoint pt = new TrackPoint(result.get(i).get(TRACKING_DATA.VELOCITY),
	    			   result.get(i).get(TRACKING_DATA.DIRECTION),
	    			   result.get(i).get(TRACKING_DATA.LONGITUDE),
	    			   result.get(i).get(TRACKING_DATA.LATITUDE),
	    			   result.get(i).get(TRACKING_DATA.TIMESTAMP).getTime()/1000,
	    			   (short)0);  
	    	   
	    	   long delta = pt.getTimestamp() - tmpPt.getTimestamp();

	    	   //filtering unreal points 
	         	if (i > 0) {
	         			tmpOdometer = getDistance(tmpPt, pt);		
		         		if(delta==0 || tmpOdometer/delta > filterVelocity) {
		         			continue; // skip iteration
		         	}
	         	}else {
	         		tmpOdometer = 0.0;
	         	}

	         	if (odometerFlag ) {// use vehicle odometer value
	         		pt.setOdometer(Double.valueOf(result.get(i).getValue(TRACKING_DATA.ODOMETER)));
	         	}else {//use calculated value
	         		odometer += tmpOdometer;
	         		pt.setOdometer(Double.valueOf(odometer));
	         	}
	         	
	         	//creating new segment
	         	
	         	if(delta > waitPointDuration){//suspense time constant        		
	         		segmentIndex++;     			
	         		jt.getSegments().add(new Segment());//new segment building
	         		jt.getSegments().get(segmentIndex-1).setTrackPoints(new ArrayList<TrackPoint>());//new point list in it  	
	         		
	         		if (segmentIndex>1) {// from second segment begins to set waiting in sec
	         			jt.getSegments().get(segmentIndex-1).setWaiting(delta);
	         			jt.getWaitTrackPoints().add(new WaitTrackPoint(tmpPt, Long.valueOf(delta).intValue()));
	         		}
	         	}
	         	jt.getSegments().get(segmentIndex-1).getTrackPoints().add(pt);
	         	tmpPt = pt;
	         }
	    }
	   jt.getSegments().stream().forEach(seg->{
		   Double distance = 0.0;
		   distance = seg.getTrackPoints().get(seg.getTrackPoints().size()-1).getOdometer() - seg.getTrackPoints().get(0).getOdometer();
		   seg.setDistance(Double.valueOf(distance).intValue());
		   
		   if (seg.getTrackPoints().size()>1) {
			   float time = seg.getTrackPoints().get(seg.getTrackPoints().size()-1).getTimestamp()-seg.getTrackPoints().get(0).getTimestamp();
			   seg.setAvaerage(distance.floatValue()/time*3.6F);
		   }else seg.setAvaerage(0.0F);   
	   });
   
	   jt.setDistance(Double.valueOf(jt.getSegments().get(jt.getSegments().size()-1).getTrackPoints().get(jt.getSegments().get(jt.getSegments().size()-1).getTrackPoints().size()-1).getOdometer()-
			   			jt.getSegments().get(0).getTrackPoints().get(0).getOdometer()).intValue());
	  
	  return jt;  		
	}
	
	/**
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws DatatypeConfigurationException 
	 */
	public static String jsonTrackData(long min_ts, long max_ts, String vehicleNumber) 
							throws ClassNotFoundException, SQLException, DatatypeConfigurationException {
		return new GsonBuilder()
			  .excludeFieldsWithoutExposeAnnotation()
			  .create().toJson(getTrackData(min_ts, max_ts, vehicleNumber));  
	}
	
	/**
	 * GetDistance between two points
	 */
	private static Double getDistance(TrackPoint fromPt, TrackPoint toPt) {
		return GeometryEngine.geodesicDistanceOnWGS84(new Point(fromPt.getLatitude().doubleValue(),fromPt.getLongitude().doubleValue()), 
				new Point(toPt.getLatitude().doubleValue(),toPt.getLongitude().doubleValue()));
	}
	
	
	/**
	 * Get 24 Hour Data of all vehicles 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	private static List<Record12<Timestamp,Short,BigDecimal,BigDecimal,Short,Integer,String,String,String,String,String,String>> getResultByDay(long tsFrom, long tsTo, String type) throws ClassNotFoundException, SQLException {
			
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    	         
	         List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>
	         result = 
		 				dsl.select(TRACKING_DATA.TIMESTAMP, 
		 							TRACKING_DATA.VELOCITY, 
		 							TRACKING_DATA.LATITUDE, 
		 							TRACKING_DATA.LONGITUDE, 
		 							TRACKING_DATA.DIRECTION, 
		 							TRACKING_DATA.ODOMETER,
		 							VEHICLE_DATA.NUMBER,
		 							VEHICLE_DATA.UID,
		 							VEHICLE_DATA.OWNER,
		 							VEHICLE_DATA.TYPE,
		 							VEHICLE_DATA.MODEL,
		 							VEHICLE_DATA.DESCRIPTION)
      							.from(TRACKING_DATA)
      								.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(TRACKING_DATA.VEHICLE_UID))
      								.where(TRACKING_DATA.TIMESTAMP
		        		 					.between(new Timestamp(tsFrom),
		        		 								new Timestamp(tsTo))
      										.and(VEHICLE_DATA.TYPE.eq(type)))
      											.orderBy(TRACKING_DATA.TIMESTAMP)
		        		 							.fetch();
	         return result; 
		}	
	}

	
	/**
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<ReportForVehicle> getVehicleStatFor(Stream<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> stream) throws ClassNotFoundException, SQLException {
		Map<String, List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>> resultByVehicle 
		= stream.collect(Collectors.groupingBy(Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>::value8));
		List<ReportForVehicle>rfvList = new ArrayList<>();
		Set<String>vehicleUidSet= resultByVehicle.keySet();
		//List<JsonTrack>jtList = new ArrayList<>();
		for(Object x:vehicleUidSet) {
			List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> recordVehicleList = resultByVehicle.get(x);
			JsonTrack jt =
					dataAnalasing(recordVehicleList.stream()
									.sorted(new Comparator<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>(){
											@Override
											public int compare(
													Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> a,
													Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> b) {
												// TODO Auto-generated method stub
												return a.value7().compareTo(b.value7());
											}
			}).collect(Collectors.toList()));
			rfvList.add(new 
							ReportForVehicle(jt.getVehicle(), jt.getDistance(), jt.getWaitTrackPoints()
						)
					);
		}
		return rfvList;
	}	
	
	
	
	/**
	 * Get statistic for last 24 hour for all vehicles
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<List<ReportForVehicle>> getStatistic24Hour(long tsFrom, long tsTo, String type) throws ClassNotFoundException, SQLException{
		List<List<ReportForVehicle>> Statistic24HourList = new ArrayList<>();
		List<Record12<Timestamp,Short,BigDecimal,BigDecimal,Short,Integer,String,String,String,String,String,String>> dayData =
				getResultByDay(tsFrom, tsTo, type).stream()
								.sorted(new Comparator<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>(){
									@Override
									public int compare(Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> a,
														Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> b) {		
										return a.value1().compareTo(b.value1());
									}
								}).collect(Collectors.toList());
		if (dayData.isEmpty()) return Statistic24HourList;
		for(int i=0;i<24;i++){
			Timestamp minTs = new Timestamp(tsFrom+i*3600000);
			Timestamp maxTs = new Timestamp(minTs.getTime()+3600000);
			Statistic24HourList.add(getVehicleStatFor(dayData.stream().filter(rec->rec.value1().after(minTs)&&rec.value1().before(maxTs))));	
		}
		return Statistic24HourList;
	}
	/**
	 * Get statistic for last 24 hour for all vehicles in JSON
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static String getStatistic24HourJson(long tsFrom, String type) throws ClassNotFoundException, SQLException {
		ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(tsFrom*1000), ZoneId.systemDefault());
		moment = moment.minusHours(moment.getHour()).minusMinutes(moment.getMinute()).minusSeconds(moment.getSecond());
		
		
		return new GsonBuilder()
				.excludeFieldsWithoutExposeAnnotation()
				.create().toJson(getStatistic24Hour(moment.toEpochSecond()*1000, moment.plusDays(1).toEpochSecond()*1000, type)); 
	}
}
