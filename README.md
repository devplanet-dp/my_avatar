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

Next step is setup the **XCode command-line tool(CLT)**. It can be enabled with following command: 
```
xcode-select --install
```
If CLT are already installed, you will get a message `xcode-select: error: command line tools are already installed, use "Software Update" to install updates`

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

# Configuring Fastlane

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
