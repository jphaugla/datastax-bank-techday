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
      .option("startingOffsets", "latest")
      .option("kafka.bootstrap.servers", "node0:9092")
      .option("includeTimestamp", true)
      .load()
      .selectExpr("CAST(value AS STRING)","CAST(timestamp as Timestamp)",
                  "CAST(key as STRING)")
      .as[(String, Timestamp, String)] 

    println(s"finished reading transaction kafka stream ")
    transDS.printSchema()

/*
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
*/
val sens_df = transDS.withColumn("splitData", split(col("value"),";")).select(
                                        $"splitData".getItem(0).as("account_no"),
                                        $"splitData".getItem(1).cast("Timestamp").as("tranpostdt"),
                                        $"splitData".getItem(2).as("tranid"),
                                        $"splitData".getItem(3).cast("Double").as("amount"),
                                        $"splitData".getItem(4).as("bucket"),
                                        $"splitData".getItem(5).as("cardnum"),
                                        $"splitData".getItem(6).cast("Double").as("tranamt"),
                                        $"splitData".getItem(7).as("trancd"),
                                        $"splitData".getItem(8).as("trandescription"),
                                        $"splitData".getItem(9).as("transrsncd"),
                                        $"splitData".getItem(10).as("transrsndesc"),
                                        $"splitData".getItem(11).as("transrsntype"),
                                        $"splitData".getItem(12).as("transtat"),
                                        $"splitData".getItem(13).as("trantype"),
                                        $"splitData".getItem(1).cast("Timestamp").as("time_stamp")
                                        )
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
  
    val fraud_query = clean_df.writeStream
      .format("org.apache.spark.sql.cassandra")
      .option("checkpointLocation", "dsefs://node0:5598/checkpoint/cust_fraud/")
      .option("keyspace", "bank")
      .option("table", "cust_fraud")
      .outputMode(OutputMode.Update)
      .start()

    println (s"after write to  cust_fraud")

    val trans_query = tran_df.writeStream
      .format("org.apache.spark.sql.cassandra")
      .option("checkpointLocation", "dsefs://node0:5598/checkpoint/trans/")
      .option("keyspace", "bank")
      .option("table", "stream_transaction")
      .outputMode(OutputMode.Update)
      .start()
    println (s"after write to stream_transaction")

    fraud_query.awaitTermination()
    trans_query.awaitTermination()
    println(s"after awaitTermination ")
    sparkSession.stop()
  }
}
