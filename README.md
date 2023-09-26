# FIWARE Machine Learning and MLOps - Smart parking example


## Description

MLOps use case with FIWARE to predict the % of occupancy of Malaga's Parkings.


## Instructions

* Load and save the `prediction-job/malaga_parking.csv` [from external source](https://drive.upm.es/s/hA1jEQChEwZyVQj)
```shell
curl -o prediction-job/malaga_parking.csv https://drive.upm.es/s/hA1jEQChEwZyVQj/download
```

* Start base infraestructure
```
docker compose up -d
```


* Start training job
```
docker compose -f docker-compose.submit_train.yml up -d
```


* Start prediction job
```
docker compose -f docker-compose.submit_predict.yml up -d
```

- Access http://localhost:5000 to access MLFlow client

- Access http://localhost:3000 to watch the web

- Access http://localhost:8080 to access the Spark Cluste UI client


* Train again
Start training job
```
docker compose -f docker-compose.submit_train.yml up -d
```
