package com.hacettepe.iwms.service;

import com.hacettepe.iwms.config.FileStorageProperties;
import com.hacettepe.iwms.exception.FileStorageException;
import com.hacettepe.iwms.exception.ResourceNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String store(MultipartFile file, String subDirectory) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(subDirectory);
            if (!Files.exists(targetLocation)) {
                Files.createDirectories(targetLocation);
            }
            Path filePath = targetLocation.resolve(originalFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return originalFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.fileStorageLocation, 1)
                    .filter(path -> !path.equals(this.fileStorageLocation))
                    .map(this.fileStorageLocation::relativize);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return fileStorageLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename, String subDirectory) {
        try {
            Path filePath = fileStorageLocation.resolve(subDirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File not found " + filename);
        }
    }

    @Override
    public void delete(String filename, String subDirectory) {
        try {
            Path filePath = fileStorageLocation.resolve(subDirectory).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + filename, ex);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage", e);
        }
    }
}
