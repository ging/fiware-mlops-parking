#!/bin/bash

sleep 10
PYSPARK_PYTHON=python3 /spark/bin/spark-submit --master spark://spark-master-train:7077 ./prediction-job/TrainingJob.py  
