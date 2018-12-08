package ru.smartsarov.trackviewer;

import java.sql.SQLException;
import java.time.Instant;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ControlJob implements Job{
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
					try {
						Trackviewer.createHourlyReport(Instant.now().toEpochMilli());
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
		}

	}