terraform {
  backend "s3" {
    bucket = "onlynotes-tf-state-management"
    key    = "state/development/terraform.tfstate"
    region = "ap-southeast-1"
    encrypt = true

    dynamodb_table = "onlynotes_tf-state-lock-development"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}


# Configure the AWS Provider
provider "aws" {
  region = "ap-southeast-1"
}
