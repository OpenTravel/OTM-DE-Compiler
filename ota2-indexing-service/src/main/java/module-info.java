module org.opentravel.ota2indexingservice {
    requires commons.logging;
    requires lucene.analyzers.common;
    requires lucene.core;
    requires org.opentravel.ota2repositorycommon;
    requires spring.context;
    requires spring.jms;
    requires java.management;
    requires jakarta.jms.api;
    requires activemq.client;
    requires commons.lang3;
    requires java.rmi;
    requires spring.beans;
    requires org.apache.logging.log4j;
    requires java.xml.bind;
}
