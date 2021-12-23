
# store.consumePurchase()

> --------------------- ------------------------------------------------------------------------------------------
> __Type__              [Function][api.type.Function]
> __Return value__      none
> __Revision__          [REVISION_LABEL](REVISION_URL)
> __Keywords__          Samsung, IAP, in-app purchases, consumePurchase
> __See also__          [store.purchase()][plugin.google-iap-v3.purchase]
> --------------------- ------------------------------------------------------------------------------------------


## Overview

This function "consumes" purchases and makes the item(s) available for purchase again.

Note that some items are designed to be purchased only once and you should __not__ consume them. For example, if a purchase unlocks a new world within a game, it should be ineligible for future consumption. Alternatively, some items can be purchased multiple times, for example energy packs and gems &mdash; these type of items must be consumed before they can be purchased again.



## Syntax

	store.consumePurchase( productIdentifier )

##### productIdentifier ~^(required)^~
_[String][api.type.String]._ String representing the product identifier of the item to consume.
