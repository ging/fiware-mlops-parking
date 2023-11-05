package org.fiware.cosmos.orion.spark.connector.prediction

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.fiware.cosmos.orion.spark.connector.{ContentType, HTTPMethod, NGSILDReceiver, OrionSink, OrionSinkObject}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.feature.{VectorAssembler}
import org.apache.spark.ml.classification.{RandomForestClassificationModel}
import org.apache.spark.sql.SparkSession

case class PredictionResponse(predictionValue: Int) {
  override def toString :String = s"""{
  "value":${predictionValue}
  }""".trim()
}
case class PredictionRequest(name: String, weekday: Int, hour: Int, month: Int)

object PredictionJob {

  final val HOST_CB = sys.env.getOrElse("HOST_CB", "localhost")
  final val MASTER = sys.env.getOrElse("SPARK_MASTER", "local[*]")
  final val MODEL_VERSION = sys.env.getOrElse("MODEL_VERSION", "1")
  final val URL_CB = s"http://$HOST_CB:1026/ngsi-ld/v1/entities/urn:ngsi-ld:MalagaParking:001/attrs/predictionValue"
  final val CONTENT_TYPE = ContentType.JSON
  final val METHOD = HTTPMethod.PATCH
  final val BASE_PATH = "/prediction-job"
  final val MODEL_PATH = s"$BASE_PATH/model/$MODEL_VERSION"

    def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("PredictingParkingMalaga")
      .master(MASTER)
      .config("spark.cores.max", (Runtime.getRuntime.availableProcessors / 2).toString)
      .getOrCreate()
    import spark.implicits._
    spark.sparkContext.setLogLevel("WARN")
    
    println("STARTING")
    
    val ssc = new StreamingContext(spark.sparkContext, Seconds(1))


    // Load model
    val model = PipelineModel.load(MODEL_PATH)

    // Create Orion Source. Receive notifications on port 9001
    val eventStream = ssc.receiverStream(new NGSILDReceiver(9001))

    // Process event stream to get updated entities
    val processedDataStream = eventStream
      .flatMap(event => event.entities)
      .map(ent => {
        println(s"ENTITY RECEIVED: $ent")
        val month = ent.attrs("month")("value").toString.toInt
        val name = ent.attrs("name")("value").toString
        val hour = ent.attrs("hour")("value").toString.toInt
        val weekday = ent.attrs("weekday")("value").toString.toInt
        PredictionRequest(name, weekday, hour, month)
      })

    // Feed each entity into the prediction model
    val predictionDataStream = processedDataStream
      .transform(rdd => {
        val df = spark.createDataFrame(rdd)
        val predictions = model
          .transform(df)
          .select("prediction", "name", "weekday", "hour", "month")

        predictions.toJavaRDD
    })
      .map(pred=> PredictionResponse(
        pred.get(0).toString.toFloat.round * 10
      )
    )

    // Convert the output to an OrionSinkObject and send to Context Broker
    val sinkDataStream = predictionDataStream
     .map(res => OrionSinkObject(res.toString, URL_CB, CONTENT_TYPE, METHOD))

    // Add Orion Sink
    OrionSink.addSink(sinkDataStream)
    //sinkDataStream.print()
    predictionDataStream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}