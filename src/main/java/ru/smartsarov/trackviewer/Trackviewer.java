package ru.smartsarov.trackviewer;
import static ru.smartsarov.trackviewer.postgres.tables.TrackingData.*;
import static ru.smartsarov.trackviewer.postgres.tables.VehicleData.*;
import static ru.smartsarov.trackviewer.postgres.tables.RegionRb.*;
import static ru.smartsarov.trackviewer.postgres.tables.HourReport.*;
import static ru.smartsarov.trackviewer.postgres.tables.WaitPoints.*;
import ru.smartsarov.trackviewer.postgres.tables.records.TrackingDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.VehicleDataRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.WaitPointsRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.HourReportRecord;
import ru.smartsarov.trackviewer.postgres.tables.records.RegionRbRecord;

import org.jooq.DSLContext;
import org.jooq.JSONFormat;
import org.jooq.Record8;
import org.jooq.Record9;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.JSONFormat.RecordFormat;

import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.impl.DSL;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;





import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;



import ru.smartsarov.trackviewer.JsonTrack.JsonTrack;
import ru.smartsarov.trackviewer.JsonTrack.ReportForVehicle;
import ru.smartsarov.trackviewer.JsonTrack.Segment;
import ru.smartsarov.trackviewer.JsonTrack.TrackPoint;
import ru.smartsarov.trackviewer.JsonTrack.WaitTrackPoint;
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
	 * Returns JsonTrack Object for the vehicle result 
	 * 
	 */
	private static JsonTrack dataAnalasing(List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> result) {
		JsonTrack jt = new JsonTrack();
		int filterVelocity = Integer.valueOf(Props.get().getProperty("filterVelocity", "50"));
		int waitPointDuration = Integer.valueOf(Props.get().getProperty("suspense","300"));
		boolean odometerFlag = false;
		double tmpOdometer = 0.0;
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
	 * returns JSON formated track for the vehicle between two timestamp values
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws DatatypeConfigurationException 
	 */
	public static String jsonTrackData(long min_ts, long max_ts, String vehicleNumber) 
							throws ClassNotFoundException, SQLException {
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
		for(String x:vehicleUidSet) {
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
	
	
	/**
	 * This method is called for creating report hourly
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void createHourlyReport(long ts) throws ClassNotFoundException, SQLException {		
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	         //get mapped tracking data for vehicle number as key
	         Map<String, List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>>
	         result = dsl.select(TRACKING_DATA.TIMESTAMP, 
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
				        		 					.between(new Timestamp(ts-3600000), new Timestamp(ts)))
				        		 						.fetch()
				        		 						.stream()
				        		 						.collect(Collectors.groupingBy(Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>::value7));       
	         //get last waitpoints_id value in waiting_table
	          List<Record1<Integer>> lastWaitpoints_id_result =  dsl
	        		 						.select(DSL.max(HOUR_REPORT.WAITPOINTS_ID))
	        		 						.from(HOUR_REPORT)
	        		 						.fetch();
	          Integer lastWaitpoints_id = null;
	          if (!lastWaitpoints_id_result.isEmpty() && lastWaitpoints_id_result.get(0)!=null) {
	        	  lastWaitpoints_id = lastWaitpoints_id_result.get(0).value1();
	        	  if (lastWaitpoints_id==null){
	        		  lastWaitpoints_id=0;
	        	  }
	          }
	          
	          
	         //get vehicleNumber set
	         Set<String>vehicleNumberSet= result.keySet();
	        
	         //ArrayList of HourReport Record
	         List<HourReportRecord> hourReportList = new ArrayList<>();
	         //ArrayList of WaitPoints Record
	         List<WaitPointsRecord> waitPointsList = new ArrayList<>();
	       //TODO  Рассмотреть механизм поиска ID по номеру без обращения к базе. Т.е. на этапе формирования JT 
	         //Selecting Records from vehicleData table for finding ID as foreign key for tracking data record. 
	         List<VehicleDataRecord>vehicleDataRecordResult = dsl
						.selectFrom(VEHICLE_DATA)
							.fetch();
			 Map<String, Integer> vehicleDataMap = new HashMap<>();
			 vehicleDataRecordResult.stream().forEach(j->{
				 vehicleDataMap.put(j.getNumber(),j.getId());
			});
			 
	         //for each number create JsonTrack object
	         for(String num:vehicleNumberSet) {
	        	 //get List with data for the vehicle number
	        	 JsonTrack jt =
	 					dataAnalasing(result.get(num).stream()
	 									.sorted(new Comparator<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>>(){
	 											@Override
	 											public int compare(//compare two records by timestamps
	 													Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> a,
	 													Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> b) {
	 												return a.value7().compareTo(b.value7());
	 											}
	 			}).collect(Collectors.toList()));       
	        	 //begin to add batch for waitpoints table
	        	 //parse jt
	        	 
	        	 if(!jt.getWaitTrackPoints().isEmpty()) {//if not empty  
	        		 lastWaitpoints_id++;
	        		 for(WaitTrackPoint wp: jt.getWaitTrackPoints()){
	        			 WaitPointsRecord tmpRec=new WaitPointsRecord(0L,//Long id
							        new Timestamp(wp.getTrackPoint().getTimestamp()*1000),//Timestamp ts,
							        wp.getTrackPoint().getLongitude(),//BigDecimal lng
							        wp.getTrackPoint().getLatitude(),//BigDecimal lat,
							        wp.getWaiting(),//Integer waiting,
							        lastWaitpoints_id//lastWaitpoints_id//Integer waitpointsId
							        );
	        			 tmpRec.changed(WAIT_POINTS.ID, false);
	        			 waitPointsList.add(tmpRec);
	        		 }
	        	 }    	 
 
	        	int totalDriving = jt.getSegments().isEmpty()?0:jt.getSegments().stream()
	        			.map(seg->seg.getTrackPoints().get(seg.getTrackPoints().size()-1).getTimestamp()-seg.getTrackPoints().get(0).getTimestamp())
	        			.reduce((x,y)->x+y).get().intValue();
	        	int totalWaiting = jt.getWaitTrackPoints().isEmpty()?0:jt.getWaitTrackPoints()
	        			.stream()
	        			.map(wp->wp.getWaiting())
	        			.reduce((x,y)->x+y).get();
	        	//begin to add batch for hour Report table
	        	 //parse jt
	        	 HourReportRecord tmpHrr = new HourReportRecord(0L,//Long id, 
											        			 new Timestamp(ts),//Timestamp ts, 
											        			 vehicleDataMap.get(jt.getVehicle().getNumber()),//Integer vehicleId, 
											        			 jt.getDistance(),//Integer distance, 
											        			 totalWaiting,// Integer waiting, 
											        			 totalDriving,// Integer driving, 
											        			 jt.getWaitTrackPoints().isEmpty()?0:lastWaitpoints_id,//Integer waitpointsId, 
											        			 0.0F);//Float fuel);
	        	 tmpHrr.changed(HOUR_REPORT.ID, false);
	        	 hourReportList.add(tmpHrr);
	         }
	         //batch created records...
	         dsl.batchInsert(waitPointsList).execute();
	         dsl.batchInsert(hourReportList).execute();
	         conn.commit();
		}	
	}

	
	
	/**
	 * Returns 7days report for typed vehicles
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public static String getWeekReportByType(long ts, String type) throws ClassNotFoundException, SQLException {
		//for(long i=0;i<24;i++) {
		//	createHourlyReport(1544043600000L+3600000*i);
		//}
		ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts*1000), ZoneId.systemDefault());
		moment = moment.minusDays(6).minusHours(moment.getHour()).minusMinutes(moment.getMinute()).minusSeconds(moment.getSecond());
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	        Map<Timestamp, List<Record9<Timestamp, Integer, Integer, Integer, Integer, Float, BigDecimal, BigDecimal, Timestamp>>> resultMap =		
	        		dsl.select(HOUR_REPORT.TS, 
		        		 	VEHICLE_DATA.ID,
		        		    HOUR_REPORT.DISTANCE,
		        		    HOUR_REPORT.DRIVING,
		        		    HOUR_REPORT.WAITING,
		        		    HOUR_REPORT.FUEL,
		        		    WAIT_POINTS.LNG,
		        		    WAIT_POINTS.LAT,
		        		    WAIT_POINTS.TS)
		         				.from(HOUR_REPORT)
		         					.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(HOUR_REPORT.VEHICLE_ID))
		         						.leftJoin(WAIT_POINTS).on(WAIT_POINTS.WAITPOINTS_ID.eq(HOUR_REPORT.WAITPOINTS_ID))
		         							.where(HOUR_REPORT.TS
		         								.between(new Timestamp(moment.toEpochSecond()*1000), new Timestamp(moment.plusDays(7).toEpochSecond()*1000)))
		         									.fetch()
		         									.stream()
		         									.collect(Collectors.groupingBy(Record9<Timestamp, Integer, Integer, Integer, Integer, Float, BigDecimal, BigDecimal, Timestamp>::value1));
	      
	        List<Timestamp> resultSet = resultMap.keySet().stream()
											    		.sorted()
											    		.collect(Collectors.toList());
	       
	        
	        return null;
		}	
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
