# Tranfer System Exercise - Instructions

Create a simple application that supports transfering funds out of and into specific accounts.

The application should support the following features:

* account storage
* account debiting/crediting
* accounts import/export

## Feature Details

### Account Storage

Internally, the application should store information on accounts.

#### Account Number

Each account should be uniquely identified by an account number - a non-empty string containing only digits. 
No two accounts stored by the application should be allowed to have the same account number.

#### Currency Holdings

Along with each account, the application should store the account's holdings - by currency. 
For example, an account could have 50 PLN, and 140 USD. 
If an account does not contain a record of the amount for a given currency, 
then that should be understood as the account not being allowed to hold that currency.

### Account Debiting/Crediting

The application should support a MQ (message queue) based request/response interface 
through which a specific account can be credited or debited some amount in a given currency. 

#### Requests

The application should consume messages from a request queue 
(the name of this queue should be configurable). 
These messages will have a XML payload matching the TransferRequest schema contained in [transfer-request-response.xsd]
(transfer-request-response.xsd). 
An example of a conformant XML can be found in [example-transfer-request.xml](example-transfer-request.xml).

#### Responses

The application should produce messages to a response queue (the name of this queue should be configurable).
These messages will have a XML payload matching the TransferResponse schema contained in [transfer-request-response.xsd](transfer-request-response.xsd).
 An example of a conformant XML can be found in [example-transfer-response.xml](example-transfer-response.xml).

#### Logic

The application should only accept the request message if it matches the TransferRequest schema and the following conditions are met:

 * the application has in its storage an account with account number matching the value in the *TargetAccountNumber* field,
 * the account is allowed to hold the currency specified in the *Currency* field,
 * if the value in the *Action* field is `DEBIT`, then the account must hold at least the amount specified in the *Quantity* field.

If these conditions are met, then:

 * For *Action* = `CREDIT`, the amount specified by field *Quantity* of currency specified by field *Currency* should be added to the account.
 * For *Action* = `DEBIT`, the amount specified by field *Quantity* of currency specified by field *Currency* should by subtracted from the account.
 
 Additionally, a reponse message should be sent, with the value `ACCEPT` in the *Outcome* field. 
 All other fields should have values matching the equivalent fields from the incoming request message. 

In all other situations, the request message should be rejected. If the rejected request message is malformed (does not match the TransferRequest schema), 
the error should only be logged. 
Otherwise, a response message should be sent, with value `REJECT` in the *Outcome* field. 
All other fields should have values matching the equivalent fields from the incoming request message.

### Account Import/Export

On startup, the application should import the account information from a JSON file. 
A path to the file should be specified as a commandline argument to the applicatio`n. 
On each update to the account information stored in the application, the file should be updated too.

#### JSON Schema

The file used for import and export of account information should conform 
to the schema contained in [transfersystem.schema.json](transfersystem.schema.json). 
An example of a conformant JSON file can be found in [example-transfer-system.json](example-transfer-system.json). 


## Additional Requirements

* The source code should be stored in a **git** repository, with non-relevant files excluded via **gitignore**.
* **JDK 11** or higher should be used for the project.
* Either **Maven** or **Gradle** should be used as a build tool for the project.
* Supporting code should be generated from the JSON schema and XSD via a maven or gradle plugin.
* A **JMS**-compatible MQ implementation such as ActiveMQ should be used for the MQ related functionality.
* As many unit tests as possible and at least one integration test should be written for the project, preferably with JUnit 5.
* Attention should be given to maintaining a clear structure and general cleanliness within the project.

## Useful Links

* [JSON Schema](https://json-schema.org)
* [W3 Schools - XML Schema](https://www.w3schools.com/xml/schema_intro.asp)
* [ActiveMQ - an example MQ implementation](https://activemq.apache.org/)
* [JAXB - Java Architecture for XML Binding](https://javaee.github.io/jaxb-v2/)