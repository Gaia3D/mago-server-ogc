package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.domain.ConversionProcess;
import com.gaia3d.mago.server.domain.type.ProcessType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class ExecutionStatusService {
    private final List<ConversionProcess> processList = new ArrayList<>();
    private static final String STATUS_FILE_NAME = "status.txt";

    public void setStatus(File input, ProcessType executionStatus) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(input, STATUS_FILE_NAME)))) {
            writer.write(executionStatus.name());
        } catch (IOException e) {
            log.error("Failed to write status file", e);
            log.error("input: {}", input);
            log.error("executionStatus: {}", executionStatus);
            throw new RuntimeException(e);
        }
    }

    public ProcessType getStatus(File input) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(input, STATUS_FILE_NAME)))) {
            return ProcessType.valueOfSafetyNull(reader.readLine());
        } catch (FileNotFoundException e) {
            return ProcessType.NOT_FOUND;
        } catch (IOException e) {
            log.error("Failed to read status file", e);
            throw new RuntimeException(e);
        }
    }
}
