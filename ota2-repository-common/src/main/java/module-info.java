module org.opentravel.ota2repositorycommon {

    requires transitive org.opentravel.schemacompiler;
    requires spring.beans;
    requires spring.context;
    requires transitive spring.jms;
    requires transitive jakarta.jms.api;
    requires svnkit;
    requires java.naming;
    requires velocity;
    requires javax.mail;
    requires java.management;
    requires org.apache.logging.log4j;
    requires com.sun.xml.bind;
    requires jakarta.activation;
    requires org.apache.commons.codec;
    requires transitive org.apache.lucene.core;
    requires org.apache.lucene.queryparser;

    exports org.opentravel.repocommon.config;
    exports org.opentravel.repocommon.index;
    exports org.opentravel.repocommon.index.builder;
    exports org.opentravel.repocommon.jmx;
    exports org.opentravel.repocommon.lock;
    exports org.opentravel.repocommon.notification;
    exports org.opentravel.repocommon.repository;
    exports org.opentravel.repocommon.security;
    exports org.opentravel.repocommon.security.impl;
    exports org.opentravel.repocommon.subscription;
    exports org.opentravel.repocommon.util;
    exports org.opentravel.ns.ota2.repositoryinfoext_v01_00;
    exports org.opentravel.ns.ota2.security_v01_00;

    opens org.opentravel.ns.ota2.security_v01_00 to java.xml.bind;
    opens org.opentravel.ns.ota2.repositoryinfoext_v01_00 to java.xml.bind;

}
