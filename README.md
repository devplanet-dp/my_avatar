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
 
 Next, you can implement the test function by adding following methods:
 
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
        //1
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        Espresso.onView(ViewMatchers.withId(R.id.genrateButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //2
        Screengrab.screenshot("myAvatar_before_click")

        //3
        Espresso.onView(ViewMatchers.withId(R.id.genrateButton))
            .perform(ViewActions.click())

        //4
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
