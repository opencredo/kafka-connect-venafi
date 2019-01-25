package com.opencredo.connect.venafi.tpp.log.model;

import com.google.common.annotations.VisibleForTesting;
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
    public static final String DATA = "Data";
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
            .field(GROUPING, Schema.INT32_SCHEMA)
            .field(ID, Schema.INT64_SCHEMA)
            .field(NAME, Schema.STRING_SCHEMA)
            .field(SERVER_TIMESTAMP, Timestamp.SCHEMA)
            .field(SEVERITY, Schema.STRING_SCHEMA)
            .field(SOURCE_IP, Schema.STRING_SCHEMA)
            .field(COMPONENT, Schema.OPTIONAL_STRING_SCHEMA)
            .field(COMPONENT_ID, Schema.OPTIONAL_INT32_SCHEMA)
            .field(COMPONENT_SUBSYSTEM, Schema.OPTIONAL_STRING_SCHEMA)
            .field(EVENT_ID, Schema.OPTIONAL_STRING_SCHEMA)
            .field(TEXT_1, Schema.OPTIONAL_STRING_SCHEMA)
            .field(TEXT_2, Schema.OPTIONAL_STRING_SCHEMA)
            .field(VALUE_1, Schema.OPTIONAL_INT32_SCHEMA)
            .field(VALUE_2, Schema.OPTIONAL_INT32_SCHEMA)
            .field(DATA, Schema.OPTIONAL_STRING_SCHEMA)
            .build();


    private ZonedDateTime ClientTimestamp;
    private String Component;
    private Integer ComponentId;
    private String ComponentSubsystem;
    private Integer Grouping;
    private Long Id;
    private String EventId;
    private String Name;
    private String Data;
    private ZonedDateTime ServerTimestamp;
    private String Severity;
    private String SourceIP;
    private String Text1;
    private String Text2;
    private Integer Value1;
    private Integer Value2;

    @VisibleForTesting
    EventLog(ZonedDateTime clientTimestamp, String component, Integer componentId, String componentSubsystem, Integer grouping, Long id, String eventId, String name, String data, ZonedDateTime serverTimestamp, String severity, String sourceIP, String text1, String text2, Integer value1, Integer value2) {
        ClientTimestamp = clientTimestamp;
        Component = component;
        ComponentId = componentId;
        ComponentSubsystem = componentSubsystem;
        Grouping = grouping;
        Id = id;
        EventId = eventId;
        Name = name;
        Data = data;
        ServerTimestamp = serverTimestamp;
        Severity = severity;
        SourceIP = sourceIP;
        Text1 = text1;
        Text2 = text2;
        Value1 = value1;
        Value2 = value2;
    }

    public static Schema TppLogSchema() {

        return SCHEMA;
    }

    public String getEventId() {
        return EventId;
    }

    public ZonedDateTime getClientTimestamp() {
        return ClientTimestamp;
    }

    public String getComponent() {
        return Component;
    }

    public Integer getComponentId() {
        return ComponentId;
    }

    public String getComponentSubsystem() {
        return ComponentSubsystem;
    }

    public Integer getGrouping() {
        return Grouping;
    }

    public Long getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public ZonedDateTime getServerTimestamp() {
        return ServerTimestamp;
    }

    public String getSeverity() {
        return Severity;
    }

    public String getSourceIP() {
        return SourceIP;
    }

    public String getText1() {
        return Text1;
    }

    public String getText2() {
        return Text2;
    }

    public Integer getValue1() {
        return Value1;
    }

    public Integer getValue2() {
        return Value2;
    }

    public String getData() {
        return Data;
    }

    public Struct toStruct() {

        Struct tppLog = new Struct(TppLogSchema())
                .put(CLIENT_TIMESTAMP, Date.from(getClientTimestamp().toInstant()))
                .put(GROUPING, getGrouping())
                .put(ID, getId())
                .put(NAME, getName())
                .put(SERVER_TIMESTAMP, Date.from(getServerTimestamp().toInstant()))
                .put(SEVERITY, getSeverity())
                .put(SOURCE_IP, getSourceIP());

        if (getComponent() != null) {
            tppLog.put(COMPONENT, getComponent());
        }
        if (getEventId() != null) {
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
        if (getData() != null) {
            tppLog.put(DATA, getData());
        }
        if (getComponentId() != null) {
            tppLog.put(COMPONENT_ID, getComponentId());
        }
        if (getComponentSubsystem() != null) {
            tppLog.put(COMPONENT_SUBSYSTEM, getComponentSubsystem());
        }

        return tppLog;
    }

}
