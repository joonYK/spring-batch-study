package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 쿼츠 잡 정의.
 * 일정 이벤트가 발생할 때 잡을 실행하는 매커니즘을 구현.
 */
@RequiredArgsConstructor
public class BatchScheduledJob extends QuartzJobBean
{

    private final Job job;

    private final JobExplorer jobExplorer;

    private final JobLauncher jobLauncher;

    /**
     * 스케줄링된 이벤트가 발생할 때마다 한 번씩 호출됨
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //증분기를 적용한 jobParameters
        JobParameters jobParameters = new JobParametersBuilder(this.jobExplorer)
                .getNextJobParameters(this.job)
                .toJobParameters();

        try {
            this.jobLauncher.run(this.job, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
