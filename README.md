# How to build the perfect fastlane pipeline for Android

Building Android apps is an exhilarating experience, and successfully overcoming the various development challenges brings a sense of accomplishment. However, the true thrill comes when you submit your app for review in the Google Play Store. It marks the beginning of a new journey, akin to a baby taking its first steps—a moment filled with excitement and anticipation that opens up a world of possibilities.

Unfortunately, the subsequent process of submitting updates can be lengthy, repetitive, and monotonous, often becoming a dreaded task for engineers. From signing the package and updating app metadata to adding screenshots, checking descriptions, and updating change logs, the repetitive nature of these tasks can be discouraging. It's similar to the initial joy of owning a car and performing its first wash or oil change, only to realize that it needs to be done repeatedly, dampening the enthusiasm.

Thankfully, there is a way to automate these tasks and allow engineers to focus on what truly matters. One such automation tool that simplifies Android and iOS development and deployment is [fastlane](https://fastlane.tools/), an open-source set of tools. By [incorporating fastlane](https://www.runway.team/blog/a-newcomers-guide-to-fastlane) into your development workflow, you can streamline the process and regain your productivity.

In this tutorial, we will explore how to build a local fastlane pipeline that automates all the necessary steps to build and update an Android application, ensuring it is ready for the Google Play Store. The final source code can be found on [GitHub](https://github.com/devplanet-dp/my_avatar). Throughout this tutorial, you will learn how to:

1.  Set up fastlane in your project
2.  Automatically capture and save screenshots
3.  Distribute the app to beta testers
4.  Upload marketing materials and release notes
5.  Deploy the finalized bundle to the Google Play Store

If you're seeking a similar guide for iOS, you can refer to [my previous tutorial here](https://runway.team/blog/how-to-build-the-perfect-fastlane-pipeline-for-ios). This tutorial aims to empower you with the knowledge and tools to automate your Android app deployment process, freeing up your time and allowing you to focus on more important aspects of your work.

### Setting up fastlane

There are many ways to install fastlane, but for this tutorial, we’ll use  [Ruby](https://www.ruby-lang.org/en/).

You must have Ruby installed on your machine. To confirm that this is the case, open your terminal and run:

`ruby -v `

If Ruby is not installed, follow  [the instructions here](https://www.ruby-lang.org/en/documentation/installation/)  to add it.

To install fastlane, run the following command in your terminal:

`sudo gem install fastlane --verbose`

> fastlane is officially supported to run on macOS. Some actions and tools may not work accordingly on other platforms.

When the installation completes, confirm it by running the following command:
```
fastlane --version  
  
Output:  
fastlane installation at path:
/Users/user/.rbenv/versions/3.2.2/lib/ruby/gems/3.2.0/gems/fastlane-2.212.2/bin/fastlane
-----------------------------
[✔] 🚀
fastlane 2.212.2  
```

Now you’re ready to use fastlane for your next Android project. Let’s configure some of the basic options you’ll need to get started.

### Configuring fastlane for an Android project

To add fastlane to an existing project, go to your project’s root directory and enter:

`bundle exec fastlane init`

Next, enter your app’s unique  [package name](https://developer.android.com/studio/build/application-id). This package name will be stored in fastlane for future tasks.

The terminal prompt will then ask for the path to your service account JSON file, but we’ll handle that later in the tutorial, so press  **Enter**  to skip it for now.

Next, fastlane will ask if you plan on uploading metadata, screenshots, and builds to Google Play. You can either create brand new app metadata and screenshots or download the existing metadata if your app has already been submitted. For now, press  **n**  to skip this step, as we’ll set it up later in the tutorial.

When the installation is finished, you will see a new directory called  `fastlane`  that contains:

-   **Appfile**  - contains your package and signing information
-   **Fastfile**  - contains automation configurations and lanes

> In addition to the fastlane directory, a Gemfile and Gemfile.lock are
> also created to manage the plugins needed by fastlane. Please make
> sure to add and commit all these files into version control.

### Understanding fastlane actions and lanes

fastlane stores automation configurations in the Fastfile where it groups different  *actions*  into  *lanes*. These lanes represent different release processes, and the actions are steps taken within each lane.

A lane starts with  `lane:name`  and then includes an optional description in the  `desc`  field. Open your Fastfile, and you will see:

```
default_platform(:android)    
    
platform :android do    
    desc "Runs all the tests"    
    lane :test do    
    gradle(task: "test")    
    end    
    
    desc "Submit a new Beta Build to Firebase App Distribution"    
    lane :beta do    
    gradle(task: "clean assembleRelease")    
    firebase_app_distribution    
        
    # sh "your_script.sh"    
    # You can also use other beta testing services here    
	end    
    
    desc "Deploy a new version to Google Play"    
    lane :deploy do    
    gradle(task: "clean assembleRelease")    
    upload_to_play_store    
    end 
end
```
As you can see, there are already three lanes inside the file:  *test, beta, deploy*.

-   **test**  - Runs all the tests for the project using the  [Gradle](https://docs.fastlane.tools/actions/gradle/)  action
-   **beta**  - Submits a beta build using the Gradle and  [firebase_app_distribution](https://github.com/fastlane/fastlane-plugin-firebase_app_distribution)  actions.
-   **deploy**  - Deploys a new public release to the Google Play Store using  _gradle_  and  [upload_to_play_store](https://docs.fastlane.tools/actions/upload_to_play_store/)  actions.

In the rest of this tutorial, we’ll see how to fill in details for each of these lanes to build your app, create screenshots, and ultimately submit the app to the Google Play Store for distribution.

### Building your app with fastlane

fastlane’s  [Gradle action](https://docs.fastlane.tools/actions/gradle/)  can run any Gradle-related tasks, including building and testing your Android app. As you’ll see in the following sections, you can configure the Gradle action to suit the type of build you’re trying to run.

#### Build types

When you create a new Android project, you get two build types:  `debug`  and  `release`. You can add the appropriate configuration to each build type to add or change specific preferences for each.

To define your build types, add the following to your `app/build.gradle` file:
```gradle
buildTypes {  
  debug {  
  applicationIdSuffix ".debug"  
  debuggable true  
  }  
  
  release {  
  minifyEnabled true  
  proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'  
  }  
}
```
This includes options to make sure the debug build stands out (with a suffix), and it ensures the release build is minified, and Proguard rules are applied. Any of the  [Android build system configuration options](https://developer.android.com/studio/build)  can be added to your fastlane build options.

#### Product flavors

[Product flavors](https://developer.android.com/studio/build/build-variants)  give you additional options for Android build variants. Add the following to create two product flavors (`demo`  and `full`) to your `app/build.gradle` file:

```gradle
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
#### Incrementing version code

Version code determines whether the current build version is more recent than another, so it’s a critical component of your app update and maintenance strategy. You must increase the value of your  `version code`  with each release to indicate changes.

If you’re not using fastlane, you have to manually update your  version code  in your  `app/build.gradle`  file every time you make an update. Versioning is much easier with fastlane; you simply need to add the plugin shown below:

`bundle exec fastlane add_plugin increment_version_code`

Once the plugin is installed, open your Fastfile and add a lane to increment the version code automatically:

```
desc "Increment version code"  
 lane :increment_vc do  
 increment_version_code(  
       gradle_file_path: "./app/build.gradle",  
 )  
end
```
Next, run the lane in your Terminal:

`bundle exec fastlane increment_vc`

Once the command runs, your  `version code`  inside the  `app/build.gradle`  file will be incremented. You can add this lane for every build going forward, thus removing the need to manually update your app’s version number every time you submit a release.

#### Configuring the  Build  lane

Now that you’ve added build types, flavors, and versioning to your project, you can run them by adding the following to your  build  lane:

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

To build the app, run:

`bundle exec fastlane build`

fastlane will clean and assemble the app, building only the  `release`  build type. App signing will be done according to the default Android Studio configurations. Later in this tutorial, we’ll configure a signing key, so you can skip this part for now.

If your app build succeeded, you will see the following message:

`[13:37:40]: fastlane.tools finished successfully 🎉`

With one major task down, let’s take a look at how you can automate your screenshot process in fastlane.

### Automating screenshots with screengrab

Fastlane uses  [screengrab](https://docs.fastlane.tools/actions/screengrab/)  to automatically generate localized screenshots of your Android app across dozens of device types. Taking hundreds of screenshots like this manually would take hours, so this could be a huge win for your team.

Before proceeding with the automation, you need to add the following permissions to your  **src/debug/AndroidManifest.xml**:

```
<!-- Allows unlocking your device and activating its screen so UI tests can succeed -->  
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />  
<uses-permission android:name="android.permission.WAKE_LOCK" />  
  
<!-- Allows for storing and retrieving screenshots -->  
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"  
  android:maxSdkVersion="32" />  
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"  
  android:maxSdkVersion="32" />
  
<!-- Allows changing locales -->  
<uses-permission  
android:name="android.permission.CHANGE_CONFIGURATION"  
tools:ignore="ProtectedPermissions" />

<!-- Allows changing SystemUI demo mode -->  
<uses-permission 
android:name="android.permission.DUMP" 
tools:ignore="ProtectedPermissions" />
```

With the required permissions in place, you can use the  [instrumentation testing](https://developer.android.com/training/testing/unit-testing/instrumented-unit-tests)  toolchain to set up screenshot automation.

Before you begin, open  **app/build.gradle**  and add the following  dependencies:

```
testImplementation 'junit:junit:4.13.2'  
androidTestImplementation 'androidx.test.ext:junit:1.1.5'  
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'  
androidTestImplementation 'androidx.test:rules:1.5.0'  
  
androidTestImplementation 'tools.fastlane:screengrab:2.1.1'
```

Your project may already have some of these dependencies, so adjust accordingly.

Next, inside the  `defaultConfig`  block, add  testInstrumentationRunner:

`testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'`

After you sync Gradle, you can move on to configuring your instrumentation tests.

#### Configuring instrumentation tests

Find or create an instrumentation test class named `ExampleInstrumentedTest`  inside your project’s  `app/src/androidTest/`  folder.

![New test file](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0bfadccb5ca239ae0c_zaepGkI.png)

Next, implement the test function by adding the following:

```
package com.devplanet.myavatar

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(JUnit4::class)
class ExampleInstrumentedTest {

    // JVMField needed!
    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testTakeScreenshot() {
        val scenario: ActivityScenario<*> = activityRule.scenario
        scenario.moveToState(Lifecycle.State.RESUMED)
        //Prepare app to take screenshots
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        Espresso.onView(ViewMatchers.withId(R.id.generateButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //Takes screenshot of the first screen
        Screengrab.screenshot("myAvatar_before_click")
        //Trigger the generate button onClick function
        Espresso.onView(ViewMatchers.withId(R.id.generateButton)).perform(ViewActions.click())
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

With the instrumentation test in place to capture the screenshot, you can set up screengrab and then a lane to automate the screenshot generation process.

#### Installing screengrab

To install screengrab, add the Ruby gem:

`sudo gem install screengrab`

Next, initialize screengrab by running the following inside your project:

`bundle exec fastlane screengrab init`

To run screengrab, you need  `-debug.apk ` and  `-debug-AndroidTest.apk`, but you can generate them automatically by building your project with the  `./gradlew assembleDebug assembleAndroidTest ` Gradle task. After a successful build, you can find both the APKs inside the project’s  `/app/build/outputs/apk/`.

Next, find the  `Screengrabfile`  inside your  `fastlane` directory. Open the Screengrabfile and replace its contents with the following:

```
# Set the path to the Android SDK
android_home('$PATH')

# Start ADB in root mode, giving you elevated permissions to write to the device
use_adb_root(true)

# Your project’s package name
app_package_name('com.devplanet.myavatar')

# Paths for APK files, which you are creating via fastlane
app_apk_path('app/build/outputs/apk/debug/app-debug.apk')
tests_apk_path('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')

# Locations where you want to create screenshots. Add locales as required.
locales(['en-US', 'fr-FR', 'it-IT'])

# Clears all previously generated screenshots before creating new ones
clear_previous_screenshots(true)
```

The above code configures screengrab by setting the path of the Android SDK, path to the generated debug apks, and also clears all previous screenshots to prevent them from appearing in a new report. screengrab is now configured, so once you add it to a lane, fastlane will be able to automatically capture and store screenshots of your app.

#### Adding a lane

Open your Fastfile and add a new lane to create screenshots:

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

To run this lane and create new screenshots, use the following command:

`bundle exec fastlane grab_screens`

Make sure to have an android device connected or an android emulator running. You can check this by running this command: `adb devices -l`.

> If running on an emulator with **API 24** or above, adb needs to run as
> root. This is only possible if the emulator is configured with the
> **Google APIs** target. An emulator with **Google Play** target won`t work.
>
> However, if you run a device or emulator with **API 23** or below, either
> option will work. See [comment
> #15788](https://github.com/fastlane/fastlane/issues/15788) under _fastlane issues_ for more information.

This will start the screenshot-grabbing process. When the execution is completed, it will open the screenshots in your browser, but you can also find them inside the `/fastfile/metadata/` directory. Later in this tutorial, you’ll see how to upload these screenshots (with all your metadata) to the Google Play Store for distribution.

### Beta app distribution with fastlane

Before you distribute updates of your app to production users, you may want to share them with beta testers to get feedback. To do that, you can upload your Android app to an app distribution provider. fastlane supports  [beta app deployment](https://docs.fastlane.tools/getting-started/android/beta-deployment/)  with multiple beta app providers, including Google Play and Firebase App Distribution.

In this tutorial, I’ll show you how to distribute your beta app with  [Firebase App Distribution](https://firebase.google.com/docs/app-distribution), which includes distribution and user management features for Android apps.

#### Using the Firebase CLI

To get started, you have to add your project to  [Firebase](https://firebase.google.com/).

After logging in, create a new project by clicking on  **Go to Console**  at the top right of your screen. Next, create a new Firebase project by clicking on  **Add project**:

![Add Firebase project](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0b95692ba5a871a779_Ke24cP8.png)

Name the project and continue. Once the project creation is complete, you can click on  **continue**  to view the project’s console.

Fastlane uses the Firebase CLI to upload your builds to Firebase App Distribution.  [Follow the instructions here to install Firebase CLI on your OS](https://firebase.google.com/docs/cli).

After the installation is complete, sign in to your Firebase account using the command below:

`firebase login`

Before you continue, you also need to install the Firebase fastlane plugin:

`bundle exec fastlane add_plugin firebase_app_distribution`

Press y to allow fastlane to modify your Gemfile and continue.

After installing the plugin, you need to add Firebase to your Android app. Follow  [these steps](https://firebase.google.com/docs/android/setup)  to add Firebase to your project. Once Firebase is added, open the  [General Settings page](https://console.firebase.google.com/u/0/project/_/settings/general)  and save your  **App ID**  so you can add it to your project’s configuration later.

![App ID](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0b01cf3b7f65e94ae8_fe927OA.png)

Now you can create beta user groups and release your app to them before distributing to a wider audience in the Google Play Store. To create a user group, navigate to the  **App Distribution**  tab and press  **Get Started**:

![App Distribution](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0bfae0415aae130bce_4DNQESk.png)

Go to the  **Testers and Groups**  tab and create a group. You can name it  **group-one**.

![Add group](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0b291833501e2a413e_tNCvtfQ.png)

Finally, add  **testers**. Make sure you enter your own email to add yourself as a tester so you can try this out. Once that’s done, you are ready to upload your first beta build with fastlane.

#### Deploying for beta testing

Open your Fastfile and replace the  beta  lane with the following. Remember to replace  <YOUR_FIREBASE_APP_ID>  with the App ID you copied previously:

```
   desc "Submit a new Beta Build to Firebase App Distribution"
   lane :beta do
   build firebase_app_distribution(
   app: "<YOUR_FIREBASE_APP_ID>",
   groups: "group-one",
   release_notes: "Lots of new avatars to try out!"
   )
   end
```

The above beta lane will upload your build to the **group-one** test group. From your terminal, run the beta lane with fastlane:

```
bundle exec fastlane beta  
  
# Output:  
[19:38:15]: 🔐 Authenticated successfully.  
  
[19:38:18]: ⌛ Uploading the APK.  
  
[19:38:33]: ✅ Uploaded the APK.  
  
[19:38:35]: ✅ Posted release notes.  
  
[19:38:37]: ✅ Added testers/groups.  
  
[19:38:37]: 🎉 App Distribution upload finished successfully.  
------  
[19:38:37]: fastlane.tools finished successfully 🎉
```

Navigate to  [Firebase App Distribution](https://console.firebase.google.com/u/0/project/_/appdistribution)  to ensure that the new build is available in your console:

![Beta build](https://imgur.com/ds3RTYE.png)

The users in your test group also will receive an email with instructions:

![Beta email](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0bfadccb20f339ae0d_uxMHayk.png)

Congratulations! You deployed your beta build to testers with fastlane.

Next time, you can make beta updates with a single command from your terminal, or you can hook this command into your continuous integration workflow to completely automate the process.

### Releasing to the Play Console

The last task we’re going to automate is releasing a new version of your app to users through the Google Play Store. Before you complete these steps, you’ll need to get a Google Play account ([which you can sign up for here](https://play.google.com/console/u/0/signup)).

Fastlane uses the  [supply action](https://docs.fastlane.tools/actions/supply/)  to upload app metadata, screenshots, and binaries to Google Play. supply needs to have at least one previous version of your app uploaded to the Google Play Store before you run it because fastlane cannot create a completely new app in the Play Store. But after you’ve created your app manually the first time, fastlane can completely automate the update process, as you’ll see below.

#### Creating and signing your app

You can follow the steps on the official  [Play Console Help page](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en)  to create and set up your app for the first time. Once you have created your app in the Play Console, upload it to one of the available tracks so that Play Console knows your app’s  **package**  name. You don’t need to actually publish the app if you’re not ready to do so yet.

Next, Android requires that all APKs or app bundles be digitally signed with a certificate before they are uploaded to Play Console. If you haven’t generated an  **upload key**  for your app yet, you can  [follow the steps here](https://developer.android.com/studio/publish/app-signing)  to create one.

Once you have your key, best practice is to remove signing information from your build files so it is not easily accessible to others. To do this, create a separate  **properties**  file to store that information, as you will refer to this in your build files.

Navigate to your app’s  `build.gradle`  file and add the following. Be sure to add a reference to the  **key.properties**  before the  android  block:

```
def keystoreProperties = new Properties()  
def keystorePropertiesFile = rootProject.file('key.properties')  
if (keystorePropertiesFile.exists()) {  
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))  
}
```

Now, you can configure a single signing configuration for your release build type:

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

Once you have completed your signing configuration, Gradle will automatically sign each release when it’s built.

#### Building the Android app bundle

Currently,  [Android App Bundles](https://developer.android.com/platform/technology/app-bundle)  are preferred over APKs. fastlane supports both, but you’ll need to update your Fastfile to build the app bundle for you.

To do so, add the following lane:

```
desc "Build release app bundle"  
lane :build_bundle do  
gradle(task: "clean bundleRelease")  
end  
```

Next, execute the lane:

`bundle exec fastlane build_bundle`

Once the execution is completed, you can find your bundle in the app’s build directory (the default location is  *app/build/outputs/bundle/release*). Now you have an app bundle file that is ready to upload to the Google Play Console. While you’ll automate subsequent releases with fastlane, you have to upload this first build manually.

Go to Play Console, select the  **Production**  track, and create a release.

![Create a release](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0b40345c7d8418dff5_SvLVYpQ.png)

You can enable  **Play App Signing**  and upload the  **app-release.aab**  bundle file you previously created. Since this is the first upload of your app, you can provide a short description of it in  **Release notes**.

Tap on  **save**  and navigate to  **All apps**  where your list of apps appears. Your new app will be available with your package name.

With your first build manually uploaded to the Play Store, you are ready to automate this process with fastlane for future updates.

#### Creating Play Console credentials

To connect fastlane with Play Console, you need to provide appropriate credentials in the form of an API key file. This file contains the credentials that fastlane will use to connect to your Google Play Console account and make updates on your behalf.

To create a key file, follow these steps from the  [fastlane official documentation](https://docs.fastlane.tools/getting-started/android/setup/#collect-your-google-credentials). Once your key file is created, you can connect fastlane with your Google Play Console APIs.

Run this command to verify your API key file:

`bundle exec fastlane run validate_play_store_json_key json_key:/path/to/your/downloaded/file.json `

Next, add your key file to fastlane. Open the  `fastlane/Appfile`  and update the  `json_key_file`  property with your key file location. In this tutorial, it is located inside the root directory of your project:

` json_key_file("./api-key.json")  `

#### Updating metadata

With the key file in place, you should be able to run supply to push the latest version of your app to the Google Play Store. Using an automated process like the one below is the best way to keep your metadata up-to-date with every release.

First, download your app’s existing metadata from the Play Store by running the following command. (Note: If you followed the previous steps on creating screenshots, you’ll already have a screenshot directory created. Remove that folder before initializing supply, as it will replace it the first time it runs.)

`bundle exec fastlane supply init  `

fastlane will download any existing content and put it into the  `fastlane/metadata/android`  directory. Next time you update your app’s metadata, you’ll only need to update these local files — supply will take care of the rest.

For example, to upload screenshots, first run the lane you created previously to generate screenshots:

`bundle exec fastlane grab_screens  `

Now, you will see your screenshots in  `metadata/android/en-US/images/phoneScreenshots`. Run the following to upload your new screenshots:

`bundle exec fastlane supply --skip_upload_changelogs  `

The  `--skip_upload_changelogs`  property skips uploading changelogs so you do not have to increment your app’s  **version code**  when updating only metadata.

When the command runs successfully, navigate to the Play Console. On your app’s Main Store listing, you’ll see the screenshots:

![Play Console screenshots](https://uploads-ssl.webflow.com/5ef1f28e08458502ba614d85/6140bc0bfadccb51cb39ae0e_TU3CveC.png)

You have now configured fastlane to retrieve and update app metadata, so the final step is to upload a release build to the Google Play Store.

#### Deploying to the Google Play Store

In the previous section, you created an Android App Bundle and uploaded it to the Play Console manually. From now on, you can do that automatically by running a single command on your Terminal.

First, update the  deploy  lane inside your Fastfile:

```
   desc "Deploy a new version to the Google Play"
   lane :deploy do
      # The lane you created previously to increment your app’s `version code`.
      increment_vc
      # The build release app bundle lane you created previously.
      build_bundle
      # Uploads Android App Bundle to Play Console.
      upload_to_play_store(release_status:"draft")
   end
   ```

The  `release_status`  property is used to maintain the status of the APKs or AABs when uploaded. (You can  [read about more parameters available for the supply action](https://docs.fastlane.tools/actions/supply/).)

Run  `bundle exec fastlane deploy`, and fastlane will take care of everything for your new release. When the command completes, your new build will appear in the Play Console.

### Conclusion

In this tutorial, we’ve built a complete fastlane pipeline for Android app deployment. Hopefully you’ve seen how it can save you time over building, testing, and releasing apps manually. As you scale up your app development team and release efforts, automation tools become increasingly important, to prevent errors and ensure that no single team member becomes a bottleneck in the release process. Soon enough, your team will be spending more time building and iterating on your app instead of worrying about manual release tasks!