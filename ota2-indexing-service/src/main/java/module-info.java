module org.opentravel.ota2indexingservice {
    requires org.opentravel.ota2repositorycommon;
    requires spring.context;
    requires spring.jms;
    requires java.management;
    requires jakarta.jms.api;
    requires commons.lang3;
    requires java.rmi;
    requires spring.beans;
    requires org.apache.logging.log4j;
    requires java.xml.bind;
    requires org.apache.lucene.core;
    requires commons.lang;

    opens org.opentravel.schemacompiler.index to spring.beans;
}
