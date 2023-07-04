package org.fiware.cosmos.orion.spark.connector.prediction

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector.{ContentType, HTTPMethod, NGSILDReceiver, OrionSink, OrionSinkObject}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.{VectorAssembler}
import org.apache.spark.ml.classification.{RandomForestClassificationModel}
import org.apache.spark.sql.SparkSession

case class PredictionResponse(socketId: String, predictionId: String, predictionValue: Int, name: String, weekday: Int, hour: Int, month: Int) {
  override def toString :String = s"""{
  "socketId": { "value": "${socketId}", "type": "Property"},
  "predictionId": { "value":"${predictionId}", "type": "Property"},
  "predictionValue": { "value":${predictionValue}, "type": "Property"},
  "name": { "value":"${name}", "type": "Property"},
  "weekday": { "value":${weekday}, "type": "Property"},
  "time": { "value": ${hour}, "type": "Property"},
  "month": { "value": ${month}, "type": "Property"}
  }""".trim()
}
case class PredictionRequest(name: String, weekday: Int, hour: Int, month: Int, socketId: String, predictionId: String)

object PredictionJob {

  final val URL_CB = "http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:ResMalagaParkingPrediction1/attrs"
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val BASE_PATH = "./prediction-job"

    def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("PredictingParkingMalaga")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    
    println("STARTING")
    
    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))


    // Load model
    val model = PipelineModel.load(BASE_PATH+"/model")

    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new NGSILDReceiver(9001))

    // Process event stream to get updated entities
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(ent => {
        println(s"ENTITY RECEIVED: $ent")
        //val year = ent.attrs("year")("value").toString.toInt
        val month = ent.attrs("month")("value").toString.toInt
        //val day = ent.attrs("day")("value").toString.toInt
        val name = ent.attrs("name")("value").toString
        val hour = ent.attrs("time")("value").toString.toInt
        val weekday = ent.attrs("weekday")("value").toString.toInt
        val socketId = ent.attrs("socketId")("value").toString
        val predictionId = ent.attrs("predictionId")("value").toString
        PredictionRequest(name, weekday, hour, month, socketId, predictionId)
      })

    // Feed each entity into the prediction model
    val predictionDataStream = processedDataStream
      .transform(rdd => {
        val df = spark.createDataFrame(rdd)
        val predictions = model
          .transform(df)
          .select("socketId","predictionId", "prediction", "name", "weekday", "hour", "month")

        predictions.toJavaRDD
    })
      .map(pred=> PredictionResponse(
        pred.get(0).toString,
        pred.get(1).toString,
        pred.get(2).toString.toFloat.round * 10,
        pred.get(3).toString,
        pred.get(4).toString.toInt,
        pred.get(5).toString.toInt,
        pred.get(6).toString.toInt
      )
    )

    // Convert the output to an OrionSinkObject and send to Context Broker
    val sinkDataStream = predictionDataStream
      .map(res => OrionSinkObject(res.toString, URL_CB, CONTENT_TYPE, METHOD))

    // Add Orion Sink
    OrionSink.addSink(sinkDataStream)
    //sinkDataStream.print()
    //predictionDataStream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}