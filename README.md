# HVAC Controller
A java controller library for Samsung HVAC via RS485.

Tested next to an original wired remote control [Samsung Standard Wired Controller MWR-WH00](https://www.samsunghvac.com/Control-Non-NASA-Individual-Control/MWR-WH00U). 

Supported operations:
* Set
  * On/Off
  * OperatingMode
  * FanSpeed 
  * Target temperature
  * Set sleep and quite modes
* Get
  * All above plus output air temperature

## Prerequisites
* USB to RS485 adapter
  * Tested with [this one](https://www.czc.cz/premiumcord-usb-usb2-0-na-rs485-adapter/80182/produkt).
* [RXTX library](http://fizzed.com/oss/rxtx-for-java)
  * Linux: install `librxtx-java`
    * `sudo apt-get install librxtx-java`
  * Windows: package `Windows-x64` from http://fizzed.com/oss/rxtx-for-java
