package my.Bank.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import my.Bank.entities.creator.AccountFactory;
import com.networknt.schema.SpecVersion.VersionFlag;

@Component
public class JsonService {

	private static final Logger logger = LoggerFactory.getLogger(JsonService.class);

	@Value("${jsonSchema}")
	private String jsonSchemaPath;
	
	@Autowired
	private AccountFactory accountCreator;

	@Autowired
	private ApplicationContext context;

	private ObjectMapper mapper = new ObjectMapper();
	private String fileWithLoadedData;

	@PostConstruct
	private void init() throws JsonProcessingException, IOException {

		fileWithLoadedData = context.getEnvironment().getProperty("jsonFilePath");
		final File schemaFile = new File(jsonSchemaPath);
		final File jsonFile = new File(fileWithLoadedData);
		final InputStream targetSchemaStream = new FileInputStream(schemaFile);
		final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
		final JsonSchema jsonSchema = factory.getSchema(targetSchemaStream);
		final JsonNode root = mapper.readTree(jsonFile);
		Set<ValidationMessage> errors = jsonSchema.validate(root);

		if (errors.isEmpty()) {

			for (JsonNode acctNode : root.path("accounts")) {

				String acctNo = acctNode.path("accountNumber").asText();

				for (JsonNode currencyAmounts : acctNode) {

					for (JsonNode currencyAmount : currencyAmounts) {

						String currency = currencyAmount.path("currency").asText();
						BigDecimal amount = currencyAmount.path("amount").decimalValue();
						accountCreator.setAcctId(acctNo).setCurrency(currency).setCurAmt(amount).buildEntity();
						logger.info("Account no " + acctNo + " added");
					}
				}
			}
		}

		IOUtils.closeQuietly(targetSchemaStream);
	}

	public void changeAmountForAccount(String searchAccount, String searchCurr, BigDecimal newAmount) {

		final File jsonFile = new File(fileWithLoadedData);
		JsonNode root = null;
		try {
			root = mapper.readTree(jsonFile);
		} catch (IOException e) {
			logger.error("Reading Json exception");
			return;
		}

		for (JsonNode acctNode : root.path("accounts")) {

			String acctNoStr = acctNode.path("accountNumber").asText();

			if (acctNoStr.equals(searchAccount)) {

				JsonNode currencyAmounts = acctNode.path("currencyAmounts");
				for (JsonNode singleCurrencyAmount : currencyAmounts) {

					String currency = singleCurrencyAmount.path("currency").asText();
					if (currency.equals(searchCurr)) {

						JsonNode foundNode = singleCurrencyAmount;
						logger.debug("foundNode: " + foundNode);
						ObjectNode obj = (ObjectNode) foundNode;
						obj.put("amount", newAmount.setScale(2, RoundingMode.HALF_UP));
						break;
					}
				}
				break;
			}
		}

		try {
			mapper.writeValue(jsonFile, root);
		} catch (IOException e) {
			logger.error("Writing Json exception");
		}
	}
}
