package com.dvuckovic.busplus;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class EncryptDecrypt {

	public static class EncryptDecryptException extends RuntimeException {

		private static final long serialVersionUID = 3187961766346610837L;

		public EncryptDecryptException(Throwable e) {
			super(e);
		}

	}

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
	private static final String CHARSET = "UTF-8";

	//private final boolean encryptKeys;
	private final Cipher writer;
	private final Cipher reader;
	private final Cipher keyWriter;

	/**
	 * This will initialize an instance of the SecurePreferences class
	 * @param context your current context.
	 * @param preferenceName name of preferences file (preferenceName.xml)
	 * @param secureKey the key used for encryption, finding a good key scheme is hard. 
	 * Hardcoding your key in the application is bad, but better than plaintext preferences. Having the user enter the key upon application launch is a safe(r) alternative, but annoying to the user.
	 * @param encryptKeys settings this to false will only encrypt the values, 
	 * true will encrypt both values and keys. Keys can contain a lot of information about 
	 * the plaintext value of the value which can be used to decipher the value.
	 * @throws EncryptDecryptException
	 */
	public EncryptDecrypt(String secureKey) throws EncryptDecryptException {
		try {
			this.writer = Cipher.getInstance(TRANSFORMATION);
			this.reader = Cipher.getInstance(TRANSFORMATION);
			this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION);

			initCiphers(secureKey);

			//this.encryptKeys = encryptKeys; 
		}
		catch (GeneralSecurityException e) {
			throw new EncryptDecryptException(e);
		}
		catch (UnsupportedEncodingException e) {
			throw new EncryptDecryptException(e);
		}
	}

	protected void initCiphers(String secureKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException,
			InvalidAlgorithmParameterException {
		IvParameterSpec ivSpec = getIv();
		SecretKeySpec secretKey = getSecretKey(secureKey);

		writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
		reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
		keyWriter.init(Cipher.ENCRYPT_MODE, secretKey);
	}
	
	protected IvParameterSpec getIv() {
		byte[] iv = new byte[writer.getBlockSize()];
		System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, iv, 0, writer.getBlockSize());
		return new IvParameterSpec(iv);
	}
	
	protected SecretKeySpec getSecretKey(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] keyBytes = createKeyBytes(key);
		return new SecretKeySpec(keyBytes, TRANSFORMATION);
	}

	protected byte[] createKeyBytes(String key) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md;
		byte[] keyBytes;
		switch (key.charAt(0)) {
			case 0:
				md = MessageDigest.getInstance("SHA-64");
				md.reset();
				keyBytes = md.digest(key.getBytes(CHARSET));
				break;
			case 1:
				md = MessageDigest.getInstance("SHA-128");
				md.reset();
				keyBytes = md.digest(key.getBytes(CHARSET));
				break;
			case 2:
				md = MessageDigest.getInstance("SHA-512");
				md.reset();
				keyBytes = md.digest(key.getBytes(CHARSET));
				break;
			default:
				md = MessageDigest.getInstance("SHA-256");
				md.reset();
				keyBytes = md.digest(key.getBytes(CHARSET));
		}
		return keyBytes;
	}
	
	public String d(String encodedValue) {
		return decrypt(encodedValue);
	}
	
	public String e(String value) {
		return encrypt(value, writer);
	}

	protected String encrypt(String value, Cipher writer) throws EncryptDecryptException {
		byte[] secureValue;
		try {
			secureValue = convert(writer, value.getBytes(CHARSET));
		}
		catch (UnsupportedEncodingException e) {
			throw new EncryptDecryptException(e);
		}
		String secureValueEncoded = Base64.encodeToString(secureValue, Base64.NO_WRAP);
		return secureValueEncoded;
	}

	protected String decrypt(String securedEncodedValue) {
		byte[] securedValue = Base64.decode(securedEncodedValue, Base64.NO_WRAP);
		byte[] value = convert(reader, securedValue);
		try {
			return new String(value, CHARSET);
		}
		catch (UnsupportedEncodingException e) {
			throw new EncryptDecryptException(e);
		}
	}

	private static byte[] convert(Cipher cipher, byte[] bs) throws EncryptDecryptException {
		try {
			return cipher.doFinal(bs);
		}
		catch (Exception e) {
			throw new EncryptDecryptException(e);
		}
	}
}
