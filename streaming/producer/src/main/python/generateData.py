#!/usr/bin/python
#  import necessary packages 
from kafka import KafkaProducer
import datetime
import random
import socket
import csv
import time

# connect to socket to as a server 
producer = KafkaProducer(bootstrap_servers='node0:9092')
#  keep this open until killed
while 1:
        #  open csv file holding sensors metadata 
        filename = "streaming/producer/data/transaction.csv"
        f = open(filename,'r')
        #  get current time
        current_time = str(datetime.datetime.now() )
        cntr = 0
        #   use the csv reader to parse the csv file
        csv_f = csv.reader(f)
        #    loop through all the sensors in the file
        for nextTrans in csv_f:
        #  generate random movement number
                cntr += 1
                if cntr > 1:
                        account_no = nextTrans[0]
                        tranid = nextTrans[2]
                        amount = nextTrans[3]
                        bucket = nextTrans[4]
                        cardnum = nextTrans[5]
                        tranamt = nextTrans[6]
                        trancd = nextTrans[7]
                        trandescription = nextTrans[8]
                        transrsncd = nextTrans[9]
                        transrsndesc = nextTrans[10]
                        transrsntype = nextTrans[11]
                        transtat = nextTrans[12]
                        trantype = nextTrans[13]
        #   send to the link
        #       print 'this is serial %s' % (serial_number)
                        print_string =  '%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n' % (account_no,current_time,tranid,amount,bucket,cardnum,tranamt,trancd,trandescription,transrsncd,transrsndesc,transrsntype,transtat,trantype)
        #                print (print_string)
                        producer.send("transaction", print_string)
                        producer.flush()
        time.sleep(10)
# close the client socket
