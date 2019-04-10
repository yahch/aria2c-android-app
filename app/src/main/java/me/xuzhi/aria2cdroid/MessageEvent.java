package me.xuzhi.aria2cdroid;

/**
 * Created by xuzhi on 2018/3/12.
 */

public class MessageEvent {

    private String eventName;

    private Object eventData;

    public MessageEvent(String eventName, Object eventData) {
        this.eventName = eventName;
        this.eventData = eventData;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }
}
