package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.domain.*;
import com.gaia3d.mago.server.domain.type.ProcessType;
import com.gaia3d.mago.server.exception.TilesInvalidException;
import com.gaia3d.mago.server.exception.TilesNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.thymeleaf.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagoTilerService implements DefaultConverterService {
    private final ExecutionStatusService executionStatusService;
    private final NotificationService notificationService;

    @Value("${java.path}")
    private String JAVA_BIN_PATH;

    @Value("${data.path}")
    private String DATA_PATH;

    @Value("${asset.path}")
    private String ASSET_PATH;

    @Value("${tiler.module.name}")
    private String MODULE_NAME;

    @Value("${use.docker.module}")
    private boolean USE_DOCKER_MODULE;

    private ClassPathResource getModulePath() {
        return new ClassPathResource("modules/" + MODULE_NAME);
    }

    private File getTempPath() {
        File tempDir = new File(DATA_PATH, "temp");
        if (!tempDir.exists() && tempDir.mkdirs()) {
            log.debug("Temp directory is created");
        }
        return tempDir;
    }

    private File getTempPath(String subDir) {
        File tempDir = getTempPath();
        File subTempDir = new File(tempDir, subDir);
        if (!subTempDir.exists() && subTempDir.mkdirs()) {
            log.debug("Sub directory {} is created.", subTempDir.getName());
        }
        return subTempDir;
    }

    private File getInputPath() {
        File inputDir = new File(DATA_PATH, "input");
        if (!inputDir.exists() && inputDir.mkdirs()) {
            log.debug("Input directory is created");
        }
        return inputDir;
    }

    private File getInputPath(String subDir) {
        File inputDir = getInputPath();
        File subTempDir = new File(inputDir, subDir);
        if (!subTempDir.exists() && subTempDir.mkdirs()) {
            log.debug("Sub directory {} is created.", subTempDir.getName());
        }
        return subTempDir;
    }

    private File getOutputPath() {
        File outputDir = new File(ASSET_PATH);
        if (!outputDir.exists() && outputDir.mkdirs()) {
            log.debug("Output directory is created");
        }
        return outputDir;
    }

    private File getOutputPath(String subDir) {
        File outputDir = getOutputPath();
        File subTempDir = new File(outputDir, subDir);
        if (!subTempDir.exists() && subTempDir.mkdirs()) {
            log.debug("Sub directory {} is created.", subTempDir.getName());
        }
        return subTempDir;
    }

    public void saveTempFiles(List<MultipartFile> multipartFiles, TileAssetRequest request) {
        if (multipartFiles.isEmpty()) {
            throw new TilesNotFoundException("Upload files are not found. Please check the input files.");
        }

        File outputDir = getOutputPath(request.getName());
        executionStatusService.setStatus(outputDir, ProcessType.UPLOADING);

        log.debug("---Save Upload Files---");
        File tempDir = getTempPath(request.getName());
        if (!tempDir.exists() && tempDir.mkdirs()) {
            log.debug("Temp directory is created");
        } else if (tempDir.exists() && tempDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(tempDir);
                if (!tempDir.mkdirs()) {
                    throw new RuntimeException("Can't create directory");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Can't create directory");
        }

        executionStatusService.setStatus(outputDir, ProcessType.READY);

        request.setInputPath(tempDir.getAbsolutePath());
        request.setOutputPath(outputDir.getAbsolutePath());
        multipartFiles.forEach(multipartFile -> {
            String fileName = multipartFile.getOriginalFilename();
            assert fileName != null;
            File file = new File(tempDir, fileName);
            try {
                multipartFile.transferTo(file);
                if (fileName.endsWith(".zip")) {
                    File unzipDir = unzipFile(file, tempDir);
                    FileUtils.deleteQuietly(file);
                    //FileUtils.deleteQuietly(unzipDir);
                }
            } catch (IOException e) {
                log.error("error", e);
            }
        });
    }

    public byte[] download(TileAssetRequest request) {
        String userId = request.getUserId();
        String tileSetName = request.getName();
        File zipFile = new File(getTempPath(tileSetName), tileSetName + ".zip");
        File outputDir = getOutputPath(tileSetName);
        createZip(outputDir, zipFile);
        try (BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(zipFile))) {
            return fileReader.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(zipFile);
        }
    }

    public List<TileAsset> getAllList(Pagination request, String userId) {
        String assetStatus = request.getAssetStatus() == null ? "" : request.getAssetStatus();

        List<TileAsset> tileAssets = new ArrayList<>();
        File output = getOutputPath();
        if (output.isDirectory()) {
            for (File child : Objects.requireNonNull(output.listFiles())) {
                if (child.isDirectory()) {
                    if (request.getFilterWord() != null && !StringUtils.containsIgnoreCase(child.getName(), request.getFilterWord(), Locale.KOREAN)) {
                        continue;
                    }

                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(child.toPath(), "creationTime");
                        Date date = new Date(creationTime.toMillis());
                        LocalDateTime newDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedDateTime = newDateTime.format(formatter);

                        ProcessType status = executionStatusService.getStatus(child);
                        if (assetStatus.equalsIgnoreCase("running") && status != ProcessType.RUNNING) {
                            continue;
                        } else if (assetStatus.equalsIgnoreCase("success") && status != ProcessType.SUCCESS) {
                            continue;
                        } else if (assetStatus.equalsIgnoreCase("fail") && status != ProcessType.FAIL) {
                            continue;
                        }

                        tileAssets.add(TileAsset.builder().name(child.getName()).outputPath("./" + output.getName() + "/" + child.getName())
                                //.size(size)
                                .statusCode(status).date(formattedDateTime).build());
                    } catch (IOException e) {
                        log.error("error", e);
                    }
                }
            }
        }

        tileAssets.sort(Comparator.comparing(TileAsset::getDate));
        Collections.reverse(tileAssets);
        AtomicInteger id = new AtomicInteger(1);
        tileAssets.forEach(tileAsset -> tileAsset.setId(id.getAndIncrement()));
        return tileAssets;
    }

    public List<TileAsset> getListWithPagination(Pagination request, String userId) {
        String assetType = request.getAssetType() == null ? "" : request.getAssetType();
        if (!(assetType.isBlank() || assetType.equals("tileset"))) {
            return new ArrayList<>();
        }

        List<TileAsset> datas = getAllList(request, userId);
        if (datas.isEmpty()) {
            request.setPageNumber(0);
            request.setPageSize(0);
            request.setTotalPages(0);
            request.setTotalElements(0);
            return datas;
        }

        int pageNumber = request.getPageNumber();
        int pageSize = request.getPageSize();
        int totalElements = datas.size();
        int totalPages = (totalElements / pageSize);
        int remainder = totalElements % pageSize;
        if (remainder > 0) {
            totalPages++;
        }

        if (pageNumber > totalPages) {
            pageNumber = totalPages;
        } else if (pageNumber < 1) {
            pageNumber = 1;
        }

        int start = (pageNumber - 1) * pageSize;
        int end = pageNumber * pageSize;
        if (end > totalElements) {
            end = totalElements;
        }

        request.setPageNumber(pageNumber);
        request.setPageSize(pageSize);
        request.setTotalPages(totalPages);
        request.setTotalElements(totalElements);
        return datas.subList(start, end);
    }

    public TileAsset create(TileAssetRequest request) throws IOException {
        TileAsset tileAsset = TileAsset.from(request);
        executionStatusService.setStatus(new File(tileAsset.getOutputPath()), ProcessType.RUNNING);
        tileAsset.setStatusCode(ProcessType.RUNNING);

        if (!USE_DOCKER_MODULE) {
            validateExecute(tileAsset);
        }
        boolean isSuccess = executeProcess(tileAsset);
        ProcessType status = isSuccess ? ProcessType.RUNNING : ProcessType.FAIL;

        executionStatusService.setStatus(new File(tileAsset.getOutputPath()), status);
        tileAsset.setStatusCode(status);
        return tileAsset;
    }

    public TileAsset delete(TileAssetRequest request) {
        request.setOutputPath(DATA_PATH);
        TileAsset tileAsset = TileAsset.from(request);
        File outputDir = getOutputPath(tileAsset.getName());
        if (outputDir.exists() && outputDir.isDirectory()) {
            try {
                FileUtils.deleteDirectory(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tileAsset;
    }

    public boolean executeProcess(TileAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getInputPath();
        String outputPath = tileAsset.getOutputPath();
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);

        /*File javaFile = getJavaPath(JAVA_BIN_PATH);
        ClassPathResource classPathResource = getModulePath();
        File jarFile = getClassPathFile(classPathResource, MODULE_NAME);
        File logFile = new File(outputDir, LOG_FILE_NAME);*/
        File terrainFile = getGeoTiff(inputDir);

        List<String> argList = new ArrayList<>();

        if (USE_DOCKER_MODULE) {
            log.info("===Use Docker Module===");
            log.info("Docker Image : mago-tiler");

            argList.add("docker");
            argList.add("run");
            argList.add("--rm");
            argList.add("-v");
            argList.add(inputDir.getAbsolutePath() + ":/input");
            argList.add("-v");
            argList.add(outputDir.getAbsolutePath() + ":/output");
            argList.add("gaia3d/mago-3d-tiler");

            argList.add("-input");
            argList.add("/input");

            argList.add("-output");
            argList.add("/output");

            argList.add("-log");
            argList.add("/output/log.txt");

            argList.add("-refineAdd");
        } else {
            File javaFile = getJavaPath(JAVA_BIN_PATH);
            ClassPathResource classPathResource = getModulePath();
            File jarFile = getClassPathFile(classPathResource, MODULE_NAME);
            File logFile = new File(outputDir, LOG_FILE_NAME);

            argList.add(javaFile.getAbsolutePath());

            argList.add("-jar");
            argList.add(jarFile.getAbsolutePath());

            argList.add("-input");
            argList.add(inputDir.getAbsolutePath());

            argList.add("-output");
            argList.add(outputDir.getAbsolutePath());

            argList.add("-log");
            argList.add(logFile.getAbsolutePath());
        }

        argList.add("-recursive");

        if (terrainFile != null) {
            argList.add("-terrain");
            argList.add(terrainFile.getAbsolutePath());
        }

        if (tileAsset.getInputFormat() != null && !tileAsset.getInputFormat().isEmpty()) {
            argList.add("-inputType");
            argList.add(tileAsset.getInputFormat());
        }

        if (tileAsset.getOutputFormat() != null && !tileAsset.getOutputFormat().isEmpty()) {
            argList.add("-outputType");
            argList.add(tileAsset.getOutputFormat());
        }

        if (tileAsset.getDebugMode() != null && tileAsset.getDebugMode()) {
            argList.add("-debug");
        }

        if (tileAsset.getRotateX() != null && !tileAsset.getRotateX().isEmpty()) {
            argList.add("-rotateX");
            argList.add(tileAsset.getRotateX());
        }

        if (tileAsset.getPointRatio() != null && !tileAsset.getPointRatio().isEmpty()) {
            argList.add("-pointRatio");
            argList.add(tileAsset.getPointRatio());
        }

        /*if (tiler.getSwapUpAxis() != null && tiler.getSwapUpAxis()) {
            argList.add("-swapUpAxis");
        }

        if (tiler.getFlipUpAxis() != null && tiler.getFlipUpAxis()) {
            argList.add("-flipUpAxis");
        }

        if (tiler.getPointSkip() != null && !tiler.getPointSkip().isEmpty()) {
            argList.add("-pointSkip");
            argList.add(tiler.getPointSkip());
        }

        if (tiler.getNameColumn() != null && !tiler.getNameColumn().isEmpty()) {
            argList.add("-nameColumn");
            argList.add(tiler.getNameColumn());
        }*/

        if (tileAsset.getHeightColumn() != null && !tileAsset.getHeightColumn().isEmpty()) {
            argList.add("-heightColumn");
            argList.add(tileAsset.getHeightColumn());
        }

        if (tileAsset.getAltitudeColumn() != null && !tileAsset.getAltitudeColumn().isEmpty()) {
            argList.add("-altitudeColumn");
            argList.add(tileAsset.getAltitudeColumn());
        }

        if (tileAsset.getMinLevel() != null && !tileAsset.getMinLevel().isEmpty()) {
            argList.add("-minLod");
            argList.add(tileAsset.getMinLevel());
        }

        if (tileAsset.getMaxLevel() != null && !tileAsset.getMaxLevel().isEmpty()) {
            argList.add("-maxLod");
            argList.add(tileAsset.getMaxLevel());
        }

        if (tileAsset.getProjCode() != null && !tileAsset.getProjCode().isEmpty()) {
            argList.add("-proj");
            argList.add(tileAsset.getProjCode());
        } else if (tileAsset.getCrs() != null && !tileAsset.getCrs().isEmpty()) {
            argList.add("-crs");
            argList.add(tileAsset.getCrs());
        }

        String[] args = argList.toArray(new String[0]);

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);

        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg).append(" ");
        }
        String command = stringBuilder.toString();

        ConversionProcess conversionProcess = new ConversionProcess();
        conversionProcess.setIndex(0);
        conversionProcess.setName(inputDir.getName());
        conversionProcess.setStartTime(LocalDateTime.now());
        conversionProcess.setStatus(ProcessType.READY);
        conversionProcess.setInputPath(inputPath);
        conversionProcess.setOutputPath(outputPath);
        List<ConversionProcess> processList = executionStatusService.getProcessList();
        processList.add(conversionProcess);

        SseEmitter emitter = notificationService.getEmitter("user00");
        NotificationResponse firstNotificationResponse = NotificationResponse.builder()
                .name(outputDir.getName())
                .status("READY")
                .type("3dTiles")
                .conversionProcesses(processList)
                .build();
        if (emitter != null) {
            emitter.send(SseEmitter.event().name("connection").data(firstNotificationResponse).build());
        }

        ExecutionThreadPoolService.execute(() -> {
            conversionProcess.setStatus(ProcessType.RUNNING);
            try {
                log.info("Process Command : {}", command);
                log.info("---Start Tiling Process---");
                //executeProcess(args, inputDir, outputDir);
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

            conversionProcess.setEndTime(LocalDateTime.now());
            //emitter.send(
            File tilesetFile = new File(outputDir, "tileset.json");

            if (!tilesetFile.exists() || !tilesetFile.isFile()) {
                //deleteDirectory(outputDir);
                executionStatusService.setStatus(outputDir, ProcessType.FAIL);
                conversionProcess.setStatus(ProcessType.FAIL);
                try {
                    NotificationResponse notificationResponse = NotificationResponse.builder()
                            .name(outputDir.getName())
                            .status("failed")
                            .type("3dTiles")
                            .conversionProcesses(processList)
                            .build();
                    processList.remove(conversionProcess);
                    if (emitter != null) {
                        emitter.send(SseEmitter.event().name("connection").data(notificationResponse).build());
                    }
                } catch (IOException e) {
                    log.error("error emitter", e);
                }
                throw new TilesInvalidException("Failed to create tileset.json file. Please check the server log.");
            }
            File dataDir = new File(outputDir, "data");
            if (!dataDir.exists() || !dataDir.isDirectory()) {
                //deleteDirectory(outputDir);
                executionStatusService.setStatus(outputDir, ProcessType.FAIL);
                conversionProcess.setStatus(ProcessType.FAIL);
                try {
                    NotificationResponse notificationResponse = NotificationResponse.builder()
                            .name(outputDir.getName())
                            .status("failed")
                            .type("3dTiles")
                            .conversionProcesses(processList)
                            .build();
                    processList.remove(conversionProcess);
                    if (emitter != null) {
                        emitter.send(SseEmitter.event().name("connection").data(notificationResponse).build());
                    }
                } catch (IOException e) {
                    log.error("error emitter", e);
                }
                throw new TilesNotFoundException("Data directory is not found. Please check the arguments and input files/input type.");
            }

            executionStatusService.setStatus(outputDir, ProcessType.SUCCESS);
            conversionProcess.setStatus(ProcessType.SUCCESS);
            try {
                NotificationResponse notificationResponse = NotificationResponse.builder()
                        .name(outputDir.getName())
                        .status("success")
                        .type("3dTiles")
                        .conversionProcesses(processList)
                        .build();
                processList.remove(conversionProcess);
                if (emitter != null) {
                    emitter.send(SseEmitter.event().name("connection").data(notificationResponse).build());
                }
            } catch (IOException e) {
                log.error("error emitter", e);
            }
        });
        return true;
    }

    public void createZip(File inputFolder, File outputZipFile) {
        File tileset = new File(inputFolder, "tileset.json");
        File dataDir = new File(inputFolder, "data");
        File[] dataFiles = dataDir.listFiles();

        List<File> files = new ArrayList<>();
        files.add(tileset);
        assert dataFiles != null;
        Collections.addAll(files, dataFiles);

        byte[] buf = new byte[4096];
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
            for (File file : files) {
                try (FileInputStream in = new FileInputStream(file)) {
                    ZipEntry ze;
                    if (file.equals(tileset)) {
                        ze = new ZipEntry(file.getName());
                    } else {
                        ze = new ZipEntry("data" + File.separator + file.getName());
                    }
                    out.putNextEntry(ze);
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*private String createPath(String outputPath, String userId, String fileName) {
        return outputPath + File.separator + userId + File.separator + fileName;
    }*/

    private void validateExecute(TileAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getInputPath();
        File inputDir = new File(inputPath);
        File javaFile = getJavaPath(JAVA_BIN_PATH);
        ClassPathResource classPathResource = getModulePath();
        File jarFile = getClassPathFile(classPathResource, MODULE_NAME);

        if (!javaFile.exists()) {
            throw new TilesNotFoundException("Java file is not found. Please check the JDK path.");
        }
        if (!jarFile.exists()) {
            throw new TilesNotFoundException("Jar file is not found. Please check the tiler.jar path.");
        }
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            throw new TilesNotFoundException("Input directory is not found. Please check the input path.");
        }
    }

    private File getClassPathFile(ClassPathResource classPathResource, String fileName) throws IOException {
        InputStream inputStream = classPathResource.getInputStream();
        File file = File.createTempFile("temp", fileName);
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return file;
    }

    private File getGeoTiff(File inputDir) {
        File[] files = inputDir.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().endsWith(".tif") || file.getName().endsWith(".tiff") || file.getName().endsWith(".geotiff")) {
                return file;
            }
        }
        return null;
    }
}
