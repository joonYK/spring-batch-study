package jy.study.springBatch.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.annotation.OnReadError;

public class ErrorLogListener {

    private static final Log logger = LogFactory.getLog(ErrorLogListener.class);

    @OnReadError
    public void onReadError(Exception e) {
        logger.error(e.getMessage(), e);
    }
}
