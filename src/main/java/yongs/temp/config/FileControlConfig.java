package yongs.temp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("filecontrol")
public class FileControlConfig {
    private String uploadRoot;
    private String albumRoot;
    
	public String getUploadRoot() {
		return uploadRoot;
	}
	public void setUploadRoot(String uploadRoot) {
		this.uploadRoot = uploadRoot;
	}
	public String getAlbumRoot() {
		return albumRoot;
	}
	public void setAlbumRoot(String albumRoot) {
		this.albumRoot = albumRoot;
	}
}
