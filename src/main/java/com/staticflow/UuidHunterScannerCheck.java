package main.java.com.staticflow;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.ConsolidationAction;
import burp.api.montoya.scanner.ScanCheck;
import burp.api.montoya.scanner.audit.insertionpoint.AuditInsertionPoint;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static burp.api.montoya.scanner.ConsolidationAction.KEEP_BOTH;
import static java.util.Collections.emptyList;
import static main.java.com.staticflow.Utils.parseUuidString;

/**
 * Custom {@link ScanCheck} for this Extension.<br>
 * The passive scanner check monitors the following points within Requests/Responses for potential V1 UUIDs: <br>
 *  * <ul>
 *  * <li>Request Headers</li>
 *  * <li>Response Headers</li>
 *  * <li>Request Parameters</li>
 *  * <li>Response Body</li>
 *  * </ul>
 *  <br>
 *  * The Active scanner check first performs the following actions:<br>
 *  * <ul>
 *  *     <li>determine if insertion point is a valid V1 UUID</li>
 *  *     <li>Build a list of similar UUIDs 1 second before and after the initial UUID in 100 nanosecond increments</li>
 *  *     <li>send a request for each UUID in the list</li>
 *  *     <li>If the response code is 200-299 create a {@link AuditIssueConfidence#FIRM} AuditIssue</li>
 *  *     <li>If the response code is 300-399 create a {@link AuditIssueConfidence#TENTATIVE} AuditIssue</li>
 *  * </ul>
 */

public class UuidHunterScannerCheck implements ScanCheck {
    @Override
    public AuditResult activeAudit(HttpRequestResponse httpRequestResponse, AuditInsertionPoint auditInsertionPoint) {
        List<AuditIssue> auditIssueList = new ArrayList<>();
        //ensure the base insertion point is a V1 UUID
        UUID uuid = parseUuidString(auditInsertionPoint.baseValue());
        if(uuid != null) {
            //generate a list of candidate V1 UUIDs in 100 nanosecond increments between the user defined range
           Iterable<UUID> possibleUuids = Utils.genUUIDs(uuid, 10000, ExtensionState.getInstance().getUuidScanRange());
           possibleUuids.forEach(possibleUuid -> {
               //Don't scan the original UUID
               if(!possibleUuid.toString().equals(uuid.toString())) {
                   //Update insertion point and send request
                   HttpRequest request = auditInsertionPoint.buildHttpRequestWithPayload(
                           ByteArray.byteArray(possibleUuid.toString())).withService(httpRequestResponse.httpService());
                   HttpRequestResponse response = ExtensionState.getInstance().getCallbacks().http().sendRequest(request);
                   short status = response.statusCode();
                   //if the status code is a 2XX it's a FIRM HIGH Issue
                   if (status > 199 && status < 300) {
                       auditIssueList.add(new UuidHunterAuditIssue(possibleUuid, response, AuditIssueConfidence.FIRM, AuditIssueSeverity.HIGH));
                   } else if (status >299 && status < 400) {
                       //If the status code is a 3XX it's a TENTATIVE MEDIUM Issue
                       auditIssueList.add(new UuidHunterAuditIssue(possibleUuid, response, AuditIssueConfidence.TENTATIVE,AuditIssueSeverity.MEDIUM));
                   }
               }
           });
           return AuditResult.auditResult(auditIssueList);
       } else {
           return AuditResult.auditResult(emptyList());
       }
    }

    @Override
    public AuditResult passiveAudit(HttpRequestResponse httpRequestResponse) {
        ArrayList<UUID> potentialUuidStrings = new ArrayList<>();
        /*
            This mess of stream().map().filter() calls collects the Request/Response header/parameter values then filters for only V1 UUIDs
         */
        potentialUuidStrings.addAll(httpRequestResponse.request().headers().stream().map(HttpHeader::value).map(value -> {
            try {
                return parseUuidString(value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }).filter(Objects::nonNull).toList());
        potentialUuidStrings.addAll(httpRequestResponse.response().headers().stream().map(HttpHeader::value).map(value -> {
            try {
                return parseUuidString(value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }).filter(Objects::nonNull).toList());
        potentialUuidStrings.addAll(httpRequestResponse.request().parameters().stream().map(ParsedHttpParameter::value).map(value -> {
            try {
                return parseUuidString(value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }).filter(Objects::nonNull).toList());

        List<AuditIssue> issues = potentialUuidStrings.stream().map(uuid -> {
            ExtensionState.getInstance().getCallbacks().logging().logToOutput(uuid.toString());
            return new UuidHunterAuditIssue(uuid, httpRequestResponse, AuditIssueConfidence.CERTAIN,AuditIssueSeverity.LOW);
        }).collect(Collectors.toList());

        //This extension also checks the response body for V1 UUID's
        if(httpRequestResponse.response().body().countMatches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}") > 0) {
            issues.add(new UuidHunterAuditIssue(null,httpRequestResponse,AuditIssueConfidence.CERTAIN,AuditIssueSeverity.LOW));
        }

        return issues.isEmpty() ? AuditResult.auditResult(emptyList()) : AuditResult.auditResult(issues);
    }

    @Override
    public ConsolidationAction consolidateIssues(AuditIssue newIssue, AuditIssue existingIssue) {
        return KEEP_BOTH;
    }

}
