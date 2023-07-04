package org.fiware.cosmos.orion.spark.connector.prediction

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{VectorAssembler, VectorIndexer, StringIndexer, OneHotEncoder}
import org.apache.spark.ml.classification._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, DoubleType, StructField, StructType}

object TrainingJob {

  def main(args: Array[String]): Unit = {
    train().write.overwrite().save("./prediction-job/model")

  }
  
  def train() = {
    // ocupation= round((1-available)*10)
    val schema = StructType(
      Array(StructField("name", StringType),
            StructField("availableSpotNumber", IntegerType),
            StructField("timestamp", StringType),
            StructField("weekday", IntegerType),
            StructField("total", IntegerType),
            StructField("day", IntegerType),
            StructField("month", IntegerType),
            StructField("hour", IntegerType),
            StructField("minute", IntegerType),
            StructField("hour_interval", IntegerType),
            StructField("time", StringType),  
            StructField("available", DoubleType), 
            StructField("occupation", IntegerType)                   
      ))
    val spark = SparkSession
      .builder
      .appName("TrainingParkingMalaga")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    spark.sparkContext.setLogLevel("WARN")

    // convert to dataframe
    val data = spark.read.format("csv")
      .schema(schema)
      .option("header", "true")
      .option("delimiter", ",")
      .load("./prediction-job/malaga_parking.csv")

    // onehotencoder
    val it = Array("name", "weekday")
    val stringIndexers = it.map (
        c => new StringIndexer().setInputCol(c).setOutputCol(s"${c}-index")
    )
    val oneHotEncoders = it.map (
        c => new OneHotEncoder().setInputCol(s"${c}-index").setOutputCol(s"${c}-ohe")
    )
              
    val vectorAssembler  = new VectorAssembler()
      .setInputCols(Array("name-ohe","weekday-ohe","hour","month"))
      .setOutputCol("features")

    // Automatically identify categorical features, and index them.
    // var transformedDf = vectorAssembler.transform(data)

    //val Array(trainingData, testData) = transformedDf.randomSplit(Array(0.8, 0.2))


    val rfc = new RandomForestClassifier()
      .setNumTrees(200)
      .setFeatureSubsetStrategy("log2")
      .setLabelCol("occupation")
      .setFeaturesCol("features")

    val pipeline = new Pipeline().setStages(stringIndexers ++ oneHotEncoders ++ Array(vectorAssembler,rfc))
    val Array(trainingData,testData) = data.randomSplit(Array(0.8,0.2))
    val model = pipeline.fit(trainingData)
    val predictions = model.transform(testData)

    predictions.select("prediction","occupation", "hour", "weekday", "name","month").show(10)

    // val evaluator = new MulticlassClassificationEvaluator()
    //   .setLabelCol("occupation")
    //   .setPredictionCol("prediction")
    //   .setMetricName("accuracy")
    // val accuracy = evaluator.evaluate(predictions)
    // println(s"Accuracy = ${accuracy}")

    //println(s"Learned classification forest model:\n ${rfModel.toDebugString}")

    //return the model with the pipeline
    model
  }
}