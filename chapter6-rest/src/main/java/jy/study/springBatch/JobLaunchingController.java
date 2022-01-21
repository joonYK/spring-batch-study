package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JobLaunchingController {

    private final JobLauncher jobLauncher;

    private final ApplicationContext context;

    private final JobExplorer jobExplorer;

    @PostMapping("/run")
    public ExitStatus runJob(@RequestBody JobLaunchRequest request) throws Exception {
        Job job = this.context.getBean(request.getName(), Job.class);

        //job에 RunIdIncrementer를 적용했기때문에 run.id라는 파라미터가 추가된 새로운 JobParameters 인스턴스 생성.
        JobParameters jobParameters = new JobParametersBuilder(request.getJobParameters(), this.jobExplorer)
                //Job이 JobParametersIncremeter를 가지고 있는지 해당 Job을 보고 판별.
                //JobParametersIncremeter를 가지고 있으면 마지막 JobExecution에 사용됐던 JobParameters를 이용해서 적용.
                .getNextJobParameters(job)
                .toJobParameters();

        return this.jobLauncher.run(job, jobParameters).getExitStatus();
    }
}
