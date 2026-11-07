package io.github.forgestove.flexconfig.platform.services;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
@FunctionalInterface
public interface PayloadHandler<P extends NetworkPayload> {
	void handle(P payload, PayloadContext context);
}
