
# yowsup installation for openHAB WhatsApp binding #

This doc provides some help on how to install yowsup. This is an open source project which integrates the in-official WhatsApp api as a Python library and provides a command line interface, which will be used by the WhatsApp binding to send and receive WhatsApp messages within the openHAB environment.

Please note: yowsup requires several Python libs, which might not be installed on the system. Due to the fact that the installation procedure could be very specific for the different platforms (Linux, macOS, Windows) there are various differences how to get yowsup up and running. This doc tries to give hints etc., but you shouldn't expect a "next-next-next" install.

## Preparing your system

###  Python 3.6
If you not yet installed Pythong 3.6 please do before installing the bindinng to make sure that you are running on an supported environment. Preferred is Python 3.6.5.
```
sudo apt-get install python3-dev libffi-dev libssl-dev -y
wget https://www.python.org/ftp/python/3.6.5/Python-3.6.5.tar.xz
tar xJf Python-3.6.5.tar.xz
cd Python-3.6.5
./configure
make
sudo make altinstall
sudo pip3.6 install --upgrade pip
```
### Prepare for installation

**Linux**
```
sudo apt-get update<nt>
sudo apt-get install libncurses5-dev
```
**macOS** (needs [Homwbrew](https://www.howtogeek.com/211541/homebrew-for-os-x-easily-installs-desktop-apps-and-terminal-utilities/)

```
brew install wget --with-libressl
brew install ncurses
brew install freetype imagemagick exiftool ffmpeg pkg-config
brew install libmagic

cd /usr/local/lib/
ln -s ../Cellar/libmagic/5.35/lib/libmagic.dylib libmagic.dylib
```
### Required Python Package

Install Python modules:
```
apt-get install python3.6
pip3.6 install --upgrade pip
pip3.6 install cryptography==2.5
pip3.6 install consonance==0.1.2
pip3.6 install python-axolotl>=0.1.39
pip3.6 install protobuf>=3.6.0
pip3.6 install six==1.10
pip3.6 install certifi
pip3.6 install config.manager
```

### Installing yowsup

Check out [yowsup installation](https://github.com/tgalal/yowsup#installation) for details.

You could try to install the package directly with pip:
```
pip3.6 install yowsup==3.2.0
```
Do a manual download and installation if pip doesn't find the package.
Go to the directory where you want to download yowsup and run

```
wget https://github.com/tgalal/yowsup/archive/v3.2.0.zip
unzip v3.2.0.zip
python3.6 setup.py install
```

macOS: If you get an error message that SSL certificates are missing run the following command from a terminal
```
sudo installer -pkg /Library/Developer/CommandLineTools/Packages/macOS_SDK_headers_for_macOS_10.14.pkg -target /
pip3.6 install certifi
/Applications/Python\ 3.6/Install\ Certificates.command 
```

Make sure that the /usr/local/bin/yowsup-cli command was installed.

run
```
yowsup-cli version
```
to verify the installation.

If the installaer reports missing packages you need to add them to your system (pip3.6 install xxxx). Sometime you need to google how to install a specific package or get rid of an error message.

## WhatsApp registration

Check out [yowsup-cli on github](https://github.com/tgalal/yowsup/wiki/yowsup-cli) for details.

You need to lookup _Mobile Country Code_ (MCC), the _Mobile Network Code_ (MNC) and the normal Country Code (CC) you want to use: Open [List of mobile codes](https://en.wikipedia.org/wiki/Mobile_country_code) and look for MCC and MNC. Those identify the mobile network you are using, e.g. MCC=262 for Germany and MNC=1 for Telekom, MNC=2 for VodafoneDE etc.

Make sure to prefix your phone number also with the country code, in this case 49xxxx. Do not include the 0 for the phone number, e.g. 01711234567 becomes 491711234567.

### WhatsApp registration:
```
yowsup-cli registration --requestcode sms --config-phone 49XXXXXXXX --config-cc <CC> --config-mcc <MCC> --config-mnc <MNC>
yowsup-cli registration --register 123456 --config-phone 49XXXXXXXX
```
Example: 
Germany (MCC=262, Country Code=49) and T-Mobile (MNC=1), phone number 491711234567, registration code 123-456
```
yowsup-cli registration --requestcode sms --config-phone 491711234567 --config-cc 49 --config-mcc 262 --config-mnc 1
yowsup-cli registration --register 123-456 --config-phone 491711234567
```

As a result you receive a SMS on your mobile phone. You need the 6 digit registration code in format XXX-XXX sent by WhatsApp. SMS text will be something like:
Your WhatsApp code: XXX-XXX

### First time initialization

Generate some keys with the following command:
```
yowsup-cli demos --config-phone 491711234567 -y
```
This opens the yowsup command line interface.
Do a Login by entering /L on the console. This should result in a response like:
```
INFO:yowsup.layers.axolotl.layer_control:Axolotl layer is generating keys
Auth: Logged in!
```

### Send some messages

Finally you are able to send messages.
```
yowsup-cli demos  --config-phone <reguistered number> -s <destination number> <message>
```
example: 
```
yowsup-cli demos  --config-phone 491711234567 -s 491729876543 "Welcome to openHAB"
```

You should receive the message in WhatsApp on the target phone

### Echo-Client

You could also try the echo client:
```
yowsup-cli demos --config-phone <reguistered number> -e
```

Send some messages to yowsup and get the echo.

## Trouble Shooting

### yowsup installation problems

If you get a message “WARNING:yowsup.layers.axolotl.layer_receive:InvalidMessage or InvalidKeyIdException for xxxxxxxxxx, going to send a retry
also described here: https://github.com/tgalal/yowsup/issues/2403

From Solution https://github.com/tgalal/yowsup/issues/2525:
There is a little annoying solution, that i am using since 2 month and it always works.
login with the yowsup cli (... -y)
/L
/disconnect

repeat step 2 and 3 repeatedly until it resolves. it will auto generate the key.
maybe you have to try 50 to 100 times to solve it keep patience , it will work.
Try deleting the .yowsup folder, and then try again.
What helped me was just sending messages in both directions, also using the echo client (-e).

---

If you get a message “ModuleNotFoundError: No module named ‘Crypto’:” then run the following:
pip3.6 uninstall crypto
pip3.6 uninstall pycrypto
pip3.6 install pycrypto

---

If you get an error that MagicWand is not found: I seems that MagicWand doesn't support imagemagick 7 yet as mentioned in other answers.
There's a new brew formula for Imagemagick 6 which can be used to install the older version in the meanwhile:
brew install imagemagick@6

Create a symlink to this newly installed dylib file as mentioned in other answer to get things working.
ln -s /usr/local/Cellar/imagemagick@6/<your specific 6 version>/lib/libMagickWand-6.Q16.dylib /usr/local/lib/libMagickWand.dylib

e.g.: ln -s /usr/local/Cellar/imagemagick@6/6.9.10-14/lib/libMagickWand-6.Q16.dylib /usr/local/lib/libMagickWand.dylib

---

If you see the error “ERROR:yowsup.layers.protocol_media.mediauploader:Error occured at transfer object of type ‘int’ has no len()” run
pip3.6 uninstall pyOpenSSL
see https://github.com/danielcardeenas/whatsapp-framework/issues/133

