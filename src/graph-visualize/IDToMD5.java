package memex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IDToMD5 {
	private static String md5(String s) throws NoSuchAlgorithmException{
		String original = s;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(original.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException{
		System.out.println(md5("深圳市鹏盛达电子有限公司"));
	}
}
