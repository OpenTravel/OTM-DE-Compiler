package org.opentravel.schemacompiler.codegen.example;

/**
 * Specifies the options to use when generating examples using the <code>ExampleNavigator</code>
 * component.
 * 
 * @author S. Livezey
 */
public class ExampleGeneratorOptions {

    public static enum DetailLevel {
        MINIMUM, MAXIMUM
    };

    private DetailLevel detailLevel = DetailLevel.MAXIMUM;
    private String exampleContext;
    private int maxRepeat = 3;
    private int maxRecursionDepth = 2;

    /**
     * Returns the amount of detail to include in the generated example.
     * 
     * @return DetailLevel
     */
    public DetailLevel getDetailLevel() {
        return detailLevel;
    }

    /**
     * Assigns the amount of detail to include in the generated example.
     * 
     * @param detailLevel
     *            the detail level to assign
     */
    public void setDetailLevel(DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
    }

    /**
     * Returns the exampleContext to use identify examples for simple data types.
     * 
     * @return String
     */
    public String getExampleContext() {
        return exampleContext;
    }

    /**
     * Assigns the exampleContext to use identify examples for simple data types.
     * 
     * @param exampleContext
     *            the exampleContext value to assign
     */
    public void setExampleContext(String context) {
        this.exampleContext = context;
    }

    /**
     * Returns the maximum number of time a repeating element should repeat.
     * 
     * @return int
     */
    public int getMaxRepeat() {
        return maxRepeat;
    }

    /**
     * Assigns the maximum number of time a repeating element should repeat.
     * 
     * @param maxRepeat
     *            the maximum repeat value to assign
     */
    public void setMaxRepeat(int maxRepeat) {
        this.maxRepeat = maxRepeat;
    }

    /**
     * Returns the maximum number of times that an element should be recursively visited within a
     * nested object structure.
     * 
     * @return int
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    /**
     * Assigns the maximum number of times that an element should be recursively visited within a
     * nested object structure.
     * 
     * @param maxRecursionDepth
     *            the maximum recursion depth to allow during example navigation
     */
    public void setMaxRecursionDepth(int maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }

}
