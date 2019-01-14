package com.opencredo.connect.venafi.tpp.log.model;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;

import java.time.ZonedDateTime;
import java.util.Date;

public class EventLog {
    public static final String CLIENT_TIMESTAMP = "ClientTimestamp";
    public static final String COMPONENT = "Component";
    public static final String COMPONENT_ID = "ComponentId";
    public static final String COMPONENT_SUBSYSTEM = "ComponentSubsystem";
    public static final String GROUPING = "Grouping";
    public static final String ID = "Id";
    public static final String EVENT_ID = "EventId";
    public static final String NAME = "Name";
    public static final String SERVER_TIMESTAMP = "ServerTimestamp";
    public static final String SEVERITY = "Severity";
    public static final String SOURCE_IP = "SourceIP";
    public static final String TEXT_1 = "Text1";
    public static final String TEXT_2 = "Text2";
    public static final String VALUE_1 = "Value1";
    public static final String VALUE_2 = "Value2";

    public static final Schema SCHEMA = SchemaBuilder.struct()
            .name(EventLog.class.getSimpleName())
            .field(CLIENT_TIMESTAMP, Timestamp.SCHEMA)
            .field(COMPONENT, Schema.STRING_SCHEMA)
            .field(COMPONENT_ID, Schema.INT32_SCHEMA)
            .field(COMPONENT_SUBSYSTEM, Schema.STRING_SCHEMA)
            .field(EVENT_ID, Schema.OPTIONAL_STRING_SCHEMA)
            .field(GROUPING, Schema.INT32_SCHEMA)
            .field(ID, Schema.INT32_SCHEMA)
            .field(NAME, Schema.STRING_SCHEMA)
            .field(SERVER_TIMESTAMP, Timestamp.SCHEMA)
            .field(SEVERITY, Schema.STRING_SCHEMA)
            .field(SOURCE_IP, Schema.STRING_SCHEMA)
            .field(TEXT_1, Schema.OPTIONAL_STRING_SCHEMA)
            .field(TEXT_2, Schema.OPTIONAL_STRING_SCHEMA)
            .field(VALUE_1, Schema.OPTIONAL_INT32_SCHEMA)
            .field(VALUE_2, Schema.OPTIONAL_INT32_SCHEMA)
            .build();


    private ZonedDateTime ClientTimestamp;
    private String Component;
    private Integer ComponentId;
    private String ComponentSubsystem;
    private Integer Grouping;
    private Integer Id;
    private String EventId;
    private String Name;
    private ZonedDateTime ServerTimestamp;
    private String Severity;
    private String SourceIP;
    private String Text1;
    private String Text2;
    private Integer Value1;
    private Integer Value2;

    public static Schema TppLogSchema() {

        return SCHEMA;
    }

    public String getEventId() {
        return EventId;
    }

    public void setEventId(String eventId) {
        EventId = eventId;
    }

    public ZonedDateTime getClientTimestamp() {
        return ClientTimestamp;
    }

    public void setClientTimestamp(ZonedDateTime clientTimestamp) {
        this.ClientTimestamp = clientTimestamp;
    }

    public String getComponent() {
        return Component;
    }

    public void setComponent(String component) {
        Component = component;
    }

    public Integer getComponentId() {
        return ComponentId;
    }

    public void setComponentId(Integer componentId) {
        ComponentId = componentId;
    }

    public String getComponentSubsystem() {
        return ComponentSubsystem;
    }

    public void setComponentSubsystem(String componentSubsystem) {
        ComponentSubsystem = componentSubsystem;
    }

    public Integer getGrouping() {
        return Grouping;
    }

    public void setGrouping(Integer grouping) {
        Grouping = grouping;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public ZonedDateTime getServerTimestamp() {
        return ServerTimestamp;
    }

    public void setServerTimestamp(ZonedDateTime serverTimestamp) {
        ServerTimestamp = serverTimestamp;
    }

    public String getSeverity() {
        return Severity;
    }

    public void setSeverity(String severity) {
        Severity = severity;
    }

    public String getSourceIP() {
        return SourceIP;
    }

    public void setSourceIP(String sourceIP) {
        SourceIP = sourceIP;
    }

    public String getText1() {
        return Text1;
    }

    public void setText1(String text1) {
        Text1 = text1;
    }

    public String getText2() {
        return Text2;
    }

    public void setText2(String text2) {
        Text2 = text2;
    }

    public Integer getValue1() {
        return Value1;
    }

    public void setValue1(Integer value1) {
        Value1 = value1;
    }

    public Integer getValue2() {
        return Value2;
    }

    public void setValue2(Integer value2) {
        Value2 = value2;
    }

    public Struct toStruct() {

        Struct tppLog = new Struct(TppLogSchema())
                .put(CLIENT_TIMESTAMP, Date.from(getClientTimestamp().toInstant()))
                .put(COMPONENT, getComponent())
                .put(COMPONENT_ID, getComponentId())
                .put(COMPONENT_SUBSYSTEM, getComponentSubsystem())

                .put(GROUPING, getGrouping())
                .put(ID, getId())
                .put(NAME, getName())
                .put(SERVER_TIMESTAMP, Date.from(getServerTimestamp().toInstant()))
                .put(SEVERITY, getSeverity())
                .put(SOURCE_IP, getSourceIP());

        if(getEventId() != null){
            tppLog.put(EVENT_ID, getEventId());
        }

        if (getText1() != null) {
            tppLog.put(TEXT_1, getText1());
        }

        if (getText2() != null) {
            tppLog.put(TEXT_2, getText2());
        }
        if (getValue1() != null) {
            tppLog.put(VALUE_1, getValue1());
        }
        if (getValue2() != null) {
            tppLog.put(VALUE_2, getValue2());
        }

        return tppLog;
    }

}
