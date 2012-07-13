package com.soartech.bolt.testing;

import java.io.File;
import java.io.IOException;

import edu.umich.sbolt.SBolt;

public class Settings {
	final static private Settings instance = new Settings();
	// final static private String scriptDirectory = "/script";

	private String sboltDirectory;

	private Settings() {
		 ClassLoader classLoader = SBolt.class.getClassLoader();
		 File classpathRoot = new File(classLoader.getResource("").getPath());
		 sboltDirectory = classpathRoot.getPath();
	}

	public static Settings getInstance() {
		return instance;
	}

	public String getSboltDirectory() {
		return sboltDirectory;
	}

	public void setSboltDirectory(String sboltDirectory) {
		this.sboltDirectory = sboltDirectory;
	}
}
