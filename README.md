# authlib-patcher

## Contact us
Contact info can be found at our github, https://github.com/The-AltMC-Project

## Installation
Download https://github.com/The-AltMC-Project/authlib-patcher/releases/latest/download/authlib-javaagent-1.0-SNAPSHOT.jar

Move it to your `minecraft` folder
  - `%appdata%\.minecraft\` for Windows
  - `/Users/username/Library/Application Support/minecraft/` for MacOS
  - `/home/username/.minecraft/` for Linux

Add `-javaagent:<location of the jar>` to your Minecraft launch arguments
  - Windows: `"-javaagent:C:\Users\username\AppData\Roaming\.minecraft\authlib-javaagent-1.0-SNAPSHOT.jar"`
  - MacOS: `"-javaagent:/Users/username/Library/Application Support/minecraft/authlib-javaagent-1.0-SNAPSHOT.jar"`
  - Linux: `"-javaagent:/home/username/.minecraft/authlib-javaagent-1.0-SNAPSHOT.jar"`

Make sure to *include* the quotes, and make sure to replace `username` with your computer username

### Done
Now, you can launch the game and play normally.

The AltMC authentication servers will only activate if the Microsoft authentication servers do not work for your account, meaning that you don't have to fiddle with your launch arguments every time you want to switch AltMC auth on/off. I recommend keeping it in the launch arguments, simply because there are no downsides.


## Building

### Pre requisites:
Install maven

### Building
Run these commands:

`git clone https://github.com/The-AltMC-Project/authlib-patcher`

`mvn clean package`

The `authlib-javaagent-1.0-SNAPSHOT.jar` is the correct jar, it will be under the `target` directory. 
