package com.soartech.bolt.testing;

import java.io.File;
import java.io.IOException;

public class Settings {
	final static private Settings instance = new Settings();
	// final static private String scriptDirectory = "/script";

	private File sboltDirectory;

	private Settings() {
		File dir;
		try {
			dir = new File(new File("").getCanonicalPath());
		} catch (IOException e) {
			dir = new File("");
		}
		sboltDirectory = dir;
	}

	public static Settings getInstance() {
		return instance;
	}

	public File getSboltDirectory() {
		return sboltDirectory;
	}

	public void setSboltDirectory(File sboltDirectory) {
		this.sboltDirectory = sboltDirectory;
	}
}
