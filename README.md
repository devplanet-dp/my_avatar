# How to Build the Perfect fastlane Pipeline for Android

During a typical Android app development cycle when you're ready to prepare a **release** version of your app, you might sigining your app, capturing screenshot and collecting metadata. The good news is you can automate these tasks, saving your time for new features and bug fixing. All you have to do is use [Fastlane](https://fastlane.tools).

In this tutorial, you will learn how to build a local fatslane pipeline that will automate common Android tasks for a simple Android application called **My Avatar**. You can find the source code on [Github](https://github.com/devplanet-dp/my_avatar). By the end, you will learn how to:
1.
2.
3.

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
 
 Note: If you run an emulator with API 24 or above, you must configure it with the **Google APIs** target. An emulator with **Google Play** won’t work because adb needs to run as root. That’s only possible with the Google APIs target.You can read more [here](https://developer.android.com/studio/run/managing-avds) on creating an emulator. However, if you run a device or emulator with API 23 or below, either option will work. See [comment #15788](https://github.com/fastlane/fastlane/issues/15788) under fastlane issues for more information.
 
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
 
 Congratulations!you have created screenshots with fastlane. It's time to keep your screenshots perfectly up-yo-date with every app update. You only have to do is run the lane.
