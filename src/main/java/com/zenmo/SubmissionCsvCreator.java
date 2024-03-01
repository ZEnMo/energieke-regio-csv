package com.zenmo;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import static com.zenmo.JsonFlattener.flattenJson;

public class SubmissionCsvCreator {
    private OkHttpClient httpClient;

    private String energiekeRegioApiKey;

    public SubmissionCsvCreator(OkHttpClient httpClient, String energiekeRegioApiKey) {
        this.httpClient = httpClient;
        this.energiekeRegioApiKey = energiekeRegioApiKey;
    }

    public Map<String, String> getSubmission(String link) throws IOException, InterruptedException {
        link = link.replace("/zenmo", "/zenmo/");
        var request = new Request.Builder()
                .header("Authorization", "Bearer " + energiekeRegioApiKey)
                .url(link)
                .header("Accept", "application/json")
                .build();

        var response = this.httpClient.newCall(request).execute();
        var jsonObject = Json.createReader(new StringReader(response.body().string())).readObject();

        var result = flattenJson(jsonObject);

        if (!result.containsKey("Bestanden.0.Link")) {
            return result;
        }

        var csvLink = result.get("Bestanden.0.Link").replace("/zenmo", "/zenmo/");
        appendCsvContent(csvLink, result);

        var i = -1;
        for (var bestand : jsonObject.getJsonArray("Bestanden")) {
            i++;
            if (i == 0) {
                continue;
            }

            var bestandObject = (JsonObject) bestand;
            var otherRequest = new Request.Builder()
                    .header("Authorization", "Bearer " + energiekeRegioApiKey)
                    .url(bestandObject.getString("Link"))
                    .build();
            var response2 = httpClient.newCall(otherRequest).execute();

            var fileName =
                    jsonObject.getString("Project")
                    + " - " +
                    result.get("Organisatie.Naam")
                    + " - " +
                    bestandObject.getString("Omschrijving")
                    + " - " +
                    bestandObject.getString("Naam");

            fileName = fileName.replaceAll("[*\"<>:|/\\?]", "_");

            if (!new File(fileName).isFile()) {
                response2.body().byteStream().transferTo(new FileOutputStream(fileName));
            }
        }

        return result;
    }

    public void appendCsvContent(String csvUrl, Map<String, String> map) throws IOException {
        var csvRequest = new Request.Builder()
                .header("Authorization", "Bearer " + energiekeRegioApiKey)
                .url(csvUrl)
                .header("Accept", "text/csv")
                .build();

        var response = httpClient.newCall(csvRequest).execute();
        var responseBody = response.body();
        if (responseBody == null) {
            throw new RuntimeException("No response body");
        }

        var parsed = CsvParser.parseCsv(responseBody.string());
        map.putAll(parsed);
    }
}
