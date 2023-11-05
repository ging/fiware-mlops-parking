# FIWARE Machine Learning and MLOps - Smart parking example


## Description

MLOps use case with FIWARE to predict the % of occupancy of Malaga's Parkings.

## Instructions


First time (to build the .jar):
```
docker compose -f docker-compose.maven.yml up -d
```

Start base infraestructure
```
docker compose up -d
```
It also creates the following entity and subscription:

Entity:

```
{
    "id": "urn:ngsi-ld:MalagaParking:001",
    "type": "OffStreetParking",
    "name": {
      "value": "2",
      "type": "Property"
    },
    "capacity": {
        "value": 50,
        "type": "Property"
      },
      "occupancy": {
        "value": 20,
        "type": "Property"
      },
      "month":{
        "value": 0,
        "type": "Property"
      },
      "hour": {
        "value": 0,
        "type": "Property"
      },
      "weekday": {
        "value": 0,
        "type": "Property"
      },
      "dateObserved": {
        "value": 0,
        "type": "Property"
      },
      "predictionValue": {
        "value": 0,
        "type": "Property"
      },
      @context: "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.6.jsonld"
}
```

Subscription:
```
{
  "description": "Notify me changes in MalagaParking",
  "type": "Subscription",
  "entities": [{"type": "OffStreetParking"}],
  "watchedAttributes": ["month", "hour", "name", "weekday"],
  "notification": {
    "endpoint": {
      "uri": "http://spark-worker:9001",
      "accept": "application/json"
    }
  },
   "@context": "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.6.jsonld"
}
```

### With Airflow as orchestrator

Run airflow:

```
cd airflow
docker compose up -d
```


- Access http://localhost:5000 to access MLFlow client

- Access http://localhost:3000 to watch the web

- Access http://localhost:8081 to access the Spark Cluster UI client

- Access http://localhost:8080 to access the Airflow Web UI (user: airflow, password: airflow)


Run airflow flows from the web (first training then predicting)


### Without Airflow as orchestrator


Start training job
```
docker compose -f docker-compose.submit_train.yml up -d
```

Start prediction job
```
docker compose -f docker-compose.submit_predict.yml up -d
```

## Make a prediction

Update the `urn:ngsi-ld:MalagaParking:001`entity and Cosmos receives a notification and generates a new `predictionValue`:

```
curl -iX PATCH 'http://localhost:1026/ngsi-ld/v1/entities/urn:ngsi-ld:MalagaParking:001/attrs' \
-H 'Content-Type: application/json' \
-H 'Link: <https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.6.jsonld>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"' \
--data-raw '{
        "name": {
            "value": "2",
            "type": "Property"
        },
        "month":{
            "value": 1,
            "type": "Property"
        },
        "hour": {
            "value": 2,
            "type": "Property"
        },
        "weekday": {
            "value": 3,
            "type": "Property"
        }
}'

```

```
curl --location --request GET 'http://localhost:1026/ngsi-ld/v1/entities/urn:ngsi-ld:MalagaParking:001'
```