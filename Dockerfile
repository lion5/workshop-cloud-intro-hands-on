# mvn clean install
# docker build --no-cache -t bookshelf:local -f Dockerfile .
# docker run -d --rm -p 8080:8080 bookshelf:local
# for Elastic Beanstalk deployment
#
FROM jetty:jre8

ADD target/bookshelf-5-1.0-SNAPSHOT/ /var/lib/jetty/webapps/ROOT

EXPOSE 8080
