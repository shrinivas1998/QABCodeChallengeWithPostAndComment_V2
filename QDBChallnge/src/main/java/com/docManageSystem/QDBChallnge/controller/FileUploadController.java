package com.docManageSystem.QDBChallnge.controller;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.docManageSystem.QDBChallnge.Exceptions.FileNameAlreadyExistException;
import com.docManageSystem.QDBChallnge.Exceptions.NoFileFoundWithNameException;
import com.docManageSystem.QDBChallnge.Exceptions.NoSuchFileFoundException;
import com.docManageSystem.QDBChallnge.Exceptions.NonPDfFileFoundException;
import com.docManageSystem.QDBChallnge.entity.Comment;
import com.docManageSystem.QDBChallnge.entity.DocumentWithMetadata;
import com.docManageSystem.QDBChallnge.entity.Post;
import com.docManageSystem.QDBChallnge.repo.FeignPost;
import com.docManageSystem.QDBChallnge.service.DocumentService;
import com.docManageSystem.QDBChallnge.service.MyRetryer;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

@RestController
public class FileUploadController {
	@Autowired
	public DocumentService documentService;
	
//	@ResponseStatus(value = HttpStatus.OK)
//	@PostMapping("/upload")
//	public HttpStatus uploadImage(@RequestParam("pdfFile")MultipartFile file) throws IOException{
//		if(!file.getOriginalFilename().contains(".pdf")) {
//			return HttpStatus.NOT_ACCEPTABLE;
//		}
//		documentService.uploadDocument(file);
//		return HttpStatus.CREATED;
//	}
	private static final Logger LOGGER = LogManager.getLogger(FileUploadController.class);
	
	@PostMapping("/upload")
//	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> uploadDocument(@RequestParam("pdfFile")MultipartFile file,@RequestParam("userId")String userId, @RequestParam("userName")String userName
			,@RequestParam("library")String library,@RequestParam("description")String description) throws IOException{
		try {
		
		if(!file.getOriginalFilename().contains(".pdf")) {
			throw new NonPDfFileFoundException("Non pdf file found");
		}
		DocumentWithMetadata dWMD = new DocumentWithMetadata();
		dWMD.setUserId(userId);
		dWMD.setUserName(userName);
		dWMD.setLibrary(library);
		dWMD.setDescription(description);
		DocumentWithMetadata dWMDfromDB = documentService.uploadDocument(file,dWMD);
		
		FeignPost client = feignClient();

//		DocumentWithMetadata data = new DocumentWithMetadata();
	        // Set the necessary data properties
		System.out.println(dWMDfromDB.getdId());
		System.out.println(dWMDfromDB.getDescription());	
	    client.postData(dWMDfromDB.getdId(),dWMDfromDB.getNameOfDocument());
		System.out.println(file.getOriginalFilename());
			LOGGER.info("Content is created");
			return ResponseEntity.status(HttpStatus.CREATED).body("Entry is saved in DB");
		}
		catch(NonPDfFileFoundException ex) {
			 LOGGER.error(ex.getMessage());
			 return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Non pdf file is uploded");
		}  
		catch(FileNameAlreadyExistException ex) {
			 LOGGER.error("File already exisit in h2 DB");
			 return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body("Name with doc alredy exist");
		} 
		catch(Exception ex) {
			LOGGER.error("Internal server error");
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("some error happned");
		}
		
	}
	private FeignPost feignClient() {
		FeignPost client = Feign.builder()
	                .encoder(new GsonEncoder())
	                .decoder(new GsonDecoder())
	                .retryer(new MyRetryer(100,3))
//	                .target(FeignPost.class, "https://my-json-server.typicode.com");
	                .target(FeignPost.class, "http://localhost:3000");
		return client;
	}
	@GetMapping("/download/{fileName}")
//	@PreAuthorize("hasAuthority('ADMIN')")
	public ResponseEntity<?> downloadDocument(@PathVariable String fileName) {
		try {
			byte[] pdf = documentService.downloadDocument(fileName);
			LOGGER.info("Content is found and returned");
			return ResponseEntity.status(HttpStatus.OK).body(pdf);
			
		}
		catch(NoSuchFileFoundException ex) {
			 LOGGER.error("No such file name found");
			 return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Entry is not found in DB");
		}
		catch(Exception ex) {
			LOGGER.error("Internal server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("some error happned");
		}
		
	}

	
	@DeleteMapping("/delete/{docName}")
	public ResponseEntity<?>  deleteDocumentByName(@PathVariable String docName) {
		try {
			if(documentService.deleteDocument(docName)) {
				LOGGER.info("Content is deleted from DB");
				return ResponseEntity.status(HttpStatus.CREATED).body("Entry is deleted in DB");
			} else {
				LOGGER.info("Content is not found in DB");
				throw new NoFileFoundWithNameException(" No file found with name "+docName);
			}
		}catch(NoFileFoundWithNameException ex) {
			LOGGER.error("Internal server error");
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("file is not found happned");
		}
		catch(Exception ex) {
			LOGGER.error("Internal server error");
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("some error happned");
		}
		
	}
	@GetMapping("/getAllDocs/{userName}")
	public ResponseEntity<?> getAlldocByUserName(@PathVariable String userName){
		try {
			Set<Optional<DocumentWithMetadata>> listofDocForUser = documentService.getListofDocForUser(userName);
			if(listofDocForUser.size()<=0) {
				LOGGER.info("Content is not found in DB");
				return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Entry is not found in DB");
			}else {
				LOGGER.info("Content is found and returned ");
				return ResponseEntity.status(HttpStatus.OK).body(listofDocForUser);
			}
		}  catch(Exception ex) {
			LOGGER.error("Internal server error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("some error happned");
		}
	
	}
	@GetMapping("/getAllPosts")
	public Set<Post> getAllposts(){

		try {
			FeignPost client = feignClient();
		
		Set<Post> setPost = client.getPosts();
		LOGGER.info("All posts are retrived");
		return setPost;
		} catch(Exception ex) {
			LOGGER.error("internal error happned ");
			ex.printStackTrace();
			
		}
		return null;
	}
	
	@PostMapping("/createComment/{pId}/{body}")
	public ResponseEntity<?> postComment(@PathVariable("pId") Long pId,@PathVariable("body") String body) {

		try {
			FeignPost client = feignClient();
			client.postComment(body, pId);
			LOGGER.info("Comment is created ");
			return ResponseEntity.status(HttpStatus.CREATED).body("Entry is deleted in DB");
		} catch(Exception ex) {
			LOGGER.error("Internal server error");
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("some error happned");
		
	}
	
	@PutMapping("/updateDocument/{dId}")
	public void updatePdfFile(@RequestParam("pdfFile")MultipartFile file, @PathVariable("dId") Long dId) throws IOException {
		try {
			documentService.updateDocumentBydId(file,dId);
			LOGGER.info("Doc is updated ");
		} catch(Exception ex) {
			LOGGER.error("Internal server error");
		}
	}
	
	
	@GetMapping("/getAllComments/{pId}")
	public Set<Comment> getAllComments(@PathVariable("pId") Long pId){

		try {
			FeignPost client = feignClient();
			
		Set<Comment> setComment = client.getCommentsPerPost(pId);
		LOGGER.info("all comments are retrived ");
		return setComment;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
