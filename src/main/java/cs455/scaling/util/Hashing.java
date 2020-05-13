package cs455.scaling.util;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cs455.scaling.client.*;
import cs455.scaling.server.*;

public class Hashing {

	public Hashing() {
		
	}
	
	public String getHash(byte[] data) throws NoSuchAlgorithmException {
		String getHash = SHA1FromBytes(data);
		while(getHash.length() < 40) {
			getHash = 0 + getHash;
		}
		return getHash;
	}
	
	public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash  = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		return hashInt.toString(16);
	}
}
