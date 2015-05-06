package bot.nat.sumi;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {
	public static final String folder = "H:\\Java\\NatsumiBot2\\data\\";

	public static void main(String[] arg){
		loadChannels(loadJARs());
	}

	private static void loadChannels(ArrayList<Module> mod) {
		File[] files = new File(folder +"connect/").listFiles();

		for(File f : files != null ? files : new File[0]){
			System.out.println(f.getAbsolutePath());
			Servers.addServer(new Server(new ConfigFile(f.getAbsolutePath(), ConfigFile.READ, read(f)), mod));
		}
	}

	private static String read(File f) {
		try {
			InputStream is = new FileInputStream(f);
			java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
			String data = s.hasNext() ? s.next() : "";
			s.close();
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/* load commands */
	private static ArrayList<Module> loadJARs() {
		File[] files = new File(folder +"commands/").listFiles();
		ArrayList<Module> mod = new ArrayList<Module>();

		for(File f : files != null ? files : new File[0]){
			try {
				loadJAR(f.getAbsolutePath());
				mod.add(getInstance(f.getName().replace(".jar", ""), f.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		return mod;
	}

	/* get new module instace */
	private static Module getInstance(String str, String file) {
		try {
			return (Module) ClassContainer.get().getClass(file, str).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	/* loads lang in JAR to memory for use */
	private static void loadJAR(String location) throws IOException, ClassNotFoundException, URISyntaxException {
		System.out.print("loading Classes from '" + location +"'");

        /* list of class names */
		ArrayList<String> classNames = new ArrayList<String>();
        /* list of configuration files */
		ArrayList<String> configs = new ArrayList<String>();

        /* get the JAR to this instance */
		ZipInputStream zip = new ZipInputStream(new FileInputStream(location));

        /* get each file in the zip */
		for(ZipEntry entry = zip.getNextEntry();entry != null;entry = zip.getNextEntry()){

            /* if this is a class */
			if(entry.getName().endsWith(".class") && !entry.isDirectory()) {

				// This ZipEntry represents a class. Now, what class does it represent?
				StringBuilder className = new StringBuilder();
				for(String part : entry.getName().split("/")) {
					if(className.length() != 0) {
						className.append(".");
					}

					className.append(part);
					if(part.endsWith(".class")) {
						className.setLength(className.length() - ".class".length());
					}
				}

                /* add final result to string array */
				classNames.add(className.toString());
            /* if its a configuration file */
			} else if(entry.getName().endsWith(".spc")){
				configs.add(entry.getName());
			}
		}

        /* add url to make sure the classes work */
		ClassContainer.get().addURL(location);

        /* load all the classes */
		for(String cls : classNames){
            /* load next class */
			ClassContainer.get().loadClass(cls);
		}

        /* load all the configurations */
		for(String cfg : configs){
            /* load next class */
			ClassContainer.get().loadConfig(location, cfg);
		}

		System.out.println(" ...Done!");
	}
}
