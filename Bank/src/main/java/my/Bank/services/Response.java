package my.Bank.services;

import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import my.Bank.ObjectFactory;
import my.Bank.OutcomeType;
import my.Bank.TransferRequestType;
import my.Bank.TransferResponseType;

public class Response {

	public static class Builder {

		private TransferRequestType requestType;
		private OutcomeType outcome;
		private ObjectFactory objFact = new ObjectFactory();

		public String buildResponseXmlAsString() {

			TransferResponseType responseType = createResponseType(requestType, outcome);

			JAXBElement<TransferResponseType> el = buildResponseElement(responseType);

			try {
				JAXBContext ctx = JAXBContext.newInstance(TransferResponseType.class);
				Marshaller m = ctx.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				Writer writer = new StringWriter();
				m.marshal(el, writer);
				return writer.toString();

			} catch (JAXBException e) {
				e.printStackTrace();
			}
			return null;
		}

		private JAXBElement<TransferResponseType> buildResponseElement(TransferResponseType responseType) {

			JAXBElement<TransferResponseType> el = objFact.createTransferResponse(responseType);
			return el;
		}

		private TransferResponseType createResponseType(TransferRequestType requestType, OutcomeType outcome) {

			TransferResponseType responseType = objFact.createTransferResponseType();
			responseType.setRequestId(requestType.getRequestId());
			responseType.setTargetAccountNumber(requestType.getTargetAccountNumber());
			responseType.setAction(requestType.getAction());
			responseType.setCurrency(requestType.getCurrency());
			responseType.setQuantity(requestType.getQuantity());
			responseType.setOutcome(outcome);
			return responseType;
		}

		public Builder setRequestType(TransferRequestType requestType) {
			this.requestType = requestType;
			return this;
		}

		public Builder setOutcome(OutcomeType outcome) {
			this.outcome = outcome;
			return this;
		}
	}

}