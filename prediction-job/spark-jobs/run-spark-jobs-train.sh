#! /bin/bash -eu
FILE=./prediction-job/target/orion.spark.connector.prediction-1.0.1.jar


echo "sleeping 60 seconds..." 

sleep 60

while [ ! -f $FILE ]
do
  echo "Waiting for generating the package"
  sleep 5  
done
echo "Package generated"


/spark/bin/spark-submit --driver-memory 4g --class  org.fiware.cosmos.orion.spark.connector.prediction.TrainingJob --master  spark://spark-master:7077 --deploy-mode client $FILE --conf "spark.driver.extraJavaOptions=-Dlog4jspark.root.logger=WARN,console"