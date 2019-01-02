# WhatsApp Binding

<hr><b>Release: 2.4.1pre</b><hr><p>

This binding provides access to the WhatsApp messaging service. Sending and receiving text messages are supported as well as sending images. Other media formats need to be supported by the underlying WhatsApp interface before they could be integrated.

The technical interface to WhatsApp is provided by the yowsup project. This is a Phyton-based implementation using an in-offcial WhatsApp API. Be ware that this can be shutdown by WhatsApp at any time.  The project includes yowsup-cli, which will be started and controlled by the binding. It's important to carefully read the installation notes and make sure that yowsup-cli is running before installing the binding. Otherwise you could expect unpredictable results.

The binding uses <a href="https://github.com/AragurDEV">AragurDEV's yowsup fork</a>. While there are various forks no other fork will be supported unless there are good reasons. 

## yowsup installation ##


Please make sure to read the yowsup installation guideline (README.yowsup-install.md). A correctly installed yowsup is very important, otherwise the binding will not work.

<hr>

## Preparing the WhatsApp account##

To send and receive messages you need to setup a valid WhatsApp account.  You need a mobile number, which is not yet registered with WhatsApp, e.g. a prepaid SIM. This is only required once, the SIM is not relevant anymore, but shouldn't be used in a smartphone with WhatsApp at any time.<p>

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
<hr>

## Supported Things

The binding has a single Thing "WhatsApp Hub". It implementes the technical interface and provides channels for sending and receiving WhatsApp messages.<p>
Please note: The binding currently supports only ONE thing. Multiple things would required multiple MSISDNs and multiple yowsup instances.<p>

A WhatsApp thing could be added using PaperUI. 
- Change to Configuration-&gt;Things and click on '+'
- Select the WhatsApp binding and continue, then the WhatsApp Hub and continue.<p>

or by defining in a .things file.

Enter the originating number of the SIM card, which was registered. Do not enter the leading '0' and use international format:<br>
e.g. 01711234567 is a number in Germany, then replace the loading '0' with '49' = 491711234567<p>

The API password is the one you generated during yowsup installation.<p>

If you used the default installation you should find yowsup-cli under /usr/local/bin
## Discovery

There is NO auto-discovery for WhatsApp things. You need to add the thing manually.


## Thing Configuration

The thing has a few configuration parameters:<p>
<table>
<tr><td>Parameter</td><td>Description</td></tr>
<tr><td>originatingNumber</td><td>Originating MSISDN. The number has to be registered before it can be used. See installation for yowsup.</td></tr>
<tr><td>apiPassword</td><td>The API password will be generated during the installation process.</td></tr>
<tr><td>defaultCC</td><td>Default country code. The binding support number normilization, see below.</td></tr>
<tr><td>cliPath</td><td>Path to yowsup-cli (usually /usr/local/bin/yowsup-clu)</td></tr>
<tr><td>dbPath</td><td>Optional: Path to the yowsup key db, usually ~/.yowsup. Configure only if required.</td></tr>
</table>

## Channels


<table>
<tr><td>Thing</td><td>Description</td></tr>
<tr><td>Group textMessages</td><td></td></tr>
<tr><td>messageOut</td><td>Send a WhatsApp message. Use the format '&lt;number&gt;:&lt;text&gt;'.</td></tr>
<tr><td>messageIn</td><td>Receive a WhatsApp message in the format '&lt;number&gt;:&lt;text&gt;</td></tr>
<tr><td>Group  mediaMessages</td><td></td></tr>
<tr><td>mediaOut</td><td>Send a media file (e.g. image). The channel expects a JSON format, (see below).</td></tr>
<tr><td>mediaIn</td><td>A media message has been received. Information is encoded in a JSON format (see below).</td></tr>
</table>
<p>
See "Sending and Receiving Messages" for more information.

## Full Example

<b>whatsapp.things:</b><p>

Thing whatsapp:hub:whatsapp1 "WhatsApp Hub" @ "network" [ originatingNumber="491711234567", apiPassword="2X83AzXZLLkjHw3/faNBL1mudfZ=", defaultCC="49", ... ]<p>

<b>Sitemap:</b>


## Sending and Receiving Messages

<b>Number format</b><p>

WhatsApp requires the number in international format. The binding normalizes the given number to allow different formats. You need to set the default country code in the binding config if you want to use 0xxx (being transformed to CCxxxx).
examples:
<table>
<tr><td>+491711234567</td><td>-&gt;</td><td>491711234567</td></tr>
<tr><td>00491711234567</td><td>-&gt;</td><td>491711234567</td></tr>
<tr><td>01711234567</td><td>-&gt;</td><td>491711234567</td></tr>
</table>
<p>
<b>Text messages</b><p>
Send a message: You need to create an item, which is linked to the "Text Messages->Outbound message" channel. <br>
Use sendCommand(Item, Message) from an openHAB rule and use the following notation: <number>:<message><o>
Inbound messages are posted to the "Text Messages->Inbound message" channel in the same format.<p>

<b>Media Messages</b><p> 

The binding uses a JSON format to send/receive non-text media messages, e.g.<br>
{ "type" : "image", "number" : "491711234567", "path" : "/home/markus7017/Downloads/image.png", "caption" : "Hello from openHAB" }<br>
sends an image. Please make sure to incude the fully qualified path.

<b>JSON properties:</b><br>
<table>
<tr><td>Property</td><td>Description</td></tr>
<tr><td>type</td><td>Message type: text|image|audio|video|document|vcard|location *</td></tr>
<tr><td>number</td><td>MSISDN to send to</td></tr>
<tr><td>message</td><td>Text message</td></tr>
<tr><td>path</td><td>Fully qualified path to the media file (e.g. /home/pi/test.png)</td></tr>
<tr><td>caption</td><td>Optional: Caption text for media message</td></tr>
</table>
<p>
Inbound messages also have the timestamp and id attributes.<p>
<p>* Note: The binding currently only supports sending media messages of type image.


