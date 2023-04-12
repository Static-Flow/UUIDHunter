package main.java.com.staticflow;

import burp.api.montoya.MontoyaApi;

/**
 * This Singleton class holds all custom state for the extension and provides a central means of accessing it.
 */
public class ExtensionState {

    // Reference to this Singleton
    private static ExtensionState state = null;

    // Burp Suite callback and helper APIs
    private MontoyaApi callbacks;

    // Constructor which initializes any custom extension state
    private ExtensionState(){}

    // The range, in seconds, the active scan check should check before and after the provided UUID for other valid ones
    private int uuidScanRange;

    /**
     * Getter for the Singleton State object
     * @return reference to the Singleton State object
     */
    static ExtensionState getInstance() {
        if (state == null) {
            state = new ExtensionState();
            state.uuidScanRange = 1;
        }
        return state;
    }

    /**
     * Getter for the range, in seconds, used when generating potentially valid V1 UUIDs by the active scan check
     * @return the number of seconds before and after the initial UUID to generate candidate for
     */
    public int getUuidScanRange() {
        return uuidScanRange;
    }

    /**
     * Setter for the range, in seconds, used when generating potentially valid V1 UUIDs by the active scan check
     * @param uuidScanRange the number of seconds before and after the initial UUID to generate candidate for
     */
    public void setUuidScanRange(int uuidScanRange) {
        this.uuidScanRange = uuidScanRange;
    }

    /**
     * Setter for the Burp Suite callback and helper APIs
     * @param callbacks the callback and helper API reference given to this extension by Burp Suite
     */
    public void setCallbacks(MontoyaApi callbacks) {
        this.callbacks = callbacks;
    }


    /**
     * Getter for the Burp Suite callback and helper APIs
     * @return the Burp Suite callback and helper APIs
     */
    public MontoyaApi getCallbacks() {
        return callbacks;
    }
}