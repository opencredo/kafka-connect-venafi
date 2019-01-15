package com.opencredo.connect.venafi.tpp.log;

import com.opencredo.connect.venafi.tpp.log.api.client.LogsClient;
import com.opencredo.connect.venafi.tpp.log.api.client.TokenClient;
import com.opencredo.connect.venafi.tpp.log.model.EventLog;
import com.opencredo.connect.venafi.tpp.log.model.LogResponse;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TppLogSourceTask extends SourceTask {

    public static final String URL = "url";
    public static final String LAST_READ = "last_read";
    public static final String DEFAULT_FROM_TIME = "2018-05-04T00:00:00.0000000Z";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TppLogSourceConnector.class);
    private static ZonedDateTime staticOffset;
    private String baseUrl;
    private String topic;
    private String batchSize;
    private Long interval;
    private Long last_execution = 0L;


    @Override
    public String version() {
        return null;
    }

    @Override
    public void start(Map<String, String> props) {
        baseUrl = props.get(TppLogSourceConnector.BASE_URL_CONFIG);
        topic = props.get(TppLogSourceConnector.TOPIC_CONFIG);
        batchSize = props.get(TppLogSourceConnector.BATCH_SIZE);
        interval = Long.parseLong(props.get(TppLogSourceConnector.POLL_INTERVAL));
    }

    @Override
    public List<SourceRecord> poll() {
        if (System.currentTimeMillis() > (last_execution + interval)) {
            last_execution = System.currentTimeMillis();
            return getTppLogsAsSourceRecords();
        } else {
            return Collections.emptyList();
        }
    }

    private List<SourceRecord> getTppLogsAsSourceRecords() {
        String token = getToken();
        String fromDate = DEFAULT_FROM_TIME;

        Map<String, Object> offset = context.offsetStorageReader()
                .offset(Collections.singletonMap(URL, baseUrl));
        if (offset != null) {
            ZonedDateTime lastRecordedOffset = (ZonedDateTime) offset.get(LAST_READ);
            if (lastRecordedOffset != null) {
                fromDate = lastRecordedOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        } else if (staticOffset != null) {
            fromDate = staticOffset.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        List<EventLog> jsonLogs = getTppLogs(token, fromDate);
        ArrayList<SourceRecord> records = new ArrayList<>();
        for (EventLog eventLog : jsonLogs) {
            Map<String, Object> sourcePartition = Collections.singletonMap(URL, baseUrl);
            Map<String, Object> sourceOffset = Collections
                    .singletonMap(LAST_READ, eventLog.getClientTimestamp());
            staticOffset = eventLog.getClientTimestamp();
            records.add(new SourceRecord(sourcePartition, sourceOffset, topic,
                    EventLog.TppLogSchema(), eventLog.toStruct()));
        }

        return records;


    }

    List<EventLog> getTppLogs(String token, String date) {
        LogResponse logResponse = LogsClient.getLogs(token, date, baseUrl, batchSize);

        return logResponse.getLogEvents();
    }

    String getToken() {
        return TokenClient.getToken(baseUrl);
    }

    @Override
    public void stop() {

    }


}
