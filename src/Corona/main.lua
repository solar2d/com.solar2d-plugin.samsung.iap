local store = require('plugin.samsung.iap')
local widget = require('widget')
local json = require('json')

-- The main store listener receives events when you make a purchase, restore
local function storeListener(event)
	print('storeListener event:')
	if not event.isError then
		print('transaction:', json.prettify(event.transaction))
	else
		print(json.prettify(event))
	end
	store.finishTransaction(event.transaction)
end

store.init(storeListener, "testMode")

local _W, _H = display.actualContentWidth, display.actualContentHeight
local _CX = display.contentCenterX

local width = _W * 0.8
local size = _H * 0.1
local buttonFontSize = 16

local y = size * 0.5
local spacing = _H * 0.12



widget.newButton{
	x = _CX, y = y + spacing * 1,
	width = width, height = size,
	label = 'loadProducts()',
	fontSize = buttonFontSize,
	onRelease = function()
		store.loadProducts({'consumable1', 'subscription1weekly'}, function(event)
			print('loadProducts() event:')
			if not event.isError then
				print('products:', json.prettify(event.products))
				print('invalidProducts:', json.prettify(event.invalidProducts))
			else
				print(json.prettify(event))
			end
		end)
	end
}

widget.newButton{
	x = _CX, y = y + spacing * 2,
	width = width, height = size,
	label = 'purchase() consumable',
	fontSize = buttonFontSize,
	onRelease = function()
		store.purchase('consumable1')
	end
}



widget.newButton{
	x = _CX, y = y + spacing * 3,
	width = width, height = size,
	label = 'purchase() subscription',
	fontSize = buttonFontSize,
	onRelease = function()
		store.purchase('subscription1weekly')
	end
}

widget.newButton{
	x = _CX, y = y + spacing * 4,
	width = width, height = size,
	label = 'restore()',
	fontSize = buttonFontSize,
	onRelease = function()
		store.restore()
	end
}
