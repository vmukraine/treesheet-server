const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 8080 });

console.log("Signaling server running on ws://localhost:8080");

wss.on('connection', (ws) => {
    console.log("New client connected");

    ws.on('message', (message) => {
        const data = JSON.parse(message);
        
        // Broadcast to everyone else (simple relay)
        wss.clients.forEach((client) => {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(data));
            }
        });
    });

    ws.on('close', () => {
        console.log("Client disconnected");
    });
});