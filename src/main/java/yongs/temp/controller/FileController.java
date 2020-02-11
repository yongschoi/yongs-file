package yongs.temp.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import yongs.temp.service.FileService;
import yongs.temp.vo.FileUploadResponse;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);	
    
    @Autowired
    private FileService service;
    
    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadPath") String uploadPath) {
        String fileName = service.saveFile(file, uploadPath);
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/downloadFile/")
                                .path(fileName)
                                .toUriString();
        
        return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
 
    } 
    /*
    @PostMapping("/file/uploadMultiple")
    public List<FileUploadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files){
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }
    */ 
   
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filePath") String filePath, HttpServletRequest request){
    	// Load file as Resource
        Resource resource = service.loadFileAsResource(filePath);
 
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }
 
        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }
 
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
	
	@PostMapping("/list")
	public List<String> getRootFileList(@RequestParam("upPath") String dir) throws Exception{
		logger.debug("yongs-file|FileController|getRootFileList({})", dir); 
		return service.listFilesUsingDirectoryStream(dir); 
	}
}