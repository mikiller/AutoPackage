package com.util.autopackage;

public class AutoPackage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		switch(args.length){
			case 0:
				//����sdk·��
				//ѡ����1.������� 2. ����ǩ��
				//1
				//������Ŀ·��
				//����ĸ����
				//���������ļ�·��
				//����keystore
				//2
				//�����ļ�����
				//�����ļ�ǰ׺
				//�����ļ���׺
				//���������ļ�·��
				//����keystore
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
