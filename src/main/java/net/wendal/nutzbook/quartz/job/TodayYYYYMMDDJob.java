package net.wendal.nutzbook.quartz.job;

import org.nutz.ioc.loader.annotation.IocBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.wendal.nutzbook.util.Toolkit;

@IocBean
public class TodayYYYYMMDDJob implements Job {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		Toolkit.updateTodayString();
	}

}
