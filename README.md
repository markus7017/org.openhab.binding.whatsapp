# WhatsApp Binding

<hr><b>Release: beta1</b><hr><p>

This binding provides access to the WhatsApp messaging service. Initially it will provide full support of text messages. Media files (image / video / audio) will be likely supported, but need more work.

The actual technical interface to WhatsApp is provided by the yowsup project. This is a pearl-based implementation using an inoffcial WhatsApp API. Be ware that this can be shutdown by WhatsApp at any time.  The project includes yowsup-cli, which will be started and controlled by the binding. It's imprtant to carefully read the installation notes and make sure that yowsup-cli is running before installing the binding. Otherwise you could expect unpredictable results.

The binding uses the AragurDEV yowsup fork (see below).

## Pre-Requisite - yowsup installation

Currently the installation is intended for macOS, for Linux some dependencies must be added (install xxx) and Windows is not yet tested.

#### Preparing the SIM card ###

You need a mobile number, which is not yet registered with WhatsApp, e.g. a prepaid SIM. This is only required once, the SIM is not relevant anymore, but shouldn't be used in a smartphone with WhatsApp at any time.<p>

<b>If you use the yowsup-cli command for registration of a new number, you run the risk of being black-listed by WhatsApp after sending your first message.<p> 
To avoid this,<br></b>

- insert a new SIM<br>
- install WhatsApp on the mobile device<br>
- Start conversations with several numbers in your address book<br> 
- do some ping pong with different contacts for 24h-48h<br>
- deinstall Whatsapp (you still need the SIM for the registration SMS)<br>
- register with yowsup and the registration SMS (see above)<br>
- remove SIM from phone (don't use it in parallel to yowsup, e.g. in  different phone)<br>
- use different phone to send 2 messages to this number from 2 different contacts<br>
- start sending messages with yousup - you should them incoming messages on the console<br>
- do some ping pong with 2 contacts, e.g. using the yowsup echo client (see below)<br>
<p>
Important Note:<br>
Be aware not to fix the same phone number (MSISDN) on multiple servers. You need one number per server to avoid those issues!<br>
This could also happen when you start more than once instance on the same system (I suppose that caused the problem on my dev system)<p>
Do not run yowsup in parrallel to the binding!<p>

#### Install required packages

Preperation on Linux<br>
sudo apt-get update<nt>
sudo apt-get install libncurses5-dev<br>
<p>
Preperation on Mac (needs [Homwbrew](https://www.howtogeek.com/211541/homebrew-for-os-x-easily-installs-desktop-apps-and-terminal-utilities/))<br>
brew install wget --with-libressl<br>
brew install ncurses<br>
brew install freetype imagemagick exiftool ffmpeg pkg-config<br>
brew install libmagic<br>
cd /usr/local/lib/<br>
ln -s ../Cellar/libmagic/5.35/lib/libmagic.dylib libmagic.dylib<br>
<p>

Install Python modules:<br>
pip3 install --upgrade pip<br>
pip3 install argparse<br>
pip3 install python-dateutil<br>
pip3 install --upgrade readline<br>
pip3 install protobuf<br>
pip3 install preview-generator<br>
pip3 install xvfbwrapper<br>
pip3 install urllib3<br>
pip3 install idna<br>
pip3 install chardet<br>
pip3 install certifi<br>
pip3 install cryptographypip3 install pycrypto<br>
pip3 install python-axolotl-curve25519<br>
pip3 install --upgrade pillow<p>


Download https://files.pythonhosted.org/packages/ce/17/9eeb6bc3a7cc1dc8ba7db35a2038c61bef49336ec21057258801e9aef2a5/preview_generator-0.9.tar.gz<br>
unzip preview_generator-0.9.tar.gz<br>
phyton3 ./setup.py install<p>

### Install yowsup fork 

go to the directory where you want to download and build yowsup<p>

clone the repository:<br>
git clone https://github.com/AragurDEV/yowsup.git<p>

**don't run setup.py yet!**


### Updating WhatsApp version

You need to update the WhatsApp version and a key, otherwise you get error "old_version"!<br>

You find suitable settings under https://coderus.openrepos.net/whitesoft/whatsapp_scratch<br>
{"a": "HVpGIJI3MRi3wZmsvjJDqw==", "b": "PdA2DJyKoUrwLw1Bg6EIhzh502dF9noR9uFCllGk", "c":"1478194306452L", "d":"**2.18.355**", "e":"2.16.12", "f":"2.11.634", "g":"PdA2DJyKoUrwLw1Bg6EIhzh502dF9noR9uFCllGk", "h":"1478194472015L", "i":"PdA2DJyKoUrwLw1Bg6EIhzh502dF9noR9uFCllGk", "j":"1478194472015L"}<p>

Use parameter a and d and update the file yowsup/env/env_android.py<br>
nano yowsup/env/env_android.py<br>
and change the lines<br>
_MD5_CLASSES = "&lt;hash&gt;"<br>
_VERSION = "&lt;version&gt;"<br>
<br>
for example:<br>
_MD5_CLASSES = "HVpGIJI3MRi3wZmsvjJDqw=="<br>
_VERSION = "2.18.355"<br>
<br>

Now build and install yowsup_
Linux:<br>
python ./setup.py install<p> 

MacOS:<br>
python2 ./setup.py install<br> 

### WhatsApp registration

You need to lookup _Mobile Country Code_ (MCC), the _Mobile Network Code_ (MNC) and the normal Country Code (CC) you want to use:<br>
Open https://en.wikipedia.org/wiki/Mobile_country_code and look for MCC and MNC. Those identify the mobile network you are using, e.g. MCC=262 for Germany and MNC=1 for Telekom, MNC=2 for VodafoneDE etc.<p>

Run the WhatsApp registration:<br>
yowsup-cli registration -d -E android -m &lt;MCC&gt; -n &lt;MNC&gt; -p &lt;phone number&gt; -C &lt;country code&gt; -r sms<p>

Example: Germany (MCC=262, Country Code=49) and T-Mobile (MNC=1), phone number 491711234567<br>
yowsup-cli registration -d -E android -m 262 -n 1 -p 491711234567 -C 49 -r sms<p>

Make sure to prefix your phone number also with the country code, in this case 49xxxx. Do not include the 0 for the phone number, e.g. 01711234567 becomes 491711234567.<br>
As a result you receive a SMS on your mobile phone. You need the 6 digit registration code in format XXX-XXX sent by WhatsApp. SMS text will be something like:<br>
Your WhatsApp code: XXX-XXX<br>
...<p>

Registration will be completed with the following command:<br>
yowsup-cli registration -d -E android -p &lt;CC&gt;<phone number without 0&gt; -C &lt;CC&gt; -R <Registration Code&gt;<p>

for example: yowsup-cli registration -d -E android -p 491711234567 -C 49 -R 123-456<p>
<br>
Now you should receive a result like this one:<br>
status: ok<br>
kind: free<br>
pw: &lt;password&gt;<br>
price: $0.99<br>
price_expiration: 1543012826<br>
currency: USD<br>
cost: 0.99<br>
expiration: 4444444444.0<br>
login: &lt;phone number&gt;<br>
<p>

Generate some keys with the following command:<br>
yowsup-cli demos -l "491711234567:XXXXXX0uB6IMp9spB9FqedKFak=" -y<p>

This opens the yowsup command line interface.<p>

Do a Quick Login by entering "/L&lt;return&gt;"<p>

This should result in a response like:<br>
INFO:yowsup.layers.axolotl.layer_control:Axolotl layer is generating keys<br>
Auth: Logged in!<br>
general: Disconnected: Requested<br>
<p>

### Send some messages

Finally you are able to send messages.<p>

yowsup-cli demos -l "&lt;originating number&gt;:&lt;password from previous step&gt;" -s &lt;destination number&gt; "&lt;message&gt;"<p>

example: <br>
yowsup-cli demos -l "491711234567: XXXXXX0uB6IMp9spB9FqedKFak =" -s 491727654321 "Welcome to openHAB"<p>

You should receive the message in WhatsApp on the target phone<p>

### Echo-Client

You could also try the echo client:<br>
yowsup-cli demos -l "49170XXXXXXX:XXXXXXXXLQkjHw2/faNBL0XXXX=" -e<p>

Send some messages to yowsup and get the echo.<br>

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Discovery

There is no auto-discovery for WhatsApp things.


## Thing Configuration

A WhatsApp thing could be added using PaperUI. Change to Configuration-&gt;Things and click on '+'.<p>

Select the WhatsApp binding and continue, then the WhatsApp Hub and continue.<p>

Enter the originating number of the SIM card, which was registered. Do not enter the leading '0' and use international format:<br>
e.g. 01711234567 is a number in Germany, then replace the loading '0' with '49' = 491711234567<p>

The API password is the one you generated during yowsup installation.<p>

If you used the default installation you should find yowsup-cli under /usr/local/bin

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._


## Sending and Receiving Messages

### Number format

WhatsApp requires the number in international format. The binding normalizes the given number to allow different formats. You need to set the default country code in the binding config if you want to use 0xxx (being transformed to CCxxxx).
examples:
+491711234567 -> 491711234567
00491711234567 -> 491711234567
01711234567 -> 491711234567

### Text messages

Send a message: You need to create an item, which is linked to the "Text Messages->Outbound message" channel. <br>
Use sendCommand(Item, Message) from an openHAB rule and use the following notation: <number>:<message><o>
Inbound messages are posted to the "Text Messages->Inbound message" channel in the same format.<p>

### Media Messages 

The binding uses a JSON format to send/receive non-text media messages, e.g.<br>
{ "type" : "image", "number" : "491711234567", "path" : "/home/markus7017/Downloads/image.png", "caption" : "Hello from openHAB" }<br>
sends an image. Please make sure to incude the fully qualified path.
<p>


## Trouble Shooting

### yowsup installation problems

If you get a message “WARNING:yowsup.layers.axolotl.layer_receive:InvalidMessage or InvalidKeyIdException for xxxxxxxxxx, going to send a retry
also described here: https://github.com/tgalal/yowsup/issues/2403<p>

From Solution https://github.com/tgalal/yowsup/issues/2525:<p>
There is a little annoying solution, that i am using since 2 month and it always works.<br>
login with the yowsup cli (... -y)<br>
/L<br>
/disconnect<br>
<br>
repeat step 2 and 3 repeatedly until it resolves. it will auto generate the key.<br>
maybe you have to try 50 to 100 times to solve it keep patience , it will work.<br>
Try deleting the .yowsup folder, and then try again.<br>
What helped me was just sending messages in both directions, also using the echo client (-e).<p>
<p>
---<br>
<p>
If you get a message “ModuleNotFoundError: No module named ‘Crypto’:” then run the following:<br>
pip3 uninstall crypto<br>
pip3 uninstall pycrypto<br>
pip3 install pycrypto<br>
<p>
---<br>
<p>
If you get an error that MagicWand is not found: I seems that MagicWand doesn't support imagemagick 7 yet as mentioned in other answers.<br>
There's a new brew formula for Imagemagick 6 which can be used to install the older version in the meanwhile:<p>
brew install imagemagick@6<p>

Create a symlink to this newly installed dylib file as mentioned in other answer to get things working.<br>
ln -s /usr/local/Cellar/imagemagick@6/<your specific 6 version>/lib/libMagickWand-6.Q16.dylib /usr/local/lib/libMagickWand.dylib<p>

e.g.: ln -s /usr/local/Cellar/imagemagick@6/6.9.10-14/lib/libMagickWand-6.Q16.dylib /usr/local/lib/libMagickWand.dylib<p>

--

If you see the error “ERROR:yowsup.layers.protocol_media.mediauploader:Error occured at transfer object of type ‘int’ has no len()” run<br>
pip3 uninstall pyOpenSSL<p>
see https://github.com/danielcardeenas/whatsapp-framework/issues/133<br>
<p>