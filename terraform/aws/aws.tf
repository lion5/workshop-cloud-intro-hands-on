variable "region" {
  default = "eu-west-1"
}

variable availability_zone {
  type = "string"
  default = "eu-west-1"
}

data "aws_availability_zones" "available" {
}

resource "aws_security_group" "allow_db_access" {
  name = "allow_db_access"
  description = "Allow inbound DB traffic"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port = 3306
    protocol = "tcp"
    to_port = 3306
    cidr_blocks = ["0.0.0.0/0"]
  }
}

### Network ###
// https://github.com/terraform-aws-modules/terraform-aws-vpc
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "2.6.0"

  name                 = "cloud-schulung-vpc"
  cidr                 = "10.0.0.0/16"
  azs                  = data.aws_availability_zones.available.names
  public_subnets       = ["10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24"]
  database_subnets     = ["10.0.7.0/24", "10.0.8.0/24"]
  enable_dns_hostnames = true

  # For public access to RDS
  create_database_subnet_group           = true
  create_database_subnet_route_table     = true
  create_database_internet_gateway_route = true
  enable_dns_support   = true

  tags = {
    "kubernetes.io/cluster/cloud-schulung-terraform" = "shared"
  }
}

### IAM ###
resource "aws_iam_role" "lambda_s3_rekognition" {
  name = "lambda_s3_rekognition"
  assume_role_policy = "${data.aws_iam_policy_document.role_assume_role_policy.json}"
}
output "iam_role_arn" {
  value = "${aws_iam_role.lambda_s3_rekognition.arn}"
}

data "aws_iam_policy_document" "role_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

# Define policy ARNs as list
variable "iam_policy_arn" {
  description = "IAM Policy to be attached to role"
  type = "list"
  default = ["arn:aws:iam::aws:policy/AmazonS3FullAccess","arn:aws:iam::aws:policy/AmazonRekognitionFullAccess", "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"]
}

# Then parse through the list using count
resource "aws_iam_role_policy_attachment" "role-policy-attachment" {
  role       = "${aws_iam_role.lambda_s3_rekognition.name}"
  count      = "${length(var.iam_policy_arn)}"
  policy_arn = "${var.iam_policy_arn[count.index]}"
}

### S3 Bucket ###
resource "aws_s3_bucket" "cloud-schulung-production" {
  bucket = "cloud-schulung-production"
  acl    = "private"
  region = "${var.region}"
  #force_destroy = true
}
output "bucket_prod_arn" {
  value = "${aws_s3_bucket.cloud-schulung-production.arn}"
}

resource "aws_s3_bucket" "cloud-schulung-stage" {
  bucket = "cloud-schulung-stage"
  acl    = "private"
  region = "${var.region}"
  #force_destroy = true
}
output "bucket_staging_arn" {
  value = "${aws_s3_bucket.cloud-schulung-stage.arn}"
}

# Bucket used by all training participants in the EKS Hands On
resource "aws_s3_bucket" "cloud-schulung-shared" {
  bucket = "cloud-schulung-shared"
  acl    = "private"
  region = "${var.region}"
  #force_destroy = true
}

### Lambda Layer (Pillow) ###
resource "aws_lambda_layer_version" "lambda_layer" {
  filename   = "../../functions/aws_image_scale/pillow_layer.zip"
  layer_name = "lambda_pillow"
  description = "Layer providing Pillow (Python Imaging Library Fork)"
  compatible_runtimes = ["python3.7"]
}
output "pillow_layer_arn" {
  value="${aws_lambda_layer_version.lambda_layer.arn}"
}

### MySQL Database ###
resource "aws_db_instance" "cloud-schulung-mysql" {
  identifier           = "cloud-schulung-mysql"
  allocated_storage    = 5
  db_subnet_group_name = module.vpc.database_subnet_group
  storage_type         = "gp2"
  engine               = "mysql"
  engine_version       = "5.7"
  instance_class       = "db.t2.micro"
  username             = "root"
  password             = "myRootPassword1234"
  parameter_group_name = "default.mysql5.7"
  skip_final_snapshot  = true
  publicly_accessible = true
  vpc_security_group_ids = ["${aws_security_group.allow_db_access.id}"]
}
output "rds_endpoint" {
  value = "${aws_db_instance.cloud-schulung-mysql.endpoint}"
}
# Info: https://www.terraform.io/docs/providers/mysql/index.html
provider "mysql" {
  endpoint = "${aws_db_instance.cloud-schulung-mysql.endpoint}"
  username = "${aws_db_instance.cloud-schulung-mysql.username}"
  password = "${aws_db_instance.cloud-schulung-mysql.password}"
}

resource "mysql_database" "production"{
  name = "production"
}

resource "mysql_database" "staging" {
  name = "staging"
}

resource "mysql_database" "group1" {
  name = "group01"
}

resource "mysql_database" "group2" {
  name = "group02"
}

resource "mysql_database" "group3" {
  name = "group03"
}

resource "mysql_database" "group4" {
  name = "group04"
}

resource "mysql_database" "group5" {
  name = "group05"
}

resource "mysql_database" "group6" {
  name = "group06"
}

resource "mysql_database" "group7" {
  name = "group07"
}

### EKS Cluster ###
// https://github.com/terraform-aws-modules/terraform-aws-eks
module "my-cluster" {
  source = "terraform-aws-modules/eks/aws"
  cluster_name = "cloud-schulung-terraform"
  subnets = module.vpc.public_subnets
  vpc_id = module.vpc.vpc_id
  map_users = var.map_users
  workers_additional_policies = ["arn:aws:iam::aws:policy/AmazonS3FullAccess", "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"]
  worker_groups = [
    {
      instance_type = "t3.medium"
      public_ip            = true
      asg_desired_capacity = 3
      asg_min_size = 2
      asg_max_size = 3

      tags = [
        {
          key = "Name"
          value = "cloud-schulung-terraform-wg"
          propagate_at_launch = true
        }]
    }
  ]
  tags = {
    Name = "cloud-schulung-terraform-cluster"
  }
}

resource "aws_ecr_repository" "bookshelf_registry" {
  name = "bookshelf"
}

resource "aws_ecr_lifecycle_policy" "bookshelf-ecr-lifecycle-untagged" {
  repository = "${aws_ecr_repository.bookshelf_registry.name}"
  policy = <<EOF
{
    "rules": [
        {
            "rulePriority": 1,
            "description": "Expire untagged images older than 1 day",
            "selection": {
                "tagStatus": "untagged",
                "countType": "sinceImagePushed",
                "countUnit": "days",
                "countNumber": 1
            },
            "action": {
                "type": "expire"
            }
        }
    ]
}
EOF
}
output "bookshelf_repository" {
  value = "${aws_ecr_repository.bookshelf_registry.repository_url}"
}

