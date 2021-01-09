package fr.thomah.souvenirs.api;

import fr.thomah.souvenirs.api.exception.BadRequestException;
import fr.thomah.souvenirs.api.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
public class FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileRepository fileRepository;

    @Value("${fr.thomah.souvenirs.api.storage}")
    private String storageFolder;

    @RequestMapping(value = "/files", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileEntity> list() {
        return fileRepository.findAll();
    }

    @RequestMapping(value = "/img/{id}.{extension}", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    public @ResponseBody byte[] getImage(@PathVariable("id") String id, @PathVariable("extension") String extension) throws IOException {

        // Verify that params are correct
        if(id == null || id.isEmpty() || extension == null || extension.isEmpty()) {
            LOGGER.error("Cannot get image : id or extension is incorrect");
            throw new BadRequestException();
        }

        Optional<FileEntity> optionalFile = fileRepository.findById(id);
        if(optionalFile.isPresent()) {
            FileEntity file = optionalFile.get();
            String fullPath = storageFolder + File.separator + file.getDirectory() + File.separator + file.getId() + "." + file.getExtension();
            return Files.readAllBytes(Paths.get(fullPath));
        } else {
            throw new NotFoundException();
        }
    }

    @RequestMapping(value = "/files", method = RequestMethod.POST)
    public void upload(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new RuntimeException("You must select the a file for uploading");
        }

        String finalFileName = multipartFile.getOriginalFilename();
        String fullPath;
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

    @RequestMapping(value = "/files/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") String id) {
        Optional<FileEntity> optionalFile = fileRepository.findById(id);
        optionalFile.ifPresent(fileEntity -> fileRepository.delete(fileEntity));
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
