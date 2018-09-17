package org.openmrs.module.qft.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TSVReader {
	
	private static final char DEFAULT_SEPARATOR = ',';
	
	private static final char DEFAULT_QUOTE = '"';
	
	private static final String TAB_SEPARATOR = "\t";
	
	private static final String LINE_SEPARATOR = "\n";
	
	public static void main(String[] args) throws Exception {
		String path = "C:\\Users\\AHMED\\Downloads\\26Jul18_ResultExpa.txt";
		// String path = "C:\\Users\\AHMED\\Downloads\\1.txt";
		String data = readTextFile(path);
		
		parseTestFileData(data);
	}
	
	public static List<Map<String, String>> parseTestFile(File file) throws IOException {
		if (file == null) {
			throw new IOException("File Object is null!");
		}
		return parseTestFileData(readTextFile(file));
	}
	
	public static List<Map<String, String>> parseTestFileData(String data) {
		System.out.println(data);
		String[] dataArray = data.split(System.lineSeparator());
		String versionValueString = dataArray[0].substring(8).trim();
		String operatorValueString = dataArray[1].substring(9).trim();
		String batchNumberValueString = dataArray[2].substring(17).trim();
		
		String[] headingData = dataArray[3].split(TAB_SEPARATOR);
		String[] values = Arrays.copyOfRange(dataArray, 4, dataArray.length - 1);
		List<Map<String, String>> listOfValues = new ArrayList<Map<String, String>>();
		for (String v : values) {
			Map map = new HashMap<String, String>();
			String[] valuess = v.split("\t");
			for (int i = 0; i < valuess.length; i++) {
				map.put(headingData[i], valuess[i]);
			}
			map.put("version", versionValueString);
			map.put("operator", operatorValueString);
			map.put("kitBatchNumber", batchNumberValueString);
			
			listOfValues.add(map);
		}
		
		return listOfValues;
	}
	
	public static String readTextFile(String path) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			return everything;
		}

	}
	
	public static String readTextFile(File file) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-16"))) {
			//try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
				
			}
			String everything = sb.toString();
			return everything;
		}

	}
	
	public static String readFileAsString(String fileName) throws Exception {
		String data = "";
		data = new String(Files.readAllBytes(Paths.get(fileName)));
		return data;
	}
	
	public static String readFile(String path) throws IOException {
		File file = new File(path);
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		StringBuilder sb = new StringBuilder();
		String st;
		while ((st = br.readLine()) != null) {
			// System.out.println(st);
			sb.append(st);
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

		List<String> result = new ArrayList<>();

		// if empty, return!
		if (cvsLine == null && cvsLine.isEmpty()) {
			return result;
		}

		if (customQuote == ' ') {
			customQuote = DEFAULT_QUOTE;
		}

		if (separators == ' ') {
			separators = DEFAULT_SEPARATOR;
		}

		StringBuffer curVal = new StringBuffer();
		boolean inQuotes = false;
		boolean startCollectChar = false;
		boolean doubleQuotesInColumn = false;

		char[] chars = cvsLine.toCharArray();

		for (char ch : chars) {

			if (inQuotes) {
				startCollectChar = true;
				if (ch == customQuote) {
					inQuotes = false;
					doubleQuotesInColumn = false;
				} else {

					// Fixed : allow "" in custom quote enclosed
					if (ch == '\"') {
						if (!doubleQuotesInColumn) {
							curVal.append(ch);
							doubleQuotesInColumn = true;
						}
					} else {
						curVal.append(ch);
					}

				}
			} else {
				if (ch == customQuote) {

					inQuotes = true;

					// Fixed : allow "" in empty quote enclosed
					if (chars[0] != '"' && customQuote == '\"') {
						curVal.append('"');
					}

					// double quotes in column will hit this!
					if (startCollectChar) {
						curVal.append('"');
					}

				} else if (ch == separators) {

					result.add(curVal.toString());

					curVal = new StringBuffer();
					startCollectChar = false;

				} else if (ch == '\r') {
					// ignore LF characters
					continue;
				} else if (ch == '\n') {
					// the end, break!
					break;
				} else {
					curVal.append(ch);
				}
			}

		}

		result.add(curVal.toString());

		return result;
	}
}
