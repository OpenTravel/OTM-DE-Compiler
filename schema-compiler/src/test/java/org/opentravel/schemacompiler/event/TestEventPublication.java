package org.opentravel.schemacompiler.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentravel.schemacompiler.event.DefaultModelEventFilter;
import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.OwnershipEvent;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Test cases that validate the publication framework for events dispatched through the TLModel.
 * 
 * @author S. Livezey
 */
public class TestEventPublication extends AbstractModelEventTests {

    @Test
    public void testPublishEvent_sameEvent_sameSourceObject() throws Exception {
        CapturingEventListener<ValueChangeEvent<TLLibrary, String>, TLLibrary> listener = new CapturingEventListener<ValueChangeEvent<TLLibrary, String>, TLLibrary>(
                ValueChangeEvent.class, TLLibrary.class);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        String oldComments = library.getComments();
        String newComments = "library_1_p2:newComments";

        try {
            testModel.addListener(listener);
            library.setComments(newComments);
            assertEquals(1, listener.getCapturedEvents().size());
            assertEquals(ModelEventType.COMMENTS_MODIFIED, listener.getCapturedEvents().get(0)
                    .getType());
            assertEquals(library, listener.getCapturedEvents().get(0).getSource());
            assertEquals(oldComments, listener.getCapturedEvents().get(0).getOldValue());
            assertEquals(newComments, listener.getCapturedEvents().get(0).getNewValue());

        } finally {
            testModel.removeListener(listener);
            library.setComments(oldComments);
        }
    }

    @Test
    public void testPublishEvent_sameEvent_subclassSourceObject() throws Exception {
        CapturingEventListener<ValueChangeEvent<AbstractLibrary, String>, AbstractLibrary> listener = new CapturingEventListener<ValueChangeEvent<AbstractLibrary, String>, AbstractLibrary>(
                ValueChangeEvent.class, AbstractLibrary.class);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        String oldComments = library.getComments();
        String newComments = "library_1_p2:newComments";

        try {
            testModel.addListener(listener);
            library.setComments(newComments);
            assertEquals(1, listener.getCapturedEvents().size());
            assertEquals(ModelEventType.COMMENTS_MODIFIED, listener.getCapturedEvents().get(0)
                    .getType());
            assertEquals(library, listener.getCapturedEvents().get(0).getSource());
            assertEquals(oldComments, listener.getCapturedEvents().get(0).getOldValue());
            assertEquals(newComments, listener.getCapturedEvents().get(0).getNewValue());

        } finally {
            testModel.removeListener(listener);
            library.setComments(oldComments);
        }
    }

    @Test
    public void testPublishEvent_subclassEvent_sameSourceObject() throws Exception {
        CapturingEventListener<ModelEvent<TLLibrary>, TLLibrary> listener = new CapturingEventListener<ModelEvent<TLLibrary>, TLLibrary>(
                ModelEvent.class, TLLibrary.class);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        String oldComments = library.getComments();
        String newComments = "library_1_p2:newComments";

        try {
            testModel.addListener(listener);
            library.setComments(newComments);
            assertEquals(1, listener.getCapturedEvents().size());
            assertEquals(ModelEventType.COMMENTS_MODIFIED, listener.getCapturedEvents().get(0)
                    .getType());
            assertEquals(library, listener.getCapturedEvents().get(0).getSource());

        } finally {
            testModel.removeListener(listener);
            library.setComments(oldComments);
        }
    }

    @Test
    public void testPublishEvent_differentEvent_sameSourceObject() throws Exception {
        CapturingEventListener<OwnershipEvent<TLLibrary, String>, TLLibrary> listener = new CapturingEventListener<OwnershipEvent<TLLibrary, String>, TLLibrary>(
                OwnershipEvent.class, String.class);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        String oldComments = library.getComments();
        String newComments = "library_1_p2:newComments";

        try {
            testModel.addListener(listener);
            library.setComments(newComments);
            assertEquals(0, listener.getCapturedEvents().size()); // listener does not respond to
                                                                  // this event

        } finally {
            testModel.removeListener(listener);
            library.setComments(oldComments);
        }
    }

    @Test
    public void testPublishEvent_sameEvent_differentSourceObject() throws Exception {
        CapturingEventListener<ValueChangeEvent<TLSimple, String>, TLSimple> listener = new CapturingEventListener<ValueChangeEvent<TLSimple, String>, TLSimple>(
                ValueChangeEvent.class, TLSimple.class);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        String oldComments = library.getComments();
        String newComments = "library_1_p2:newComments";

        try {
            testModel.addListener(listener);
            library.setComments(newComments);
            assertEquals(0, listener.getCapturedEvents().size()); // listener does not respond to
                                                                  // this event

        } finally {
            testModel.removeListener(listener);
            library.setComments(oldComments);
        }
    }

    @Test
    public void testEventFilter_constrainEventType() throws Exception {
        CapturingEventListener<ValueChangeEvent<TLSimple, String>, TLSimple> listener = new CapturingEventListener<ValueChangeEvent<TLSimple, String>, TLSimple>(
                ValueChangeEvent.class, TLSimple.class);
        DefaultModelEventFilter<ValueChangeEvent<TLSimple, String>, TLSimple> filteredListener = new DefaultModelEventFilter<ValueChangeEvent<TLSimple, String>, TLSimple>(
                listener);
        TLLibrary library = (TLLibrary) testModel.getLibrary(PACKAGE_1_NAMESPACE, "library_1_p1");
        TLSimple simpleType = library.getSimpleType("TestString");
        String oldPattern = simpleType.getPattern();
        String newPattern = "newPattern";
        int oldMaxLength = simpleType.getMaxLength();
        int newMaxLength = 1000;

        try {
            filteredListener.setAllowableEventTypes(ModelEventType.PATTERN_MODIFIED);
            testModel.addListener(filteredListener);
            simpleType.setPattern(newPattern);
            simpleType.setMaxLength(newMaxLength);
            assertEquals(1, listener.getCapturedEvents().size()); // listener only responds to one
                                                                  // of the two events
            assertEquals(ModelEventType.PATTERN_MODIFIED, listener.getCapturedEvents().get(0)
                    .getType());
            assertEquals(simpleType, listener.getCapturedEvents().get(0).getSource());
            assertEquals(oldPattern, listener.getCapturedEvents().get(0).getOldValue());
            assertEquals(newPattern, listener.getCapturedEvents().get(0).getNewValue());

        } finally {
            testModel.removeListener(filteredListener);
            simpleType.setPattern(oldPattern);
            simpleType.setMaxLength(oldMaxLength);
        }
    }

    @Test
    public void testEventFilter_constrainSourceObjectType() throws Exception {
        CapturingEventListener<ValueChangeEvent<TLModelElement, String>, TLModelElement> listener = new CapturingEventListener<ValueChangeEvent<TLModelElement, String>, TLModelElement>(
                ValueChangeEvent.class, TLModelElement.class);
        DefaultModelEventFilter<ValueChangeEvent<TLModelElement, String>, TLModelElement> filteredListener = new DefaultModelEventFilter<ValueChangeEvent<TLModelElement, String>, TLModelElement>(
                listener);
        TLLibrary library1 = (TLLibrary) testModel.getLibrary(PACKAGE_1_NAMESPACE, "library_1_p1");
        TLLibrary library2 = (TLLibrary) testModel.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        TLSimple simpleType = library1.getSimpleType("TestString");
        TLBusinessObject businessObj = library2.getBusinessObjectType("SampleBusinessObject");
        String oldPattern = simpleType.getPattern();
        String newPattern = "newPattern";
        TLDocumentation oldDocumentation = businessObj.getDocumentation();
        try {
            filteredListener.setAllowableSourceObjectTypes(TLSimple.class);
            testModel.addListener(filteredListener);
            simpleType.setPattern(newPattern);
            businessObj.setDocumentation(null);
            assertEquals(1, listener.getCapturedEvents().size()); // listener only responds to one
                                                                  // of the two events
            assertEquals(ModelEventType.PATTERN_MODIFIED, listener.getCapturedEvents().get(0)
                    .getType());
            assertEquals(simpleType, listener.getCapturedEvents().get(0).getSource());
            assertEquals(oldPattern, listener.getCapturedEvents().get(0).getOldValue());
            assertEquals(newPattern, listener.getCapturedEvents().get(0).getNewValue());

        } finally {
            testModel.removeListener(filteredListener);
            simpleType.setPattern(oldPattern);
            businessObj.setDocumentation(oldDocumentation);
        }
    }

}
