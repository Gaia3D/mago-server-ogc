package com.gaia3d.mago.server.service;

import com.gaia3d.mago.server.domain.Pagination;
import com.gaia3d.mago.server.domain.SimulationAsset;
import com.gaia3d.mago.server.domain.SimulationAssetRequest;
import com.gaia3d.mago.server.domain.type.ProcessType;
import com.gaia3d.mago.server.exception.TilesNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
public class MagoSimulationService implements DefaultConverterService {
    private final ExecutionStatusService executionStatusService;
    private final NotificationService notificationService;
    private final CommandExecutorService commandExecutorService;

    @Value("${java.path}")
    private String JAVA_BIN_PATH;

    @Value("${data.path}")
    private String DATA_PATH;

    @Value("${asset.path}")
    private String ASSET_PATH;

    @Value("${ogc.module.name}")
    private String OGC_MODULE_NAME;

    @Value("${simulator.module.name}")
    private String SIMULATOR_MODULE_NAME;

    @Value("${tiler.module.name}")
    private String TILER_MODULE_NAME;

    @Value("${use.docker.module}")
    private boolean USE_DOCKER_MODULE;

    private ClassPathResource getOgCModulePath() {
        return new ClassPathResource("modules/" + OGC_MODULE_NAME);
    }

    private ClassPathResource getSimulatorModulePath() {
        return new ClassPathResource("modules/" + SIMULATOR_MODULE_NAME);
    }

    private ClassPathResource getTilerModulePath() {
        return new ClassPathResource("modules/" + TILER_MODULE_NAME);
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

    public void saveTempFiles(List<MultipartFile> multipartFiles, SimulationAssetRequest request) {
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

    public byte[] download(SimulationAssetRequest request) {
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

    public List<SimulationAsset> getAllList(Pagination request, String userId) {
        String assetStatus = request.getAssetStatus() == null ? "" : request.getAssetStatus();

        List<SimulationAsset> tileAssets = new ArrayList<>();
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

                        tileAssets.add(SimulationAsset.builder().name(child.getName()).outputPath("./" + output.getName() + "/" + child.getName())
                                //.size(size)
                                .statusCode(status).date(formattedDateTime).build());
                    } catch (IOException e) {
                        log.error("error", e);
                    }
                }
            }
        }

        tileAssets.sort(Comparator.comparing(SimulationAsset::getDate));
        Collections.reverse(tileAssets);
        AtomicInteger id = new AtomicInteger(1);
        tileAssets.forEach(tileAsset -> tileAsset.setId(id.getAndIncrement()));
        return tileAssets;
    }

    public List<SimulationAsset> getListWithPagination(Pagination request, String userId) {
        String assetType = request.getAssetType() == null ? "" : request.getAssetType();
        if (!(assetType.isBlank() || assetType.equals("simulation"))) {
            return new ArrayList<>();
        }

        List<SimulationAsset> datas = getAllList(request, userId);
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

    public SimulationAsset create(SimulationAssetRequest request) throws IOException {
        SimulationAsset tileAsset = SimulationAsset.from(request);
        executionStatusService.setStatus(new File(tileAsset.getOutputPath()), ProcessType.RUNNING);
        tileAsset.setStatusCode(ProcessType.RUNNING);

        if (!USE_DOCKER_MODULE) {
            validateExecute(tileAsset);
        }

        //boolean isSuccess = executeProcess(tileAsset);
        boolean isSuccess = executePipeline(tileAsset);
        ProcessType status = isSuccess ? ProcessType.SUCCESS : ProcessType.FAIL;

        executionStatusService.setStatus(new File(tileAsset.getOutputPath()), status);
        tileAsset.setStatusCode(status);
        return tileAsset;
    }

    public SimulationAsset delete(SimulationAssetRequest request) {
        request.setOutputPath(DATA_PATH);
        SimulationAsset tileAsset = SimulationAsset.from(request);
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

    public boolean executePipeline(SimulationAsset tileAsset) throws IOException {
        List<String> ogcArguments = createOgcConvertingArguments(tileAsset);
        List<String> simulatorArguments = createSimulatorConvertingArguments(tileAsset);
        List<String> tilesetArguments = createTilesetConvertingArguments(tileAsset);
        boolean result = commandExecutorService.executeProcessWithoutThread(ogcArguments);
        if (!result) {
            return false;
        }
        result = commandExecutorService.executeProcessWithoutThread(simulatorArguments);
        if (!result) {
            return false;
        }
        result = commandExecutorService.executeProcessWithoutThread(tilesetArguments);
        return result;
    }

    public List<String> createOgcConvertingArguments(SimulationAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getInputPath();
        //String outputPath = tileAsset.getOutputPath();
        File inputDir = new File(inputPath);
        File outputDir = new File(inputPath); // outputDir is same as inputDir
        List<String> argList = new ArrayList<>();

        File javaFile = getJavaPath(JAVA_BIN_PATH);
        ClassPathResource classPathResource = getOgCModulePath();
        File jarFile = getClassPathFile(classPathResource, OGC_MODULE_NAME);

        argList.add(javaFile.getAbsolutePath());

        argList.add("-jar");
        argList.add(jarFile.getAbsolutePath());

        argList.add("-input");
        argList.add(inputDir.getAbsolutePath());

        argList.add("-output");
        argList.add(outputDir.getAbsolutePath());

        if (tileAsset.getProjection() != null && !tileAsset.getProjection().isEmpty()) {
            argList.add("-proj");
            argList.add(tileAsset.getProjection());
        }

        return argList;
    }

    public List<String> createSimulatorConvertingArguments(SimulationAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getInputPath();
        String outputPath = tileAsset.getOutputPath();
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);
        List<String> argList = new ArrayList<>();

        File javaFile = getJavaPath(JAVA_BIN_PATH);
        ClassPathResource classPathResource = getSimulatorModulePath();
        File jarFile = getClassPathFile(classPathResource, SIMULATOR_MODULE_NAME);

        argList.add(javaFile.getAbsolutePath());

        argList.add("-jar");
        argList.add(jarFile.getAbsolutePath());

        argList.add("-input");
        argList.add(inputDir.getAbsolutePath());

        argList.add("-output");
        argList.add(outputDir.getAbsolutePath());

        argList.add("-type");
        argList.add("SOUND_SIMULATION");

        return argList;
    }

    public List<String> createTilesetConvertingArguments(SimulationAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getOutputPath();
        String outputPath = tileAsset.getOutputPath();
        File inputDir = new File(inputPath);
        File outputDir = new File(outputPath);
        List<String> argList = new ArrayList<>();

        File javaFile = getJavaPath(JAVA_BIN_PATH);
        ClassPathResource classPathResource = getTilerModulePath();
        File jarFile = getClassPathFile(classPathResource, TILER_MODULE_NAME);

        argList.add(javaFile.getAbsolutePath());

        argList.add("-jar");
        argList.add(jarFile.getAbsolutePath());

        argList.add("-i");
        argList.add(inputDir.getAbsolutePath());

        argList.add("-it");
        argList.add("kml");

        argList.add("-o");
        argList.add(outputDir.getAbsolutePath());

        argList.add("-debug");
        return argList;
    }

    public void createZip(File inputFolder, File outputZipFile) {
        File inputDir = new File(inputFolder.getAbsolutePath());
        List<File> files = (List<File>) FileUtils.listFiles(inputDir, null, true);

        byte[] buf = new byte[4096];
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
            for (File file : files) {
                try (FileInputStream in = new FileInputStream(file)) {
                    ZipEntry ze = new ZipEntry(file.getName());
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

    private void validateExecute(SimulationAsset tileAsset) throws IOException {
        String inputPath = tileAsset.getInputPath();
        File inputDir = new File(inputPath);
        File javaFile = getJavaPath(JAVA_BIN_PATH);

        ClassPathResource classPathOgcResource = getOgCModulePath();
        File ogcJarFile = getClassPathFile(classPathOgcResource, OGC_MODULE_NAME);

        ClassPathResource classPathSimulatorResource = getSimulatorModulePath();
        File simulatorJarFile = getClassPathFile(classPathSimulatorResource, SIMULATOR_MODULE_NAME);

        if (!javaFile.exists()) {
            throw new TilesNotFoundException("Java file is not found. Please check the JDK path.");
        }
        if (!ogcJarFile.exists()) {
            throw new TilesNotFoundException("Jar file is not found. Please check the ogc.jar path.");
        }
        if (!simulatorJarFile.exists()) {
            throw new TilesNotFoundException("Jar file is not found. Please check the simulator.jar path.");
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
}
