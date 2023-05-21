# HVAC Controller

A java controller library for Samsung HVAC via RS485.

Developed and tested next to an original wired remote controller
[Samsung Standard Wired Controller MWR-WH00](https://www.samsunghvac.com/Control-Non-NASA-Individual-Control/MWR-WH00U) running as master
and `NS071SDXEA` HVAC unit.

This controller runs in slave mode only. It doesn't send periodic get requests to hvac device 
to get its current state, but it only listens responses to get requests sent by a master 
controller.    

The implementation is based on my reverse engineering done in 2017. Anyway, the younger python
project [Samsung-HVAC-buscontrol](https://github.com/DannyDeGaspari/Samsung-HVAC-buscontrol) contains very nice
documentation. See there for more details, it has no sense to write it again. Copy of its readme
is [here](doc/Samsung-HVAC-buscontrol.md).

Supported operations:

* Set
  * On/off
  * Operating mode
  * Fan speed
  * Target temperature
  * Set sleep and quite modes
* Get
  * All above plus:
    * 2 output air temperatures
    * room/input air temperature (probably)
    * defrost flag
    * ?unit temperature?

## Prerequisites
* USB to RS485 adapter
  * Tested with [this one](https://www.czc.cz/premiumcord-usb-usb2-0-na-rs485-adapter/80182/produkt).
  * Originally tried with a one dollar device from Aliexpress, but it was not able to send commands. The most probably a
    broken piece, it cost me a lot of evenings :-(
