package jy.study.springBatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RequiredArgsConstructor
public class CustomerByCityQueryProvider extends AbstractJpaQueryProvider {

    private final String cityName;

    @Override
    public Query createQuery() {
        EntityManager entityManager = getEntityManager();

        Query query = entityManager.createQuery(
                "select c from Customer c where c.city = :city");
        query.setParameter("city", cityName);

        return query;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(cityName, "City name is required");
    }
}
