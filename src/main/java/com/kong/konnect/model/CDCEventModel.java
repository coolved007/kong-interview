package com.kong.konnect.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CDCEventModel {

    @JsonProperty("op")
    private String op;

    @JsonProperty("ts_ms")
    private long tsMs;

    @JsonProperty("after")
    private CDCAfter after;

    @JsonProperty("before")
    private CDCBefore before;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public long getTsMs() {
        return tsMs;
    }

    public void setTsMs(long tsMs) {
        this.tsMs = tsMs;
    }

    public CDCAfter getAfter() {
        return after;
    }

    public void setAfter(CDCAfter after) {
        this.after = after;
    }

    public CDCBefore getBefore() {
        return before;
    }

    public void setBefore(CDCBefore before) {
        this.before = before;
    }

    public String getId() {
        if (after == null) {
            return before.getId();
        }
        return after.getValue() != null && after.getValue().getObject().getId() != null ?
                after.getValue().getObject().getId() : after.getKey();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CDCBefore {

        @JsonProperty("id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CDCAfter {

        @JsonProperty("key")
        private String key;
        @JsonProperty("value")
        private CDCValue value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public CDCValue getValue() {
            return value;
        }

        public void setValue(CDCValue value) {
            this.value = value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CDCValue {
        @JsonProperty("object")
        private CDCObject object;

        public CDCObject getObject() {
            return object;
        }

        public void setObject(CDCObject object) {
            this.object = object;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CDCObject {
        @JsonProperty("id")
        private String id;

        private final Map<String, Object> data = new HashMap<>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            data.put(key, value);
        }

        public Map<String, Object> getData() {
            return data;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
}



