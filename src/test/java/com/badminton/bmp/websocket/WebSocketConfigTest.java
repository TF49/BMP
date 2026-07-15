package com.badminton.bmp.websocket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

class WebSocketConfigTest {

    @Test
    void registersSockJsWebEndpointAndNativeMiniProgramEndpoint() {
        WebSocketAuthInterceptor interceptor = mock(WebSocketAuthInterceptor.class);
        WebSocketConfig config = new WebSocketConfig(interceptor, new String[]{"https://example.test"});
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration sockJsEndpoint = mock(StompWebSocketEndpointRegistration.class);
        StompWebSocketEndpointRegistration nativeEndpoint = mock(StompWebSocketEndpointRegistration.class);
        SockJsServiceRegistration sockJs = mock(SockJsServiceRegistration.class);
        when(registry.addEndpoint("/ws")).thenReturn(sockJsEndpoint);
        when(registry.addEndpoint("/ws-native")).thenReturn(nativeEndpoint);
        when(sockJsEndpoint.setAllowedOriginPatterns("https://example.test")).thenReturn(sockJsEndpoint);
        when(nativeEndpoint.setAllowedOriginPatterns("https://example.test")).thenReturn(nativeEndpoint);
        when(sockJsEndpoint.withSockJS()).thenReturn(sockJs);
        when(sockJs.setClientLibraryUrl(org.mockito.ArgumentMatchers.anyString())).thenReturn(sockJs);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
        verify(registry).addEndpoint("/ws-native");
        verify(sockJsEndpoint).withSockJS();
        verify(nativeEndpoint, never()).withSockJS();
    }
}
