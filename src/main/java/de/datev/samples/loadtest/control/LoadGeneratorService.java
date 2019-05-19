package de.datev.samples.loadtest.control;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface LoadGeneratorService {

    int computeFibonacciRecursive(int i);

    long stream(OutputStream out, int numberOfKiloByteBlocks) throws IOException;

    Stream<byte[]> getBlockStream(int numberOfKiloByteBlocks);

    String createStringOfSize(int size);

    Map<String, List<String>> createLargeObject(int factor);

    int calculateSizeOfLargeObject(Map<String, List<String>> object);
}
