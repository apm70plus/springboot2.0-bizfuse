package com.apm70.bizfuse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import org.springframework.util.StringUtils;

import com.apm70.bizfuse.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;


/**
 * 共通工具类。
 */
@Slf4j
public class CommonUtils {

	private static BitSet dontNeedEncoding;

	/**
	 * 这里会有误差,比如输入一个字符串 123+456,它到底是原文就是123+456还是123 456做了urlEncode后的内容呢？<br>
	 * 其实问题是一样的，比如遇到123%2B456,它到底是原文即使如此，还是123+456 urlEncode后的呢？ <br>
	 * 在这里，我认为只要符合urlEncode规范的，就当作已经urlEncode过了<br>
	 * 毕竟这个方法的初衷就是判断string是否urlEncode过<br>
	 */
	static {
		CommonUtils.dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++) {
			CommonUtils.dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++) {
			CommonUtils.dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++) {
			CommonUtils.dontNeedEncoding.set(i);
		}
		CommonUtils.dontNeedEncoding.set('+');
		CommonUtils.dontNeedEncoding.set('-');
		CommonUtils.dontNeedEncoding.set('_');
		CommonUtils.dontNeedEncoding.set('.');
		CommonUtils.dontNeedEncoding.set('*');
	}

	public enum Md5Type {
		upperCase, lowerCase
	}

	public static String md5(final String text) {
		return CommonUtils.md5(text, "UTF-8", Md5Type.upperCase);
	}

	public static String md5(final String text, final String encoding, final Md5Type md5Type) {
		final String md5Hex = DigestUtils.md5Hex(CommonUtils.getContentBytes(text, encoding));
		if (Md5Type.upperCase.equals(md5Type)) {
			return md5Hex.toUpperCase();
		} else {
			return md5Hex.toLowerCase();
		}
	}

	private static byte[] getContentBytes(final String content, final String encoding) {
		if ((encoding == null) || "".equals(encoding)) {
			return content.getBytes();
		}

		try {
			return content.getBytes(encoding);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + encoding);
		}
	}

	/**
	 * 根据参数组合URI串。
	 *
	 * @param prefixUri
	 *            URI前缀
	 * @param base
	 *            共通部分
	 * @return 组合后的URI
	 */
	public static String makeURI(final String prefixUri, final String base) {
		final StringBuilder sb = new StringBuilder();
		sb.append(prefixUri);
		sb.append(base);
		return sb.toString();
	}

	/**
	 * 根据[yyyy-MM-dd HH:mm:ss]转换日期字符串。
	 *
	 * @param str
	 *            日期字符串
	 * @return 日期对象
	 */
	public static Date str2Date(final String str) {
		final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return dataFormat.parse(str);
		} catch (final ParseException e) {
			return null;
		}
	}

	/**
	 * URL编码。
	 *
	 * @param uri
	 *            URI
	 * @return 编码后的URI
	 */
	public static String sanitizeForURI(final String uri) {
		final URLCodec codec = new URLCodec();
		try {
			return codec.encode(uri).replaceAll("\\+", "%20");
		} catch (final EncoderException e) {
			final String message = "Failed to encode url: " + uri;
			log.warn(message, e);
			throw new BusinessException(message, e);
		}
	}

	/**
	 * URL编码（保留反斜线）。
	 *
	 * @param uri
	 *            URI
	 * @return 编码后的URI
	 */
	public static String sanitizeURIAndPreserveSlashes(final String uri) {
		final URLCodec codec = new URLCodec();
		try {
			return codec.encode(uri).replaceAll("\\+", "%20").replaceAll("%2F", "/");
		} catch (final EncoderException e) {
			final String message = "Failed to encode url: " + uri;
			log.warn(message, e);
			throw new BusinessException(message, e);
		}
	}

	/**
	 * URL解码。
	 *
	 * @param uri
	 *            URI
	 * @return 解码后的URI
	 */
	public static String decodeURI(final String uri) {
		final URLCodec codec = new URLCodec();
		try {
			return codec.decode(uri);
		} catch (final DecoderException e) {
			final String message = "Failed to decode url: " + uri;
			log.warn(message, e);
			throw new BusinessException(message, e);
		}
	}

	/**
	 * 计算文件的MD5码，返回十六进制结果。
	 *
	 * @param file
	 *            文件对象
	 * @return 十六进制MD5结果码
	 */
	public static String md5Sum(final File file) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			final InputStream is = new FileInputStream(file);
			final byte[] buffer = new byte[1024];
			int read = 0;

			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}

			is.close();

			final byte[] md5sum = digest.digest();
			final BigInteger bigInt = new BigInteger(1, md5sum);

			// Front load any zeros cut off by BigInteger
			String md5 = bigInt.toString(16);
			while (md5.length() != 32) {
				md5 = "0" + md5;
			}
			return md5;
		} catch (final IOException e) {
			final String message = "Failed to read the file data.";
			log.warn(message, e);
			throw new BusinessException(message, e);
		} catch (final NoSuchAlgorithmException e) {
			final String message = "Failed to find the MD5 algorithm(JRE miconfigured?).";
			log.warn(message, e);
			throw new BusinessException(message, e);
		}
	}

	public static String stringMap(final Map<String, String> data) {
		final StringBuilder builder = new StringBuilder();
		CommonUtils.stringMap(builder, data, 1);
		return builder.toString();
	}

	public static void stringMap(final StringBuilder builder, final Map<String, String> data, final int floor) {
		if (StringUtils.isEmpty(data)) {
			builder.append("[]").append("\n");
			return;
		} else {
			builder.append("[").append("\n");
		}

		StringBuilder blankBuilder = new StringBuilder();
		for (int i = 0; i < floor; i++) {
			blankBuilder.append("  ");
		}
		final Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<String, String> entry = it.next();
			builder.append(blankBuilder).append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
		}

		blankBuilder = new StringBuilder();
		for (int i = 0; i < (floor - 1); i++) {
			blankBuilder.append("  ");
		}
		builder.append(blankBuilder).append("]").append("\n");
	}

	/**
	 * 生成由数字组成的随机数
	 *
	 * @param length
	 *            随机数的位数
	 */
	public static String getRandomNumber(final int length) {
		final Random rd = new Random();
		String n = "";
		int getNum;
		do {
			getNum = (Math.abs(rd.nextInt()) % 10) + 48;
			final char num1 = (char) getNum;
			final String dn = Character.toString(num1);
			n += dn;
		} while (n.length() < length);
		return n;
	}

	/**
	 * 生成指定位数的随机文本串
	 *
	 * @param seed
	 *            作为种子的字符串
	 * @param length
	 *            随机文本串的位数
	 */
	public static String getRandomText(final String seed, final int length) {
		final char[] chars = seed.toCharArray();
		final Random rand = new Random();
		final StringBuffer text = new StringBuffer();
		for (int i = 0; i < length; i++) {
			text.append(chars[rand.nextInt(chars.length)]);
		}
		return text.toString();
	}

	public static boolean hasUrlEncoded(final String str) {

		/**
		 * 支持JAVA的URLEncoder.encode出来的string做判断。 即: 将' '转成'+' <br>
		 * 0-9a-zA-Z保留 <br>
		 * '-'，'_'，'.'，'*'保留 <br>
		 * 其他字符转成%XX的格式，X是16进制的大写字符，范围是[0-9A-F]
		 */
		boolean needEncode = false;
		for (int i = 0; i < str.length(); i++) {
			final char c = str.charAt(i);
			if (CommonUtils.dontNeedEncoding.get(c)) {
				continue;
			}
			if ((c == '%') && ((i + 2) < str.length())) {
				// 判断是否符合urlEncode规范
				final char c1 = str.charAt(++i);
				final char c2 = str.charAt(++i);
				if (CommonUtils.isDigit16Char(c1) && CommonUtils.isDigit16Char(c2)) {
					continue;
				}
			}
			// 其他字符，肯定需要urlEncode
			needEncode = true;
			break;
		}

		return !needEncode;
	}

	/**
	 * 判断c是否是16进制的字符
	 *
	 * @param c
	 * @return
	 */
	private static boolean isDigit16Char(final char c) {
		return ((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'F'));
	}
}
