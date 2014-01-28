package org.opentravel.schemacompiler.providers;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import com.sun.jersey.core.provider.jaxb.AbstractJAXBElementProvider;
import com.sun.jersey.spi.inject.Injectable;

/**
 * Custom JAXB element provider that resolves the issue related to type bindings when unmarshalling
 * XML elements that belong to substitution groups.
 * 
 * @author S. Livezey
 */
public abstract class JAXBElementProvider extends AbstractJAXBElementProvider {

    private final Injectable<SAXParserFactory> spf;

    /**
     * Constructor that specifies the injectable SAX parser factory and the jax-rs provider
     * instances.
     * 
     * @param spf
     *            the injectable SAX parser factory
     * @param ps
     *            the jax-rs provider instances
     */
    public JAXBElementProvider(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
        super(ps);
        this.spf = spf;
    }

    /**
     * Constructor that specifies the injectable SAX parser factory and the jax-rs provider
     * instances, as well as the media type to be supported by the provider.
     * 
     * @param spf
     *            the injectable SAX parser factory
     * @param ps
     *            the jax-rs provider instances
     * @param mt
     *            the media type to be supported by this provider
     */
    public JAXBElementProvider(@Context Injectable<SAXParserFactory> spf, @Context Providers ps,
            @Context MediaType mt) {
        super(ps, mt);
        this.spf = spf;
    }

    /**
     * Concrete sub-class that provides read/write services for the 'application/xml' media type.
     */
    @Provider
    @Produces("application/xml")
    @Consumes("application/xml")
    public static final class App extends JAXBElementProvider {
        public App(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.APPLICATION_XML_TYPE);
        }
    }

    /**
     * Concrete sub-class that provides read/write services for the 'text/xml' media type.
     */
    @Provider
    @Produces("text/xml")
    @Consumes("text/xml")
    public static final class Text extends JAXBElementProvider {
        public Text(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.TEXT_XML_TYPE);
        }
    }

    /**
     * Concrete sub-class that provides read/write services for all '...+xml' media types.
     */
    @Provider
    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JAXBElementProvider {
        public General(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    /**
     * @see com.sun.jersey.core.provider.jaxb.AbstractJAXBElementProvider#readFrom(java.lang.Class,
     *      javax.ws.rs.core.MediaType, javax.xml.bind.Unmarshaller, java.io.InputStream)
     */
    protected final JAXBElement<?> readFrom(Class<?> type, MediaType mediaType, Unmarshaller u,
            InputStream entityStream) throws JAXBException {
        JAXBElement<?> jaxbElement = (JAXBElement<?>) u.unmarshal(getSAXSource(spf.getValue(),
                entityStream));
        Class<?> elementType = jaxbElement.getValue().getClass();

        if (!type.isAssignableFrom(elementType)) {
            throw new JAXBException("Incompatible JAXB object type: " + type.getName());
        }
        return jaxbElement;
    }

    /**
     * @see com.sun.jersey.core.provider.jaxb.AbstractJAXBElementProvider#writeTo(javax.xml.bind.JAXBElement,
     *      javax.ws.rs.core.MediaType, java.nio.charset.Charset, javax.xml.bind.Marshaller,
     *      java.io.OutputStream)
     */
    protected final void writeTo(JAXBElement<?> t, MediaType mediaType, Charset c, Marshaller m,
            OutputStream entityStream) throws JAXBException {
        m.marshal(t, entityStream);
    }

}
