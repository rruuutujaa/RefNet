import { storage } from "./storage.js";

let stompClient = null;
let subscriptions = [];

export function connectWebSocket({ onMessage, onError } = {}) {
    if (stompClient?.connected) return Promise.resolve(stompClient);
    if (!window.SockJS || !window.Stomp) return Promise.reject(new Error("SockJS/STOMP missing"));

    return new Promise((resolve, reject) => {
        const socket = new SockJS("http://localhost:8080/ws");
        stompClient = window.Stomp.over(socket);
        stompClient.debug = () => {};

        const token = storage.getAccessToken();
        stompClient.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                if (onMessage) {
                    const sub = stompClient.subscribe("/user/queue/messages", (frame) => {
                        onMessage(JSON.parse(frame.body));
                    });
                    subscriptions.push(sub);
                }
                if (onError) {
                    const sub = stompClient.subscribe("/user/queue/errors", (frame) => onError(frame.body));
                    subscriptions.push(sub);
                }
                resolve(stompClient);
            },
            (err) => reject(err)
        );
    });
}

export function sendChatMessage(receiverId, content) {
    if (!stompClient?.connected) throw new Error("WebSocket not connected");
    stompClient.send("/app/chat.send", {}, JSON.stringify({ receiverId, content }));
}

export function disconnectWebSocket() {
    subscriptions.forEach((sub) => sub?.unsubscribe?.());
    subscriptions = [];
    if (stompClient?.connected) stompClient.disconnect();
    stompClient = null;
}
