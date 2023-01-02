package my.Bank;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import my.Bank.services.RequestService;
import my.Bank.services.ResponseService;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

@Component
public class MessageListenerAdapter implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(MessageListenerAdapter.class);

	private final String xsdRequestPath = "src/main/xsd/transfer-request-response.xsd";
	private SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	private Schema reqRespSchema;
	private XMLInputFactory xif = XMLInputFactory.newInstance();

	@Autowired
	private RequestService reqSrv;
	
	@Autowired
	private ResponseService respSrv;	

	public MessageListenerAdapter() throws Exception {
		try {
			reqRespSchema = sf.newSchema(new StreamSource(xsdRequestPath));
		} catch (SAXException e) {
			throw e;
		}
	}

	@JmsListener(destination = "${srcQueue}")
	@Override
	public void onMessage(Message mss) {

		if (mss instanceof TextMessage) {

			TextMessage textXmlMsg = (TextMessage) mss;
			TransferRequestType r = null;
			try {

				// https://howtodoinjava.com/jaxb/xsd-schema-validation/
				String requestXmlStr = textXmlMsg.getText();
				InputStream requestInputStream = new ByteArrayInputStream(requestXmlStr.getBytes());

				JAXBContext ctx = JAXBContext.newInstance(TransferRequestType.class);
				Unmarshaller u = ctx.createUnmarshaller();
				u.setSchema(reqRespSchema);
				
				XMLStreamReader reader = xif.createXMLStreamReader(requestInputStream);			
				JAXBElement<TransferRequestType> jaxEl = u.unmarshal(reader, TransferRequestType.class);
				r = jaxEl.getValue();
				
				logger.info("Request " + r.getRequestId() + " being processed");
				
				//boolean processRequest = reqSrv.processRequest(r);

			} catch (JAXBException | JMSException | XMLStreamException e) {

				logger.info("Parsing Exception caught, message not processed");
				logger.error(e.toString());
				return;
			}
			
			OutcomeType outcome = reqSrv.processRequest(r);
			respSrv.sendResponseMessage(r, outcome);
		}

	}
}