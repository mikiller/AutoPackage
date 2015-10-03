package com.util.autopackage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public class PackageUtil {
	private List<String> qudaoInfos = null;
	private String configFilePath = null;
	private String srcProjDir = null;
	private String apkName = null;
	private String simpleApkName = null;
	private String unZipDirName = null;
	private String outputDir = "..\\..\\KaQuApk\\";
	private String keyStore = null;
	private String keyStroePwd = "kaqu2015";
	
	private static final String cmdHead = "cmd.exe /C ";
	
	private boolean reEditSuccess = false;
	
	private static class PackageUtilFactory {
		public static PackageUtil instance = new PackageUtil();
	}

	private PackageUtil() {
	}

	public static PackageUtil getInstance(String projDir, String filePath, String keystorePath) {
		PackageUtilFactory.instance.Log(projDir);
		PackageUtilFactory.instance.srcProjDir = projDir;
		PackageUtilFactory.instance.apkName = "..\\..\\KaQu-release-unsigned.apk";
		PackageUtilFactory.instance.configFilePath = filePath;
		PackageUtilFactory.instance.simpleApkName = "KaQu-release-unsigned.apk";
		PackageUtilFactory.instance.unZipDirName = "KaQu-release-unsigned";
		PackageUtilFactory.instance.keyStore = keystorePath;
		return PackageUtilFactory.instance;
	}
	
	public static PackageUtil getInstance(){
		return PackageUtilFactory.instance;
	}
	
	public void autoPackage() {
		buildSrcProject();
		
		getChannelId();
		unZipApk();
		rePackageApk(copyManifest());
	}
	
	private void buildSrcProject(){
		//运行ant clean
		Log(runCmd(concatStr(cmdHead, "ant clean")));
		//运行ant release
		Log(runCmd(concatStr(cmdHead, "ant release")));
		//复制bin下signedApk到目标目录
		Log(runCmd(concatStr(cmdHead, "copy ", concatStr(srcProjDir, "bin\\KaQu-release-unsigned.apk "), apkName)));
		//运行ant clean
		Log(runCmd(concatStr(cmdHead, "ant clean")));
	}
	
	private void getChannelId() {
		qudaoInfos = new ArrayList<String>();
		try {
			Log("==INFO 1.==从" + configFilePath + "获取渠道=====");
			fileOperator(this.getClass().getDeclaredMethod("setQudaoInfos",String.class),false,null,new File(configFilePath));
			Log("==INFO 1.==获取渠道成功，一共有" + qudaoInfos.size() + "个渠道======");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
	}
	
	private void unZipApk() {
		Log("==INFO 2.==开始解压apk " + apkName + "======");
		Log(runCmd(concatStr(cmdHead, "java -jar %ANDROID_PATH%/apktool.jar d -f -s ", apkName)));
		Log("==INFO 2.==解压apk成功，准备移动======");
	}
	
	private File[] copyManifest(){
		Log("==INFO 3.==复制AndroidManifest.xml======");
		String manifestSrcPath = new File(unZipDirName).getAbsolutePath().concat("\\AndroidManifest.xml");
		String manifestBakPath = "..\\AndroidManifest.xml";
		
		Log(runCmd(concatStr(cmdHead, "copy ", manifestSrcPath, " ", manifestBakPath)));
		Log("==INFO 3.==复制AndroidManifest.xml成功======");
		return new File[]{ new File(manifestBakPath), new File(manifestSrcPath)};
	}
	
	private void rePackageApk(File[] files){
		for(String info : qudaoInfos){
			Log("==INFO 4.1. == 正在生成包: " + info + " ======");
			try {
				Log("==INFO 4.1.1 == 替换AndroidManife.xml字段: " + info + " ======");
				fileOperator(this.getClass().getDeclaredMethod("reEditManifest", String.class, String.class, StringBuffer.class), true, info, files);
				if(reEditSuccess == true){
					packageApk(info, concatStr(outputDir, "\\", info, "_", simpleApkName));
					signedApk(info, concatStr(outputDir, "\\", info, "_", simpleApkName));
					zipAlignApk(info);
					reEditSuccess = false;
				}else{
					Log("==ERROR==打包" + info + "失败======");
				}
				
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		deleteBakFiles(files[0]);
	}
	
	private void deleteBakFiles(File maniBak){
		Log(runCmd(concatStr(cmdHead, "rd /s/q ", unZipDirName)));
		maniBak.delete();
		new File(apkName).delete();
	}
	
	private void fileOperator(Method method, boolean needWrite, String argStr, File...file){
		BufferedReader buffReader = null;
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file[0]);
			buffReader = new BufferedReader(fileReader);
			if(needWrite){
				writeFileWithMethod(buffReader, method, argStr, file.length == 2 ? file[1] : file[0]);
			}else{
				readFileWithMethod(buffReader, method);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log("==ERROR==" + configFilePath + "文件不存在======");
		} catch (IOException e) {
			e.printStackTrace();
			Log("==ERROR==" + configFilePath + "文件读写异常======");
		} finally {
			try {
				if (fileReader != null)
					fileReader.close();
				if (buffReader != null)
					buffReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void packageApk(String info, String unSignedApk){
		Log("==INFO 4.2. == 准备打包: " + info + " ======");
		Log(runCmd(concatStr(cmdHead, "java -jar %ANDROID_PATH%/apktool.jar b ", unZipDirName)));
		checkoutDirExist(outputDir);
		Log(runCmd(concatStr(cmdHead, String.format("copy %s\\dist\\%s %s", unZipDirName, simpleApkName, unSignedApk))));
		Log("==INFO 4.2. == 打包成功======");
	}
	
	private void signedApk(String info, String unsignedApk){
		Log("==INFO 4.3. == 开始签名: " + info + " ======");
		Log(runCmd(String.format("jarsigner -digestalg SHA1 -sigalg SHA1withDSA -verbose -keystore %s -signedjar %s %s %s -storepass  %s", keyStore,concatStr(outputDir, "\\", info, "_Kaqu_signed.apk"),unsignedApk,keyStore, keyStroePwd)));
		Log("==INFO 4.4. == 签名成功: " + info + " ======\r\n");
		new File(unsignedApk).delete();
	}
	
	private void signedApk(String srcFile){
		String temp = srcFile.substring(0, srcFile.indexOf(".apk"));
		String desFile = concatStr(srcFile.substring(0, srcFile.indexOf(".apk")), "_signed.apk");
		Log(runCmd(String.format("jarsigner -digestalg SHA1 -sigalg SHA1withDSA -verbose -keystore %s -signedjar %s %s %s -storepass  %s", "kaqu.keyStore",concatStr(srcProjDir, "\\", srcFile.substring(0, srcFile.indexOf(".apk")), "_signed.apk"),concatStr(srcProjDir, "\\", srcFile),"kaqu.keyStore", keyStroePwd)));
	}
	
	private void zipAlignApk(String info){
		Log(runCmd(concatStr(cmdHead, "zipalign -v 4 ", concatStr(outputDir, "\\", info, "_Kaqu_signed.apk "), concatStr(outputDir, "\\", info, "_Kaqu.apk"))));
	}
	
	private void zipAlignApk(String srcFile, String desFile){
		Log(runCmd(concatStr(cmdHead, "zipalign -v 4 ", concatStr(srcProjDir, "\\", srcFile)," ", concatStr(srcProjDir, "\\", desFile))));
	}
	
	private void writeFileWithMethod(BufferedReader buffReader, Method method, String argStr, File file) throws IOException{
		FileWriter fileWriter = new FileWriter(file);
		StringBuffer strBuff = new StringBuffer();
		String line =  null;
		try {
			while ((line = buffReader.readLine()) != null) 
				method.invoke(this, line, argStr, strBuff);
			fileWriter.write(strBuff.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log("==ERROR==" + method.getName() + "参数非法======");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Log("==ERROR==" + method.getName() + "未获得权限======");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}finally{
			fileWriter.close();
		}
	}
	
	private void readFileWithMethod(BufferedReader buffReader, Method method) throws IOException{
		String line = null;
		try {
			while ((line = buffReader.readLine()) != null) 
				method.invoke(this, line);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log("==ERROR==" + method.getName() + "参数非法======");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Log("==ERROR==" + method.getName() + "未获得权限======");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	private void checkoutDirExist(String dirPath){
		File file = new File(dirPath);
		if(!file.exists())
			file.mkdirs();
	}
	
	private void setQudaoInfos(String info){
		qudaoInfos.add(info.trim());
		Log(qudaoInfos.toString());
	}

	private void reEditManifest(String line, String replaceStr, StringBuffer strBuff){
		if(line.contains("UMENG_CHANNEL_VALUE")){
			line = line.replaceAll("UMENG_CHANNEL_VALUE", replaceStr);
			Log(line);
			reEditSuccess = true;
		}
		strBuff.append(line + "\n");
	}
	
	private Process runCmd(String cmd) {
		try {
			Log(cmd);
			return Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return runCmd(cmdHead.concat("echo run ").concat(cmd).concat(" error"));
		}
	}

	private void Log(Process prc) {
		BufferedReader buffReader = null;
		String str = null;
		try {
			buffReader = new BufferedReader(new InputStreamReader(prc.getInputStream()));
			while ((str = buffReader.readLine()) != null) {
				System.out.println(str);
			}
			
			buffReader = new BufferedReader(new InputStreamReader(prc.getErrorStream()));
			while((str = buffReader.readLine()) != null){
				System.out.println("!!!" + str);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				buffReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	private void Log(String msg) {
		System.out.println(msg);
	}
	
	private String concatStr(final String src, String...strs){
		String rst = src;
		for(String str : strs){
			rst = rst.concat(str);
		}
		return rst;
	}
	
	public void zipAlignApks(String dirPath){
		srcProjDir = dirPath;
		File file = new File(srcProjDir);
		
		File[] apks = file.listFiles();
		
		for(File apk : apks){
			String newFile = apk.getName().substring(apk.getName().indexOf("android"), apk.getName().indexOf(".apk")).concat("_aligned.apk");
			signedApk(apk.getName());
			String signedApk = concatStr(apk.getName().substring(0, apk.getName().indexOf(".apk")), "_signed.apk");
			zipAlignApk(concatStr(apk.getName().substring(0, apk.getName().indexOf(".apk")), "_signed.apk"), newFile);
			new File(signedApk).delete();
			apk.delete();
		}
	}

}
