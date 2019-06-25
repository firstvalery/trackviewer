package ru.smartsarov.trackviewer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.text.DocumentException;

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
						return Response.status(Response.Status.OK).entity(new Gson().toJson(Trackviewer.getVehicleList())).build();
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						return Response.status(Response.Status.OK).entity(e.toString()).build();
					}

    }
	
	@GET
	@Path("/vehicle/types/show")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getVehicleTypes() 
    {

				try {
						return Response.status(Response.Status.OK).entity(new Gson().toJson(
																					Trackviewer.getVehicleTypes())).build();
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
    		@QueryParam ("vehicle_number") String vehicleNumber)
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
    		@QueryParam ("type") String type)
    {
		try {
				return Response.status(Response.Status.OK).entity(Trackviewer.getDayReportByType(ts, type)).build();
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
			}catch(JsonSyntaxException e) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Trackviewer.getJsonMessage(e.toString())).build();
			} 
	}
	

	@GET
	@Path("/track/timeline/get_week")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getJsonWeek(
    		@QueryParam ("ts") long ts,
    		@QueryParam ("type") String type)
    {
		try {
				return Response.status(Response.Status.OK).entity(Trackviewer.getWeekReportByType(ts, type)).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity( Trackviewer.getJsonMessage(e.toString())).build();
		}
    }
	
	
	@GET
	@Path("/track/report/generate_hourly")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response generateHourReport(
    		@QueryParam ("ts") long ts)
    {
		try {
			Trackviewer.createHourlyReport(ts);
				return Response.status(Response.Status.OK).entity("Ok").build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }

	
	@GET
	@Path("/track/report/generate_for_day")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response generateHourlyForDay(
    		@QueryParam ("ts") long ts,
    		@QueryParam ("type") String type)
    {
		try {
			Trackviewer.createHourlyReportForDay(ts);
				return Response.status(Response.Status.OK).entity("Ok").build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Response.status(Response.Status.OK).entity(e.toString()).build();
		}
    }
	

	@GET
	@Path("/track/report/commonPdf")
	@Produces({"application/pdf"})
	public StreamingOutput getCommonPdf(
			@QueryParam ("ts_min") long ts_min,
			@QueryParam ("ts_max") long ts_max,
    		@QueryParam ("type") String type){
		try {
		    return new StreamingOutput() {
		    	@Override
		        public void write(OutputStream output) throws IOException, WebApplicationException {
			    	try {
						Trackviewer.createCommonPdfReport(ts_min, ts_max, type, output);					
					} catch (DocumentException | ClassNotFoundException | SQLException e) {
						throw new WebApplicationException();
					}	
		        }
		    };
		}catch(WebApplicationException e) {
			//TODO
			return null;
		}
	}  
	

	@GET
	@Path("/track/report/commonPdfByNumber")
	@Produces({"application/pdf"})
	public StreamingOutput getCommonPdfByNumberList(
			@QueryParam ("ts_min") long ts_min,
			@QueryParam ("ts_max") long ts_max,
    		@QueryParam ("numList") String numList){
		try {
		    return new StreamingOutput() {
		    	@Override
		        public void write(OutputStream output) throws IOException, WebApplicationException {
			    	try {
						Trackviewer.createCommonPdfReportByNumberList(ts_min, ts_max, numList, output);					
					} catch (DocumentException | ClassNotFoundException | SQLException e) {
						throw new WebApplicationException();
					}	
		        }
		    };
		}catch(WebApplicationException e) {
			//TODO
			return null;
		}
	}  
	
	

	@GET
	@Path("/track/report/specificPdf")
	@Produces({"application/pdf"})
	public StreamingOutput getSpecificPdf(
			@QueryParam ("ts_min") long ts_min,
			@QueryParam ("ts_max") long ts_max,
    		@QueryParam ("number") String number){
		try {
		    return new StreamingOutput() {
		    	@Override
		        public void write(OutputStream output) throws IOException, WebApplicationException {
			    	try {
						Trackviewer.createSpecificPdfReport(ts_min, ts_max, number, output);					
					} catch (DocumentException | ClassNotFoundException | SQLException e) {
						throw new WebApplicationException();
					}	
		        }
		    };
		}catch(WebApplicationException e) {
			//TODO
			return null;
		}
	}  
}