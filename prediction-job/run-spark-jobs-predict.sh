#! /bin/bash -eu

/spark/bin/spark-submit  --class  org.fiware.cosmos.orion.spark.connector.prediction.PredictionJob --master  spark://spark-master:7077 ./prediction-job/target/orion.spark.connector.prediction-1.0.1.jar --conf "spark.driver.extraJavaOptions=-Dlog4jspark.root.logger=WARN,console"