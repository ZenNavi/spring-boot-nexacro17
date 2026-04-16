package com.nexacro.xapi.tx;

import com.nexacro.xapi.data.DataSet;
import com.nexacro.xapi.data.PlatformData;

import javax.servlet.http.HttpServletRequest;

/**
 * Stub for com.nexacro.xapi.tx.HttpPlatformRequest.
 * Parses a simple text-based PlatformData format sent by Nexacro 17 client.
 *
 * Real format: Binary or XML PlatformData from Nexacro.
 * Stub format (for testing without real Nexacro client):
 *   Accepts JSON-like or raw body. For actual Nexacro client, replace with real jar.
 *
 * Replace with real nexacro-xapi17.jar when available.
 */
public class HttpPlatformRequest {

    private final HttpServletRequest request;
    private PlatformData platformData;

    public HttpPlatformRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Reads and parses the request body as PlatformData.
     * Stub implementation: reads raw body and returns empty PlatformData.
     * The real implementation parses Nexacro binary/XML format.
     */
    public void receiveData() throws Exception {
        platformData = new PlatformData();
        // Stub: body is not parsed here.
        // In production: replace with real Nexacro PlatformData parsing.
    }

    public PlatformData getData() {
        return platformData;
    }
}
