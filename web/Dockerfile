# syntax=docker/dockerfile:1

FROM node:12.18.1
ENV NODE_ENV=production
ENV MONGO_URI=mongodb://mongodb-0.mongodb-svc.tfm.svc.cluster.local:27017,mongodb-1.mongodb-svc.tfm.svc.cluster.local:27017,mongodb-2.mongodb-svc.tfm.svc.cluster.local:27017/sth_test
ENV URL_CB=http://orion:1026/ngsi-ld/v1/entities/urn:ngsi-ld:ReqMalagaParkingPrediction1/attrs
WORKDIR /app
COPY ["package.json", "package-lock.json*", "./"]
RUN npm install --production
COPY . .
CMD [ "node", "app.js" ]
