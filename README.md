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

This tutorial uses a simple Android application called **My Avatar**. You can fin the source code [here on Github](https://github.com/devplanet-dp/my_avatar).

