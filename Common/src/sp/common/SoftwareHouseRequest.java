package sp.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SoftwareHouseRequest {
	private static final String signatureAlgorithm = "SHA1withRSA";
	private byte[] encryptedRequest;
	private byte[] symmetricEncryptionKey;
	private byte[] senderSignature;
	
	private HashMap<String,String> errors;
	
	private class EncryptionReceipt {
		SecretKey key;
		byte[] encrypted;
		
		public EncryptionReceipt(SecretKey key, byte[] encrypted){
			this.key = key;
			this.encrypted = encrypted;
		}
		
		public byte[] getEncrypted(){ return encrypted.clone(); }
		
		public SecretKey getKey(){ 
			return key;
		}
	}
	
	public SoftwareHouseRequest(LinkingRequest request, PublicKey publicKey, String symmetricEncryption) 
			throws NoSuchAlgorithmException {
		
		EncryptionReceipt receipt = encryptRequest(request, symmetricEncryption);
		encryptedRequest = receipt.getEncrypted();
		symmetricEncryptionKey = encryptSymmetricKey(receipt.getKey(), publicKey);
	}
	
	public LinkingRequest getRequest(PrivateKey privateKey, String symmetricalEncryptionType) {
		SecretKey secretKey = decryptSymmetricKey(privateKey,symmetricalEncryptionType);
		
		byte[] decryptedRequest = null;
		
		try {
			Cipher encryptionCipher = Cipher.getInstance(secretKey.getAlgorithm());
			encryptionCipher.init(Cipher.DECRYPT_MODE, secretKey);
			
			decryptedRequest = encryptionCipher.doFinal(encryptedRequest);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return deserialiseRequest(decryptedRequest);
	}

	 public boolean isSigned() {
		return senderSignature != null;
	}

	public boolean isSignatureCorrect(PublicKey publicKey) {
		if(senderSignature == null)
			return false;
		
		Signature signature;
		boolean verified = false;

		try {
			signature = Signature.getInstance(signatureAlgorithm);
			signature.initVerify(publicKey);
			signature.update(encryptedRequest);
			signature.update(symmetricEncryptionKey);
			verified = signature.verify(senderSignature); 
		} catch (NoSuchAlgorithmException | InvalidKeyException | 
				SignatureException e) {
			e.printStackTrace();
		}
		
		return verified;
	}

	public void sign(PrivateKey privateKey) {
		Signature signature;
		
		try {
			signature = Signature.getInstance(signatureAlgorithm);
			signature.initSign(privateKey);
			signature.update(encryptedRequest);
			signature.update(symmetricEncryptionKey);
			senderSignature = signature.sign(); 
		} catch (NoSuchAlgorithmException | InvalidKeyException | 
				SignatureException e) {
			e.printStackTrace();
		}
		
	}

	private byte[] encryptSymmetricKey(SecretKey secretKey, Key publicKey) {
         
         byte[] encrypted = null;
         
         try {
                 Cipher encryptionCipher = Cipher.getInstance(publicKey.getAlgorithm());
                 
                 encryptionCipher.init(Cipher.ENCRYPT_MODE, publicKey);
                 encrypted = encryptionCipher.doFinal(secretKey.getEncoded());
                 
         } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                 e.printStackTrace();
         } catch (IllegalBlockSizeException | BadPaddingException e) {
                 e.printStackTrace();
         }
         
         return encrypted;
 }
	
	 private SecretKey decryptSymmetricKey(Key encryptionKey, String symmetricalEncryptionType){
         SecretKey key = null;
         
         String encryption = encryptionKey.getAlgorithm();
         
         try {
				 Cipher encryptionCipher = Cipher.getInstance(encryption);
                 encryptionCipher.init(Cipher.DECRYPT_MODE, encryptionKey);
                 
                 byte[] decryptedKey = encryptionCipher.doFinal(symmetricEncryptionKey);
                 
                 key = new SecretKeySpec(decryptedKey, symmetricalEncryptionType);
                 
         } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | 
        		 IllegalBlockSizeException | BadPaddingException e) {
                 e.printStackTrace();
         }
         
         return key;
	 }

	/**
	 * Encrypts a Software House's request for linked libraries and adds it
	 * to a list of other encrypted Software house requests.
	 * @param softwareHouse Name of the Software House to encrypt the request for
	 * @throws NoSuchAlgorithmException 
	 */
	private EncryptionReceipt encryptRequest(LinkingRequest request, String symmetricEncryptionType) throws NoSuchAlgorithmException {
		
		byte[] encrypted = null;
		SecretKey encryptionKey = null;
		
		ByteArrayOutputStream serialisedRequest = serialiseRequest(request);
		
		try {
			encryptionKey = KeyGenerator.getInstance(symmetricEncryptionType).generateKey();
			
			Cipher encryptionCipher = Cipher.getInstance(symmetricEncryptionType);
			encryptionCipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
			
			encrypted = encryptionCipher.doFinal(serialisedRequest.toByteArray());
			
		} catch (InvalidKeyException | NoSuchPaddingException | 
				IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return new EncryptionReceipt(encryptionKey, encrypted);
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
	
	private LinkingRequest deserialiseRequest(byte[] serialisedRequest) {
		ByteArrayInputStream serialisedRequestInputStream = new ByteArrayInputStream(serialisedRequest);
		ObjectInputStream requestObjectInputStream;
		
		LinkingRequest request = null;
		
		try {
			
			requestObjectInputStream = new ObjectInputStream(serialisedRequestInputStream);
			request = (LinkingRequest) requestObjectInputStream.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return request;
	}

	private String errorMessage(String errorCode) {
		if(errors == null){
			errors = new HashMap<String,String>();
		}
		
		return errors.get(errorCode);
		
	}
}
