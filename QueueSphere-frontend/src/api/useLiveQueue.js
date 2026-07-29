import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Subscribes to /topic/queue/{doctorId} and keeps `status` updated in real time.
 * Falls back gracefully if the socket disconnects (auto-reconnects).
 */
export function useLiveQueue(doctorId) {
  const [status, setStatus] = useState(null);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    if (!doctorId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);
        client.subscribe(`/topic/queue/${doctorId}`, (message) => {
          setStatus(JSON.parse(message.body));
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [doctorId]);

  return { status, connected, setStatus };
}
