package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.exception.TilesInvalidException;
import com.gaia3d.mago.server.exception.TilesNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutorService {

    public boolean executeProcess(List<String> argList) {
        String[] args = argList.toArray(new String[0]);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);

        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        String command = stringBuilder.toString();

        ExecutionThreadPoolService.execute(() -> {
            try {
                log.info("Process Command : {}", command);
                log.info("---Start Tiling Process---");
                Process process = processBuilder.start();

                BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                for (String str; (str = inputReader.readLine()) != null; ) {
                    log.info(str);
                }
                for (String str; (str = errorReader.readLine()) != null; ) {
                    log.info(str);
                }
                log.info("---End Tiling Process---");
            } catch (TilesInvalidException | TilesNotFoundException | IOException e) {
                log.error("error", e);
            }
        });
        return true;
    }

    public boolean executeProcessWithoutThread(List<String> argList) {
        String[] args = argList.toArray(new String[0]);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);

        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        String command = stringBuilder.toString();

        try {
            log.info("Process Command : {}", command);
            log.info("---Start Tiling Process---");
            Process process = processBuilder.start();

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            for (String str; (str = inputReader.readLine()) != null; ) {
                log.info(str);
            }
            for (String str; (str = errorReader.readLine()) != null; ) {
                log.info(str);
            }
            log.info("---End Tiling Process---");
        } catch (TilesInvalidException | TilesNotFoundException | IOException e) {
            log.error("error", e);
        }
        return true;
    }
}
