package com.util.autopackage;

public class AutoPackage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		switch(args.length){
			case 0:
				//输入sdk路径
				//选择功能1.批量打包 2. 批量签名
				//1
				//输入项目路径
				//输入母包名
				//输入配置文件路径
				//输入keystore
				//2
				//输入文件夹名
				//输入文件前缀
				//输入文件后缀
				//输入配置文件路径
				//输入keystore
				break;
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			default:
				break;
		}





		if(args.length == 1){
			PackageUtil util = PackageUtil.getInstance();
			util.zipAlignApks(args[0]);
		}else
			PackageUtil.getInstance(args[0], args[1], args[2]).autoPackage();
	}

}
