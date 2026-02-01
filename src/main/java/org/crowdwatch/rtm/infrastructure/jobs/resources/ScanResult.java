package org.crowdwatch.rtm.infrastructure.jobs.resources;

import java.util.List;

import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class ScanResult {
    private final String cursor;
    private final List<String> keys;
    private final boolean complete;

    public ScanResult(String cursor, List<String> keys, boolean complete) {
        this.cursor = cursor;
        this.keys = keys;
        this.complete = complete;
    }
}
