package jy.study.springBatch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

/*
 * 잡 파라미터 유효성 검증기 (커스텀)
 */
public class ParameterValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String fileName = parameters.getString("fileName");

        //fileName 파라미터 있는지 체크
        if (!StringUtils.hasText(fileName))
            throw new JobParametersInvalidException("fileName parameter is missing");
        //.csv 파일인지 체크
        else if (!StringUtils.endsWithIgnoreCase(fileName, "csv"))
            throw new JobParametersInvalidException("fileName parameter doesn't use the csv file extension");
    }
}
