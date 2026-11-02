package com.nathalyamaya.qa.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads external test data sets from src/test/resources/testdata so that test
 * conditions can be added or changed without touching test code.
 */
public final class TestData {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestData() {
    }

    /**
     * Reads a pipe-delimited table ('|') with a header row. Pipes are used instead of
     * commas because expected UI messages often contain commas. Lines starting with
     * '#' are comments. An empty cell becomes an empty string (used to test blank inputs).
     */
    public static Object[][] table(String resource) {
        List<Object[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(open(resource), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                rows.add(line.split("\\|", -1));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
        return rows.toArray(new Object[0][]);
    }

    /** Reads a JSON array of objects; each object becomes one data-provider row. */
    public static Object[][] jsonList(String resource) {
        try (InputStream in = open(resource)) {
            List<Map<String, Object>> items = MAPPER.readValue(in, new TypeReference<>() {
            });
            return items.stream().map(item -> new Object[]{item}).toArray(Object[][]::new);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
    }

    private static InputStream open(String resource) {
        InputStream in = TestData.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("Test data not found on classpath: " + resource);
        }
        return in;
    }
}
