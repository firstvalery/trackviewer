package ru.smartsarov.trackviewer;

import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import ru.samrtsarov.yandexgeo.GetGeo;
import ru.smartsarov.trackviewer.JsonTrack.JsonTrack;
import ru.smartsarov.trackviewer.JsonTrack.ReportForVehicle;
import ru.smartsarov.trackviewer.JsonTrack.TrackPoint;

public class PdfGenerator {


	/**
	 * This method writes a common PDF report into the OutputStream output
	 * @throws DocumentException 
	 */
	public static void generateCommonPdfReport(List<ReportForVehicle>rfvList, OutputStream output, long ts_min, long ts_max) throws DocumentException {
    	Document document = new Document();
    	PdfWriter.getInstance(document, output);
		document.open();
		
		String fontStyleFileName = Props.get().getProperty("report_font", FontFactory.COURIER);
		
		Font basicFont = FontFactory.getFont(fontStyleFileName, "Cp1251", true);
		Font majorFont = FontFactory.getFont(fontStyleFileName, "Cp1251", true); 
		
		basicFont.setSize(10);
		majorFont.setSize(22);
		
		 
         
        Paragraph paragraph = new Paragraph();

        
        paragraph.setSpacingBefore(25);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
		
		document.add(new Phrase(150));
		
		paragraph.add(new Phrase("Отчет о передвижении автотранспорта", majorFont));
		document.add(paragraph);
		
		
        
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(0);
        paragraph.setIndentationRight(0);
        paragraph.clear();
		
        
        paragraph.add(new Phrase("за период:", basicFont));
        document.add(paragraph);
        paragraph.clear();
        
        
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts_min), ZoneId.systemDefault());
        paragraph.setSpacingBefore(0);
        paragraph.add(new Phrase(zdt.format(DateTimeFormatter.ofPattern("с  HH:mm:ss   dd.MM.yyyy")).toString(), basicFont));
        document.add(paragraph);
        paragraph.clear();
        zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts_max), ZoneId.systemDefault());
 		paragraph.add(new Phrase(zdt.format(DateTimeFormatter.ofPattern("по HH:mm:ss   dd.MM.yyyy")).toString(), basicFont));
        document.add(paragraph);
        
        PdfPTable table = new PdfPTable(6); // 6 columns.
        table.setWidthPercentage(100);
        addTableCommonPdfReportHeader(table, basicFont);
        addTableCommonPdfReportRows(rfvList, table, basicFont);
        table.setSpacingBefore(50f);
        document.add(table);

		document.close();
	}
	
	
	/**
	 * This method adds a headers into the table of the common report
	 */
	private static void addTableCommonPdfReportHeader(PdfPTable table, Font basicFont) {
	    Stream.of("Гос. номер", "Дата и время", "Время движения", "Время стоянки","Пробег, км", "Расход, л")
	      .forEach(columnTitle -> {
	    	  			PdfPCell header = new PdfPCell();
	    	  			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	    	  			header.setBorderWidth(2);
	    	  			header.setPhrase(new Phrase(columnTitle, basicFont));
	    	  			table.addCell(header);
	      			});
	}
	
	
	/**
	 * This method adds a cells into the table the common report
	 */
	private static void addTableCommonPdfReportRows(List<ReportForVehicle> rfvList, PdfPTable table, Font font) {
		rfvList.stream()
				.forEach(rfv->{	
					PdfPCell cell = new PdfPCell();
					
					//Гос номер
					cell.setPhrase(new Phrase(rfv.getVehicle().getNumber(), font));
					table.addCell(cell);
					
					//Дата и время
					cell.setPhrase(new Phrase(ZonedDateTime.ofInstant(Instant.ofEpochSecond(rfv.getTsFrom()), ZoneId.systemDefault())
												.format(DateTimeFormatter.ofPattern("с HH:mm   dd.MM.yyyy ")).toString()
												
												+ ZonedDateTime.ofInstant(Instant.ofEpochSecond(rfv.getTsTo()), ZoneId.systemDefault())
												.format(DateTimeFormatter.ofPattern("по HH:mm   dd.MM.yyyy")).toString() , 
												font));
					table.addCell(cell);
					
					//Общее время движения
					cell.setPhrase(new Phrase(secToHHMMSS(rfv.getTotalDriving()), font));		
					table.addCell(cell);
					
					//"Время стоянки"
					cell.setPhrase(new Phrase(secToHHMMSS(rfv.getTotalWaiting()), font));		
					table.addCell(cell);
					
					//Пробег
					cell.setPhrase(new Phrase(String.format("%.1f", rfv.getDistance()/1000.0F), font));		
					table.addCell(cell);
					//Расход
					cell.setPhrase(new Phrase(String.format("%.1f", 0F), font));		
					table.addCell(cell);
				});
	}
	
	
	
	/**
	 * This method writes a specific PDF report into the OutputStream output
	 * @throws DocumentException 
	 */
	public static void generateSpecificPdfReport(JsonTrack jt, OutputStream output) throws DocumentException {
		Document document = new Document();
    	PdfWriter.getInstance(document, output);
		document.open();
		
		String fontStyleFileName = Props.get().getProperty("report_font", FontFactory.COURIER);	
		Font basicFont = FontFactory.getFont(fontStyleFileName, "Cp1251", true);
		Font majorFont = FontFactory.getFont(fontStyleFileName, "Cp1251", true); 
		basicFont.setSize(10);
		majorFont.setSize(22);
		
		Paragraph paragraph = new Paragraph();
        paragraph.setSpacingBefore(25);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setIndentationLeft(50);
        paragraph.setIndentationRight(50);
		
		document.add(new Phrase(150));	
		paragraph.add(new Phrase("Отчет о местах остановок", majorFont));
		document.add(paragraph);
		
		paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(0);
        paragraph.setIndentationRight(0);
        paragraph.clear();
		
        
        paragraph.add(new Phrase(jt.getVehicle().getModel()
        											.concat(" ")
        											.concat(jt.getVehicle().getNumber().toUpperCase(Locale.ROOT)), basicFont));
        document.add(paragraph);
        paragraph.clear();
		
		
		paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(0);
        paragraph.setIndentationRight(0);
        paragraph.setSpacingBefore(10);
        paragraph.clear();
		
        
        paragraph.add(new Phrase("за период:", basicFont));
        document.add(paragraph);
        paragraph.clear();
        
        
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(jt.getTsFrom()), ZoneId.systemDefault());
        paragraph.setSpacingBefore(0);
        paragraph.add(new Phrase(zdt.format(DateTimeFormatter.ofPattern("с  HH:mm   dd.MM.yyyy")).toString(), basicFont));
        document.add(paragraph);
        paragraph.clear();
        zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(jt.getTsTo()), ZoneId.systemDefault());
 		paragraph.add(new Phrase(zdt.format(DateTimeFormatter.ofPattern("по HH:mm   dd.MM.yyyy")).toString(), basicFont));
        document.add(paragraph);
        
        PdfPTable table = new PdfPTable(5); // 5 columns.
        table.setWidthPercentage(100);
        addTableSpecificPdfReportHeader(table, basicFont);
        addTableSpecificPdfReportRows(jt, table, basicFont);
        
        table.setSpacingBefore(50f);
        document.add(table);
        

		
		document.close();
	}
	
	/**
	 * This method adds a headers into the table of the specific report
	 */
	private static void addTableSpecificPdfReportHeader(PdfPTable table, Font font) {
	    Stream.of("Время остановки", "Время начала движения", "Продолжительность остановки", "Адрес", "Расход, л")
	      .forEach(columnTitle -> {
	    	  			PdfPCell header = new PdfPCell();
	    	  			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
	    	  			header.setBorderWidth(2);
	    	  			header.setPhrase(new Phrase(columnTitle, font));
	    	  			table.addCell(header);
	      			});
	}
	
	/**
	 * This method adds a cells into the table the common report
	 */
	private static void addTableSpecificPdfReportRows(JsonTrack jt, PdfPTable table, Font font) {
		jt.getWaitTrackPoints().stream()
				.forEach(wp->{	
					PdfPCell cell = new PdfPCell();
					
					//Время остановки
					cell.setPhrase(new Phrase(ZonedDateTime.ofInstant(Instant.ofEpochSecond(wp.getTrackPoint().getTimestamp()), ZoneId.systemDefault())
							.format(DateTimeFormatter.ofPattern("с HH:mm   dd.MM.yyyy ")).toString(), font));
					table.addCell(cell);
					//Время начала движения
					cell.setPhrase(new Phrase(ZonedDateTime.ofInstant(Instant.ofEpochSecond(wp.getTrackPoint().getTimestamp() + wp.getWaiting()), ZoneId.systemDefault())
							.format(DateTimeFormatter.ofPattern("по HH:mm   dd.MM.yyyy ")).toString(), font));
					table.addCell(cell);
					//Продолжительность
					cell.setPhrase(new Phrase(secToHHMMSS(wp.getWaiting()), font));
					table.addCell(cell);
					
					
					
					cell.setPhrase(new Phrase(GetGeo.getAdress(trackPointToGeo(wp.getTrackPoint())), font));
					table.addCell(cell);
					
					cell.setPhrase(new Phrase("Расход",font));
					table.addCell(cell);
				});
	}
	
	/**
	 * TrackPoint to String for geocoder
	 */
	public static String trackPointToGeo(TrackPoint tp) {
		return tp.getLongitude().toString()+","+tp.getLatitude().toString();
	}
	
	

	
	/**
	 * returns String value HH:MM:SS of long seconds
	 * 
	 */
	private static String secToHHMMSS(long duration) {
		return String.format("%02d:%02d:%02d", duration / 3600, duration / 60 % 60, duration % 60);
	}
	
	
	
}
