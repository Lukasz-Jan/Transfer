package my.Bank.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	@Value("${jsonFilePath}")
	private String jsonFilePath;

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private AccountFactory accountCreator;
	
	@PostConstruct
	private void init() throws JsonProcessingException, IOException {

		final File schemaFile = new File(jsonSchemaPath);
		final File jsonFile = new File(jsonFilePath);
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
	}
}
