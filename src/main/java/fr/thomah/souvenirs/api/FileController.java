package fr.thomah.souvenirs.api;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;

@RestController
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileRepository fileRepository;

    @Value("${fr.thomah.souvenirs.api.storage}")
    private String storageFolder;

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<FileEntity> getPaginated(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {

        // Verify that params are correct
        if(offset < 0 || limit <= 0) {
            LOGGER.error("Cannot get paginated entities : offset or limit is incorrect");
            throw new BadRequestException();
        }

        // Get and transform contents
        Pageable pageable = PageRequest.of(offset, limit, Sort.by("createdAt").descending());
        return fileRepository.findAll(pageable);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void upload(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new RuntimeException("You must select the a file for uploading");
        }

        String finalFileName = multipartFile.getOriginalFilename();
        String fullPath = Strings.EMPTY;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            LOGGER.debug("inputStream: " + inputStream);
            String originalName = multipartFile.getOriginalFilename();
            LOGGER.debug("originalName: " + originalName);
            String contentType = multipartFile.getContentType();
            LOGGER.debug("contentType: " + contentType);
            long size = multipartFile.getSize();
            LOGGER.debug("size: " + size);
            String extension = MimeTypes.getDefaultExt(contentType);
            LOGGER.debug("extension: " + extension);

            if(!isValidExtension(extension)) {
                throw new BadRequestException();
            }

            LocalDate now = LocalDate.now();
            String directoryPath = String.format("%d" + File.separator + "%d", now.getYear(), now.getMonth().getValue());
            prepareDirectories(directoryPath);

            FileEntity entity = new FileEntity();
            entity.setOriginalName(originalName);
            entity.setExtension(extension);
            entity.setDirectory(directoryPath);
            entity = fileRepository.save(entity);

            fullPath = storageFolder + File.separator + directoryPath + File.separator + entity.getId() + "." + extension;
            File file = new File(fullPath);
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(multipartFile.getBytes());
            }

        } catch (IOException e) {
            LOGGER.error("Cannot save file {}", finalFileName, e);
        }

    }

    private boolean isValidExtension(String extension) {
        return switch (extension) {
            case "jpg", "gif", "png" -> true;
            default -> false;
        };
    }

    private void prepareDirectories(String directoryPath) {
        File directory = new File(storageFolder);
        if (!directory.exists()) {
            LOGGER.info("Creating path {}", directory.getPath());
            if (!directory.isDirectory()) {
                LOGGER.error("The path {} is not a directory", directory.getPath());
            }
        }
        if (!directory.isDirectory()) {
            LOGGER.error("The path {} is not a directory", directory.getPath());
        }

        if (directoryPath != null && !directoryPath.isEmpty()) {
            directory = new File(storageFolder + File.separator + directoryPath.replaceAll("/", File.separator));
            LOGGER.debug("Prepare directory {}", directory.getAbsolutePath());
            if (!directory.exists()) {
                LOGGER.info("Creating path {}", directory.getPath());
                if(!directory.mkdirs()) {
                    LOGGER.error("Cannot create directory {}", directory.getAbsolutePath());
                }
            }
            if (!directory.isDirectory()) {
                LOGGER.error("The path {} is not a directory", directory.getPath());
            }
        }
    }

}
