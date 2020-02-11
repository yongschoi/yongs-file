package yongs.temp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import yongs.temp.config.FileControlConfig;
import yongs.temp.exception.FileException;

@Service
public class FileService {
	private final Path fileLocation;
	
    @Autowired
    public FileService(FileControlConfig config) {
        this.fileLocation = Paths.get(config.getUploadRoot()).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileLocation);
        }catch(Exception e) {
            throw new FileException("파일을 업로드할 디렉토리를 생성하지 못했습니다.");
        }
    }
    
    public String saveFile(MultipartFile file, String uploadPath) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // if(uploadPath != "")  <-- 체크하면 안됨.
        if(!uploadPath.equals("")) { // 반드시 equals로 체크해야 함.
        	fileName = uploadPath + "/" + fileName;
        }
             
        try {
            // 파일명에 부적합 문자가 있는지 확인한다.
            if(fileName.contains(".."))
                throw new FileUploadException("파일명에 부적합 문자가 포함되어 있습니다. " + fileName);
            
            Path targetLocation = this.fileLocation.resolve(fileName);          
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
        }catch(Exception e) {
            throw new FileException("["+fileName+"] 파일 업로드에 실패하였습니다. 다시 시도하십시오.");
        }
    }
    
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if(resource.exists()) {
                return resource;
            }else {
                throw new FileException(fileName + " 파일을 찾을 수 없습니다.");
            }
        }catch(MalformedURLException e) {
            throw new FileException(fileName + " 파일을 찾을 수 없습니다.");
        }
    }
    
    public List<String> listFilesUsingDirectoryStream(String dir) throws IOException {
    	Set<String> fileList = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.fileLocation + "/" + dir))) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                	// B --> 디렉토리
                	fileList.add("B" + path.getFileName().toString());
                } else {
                	// C --> 파일
                	fileList.add("C" + path.getFileName().toString());
                }
            }
        }
        List<String> list = fileList.stream().collect(Collectors.toList());
        Collections.sort(list);
        return list;
    }
}
