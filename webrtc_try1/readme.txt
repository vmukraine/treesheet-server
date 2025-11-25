I will generate three files to create a complete WebRTC communication loop:

signaling_server.js: A Node.js WebSocket server to handshake the connection (exchange SDP and ICE candidates) between Java and the Browser.

WebRTCJavaClient.java: A Java application using the webrtc-java library (wrapper for Google's native WebRTC) to connect, establish a Data Channel, and send/receive lists of strings.

index.html: The Web client with a modern UI to connect to the server, display received lists, and send lists to the Java client.

Prerequisites (Java)
For the Java code to run, you will need a WebRTC wrapper library. This code is written for dev.onvoid.webrtc (a popular, modern Java wrapper). You would typically add these dependencies to your Maven/Gradle project:

dev.onvoid.webrtc:webrtc-java:0.6.0

org.java-websocket:Java-WebSocket:1.5.4 (for signaling)

1. The Signaling Server (Node.js)
Save this, then run npm install ws and node signaling_server.js.
Signaling Server
25 лист., 00:03

2. The Java Client
This client connects to the signaling server, establishes a peer connection, handles the Data Channel, and serializes the list of strings using JSON.
Java WebRTC Client
25 лист., 00:03

Відкрити

3. The Web Client
Open this file in your browser. It includes the logic to initiate the connection (Offer) to the Java client.
Web Client
25 лист., 00:03

Відкрити

How to Run
Install/Run Signaling Server:

npm install ws

node signaling_server.js

Run Java Client:

Ensure the dev.onvoid.webrtc dependencies (and native libs) are in your classpath.

Run WebRTCJavaClient.java. It will connect to the WebSocket and wait.

Run Web Client:

Open index.html in a browser.

Click "Connect (Send Offer)".

The Java terminal should show "Received Offer", then "Sent Answer".

The Web status should turn Green (Connected).

Transfer Data:

Web to Java: Add strings in the UI and click "Send List". Check the Java console output.

Java to Web: Type strings (e.g., one,two,three) in the Java console and hit Enter. Check the Web UI log.