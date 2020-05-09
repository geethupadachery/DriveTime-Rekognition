package com.sjsu.drive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;

import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.github.sarxos.webcam.Webcam;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Service
public class QuizService {

	@Value("${cloud.aws.credentials.accessKey}")
	private String key;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	@Value("${AWS_BUCKET}")
	private String AWS_BUCKET="cloud-project-drivetime";

	S3Client s3Client;
	AmazonS3 s3;

	String bucket1 = "driving-user-db";
	String bucket2 = "cloud-project-drivetime";

	//String keyName = "input1.png";

	//String filePath = "/Users/geethu/Projects/myWorkspace/aws-rekognition/src/main/resources/tmp/input1.png";
	//String amazonFileUploadLocationOriginal=AWS_BUCKET;

	//private static AmazonRekognition rekognitionClient;

	@PostConstruct
	public void initialize() {
		System.out.println("Inside initialize 1");
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(key, secretKey);
		System.out.println("Inside initialize 2");
		s3Client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
				.region(Region.US_WEST_1).build();
		System.out.println("Inside initialize 3");
		AWSCredentials cred = new BasicAWSCredentials(key, secretKey);
		s3 = new AmazonS3Client(cred);
		System.out.println("Inside initialize 4");

	}


/*
	public void WebCameraCapture()throws IOException {
		Webcam webcam = Webcam.getDefault();
		webcam.open();
		ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));

	}



	public void UploadInputImage()throws Exception {
		FileInputStream stream = new FileInputStream(filePath);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		PutObjectRequest putObjectRequest = new PutObjectRequest(amazonFileUploadLocationOriginal, keyName, stream, objectMetadata);
		PutObjectResult result = s3.putObject(putObjectRequest);
		System.out.println("Etag:" + result.getETag() + "-->" + result);

	}*/



	public Output compareFaces() {
		Output output = null;
		ArrayList<String> inputimage = new ArrayList();
		ArrayList<String> listofdbimages = new ArrayList();
		//bucket with list of bd images to be compared
		ListObjectsV2Request req1 = new ListObjectsV2Request().withBucketName(bucket1).withDelimiter("/");
		ListObjectsV2Result listing1 = s3.listObjectsV2(req1);


		List<S3ObjectSummary> objects1 = listing1.getObjectSummaries();
		for (S3ObjectSummary os: objects1) {
			listofdbimages.add(os.getKey());
			System.out.println("222 "+os.getKey());

		}
		//bucket with input image
		ListObjectsV2Request req2 = new ListObjectsV2Request().withBucketName(bucket2).withDelimiter("/");
		ListObjectsV2Result listing2 = s3.listObjectsV2(req2);


		List<S3ObjectSummary> objects2 = listing2.getObjectSummaries();
		for (S3ObjectSummary os: objects2) {
			inputimage.add(os.getKey());
			System.out.println("333 "+os.getKey());

		}

		AmazonRekognition rekognitionClient;// = AmazonRekognitionClientBuilder.standard().withRegion("us-west-1").build();

		rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(key,secretKey)))
				.withRegion("us-west-1").build();

		for(String i: listofdbimages) {
			CompareFacesRequest compareFacesRequest = new CompareFacesRequest().withSourceImage(new Image()
					.withS3Object(new S3Object()
							.withName(inputimage.get(0)).withBucket(bucket2))).withTargetImage(new Image()
					.withS3Object(new S3Object()
							.withName(i).withBucket(bucket1))).withSimilarityThreshold(80F);

			try {

				CompareFacesResult result= rekognitionClient.compareFaces(compareFacesRequest);
				List<CompareFacesMatch> lists= result.getFaceMatches();

				System.out.println("Detected labels for " + inputimage.get(0)+ " and "+i);

				if(!lists.isEmpty()){
					for (CompareFacesMatch label: lists) {
						System.out.println(label.getFace() + ": Similarity is " + label.getSimilarity().toString());
						output = new Output();
						output.setFile1(i);
						output.setFile2(inputimage.get(0));
						output.setSimilarity(Float.parseFloat(label.getSimilarity().toString()));
						if(label.getSimilarity()>90.0) {
							System.out.println("Files compared "+i +"&"+inputimage.get(0));
						/*	output = new Output();
							output.setFile1(i);
							output.setFile2(inputimage.get(0));
							output.setSimilarity(Float.parseFloat(label.getSimilarity().toString()));*/
							break;
						}
					}
				}else{
					System.out.println("Faces Does not match");
				}
			} catch(AmazonRekognitionException e) {

			}
		}
		if(null == output){
			output = new Output();
			output.setFile1(inputimage.get(0));
			output.setSimilarity(0);
		}
		return output;

	}


	public File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = null;
		System.out.println("Inside ConvertMultiPart");
		if(null != file){
			convFile = new File(file.getOriginalFilename());
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} else{
			System.out.println("File is null. Please send a file ");
		}
		System.out.println("covFile "+ convFile);
		return convFile;
	}

	public void uploadFileTos3bucket(String fileName, File file, String bucketName) {
			System.out.println("Inside UploadFileToS3Bucket");
			PutObjectResult putObject = s3.putObject(bucketName, fileName, file);
			System.out.println("uploadFileTos3bucket");
			System.out.println(putObject.getETag());
			System.out.println(putObject.getVersionId());
	}

	public void deleteFile(String fileName) {
		System.out.println("Inside delete file");
		try {
			com.amazonaws.services.s3.model.DeleteObjectRequest deleteObjectRequest = new com.amazonaws.services.s3.model.DeleteObjectRequest(
					AWS_BUCKET, fileName);

			s3.deleteObject(deleteObjectRequest);
			System.out.println("delete completed");
		} catch (AmazonServiceException ex) {
			System.out.println("Exception in delete");
			ex.printStackTrace();
		}

	}


}
