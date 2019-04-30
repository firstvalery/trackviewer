package ru.smartsarov.trackviewer;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CheckBreath implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
			Trackviewer.checkForBreath();
	}
}
