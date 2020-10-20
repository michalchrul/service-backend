package scc.ctrls;

import scc.utils.Hash;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.microsoft.azure.storage.StorageException;


import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import scc.MediaService;
import scc.StringObj;
import scc.UploadFileResponse;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;



@RestController
@Path("/media")
public class MediaCtrl {

    //TODO: Method to be removed
    @Path("/x")
    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    public String hello() {
        return "v: 0014";
    }

    @PostMapping
    @Path("/")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)

    public String uploadBytes( byte[] contents) {
        String storageConnectionString =
                "DefaultEndpointsProtocol=https;AccountName=scc1920;AccountKey=suTNjWBGcTDCB60EJ6YEngRIefzRbC9BbkLXl+yAzgWOYCnDuutwgvWWArdppu8erq7dLhyOk6DAkaXcr5GOGg==;EndpointSuffix=core.windows.net";
        try {
            String id = Hash.of(contents);
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference("images");
            CloudBlob blob = container.getBlockBlobReference( id);
            blob.uploadFromByteArray(contents, 0, contents.length);
            return id;
        } catch( Exception e) {
            return e.getMessage();
        }
    }

//    @GetMapping
//    @Path("/{id}")
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    public byte[] download( @PathParam("id") String id) {
//        String storageConnectionString =
//                "DefaultEndpointsProtocol=https;AccountName=scc58558;AccountKey=WUrddNXEXJX4NxYcS5nFbhqfw+plD8PEctixa0P7U9Sp1NJirH+StW+0wimi8MpP9GtIlqKfVndLNe6mzYJ05Q==;EndpointSuffix=core.windows.net";
//
//        try {
//            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
//            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
//            CloudBlobContainer container = blobClient.getContainerReference("images");
//            CloudBlob blob = container.getBlobReferenceFromServer( id);
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            blob.download(out);
//            out.close();
//            return out.toByteArray();
//        } catch( Exception e) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//
//
//    }

    private static final Logger logger = LoggerFactory.getLogger(MediaCtrl.class);

    @Autowired
    private MediaService fileStorageService;

    @GetMapping
    public HttpEntity download() {
        StringObj result = new StringObj();
        result.setHello("Hello world !!!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=storage1920;AccountKey=Zgo5bykmGxufmew62KYtYGPqV+deZZVdOowEUaz2p1gNm4DmZwruE0afEy/lHyjiQlIvN1M7s7i3iCMhQxN2LQ==;EndpointSuffix=core.windows.net";
        CloudStorageAccount storageAccount = null;
        try {
            storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference("images");
            // Get reference to blob
            CloudBlob blob = container.getBlockBlobReference( file.getOriginalFilename());
            blob.getProperties().setContentType(file.getContentType());
            // Upload contents from byte array (check documentation for other alternatives)
            blob.uploadFromByteArray(file.getBytes(), 0, file.getBytes().length);
        } catch (URISyntaxException | StorageException | IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        //String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/media/downloadFile/")
                .path(file.getOriginalFilename())
                .toUriString();

        return new UploadFileResponse(file.getOriginalFilename(), fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        //create client to blob storage
        String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=storage1920;AccountKey=Zgo5bykmGxufmew62KYtYGPqV+deZZVdOowEUaz2p1gNm4DmZwruE0afEy/lHyjiQlIvN1M7s7i3iCMhQxN2LQ==;EndpointSuffix=core.windows.net";
        CloudStorageAccount storageAccount = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] contents = null;
        try {
            storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            CloudBlobContainer container = blobClient.getContainerReference("images");
            // Get reference to blob
            CloudBlob blob = container.getBlobReferenceFromServer(fileName);
            byteArrayOutputStream = new ByteArrayOutputStream();
            blob.download(byteArrayOutputStream);
            byteArrayOutputStream.close();
            contents = byteArrayOutputStream.toByteArray();
        } catch (URISyntaxException | StorageException | IOException e) {
            e.printStackTrace();
            logger.info("probably storage exception !");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        Resource byteArrayResource = new ByteArrayResource(contents);

        // Try to determine file's content type
        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(byteArrayResource);
    }

}
