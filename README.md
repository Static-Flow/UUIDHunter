# UUIDHunter
This Extension provides a Passive and Active Scan Check that detects V1 UUIDs and attempts to find other potentially valid ones.

# How to Use (Passive Check)
Do nothing :) UUIDHunter will watch the following sinks for V1 UUIDs
* Request Header Values
* Response Header Values
* Request Parameter Values
* Response Body Contents

# How to Use (Active Check)
This Extension provides a custom Active Scan check which performs the following steps:
* for a given insertion point, determine if it is a valid V1 UUID
* If so, psarse the UUID for it's timestamp and build an interval a user configurable amount of seconds before and after it
* In 100 nanosecond increments, build a new UUID with the next timestamp, submit the request and check the response code
* If the response is a 2XX, there is a FIRM likelyhood of an issue
* If the response is a 3XX, there is a TENTATIVE likelyhood of an issue 

# Active San Configuration
To configure how many seconds before and after a V1 UUID timestamp to build candidates for, follow the steps below:
* Open the "Settings" Window in the top right
* Expand the "Custom Extension Settings" dropdown
* Click the "UuidHunter" option
* In the textbox enter a number between 1-99
* Click Save
