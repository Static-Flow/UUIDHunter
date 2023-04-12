package main.java.com.staticflow;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;

/**
 * This extension builds off of the great tool by intruder-io <a href="URL#https://github.com/intruder-io/guidtool ">guidtool</a> to provide a passive and
 * active scan check for V1 UUIDs which are time based. <br> The passive scanner check monitors the following points within Requests/Responses for potential
 * V1 UUIDs: <br>
 * <ul>
 * <li>Request Headers</li>
 * <li>Response Headers</li>
 * <li>Request Parameters</li>
 * <li>Response Body</li>
 * </ul><br>
 * The Active scanner check first performs the following actions:<br>
 * <ul>
 *     <li>determine if insertion point is a valid V1 UUID</li>
 *     <li>Build a list of similar UUIDs 1 second before and after the initial UUID in 100 nanosecond increments</li>
 *     <li>send a request for each UUID in the list</li>
 *     <li>If the response code is 200-299 create a {@link AuditIssueConfidence#FIRM} AuditIssue</li>
 *     <li>If the response code is 300-399 create a {@link AuditIssueConfidence#TENTATIVE} AuditIssue</li>
 * </ul>
 */
public class UuidHunter implements BurpExtension, ExtensionUnloadingHandler {

    public static final String EXTENSION_NAME = "UuidHunter";

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        //Set the extension name
        montoyaApi.extension().setName(EXTENSION_NAME);
        //Save a reference to the Burp Suite supplied callbacks and helper methods to the internal singleton state object
        ExtensionState.getInstance().setCallbacks(montoyaApi);
        //Register this class to handle Extension unloading
        montoyaApi.extension().registerUnloadingHandler(this);
        //Register the custom Scanner Check
        montoyaApi.scanner().registerScanCheck(new UuidHunterScannerCheck());
        //Inject this Extension's custom menu into the global menu window
        try {
            BurpGuiControl.addMenuToSettingsTree(EXTENSION_NAME,new UuidHunterGui());
        } catch (Exception e) {
            montoyaApi.logging().logToError(e.toString());
        }
    }

    /**
     * Remove this Extension's custom menu from the global menu window
     */
    @Override
    public void extensionUnloaded() {
        BurpGuiControl.removeCustomSettingsTree(EXTENSION_NAME);
    }
}
