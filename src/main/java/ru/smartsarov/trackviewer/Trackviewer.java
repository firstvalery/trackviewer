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
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.JSONFormat.RecordFormat;

import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Record12;
import org.jooq.Record15;
import org.jooq.impl.DSL;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.DocumentException;
import com.sendpulse.restapi.Sendpulse;

import java.io.IOException;
import java.io.OutputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	
	/**Самодигностика!
	 * Отсылает сообщение по e-mail
	 */	
	public static String sendEmail(String comment) {
		Sendpulse sendpulse = new Sendpulse(Props.get().getProperty("client_id",""), Props.get().getProperty("client_secret",""));
		  Map<String, Object> from = new HashMap<String, Object>();
		  //Список от кого
		  from.put("name", "Track Application");
		  from.put("email", "service@100gorodov.ru");
		 //список адресатов
		  ArrayList<Map<String, Object>> to = new ArrayList<Map<String, Object>>();
		  //Данные адресата
		  Map<String, Object> elementto = new HashMap<String, Object>();
		  elementto.put("name", "mr. Kalachev");
		  elementto.put("email", "testkalach@gmail.com");
		  to.add(elementto);
		  //Данные самого письма
		  Map<String, Object> emaildata = new HashMap<String, Object>();
		  emaildata.put("html", comment);
		  emaildata.put("text", comment);
		  emaildata.put("subject","Проблема с работой сервиса архивного трэкинга");
		  emaildata.put("from",from);
		  emaildata.put("to",to);
		  //отправляем
		  Map<String, Object> result = sendpulse.smtpSendMail(emaildata);
		  return ("Result: " + result);	
	}
	
	
	/**
	 *Получим из таблицы TrackingData последнюю запись. Если данные не приходят в течение 15 минут, то вызывает
	 *метод для отправки сообщения по почте 
	 */
	public static void checkForBreath() {
		try (Connection conn = getConnection()) {
	        DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	        Record1<Timestamp> res = dsl.select(DSL.max(TRACKING_DATA.TIMESTAMP))
	         	.from(TRACKING_DATA)
	         	.fetchOne();
	        
	        //получим время последней метки времени
	        if(res!=null) {
	        	Timestamp lastTimestamp = res.get("max", Timestamp.class);
	        	//если Timestamp null
	        	if(lastTimestamp ==null) {
	        		sendEmail("Ошибка выборки В БД!"); 
	        		return;
	        	}
	        	//если за 15 минут ничего не приходило, то отошлем сообщение по почте
	        	if(lastTimestamp.getTime()/1000 + 900 < Instant.now().getEpochSecond()) {
	        		sendEmail(String.format("Сервер не получает данные с: %s!", lastTimestamp)); 
	        	}
	        	return;
	        }//res ==null
	        sendEmail("Ошибка выборки В БД!"); 
		}	
		catch(ClassNotFoundException | SQLException e) {
			sendEmail(e.toString()); 
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

		boolean commitFl = false;
		
		//build set of vehicle
		Set<VehicleDataRecord> vehicleDataRecordSet = incomingDataList.stream()
												  .filter(j->j.getVehicle()!=null && j.getVehicle().getNumber()!=null && j.getVehicle().getUid()!=null)
												  .map(j->{
													  return new VehicleDataRecord(0, 
															j.getVehicle().getUid(),
															j.getVehicle().getNumber().toLowerCase().replaceAll("\\s", ""),			
															j.getVehicle().getType(),
															j.getVehicle().getOwner(),
															j.getVehicle().getModel(),
															j.getVehicle().getDescription());
												    })
												  .collect(Collectors.toSet());
		
		//build set of region
		Set<RegionRbRecord> regionRbRecordSet = incomingDataList.stream()
													  .filter(j->j.getVehicle()!=null && j.getVehicle().getNumber()!=null && j.getVehicle().getUid()!=null)
													  .map(j->{
														  return new RegionRbRecord(0,j.getRegion());
													    })
													  .collect(Collectors.toSet());
											
		//Creating collection for batch Insert with  Hash codes of region and vehicle
		List<TrackingDataRecord>fileData = incomingDataList.stream()
														.filter(j->j.getVehicle()!=null &&
														j.getVehicle().getNumber()!=null &&
														j.getVehicle().getUid()!=null &&
														j.getTime()!=null &&
														j.getLatitude()!=null &&
														j.getLatitude()!= null)
														.map(j->{
															TrackingDataRecord tdr = new TrackingDataRecord( 
																	new Timestamp(1000*j.getTime()),
																	j.getLongitude(),
																	j.getLatitude(),
																	j.getVelocity(),
																	j.getDirection(),
																	Arrays.asList(j.getVehicle().getNumber()
																			.toLowerCase().replaceAll("\\s", "")).hashCode(),
																	null,
																	Arrays.asList(j.getRegion()).hashCode(),
																	j.getExtra()!=null ? j.getExtra().get16():null);
																tdr.changed(TRACKING_DATA.ID, false);
																return tdr;
														})
														.collect(Collectors.toList());
	
		//working with DB
			try (Connection conn = getConnection()){
				DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    

				//очистим набор для записи данных машин от имеющихся в базе
				Result<VehicleDataRecord> vehicleDataRecordResult = dsl.selectFrom(VEHICLE_DATA).fetch();
				vehicleDataRecordSet.removeAll(vehicleDataRecordResult.stream()
																	  .map(j->j.value1(0))
																	  .collect(Collectors.toList()));
				//определим набор номеров ТС, которых нет еще в базе
				Set<String> numberSet= vehicleDataRecordResult.stream()
												.map(j->j.getNumber())
												.collect(Collectors.toSet());
				//Добавление в таблицу VEHICLE новых данных	 
				if(!vehicleDataRecordSet.isEmpty()) {		    	 
				    vehicleDataRecordSet.stream().forEach(j->{	  	
				    	if (numberSet.contains(j.getNumber())) {//ТС, для номера которого нет сведений в БД 	
				    		dsl.update(VEHICLE_DATA)
				    			.set(VEHICLE_DATA.MODEL, j.getModel())
				    			.set(VEHICLE_DATA.OWNER, j.getOwner())
				    			.set(VEHICLE_DATA.TYPE, j.getType())
				    			.set(VEHICLE_DATA.UID, j.getUid())
				    			.set(VEHICLE_DATA.DESCRIPTION, j.getDescription())
				    				.where(VEHICLE_DATA.NUMBER.equal(j.getNumber()))
				    				.execute();
				    	}else {//ТС, для номера которого есть сведения в БД, обновим данные 
				    		j.changed(VEHICLE_DATA.ID, false);
				    		dsl.insertInto(VEHICLE_DATA)
				    		.set(j).execute();
				    	}	    	
				    });
				    commitFl=true;
				}
				//Удалим из набора для записи регионов те регионы, для которых есть запись в базе
				regionRbRecordSet.removeAll(dsl.selectFrom(REGION_RB).fetch()
																	.stream()
																	.map(j->j.value1(0))
																	.collect(Collectors.toList()));								
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
				Map<Integer, Integer> vehicleDataMap = dsl
								.selectFrom(VEHICLE_DATA)
								.fetch()
								.stream()
								.collect(Collectors.toMap(vdr ->Arrays.asList(vdr.getNumber()).hashCode(), VehicleDataRecord::getId));
								
				 
				//Selecting Records from regionRb table for finding ID as foreign key for tracking data record. 
				 Map<Integer, Integer> regionRbMap = dsl
							.selectFrom(REGION_RB)
							.fetch()
							.stream()
							.collect(Collectors.toMap(rg->Arrays.asList(rg.getRegion()).hashCode(), RegionRbRecord::getId));
				 
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
		try/*(FileWriter writer = new FileWriter("C:/conf/trackviewer/log.txt", true))*/
			{
			/*writer.write(jsonData);
			writer.append('\n');
			writer.flush();*/
			
			List<JsonInsert>jiList = new Gson().fromJson(jsonData, new TypeToken<List<JsonInsert>>(){}.getType()); 
			int rc = InsertListInto(jiList);
			return getJsonMessage("Was received records: "+String.valueOf(rc));
		}catch(JsonSyntaxException e) {
			System.out.println(e.toString()+"  "+Instant.now().toString());
			return getJsonMessage("Json syntax error! 0 records was added!");
		}/*catch(IOException ex){
            System.out.println(ex.getMessage());
            return getJsonMessage("Internal error! 0 records was added!");
        } */
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
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);    
	         return dataAnalasing(
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
	         									.fetch());       
		}       
	}
	
	/**
	 * dataAnalasing
	 * Returns JsonTrack Object for the vehicle result 
	 * 
	 */
	private static JsonTrack dataAnalasing(List<Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String>> result) {
		JsonTrack jt = new JsonTrack();

		jt.setSegments(new ArrayList<Segment>());
		jt.setWaitTrackPoints(new ArrayList<WaitTrackPoint>());
		int filterVelocity = Integer.valueOf(Props.get().getProperty("filterVelocity", "50"));
		int waitPointDuration = Integer.valueOf(Props.get().getProperty("suspense","300"));
		boolean odometerFlag = false;
		double tmpOdometer = 0.0;

		if(!result.isEmpty()) {
			jt.setVehicle(new Vehicle(result.get(0).getValue(VEHICLE_DATA.TYPE),
										result.get(0).getValue(VEHICLE_DATA.UID),
											result.get(0).getValue(VEHICLE_DATA.NUMBER),
												result.get(0).getValue(VEHICLE_DATA.OWNER),
													result.get(0).getValue(VEHICLE_DATA.MODEL),
														result.get(0).getValue(VEHICLE_DATA.DESCRIPTION)));
			jt.setTsFrom(result.get(0).getValue(TRACKING_DATA.TIMESTAMP).getTime()/1000);
			jt.setTsTo(result.get(result.size()-1).getValue(TRACKING_DATA.TIMESTAMP).getTime()/1000);
			//TODO пересмотреть принцип формирования флага одометра
			odometerFlag = result.get(0).getValue(TRACKING_DATA.ODOMETER)!=null;
		}else { 
			jt.setVehicle(new Vehicle());
			return jt;
		}
		
		//define temporary instances and variables for saving data outside of cycle 
	    TrackPoint tmpPt = new TrackPoint((short)0, (short)0, new BigDecimal(0), new BigDecimal(0), 0L, (short)0);  
	    int segmentIndex = 0;  
	    Double odometer = 0.0;
	    
	    // Поиск первой точки с координатами, соответствующими критерию достоверности.
	    // Критерий достоверности: точка принадлежит множеству, состоящему не менее, чем из 2 последовательных точек, 
	    // удовлетворяющим условию "физичности" скорости перемещения из одной в другую
	    
	    int count = 0;
	    if(result.size()>1) {
		    for (int i = 1; i < result.size(); i++) {
		    	tmpPt = new TrackPoint(result.get(i-1).get(TRACKING_DATA.VELOCITY),
			    		result.get(i-1).get(TRACKING_DATA.DIRECTION),
			    		result.get(i-1).get(TRACKING_DATA.LONGITUDE),
			    		result.get(i-1).get(TRACKING_DATA.LATITUDE),
			    		result.get(i-1).get(TRACKING_DATA.TIMESTAMP).getTime()/1000,
			    		(short)0);
		    	
		    	if (!result.get(i).get(TRACKING_DATA.LATITUDE).equals( tmpPt.getLatitude()) ||
	     				!result.get(i).get(TRACKING_DATA.LONGITUDE).equals(tmpPt.getLongitude())) {
		    	 //Creating wpt by result[i]
		    	   TrackPoint pt = new TrackPoint(result.get(i).get(TRACKING_DATA.VELOCITY),
		    			   result.get(i).get(TRACKING_DATA.DIRECTION),
		    			   result.get(i).get(TRACKING_DATA.LONGITUDE),
		    			   result.get(i).get(TRACKING_DATA.LATITUDE),
		    			   result.get(i).get(TRACKING_DATA.TIMESTAMP).getTime()/1000,
		    			   (short)0);    	   
		    	   long delta = pt.getTimestamp() - tmpPt.getTimestamp();
		    	   if (delta<=0) continue;// skip iteration
		    	   
		    	   count++;
		    	   
		    	   if (getDistance(tmpPt, pt)/delta > filterVelocity) {
		    		   count = 0;
		    	   }
		    		 
		    	   if (count == 1) {
		    		   count = i - count;
		    		   break;	   
		    	   }	   
		    	   tmpPt = pt;
		    	}
		    }    
	    } else {
	    	count = 0;
	    }

	   tmpPt = new TrackPoint((short)0, (short)0, new BigDecimal(0), new BigDecimal(0), 0L, (short)0); 
	    //Building of track segments     	

	   for (int i = count; i < result.size(); i++) {
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
		         		if(delta<=0 || tmpOdometer/delta > filterVelocity) {
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
		return GeometryEngine.geodesicDistanceOnWGS84(new Point(fromPt.getLongitude().doubleValue(), fromPt.getLatitude().doubleValue()), 
				new Point(toPt.getLongitude().doubleValue(), toPt.getLatitude().doubleValue()));
	}
	

	/**
	 * Get statistic for last 24 hour for all vehicles in JSON
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static String toAnnotatedJson(Object obj){
		return new GsonBuilder()
				.excludeFieldsWithoutExposeAnnotation().create().toJson(obj); 
	}
	
	/**
	 * This method is called for creating report hourly
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void createHourlyReport(long ts) throws ClassNotFoundException, SQLException {	
		//Создаем отчет с меткой времени всегда соответсвующей виду HH:00:00
		ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts*1000), ZoneId.systemDefault());
		ts = moment.minusMinutes(moment.getMinute()).minusSeconds(moment.getSecond()).toEpochSecond()*1000;
	
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	         
	         //get last waitpoints_id value in waiting_table
	          Record1<Integer> lastWaitpoints_id_result =  dsl
	        		 						.select(DSL.max(HOUR_REPORT.WAITPOINTS_ID))
	        		 						.from(HOUR_REPORT)
	        		 						.fetchAny();
	        		 						
	          Integer lastWaitpoints_id = null; 
	          if (lastWaitpoints_id_result!=null && (lastWaitpoints_id = lastWaitpoints_id_result.value1())!=null) {
	        	 
	          } else {
	        	  lastWaitpoints_id = 0;  
	          }
	          
	         //ArrayList of HourReport Record
	         List<HourReportRecord> hourReportList = new ArrayList<>();
	         
	         //ArrayList of WaitPoints Record
	         List<WaitPointsRecord> waitPointsList = new ArrayList<>();
	       //TODO  Рассмотреть механизм поиска ID по номеру без обращения к базе. Т.е. на этапе формирования JT 
	         //Selecting Records from vehicleData table for finding ID as foreign key for tracking data record. 

			 Map<String, Integer> vehicleDataMap = dsl
													.selectFrom(VEHICLE_DATA)
													.fetch()
													.stream()
													.collect(Collectors.toMap(k->k.getNumber(), k->k.getId()));

			//get mapped tracking data for vehicle number as key
			 List<JsonTrack>jtList = dsl.select(TRACKING_DATA.TIMESTAMP, 
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
	        		 		.collect(Collectors.groupingBy(Record12<Timestamp, Short, BigDecimal, BigDecimal, 
	        		 																	Short, Integer, String, String, 
	        		 																		String, String, String, String>::value7))       

							 .entrySet()
							 .stream()
							 .map(s->{return dataAnalasing(s.getValue()
									  		.stream()
					  						.sorted(new Comparator<Record12<Timestamp, Short, BigDecimal, BigDecimal, 
					  															Short, Integer, String, String, 
					  																String, String, String, String>>(){
	 											@Override
	 											public int compare(//compare two records by timestamps
	 													Record12<Timestamp, Short, BigDecimal, BigDecimal, 
	 																Short, Integer, String, String, 
	 																	String, String, String, String> a,
	 													Record12<Timestamp, Short, BigDecimal, BigDecimal, 
	 																Short, Integer, String, String, String, 
	 																	String, String, String> b) {
	 												return a.value7().compareTo(b.value7());
	 											}
					  						}).collect(Collectors.toList()));		 
			 }).collect(Collectors.toList());
			 
		//TODO	Требуется рефакторинг  for(JsonTrack jt :jtList)  под Stream API 
	         for(JsonTrack jt :jtList) {	 
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
		 * Returns day report for typed vehicles
		 * @throws SQLException 
		 * @throws ClassNotFoundException 
		 */
		public static String getDayReportByType(long ts, String type) throws ClassNotFoundException, SQLException {

			ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts*1000), ZoneId.systemDefault());
			moment = moment.minusHours(moment.getHour()-1).minusMinutes(moment.getMinute()).minusSeconds(moment.getSecond());

			try (Connection conn = getConnection()) {
		         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
		         List<List<ReportForVehicle>>rfvReport 	
		        	= dsl.select(HOUR_REPORT.TS, 
			        		 	VEHICLE_DATA.NUMBER,
			        		 	VEHICLE_DATA.TYPE,
			        		 	VEHICLE_DATA.UID,
			        		 	VEHICLE_DATA.OWNER,
			        		 	VEHICLE_DATA.MODEL,
			        		 	VEHICLE_DATA.DESCRIPTION,
			        		    HOUR_REPORT.DISTANCE,
			        		    HOUR_REPORT.DRIVING,
			        		    HOUR_REPORT.WAITING,
			        		    HOUR_REPORT.FUEL,
			        		    WAIT_POINTS.LNG,
			        		    WAIT_POINTS.LAT,
			        		    WAIT_POINTS.TS,
			        		    WAIT_POINTS.WAITING)
			         				.from(HOUR_REPORT)
			         				.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(HOUR_REPORT.VEHICLE_ID))
			         				.leftJoin(WAIT_POINTS).on(WAIT_POINTS.WAITPOINTS_ID.eq(HOUR_REPORT.WAITPOINTS_ID))
			         				.where(HOUR_REPORT.TS
			         				.between(new Timestamp(moment.toEpochSecond()*1000), new Timestamp(moment.plusHours(23).toEpochSecond()*1000))
			         				.and(VEHICLE_DATA.TYPE.eq(type)))
			         				.fetch()
			         				.stream()

			         				.collect(Collectors.groupingBy(Record15<Timestamp, String, String, String, 
			         																String, String, String, Integer, 
			         																	Integer, Integer, Float, BigDecimal, 
			         																		BigDecimal, Timestamp, Integer>::value1))
		        						.entrySet()
		        						.stream()
		        						.map(t->{				
				         					return t.getValue()
				         					.stream()
											.collect(Collectors.groupingBy(Record15<Timestamp, String, String, String,//маппируем по номеру ТС
																						String, String, String, Integer, 
																							Integer, Integer, Float, BigDecimal, 
																								BigDecimal, Timestamp, Integer>::value2))
											.entrySet()
											.stream()//по каждому элементу Entry
											.map(p->{ 
														ReportForVehicle rfv = new ReportForVehicle(null,0,null);												 
														List<WaitTrackPoint> wptList = p.getValue()//создадим список остановок
														.stream()
														.map(s->{   
																	if(s.getValue(WAIT_POINTS.WAITING)==null) 
																			return null;
																	WaitTrackPoint wtp = new WaitTrackPoint(
																			new TrackPoint((short)0,
																							(short)0,
																							s.getValue(WAIT_POINTS.LNG),
																							s.getValue(WAIT_POINTS.LAT),
																							s.getValue(WAIT_POINTS.TS).getTime()/1000,
																							(short)0),
																										s.getValue(WAIT_POINTS.WAITING));
																					return wtp;		
																})
														 .sorted(new Comparator<WaitTrackPoint>() {

															@Override
															public int compare(WaitTrackPoint a, WaitTrackPoint b) {
																if(a==null&b!=null)return -1;
																else if(a!=null&b==null)return 1;
																else if(a==null && b==null) return 0;
																if (a.getTrackPoint().getTimestamp()>b.getTrackPoint().getTimestamp())return 1;
																if (a.getTrackPoint().getTimestamp()<b.getTrackPoint().getTimestamp())return -1;
																return 0;	
															}})
													.collect(Collectors.toList());
														rfv.setWaitTrackPoints(wptList.contains(null)?null:wptList);
														
														Record15<Timestamp, String, String, String, 
															String, String, String, Integer, 
																Integer, Integer, Float, BigDecimal, 
																	BigDecimal, Timestamp, Integer> tmpRec = p.getValue().get(0);
														
														rfv.setDistance(tmpRec.getValue(HOUR_REPORT.DISTANCE));
														rfv.setTsTo(tmpRec.getValue(HOUR_REPORT.TS).getTime()/1000);
														rfv.setTsFrom(rfv.getTsTo()-3600);
														rfv.setVehicle(new Vehicle(tmpRec.getValue(VEHICLE_DATA.TYPE),
																					tmpRec.getValue(VEHICLE_DATA.UID),
																					tmpRec.getValue(VEHICLE_DATA.NUMBER),
																					tmpRec.getValue(VEHICLE_DATA.OWNER),
																					tmpRec.getValue(VEHICLE_DATA.MODEL),
																					tmpRec.getValue(VEHICLE_DATA.DESCRIPTION)
																				));
														rfv.setTotalDriving(tmpRec.getValue(HOUR_REPORT.DRIVING));
														rfv.setTotalWaiting(tmpRec.getValue(HOUR_REPORT.WAITING));
														return  rfv;
													})
											.collect(Collectors.toList());								
				         				})
				         				.sorted(new Comparator<List<ReportForVehicle>>(){
											@Override
											public int compare(List<ReportForVehicle> a, List<ReportForVehicle> b) {
													if (a.get(0).getTsFrom() < b.get(0).getTsFrom()) return -1;				
													if (a.get(0).getTsFrom() > b.get(0).getTsFrom()) return 1;
													return 0;
											}
				         				}).collect(Collectors.toList());
			        
		        //определим и заполним недостающие часовые отчеты пустыми 
		        //объектами для соблюдения ранее принятого соглашения по  JSON 
		         
		         //Set меток времени в получившихся суточном отчете
		         Set<Long> rfvReportTiestampSet = rfvReport.stream().map(p->p.get(0).getTsTo()).collect(Collectors.toSet());
		         
		         //пытаемся добавить в набор требуемые метки отчета. 
		         //там где это получается нужно добавить пропуск в основной отчет
		         for(int i = 0; i < 24; i++) {
		        	 if (rfvReportTiestampSet.add(moment.toEpochSecond()+3600*i)) {
		        		 rfvReport.add(i, new ArrayList<>());
		        	 }
		         }
		        return toAnnotatedJson(rfvReport);
			}		
	}	
		
		
	/**
	 * Returns week report for typed vehicles
	 * 
	 */	
		public static String getWeekReportByType(long ts, String type) throws ClassNotFoundException, SQLException {
			ZoneId zoneId = ZoneId.systemDefault();
			ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts*1000), zoneId);
			moment = moment.minusDays(6).minusHours(moment.getHour()-1).minusMinutes(moment.getMinute()).minusSeconds(moment.getSecond());
			
			try (Connection conn = getConnection()) {
		         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
		          List<List<ReportForVehicle>> rfvReport 	
		          = dsl.select(HOUR_REPORT.TS, 
			        		 	VEHICLE_DATA.NUMBER,
			        		 	VEHICLE_DATA.TYPE,
			        		 	VEHICLE_DATA.UID,
			        		 	VEHICLE_DATA.OWNER,
			        		 	VEHICLE_DATA.MODEL,
			        		 	VEHICLE_DATA.DESCRIPTION,
			        		    HOUR_REPORT.DISTANCE,
			        		    HOUR_REPORT.DRIVING,
			        		    HOUR_REPORT.WAITING,
			        		    HOUR_REPORT.FUEL)
			         				.from(HOUR_REPORT)
			         				.join(VEHICLE_DATA).on(VEHICLE_DATA.ID.eq(HOUR_REPORT.VEHICLE_ID))
			         				.where(HOUR_REPORT.TS
			         				.between(new Timestamp(moment.toEpochSecond()*1000), new Timestamp(moment.plusDays(6).plusHours(23).toEpochSecond()*1000))
			         				.and(VEHICLE_DATA.TYPE.eq(type)))
			         				.fetch().stream()
						        	.collect(Collectors.toSet())
						        	.stream()
						        	.map(t->{
									        	 ZonedDateTime tmpTime = ZonedDateTime.ofInstant(t.getValue(HOUR_REPORT.TS).toInstant(), zoneId);
									        	 if(tmpTime.getHour()==0) {//если отчет полуночный, то он относится к предыдущему дню
									        		 tmpTime = tmpTime.minusDays(1L);
									        	 }
									        	 tmpTime = tmpTime.minusHours(tmpTime.getHour());
									        	 t.setValue(HOUR_REPORT.TS, new Timestamp(tmpTime.toEpochSecond()*1000));
									        	 return t; 
									     })//группируем по дням
									.collect(Collectors.groupingBy(Record11<Timestamp, String, String, String, 
									        		 									String, String, String, Integer, 
									        		 										Integer, Integer, Float>::value1))
									.entrySet()
						        	.stream()
						        	.map(p->{
						        			return p.getValue()
						        					.stream()//группируем по номерам ТС
						        					.collect(Collectors.groupingBy(Record11<Timestamp, String, String, String, 
									        		 									String, String, String, Integer, 
									        		 										Integer, Integer, Float>::value2))
						        					.entrySet()
						        					.stream()
						        					.map(s->{//для каждой машины определяем пробег и т.д. исходя из коллекции List<Record11> почасовых отчеттов
						        						ReportForVehicle rfv = new ReportForVehicle(null, 0, null);
						        						
						        						rfv.setDistance(s.getValue()//считаем пробег за день
						        										.stream()
						        										.map(q->q.getValue(HOUR_REPORT.DISTANCE))
						        										.reduce((x,y)->x + y).get());
						        						rfv.setTotalDriving(s.getValue()//считаем время в пути
								        								.stream()
								        								.map(q->q.getValue(HOUR_REPORT.DRIVING))
								        								.reduce((x,y)->x + y).get());
						        						rfv.setTotalWaiting(s.getValue()//считаем время простоя
								        								.stream()
								        								.map(q->q.getValue(HOUR_REPORT.WAITING))
								        								.reduce((x,y)->x + y).get());
						        						 Record11<Timestamp, String, String, String, 
						        						 				String, String, String, Integer, 
						        						 					Integer, Integer, Float> tmpRec = s.getValue().get(0);
														
														rfv.setTsFrom(tmpRec.getValue(HOUR_REPORT.TS).getTime()/1000);
														rfv.setTsTo(rfv.getTsFrom()+24*3600);
														rfv.setVehicle(new Vehicle(tmpRec.getValue(VEHICLE_DATA.TYPE),
																					tmpRec.getValue(VEHICLE_DATA.UID),
																					tmpRec.getValue(VEHICLE_DATA.NUMBER),
																					tmpRec.getValue(VEHICLE_DATA.OWNER),
																					tmpRec.getValue(VEHICLE_DATA.MODEL),
																					tmpRec.getValue(VEHICLE_DATA.DESCRIPTION)
																));						        						
						        						return rfv;
						        					}).collect(Collectors.toList());
						        		})
						        	.sorted(new Comparator<List<ReportForVehicle>>(){
										@Override
										public int compare(List<ReportForVehicle> a, List<ReportForVehicle> b) {
												if (a.get(0).getTsFrom() < b.get(0).getTsFrom()) return -1;				
												if (a.get(0).getTsFrom() > b.get(0).getTsFrom()) return 1;
												return 0;
											}
						        		})
						        	.collect(Collectors.toList());
						        	
				//определим и заполним недостающие часовые отчеты пустыми 
				//объектами для соблюдения ранее принятого соглашения по  JSON 
					         
				//Set меток времени в получившихся недельном отчете
				Set<Long> rfvReportTimestampSet = rfvReport.stream().map(p->p.get(0).getTsFrom()).collect(Collectors.toSet());			         
				//пытаемся добавить в набор требуемые метки отчета. 
				//там где это получается нужно добавить пропуск в основной отчет
				for(int i = 0; i < 7; i++) {
					if (rfvReportTimestampSet.add(moment.minusHours(moment.getHour()).toEpochSecond() + 3600*24*i)) {
						rfvReport.add(i, new ArrayList<>());
					}
				}	
				return toAnnotatedJson(rfvReport);
			}
		}
	/**
	 * Generates reports in DB hourly for each vehicle
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */

	public static void createHourlyReportForDay(long ts) throws ClassNotFoundException, SQLException {
		ZonedDateTime moment = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts*1000), ZoneId.systemDefault());
		ts = moment.minusHours(moment.getHour()-1)
					.minusMinutes(moment.getMinute())
					.minusSeconds(moment.getSecond())
					.toEpochSecond();
		for (int i = 0; i < 24; i++) {
			createHourlyReport(ts + i*3600);
		}
	}	
	
	/**
	 * @throws DocumentException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * This method generates a report for several vehicles of the same type.
	 */
	public static void createCommonPdfReport(long ts_min, long ts_max, String type, OutputStream output)
												throws DocumentException, ClassNotFoundException, SQLException  {
		
		PdfGenerator.generateCommonPdfReport(getReportVehicleList(ts_min, ts_max, type), output, ts_min, ts_max);
	}
	
	/**
	 * @throws DocumentException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * This method generates a report for several vehicles by number list
	 */
	public static void createCommonPdfReportByNumberList(long ts_min, long ts_max, String numlist, OutputStream output)
												throws DocumentException, ClassNotFoundException, SQLException  {
		
		PdfGenerator.generateCommonPdfReport(getReportVehicleListByNumberList(ts_min, ts_max, numlist), output, ts_min, ts_max);
	}
	
	
	/**
	 * This method generates a report for a specific vehicle.
	 * @throws SQLException 
	 * @throws DocumentException 
	 * @throws ClassNotFoundException 
	 */
	public static void createSpecificPdfReport(long ts_min, long ts_max, String number, OutputStream output)
													throws ClassNotFoundException, DocumentException, SQLException {
		PdfGenerator.generateSpecificPdfReport(getTrackData(ts_min, ts_max, number), output);
	}
	
	
	
	/**
	 * This method generates List<ReportForVehicle> for time period for vehicle type
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<ReportForVehicle> getReportVehicleList(long ts_min, long ts_max, String type) 
																	throws ClassNotFoundException, SQLException {
		
		try (Connection conn = getConnection()) {
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	         return buildReportVehicleList(dsl.select(TRACKING_DATA.TIMESTAMP, 
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
		     							.between(new Timestamp(ts_min*1000), new Timestamp(ts_max*1000))
		     							.and(VEHICLE_DATA.TYPE.eq(type)))
				        		 		.fetch());
		}
	}	
	
	/**
	 * This method generates List<ReportForVehicle> for time period for number List numList separated by comma
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<ReportForVehicle> getReportVehicleListByNumberList(long ts_min, long ts_max, String numList) 
																	throws ClassNotFoundException, SQLException {
		
		try (Connection conn = getConnection()) {
			 List<String> numArr = Arrays.asList(numList.toLowerCase().replaceAll("\\s", "").split(",")); 
	         DSLContext dsl = DSL.using(conn, SQLDialect.POSTGRES_10);  
	         return buildReportVehicleList(dsl.select(TRACKING_DATA.TIMESTAMP, 
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
		     							.between(new Timestamp(ts_min*1000), new Timestamp(ts_max*1000))
		     							.and(VEHICLE_DATA.NUMBER.in(numArr)))
				        		 		.fetch());
		}
	}	
	
	
	/**
	 * This method builds List<ReportForVehicle> for time period for vehicle type
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<ReportForVehicle> buildReportVehicleList(Result<Record12<Timestamp, Short, BigDecimal,
																	BigDecimal, Short, Integer, String,
																		String, String, String, String, String>> result){
			return  result
					.stream()
    		 		.collect(Collectors.groupingBy(Record12<Timestamp, Short, BigDecimal, BigDecimal, 
    		 														Short, Integer, String, String, 
    		 															String, String, String, String>::value7))     
						
						.entrySet()
						.stream()
						.map(p->{
							return dataAnalasing(p.getValue()
	         								.stream()
	         								.sorted(new Comparator<Record12<Timestamp, Short, BigDecimal, BigDecimal, 
				        		 														Short, Integer, String, String, 
				        		 															String, String, String, String>>(){
																				
																@Override
																public int compare(
																	Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> a,
																	Record12<Timestamp, Short, BigDecimal, BigDecimal, Short, Integer, String, String, String, String, String, String> b) {
																	return a.value1().compareTo(b.value1());
																}
							}).collect(Collectors.toList()));
						})
						.map(q->{
							ReportForVehicle rfv = new ReportForVehicle(null, 0, null);
							rfv.setDistance(q.getDistance());
							rfv.setTotalWaiting(q.getWaitTrackPoints().isEmpty()?0:q.getWaitTrackPoints()
				        			.stream()
				        			.map(wp->wp.getWaiting())
				        			.reduce((x,y)->x+y).get());
							rfv.setTotalDriving(q.getSegments().isEmpty()?0:q.getSegments().stream()
				        			.map(seg->seg.getTrackPoints().get(seg.getTrackPoints().size()-1).getTimestamp()-seg.getTrackPoints().get(0).getTimestamp())
				        			.reduce((x,y)->x+y).get().intValue());
						rfv.setVehicle(q.getVehicle());
						rfv.setTsFrom(q.getTsFrom());
						rfv.setTsTo(q.getTsTo());
							return rfv;
						}).collect(Collectors.toList());
		}
	
	/**
	 * Метод возвращает список типов ТС
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static List<String> getVehicleTypes() throws ClassNotFoundException, SQLException{
		try (Connection conn = getConnection()) {
			return DSL.using(conn, SQLDialect.POSTGRES_10).select(VEHICLE_DATA.TYPE)
			.from(VEHICLE_DATA)
			.groupBy(VEHICLE_DATA.TYPE)
			.fetch()
			.stream()
			.map(i->new String(i.get(VEHICLE_DATA.TYPE)))
			.sorted()
			.collect(Collectors.toList());
		}
	}
}
