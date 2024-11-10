package cs302.notes.service.serviceImpl;

import cs302.notes.exceptions.FileNotConvertedException;
import cs302.notes.exceptions.InternalServerError;
import cs302.notes.exceptions.InvalidFileTypeException;
import cs302.notes.service.services.StorageService;
import cs302.notes.utils.AcceptedValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

@Service
public class StorageServiceImpl implements StorageService {

    private final List<String> ACCEPTED_FILE_EXTENSIONS;
    private final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Value("${aws.s3.bucketName}")
    private String bucketName;
    private final S3Client s3Client;

    public StorageServiceImpl(AcceptedValues acceptedValues, S3Client s3Client) {
        this.ACCEPTED_FILE_EXTENSIONS = acceptedValues.getMediaList();
        this.s3Client = s3Client;
    }

    public void deleteFile(String clientId, String url) throws InternalServerError {
        try {
            String path = new URL(url).getPath();
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            logger.info(String.format("Storage Service deleted %s.", path));
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            logger.warn(String.format("Internal Server Error when deleting file: %s", e.getMessage()));
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public void deleteAllFiles(String clientId) throws InternalServerError {
        try {
            ListObjectsRequest listRequest = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .prefix(clientId + "/")
                    .build();

            ListObjectsResponse listResponse = s3Client.listObjects(listRequest);
            if (!listResponse.contents().isEmpty()) {
                // Delete all contents of the bucket
                List<S3Object> objList = listResponse.contents();
                for (S3Object obj : objList) {
                    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(obj.key())
                            .build();
                    logger.info(String.format("Storage Service deleted %s.", obj.key()));
                    s3Client.deleteObject(deleteRequest);
                }
            }
        } catch (Exception e) {
            logger.warn(String.format("Internal Server Error when deleting file: %s", e.getMessage()));
            throw new InternalServerError(e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String clientId) throws InternalServerError {
        String secureFileName = getSecureFileName(file.getOriginalFilename());
        File convertedFile = convertMultiPartFileToFile(file, secureFileName);
        String key = clientId + "/" + System.currentTimeMillis() + "_" + secureFileName;

        try {
            // Puts object in the uploadFile
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.putObject(request, Path.of(convertedFile.toURI()));
            convertedFile.delete();
        } catch (Exception e) {
            convertedFile.delete();
            logger.warn(String.format("Internal Server Error when uploading file: %s", e.getMessage()));
            throw new InternalServerError(e.getMessage());
        }

        // Obtain url from s3
        GetUrlRequest request = GetUrlRequest.builder().bucket(bucketName ).key(key).build();

        // Return secure filename
        return s3Client.utilities().getUrl(request).toExternalForm();
    }

    private String getSecureFileName(String originalFilename) {
        if (originalFilename == null) { originalFilename = "notes.pdf"; }
        // Extract out filename and extension
        String filenameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // Validate filename and extension
        String secureFileName = filenameWithoutExtension.replace(" ", "").replaceAll("[^a-zA-Z0-9]+","");
        verifyFileExtension(extension);
        return secureFileName + extension;
    }

    private void verifyFileExtension(String extension) throws InvalidFileTypeException {
        if (!ACCEPTED_FILE_EXTENSIONS.contains(extension)) {
            logger.warn(String.format("InvalidFileTypeException: %s", extension));
            throw new InvalidFileTypeException(extension);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file, String secureFileName) throws FileNotConvertedException {

        File convertedFile = new File("uploads/" + secureFileName);
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            logger.warn(String.format("FileNotConvertedException: %s", e.getMessage()));
            throw new FileNotConvertedException(file.getOriginalFilename());
        }
        return convertedFile;
    }
}
