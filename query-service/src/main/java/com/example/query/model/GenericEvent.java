package com.example.query.model;

public class GenericEvent<T> {
    private String eventType;
    private T payload;

    public GenericEvent() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}
