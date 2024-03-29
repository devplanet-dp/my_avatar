

# How to Build the Perfect fastlane Pipeline for Android

During a typical Android app development cycle whenever you release or update your app, you have to assemble the source code into **APK** or **App Bundle** which you deploy to the Play Store. It seems like a simple process but there are several other steps involved, like signing the package and updating metadata like screenshots, descriptions, changelogs, etc. It is the same thing but you have to repeat it every time you publish your app. 

 The good news is you can automate these tasks, saving your time for new features and bug fixing. All you have to do is use [Fastlane](https://fastlane.tools).

In this tutorial, you will learn how to build a local Fastlane pipeline that will automate common Android tasks for a simple Android application called **My Avatar**. You can find the source code on [Github](https://github.com/devplanet-dp/my_avatar). By the end, you will learn how to:

1.Set up fastlane with your project
2.Automate Screenshots
3.Beta App distribution with Firebase for beta testers
4.Upload app's marketing material via fastlane
5.Play Store deployment

## Why Fastlane?

It is an **open-source** tool developed for simplifying Android and iOS deployment.**Fastlane** lets you automate your development and release process and save hours of development. **Fastlane** is powered by **Ruby** configuration file called **Fastfile** where you can add lanes to serve different purposes. 

## Setting Up fastlane

There are many ways of installing **fastlane**. This tutorial uses **Ruby** for the task as fastlane is a collection of **Ruby scripts**. You must have Ruby installed on your machine. You can simply confirm it by opening Terminal and running:
```
ruby -v
```
If Ruby is not installed, follow [their instructions here](https://www.ruby-lang.org/en/documentation/installation/) to install it on your machine.

Now you're ready to install fastlane. So Run the following command:

```
sudo gem install -n /usr/local/bin fastlane --verbose
```

You can also install fastlane using brew. Just run the `brew install fastlane` in your terminal. After running the commands you can see installation progress in Terminal. This could take several minutes. When the installation completes, confirm the installation by running the following command:
 ```
 fastlane --version
 ```
It will show you output like this
```
 fastlane installation at path:
/usr/local/Cellar/fastlane/2.185.0/libexec/gems/fastlane-2.185.0/bin/fastlane
-----------------------------
[✔] 🚀 
fastlane 2.185.0
```
Congratulations!, you're ready to use fastlane in your project. 

## Configuring fastlane with your project

This tutorial uses a simple Android application called **My Avatar**. You can find the source code [here on Github](https://github.com/devplanet-dp/my_avatar). Go inside your project's root directory and enter the command below:

```
fastlane init
```

If the fastlane successfully installed in your environment, You will see an output like this:

![fastlane_inti output](https://i.imgur.com/AjDT4IY.png)

Please enter your app unique [package name](https://developer.android.com/studio/build/application-id). This package name will be stored with fastlane for future tasks. Then you will be prompted to enter the path for service account **JSON** file. You are going to manage it later in this tutorial, So press **Enter** to skip for now. 

Next, fastlane will prompt **Do you plan on uploading metadata, screenshots, and builds to Google Play using fastlane?**. You can either create brand new metadata and screenshots or download existing metadata and set up them. Press **n**, You will set up this later.  

Once the fastlane completed installing the required dependencies, you will prompt some guidelines and information on how to use fastlane with your project. You can press **Enter** and continue. When you are done you can see a new directory **fastlane** is created inside your project containing:

* **Appfile** - file containing your package and signing information.
* **Fastfile** - file containing automation configurations and lanes. 

## Understanding actions and lanes

Fastlane uses **Fastfile** to keep its automation configurations. fastlane groups different **actions** into **lanes**. A lane starts with `lane:name` where `name` is the name given to a lane. There is also **description** for a lane. Open the **Fastfile** and you will see:

```
default_platform(:android)

platform :android do
  desc "Runs all the tests"
  lane :test do
    gradle(task: "test")
  end

  desc "Submit a new Beta Build to Crashlytics Beta"
  lane :beta do
    gradle(task: "clean assembleRelease")
    crashlytics
  
    # sh "your_script.sh"
    # You can also use other beta testing services here
  end

  desc "Deploy a new version to the Google Play"
  lane :deploy do
    gradle(task: "clean assembleRelease")
    upload_to_play_store
  end
end
```

As you can see there are already three lanes: **test, beta, deploy** inside the file.
 
* **test** - Runs all the tests of the project using the [Gradle](https://docs.fastlane.tools/actions/gradle/) action. 
* **beta** - Submits a beta build to. Firebase App Distribution using *gradle* and [crashlytics](https://docs.fastlane.tools/actions/crashlytics/) actions. 
* **deploy** - Deploys a new release version to Google Play using *gradle* and  [upload_to_play_store](https://docs.fastlane.tools/actions/upload_to_play_store/) actions.

## Building your app with fastlane

You can use fastlane [gradle](https://docs.fastlane.tools/actions/gradle/) for all Gradle related actions, including building and testing your Android app. You can configure the **gradle** action with provided parameters by fastlane. Here are some of them. 
### Build types
 When you create a new Android project, you get two build types as `debug` and `release`.  You can add your configurations inside each **buid type**, if you want to add or change certain settings.  The following code shows build type settings using in the My Avatar project. 
```
buildTypes {  
  debug {  
  applicationIdSuffix ".debug"  
  debuggable true  
  }  
  
  release {  
  signingConfig signingConfigs.release  
  minifyEnabled false  
  proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'  
  }  
}
```
### Product flavors
Product flavors are similar to creating build types. The flavors can provide their features to your gradle task.  You can read more on [product flavors](https://developer.android.com/studio/build/build-variants) if you haven't created them already. Here is an example of product flavors, which can be added to your project:
```
flavorDimensions "version"  
productFlavors {  
  create("demo") {  
  dimension = "version"  
  applicationIdSuffix = ".demo"  
  versionNameSuffix = "-demo"  
  }  
  create("full") {  
  dimension = "version"  
  applicationIdSuffix = ".full"  
  versionNameSuffix = "-full"  
  }  
}
```
### Incrementing Version Code
Version code is a critical component of your app update and maintenance strategy. The `version code` determines whether one version is more recent than another. You need to increase the value of `version code` with each release, regardless of whether the release is a major or minor. To update your `version code`, you have to edit your app's `build.gradle`  again and again. 

Versioning is now much easier with fastlane. You only have to do is add the plugin below:
```
fastlane add_plugin increment_version_code
```
 
Once the plugin installed successfully, open the **Fastfile** and add the **lane** below to increment `version code` automatically:
```
 desc "Increment version code"
  lane :increment_vc do
  increment_version_code(
        gradle_file_path: "./app/build.gradle",
    
  )
  ```

Now, run the lane in your Terminal:
```
fastlane increment_vc
```
Once the execution is completed, check your `version code` inside `build.gradle` file. It has been incremented right?. How cool is that!. Now, It is time to add this lane for every build hereafter. It will take care of versioning. 

### Configuring build lane
Now you have an idea on adding **build types** and **flavors** to your project. You can define them with fastlane **gradle** task. Open the **Fastfile** and add the following the `build` lane and save the file:

```
desc "Build"
lane :build do
 
   gradle(
      task: "assemble",
      flavor: "demo",
      build_type: "Release"
    )
end
```

To build the app with above created **lane**, open the Terminal inside your project folder and execute:

```
fastlane build
```

Once you run the command, your app will clean and assembles. Note that this will assembles and build only the `release` `build type`. The signing will be done according to the way you have declared in the `release` closure. In this tutorial, you haven't configured any signing key yet. You can forget it for now, as you going to configure it later. 

If your app built successfully without errors, you will see the following message:

```
[13:37:40]: fastlane.tools finished successfully 🎉
```

## Screenshots with Screengrab

How if you could create hundreds of screenshots by a single command on Terminal. This will save you hours. You will only need to configure it once and anyone on your team can run it. Fastlane uses [screengrab](https://docs.fastlane.tools/actions/screengrab/) to automatically generate localized screenshots of your Android app. These can be generated for different device types. 

Before proceeding with the automation you need to add the following permissions to your **src/debug/AndroidManifest.xml**.

```
<!-- Allows unlocking your device and activating its screen so UI tests can succeed -->
    <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!-- Allows for storing and retrieving screenshots -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        tools:ignore="ScopedStorage" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

    <!-- Allows changing locales -->
    <uses-permission
        android:name="android.permission.CHANGE_CONFIGURATION"
        tools:ignore="ProtectedPermissions" />
    
```    
Now you have added the required permissions. You can use [Instrumentation Testing](https://developer.android.com/training/testing/unit-testing/instrumented-unit-tests) toolchain to set up screenshot automation. Before you begin open **app/build.gradle** and add the following `dependecies`:

```
testImplementation 'junit:junit:4.13.2'
    
androidTestImplementation 'androidx.test.ext:junit:1.1.2'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
androidTestImplementation 'androidx.test:rules:1.4.0'

androidTestImplementation 'tools.fastlane:screengrab:2.0.0'
```    

Your project may already have some of these `dependencies` which are initially added when project creation. 

Next, Inside `defaultConfig` block, add `testInstrumentationRunner`:
```
testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
```

Now sync **gradle** before moving on. These are the dependencies that are needed for fastlane to run the tests and generate screenshots. 

### Configuring Instrumentation Tests

You can find Insturmentation test class named **ExampleInstrumentedTest** inside your project's **app/src/androidTest/**. If you can not find any test classes you can create a new test file. Right-click on **<your package name>(androidTest)** and select **New -> Kotlin File/Class:**.
 
 ![New Test file](https://i.imgur.com/zaepGkI.png)
 
 For the name, enter **ExampleInstrumentedTest**. Select **Class** from the type and press **Enter**.
 
 Next, you can implement the test function by adding the following methods for **AndroidJUnit4** test:
 
 ```
 package com.devplanet.myavatar

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // JVMField needed!
    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Test
    fun testTakeScreenshot() {
        activityRule.launchActivity(null)
        //prepares to take a screenshot of the app
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        Espresso.onView(ViewMatchers.withId(R.id.genrateButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //Takes screenshot of the first screen
        Screengrab.screenshot("myAvatar_before_click")

        //Trigger the generate button onClick function
        Espresso.onView(ViewMatchers.withId(R.id.genrateButton))
            .perform(ViewActions.click())

        //Takes another screenshot
        Screengrab.screenshot("myAvatar_after_click")
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.devplanet.myavatar", appContext.packageName)
    }
}
```

 Now you have created an instrumentation test for capturing the screenshot. Next, you can set up a **lane** to automate screenshots.
 
 ### Installing Screengrab
 
 As you already know you are going to use fastlane **screengrab** to automate screenshots. First, you need to install **Screengrab**, you can do it via **gem**:
 ```
 sudo gem install screengrab
 ```
 
 Next, initialize **screebgrab** by running the following command inside your project:
 
 ```
 fastlane screengrab init
 ```
 To run **Screengrab** requires `-debug.apk` and `-debug-AndroidTest.apk`.Because **Screengrab** requires to install your app on a device to grab screenshots. You can generate them by building the project with `./gradlew assembleDebug assembleAndroidTest` gradle task. After a successful build, you can find both the **APKs** inside project's `/app.build/outputs/apk/`. 
 
 ### Configuring screengrab
 
 Go inside the **fastlane** directory. You will see a newly created **Screengrabfile**, this is where you will save your **screengrab** configurations.
 Open the **Screengrabfile** and replace with the content below and save:
 
 ```
 # 1
android_home('$PATH')

# 2
use_adb_root(true)

# 3
app_package_name('com.devplanet.myavatar')

# 4
app_apk_path('app/build/outputs/apk/debug/app-debug.apk')
tests_apk_path('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')

# 5
locales(['en-US', 'fr-FR', 'it-IT'])

# 6
clear_previous_screenshots(true)
 ```
 
 Here is what these commands do:
 1. **android_home**: Sets the path to the Android SDK.
 2. **use_adb_root**: Starts ADB in root mode, giving you elevated permissions to writing to the device.
 3. **app_package_name**: Your project's package name.
 4. **app_apk_path and tests_apk_path ** : Paths for APK files, which you are creating via fastlane.
 5. **locales**: Locales where you want to create screenshots. You can add locales as you required.
 6. **clear_previous_screenshots**: Clears all previously-generated screenshots in your local output directory before creating new ones.
 
### Running test on Emulator or Device
 
 To capture screenshots, fastlane will need to run your **APK**s on an Emulator or Device. 
 
> Note: If you run an emulator with API 24 or above, you must configure it with the **Google APIs** target. An emulator with **Google Play** won’t work because ADB needs to run as root. That’s only possible with the Google APIs target. You can read more [here](https://developer.android.com/studio/run/managing-avds) on creating an emulator. However, if you run a device or emulator with API 23 or below, either option will work. See [comment #15788](https://github.com/fastlane/fastlane/issues/15788) under fastlane issues for more information.
 
 ### Adding a Lane
 
 Open the **Fastfile** and add a new lane to create screenshots:

```
 desc "Build debug, test APK for screenshots and grab screenshots"
lane :grab_screens do
  gradle(
    task: 'clean'
  )
  gradle(
    task: 'assemble',
    build_type: 'Debug'
  )
  gradle(
    task: 'assemble',
    build_type: 'AndroidTest'
  )
 screengrab
end
 ```
 
 From now you can run this lane and create new screenshots. Run following command:
 
 ```
 fastlane grab_screens
 ```
 
 This will start the screenshot-grabbing process. You will see some errors, just ignore them. When the execution is completed successfully, it will open your screenshots on your browser. You also can find them inside `/fastfile/metadata/` directory. 
 
 Congratulations! you have created screenshots with fastlane. It's time to keep your screenshots perfectly up-to-date with every app update. You only have to do is to run the lane.

## Beta App Distribution with fastlane

When you have a new update ready, you may want to share it with beta testers to get feedback before releasing it to production. To do that you have to upload your Android app to an app distribution provider. Fastlane allows you to [beta app deplyoment](https://docs.fastlane.tools/getting-started/android/beta-deployment/) with multiple beta app providers. 

The supported beta testing services with fastlane are:

 - Google play
 - Firebase App Distribution

Your next step is to distribute your app with [Firebase App Distribution](https://firebase.google.com/docs/app-distribution), which makes app distribution painless and provides better user management experience. 

### Using Firebase CLI

To get started, you have to add your project to Firebase. Open your browser and go to [Firebase](https://firebase.google.com). To create a new project, click on **Go to Console** button on the top-right of the screen. Then create a new Firebase project by clicking on **Add project**:

![Add firebase project](https://i.imgur.com/Ke24cP8.png)

Give a project name and continue. Once the project creation is completed you can click on **continue** to view the project's console. 

Fastlane uses **Firebase CLI** to upload your builds to Firebase App Distribution.  Please follow the instruction [here](https://firebase.google.com/docs/cli) to install Firebase CLI on your OS.

After the installation completed, sign in to your Firebase account using the command below:
```
firebase login
```
Before proceeding with Firebase App Distribution you need to install the firebase plugin to fastlane. 

Run the following command:

```
fastlane add_plugin firebase_app_distribution
```
 
 Your Terminal will prompt you like this:

```
[17:45:48]: Plugin 'fastlane-plugin-firebase_app_distribution' was added to './fastlane/Pluginfile'

[17:45:48]: It looks like fastlane plugins are not yet set up for this project.

[17:45:48]: fastlane will modify your existing Gemfile at path '/Users/niroshana/Desktop/fastlane-Android/project/Gemfile'

[17:45:48]: This change is necessary for fastlane plugins to work

[17:45:48]: Should fastlane modify the Gemfile at path '/Users/niroshana/Desktop/fastlane-Android/project/Gemfile' for you? (y/n)
```

Press `y` to install the plugin and continue. 

Now you have installed the plugin successfully. Next you have to Add **Firebase** to your Android app. You can follow these [steps here](https://firebase.google.com/docs/android/setup) to add Firebase to your project. 

Once you are completed with adding Firebase, open the [General Settings page](https://console.firebase.google.com/u/0/project/_/settings/general) and copy down your **App ID**, you are going to need it later. 

![App ID](https://i.imgur.com/fe927OA.png)

Now you can create groups with different users and release beta versions to them. To do that navigate to **App Distribution** tab and press **Get Started**.

![App Distribution](https://i.imgur.com/4DNQESk.png)

Then go to the **Testers and Groups** tab and create a group. You can name it as **group-one**. 

![Add group](https://i.imgur.com/tNCvtfQ.png)

Finally, you can add **testers**,  It is better to enter your email to add yourself as a tester.  Once all is done, you are ready to upload your first **beta build** with fastlane. 

###  Deploying for Beta Testing

Open **Fastfile** and replace `beta` lane with the following configurations. Remember to replace `app` with your **App ID** you copied previously:

```
desc "Submit a new Beta Build to Firebase App Distribution"
lane :beta do
  build

  firebase_app_distribution(
      app: "1:733973662153:android:96a0d652bf41e0ca784018",
      groups: "group-one",
      release_notes: "Lots of new avatars to try out!"
  )
end
```

The above `beta` lane will upload your build to **group-one** test group.  On your Terminal run the `beta` lane. 

```
fastlane beta
```

When the execution is completed, you can see the following output:

```
[19:38:15]: 🔐 Authenticated successfully.

[19:38:18]: ⌛ Uploading the APK.

[19:38:33]: ✅ Uploaded the APK.

[19:38:35]: ✅ Posted release notes.

[19:38:37]: ✅ Added testers/groups.

[19:38:37]: 🎉 App Distribution upload finished successfully.
------
[19:38:37]: fastlane.tools finished successfully 🎉
```

Please navigate to [Firebase App Distribution](https://console.firebase.google.com/u/0/project/_/appdistribution), you can see the build is available on the console:

![Beta build](https://i.imgur.com/Mzg9lNG.png)

The users in your test group also will receive an email with instructions, like below:

![Beta email](https://i.imgur.com/uxMHayk.png)

Congratulations! You deployed your **beta** build to your testers with fastlane. Next time you can send updates with a single command on Terminal. You can read more about available options with `firebase_app_distribution` on official documentation [here.](https://firebase.google.com/docs/app-distribution/android/distribute-fastlane)

## Releasing to the Play Console

Finally, you have come to release your app to users. Before proceeding, you need to have a purchased **Google Play** account. If you don't have one, you can create one from [here](https://play.google.com/console/u/0/signup).

Fastlane uses [supply](https://docs.fastlane.tools/actions/supply/) to upload app metadata, screenshots, and binaries to Google Play. **Supply** need to have at least one version uploaded in the Google Play Console because fastlane cannot create a new listing on the Play Store. 

### Creating your app
You can follow the steps on the official [Play Console Help](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en) to create and set up your app.

Once you have created your app on Play Console, you have to upload your app to one of the available tracks so the Play Console knows your app's **package** name. Remember that you do not need to publish the app.

### Configure signing in gradle

Android requires that all APKs or App Bundles be digitally signed with a certificate before they are uploaded to Play Console. If you haven't generated an **upload key** yet, you can follow the steps [here](https://developer.android.com/studio/publish/app-signing) to create one.

When you have your key, the best practice is to remove signing information from your build files. So it is not easily accessible to others. To do this, you should create a separate **properties** file to keep key information and refer to that file in your build files. Navigate to your app's `build.grdle` file and add the following configurations.

Add a reference to **key.properties**  file inside build gradle before the `android` block:

```
def keystoreProperties = new Properties()  
def keystorePropertiesFile = rootProject.file('key.properties')  
if (keystorePropertiesFile.exists()) {  
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))  
}
```
Now, you can configure a single signing configuration for your **release** build type as below. 
```
signingConfigs {  
  release {  
  keyAlias keystoreProperties['keyAlias']  
        keyPassword keystoreProperties['keyPassword']  
        storeFile keystoreProperties['storeFile'] ? file(keystoreProperties['storeFile']) : null  
  storePassword keystoreProperties['storePassword']  
    }  
}  
  
buildTypes {  
  release {  
  signingConfig signingConfigs.release  
        minifyEnabled false  
  proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'  
  }  
}
```

Once you have configured your app signing, your Gradle will take care of App Signing on every release build automatically. 

### Building Android App Bundle

Currently, [Android App Bundles](https://developer.android.com/platform/technology/app-bundle) are preferred over APKs. You can modify **Fastfile** to build an **app bundle** for you. 

Locate your **Fastfile** and add following lane:

```
  desc "Build release app bundle"
 lane :build_bundle do
  gradle(task: "clean bundleRelease")
 end
 ```

Next, execute the lane on Terminal:

```
fastlane build_bundle
```

Once the execution is completed, you can find your bundle in your apps build directory. The default location is `app/build/outputs/bundle/release`. Now you have an **app bundle** file which is ready to upload to Play Console. You have to upload this build manually prior automating the deployment using fastlane. 

Now, go to Play Console and select **Production** track and create a release. 

![Create a release](https://i.imgur.com/SvLVYpQ.png)


You can enable **Play App Signing** and upload your **app-release.aab** bundle file created previously.  Since this is the first upload of your app, you can provide a short description regarding this release on **Release notes**.  Tap on **save** and navigate to **All apps** where your list of apps appears, you can see your app is available with your package name. 

For now, you have completed uploading your first build manually to Play Store. Next, you can move to automate this process with fastlane. 

### Creating Play Console Credentials

To connect fastlane with Play Console, you need to provide appropriate access with credentials. To achieve this, all you need is an **API key** file. This file contains the credentials that fastlane uses to connect to your Play Console account. 

To create a key file, please follow these steps from the [fastlane official documentaion](https://docs.fastlane.tools/getting-started/android/setup/#collect-your-google-credentials)

When you have a key file created from the following previous steps, you can connect fastlane with your Play Console APIs.

 Run this command and verify your key file:
 ```
 fastlane run validate_play_store_json_key json_key:/path/to/your/downloaded/file.json
 ```

If the Play Store connected successfully with your key file, your Terminal will prompt the message below:
```
Successfully established connection to Google Play Store.
```

Now it is time to add a key file to **fastlane**. Open **fastlane/Appfile** and update `json_key_file` property with your key file location. In this tutorial it is located inside the **root** directory of the project:
```
json_key_file("./api-key.json")
```

### Updating Metadata

Fastlane has the feature of uploading your app's metadata including screenshots, descriptions, and release notes. This is the best way to manage your metadata perfectly up to-date with every release. 

To update your metadata, first, you need to download your app's existing metadata in **Play Store** into your local.  You only have to do is run the following code:
```
fastlane supply init
```

> If you followed the previous steps on creating screenshots in this tutorial, you will already have a screenshot directory created. Please remove that folder before initializing supply.

This command will download any existing content from your  Play Store Console. You can see the following output, when the execution completed successfully:
```
[21:19:56]: 🕗  Downloading metadata, images, screenshots...

[21:19:59]: 📝  Downloading metadata (en-US)

[21:19:59]: Writing to fastlane/metadata/android/en-US/title.txt...

[21:19:59]: Writing to fastlane/metadata/android/en-US/short_description.txt...

[21:19:59]: Writing to fastlane/metadata/android/en-US/full_description.txt...

[21:19:59]: Writing to fastlane/metadata/android/en-US/video.txt...

[21:19:59]: 🖼️  Downloading images (en-US)

[21:19:59]: Downloading `featureGraphic` for en-US...

[21:19:59]: Downloading `icon` for en-US...

[21:20:00]: Downloading `tvBanner` for en-US...

[21:20:01]: Downloading `phoneScreenshots` for en-US...

[21:20:01]: Downloading `sevenInchScreenshots` for en-US...

[21:20:02]: Downloading `tenInchScreenshots` for en-US...

[21:20:03]: Downloading `tvScreenshots` for en-US...

[21:20:03]: Downloading `wearScreenshots` for en-US...

[21:20:06]: Found '1 (1.0)' in 'production' track.

[21:20:06]: 🔨  Downloading changelogs (en-US, 1 (1.0))

[21:20:06]: Writing to fastlane/metadata/android/en-US/changelogs/1.txt...

[21:20:07]: ✅  Successfully stored metadata in 'fastlane/metadata/android'
```

You can see, your downloaded content is available at `fastlane/metadata/android` . Next time when you updating your app's metadata, you only have to update these local files. 

For example, If you need to upload screenshots, you can run the lane you created previously to generate screenshots.
```
fastlane capture_screen
```
Now, you will see your screenshots in `metadata/android/en-US/images/phoneScreenshots`. Run the following command to upload changes:
```
fastlane supply --skip_upload_changelogs
```
The `--skip_upload_changelogs` property skips uploading changelogs. It means you do not have to increment your app's **version code** when updating the metadata. 

When the command runs successfully, navigate to your **Play Console**  and inside your app's **Main Store listing** you can see the screenshots. 

![Play Console screenshots](https://i.imgur.com/TU3CveC.png)

Congratulations!, You have now configured fastlane to retrieve and update app metadata. Your next step is to upload a build to **Play Store**.

### Deploying to Play Store

In the previous section, you created an **Android App Bundle**  and uploaded it to **Play Console** manually. Hereafter, you can do it running a single command on your Terminal. 

Update your **deploy** lane inside **Fastfile** with following configurations:
```
desc "Deploy a new version to the Google Play"
  lane :deploy do
	#1
    increment_vc
    #2
    build_bundle
    #3
    upload_to_play_store(release_status:"draft")
  end
  ```

 1. **increment_vc**: The lane you created previously to increment your app's `version code`.
 2. **build_bundle**: The build release app bundle lane was created by you previously. 
 3. **upload_to_play_store**: Uploads Android App Bundle to Play Console. `release_status` property is used to maintain the status of the apks/aabs when uploaded. You can find more parameters available on **supply** [here](https://docs.fastlane.tools/actions/supply/).

Now you can just run ``fastlane deploy`` and fastlane will take care of everything in your new release. When the command completes, you can see your new build appears in **Play Console**.

## Conclusion

In this tutorial, you built a Pipeline for Android development workflows using  **fastlane**. It adds value to your regular Android development workflow by saving hours that you have spent on building, testing, and releasing apps. You can make your automation task more advanced and suited for your exact needs when you become comfortable with it.

If you are interested in other ways to handle this and get more ideas, check out  [official documentation](https://fastlane.tools/). Hopefully, you got an idea of how you could automate your next Android project.

[Runway](https://www.runway.team/features)  has brought the automation process of mobile app development to the next level. It is designed to work as an integration layer across all the team’s tools. This brings more value to you rather than seeing all of them in Terminal. How if you can schedule an automatic release?. Yes, it is achievable with Runway. There are many advanced features with  **Runway**  where you can thinking to switch from  **Fastlane**  such as visualizing and sharing the release progress with your team members, scheduling a release, handling App Store Connect and Google Play Console in one place, and many more. Every team member can see exactly where they are in the release cycle and what still needs to be done. Finally  **Runway**  is a tool that is worth trying with your mobile development.
