package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

public class StorageGai {
	private Gson gson;
	
	private BufferedReader reader;
	private PrintWriter writer;
	
	private static StorageGai storageGai;
	
	private static final String USERDEFINED_FILE_NAME = "save.txt";
	private static final String SETUP_FILE_NAME = "setup.txt";
	private static final String AUTOSAVE_FILE_NAME = "autosave.txt";
	
	private static File setupFile;
	private File userdefinedFile;
	private File autosaveFile;
	
	private String userdefinedFileName;
	
	public static StorageGai getInstance() {
		if (storageGai == null || !setupFile.exists()) {
			storageGai = new StorageGai();
		}
		return storageGai;
	}
	
	private StorageGai() {
		gson = new Gson();
		
		fileSystemSetup();
		
		autosaveFile = new File(AUTOSAVE_FILE_NAME);
		createFile(autosaveFile);
		autosaveFile.setWritable(false);
		
		userdefinedFileName = getUserDefinedFileName(setupFile);
	}
	
	private void fileSystemSetup() {
		setupFile = new File(SETUP_FILE_NAME);
		createFile(setupFile);
		
		
	}
	
	private void createFile(File file) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private getUserDefinedFileName(File setupFile) {
		String name = "";
	}
}
