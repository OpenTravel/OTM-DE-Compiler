open module org.opentravel.schemacompiler {
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires transitive com.fasterxml.jackson.databind;
    requires gson;
    requires commons.text;
    requires commons.collections;
    requires transitive org.apache.httpcomponents.httpclient;
    requires commons.lang3;
    requires transitive org.apache.httpcomponents.httpcore;
    requires transitive org.apache.httpcomponents.httpmime;
    requires org.opentravel.schemacompilerextota2;
    requires org.apache.commons.codec;
    requires commons.lang;
    requires commons.beanutils;
    requires velocity;
    requires commons.validator;
    requires org.apache.logging.log4j;
    requires com.sun.xml.bind;
    requires transitive java.xml;
    requires java.xml.bind;
    requires spring.jcl;

    exports org.opentravel.schemacompiler.codegen;
    exports org.opentravel.schemacompiler.codegen.example;
    exports org.opentravel.schemacompiler.codegen.html;
    exports org.opentravel.schemacompiler.codegen.html.builders;
    exports org.opentravel.schemacompiler.codegen.html.markup;
    exports org.opentravel.schemacompiler.codegen.html.writers;
    exports org.opentravel.schemacompiler.codegen.html.writers.info;
    exports org.opentravel.schemacompiler.codegen.impl;
    exports org.opentravel.schemacompiler.codegen.json;
    exports org.opentravel.schemacompiler.codegen.json.facet;
    exports org.opentravel.schemacompiler.codegen.json.model;
    exports org.opentravel.schemacompiler.codegen.openapi;
    exports org.opentravel.schemacompiler.codegen.openapi.model;
    exports org.opentravel.schemacompiler.codegen.swagger;
    exports org.opentravel.schemacompiler.codegen.swagger.model;
    exports org.opentravel.schemacompiler.codegen.util;
    exports org.opentravel.schemacompiler.codegen.wsdl;
    exports org.opentravel.schemacompiler.codegen.xsd;
    exports org.opentravel.schemacompiler.codegen.xsd.facet;
    exports org.opentravel.schemacompiler.diff;
    exports org.opentravel.schemacompiler.diff.impl;
    exports org.opentravel.schemacompiler.event;
    exports org.opentravel.schemacompiler.ic;
    exports org.opentravel.schemacompiler.ioc;
    exports org.opentravel.schemacompiler.loader;
    exports org.opentravel.schemacompiler.loader.impl;
    exports org.opentravel.schemacompiler.model;
    exports org.opentravel.schemacompiler.repository;
    exports org.opentravel.schemacompiler.repository.impl;
    exports org.opentravel.schemacompiler.saver;
    exports org.opentravel.schemacompiler.saver.impl;
    exports org.opentravel.schemacompiler.security;
    exports org.opentravel.schemacompiler.security.impl;
    exports org.opentravel.schemacompiler.task;
    exports org.opentravel.schemacompiler.transform;
    exports org.opentravel.schemacompiler.transform.jaxb14_2tl;
    exports org.opentravel.schemacompiler.transform.jaxb15_2tl;
    exports org.opentravel.schemacompiler.transform.jaxb16_2tl;
    exports org.opentravel.schemacompiler.transform.jaxb2xsd;
    exports org.opentravel.schemacompiler.transform.symbols;
    exports org.opentravel.schemacompiler.transform.tl2jaxb;
    exports org.opentravel.schemacompiler.transform.tl2jaxb16;
    exports org.opentravel.schemacompiler.transform.util;
    exports org.opentravel.schemacompiler.util;
    exports org.opentravel.schemacompiler.validate;
    exports org.opentravel.schemacompiler.validate.assembly;
    exports org.opentravel.schemacompiler.validate.base;
    exports org.opentravel.schemacompiler.validate.compile;
    exports org.opentravel.schemacompiler.validate.impl;
    exports org.opentravel.schemacompiler.validate.save;
    exports org.opentravel.schemacompiler.version;
    exports org.opentravel.schemacompiler.version.handlers;
    exports org.opentravel.schemacompiler.visitor;
    exports org.opentravel.schemacompiler.xml;
    exports org.opentravel.ns.ota2.appinfo_v01_00;
    exports org.opentravel.ns.ota2.assembly_v01_00;
    exports org.opentravel.ns.ota2.librarycatalog_v01_00;
    exports org.opentravel.ns.ota2.librarymodel_v01_04;
    exports org.opentravel.ns.ota2.librarymodel_v01_05;
    exports org.opentravel.ns.ota2.librarymodel_v01_06;
    exports org.opentravel.ns.ota2.project_v01_00;
    exports org.opentravel.ns.ota2.release_v01_00;
    exports org.opentravel.ns.ota2.repositoryinfo_v01_00;
    exports org.w3._2001.xmlschema;
    exports org.xmlsoap.schemas.wsdl;
    exports org.xmlsoap.schemas.wsdl.soap;

    uses org.opentravel.schemacompiler.extension.CompilerExtensionProvider;

}
