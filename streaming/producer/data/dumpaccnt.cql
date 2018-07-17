COPY bank.account (customer_id,account_no,account_origin_system,account_status,account_type,last_updated,last_updated_by)
TO  'account.csv'
WITH HEADER = TRUE 
;
