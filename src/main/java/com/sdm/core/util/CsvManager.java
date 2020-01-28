package com.sdm.core.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class CsvManager<T> {

    public List<T> parseCsvToEntity(MultipartFile file, Class<T> tClass) throws IOException {
        List<Map<?, ?>> datas = readObjectsFromCsv(file.getInputStream());
        ObjectMapper mapper = new ObjectMapper();

        List<T> results = new ArrayList<>();
        datas.forEach(data -> {
            results.add(mapper.convertValue(data, tClass));
        });

        return results;
    }

    private List<Map<?, ?>> readObjectsFromCsv(InputStream file) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(file);

        return mappingIterator.readAll();
    }

    public Resource parseEntityToCsv(List<T> entities, Class<T> tClass) throws IOException, IllegalAccessException {
        StringWriter writer = new StringWriter();

        //Write Headers
        List<Field> fields = Arrays.asList(tClass.getDeclaredFields());
        Method[] methods = tClass.getDeclaredMethods();

        List<String> headers = new ArrayList<>();
        for (Field f : fields) {
            if (f.getName() == "serialVersionUID") continue;

            headers.add(f.getName());
        }
        writeLine(writer, headers);

        //Write Datas
        for (T entity : entities) {
            List<String> rowDatas = new ArrayList<>();
            for (Field f : fields) {
                if (f.getName() == "serialVersionUID") continue;

                f.setAccessible(true);

                if (f.get(entity) == null) {
                    rowDatas.add("");
                } else {
                    if (f.getType().isPrimitive())
                        rowDatas.add(f.get(entity).toString());
                    else {
                        ObjectMapper objMapper = new ObjectMapper();
                        rowDatas.add(objMapper.writeValueAsString(f.get(entity)));
                    }
                }
            }
            writeLine(writer, rowDatas);
        }

        writer.flush();
        writer.close();
        return new ByteArrayResource(writer.toString().getBytes("UTF-8"));
    }

    private final char DEFAULT_SEPARATOR = ',';

    private void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    private void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private String followCVSformat(String value) {
        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;
    }

    private void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {
        boolean first = true;

        //default customQuote is empty
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            //Surround Value With "" To Let Excel know its data
            sb.append("\"");
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }
            sb.append("\"");

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }
}
