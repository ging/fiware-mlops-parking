from pyspark.ml import Pipeline
from pyspark.ml.evaluation import RegressionEvaluator
from pyspark.ml.feature import VectorAssembler, VectorIndexer, StringIndexer, OneHotEncoder
from pyspark.ml.classification import RandomForestClassifier
from pyspark.sql import SparkSession
from pyspark.sql.types import IntegerType, StringType, StructField, StructType, DoubleType
import mlflow
from mlflow.entities import RunStatus
from mlflow import MlflowClient
import mlflow.tracking
import mlflow.spark
import os
import datetime
import multiprocessing

MASTER = os.environ.get("SPARK_MASTER", "local[*]")
#MASTER = "local[*]"
MLFLOW_HOST = os.environ.get("MLFLOW_HOST", "localhost")
CSV_FILE = os.environ.get("CSV_FILE", "malaga_parking.csv")
MLFLOW_URI = "http://"+MLFLOW_HOST+":5000"
BASE_PATH = os.environ.get("PROYECT_BASE_PATH", "/prediction-job")

def train():
    schema = StructType([
        StructField("name", StringType()),
        StructField("availableSpotNumber", IntegerType()),
        StructField("timestamp", StringType()),
        StructField("weekday", IntegerType()),
        StructField("total", IntegerType()),
        StructField("day", IntegerType()),
        StructField("month", IntegerType()),
        StructField("hour", IntegerType()),
        StructField("minute", IntegerType()),
        StructField("hour_interval", IntegerType()),
        StructField("time", StringType()),
        StructField("available", DoubleType()),
        StructField("occupation", IntegerType())
    ])

    # Initialize MLFlow connection and experiment

    
    mlflow.set_experiment_tag("createdAt", datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S-%f"))

    spark = SparkSession.builder \
        .appName("TrainingJob") \
        .master(MASTER) \
        .config("spark.cores.max", str(int(multiprocessing.cpu_count() / 2))) \
        .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    # Load and parse the data file, converting it to a DataFrame.
    data = spark.read.format("csv") \
        .schema(schema) \
        .option("header", "true") \
        .option("delimiter", ",") \
        .load(BASE_PATH+"/"+CSV_FILE)

    # Define columns for one-hot encoding
    categorical_cols = ["name", "weekday"]

    string_indexers = [StringIndexer(inputCol=col, outputCol=col + "-index") for col in categorical_cols]

    one_hot_encoders = [OneHotEncoder(inputCol=col + "-index", outputCol=col + "-ohe") for col in categorical_cols]

    feature_cols = ["name-ohe", "weekday-ohe", "hour", "month"]
    vector_assembler = VectorAssembler(inputCols=feature_cols, outputCol="features")


    num_trees = 200

    mlflow.log_param("numTrees", str(num_trees))

    # Train a RandomForest model.
    rfc = RandomForestClassifier(numTrees=num_trees, featureSubsetStrategy="log2", labelCol="occupation", featuresCol="features")

    stages = string_indexers + one_hot_encoders + [vector_assembler, rfc]
    pipeline = Pipeline(stages=stages)

    # Split the data into training and testing sets
    trainingData, testData = data.randomSplit([0.8, 0.2])

    model = pipeline.fit(trainingData)
    
    model_path = BASE_PATH+"/model"
    rf_model = model.stages[1]

    # Register model
    model_name = 'parkingModel'
    mlflow.spark.log_model(spark_model=model, artifact_path=model_name, registered_model_name=model_name)

    client = MlflowClient(tracking_uri=MLFLOW_URI)
    model_version = client.get_latest_versions(model_name, stages=["None"])[0].version
    model.save(model_path+"/"+model_version)



if __name__ == "__main__":
    mlflow.set_tracking_uri(MLFLOW_URI)
    mlflow.set_experiment("Experiment")
    experiment = mlflow.get_experiment_by_name("Experiment")
    experiment_id = experiment.experiment_id
    with mlflow.start_run(experiment_id=experiment_id, description="Model for predicting parking state") as run:
        train()