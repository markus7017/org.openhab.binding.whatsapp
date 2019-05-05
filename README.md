# WhatsApp Binding

Release: 2.4.2 (for yowsup 3.2+)

This binding provides access to the WhatsApp messaging service. Sending and receiving text messages are supported as well as sending images. Other media formats need to be supported by the underlying WhatsApp interface before they could be integrated.

The technical interface to WhatsApp is provided by the <a href="https://github.com/tgalal/yowsup">yowsup</a> project. This is a Phyton-based implementation using an in-offcial WhatsApp API. Be ware that this can be shutdown by WhatsApp at any time.  The project includes yowsup-cli, which will be started and controlled by the binding. Thank's to <a href="https://github.com/tgalal">tgalal</a> for this great work.

It's important to carefully read the installation notes and make sure that yowsup-cli is running before installing the binding. Otherwise you could expect unpredictable results.


## yowsup installation ##


Please make sure to read the yowsup installation guideline (README.yowsup-install.md). A correctly installed yowsup is very important, otherwise the binding will not work.

## Preparing the WhatsApp account

To send and receive messages you need to setup a valid WhatsApp account.  You need a mobile number, which is not yet registered with WhatsApp, e.g. a prepaid SIM. This is only required once, the SIM is not relevant anymore, but shouldn't be used in a smartphone with WhatsApp at any time.

**If you use the yowsup-cli command for registration of a new number, you run the risk of being black-listed by WhatsApp after sending your first message.**
 
To avoid this,

- insert a new SIM
- install WhatsApp on the mobile device
- Start conversations with several numbers in your address book 
- do some ping pong with different contacts for 24h-48h
- deinstall Whatsapp (you still need the SIM for the registration SMS)
- register with yowsup and the registration SMS (see above)
- remove SIM from phone (don't use it in parallel to yowsup, e.g. in  different phone)
- use different phone to send 2 messages to this number from 2 different contacts
- start sending messages with yousup - you should them incoming messages on the console
- do some ping pong with 2 contacts, e.g. using the yowsup echo client (see below)

---

Important Note:
Be aware not to fix the same phone number (MSISDN) on multiple servers. You need one number per server to avoid those issues!
This could also happen when you start more than once instance on the same system (I suppose that caused the problem on my dev system)---
**Do not run yowsup in parrallel to the binding!**

## Supported Things

The binding has a single Thing "WhatsApp Hub". It implements the technical interface and provides channels for sending and receiving WhatsApp messages.---
Please note: The binding currently supports only ONE thing. Multiple things would required multiple MSISDNs and multiple yowsup instances.---

A WhatsApp thing could be added using PaperUI. 
- Change to Configuration-&gt;Things and click on '+'
- Select the WhatsApp binding and continue, then the WhatsApp Hub and continue.---

or by defining in a .things file.

Enter the originating number of the SIM card, which was registered. Do not enter the leading '0' and use international format:
e.g. 01711234567 is a number in Germany, then replace the loading '0' with '49' = 491711234567.

If you used the default installation you should find yowsup-cli under /usr/local/bin/yowsup-cl.

## Discovery

There is NO auto-discovery for WhatsApp things. You need to add the thing manually.


## Thing Configuration

The thing has a few configuration parameters:
|Parameter        |Description|
|-----------------|-------------------------------------------------------------------------------------------------------|
|originatingNumber|Originating MSISDN. The number has to be registered before it can be used, see notes above.|
|defaultCC        |Default country code. The binding support number normilization, see below.|
|cliPath          |Path to yowsup-cli (usually /usr/local/bin/yowsup-cli)|

## Channels

|Group        |Thing     |Description|
|-------------|----------|-------------------------------------------------------------------------------------- ||
|textMessages |messageOut|Send a WhatsApp message. Use the format '&lt;number&gt;:&lt;text&gt;'.|
|             |messageIn |Receive a WhatsApp message in the format '&lt;number&gt;:&lt;text&gt;|
|mediaMessages|mediaOut  |Send a media file (e.g. image). The channel expects a JSON format, (see below).|
|             |mediaIn   |A media message has been received. Information is encoded in a JSON format (see below).|

---

See "Sending and Receiving Messages" for more information.

## Full Example

**whatsapp.things:**

```
Thing whatsapp:hub:whatsapp1 "WhatsApp Hub" [ originatingNumber="491711234567", defaultCC="49", ... ]
```

**whatsapp.items**

```
String WhatsApp_TextOut "WhatsApp Text  Out [%s]"  {channel="whatsapp:hub:whatsapp1:textMessages#messageOut"}
String WhatsApp_TextIn  "WhatsApp Text  In [%s]"   {channel="whatsapp:hub:whatsapp1:textMessages#messageIn"}
String WhatsApp_TextOut "WhatsApp Media Out [%s]"  {channel="whatsapp:hub:whatsapp1:mediaMessages#mediaOut"}
String WhatsApp_TextOut "WhatsApp Media In [%s]"   {channel="whatsapp:hub:whatsapp1:mediaMessages#mediaIn"}
```

**Sitemap:**

t.b.d. - please contribute an example

**whatsapp.rules**
```java
rule "alarm"
when
    Alarm changed to ON
then
    WhatsApp_TextOut.sendCommand("491717654321:An alarm has been detected!")
end

while "whatsapp-in"
when
    Item WhatsApp_TextIn changed
then
    logInfo("Bot", "A WhatsApp message has been received:"+WhatsApp_TextIn.state)
end
```

## Sending and Receiving Messages

**Number format**

WhatsApp requires the number in international format. The binding normalizes the given number to allow different formats. You need to set the default country code in the binding config if you want to use 0xxx (being transformed to CCxxxx).

- 491234567
- 01711234567
- +491711234567 
- 00491711234567
will all be converted to 491711234567

---

**Text messages**

Send a message: You need to create an item, which is linked to the "Text Messages->Outbound message" channel.

Use item.sendCommand(message) from an openHAB rule and use the following notation: &lt;number&gt;:&lt;message&gt;

Inbound messages are posted to the "Text Messages->Inbound message" channel in the same format.

**Media Messages**

The binding uses a JSON format to send/receive non-text media messages, e.g.
```
{ "type" : "image", "number" : "491711234567", "path" : "/home/markus7017/Downloads/image.png", "caption" : "Hello from openHAB" }
```
sends an image. Please make sure to include the fully qualified path '~' could not be used.

**JSON properties:**

|Property||Description|
|--------|------------|
|type    |Message type:
|        |image - a picture|
|        |audio|- an audio file (specified by path property) |
|        |video - a video file (specified by path) |
|        |document - a document (e.g. vcard, spcified by path)|
|        |vcard|location *|
|number  |MSISDN to send to|
|message |Text message|
|path    |Fully qualified path to the media file (e.g. /home/pi/test.png)|
|caption ||Optional: Caption text for media message|


Inbound messages also have the timestamp and id attributes.

Note: The binding currently only supports sending media messages of type image.


