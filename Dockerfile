FROM jetty:jre8

ADD target/bookshelf-5-1.0-SNAPSHOT/ /var/lib/jetty/webapps/ROOT

EXPOSE 8080
