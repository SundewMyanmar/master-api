package com.sdm.core.model;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TaskSchedule implements Serializable {

    @Getter(AccessLevel.NONE)
    private static final String JOB_SUFFIX = "-jobs";

    @Getter(AccessLevel.NONE)
    private static final String TRIGGER_SUFFIX = "-triggers";
    private String id;
    private String name;
    private String description;
    /**
     * Generator: http://www.cronmaker.com/
     */
    private ScheduleBuilder<?> schedule;
    private Date startAt;
    private JobDataMap data;
    private boolean startAtOnce = false;
    private Class<? extends Job> jobClass;

    public TaskSchedule(String name, Class<? extends Job> jobClass) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.jobClass = jobClass;
    }

    public JobDetail buildJobDetail() {
        String group = this.name + JOB_SUFFIX;
        JobBuilder builder = JobBuilder.newJob(this.jobClass)
                .withIdentity(this.id, group)
                .withDescription(this.description)
                .storeDurably();

        if (this.data != null && this.data.size() > 0) {
            builder.usingJobData(this.data);
        }

        return builder.build();
    }

    public Trigger buildTrigger(JobDetail jobDetail) {
        String group = this.name + TRIGGER_SUFFIX;
        TriggerBuilder<?> builder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), group)
                .withDescription(this.description)
                .withSchedule(this.schedule);

        if (this.startAtOnce) {
            builder.startNow();
        } else if (this.startAt != null) {
            builder.startAt(this.startAt);
        }

        return builder.build();
    }
}
