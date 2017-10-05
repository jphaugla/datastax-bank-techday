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

Get Transactions for a Customer

	http://{server}:8080/datastax-bank-techday/rest/get/customer/{customer_id}

	http://localhost:8080/datastax-bank-techday/rest/get/customer/1000111

Get Customer Accounts
	
	http://{server}:8080/datastax-bank-techday/rest/get/accounts/{customer_id}
	
	http://localhost:8080/datastax-bank-techday/rest/get/accounts/1000111
	
Get Transactions For Account 
	
	http://{server}:8080/datastax-bank-techday/rest/get/transactions/{account_id}
	
	http://localhost:8080/datastax-bank-techday/rest/get/transactions/eeceed17-5d7e-40de-be07-bdc2f075feb6

Get Customers by email - email string can have wildcards
	
	http://{server}:8080/datastax-bank-techday/rest/get/customerByEmail/{phoneString}

	http://localhost:8080/datastax-bank-techday/rest/get/customerByEmail/100011*

Get Customers by phone number - phone string can have wildcards
	
	http://{server}:8080/datastax-bank-techday/rest/get/customerByPhone/{phoneString}

	http://localhost:8080/datastax-bank-techday/rest/get/customerByPhone/100011*

Get Customers by full name and phone number - both strings can have wildcards

	http://{server}:8080/datastax-bank-techday/rest/get/customerByFullNamePhone/{fullName}/{phoneString}/
	http://localhost:8080/datastax-bank-techday/rest/get/customerByFullNamePhone/*Jason*/100011*/

Get Customers by credit card number (wildcards work) start and end transaction date

	http://{server}:8080/datastax-bank-techday/rest/get/getcctransactions/{cardnum}/{fromDate}/{toDate}/
	http://localhost:8080/datastax-bank-techday/rest/get/getcctransactions/5a4e5d9e-56c6-41bd-855a-3c38884be07f/20170925/20180101/

To remove the tables and the schema, run the following.

    mvn clean compile exec:java -Dexec.mainClass="com.datastax.demo.SchemaTeardown"
    
To create DSE Search environment, run the following in cqlsh.

    CREATE SEARCH INDEX if not exists
	ON bank.customer
	with columns address_line1
                ,city
                ,email_address
                ,first_name
                ,full_name
                ,last_name
                ,middle_name
		,government_id
		,government_id_type
                ,phone_numbers
                ,state_abbreviation
                ,zipcode
		,custaccounts;
    CREATE SEARCH INDEX if not exists
	ON bank.transaction
	with columns tranPostDt
                ,merchantName
		,merchantCtgyDesc
		,cardNum;

Resources in src/main/resources/
    queries.txt       - cql queries on the customer table 
    trans_queries.txt - cql queries on the transaction table 
    createSolr.cql    - create solr core with cql
    copyToCSV.cql     - copy all table to csv files (run from main directory for output to go in export folder) 	

Resources in src/main/api/
    addTag.html    - add tag to a transaction by opening this file from browser
    removeTag.html - remove tag from a transaction by opening this file from browser
    addTag.sh      - add tag to a transaction using curl command
    removeTag.sh   - remove tag from a transaction using curl command

scripts in src/main/scripts/  (all of these must be run from root directory)
    compileSetup.sh - compile and run including creating customers and accounts and transactions
    runTrans.sh - compile and run without creating customers and accounts (only transactions)
    top100.sh  - put top 100 records to sharing directory from export directory for each csv 
