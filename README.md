# chatapp
			 ------------------------------------------------------||||||||||||||||||||||----------------------------------------------------------
								                                                 Server- setup
                                            
-------------------------------------------
i. Node-Red 
-------------------------------------------
1.Go to the site https://nodejs.org/en/download/ and download the 32/64 bit setup files for Node.js.

2.Double click on the downloaded .msi file to start the installation. Click the Run button in the first screen to begin the installation.

3.In the next screen, click the "Next" button to continue with the installation. In the next screen Accept the license agreement and click on the Next button. In the next screen, choose the location where Node.js needs to be installed and then click on the Next button. Accept the default components and click on the next button.

4.In the next screen, click the Install button to start the installation.

5.Click the Finish button to complete the installation.

6.Execute the following at the command prompt:

	> npm install -g --unsafe-perm node-red

7.After completion of the step 6, execute the following at the command promp

	> node-red

8.Open browser and open this link http://localhost:1880/.

9.Go to Menu and click on Manage Palette and open Install tab then click on search bar in install tab and search and install the following nodes.

	node-red-contrib-socketio 
	node-red-node-random
	node-red-node-mongodb
	node-red-node-base64
	node-red-contrib-mongodb2

              and Close the Command prompt.
10.Copy /setup/node-red-contrib-socketio folder from CD and paste and replace the folder in C:\Users\<your username>\.node-red\node_modules\

11.Open C:\Users\<username>\.node-red\setting.js in a editor and on line 210 paste the following statement :- mongodb:require("mongodb")
     and save the file.

12.Copy \setup\flows\ folder from CD and paste and replace the folder in C:\Users\<your username>\.node-red\lib

13.Repeat Step 6,7 and go to Menu>Import>Library and import the following libraries: - chat, gchat, login

14.Click on Deploy.


--------------------------------------------------------
ii.	MongoDB
--------------------------------------------------------
1.Open Browser and Go to https://www.mongodb.com/download-center/community and download the required setup.

2.Install the setup by prompted instructions.

3.MongoDB’s default data directory path is \data\db. Create this folder using the following commands from a Command Prompt:

	>md c: \data\db

	>"C:\ProgramFiles\MongoDB\Server\4.0\bin\mongod.exe" --dbpath c:\data

4.Open Browser and Go to following link and download and install MongoDB compass: https://www.mongodb.com/download-center/compass?jmp=hero

5.Open Command prompt and start the MongoDB service by executing:

	>mongod
6.Open MongoDB compass and click on connect at bottom.

7.Click on CREATE DATABASE and type ChatApp under Database Name and type Users under Collection Name and click on CREATE DATABASE.

8.Open ChatApp database and click on Create Collection and type chat under Collection Name and click on CREATE COLLECTION.


                                  
 ------------------------------------------------------||||||||||||||||||||||----------------------------------------------------------

							                                              Client Setup

--------------------------------------------------------------
i.Server IP Modification (If Applicable)
--------------------------------------------------------------
1.Unzip the source.zip to a suitable location.

2.Open Android Studio and After that you Click on “Open an existing Android Studio project”, browse and click on the unzipped folder and wait till gradle build and indexing is completed.

3.In Project Navigation Open app>java>com>company>my>chatapp> Constants.java 

	 Change the value of variable CHAT_SERVER_URL with http://<server ip address>:3000 
	 Open app>java>com>company>my>chatapp>utils>utils.java
	 Change the value of variable url  with http://<server ip address>:1880
	 And profile_url with "http://<server ip address>:1880/ChatApp?pic=" 

4.In Android Studio toolbar, go to Build > Generate Signed APK.

5.On the Generate Signed APK Wizard window, click Create new to create a new keystore. If you already have a keystore, go to step 4.

6.On the New Key Store window, provide the required information as shown in figure Your key should be valid for at least 25 years, so you can sign app updates with the same key through the lifespan of your app.

7.On the Generate Signed APK Wizard window, select a keystore (if existing), a private key, and enter the passwords for both. Then click Next.

8.On the next window, select a destination path for the signed APK and click Finish and transfer the following APK to mobile and follow the Client installation process.

-------------------------------------------------------------------
ii.APK Installation
-------------------------------------------------------------------
1.Open your Android's file manager app and locate the apk file.

2.Tap the APK file and Tap Install.

3.Tap DONE when prompted.
