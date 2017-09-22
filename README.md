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

	http://{server}:8080/datastax-bank-techday/rest/get/customer/{customer_id}

	http://localhost:8080/datastax-bank-techday/rest/get/customer/1000111

Get Customer Accounts
	
	http://{server}:8080/datastax-bank-techday/rest/get/accounts/{customer_id}
	
	http://localhost:8080/datastax-bank-techday/rest/get/accounts/1000111
	
Get Transaction For Account 
	
	http://{server}:8080/datastax-bank-techday/rest/get/transactions/{account_id}
	
	http://localhost:8080/datastax-bank-techday/rest/get/transactions/eeceed17-5d7e-40de-be07-bdc2f075feb6
	

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

Some queries below on customer table.
	
	select customer_id,full_name from bank.customer where customer_id = '1084545';
	select customer_id,full_name from bank.customer where solr_query = 'last_name:84545';
	select customer_id,full_name from bank.customer where solr_query = 'last_name:8454*';
	select customer_id,full_name,government_id, email_address from bank.customer where solr_query = 'government_id:*4545';
	select customer_id,full_name,email_address from bank.customer where solr_query = '{!tuple}email_address.email_type:Home'; 
	select customer_id,full_name,email_address,phone_numbers from bank.customer where solr_query = '{!tuple}email_address.email_address:*24541';
	select customer_id,full_name,phone_numbers from bank.customer where solr_query = '{!tuple}phone_numbers.phone_number:*14540h';
	select customer_id,full_name,address_line1,address_line2,city,state_abbreviation,zipcode from bank.customer where solr_query = 'address_line1:*14543';
	select customer_id,full_name,phone_numbers from bank.customer where solr_query = 'full_name:Jason* AND {!tuple}phone_numbers.phone_number:*14540w';
	select customer_id,custaccounts from bank.customer where solr_query = 'custaccounts:"44ba4d82-6f5b-4381-9f3e-ccee0e89099f"';

Some queries below on transactions table.
	
	select account_no,tranid,cardnum from bank.transaction where account_no = '4626b219-a324-4ad5-89f1-a9a0d114fb78';
	select account_no,tranid,cardnum
	from bank.transaction where solr_query  = 'cardnum:bccfcec5-9837-403d-9d18-26cad571125e';
	select account_no,tranid,merchantctgydesc from bank.transaction where solr_query = 'merchantctgydesc:"Grocery Stores"';
	select tranpostdt,account_no,tranid,cardnum from bank.transaction where solr_query = 'tranpostdt:[2017-08-26T00:00:00Z TO 2017-09-29T00:00:00Z]';
	select merchantname,tranpostdt from bank.transaction where solr_query = 'merchantname:"Cub Foods"';
		
