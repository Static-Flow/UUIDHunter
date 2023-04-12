package main.java.com.staticflow;

import burp.api.montoya.http.HttpService;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueDefinition;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Custom AuditIssue that represents when this Extension discovers a V1 UUID in use
 */
public class UuidHunterAuditIssue implements AuditIssue {
    //The Request/Response which contains a V1 UUID
    HttpRequestResponse issueRequestResponse;
    //The V1 UUID found in the Request/Response
    UUID uuid;
    //Confidence of this issue
    AuditIssueConfidence confidence;
    //The Severity of this issue
    AuditIssueSeverity severity;

    private static final String NAME = "Potential V1 UUID In Use";
    private static final String REMEDIATION = "V1 UUID's can be guessed by attackers bruteforcing the creation time of other UUIDs." +
            "<br>Modern UUID version such as v4 use a psuedo random generator should be used instead to ensure attackers cannot easily bruteforce valid UUIDs.";
    private static final String BACKGROUND = "UUID v1 is generated by using a combination the host computers MAC address and the current date and time.<br>" +
            "With the information obtained from analyzing a known UUID, it is often possible to forge future v1 UUIDs created by the system, if you know the approximate time they were created.";

    /**
     * Constructor for this Extension's Custom {@link AuditIssue}
     * @param uuid The V1 UUID found within the Request/Response
     * @param issueRequestResponse The {@link HttpRequestResponse} that contains a V1 UUID
     * @param confidence The {@link AuditIssueConfidence Confidence} that this issue is valid
     * @param severity The {@link AuditIssueSeverity Severity} of this issue
     */
    public UuidHunterAuditIssue(UUID uuid, HttpRequestResponse issueRequestResponse, AuditIssueConfidence confidence, AuditIssueSeverity severity) {
        this.issueRequestResponse = issueRequestResponse;
        this.uuid = uuid;
        this.confidence = confidence;
        this.severity = severity;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String detail() {
        return "It appears this request or response contains a V1 UUID with the following info:" +
                "<br>" +
                "UUID Time: " + Utils.getUuidTime(this.uuid) +
                "<br>" +
                "UUID timestamp: " + this.uuid.timestamp() +
                "<br>" +
                "UUID MAC: " + Utils.getUuidMac(this.uuid) +
                "<br>" +
                "UUID Clock Sequence: " + this.uuid.clockSequence();

    }

    @Override
    public String remediation() {
        return REMEDIATION;
    }

    @Override
    public HttpService httpService() {
        return this.issueRequestResponse.httpService();
    }

    @Override
    public String baseUrl() {
        return this.issueRequestResponse.url();
    }

    @Override
    public AuditIssueSeverity severity() {
        return this.severity;
    }

    @Override
    public AuditIssueConfidence confidence() {
        return this.confidence;
    }

    @Override
    public List<HttpRequestResponse> requestResponses() {
        return Collections.singletonList(this.issueRequestResponse);
    }

    @Override
    public AuditIssueDefinition definition() {
        return AuditIssueDefinition.auditIssueDefinition(NAME,BACKGROUND, REMEDIATION,AuditIssueSeverity.MEDIUM);
    }

}
