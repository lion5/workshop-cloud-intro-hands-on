# Cloud Intro For Developers
## Credits
- Inspiration: [bookshelf](https://github.com/GoogleCloudPlatform/getting-started-java/tree/master/bookshelf) App by Google Tutorial as Basis
- Additional Cloud Function:
  - Triggered when image uploaded to bucket (for instance via bookshelf frontend)
  - Sanitizing of images
- Adaptation by [Lion5 GmbH](https://lion5.io)

## Automation
- .gitlab-ci.yml encompasses the following stages:
  - Build .war
  - Unit and Integration tests (exemplary)
  - Terraform ensures the infrastcture is ready for the deployment on AWS
  - Staging Deployment in Kubernetes Cluster Namespace (staging)
  - Production Deployment in Kubernetes Cluster Namespace (production)
  - Cloud Functions for staging and production

## CI Environment variables


## Local Execution
In general:
- `docker build -t <TAG NAME> -f local/DockerfileDB .`

For this application
- `docker build -t lion5/schulung-mysql -f local/DockerfileDB .`
- `docker run -d --rm --name schulung-mysql -p 3306:3306 lion5/schulung-mysql`
- `source local/set_env_vars.sh`
- `mvn -Plocal clean jetty:run-exploded`
- `mvn -Plocal clean jetty:run-exploded`
 
## AWS Lambda Function (FaaS)
The function deployment package must be created in advance. The Lambda function in the example needs the Python package pillow. Pillow is provided by a Lambda layer that is managed by AWS Terraform
  - ``aws lambda create-function --function-name resize_and_blur --zip-file fileb:<PATH_TO_DEPLOYMENT_PACKAGE> --handler lambda_function.lambda_handler --runtime python3.7 --role arn:aws:iam::153171767723:role/lambda_s3_rekognition --layers $(terraform output pillow_layer_arn) --region eu-west-1``

After the function is created, it must be given the permission to access objects in the target bucket.
  - ``aws lambda add-permission --function-name resize_and_blur --statement-id add_s3_bucket_policy --action "lambda:InvokeFunction" --principal s3.amazonaws.com --source-arn "arn:aws:s3:::cloud-schulung-test-irland" --region eu-west-1``
  
Finally the trigger for function execution must be configured via S3 API (S3 Put Bucket Notification Configuration)
  - ``aws s3api put-bucket-notification-configuration --bucket cloud-schulung-test-irland --notification-configuration file://functions/aws_image_scale/lambda_on_object_create.json --region eu-west-1``

## Selenium Test against Staging
  - Quirky Behavior with dependencies
  - Working: selenium 3.6.0 + guava 22.0 and guava-jdk8
 
## Automation
  - Cloud SQL currently managed with terraform BUT:Same instance name can not be used twice in the same week
  - Teardown still problematic
  - Should this even be managed with terraform? Danger of data loss?
