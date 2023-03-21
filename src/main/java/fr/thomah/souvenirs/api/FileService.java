package fr.thomah.souvenirs.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Service
public class FileService {

    @Value("${fr.thomah.souvenirs.api.storage.root}")
    private String rootStorageFolder;

    @Value("${fr.thomah.souvenirs.api.storage.data}")
    private String dataStorageFolder;

    @Value("${fr.thomah.souvenirs.api.url}")
    private String coreUrl;

    @Autowired
    private FileRepository fileRepository;

    public byte[] readOnFilesystem(HttpServletRequest request) throws IOException {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String matchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String pathFile = new AntPathMatcher().extractPathWithinPattern(matchPattern, path);
        pathFile = pathFile.replaceAll("/", Matcher.quoteReplacement(File.separator));
        InputStream in = new FileInputStream(rootStorageFolder + File.separator + dataStorageFolder + File.separator + pathFile);
        log.debug("Getting image {}", rootStorageFolder + File.separator + dataStorageFolder + File.separator + pathFile);
        byte[] fileContent = IOUtils.toByteArray(in);
        in.close();
        return fileContent;
    }

    public List<FileEntity> list() {
        return fileRepository.findAll();
    }

    public FileEntity saveOnFilesystem(MultipartFile multipartFile, String directory, String name) throws IOException {

        String fullPath;
        String finalFileName;

        // Extract data from MultipartFile
        InputStream inputStream = multipartFile.getInputStream();
        log.debug("inputStream: " + inputStream);
        String originalName = multipartFile.getOriginalFilename();
        log.debug("originalName: " + originalName);
        String contentType = multipartFile.getContentType();
        log.debug("contentType: " + contentType);
        long size = multipartFile.getSize();
        log.debug("size: " + size);
        String format = MimeTypes.getDefaultExt(contentType);
        log.debug("format: " + format);
        finalFileName = name + "." + format;
        log.debug("saved filename: " + finalFileName);

        // Write file on filesystem
        prepareDirectories(directory);
        fullPath = getDirectoryPath(directory) + java.io.File.separator + finalFileName;
        java.io.File file = new java.io.File(fullPath);
        FileOutputStream os = new FileOutputStream(file);
        os.write(multipartFile.getBytes());
        os.close();

        // Create entity to return
        FileEntity entity = new FileEntity();
        entity.setName(name);
        entity.setFormat(format);

        if(directory != null) {
            entity.setDirectory(directory);
            entity.setUrl(coreUrl + "/files/" + directory + "/" + finalFileName);
        } else {
            entity.setDirectory("");
            entity.setUrl(coreUrl + "/files/" + finalFileName);
        }

        return entity;
    }

    public FileEntity saveInDb(FileEntity newEntity) {
        FileEntity entity = null;
        if(newEntity.getId() == null || newEntity.getId().isEmpty()) {
            entity = fileRepository.findById(newEntity.getId()).orElse(null);
        }
        if(entity == null) {
            entity = new FileEntity();
        }
        entity.setDirectory(newEntity.getDirectory());
        entity.setName(newEntity.getName());
        entity.setFormat(newEntity.getFormat());
        entity.setUrl(newEntity.getUrl());
        return fileRepository.save(entity);
    }

    public Boolean delete(String id) {
        Optional<FileEntity> optionalFile = fileRepository.findById(id);
        optionalFile.ifPresent(fileEntity -> {
            File file = new File(getPath(fileEntity));
            if(file.delete()) {
                fileRepository.delete(fileEntity);
            }
        });
        return true;
    }

    public String generateImageName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return "IMG_" + formatter.format(now);
    }

    private void prepareDirectories(String directoryPath) {
        File directory = new File(getDirectoryPath(directoryPath));
        if (!directory.exists()) {
            log.info("Creating path {}", directory.getPath());
            if(!directory.mkdirs()) {
                log.error("Cannot create directory {}", directory.getAbsolutePath());
            }
        }
        if (!directory.isDirectory()) {
            log.error("The path {} is not a directory", directory.getPath());
        }
    }

    private String getDirectoryPath(String directory) {
        String withDirectory = "";
        if(directory != null) {
            withDirectory = File.separator + directory.replaceAll("//", File.separator);
        }
        return rootStorageFolder + java.io.File.separator + dataStorageFolder + withDirectory;
    }

    private String getPath(FileEntity entity) {
        return getDirectoryPath(entity.getDirectory()) + java.io.File.separator + entity.getFullname();
    }

}
