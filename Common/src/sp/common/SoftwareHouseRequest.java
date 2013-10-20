package sp.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SoftwareHouseRequest {
	byte[] encryptedRequest;
	byte[] symmetricEncryptionKey;
	
	public SoftwareHouseRequest(LinkingRequest request, String encryption, Key publicKey, String symmetricEncryption){
		wrapKey(encryptRequest(request, symmetricEncryption), publicKey, encryption);
	}
	
	public SoftwareHouseRequest getRequest(Key encryptionKey, String encryption, String symmetricEncryption) {
		Key symmetricEncryptionKey = unwrapKey(encryptionKey, encryption);
		
		byte[] decryptedRequest = null;
		
		try {
			Cipher encryptionCipher = Cipher.getInstance(symmetricEncryption);
			
			encryptionCipher.init(Cipher.DECRYPT_MODE, symmetricEncryptionKey);
			decryptedRequest = encryptionCipher.doFinal(encryptedRequest);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		SoftwareHouseRequest request = deserialiseRequest(decryptedRequest);
		
		return request;
	}

	private void wrapKey(SecretKey secretKey, Key publicKey, String encryption) {
		
		try {
			Cipher encryptionCipher = Cipher.getInstance(encryption);
			
			encryptionCipher.init(Cipher.WRAP_MODE, publicKey);
			symmetricEncryptionKey = encryptionCipher.wrap(secretKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException e) {
			e.printStackTrace();
		}
	}
	
	private Key unwrapKey(Key encryptionKey, String encryption) {
		Key unwrappedKey = null;
		
		try {
			Cipher encryptionCipher = Cipher.getInstance(encryption);
			
			encryptionCipher.init(Cipher.UNWRAP_MODE, encryptionKey);
			unwrappedKey = encryptionCipher.unwrap(symmetricEncryptionKey, encryption, Cipher.PUBLIC_KEY);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		}
		
		return unwrappedKey;
	}

	/**
	 * Encrypts a Software House's request for linked libraries and adds it
	 * to a list of other encrypted Software house requests.
	 * @param softwareHouse Name of the Software House to encrypt the request for
	 */
	private SecretKey encryptRequest(LinkingRequest request, String symmetricEncryption) {
		
		SecretKey encryptionKey = null;
		
		ByteArrayOutputStream serialisedRequest = serialiseRequest(request);
		
		try {
			encryptionKey = KeyGenerator.getInstance(symmetricEncryption).generateKey();
			Cipher encryptionCipher = Cipher.getInstance(symmetricEncryption);
			
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
			encryptedRequest = encryptionCipher.doFinal(serialisedRequest.toByteArray());
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return encryptionKey;
	}
	
	private ByteArrayOutputStream serialiseRequest(LinkingRequest request) {
		ByteArrayOutputStream serialisedRequest = new ByteArrayOutputStream();
		ObjectOutputStream requestObjectOutputStream;
		
		try {
			requestObjectOutputStream = new ObjectOutputStream(serialisedRequest);
			requestObjectOutputStream.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serialisedRequest;
	}
	
	private SoftwareHouseRequest deserialiseRequest(byte[] serialisedRequest) {
		ByteArrayInputStream serialisedRequestInputStream = new ByteArrayInputStream(serialisedRequest);
		ObjectInputStream requestObjectInputStream;
		SoftwareHouseRequest request = null;
		
		try {
			
			requestObjectInputStream = new ObjectInputStream(serialisedRequestInputStream);
			request = (SoftwareHouseRequest) requestObjectInputStream.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return request;
	}
}
