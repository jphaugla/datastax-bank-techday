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
customer_origin_system text,
customer_status text,
customer_type text,
date_of_birth text,
email_address frozen<list<frozen<email>>>,
first_name text,
full_name text,
gender text,
government_id text,
government_id_type text,
last_name text,
last_updated timestamp,
last_updated_by text,
middle_name text,
phone_numbers frozen<list<frozen<phone>>>,
prefix text,
query_helper_column text,
state_abbreviation text,
zipcode text,
zipcode4 text,
custaccounts set<text>,
	PRIMARY KEY(customer_id)
) WITH compaction= { 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
  AND compression = {'sstable_compression' : ''}
  AND caching = {'keys':'NONE', 'rows_per_partition':'NONE'};

create table if not exists cust_change(
	account_no text,
	customer_id text,  
        last_updated text,
	PRIMARY KEY((account_no),customer_id,last_updated)
) WITH compaction= { 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
  AND compression = {'sstable_compression' : ''}
  AND caching = {'keys':'NONE', 'rows_per_partition':'NONE'};


create table if not exists account(
	customer_id text,  
	account_no text,
	account_type text,
	account_origin_system text,
        account_status text,
        address_line1 text,
        address_line2 text,
        address_type text,
        city text,
        country_code text,
        country_name text,
        created_by text,
        created_datetime timestamp,
        customer_nbr text,	
        date_of_birth text,
        first_name text,
        full_name text,
        government_id text,
        government_id_type text,
        last_name text,
        last_updated timestamp,
        last_updated_by text,
        middle_name text,
        phone_numbers frozen<list<frozen<phone>>>,
        prefix text,
        state_abbreviation text,
        zipcode text,
        zipcode4 text,
	PRIMARY KEY(customer_id, account_no)
) WITH compaction= { 'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
  AND compression = {'sstable_compression' : ''}
  AND caching = {'keys':'NONE', 'rows_per_partition':'NONE'};

create table if not exists transaction(
	account_no text,
	account_type text,
	amount_type text,
	amount double,
  cardNum text,
  first_name text,
  full_name text,
  last_name text,
  middle_name text,
  city text,
  country_code text,
  country_name text,
  address_line1 text,
  address_line2 text,
  address_type text,
  state_abbreviation text,
  zipcode text,
  zipcode4 text,
  merchantCtygCd text,
  merchantCtgyDesc text,
  merchantName text,
  origTranAmt text,
  referenceKeyType text,
  referenceKeyValue text,
tranAmt  double,
tranCd  text,
tranDescription text,
tranExpDt text,
tranId text,
tranInitDt  timestamp,
tranPostDt  timestamp,
tranStat    text,
tranType    text,
transRsnCd text,
transRsnDesc text,
transRsnType text,
transRespCd  text,
transRespDesc text,
transRespType  text,
	bucket text,
	customers set<text>, 
	tags set<text>, 
	location text,
	PRIMARY KEY ((account_no), tranPostDt,tranId)
)WITH CLUSTERING  order by (tranPostDt desc); 

create table if not exists account_st (
customer_id int,
account_no text,
account_origin_system text,
account_status text,
account_type text,
last_updated timestamp,
last_updated_by text,
PRIMARY KEY ((customer_id,account_no),last_updated)
) WITH CLUSTERING order by (last_updated desc);

create table if not exists customer_st (
customer_id text,
address_line1 text,
address_line2 text,
address_type text,
bill_pay_enrolled text,
city text,
country_code text,
customer_nbr text,
customer_origin_system text,
customer_status text,
customer_type text,
date_of_birth timestamp,
email_address text,
gender text,
government_id text,
government_id_type text,
phone_numbers text,
PRIMARY KEY (customer_id)
) ;

create table if not exists cust_fraud (
customer_id text,
start timestamp,
trans_cnt int,
changed_cust_cnt int,
PRIMARY KEY (customer_id,start)
) WITH CLUSTERING order by (start desc);
