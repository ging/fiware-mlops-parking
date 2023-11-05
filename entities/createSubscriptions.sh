curl -L -X POST 'http://orion:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
--data-raw '{
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
}'