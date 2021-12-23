local Library = require "CoronaLibrary"

-- Create stub library for simulator
local lib = Library:new{ name='plugin.samsung.iap', publisherId='com.coronalabs' }

-- Default implementations
local function defaultFunction()
	print( "WARNING: The '" .. lib.name .. "' library is not available in the Corona Simulator" )
end



lib.init = defaultFunction
lib.loadProducts = defaultFunction
lib.purchase = defaultFunction
lib.restore = defaultFunction
lib.consumePurchase = defaultFunction
--store values
lib.canLoadProducts = false
lib.canMakePurchases = false
lib.isActive = false
lib.target = "samsung"

-- Return an instance
return lib
