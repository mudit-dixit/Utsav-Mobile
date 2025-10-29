package com.example.user;

import android.content.Context;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * A centralized client for making API calls to the Utsav server.
 * It automatically handles adding the authentication token to requests.
 */
public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/api/";
    private final OkHttpClient httpClient;
    private final SessionManager sessionManager;

    public ApiClient(Context context) {
        this.httpClient = new OkHttpClient();
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Makes a GET request to a specified endpoint.
     * Automatically adds the auth token if it exists.
     * @param endpoint e.g., "teams", "users"
     * @return A Call object ready to be enqueued.
     */
    public okhttp3.Call get(String endpoint) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + endpoint);

        String token = sessionManager.getAuthToken();
        if (token != null) {
            requestBuilder.addHeader("x-auth-token", token);
        }

        Request request = requestBuilder.build();
        return httpClient.newCall(request);
    }

    /**
     * Makes a POST request to a specified endpoint with a JSON body.
     * Automatically adds the auth token if it exists.
     * @param endpoint e.g., "auth/login", "users"
     * @param jsonBody The JSON string to send.
     * @return A Call object ready to be enqueued.
     */
    public okhttp3.Call post(String endpoint, String jsonBody) {
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .post(body);

        // For most POST requests (except login/register), we need the auth token
        if (!endpoint.startsWith("auth/")) {
            String token = sessionManager.getAuthToken();
            if (token != null) {
                requestBuilder.addHeader("x-auth-token", token);
            }
        }

        Request request = requestBuilder.build();
        return httpClient.newCall(request);
    }

    /**
     * Makes a PUT request to a specified endpoint with a JSON body.
     * Automatically adds the auth token.
     * @param endpoint e.g., "users/0x123", "rounds/0xabc"
     * @param jsonBody The JSON string to send.
     * @return A Call object ready to be enqueued.
     */
    public okhttp3.Call put(String endpoint, String jsonBody) {
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .put(body); // Use .put() for the HTTP method

        String token = sessionManager.getAuthToken();
        if (token != null) {
            requestBuilder.addHeader("x-auth-token", token);
        }

        Request request = requestBuilder.build();
        return httpClient.newCall(request);
    }

    /**
     * Makes a DELETE request to a specified endpoint.
     * Automatically adds the auth token.
     * @param endpoint e.g., "users/0x123", "teams/0xdef"
     * @return A Call object ready to be enqueued.
     */
    public okhttp3.Call delete(String endpoint) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .delete(); // Use .delete() for the HTTP method

        String token = sessionManager.getAuthToken();
        if (token != null) {
            requestBuilder.addHeader("x-auth-token", token);
        }

        Request request = requestBuilder.build();
        return httpClient.newCall(request);
    }
}

