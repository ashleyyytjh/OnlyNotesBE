package cs302.notes.service.services;

import cs302.notes.exceptions.InternalServerError;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {
    void deleteAllFiles(String clientId) throws InternalServerError;
    String uploadFile(MultipartFile file, String fkAccountOwner);
}
