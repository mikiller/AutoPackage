package com.util.autopackage;

public class AutoPackage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length == 1){
			PackageUtil util = PackageUtil.getInstance();
			util.zipAlignApks(args[0]);
		}else
			PackageUtil.getInstance(args[0], args[1], args[2]).autoPackage();
	}

}
