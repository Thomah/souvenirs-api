package fr.thomah.souvenirs.api;

import fr.thomah.souvenirs.api.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${fr.thomah.souvenirs.api.storage}")
    private String storageFolder;

    @RequestMapping(value = "/files/**", method = RequestMethod.GET, produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public @ResponseBody byte[] getImage(HttpServletRequest request) throws IOException {
        return fileService.readOnFilesystem(request);
    }

    @RequestMapping(value = "/files", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileEntity> list() {
        return fileService.list();
    }

    @RequestMapping(value = "/files", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileEntity uploadImage(
            @RequestParam(value = "directory", required = false) String directory,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam("file") MultipartFile multipartFile) {


        if (multipartFile == null) {
            throw new BadRequestException();
        }

        if(name == null) {
            name = fileService.generateImageName();
        }

        FileEntity entity = new FileEntity();
        try {
            entity = fileService.saveOnFilesystem(multipartFile, directory, name);
            entity = fileService.saveInDb(entity);
        } catch (IOException e) {
            log.error("Cannot save file {}", multipartFile.getName(), e);
        }

        return entity;
    }

    @RequestMapping(value = "/files/{id}", method = RequestMethod.DELETE)
    public Boolean delete(@PathVariable("id") String id) {
        return fileService.delete(id);
    }

}
