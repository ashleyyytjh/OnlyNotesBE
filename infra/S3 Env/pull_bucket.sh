#!/bin/bash

# Variables
BUCKET_NAME="onlynotes-env"
S3_FOLDER=""
LOCAL_FOLDER="./onlynotes-env"

function confirm {
    echo "This will overwrite your local files"
    read -p "Are you sure you want to pull files to s3://$BUCKET_NAME/$S3_FOLDER? (yes/no): " response
    if [[ ! "$response" =~ ^[Yy][Ee]?([Ss]|[Ss])?$ ]]; then
        echo "Operation canceled."
        exit 1
    fi
}

# Call the confirmation function
confirm

# Ensure the local directory exists
mkdir -p "$LOCAL_FOLDER"

# Download files
aws s3 sync "s3://$BUCKET_NAME/$S3_FOLDER" "$LOCAL_FOLDER"

echo "Files downloaded from s3://$BUCKET_NAME/$S3_FOLDER to $LOCAL_FOLDER"
