package com.filet_crochet.filet_crochet;

import com.filet_crochet.filet_crochet.api.PatternsController;
import com.filet_crochet.filet_crochet.services.PatternsService;
import com.filet_crochet.filet_crochet.services.ProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Filet Crochet Application Integration Tests")
class FiletCrochetApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired(required = false)
	private PatternsController patternsController;

	@Autowired(required = false)
	private PatternsService patternsService;

	@Autowired(required = false)
	private ProgressService progressService;

	@Autowired(required = false)
	private MongoTemplate mongoTemplate;

	@Test
	@DisplayName("Application context should load successfully")
	void contextLoads() {
		assertNotNull(applicationContext);
	}

	@Test
	@DisplayName("PatternsController bean should be created")
	void testPatternsControllerBeanExists() {
		assertNotNull(patternsController, "PatternsController should be instantiated");
	}

	@Test
	@DisplayName("PatternsService bean should be created")
	void testPatternsServiceBeanExists() {
		assertNotNull(patternsService, "PatternsService should be instantiated");
	}

	@Test
	@DisplayName("ProgressService bean should be created")
	void testProgressServiceBeanExists() {
		assertNotNull(progressService, "ProgressService should be instantiated");
	}

	@Test
	@DisplayName("MongoTemplate bean should be created")
	void testMongoTemplateBeanExists() {
		assertNotNull(mongoTemplate, "MongoTemplate should be instantiated");
	}

	@Test
	@DisplayName("All required beans should be available in application context")
	void testAllRequiredBeansAvailable() {
		assertNotNull(applicationContext.getBean(PatternsController.class));
		assertNotNull(applicationContext.getBean(PatternsService.class));
		assertNotNull(applicationContext.getBean(ProgressService.class));
		assertNotNull(applicationContext.getBean(MongoTemplate.class));
	}

	@Test
	@DisplayName("Application should create singleton instances for services")
	void testServiceBeansSingleton() {
		PatternsService service1 = applicationContext.getBean(PatternsService.class);
		PatternsService service2 = applicationContext.getBean(PatternsService.class);
		assertSame(service1, service2, "PatternsService beans should be the same instance");

		ProgressService progressService1 = applicationContext.getBean(ProgressService.class);
		ProgressService progressService2 = applicationContext.getBean(ProgressService.class);
		assertSame(progressService1, progressService2, "ProgressService beans should be the same instance");
	}

	@Test
	@DisplayName("Application should have dependency injection working correctly")
	void testDependencyInjection() {
		// Services should be injected
		assertNotNull(patternsService);
		assertNotNull(progressService);

		// Controller should have access to service
		assertNotNull(patternsController);
	}

	@Test
	@DisplayName("MongoTemplate should be properly configured")
	void testMongoTemplateConfiguration() {
		assertNotNull(mongoTemplate);
		// Verify that MongoTemplate has access to MongoDB operations
		assertTrue(mongoTemplate.getClass().getName().contains("MongoTemplate"));
	}

	@Test
	@DisplayName("Application should start with Spring Boot")
	void testApplicationStartup() {
		assertNotNull(applicationContext);
		assertTrue(applicationContext.containsBean("filetCrochetApplication"));
	}

}
