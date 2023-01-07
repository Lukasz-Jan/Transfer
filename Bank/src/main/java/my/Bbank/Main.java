
/*
 * All program is written and owned by Lukasz Janowski
 * email: lukasz_jan@vp.pl
 */

package my.Bank;

import javax.annotation.PostConstruct;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
@EnableJpaRepositories(enableDefaultTransactions = false)
public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	@Value("${srcQueue}")
	private String sourceQueName;
	private static String sourceQueNameSt;

	@PostConstruct
	private void init() {
		sourceQueNameSt = sourceQueName;
	}

	@Autowired
	JmsTemplate jmsTemplate;

	public static void main(String[] args) throws InterruptedException {

		ConfigurableApplicationContext appCtx = SpringApplication.run(Main.class, args);
		log.info("sourceQueName: " + sourceQueNameSt);
	}

	@Bean
	public ActiveMQConnectionFactory activeMQConnectionFactory() {

		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(brokerUrl);
		return activeMQConnectionFactory;
	}

	@Bean
	public JmsTemplate jmsTemplate() {

		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(activeMQConnectionFactory());
		return jmsTemplate;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {

		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(activeMQConnectionFactory());
		return factory;
	}
}