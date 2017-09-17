Digital Banking
========================

To create the schema, run the following

	mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaSetup" -DcontactPoints=localhost

To create the customers, accounts and transactions, run the following (note the create parameter to create customers and accounts as well)
	
	mvn clean compile exec:java -Dexec.mainClass="com.datastax.banking.Main"  -DcontactPoints=localhost -Dcreate=true

You can use the following parameters to change the default no of transactions, customers and no of days.
	
	-DnoOfTransactions=10000000 -DnoOfCustomers=1000000 -DnoOfDays=5

RealTime transactions
When all historical transactions are loaded, the process will start creating random transactions for todays date and time. If you wish just to run real time transactions specify -DnoOfDays=0.

To use the web service run 

	mvn jetty:run
	
The api for the webservices are 

Get Customer 

	http://{server}:8080/datastax-digital-banking/rest/get/customer/{customer_id}

	http://localhost:8080/datastax-digital-banking/rest/get/customer/1000111

Get Customer Accounts
	
	http://{server}:8080/datastax-digital-banking/rest/get/accounts/{customer_id}
	
	http://localhost:8080/datastax-digital-banking/rest/get/accounts/1000111
	
Get Transaction For Account 
	
	http://{server}:8080/datastax-digital-banking/rest/get/transactions/{account_id}
	
	http://localhost:8080/datastax-digital-banking/rest/get/transactions/eeceed17-5d7e-40de-be07-bdc2f075feb6
	

To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    
     
