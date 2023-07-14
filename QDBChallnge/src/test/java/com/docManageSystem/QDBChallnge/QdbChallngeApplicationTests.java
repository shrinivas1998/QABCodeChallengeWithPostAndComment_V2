package com.docManageSystem.QDBChallnge;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.docManageSystem.QDBChallnge.controller.FileUploadController;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SpringBootTest
class QdbChallngeApplicationTests {
	@Autowired
	private MockMvc mockMvc;
	
	@InjectMocks
	private FileUploadController fileUploadController;
	
	@BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileUploadController)
                .build();
    }
	
	File file = new File("C:\\Users\\894675\\OneDrive - Cognizant\\Desktop\\test.pdf");
	
	@Test
    public void testPostUploadWithAuthentication() throws Exception {
		OkHttpClient client = new OkHttpClient();
        RequestBody builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pdfFile", file.getName(),
                 RequestBody.create(MediaType.parse("pdf"),file))
                .addFormDataPart("userId", "44")
                .addFormDataPart("userName", "shrinivas")
                .addFormDataPart("library", "xinfinity")
                .addFormDataPart("description", "test")
                .build();

        Request request = new Request.Builder()
        	      .url("multipart_url")
        	      .addHeader("Authorization", Credentials.basic("ADMIN", "password"))
        	      .post(builder)
        	      .build();
        Response response = client.newCall(request).execute();
        assertEquals(response.code(),201);
      
        
    }
	
	@Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void testGetDownloadWithAuthentication() throws Exception {
		
        mockMvc.perform(MockMvcRequestBuilders.get("/download/{fileName}")
                .header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46cGFzc3dvcmQ=")) // Base64 encoded "admin:password"
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Add additional assertions if necessary
    }
	@Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void testGetgetAllPostsWithAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/getAllPosts/{name}")
                .header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46cGFzc3dvcmQ=")) // Base64 encoded "admin:password"
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Add additional assertions if necessary
    }
	
	@Test
    @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
    public void testPostCommentWithAuthentication() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/createComment/{pId}/{body}")
                .header(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46cGFzc3dvcmQ=")) // Base64 encoded "admin:password"
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // Add additional assertions if necessary
    }

                 

}
