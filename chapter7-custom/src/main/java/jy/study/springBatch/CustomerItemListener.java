package jy.study.springBatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.annotation.OnReadError;

public class CustomerItemListener {

    private static final Log logger = LogFactory.getLog(CustomerItemListener.class);

    @OnReadError
    public void onReadError(Exception e) {
        logger.error(e.getMessage(), e);
    }
}
