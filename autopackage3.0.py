# coding=utf-8
import zipfile
import shutil
import os

# 在渠道号配置文件中，获取指定的渠道号
#'./info/channel.txt'
def getChannel(path):
	global channels;
	try:
		channelFile = open(path,'r')
		channels = channelFile.readlines()
		channelFile.close()
		print('-'*20,'all channels','-'*20)
		print(channels)
		print('-'*50)
	except FileNotFoundError as e:
		print(e)
		getChannel(path = input("请输入配置文件路径："))

# 获取当前目录下所有的apk文件
#'.'
def getApks(dirPath):
	global src_apks;
	src_apks = [];
	dirPath = os.path.basename(dirPath)
	try:
		if os.path.isfile(dirPath):
			src_apks.append(dirPath)
		else:
			for file in os.listdir(dirPath):
			    if os.path.isfile(file):
			    	#[1:]提取不带'.'的扩展名
			        extension = os.path.splitext(file)[1][1:]
			        if extension in 'apk':
			            src_apks.append(file)
	except FileNotFoundError as e:
		print(e)
		getApks(dirPath = input("请输入母包目录/文件名："))

#母包签名
def signApk(needSign):
	global finalSrcApks;
	finalSrcApks = []
	if needSign == '1':
		keyStoreName = input("请输入Keystore路径：")
		keyStorePwd = input("请输入Keystore密码：")
		if os.path.isfile(keyStoreName):
			keyStoreName = os.path.basename(keyStoreName)
			keyStoreAlign = os.path.splitext(keyStoreName)[0]
		else:
			print("keystore路径有误，请重新输入！")
			signApk(1)

		for src_apk in src_apks:
			signCmd = 'jarsigner -digestalg SHA1 -sigalg SHA1withRSA -verbose -keystore {} -signedjar {} {} {} -storepass {}'.format(keyStoreName, 'tmpApp.apk', src_apk, keyStoreAlign, keyStorePwd)
			os.system(signCmd)
			print('\n ' + '-'*20 + '签名成功' + '-'*20 + '\n')
			zipalignPath = input('输入zipalgin路径：')
			alignCmd = zipalignPath + ' -v 4 {} {}'.format('tmpApp.apk', 'aligned-' + src_apk)
			os.system(alignCmd)
			print('\n ' + '-'*20 + '对齐成功' + '-'*20 + '\n')
			finalSrcApks.append('aligned_' + src_apk)
			os.remove('tmpApp.apk')
	else:
		finalSrcApks = src_apks


# 遍历所以的apk文件，向其压缩文件中添加渠道号文件
def addChannelIntoApk(outputDir):
	for src_apk in finalSrcApks:
		src_apk_file_name = os.path.basename(src_apk)
		print('\n当前母包名称:',src_apk_file_name)
		# temp_list = os.path.splitext(src_apk_file_name)
		# src_apk_name = temp_list[0]
		# src_apk_extension = temp_list[1]

		if outputDir == '':
			outputDir = './'
		if '/' not in outputDir:
			outputDir = outputDir + '/'
		if not os.path.exists(outputDir):
			os.mkdir(outputDir)

		# 遍历从文件中获得的所以渠道号，将其写入APK包中
		for line in channels:
			target_channel = line.strip()
			target_apk = outputDir + target_channel+ "-" + src_apk_file_name
			shutil.copy(src_apk,  target_apk)
			zipped = zipfile.ZipFile(target_apk, 'a', zipfile.ZIP_DEFLATED)
			zipped.writestr("META-INF/td_channel.inf", target_channel)
			zipped.close()

	print('-'*50)
	print('打包完成 ,总共: ',len(channels), '个渠道包')



getChannel(path = input("请输入配置文件路径："));
getApks(dirPath = input("请输入母包目录/文件名："));
signApk(needSign = input('母包是否需要签名？ 1. 是  2. 否： '))
addChannelIntoApk(outputDir = input("请输入渠道包目录："));
