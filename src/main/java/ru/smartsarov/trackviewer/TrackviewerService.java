package ru.smartsarov.trackviewer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jooq.exception.DataAccessException;


@Path("/")
@Produces(MediaType.TEXT_XML + ";charset=UTF-8")
public class TrackviewerService
{
	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML + ";charset=UTF-8")
    public Response index()
    {
		InputStream is = this.getClass().getResourceAsStream("/static/index.html");
    	return Response.status(Response.Status.OK).entity(is).build();
    }
	@GET
	@Path("/xsd_scheme")
	@Produces(MediaType.TEXT_XML + ";charset=UTF-8")
    public Response xsdScheme()
    {
		InputStream is = this.getClass().getResourceAsStream("/other/scheme.xsd");
    	return Response.status(Response.Status.OK).entity(is).build();
    }
	
	
	@GET
	@Path("/vehicle/show")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response insertVehicle() 
    {

				try {
						return Response.status(Response.Status.OK).entity(Trackviewer.getVehicleList()).build();
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						return Response.status(Response.Status.OK).entity(e.toString()).build();
					}

    }
	
	
	@GET
	@Path("/track/get_json")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getTrackJson(
    		@QueryParam ("min_ts") long min_ts,
    		@QueryParam ("max_ts") long max_ts,
    		@QueryParam ("vehicle_number") String vehicleNumber) throws ClassNotFoundException
    {
		try {
				return Response.status(Response.Status.OK).entity(Trackviewer.jsonTrackData(min_ts, max_ts, vehicleNumber==null?null:vehicleNumber.toLowerCase())).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }
	
	@GET
	@Path("/track/timeline")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getJsonDay(
    		@QueryParam ("ts") long ts,
    		@QueryParam ("type") String type) throws ClassNotFoundException
    {
		try {
				return Response.status(Response.Status.OK).entity(Trackviewer.getStatistic24HourJson(ts, type)).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }
	
	
	
	@POST
	@Path("/data/insert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGeoLocationList(String jsonRequest) {
	    	try {
				return Response.status(Response.Status.OK).entity(Trackviewer.InsertJsonInto(jsonRequest)).build();
			} catch (ClassNotFoundException | IOException | SQLException e) {
				// TODO Auto-generated catch block
				return Response.status(Response.Status.OK).entity(e.toString()).build();
			}
	}
	
	
	@GET
	@Path("/track/timeline/get_hourly")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getJsonHour(
    		@QueryParam ("ts") long ts) throws ClassNotFoundException
    {
		try {
			Trackviewer.createHourlyReport(ts*1000);
				return Response.status(Response.Status.OK).entity("Ok").build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }
	@GET
	@Path("/track/timeline/get_week")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getJsonWeek(
    		@QueryParam ("ts") long ts,
    		@QueryParam ("type") String type) throws ClassNotFoundException
    {
		try {
			Trackviewer.createHourlyReport(ts*1000);
				return Response.status(Response.Status.OK).entity(Trackviewer.getWeekReportByType(ts, type)).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }
	
	
	
	
	
}