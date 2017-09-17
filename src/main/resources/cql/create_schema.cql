create keyspace if not exists bank WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

use bank;


CREATE TYPE if not exists email (
email_type text,
email_address text,
email_status text
);

CREATE TYPE if not exists phone (
phone_type text,
phone_number text
);

create table if not exists customer(
	customer_id text,
	address_line1 text,
	address_line2 text,
	address_type text,
bill_pay_enrolled text,
city text,
country_code text,
created_by text,
created_datetime timestamp,
customer_nbr text,
customer_id_list list<text>,
customer_id_set set<text>,
customer_origin_system text,
customer_status text,
customer_type text,
date_of_birth text,
email_address frozen<list<frozen<email>>>,
first text,
full_name text,
gender text,
government_id text,
government_id_type text,
last text,
last_updated timestamp,
last_updated_by text,
middle_name text,
phone_numbers frozen<list<frozen<phone>>>,
prefix text,
query_helper_column text,
state_abbreviation text,
zipcode text,
zipcode4 text,
	externalSystemIds map<text,text>,
	social map<text,text>,
	emails map<text,text>,	
	phones map<text,text>,
	accounts set<text>,
	PRIMARY KEY(customer_id)
) WITH compaction= { 'class': 'MemoryOnlyStrategy' }
  AND compression = {'sstable_compression' : ''}
  AND caching = {'keys':'NONE', 'rows_per_partition':'NONE'};

create table if not exists account(
	customer_id text,  
	account_no text,
	account_type text,
	customers set<text>,	
	iban text,
	currency text,
	balance double,
	last_updated_balance timestamp,
	PRIMARY KEY(customer_id, account_no)
) WITH compaction= { 'class': 'MemoryOnlyStrategy' }
  AND compression = {'sstable_compression' : ''}
  AND caching = {'keys':'NONE', 'rows_per_partition':'NONE'};

create table if not exists customer_addresses(
	customer_id text,
	address_type text,
	details map<text,text>,	
	PRIMARY KEY((customer_id), address_type)
);

create table if not exists customer_interactions(
	customer_id text,
	interaction_time timestamp,
	page text,
	action text,
	PRIMARY KEY(customer_id, interaction_time)
)WITH CLUSTERING order by (interaction_time desc);

create table if not exists transaction(
	account_no text,
	bucket text,
	transaction_time timestamp,
 	transaction_id text,
	customers set<text>, 
	tags set<text>, 
	location text,
	merchant text,
	amount double,
	PRIMARY KEY ((account_no, bucket), transaction_time)
)WITH CLUSTERING  order by (transaction_time desc); 

