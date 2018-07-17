COPY bank.transaction (account_no,tranpostdt,tranid,amount,bucket,cardnum,tranamt,trancd,trandescription,transrsncd,transrsndesc,transrsntype,transtat,trantype)
TO  'transaction.csv'
WITH HEADER = TRUE 
;
