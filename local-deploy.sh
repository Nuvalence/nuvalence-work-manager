#!/bin/bash

set -e

cd $(dirname $0)

export PROJECT_ID=$(gcloud config get-value project)
export REPO=$(basename $(pwd))
export TAG=$USER
export SERVICE_NAME="${USER}-${REPO}-service"
export IMAGE="us.gcr.io/$PROJECT_ID/$REPO/$SERVICE_NAME:$TAG"
export REGION='us-east4'

# ./gradlew clean build jib --image $IMAGE
./gradlew build jib --image $IMAGE

# cd infrastructure
# terraform apply -var project_id=$PROJECT_ID -var region=$REGION -var service_name=$SERVICE_NAME -var image=$IMAGE

gcloud run deploy $SERVICE_NAME \
          --image $IMAGE \
          --region $REGION

