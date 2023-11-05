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


sleep 10

curl --location --request GET 'http://localhost:1026/ngsi-ld/v1/entities/urn:ngsi-ld:MalagaParking:001'


# curl -iX PATCH 'http://localhost:1026/ngsi-ld/v1/entities/urn:ngsi-ld:MalagaParking:001/attrs/predictionValue' \
# -H 'Content-Type: application/json' \
# -H 'Link: <https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.6.jsonld>; rel="http://www.w3.org/ns/json-ld#context"; type="application/ld+json"' \
# --data-raw '{
#     "value": "5"
# }'

