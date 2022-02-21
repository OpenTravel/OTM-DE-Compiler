module org.opentravel.schemacompilerextota2 {
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires java.xml;

    exports org.opentravel.schemacompiler.extension;
    exports org.opentravel.schemacompiler.ota2;

    provides org.opentravel.schemacompiler.extension.CompilerExtensionProvider
        with org.opentravel.schemacompiler.ota2.OTA2CompilerExtensionProvider;

}
