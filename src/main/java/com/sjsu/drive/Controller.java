package com.sjsu.drive;




import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(value = "/api")
@CrossOrigin
public class Controller {


	@Autowired
	QuizService quizService;

	@GetMapping("/hello")
	public String helloWorld(){
		System.out.println("Helo world");
		return "Hello Cloud Quiz Project";
	}

/*	@GetMapping("/clickimage")
	public void WebCameraCapture() throws Exception{

		quizService.WebCameraCapture();
	}

	@GetMapping("/uploadimage")
	public void UploadInputImage()throws Exception {

		quizService.UploadInputImage();
	}

	@GetMapping("/comparefaces")
	public void CompareFaces()throws Exception {

		quizService.CompareFaces();
	}*/

	@PostMapping("/uploadimagetouserdb")
	public boolean uploadImageToUserDB(@RequestPart(value = "file", required = false) MultipartFile file)throws Exception {
		try {
			File imageFile = quizService.convertMultiPartToFile(file);
			quizService.uploadFileTos3bucket(file.getOriginalFilename(), imageFile, "driving-user-db");
		} catch (Exception e){
			System.out.println("Exception in uploadImageToUserDB");
			e.printStackTrace();
		}
		return true;
	}

	@PostMapping("/uploadandcomparefaces")
	public Output uploadAndCompareFaces(@RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
		Output output = null;

		try {
			File imageFile = quizService.convertMultiPartToFile(file);
			quizService.uploadFileTos3bucket(file.getOriginalFilename(), imageFile, "cloud-project-drivetime");
			output = quizService.compareFaces();
			quizService.deleteFile(file.getOriginalFilename());
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();

		}
		return output;
	}

}
