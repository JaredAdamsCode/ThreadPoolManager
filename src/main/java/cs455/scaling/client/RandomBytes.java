package cs455.scaling.client;


import java.util.Random;

import cs455.scaling.server.*;
import cs455.scaling.util.*;

public class RandomBytes {

	public RandomBytes() {
		
	}
	
	public byte[] getRandomBytes() {
		byte[] randBytes = new byte[Protocols.CLIENT_MESSAGE_CAPACITY];
		new Random().nextBytes(randBytes);
		return randBytes;
	}
	
}
