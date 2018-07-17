COPY bank.customer (customer_id,address_line1,address_line2,address_type,bill_pay_enrolled,city,country_code,created_by,created_datetime,customer_nbr,customer_origin_system
,customer_status,customer_type,date_of_birth,email_address,first_name,gender,government_id,government_id_type,last_name,last_updated,last_updated_by,middle_name,phone_numbers,prefix)
TO  'customer.csv'
WITH HEADER = TRUE 
;
