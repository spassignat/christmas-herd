FROM tomcat:10.1-jdk21

# add jdbc driver in tomcat
ARG PG_JDBC_VERSION=42.7.4

ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/${PG_JDBC_VERSION}/postgresql-${PG_JDBC_VERSION}.jar \
    /usr/local/tomcat/lib/postgresql.jar

# conf for jndi
COPY conf/ /usr/local/tomcat/conf/

EXPOSE 8080
