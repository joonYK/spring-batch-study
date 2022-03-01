package jy.study.springBatch;

import jy.study.springBatch.domain.Customer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * 플랫 파일에 푸터 추가.
 * 레코드 수를 기록.
 *
 * ItemWriteListener.beforeWrite를 호출하지 않고 애스펙트를 사용하는 이유는 호출 순서 때문인데,
 * MultiResourceItemWriter.write를 호출하기 전에 ItemWriteListener.beforeWrite가 호출되지만,
 * FlatFileItemWriter.open은 MultiResourceItemWriter.write 메서드 내에서 호출되는데
 * FlatFileItemWriter.write를 호출하기 전에 카운터를 초기화하려고 애스펙트를 사용.
 */
@Component
@Aspect
public class CustomerRecordCountFooterCallback implements FlatFileFooterCallback {

    private int itemsWrittenInCurrentFile = 0;

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("This file contains " + itemsWrittenInCurrentFile + " items");
    }

    /**
     * FlatFileItemWriter.open 메서드 수행전에 호출.
     * 새 파일을 열 때마다 카운터를 초기화.
     */
    @Before("execution(* org.springframework.batch.item.file.FlatFileItemWriter.open(..))")
    public void resetCounter() {
        this.itemsWrittenInCurrentFile = 0;
        System.out.println("resetCounter");
    }

    /**
     * FlatFileItemWriter.write 메서드 수행전에 호출.
     * 전달된 아이템 수에 따라 카운트를 증가.
     */
    @Before("execution(* org.springframework.batch.item.file.FlatFileItemWriter.write(..))")
    public void beforeWrite(JoinPoint joinPoint) {
        List<Customer> items = (List<Customer>) joinPoint.getArgs()[0];
        System.out.println("beforeWrite");

        this.itemsWrittenInCurrentFile += items.size();
    }
}
