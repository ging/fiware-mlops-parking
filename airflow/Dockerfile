FROM apache/airflow:2.2.5-python3.7
USER root

ENV BASE_URL=https://archive.apache.org/dist/spark/
ENV SPARK_VERSION=2.4.5
ENV HADOOP_VERSION=2.7

# Install OpenJDK-8
RUN apt update && \
    apt-get install -y openjdk-11-jdk && \
    apt-get clean;

# Set JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64/
RUN export JAVA_HOME

RUN apt-get install -y wget

RUN wget ${BASE_URL}/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && tar -xvzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/. \
      && rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

COPY ./requirements.txt /.
RUN pip install -r /requirements.txt

USER airflow

COPY --chown=airflow:root ./dags /opt/airflow/dags