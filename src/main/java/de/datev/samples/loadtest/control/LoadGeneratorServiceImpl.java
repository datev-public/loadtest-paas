package de.datev.samples.loadtest.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class LoadGeneratorServiceImpl implements LoadGeneratorService {

    private byte[] kiloByteBlock;

    @PostConstruct
    private void init() {

        byte[] ret = new byte[1024];
        Arrays.fill(ret, (byte) 'a');
        this.kiloByteBlock = ret;
    }

    @Override
    public int computeFibonacciRecursive(int i) {

        if (i <= 0)
            return 0;
        else if (i == 1)
            return 1;
        else
            return computeFibonacciRecursive(i - 2) + computeFibonacciRecursive(i - 1);
    }

    @Override
    public long stream(OutputStream out, int numberOfKiloByteBlocks) throws IOException {

        long ret = 0;
        for (int i = 0; i < numberOfKiloByteBlocks; i++) {
            out.write(kiloByteBlock);
            ret += kiloByteBlock.length;
        }
        return ret;
    }

    @Override
    public Stream<byte[]> getBlockStream(int numberOfKiloByteBlocks) {

        return IntStream.range(0, numberOfKiloByteBlocks).mapToObj(i -> kiloByteBlock);
    }

    @Override
    public String createStringOfSize(int size) {

        char[] ret = new char[size];
        Arrays.fill(ret, 'a');
        return new String(ret);
    }

    @Override
    public Map<String, List<String>> createLargeObject(int factor) {

        Map<String, List<String>> map = new HashMap<>();
        for (int mapEntries = 0; mapEntries < factor; mapEntries++) {
            List<String> list = new ArrayList<>();
            for (int listEntries = 0; listEntries < factor; listEntries++) {
                final String string = String.format(
                        "0123456789-0123456789-0123456789-0123456789-0123456-%050d", listEntries + mapEntries * 1000000);
                list.add(string);
            }
            final String key = String.format("%010d", mapEntries);
            map.put(key, list);
        }
        return map;
    }

    @Override
    public int calculateSizeOfLargeObject(Map<String, List<String>> object) {

        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(out, object);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        byte[] bytes = out.toByteArray();
        return bytes.length;
    }
}
