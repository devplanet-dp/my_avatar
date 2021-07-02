# How to Build the Perfect fastlane Pipeline for Android

During a typical Android app development cycle every time you release or uodate your app, you have to build sign apk and add to Play Store then add metadata like screenshots, description, etc. It is the same thing but you have to repeat it every time you publish your app.  The good news is you can automate these tasks, saving your time for new features and bug fixing. All you have to do is use [Fastlane](https://fastlane.tools).

In this tutorial, you will learn how to build a local fatslane pipeline that will automate common Android tasks for a simple Android application called **My Avatar**. You can find the source code on [Github](https://github.com/devplanet-dp/my_avatar). By the end, you will learn how to:

1.Set up fastlane with your project
2.Automate Screenshots
3.Beta App distribution with Firebase for beta testers
4.Upload app's marketing material via fastlane
5.Play Store deplyoment

## Why Fastlane?

It is an **open-source** tool developed for simplifying Android and iOS deplyoment.**Fastlane** lets you automate tasks of your deplyoment and save your hours of development. **Fastlane** is powered by **Ruby** configuration file called **Fastfile** where you can add lanes to serve different purposes. 

## Setting Up fastlane

There are many ways of installing **fastlane**. I am using **Ruby** for the task as fastlane is a collection of **Ruby scripts**. You must have Ruby installed on your machine. You can simply confirm it by opening Terminal and running:
```
ruby -v
```
If Ruby is not installed, follow [their instructions here](https://www.ruby-lang.org/en/documentation/installation/) to install it on your machine.

Now you're ready to install fastlane. So Run the following command:

```
sudo gem install -n /usr/local/bin fastlane --verbose
```

You can also install fastlane using brew. Just run the `brew install fastlane` in your terminal. After running the commands you can see installation progress in Terminal. This could take several minutes. When the installation completes confirm the installation by running following command:
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

This tutorial uses a simple Android application called **My Avatar**. You can fin the source code [here on Github](https://github.com/devplanet-dp/my_avatar). Go inside yout project's root directory and enter the comman below:

```
fastlane init
```

If the fastlane is installed succssefully installed in your environment, You will see a output like this:

![fastlane_inti output](https://i.imgur.com/AjDT4IY.png)

Please enter your app unique [package name](https://developer.android.com/studio/build/application-id). This package name will be stored with fastlane for future tasks. Then you will be prompted to enter the path for service account **JSON** file. You are going to manage it later in this tutorial, So press **Enter** to skip for now. 

Next fastlane will prompt **Do you plan on uploading metadata, screenshots, and builds to Google Play using fastlane?**. You can either create brandnew metadata and screenshots or download exisiting metadata and setup them. Press **n**, You will set up this later.  

Once the fastlane completed installing required dependencies, you will prompted some guidlines and information on how to use fastlane with your project. You can press **Enter** and to continue. When you are done you can see a new directory **fastlane** is created inside your project containing:

* **Appfile** - file containing your package and signing informations.
* **Fastfile** - file containing automation configurations and lanes. 

## Configuring Fastlane

fastlane uses **Fastfile** to keep its automation configurations. fastlane groups different **actions** into **lanes**. A lane starts with `lane:name` where `name` is the name given to a lane. There is also **description** for a lane. Open the **Fastfile** and you will see:

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

As you can see there are already three lanes: **test,beta,deploy** inside the file.
 
* **test** - Runs all the tests of the project using the [Gradle](https://docs.fastlane.tools/actions/gradle/) action. 
* **beta** - Submits a beta build to. Firebase App Distribution using **gradle** and [crashlytics](https://docs.fastlane.tools/actions/crashlytics/) actions. 
* **deploy** - Deploys a new release version to Google Play using **gradle** and ** [upload_to_play](https://docs.fastlane.tools/actions/upload_to_play_store/) actions.

## Building your app with fastlane

You can user fastlane [gradle](https://docs.fastlane.tools/actions/gradle/) for all gradle related actions, including building and testing your Android app. Open the **Fastfile** and add following the `build` lane and save the file:

```
desc "Build"
lane :build do
  gradle(task: "clean assembleRelease")
end
```

To build the app with above created **lane** , open the Terminal inside your project folder and execute:

```
fastlane build
```

Once you run the command, your app will clean and assembles. Note that this will assembles and build only the `release` `buildType`. The signing will be done according to way you have declared in the `release` closure. In this tutorial you haven't configured any signing key yet. You can forgot it for now, as you going to configure it later. 

If your app builded successfully without errors, you will see the following messsage:

```
[13:37:40]: fastlane.tools finished successfully 🎉
```

## Screenshots with Screengrab

How if you could create hundreds of screenshots by a single command on Terminal. This will save your hours of time. You will only need to configure it once and anyone on your team can run it. Fastlane uses [screengrab](https://docs.fastlane.tools/actions/screengrab/) to automatically generate localized screenshots of your Android app. These can be generated for different device types. 

Before proceeding for the automation you need to add following permissions to your **src/debug/AndroidManifest.xml**.

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
Now you have added required permissions. You can use [Instrumentation Testing](https://developer.android.com/training/testing/unit-testing/instrumented-unit-tests) toolchain to set up screenshot automation. Before you begin open **app/build.gradle** and add the following `dependecies`:

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

Now sync **gradle** before moving on. These are the dependecies which are needed for fastlane to run the tests and generate screenshots. 

### Configuring Instrumentation Tests

You can find Insturmentation test class named **ExampleInstrumentedTest** inside your project's **app/src/androidTest/**. If you can not find any test classes you can create a new test file. Righ-click on **<your package name>(androidTest)** and select **New -> Kotlin File/Class:**.
 
 ![New Test file](https://i.imgur.com/zaepGkI.png)
 
 For the name, enter **ExampleInstrumentedTest**. Select **Class** from the type and press **Enter**.
 
 Next, you can implement the test function by adding following methods for **AndroidJUnit4** test:
 
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

 Now you have created a instrumention test for capturing screenshot.Next you can setup a **lane** to automate screenshots.
 
 ### Installing Screengrab
 
 As you already know you are going to use fastlane **screengrab** to automate screenshots. First you need to install **Screengrab**, you can do it via **gem**:
 ```
 sudo gem install screengrab
 ```
 
 Next initialize **screebgrab** by running following command inside your project:
 
 ```
 fastlane screengrab init
 ```
 To run **Screengrab** it requires `-debug.apk` and `-debug-AndroidTest.apk`.You can generate them by building the project with `./gradlew assembleDebug assembleAndroidTest` gradle task. After successfull build, you can find both the **APKs** inside project's `/app.build/outputs/apk/`. 
 
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
 
 Here is what this commands does:
 1. **android_home**: Sets tha path to the Android SDK.
 2. **use_adb_root**: Starts adb in root mode, giving you elevated permissions to writing to the device.
 3. **app_package_name**: Your project's package name.
 4. **app_apk_path and tests_apk_path **: Paths for APK files, which you are creating via fastlane.
 5. **locales**: Locales where you want to create screenshots. You can add locales as you required.
 6. **clear_previous_screenshots**: Clears all previously-generated screenshots in your local output directory before creating new ones.
 
### Running test on Emulator or Device
 
 To capture screenshots, fastlane will need to run your **APK**s on a Emulator or Device. 
 
> Note: If you run an emulator with API 24 or above, you must configure it with the **Google APIs** target. An emulator with **Google Play** won’t work because adb needs to run as root. That’s only possible with the Google APIs target.You can read more [here](https://developer.android.com/studio/run/managing-avds) on creating an emulator. However, if you run a device or emulator with API 23 or below, either option will work. See [comment #15788](https://github.com/fastlane/fastlane/issues/15788) under fastlane issues for more information.
 
 ### Adding a Lane
 
 Open the **Fastfile** and add a new lane to create screenshots:

```
 desc "Build debug,test APK for screenshots and grab screenshots"
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
 
 This will start the screenshot-grabbing process. You will see some errors, just ignore them. When the execution completed successfully, it will open your screenshots on your browser. You also can find them inside `/fastfile/metadata/` directory. 
 
 Congratulations!you have created screenshots with fastlane. It's time to keep your screenshots perfectly up-yo-date with every app update. You only have to do is to run the lane.

## Beta App Distribution with fastlane

When you have a new update ready, you may want to share it with beta testers to get feedback before releasing to the production. To do that you have to upload your Android app to a app ditribution provider. Fastlane allows you to [beta app deplyoment](https://docs.fastlane.tools/getting-started/android/beta-deployment/) with multiple beta app providers. 

The supported beta testing services with fastlane are:

 - Google play
 - Firebase App Distribution

Your next step is to distrubute your app with [Firebase App Distribution](https://firebase.google.com/docs/app-distribution), which makes app distributing painless and provides a better user management experience. 

### Using Firebase CLI

To get started, you have to add your project to Firebase. Open your browser and go to [Firebase](https://firebase.google.com). To create a new project, click on **Go to Console** button on top-right of the screen. Then create a new Firebase project by clicking on **Add project**:

![Add firebase project](https://i.imgur.com/Ke24cP8.png)

Give a project name and continue. Once the project creation is completed you can click on **continue** to view the project's console. 

Fastlane uses **Firebasse CLI** to upload your builds to Firebase App Ditribution.  Please follow the instruction [here](https://firebase.google.com/docs/cli) to install Firebase CLI on your OS.

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

Now you have installed the plugin successfully. Next you to have to **Add Firebase to your Android app**. You can follow these [steps here](https://firebase.google.com/docs/android/setup) to add Firebase to your project. 

Once you are completed with adding Firebase, open the [General Settings [age](https://console.firebase.google.com/u/0/project/_/settings/general) and copy down your **App ID**, you are going to need it later. 

![App ID](https://i.imgur.com/fe927OA.png)

Now you can create groups with different users and release beta versions to them. In order to do that navigate to **App Distribution** tab and press **Get Started**.

![App Distribution](https://i.imgur.com/4DNQESk.png)

Then go to the **Testers and Groups** tab and create a group. You can name it as **group-one**. 

![Add group](https://i.imgur.com/tNCvtfQ.png)

Finally, you can add **testers**,  It is better to enter your email to add yourself as a tester.  Once all done, you are ready to upload your first **beta build** with fastlane. 

###  Deploying for Beta Testing

Open **Fastfile** and replace `beta` lane with following configurations. Remember to replace `app` with your **App ID** you copied previously:

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

Please navigate to [Firebase App Distribution](https://console.firebase.google.com/u/0/project/_/appdistribution), you can see the build is available on console:

![Beta build](https://i.imgur.com/Mzg9lNG.png)

The users in your test group also will receive an email with instruction, like below:

![Beta email](https://i.imgur.com/uxMHayk.png)

Congratulations! You deployed your **beta** build to your testers with fastlane. Next time you can send updates with a single command on Terminal. You can read more about avaialble options with `firebase_app_distribution` on official documentation [here.](https://firebase.google.com/docs/app-distribution/android/distribute-fastlane)

## Releasing to the Play Console

