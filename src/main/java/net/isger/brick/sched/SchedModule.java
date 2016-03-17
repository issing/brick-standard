package net.isger.brick.sched;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.isger.brick.Constants;
import net.isger.brick.core.Gate;
import net.isger.brick.core.GateCommand;
import net.isger.brick.core.GateModule;
import net.isger.brick.plugin.PluginCommand;
import net.isger.util.Asserts;
import net.isger.util.Dates;
import net.isger.util.Strings;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * 调度模块
 * 
 * @author issing
 * 
 */
public class SchedModule extends GateModule {

    private static final String META_SCHED = "meta.sched";

    private Scheduler scheduler;

    private Map<Sched, JobKey> jobKeys;

    public SchedModule() {
        jobKeys = new HashMap<Sched, JobKey>();
    }

    public Class<? extends Gate> getTargetClass() {
        Class<? extends Gate> targetClass = (Class<? extends Gate>) super
                .getTargetClass();
        if (targetClass == null) {
            targetClass = Sched.class;
        } else {
            Asserts.isAssignable(Sched.class, targetClass,
                    "The schedule %s must implement the %s", targetClass,
                    Sched.class);
        }
        return targetClass;
    }

    public Class<? extends Gate> getImplementClass() {
        return BaseSched.class;
    }

    public void initial() {
        super.initial();
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            for (Entry<String, Gate> entry : getGates().entrySet()) {
                createJob(entry.getKey(), (Sched) entry.getValue());
            }
            scheduler.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failure to create scheduler", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void create() {
        GateCommand cmd = GateCommand.getAction();
        super.create();
        if (!cmd.getTransient()) {
            Map<String, Sched> scheds = (Map<String, Sched>) cmd.getResult();
            Sched sched;
            for (Entry<String, Sched> entry : scheds.entrySet()) {
                sched = entry.getValue();
                try {
                    sched.create();
                    createJob(entry.getKey(), sched);
                } catch (Exception e) {
                    sched.remove();
                    throw new IllegalStateException(
                            "Failure to create schedule", e);
                }
            }
        }
    }

    public void pause() {
        GateCommand cmd = GateCommand.getAction();
        Sched sched;
        for (Entry<String, Object> entry : cmd.getParameter().entrySet()) {
            if ((sched = (Sched) getGate(entry.getKey())) != null) {
                try {
                    JobKey key = jobKeys.get(sched);
                    if (key != null) {
                        synchronized (key) {
                            scheduler.pauseJob(key);
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failure to pause schedule", e);
                }
            }
        }
    }

    public void resume() {
        GateCommand cmd = GateCommand.getAction();
        Sched sched;
        for (Entry<String, Object> entry : cmd.getParameter().entrySet()) {
            if ((sched = (Sched) getGate(entry.getKey())) != null) {
                try {
                    JobKey key = jobKeys.get(sched);
                    if (key != null) {
                        synchronized (key) {
                            scheduler.resumeJob(key);
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Failure to pause schedule", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void remove() {
        GateCommand cmd = GateCommand.getAction();
        super.remove();
        Map<String, Sched> scheds = (Map<String, Sched>) cmd.getResult();
        for (Sched sched : scheds.values()) {
            try {
                JobKey key = jobKeys.get(sched);
                if (key != null) {
                    synchronized (key) {
                        scheduler.deleteJob(key);
                    }
                    jobKeys.remove(sched);
                }
                sched.remove();
            } catch (Exception e) {
                throw new IllegalStateException("Failure to remove schedule", e);
            }
        }
    }

    private void createJob(String name, Sched sched) throws Exception {
        if (sched instanceof BaseSched) {
            PluginCommand cmd = ((BaseSched) sched).getCommand();
            String domain = cmd.getDomain();
            if (Strings.isEmpty(domain)) {
                cmd.setDomain(name);
            }
        }
        String group = Strings.empty(sched.getGroup(), Constants.DEFAULT);
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(name, group);
        /* 触发生命周期 */
        triggerBuilder.startAt(Dates.getDate(sched.getEffective(),
                sched.getDelay()));
        Date deadline = sched.getDeadline();
        if (deadline != null) {
            triggerBuilder.endAt(deadline);
        }
        /* 触发执行频率 */
        String interval = sched.getInterval();
        if (Strings.isNotEmpty(interval)) {
            triggerBuilder.withSchedule(CronScheduleBuilder
                    .cronSchedule(interval));
        }
        /* 任务详情参数 */
        JobBuilder jobBuilder = JobBuilder.newJob(BaseJob.class);
        jobBuilder.withIdentity(name, group);
        JobDetail detail = jobBuilder.build();
        JobDataMap data = detail.getJobDataMap();
        data.put(META_SCHED, sched);
        scheduler.scheduleJob(detail, triggerBuilder.build());
        jobKeys.put(sched, detail.getKey());
    }

    public void destroy() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
        }
        super.destroy();
    }

    public static final class BaseJob implements Job {

        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            Sched sched = (Sched) data.get(META_SCHED);
            sched.action();
        }

    }
}