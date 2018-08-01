# datstax-bank-techday

This demo simulates a bank looking for customers having transactions after having a change in their customer information such as an email change, phone number change, or and address change.  For simplicity, this demo outputs as soon as a customer with an address change has transactions.  In a real-world use case, a threshold of transactions based on dollar amount and volume would be needed. 

The purpose of this demo is to demonstrate a Kafka/Spark/Scala structured streaming.  This has a scala producer program to write transactions to a kafka topic.  Additionaly, a consumer spark structured streaming job reads a stream from the kafka topic, joins the stream to a cassandra table and writes the results to a cassandra table.  

In order to run this demo, It is assumed that you have the following installed and available on your local system.

  1. Datastax Enterprise 6.x
  2. Apache Kafka 1.1.1, Scala 2.11 build
  3. git
  4. sbt

##Getting Started with Kafka
Use the steps below to setup up a local instance of Kafka for this example. This is based off of kafka_2.11-1.1.0.tgz


Ubuntu helpful tips at https://devops.profitbricks.com/tutorials/install-and-configure-apache-kafka-on-ubuntu-1604-1/ 

### Locate and download Apache Kafka

Kafka can be located at this URL: 
	[http://kafka.apache.org/downloads.html](http://kafka.apache.org/downloads.html)

download and install the binary version for Scala 2.11.

###  install sbt

#### on ubuntu 

	echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
	sudo apt-get update
	sudo apt-get install sbt

#### on mac 
	brew install sbt

### Download and install Datastax Enterprise v6.0.x

  * `https://academy.datastax.com/downloads/welcome`

### Install Apache Kafka and Zookeeper

Once downloaded you will need to extract the file. It will create a folder/directory. Move this to a location of your choice.

#### (on mac)

	brew install kafka
	pip install kafka-python

verify the correct kafka in path from brew
	`which kafka-topics` should return "/usr/local/bin/kafka-topics"
double check the link is correct in /usr/local/bin for desired version
	`ls -lrt /usr/local/bin/kafka-topics` should point to "../Cellar/kafka/1.1.0/bin/kafka-topics"


#### (on ubuntu)

	sudo apt-get install zookeeperd
	wget http://apache.claz.org/kafka/1.1.0/kafka_2.11-1.1.0.tgz
	sudo mkdir /opt/Kafka
	cd /opt/Kafka
	sudo tar -xvf ~datastax/kafka_2.11-1.1.0.tgz -C /opt/Kafka

for convenience, created a soft link to /opt/Kafka/kafka 

	cd /opt/Kafka
	ln -s kafka_2.11-1.1.0 kafka

#### for kafka and python on ubuntu 

	sudo apt-get install python-pip python-dev build-essential
	sudo pip install kafka-python

### Start ZooKeeper and Kafka
Start local copy of zookeeper and Kafka

####  on Mac

this starts zookeeper
  * zkServer start
this starts kafka
  * `kafka-server-start  /usr/local/etc/kafka/server.properties`

####  on Ubuntu

	add kafka bin to the PATH in your profile
           echo "export PATH=/opt/Kafka/kafka/bin:$PATH" >> ~/.bashrc
	sudo /opt/Kafka/kafka/bin/kafka-server-start.sh /opt/Kafka/kafka/config/server.properties

(zookeeper automatically starts on install)

moving forward, manage zookeeper on ubuntu with "service zookeeper status"

### Prepare a message topic for use.

Create the topic we will use for the demo

###  on mac, command is kafka-topics (sh suffix not needed)
  * `kafka-topics.sh --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --topic transaction`

Validate the topic was created. 

  * `kafka-topics.sh --zookeeper localhost:2181 --list`
  
### A Couple of other useful Kafka commands
####  on ubuntu, commands need sh suffix 

Delete the topic. (Note: The server.properties file must contain `delete.topic.enable=true` for this to work)

  * `kafka-topics.sh --zookeeper localhost:2181 --delete --topic transaction`
  
Show all of the messages in a topic from the beginning

  * `kafka-console-consumer.sh --zookeeper localhost:2181 --topic transaction --from-beginning`
  
#Getting Started with Local DSE/Cassandra

see README in github home directory for this information
this streaming demo builds on having the tables created and the jetty server running for the API call from the home README.md

###To build the demo

  * Navigate to the root directory of the project where you downloaded
  * Build the Producer with this command:
  
    `sbt producer/package`
      
  * Build the Consumer with this command:
  
    `sbt consumer/package`

   see note at bottom if errors here

###To run the demo

This assumes you already have Kafka and DSE up and running and configured as in the steps above.

  * From the root directory of the project start the producer app
  
	`sbt producer/run`

    
  
  * From the root directory of the project start the consumer app

	`./runConsumer.sh   

  * To demo the asset, generate rows in the cust_change table using:
	./src/main/resources/api/addCustChange.sh
     this adds account number each 30 seconds and demonstrates static 
     cassandra table is actually refreshed on each time window to pull
     more customers with attribute changes

  * To visualize results use spark-sql to allow the sorting of the results
     dse spark-sql
         spark-sql> select customer_id, start,trans_cnt
		    from bank.cust_fraud
		    order by start;
627619a1	2018-07-31 22:39:30	2
486f2572	2018-07-31 22:39:40	2
627619a1	2018-07-31 22:39:40	2
486f2572	2018-07-31 22:39:50	2
56d18578	2018-07-31 22:39:50	1
627619a1	2018-07-31 22:39:50	2
486f2572	2018-07-31 22:40:00	2
56d18578	2018-07-31 22:40:00	1
627619a1	2018-07-31 22:40:00	2
 
     notice how in 30 second cycle only 627619a1 appears
     then in 40 second cycle 627619a1 and 486f2572 
     afterward, all three are in the cycle.  This is due to the API call
     simulating cust_change 
####  PROBLEMS with build.sbt
Needed to clean out jar files on spark and dse dependencies

	rm -rf ~/.ivy2/cache/org.apache.spark/*
	rm -rf ~/.ivy2/cache/com.datastax.dse/*
