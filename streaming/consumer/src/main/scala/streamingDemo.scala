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
      .config("spark.cassandra.connection.host", "node0")
      .getOrCreate()

  println(s"after build spark session")

  def runJob() = {

  val custCol = List("account_no","customer_id","last_updated")

  val cust_chg_df = sparkSession
     .read
     .format("org.apache.spark.sql.cassandra")
     .options(Map( "table" -> "cust_change", "keyspace" -> "bank"))
     .load()

  
  println(s"after reading cust_chg")
  cust_chg_df.printSchema()


  println(s"before reading kafka stream after runJob")

  import sparkSession.implicits._
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
        transDS.map { line =>
        val payload = line._1.split(";")
        val tranpostdt = Timestamp.valueOf(payload(1))
        (payload(0), tranpostdt,
	 payload(2), payload(3).toDouble,
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
    cust_chg_df.createOrReplaceTempView("c")
    tran_df.createOrReplaceTempView("t_st")

    val joined_df = sparkSession.sql ("""  
          select c.customer_id
          ,t_st.time_stamp
          ,1 as trans_cnt
          ,t_st.amount 
	  from t_st 
	  inner join c
	  on t_st.account_no=c.account_no
	 """); 
    println(s"after join ")
    joined_df.printSchema()

    val windowedCount = joined_df
      .groupBy( $"customer_id",window($"time_stamp", "10 seconds"))
      .agg(sum($"trans_cnt").alias("trans_cnt"),sum($"amount").alias("trans_amount"))

    println(s"after window ")
    windowedCount.printSchema()
    
    val clean_df = windowedCount.selectExpr ( "customer_id", "window.start","Cast(trans_cnt as int) as trans_cnt","Cast(trans_amount as double) as trans_amount")
    println(s"after clean_df ")
    clean_df.printSchema()
  
    val query = clean_df.writeStream
      .option("checkpointLocation", "dsefs://node0:5598/tmp/")
      .format("org.apache.spark.sql.cassandra")
      .option("keyspace", "bank")
      .option("table", "cust_fraud")
      .outputMode(OutputMode.Update)
      .start()

/*   test write to console
     val query = joined_df.writeStream
      .outputMode(OutputMode.Complete)
      .queryName("table")
      .start()
*/
    println (s"after write to  cust_fraud")

    query.awaitTermination()
    println(s"after awaitTermination ")
    sparkSession.stop()
  }
}
