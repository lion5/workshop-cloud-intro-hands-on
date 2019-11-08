import boto3
import uuid
from urllib.parse import unquote_plus
from PIL import Image, ImageFilter

# S3 Client to download and upload objects
s3_client = boto3.client('s3')
# Rekognition is needed to detect moderation labels (detect unsafe content)
rekognition = boto3.client('rekognition')


def lambda_handler(event, context):
    print(event)

    print("Request ID: {}".format(context.aws_request_id))
    print("Mem. limits(MB): {}".format(context.memory_limit_in_mb))

    # Just a single record will be processed (one file that has just been uploaded)
    for record in event['Records']:

        print("Event Name: {}".format(record['eventName']))
        print("Event Source: {}".format(record['eventSource']))
        print("Event Time: {}".format(record['eventTime']))

        bucket = record['s3']['bucket']['name']
        key = unquote_plus(record['s3']['object']['key'])
        if key[0:7] != 'rawimg-':
            return

        # We need to download the stored image to process it with Pillow (PIL fork: Python Imaging Library)
        download_path = '/tmp/{}{}'.format(uuid.uuid4(), key)
        # The processed image has to be stored in the file system before it can be uploaded to the bucket
        upload_path = '/tmp/resized-{}'.format(key)

        # Check Moderation Labels
        print("Detecting ModerationLabels in " + key)
        response = rekognition.detect_moderation_labels(Image={'S3Object': {'Bucket': bucket, 'Name': key}},
                                                        MinConfidence=75)

        blur = False
        print("Detected ModerationLabels in " + key)
        for label in response['ModerationLabels']:
            print(label['Name'] + ' : ' + str(label['Confidence']))
            print(label['ParentName'])
            if label['Name'] in ['Violence', 'Explicit Nudity', 'Suggestive']:
                blur = True

        # download file to temp directory
        s3_client.download_file(bucket, key, download_path)
        # call function that will resize (and maybe blur) the image, the processed image will be saved at upload_path
        resize_and_blur(download_path, upload_path, blur)

        # upload processed image the processed image needs a new key as we upload to the same bucket
        # -> remove "rawimg-"prefix to avoid recursion!
        new_object_key = key[7:]
        print('New object name {}'.format(new_object_key))
        s3_client.upload_file(upload_path, bucket, new_object_key, ExtraArgs={'ACL': 'public-read'})

        # Printing to CloudWatch
        print('Image saved at {}/{}'.format(
            bucket,
            new_object_key,
        ))


def resize_and_blur(original_image_path, processed_image_path, blur):
    print("Resizing image using Pillow")
    with Image.open(original_image_path) as image:
        # create 200x200 thumbnail
        image.thumbnail((200, 200), Image.ANTIALIAS)
        if blur:
            # Adds blur effect
            print("Also blurring image using Pillow")
            image = image.filter(ImageFilter.GaussianBlur(radius=6))
        image.save(processed_image_path)