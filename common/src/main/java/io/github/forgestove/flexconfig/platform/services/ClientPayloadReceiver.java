package io.github.forgestove.flexconfig.platform.services;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
public interface ClientPayloadReceiver {
	<P extends NetworkPayload> void serverToClient(String channel, Class<P> type, PayloadHandler<P> handler);
}
