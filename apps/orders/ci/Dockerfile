FROM node:20-alpine AS base

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

COPY ci/wait-for-it.sh ./

# Install dependencies
RUN npm install

RUN apk add --no-cache bash

# Copy the rest of the application code to the working directory
COPY . .

RUN chmod +x wait-for-it.sh

# Expose the port 
EXPOSE 3000

# Run the integration tests
CMD ./wait-for-it.sh mongo:27017 -- npm run test:integration