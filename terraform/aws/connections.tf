# terraform init -backend=true
# für ci: -input=false
provider "aws" {
  region = "eu-west-1"
}

terraform {
  required_version = "= 0.12.12"
  backend "s3" {
    bucket = "cloud-schulung-backend"
    key    = "terraform.tfstate"
    region = "eu-west-1"
  }
}