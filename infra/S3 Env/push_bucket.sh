#!/bin/bash

# Variables
BUCKET_NAME="onlynotes-env"
S3_FOLDER=""
LOCAL_FOLDER="./onlynotes-env"

function confirm {
    read -p "Are you sure you want to push files to s3://$BUCKET_NAME/$S3_FOLDER? (yes/no): " response
    if [[ ! "$response" =~ ^[Yy][Ee]?([Ss]|[Ss])?$ ]]; then
        echo "Operation canceled."
        exit 1
    fi
}

# Call the confirmation function
confirm

# Upload files
aws s3 sync "$LOCAL_FOLDER" "s3://$BUCKET_NAME/$S3_FOLDER"

echo "Files uploaded from $LOCAL_FOLDER to s3://$BUCKET_NAME/$S3_FOLDER"
