module org.opentravel.schemacompilerextota2 {
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires java.xml;

    exports org.opentravel.schemacompiler.extension;
    exports org.opentravel.schemacompiler.ota2;

}
