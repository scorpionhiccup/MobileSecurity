package com.cloud.secure;

import com.aws.services.s3.AmazonS3Client;

public class AppGlobals {
	// fields made empty, for confidentiality.
	public static final String USER_NAME = "";
	public static final String PASSWORD = "";
	public static final String S3_BUCKET_NAME = "";
	public static final String CLOUD_FRONT_PREFIX = "";
	public static final String S3_IP = ""
	public static final String RDS_IP="";

	public static AmazonS3 s3 = null;
	static {
		final BasicAWSCredentials credentials = new BasicAWSCredentials(
				AppGlobals.USER_NAME, AppGlobals.PASSWORD);
		s3 = new AmazonS3Client(credentials);
		s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
		s3.setEndpoint("s3-ap-southeast-1.amazonaws.com");
	}

}
