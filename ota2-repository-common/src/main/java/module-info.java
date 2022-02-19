module org.opentravel.ota2repositorycommon {

    requires transitive org.opentravel.schemacompiler;
    requires spring.beans;
    requires spring.context;
    requires spring.jms;
    requires commons.logging;
    requires lucene.core;
    requires lucene.analyzers.common;
    requires lucene.queryparser;
    requires svnkit;
    requires java.naming;
    requires velocity;
    requires javax.mail;
    requires java.management;
    requires org.apache.logging.log4j;
    requires com.sun.xml.bind;
    requires jakarta.activation;
    requires commons.codec;

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

}
