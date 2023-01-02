package my.Bank.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import my.Bank.OutcomeType;
import my.Bank.TransferRequestType;

@Component
public class ResponseService {

	private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);
	
	@Value("${responseQueue}")
	private String respQueName;
	
    @Autowired
    JmsTemplate jmsTemplate;

	public void sendResponseMessage(TransferRequestType requestType, OutcomeType outcome) {

    	String responseXmlAsString = new Response.Builder().setRequestType(requestType).setOutcome(outcome).buildResponseXmlAsString();
    	
    	jmsTemplate.convertAndSend(respQueName, responseXmlAsString);
    	logger.info("Response sent");
    	logger.debug(responseXmlAsString);
    }
}
