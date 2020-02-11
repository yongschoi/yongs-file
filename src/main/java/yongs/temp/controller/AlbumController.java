package yongs.temp.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import yongs.temp.service.AlbumService;
import yongs.temp.util.MediaUtils;
import yongs.temp.vo.FileUploadResponse;

@RestController
@RequestMapping("/album")
public class AlbumController {
    private static final Logger logger = LoggerFactory.getLogger(AlbumController.class);	
    
    @Autowired
    private AlbumService service;

    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadPath") String uploadPath) {
        String fileName = service.saveFile(file, uploadPath);
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/downloadFile/")
                                .path(fileName)
                                .toUriString();
        
        return new FileUploadResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
 
    } 
    
	@PostMapping("/list")
	public List<String> getAlbumList(@RequestParam("upPath") String dir) throws Exception{
		logger.debug("yongs-file|AlbumController|getAlbumList({})", dir); 
		return service.listFilesUsingDirectoryStream(dir); 
	}
	
	@GetMapping("/displayImg")
	public ResponseEntity<byte[]> displayFile(@RequestParam("name") String fileName)throws Exception{
		InputStream in = null;
		ResponseEntity<byte[]> entity = null;
		String albumRoot = service.getFileLocation().toString();
		try {
			String formatName = fileName.substring(fileName.lastIndexOf(".")+1);
			MediaType mType = MediaUtils.getMediaType(formatName);
			HttpHeaders headers = new HttpHeaders();
			in = new FileInputStream(albumRoot + "\\" + fileName);
			
			//step: change HttpHeader ContentType
			if(mType != null) {
				//image file(show image)
				headers.setContentType(mType);
			}else {
				//another format file(download file)
				fileName = fileName.substring(fileName.indexOf("_") + 1);//original file Name
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				headers.add("Content-Disposition", "attachment; filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1")+"\""); 
			}
			
			entity = new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
			
		} catch(Exception e) {
			e.printStackTrace();
			entity = new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		} finally {
			in.close();
		}
		
		return entity;	
	}
}