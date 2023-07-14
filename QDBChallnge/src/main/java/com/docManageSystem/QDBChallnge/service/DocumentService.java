package com.docManageSystem.QDBChallnge.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docManageSystem.QDBChallnge.entity.DocumentWithMetadata;
import com.docManageSystem.QDBChallnge.repo.DocumentWithMetadataRepo;
import com.docManageSystem.QDBChallnge.repo.FeignPost;

import antlr.collections.List;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

@Service
public class DocumentService {
	@Autowired
	private DocumentWithMetadataRepo dwmr;
	
	public DocumentWithMetadata uploadDocument(MultipartFile file, DocumentWithMetadata dWMD) throws IOException {
		 
		DocumentWithMetadata docWithMeta = new DocumentWithMetadata();
		docWithMeta.setUserId(dWMD.getUserId());
		docWithMeta.setUserName(dWMD.getUserName());
		docWithMeta.setDescription(dWMD.getUserName());
		docWithMeta.setLibrary(dWMD.getLibrary());
		docWithMeta.setNameOfDocument(file.getOriginalFilename());
		docWithMeta.setPdfFile(CompresserAndDecompressor.compressPdfDoc(file.getBytes()));
		return dwmr.save(docWithMeta);
	}
	
	public boolean deleteDocument(String docName) {
		
		Optional<Long> dId = dwmr.findDidByNameOfDoc(docName);
		System.out.println(dId.get());
		if(dId.isPresent()) {
			dwmr.deleteById(dId.get());
			FeignPost client = Feign.builder()
	                .encoder(new GsonEncoder())
	                .decoder(new GsonDecoder())
	                .retryer(new MyRetryer(100,3))
//	                .target(FeignPost.class, "https://my-json-server.typicode.com");
	                .target(FeignPost.class, "http://localhost:3000");
			client.DeletePost(dId.get());
			return true;
		} else {
			return false;
		}
		
	}
	
	public byte[] downloadDocument(String fileName){
        Optional<DocumentWithMetadata> docData = dwmr.findByNameOfDocument(fileName);
        return CompresserAndDecompressor.decompressPdfDoc(docData.get().getPdfFile());
    }
	
	public Set<Optional<DocumentWithMetadata>> getListofDocForUser(String userName){
		Set<Optional<DocumentWithMetadata>>  setOfDocs = dwmr.findAllByUserName(userName);
		return setOfDocs;
		
	}

	public void updateDocumentBydId(MultipartFile file, Long dId) throws IOException {
		// TODO Auto-generated method stub
		dwmr.updateDocumentBydId(file.getBytes(), dId);
	}

	
}