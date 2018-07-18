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
import org.apache.spark.sql.types.{IntegerType,StringType}



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

//   join static customer with streaming df


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
//  prepare the dataframes to be used in spark sql command with tempview
    cust_raw_df.createOrReplaceTempView("c")
    tran_df.createOrReplaceTempView("t_st")
    acct_raw_df.createOrReplaceTempView("a")

    val joined_df = sparkSession.sql ("""  
          select t.customer_id
          ,t.trans_cnt 
          ,1 as changed_flag
          ,t.time_stamp
          from (select a.customer_id
          ,t_st.time_stamp
	  ,count(*) trans_cnt
	  from t_st 
	  inner join a
	  on t_st.account_no=a.account_no
          group by a.customer_id,t_st.time_stamp ) t
	 """); 
    println(s"after join ")
    joined_df.printSchema()

    val windowedCount = joined_df
      .groupBy( $"customer_id",window($"time_stamp", "1 minute"))
      .agg(sum($"trans_cnt").alias("trans_cnt"),sum($"changed_flag").alias("changed_cust_cnt"))

    println(s"after window ")
    windowedCount.printSchema()
    
    val clean_df = windowedCount.selectExpr ( "customer_id", "window.start","Cast(trans_cnt as int) as trans_cnt","Cast(changed_cust_cnt as int) as changed_cust_cnt")
    println(s"after clean_df ")
    clean_df.printSchema()
  
    val query = clean_df.writeStream
      .option("checkpointLocation", "/tmp/checkit")
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "bank")
      .option("table", "cust_fraud")
      .outputMode(OutputMode.Complete)
      .start()
/*   test write to console
     val query = joined_df.writeStream
      .outputMode("complete")
      .queryName("table")
      .format("console")
      .start()
*/

    println (s"after write to  cust_fraud")

    query.awaitTermination()
    sparkSession.stop()
  }
}
