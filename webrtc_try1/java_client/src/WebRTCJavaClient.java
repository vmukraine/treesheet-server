import dev.onvoid.webrtc.*;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.video.VideoTrack;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class WebRTCJavaClient {

    private static PeerConnectionFactory factory;
    private static PeerConnection peerConnection;
    private static RTCDataChannel dataChannel;
    private static WebSocketClient signalingSocket;
    private static final String SIGNALING_URL = "ws://localhost:8080";

    public static void main(String[] args) throws Exception {
        // 1. Initialize WebRTC Factory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder().createInitializationOptions());
        factory = new PeerConnectionFactory();

        // 2. Connect to Signaling Server
        connectSignaling();

        // 3. Simple CLI for user input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Java WebRTC Client Started.");
        System.out.println("Waiting for connection... (Ensure Web Client is open)");
        System.out.println("Type comma separated strings to send (e.g., 'apple,banana,cherry')");

        while (true) {
            String input = scanner.nextLine();
            if (dataChannel != null && dataChannel.getState() == RTCDataChannelState.OPEN) {
                // Parse input into a list, convert to JSON, send
                List<String> list = Arrays.asList(input.split(","));
                String jsonPayload = listToStringJson(list);
                
                ByteBuffer buffer = ByteBuffer.wrap(jsonPayload.getBytes(StandardCharsets.UTF_8));
                dataChannel.send(new RTCDataChannelBuffer(buffer, false));
                System.out.println("Sent: " + jsonPayload);
            } else {
                System.out.println("Error: Data Channel is not open yet.");
            }
        }
    }

    private static void connectSignaling() throws Exception {
        signalingSocket = new WebSocketClient(new URI(SIGNALING_URL)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                System.out.println("Connected to Signaling Server");
                createPeerConnection();
            }

            @Override
            public void onMessage(String message) {
                handleSignalingMessage(new JSONObject(message));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Signaling closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        signalingSocket.connect();
    }

    private static void createPeerConnection() {
        RTCConfiguration config = new RTCConfiguration();
        RTCIceServer iceServer = new RTCIceServer();
        iceServer.urls.add("stun:stun.l.google.com:19302"); // Public STUN
        config.iceServers.add(iceServer);

        peerConnection = factory.createPeerConnection(config, new PeerConnectionObserver() {
            @Override
            public void onIceCandidate(RTCIceCandidate candidate) {
                JSONObject json = new JSONObject();
                json.put("type", "candidate");
                json.put("candidate", candidate.sdp);
                json.put("sdpMid", candidate.sdpMid);
                json.put("sdpMLineIndex", candidate.sdpMLineIndex);
                signalingSocket.send(json.toString());
            }

            @Override
            public void onDataChannel(RTCDataChannel dc) {
                System.out.println("Data Channel received from remote!");
                setupDataChannel(dc);
            }

            @Override
            public void onIceConnectionChange(RTCIceConnectionState state) {
                System.out.println("ICE State: " + state);
            }
            
            // Unused interface methods
            @Override public void onSignalingChange(RTCSignalingState state) {}
            @Override public void onConnectionChange(RTCPeerConnectionState state) {}
            @Override public void onIceConnectionReceivingChange(boolean receiving) {}
            @Override public void onIceGatheringChange(RTCIceGatheringState state) {}
            @Override public void onIceCandidatesRemoved(RTCIceCandidate[] candidates) {}
            @Override public void onAddStream(MediaStream stream) {}
            @Override public void onRemoveStream(MediaStream stream) {}
            @Override public void onRenegotiationNeeded() {}
            @Override public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {}
            @Override public void onTrack(RtpTransceiver transceiver) {}
        });
    }

    private static void setupDataChannel(RTCDataChannel dc) {
        dataChannel = dc;
        dataChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long previousAmount) {}

            @Override
            public void onStateChange() {
                System.out.println("Data Channel State: " + dataChannel.getState());
            }

            @Override
            public void onMessage(RTCDataChannelBuffer buffer) {
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                String msg = new String(bytes, StandardCharsets.UTF_8);
                
                System.out.println("\n-----------------------------");
                System.out.println("RECEIVED FROM WEB: " + msg);
                System.out.println("-----------------------------\n");
            }
        });
    }

    private static void handleSignalingMessage(JSONObject json) {
        String type = json.optString("type");

        if (type.equals("offer")) {
            System.out.println("Received Offer");
            RTCSessionDescription offer = new RTCSessionDescription(
                RTCSdpType.OFFER, json.getString("sdp")
            );
            
            peerConnection.setRemoteDescription(offer, new SetSessionDescriptionObserver() {
                @Override
                public void onSuccess() {
                    createAnswer();
                }
                @Override public void onFailure(String error) { System.err.println("Set Remote Fail: " + error); }
            });

        } else if (type.equals("answer")) {
            System.out.println("Received Answer");
            RTCSessionDescription answer = new RTCSessionDescription(
                RTCSdpType.ANSWER, json.getString("sdp")
            );
            peerConnection.setRemoteDescription(answer, new SetSessionDescriptionObserver() {
                @Override public void onSuccess() { System.out.println("Remote Answer Set"); }
                @Override public void onFailure(String error) { System.err.println("Set Remote Fail: " + error); }
            });

        } else if (type.equals("candidate")) {
            RTCIceCandidate candidate = new RTCIceCandidate(
                json.getString("sdpMid"),
                json.getInt("sdpMLineIndex"),
                json.getString("candidate")
            );
            peerConnection.addIceCandidate(candidate);
        }
    }

    private static void createAnswer() {
        peerConnection.createAnswer(new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription description) {
                peerConnection.setLocalDescription(description, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        JSONObject json = new JSONObject();
                        json.put("type", "answer");
                        json.put("sdp", description.sdp);
                        signalingSocket.send(json.toString());
                        System.out.println("Sent Answer");
                    }
                    @Override public void onFailure(String error) {}
                });
            }
            @Override public void onFailure(String error) { System.err.println("Create Answer Fail: " + error); }
        }, new RTCAnswerOptions());
    }
    
    // Helper to format List<String> to JSON Array string manually or via lib
    private static String listToStringJson(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}