curl -iX POST 'http://orion:1026/ngsi-ld/v1/entities/' \
-H 'Content-Type: application/json' \
-H 'Link: <https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.6.jsonld>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"' \
--data-raw '{
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
      }
}'