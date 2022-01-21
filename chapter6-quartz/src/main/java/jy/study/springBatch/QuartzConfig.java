package jy.study.springBatch;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    /**
     * 쿼츠 잡의 빈을 구성
     */
    @Bean
    public JobDetail quartzJobDetail() {
        //쿼츠 잡 클래스를 정의한 BatchScheduledJob을 전달해서 생성.
        return JobBuilder.newJob(BatchScheduledJob.class)
                .storeDurably()
                .build();
    }

    /**
     * 트리거 생성.
     * 스케줄과 JobDetail을 연관 지음.
     */
    @Bean
    public Trigger jobTrigger() {
        //스캐줄 생성 (얼마나 자주 JobDetail을 실행할 것인가)
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                //5초마다 한 번씩 잡을 수행하고, 최초 한번 수행 이후 4번 반복 수행. (총 5회 실행)
                .withIntervalInSeconds(5).withRepeatCount(4);

        return TriggerBuilder.newTrigger()
                .forJob(quartzJobDetail())
                .withSchedule(scheduleBuilder)
                .build();
    }
}
