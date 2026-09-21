package com.henri_fraise.hff_data_studio.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

	@PersistenceContext
	private EntityManager entityManager;

	@Before("execution(* com.henri_fraise.hff_data_studio.service..*(..)) " +
			"&& !execution(* com.henri_fraise.hff_data_studio.service.TenantService.*(..))")
	public void enableTenantFilter() {
		String tenant = TenantContext.getTenant();
		if (tenant == null) return;

		Session session = entityManager.unwrap(Session.class);
		Filter filter = session.getEnabledFilter("tenantFilter");
		if (filter == null) {
			filter = session.enableFilter("tenantFilter");
		}
		filter.setParameter("tenantCode", tenant);
	}
}