package ru.smartsarov.trackviewer;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 
 * 
 * 
 * 
 */


public class ThreadListener implements ServletContextListener{
	Scheduler sched;
	
	public void contextInitialized(ServletContextEvent sce) { 
		JobDetail jobOn = JobBuilder.newJob(ControlJob.class)
									.withIdentity("Control", "group1")
									.build();
		
		Trigger trigger = TriggerBuilder.newTrigger()
									.withIdentity("CronTrigger", "group1")	
				          			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
				                    .build();
		
		JobDetail jobCheck = JobBuilder.newJob(CheckBreath.class)
				.withIdentity("CheckingForBreath", "group2")
				.build();
		//триггер каждые 15 минут
		Trigger triggerForCheck = TriggerBuilder.newTrigger()
				.withIdentity("CheckTrigger", "group2")	
      			.withSchedule(CronScheduleBuilder.cronSchedule("0 0,15,30,45 * * * ?"))
                .build();
		try {
			sched = new StdSchedulerFactory().getScheduler();
			sched.start();
			sched.scheduleJob(jobOn, trigger);
			sched.scheduleJob(jobCheck, triggerForCheck);
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 public void contextDestroyed(ServletContextEvent sce) {
		 try {
			sched.shutdown();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
