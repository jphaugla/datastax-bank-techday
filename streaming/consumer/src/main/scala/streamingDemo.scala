package com.kafkaToSparkToDSE

/**
  */
import java.sql.Timestamp
import java.text.{DateFormat, SimpleDateFormat}

import com.datastax.driver.core.Session

import collection.JavaConversions._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.streaming._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}



object streaming {


  def main(args: Array[String]) {

    println(s"entered main")

    val sparkJob = new SparkJob()
    try {
      sparkJob.runJob()
    } catch {
      case ex: Exception =>
        println("error in main running spark job")
    }
  }
}

class SparkJob extends Serializable {

  println(s"before build spark session")

  val sparkSession =
    SparkSession.builder
      .appName("kafka2Spark2DSE")
      .config("spark.cassandra.connection.host", "localhost")
      .getOrCreate()

  println(s"after build spark session")

  def runJob() = {

  val custCol = List("customer_id","address_line1","address_line2","address_type",
		"city","country_code","date_of_birth","email_address","full_name",
		"state_abbreviation","zipcode","zipcode4")

  val cust_raw_df = sparkSession
     .read
     .format("org.apache.spark.sql.cassandra")
     .options(Map( "table" -> "customer", "keyspace" -> "bank"))
     .load()

  
    println(s"after reading cust")
    cust_raw_df.printSchema()

//  this second dataframe allows to select limited number of columns
//   wish could do in one step but can't get it to work
   val cust_df = cust_raw_df.select ( "customer_id","address_line1","address_line2","address_type", "city","country_code","date_of_birth","email_address","full_name",
		"state_abbreviation","zipcode","zipcode4")
    println(s"after select cust")
    cust_df.printSchema()

  val acct_raw_df = sparkSession
     .read
     .format("org.apache.spark.sql.cassandra")
     .options(Map( "table" -> "account", "keyspace" -> "bank"))
     .load()

  
    println(s"after reading acct")
    acct_raw_df.printSchema()


    println(s"before reading kafka stream after runJob")

    import sparkSession.implicits._
    val lines = sparkSession.readStream
      .format("kafka")
      .option("subscribe", "customer")
      .option("failOnDataLoss", "false")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("startingOffsets", "earliest")
      .option("includeTimestamp", true)
      .load()
      .selectExpr("CAST(value AS STRING)","CAST(timestamp as Timestamp)",
                  "CAST(key as STRING)")
      .as[(String, Timestamp, String)] 

    lines.printSchema()
    println(s"finished reading kafka stream ")

    val cols = List("customer_id","address_line1","address_line2","address_type","bill_pay_enrolled","city","country_code","customer_nbr","customer_origin_system","customer_status","customer_type","date_of_birth","email_address","gender","government_id","government_id_type","phone_numbers","time_stamp")
    val df =
      lines.map { line =>
        val payload = line._1.split(";")
        val dob_ts = Timestamp.valueOf(payload(11))
        (payload(0), payload(1),
	 payload(2), payload(3),
	 payload(4), payload(5),
	 payload(6), payload(7),
	 payload(8), payload(9),
	 payload(10), dob_ts,
	 payload(12), payload(13),
	 payload(14), payload(15),
	 payload(16), line._2
         )
      }.toDF(cols: _*)
    println(s"after toDF ")
    df.printSchema()
    println(s"after printschema ")
    println(df.isStreaming)
    println(s"after isStreaming ")
//   join static customer with streaming df
/*   this worked but remaining conditions is just oo ugly
    val joined_df = df.join(cust_df, "customer_id")
*/


    val transDS = sparkSession.readStream
      .format("kafka")
      .option("subscribe", "transaction")
      .option("failOnDataLoss", "false")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("startingOffsets", "earliest")
      .option("includeTimestamp", true)
      .load()
      .selectExpr("CAST(value AS STRING)","CAST(timestamp as Timestamp)",
                  "CAST(key as STRING)")
      .as[(String, Timestamp, String)] 

    println(s"finished reading transaction kafka stream ")
    transDS.printSchema()

    val tran_cols =  List("account_no","tranpostdt","tranid","amount","bucket","cardnum","tranamt","trancd","trandescription","transrsncd","transrsndesc","transrsntype","transtat","trantype","time_stamp")

    val tran_df =
        lines.map { line =>
        val payload = line._1.split(";")
        val tranpostdt = Timestamp.valueOf(payload(1))
        (payload(0), tranpostdt,
	 payload(2), payload(3),
	 payload(4), payload(5),
	 payload(6), payload(7),
	 payload(8), payload(9),
	 payload(10), payload(11),
	 payload(12), payload(13),
	 line._2
         )
      }.toDF(tran_cols: _*)
    println(s"after tran_df ")
    tran_df.printSchema()
//  this writes successfully to the console-hurray!
    df.createOrReplaceTempView("c_st")
    cust_raw_df.createOrReplaceTempView("c")
    tran_df.createOrReplaceTempView("t_st")
    acct_raw_df.createOrReplaceTempView("a")

/*  works
    val joined_df = sparkSession.sql ("""  
          select a.customer_id
	  ,count(*) as trans_count 
	  from t_st 
	  inner join a
	  on t_st.account_no=a.account_no
          left join c_st
	  on c_st.customer_id = a.customer_id
          group by a.customer_id
	 """); 
*/
    val joined_df = sparkSession.sql ("""  
          select a.customer_id
	  ,case when c_st.customer_id is null or 
		c_st.address_line1<>a.address_line1
		then 1 else 0 end changed_flag
	  ,t_st.time_stamp
	  from t_st 
	  inner join a
	  on t_st.account_no=a.account_no
          left join c_st
	  on c_st.customer_id = a.customer_id
	 """); 
    println(s"after join ")
    joined_df.printSchema()

/*
    val windowedCount = joined_df
      .groupBy( $"customer_id",
        window($"time_stamp", "15 seconds")
      )
        .count()
*/
    val windowedCount = joined_df
      .groupBy( window($"time_stamp", "15 seconds")
      )
        .count()
    println(s"after window ")
    windowedCount.printSchema()
/*
    val query = joined_df.writeStream
      .option("checkpointLocation", "/tmp")
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "bank")
      .option("table", "account_fraud")
      .outputMode(OutputMode.Update)
      .start()
    println (s"after write to  account_fraud")
*/

    // Group the data by window and word and compute the count of each group

//      query.awaitTermination()
      sparkSession.stop()
  }
}
