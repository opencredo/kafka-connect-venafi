package com.opencredo.connect.venafi.tpp.log;

import com.opencredo.connect.venafi.tpp.log.api.client.LogsClient;
import com.opencredo.connect.venafi.tpp.log.api.client.TokenClient;
import com.opencredo.connect.venafi.tpp.log.model.EventLog;
import com.opencredo.connect.venafi.tpp.log.model.LogResponse;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConfig.*;

public class TppLogSourceTask extends SourceTask {

    public static final String URL = "url";
    public static final String LAST_READ = "last_read";
    public static final String LAST_API_OFFSET = "last_api_offset";
    public static final String DEFAULT_FROM_TIME = "1984-05-04T00:00:00.0000000Z";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TppLogSourceTask.class);
    private String fromDate = DEFAULT_FROM_TIME;
    private String baseUrl;
    private String topic;
    private String batchSize;
    private long apiOffset;
    private Long interval;
    private Long last_execution = 0L;
    private TokenClient tokenClient;

    static boolean isNotNullOrBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        setupTaskConfig(props);

        log.debug("Trying to get persistedMap.");
        Map<String, Object> persistedMap = null;
        if (context != null && context.offsetStorageReader() != null) {
            persistedMap = context.offsetStorageReader().offset(Collections.singletonMap(URL, baseUrl));
        }
        log.info("The persistedMap is {}", persistedMap);
        if (persistedMap != null) {
            String lastRead = (String) persistedMap.get(LAST_READ);
            if (isNotNullOrBlank(lastRead)) {
                fromDate = lastRead;
            }

            Object lastApiOffset = persistedMap.get(LAST_API_OFFSET);
            if (lastApiOffset != null) {
                apiOffset = (Long) lastApiOffset;
            }


        }
    }

    private void setupTaskConfig(Map<String, String> props) {
        baseUrl = props.get(BASE_URL_CONFIG);
        topic = props.get(TOPIC_CONFIG);
        batchSize = props.get(BATCH_SIZE);
        interval = Long.parseLong(props.get(POLL_INTERVAL));

        String username = props.get(USERNAME_CONFIG);
        String password = props.get(PASSWORD_CONFIG);
        tokenClient = new TokenClient(username, password);
    }

    @Override
    public List<SourceRecord> poll() {
        if (System.currentTimeMillis() > (last_execution + interval)) {
            last_execution = System.currentTimeMillis();
            String token = getToken();
            if (isNotNullOrBlank(token)) {
                return getTppLogsAsSourceRecords(token);
            }
        }
        return Collections.emptyList();

    }


    private List<SourceRecord> getTppLogsAsSourceRecords(String token) {

        List<EventLog> jsonLogs = getTppLogs(token, fromDate, apiOffset);
        ArrayList<SourceRecord> records = new ArrayList<>();
        for (EventLog eventLog : jsonLogs) {

            String newFromDate = eventLog.getClientTimestamp().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            apiOffset = calculateApiOffset(apiOffset, newFromDate, fromDate);
            fromDate = newFromDate;

            log.debug(" The fromDate is now {}.", fromDate);
            records.add(buildSourceRecord(eventLog, fromDate, apiOffset));
        }

        return records;
    }

    private SourceRecord buildSourceRecord(EventLog eventLog, String lastRead, Long apiOffset) {
        Map<String, Object> sourceOffset = buildSourceOffset(lastRead, apiOffset);
        Map<String, Object> sourcePartition = buildSourcePartition();
        return new SourceRecord(sourcePartition, sourceOffset, topic, EventLog.TppLogSchema(), eventLog.toStruct());
    }

    private long calculateApiOffset(long currentLoopOffset, String newFromDate, String oldFromDate) {
        if (newFromDate.equals(oldFromDate)) {
            return ++currentLoopOffset;
        }
        return 1L;
    }

    private Map<String, Object> buildSourcePartition() {
        return Collections.singletonMap(URL, baseUrl);
    }

    private Map<String, Object> buildSourceOffset(String lastRead, Long apiOffset) {
        Map<String, Object> sourceOffset = new HashMap<>();
        sourceOffset.put(LAST_READ, lastRead);
        sourceOffset.put(LAST_API_OFFSET, apiOffset);
        return sourceOffset;
    }

    List<EventLog> getTppLogs(String token, String date, long offset) {
        LogResponse logResponse = LogsClient.getLogs(token, date, baseUrl, batchSize, offset);

        return logResponse.getLogEvents();
    }

    String getToken() {
        return tokenClient.getToken(baseUrl);
    }

    @Override
    public void stop() {

    }


}
