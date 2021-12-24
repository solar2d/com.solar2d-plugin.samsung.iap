//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package plugin.samsung.iap;


import android.util.Log;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTask;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NamedJavaFunction;
import com.samsung.android.sdk.iap.lib.helper.HelperDefine;
import com.samsung.android.sdk.iap.lib.helper.IapHelper;
import com.samsung.android.sdk.iap.lib.listener.OnConsumePurchasedItemsListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetOwnedListListener;
import com.samsung.android.sdk.iap.lib.listener.OnGetProductsDetailsListener;
import com.samsung.android.sdk.iap.lib.listener.OnPaymentListener;
import com.samsung.android.sdk.iap.lib.vo.ConsumeVo;
import com.samsung.android.sdk.iap.lib.vo.ErrorVo;
import com.samsung.android.sdk.iap.lib.vo.OwnedProductVo;
import com.samsung.android.sdk.iap.lib.vo.ProductVo;
import com.samsung.android.sdk.iap.lib.vo.PurchaseVo;

import java.util.ArrayList;
import java.util.List;

import com.ansca.corona.purchasing.StoreServices;


@SuppressWarnings({"WeakerAccess", "unused"})
public class LuaLoader implements JavaFunction {

	private int fLibRef;
	private CoronaRuntimeTaskDispatcher fDispatcher;
	private IapHelper inAppHelper;
	private final List<String> productIdentifiers = new ArrayList<String>();
	private CoronaRuntimeTaskDispatcher initDis;
	private int initLis = -1;

	@SuppressWarnings("unused")
	public LuaLoader() {
		CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
		// Validate.
		if (activity == null) {
			throw new IllegalArgumentException("Activity cannot be null.");
		}
	}


	@Override
	public int invoke(LuaState L) {
		fDispatcher = new CoronaRuntimeTaskDispatcher( L );
		// Register this plugin into Lua with the following functions.
		NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
			new init(),
			new loadProducts(),
				new purchase(),
				new restore(),
				new consumePurchase(),
		};
		String libName = L.toString( 1 );
		L.register(libName, luaFunctions);

		L.pushValue(-1);
		fLibRef = L.ref(LuaState.REGISTRYINDEX);

		L.pushBoolean(false);
		L.setField(-2, "canLoadProducts");

		L.pushBoolean(false);
		L.setField(-2, "canMakePurchases");

		L.pushBoolean(false);
		L.setField(-2, "isActive");

		L.pushBoolean(true);
		L.setField(-2, "canPurchaseSubscriptions");

		L.pushString(StoreServices.getTargetedAppStoreName());
		L.setField(-2, "target");
		return 1;
	}

	private static boolean isPackageNameInstalled(String packageName) {
		// Invoke PackageServices for this
		com.ansca.corona.storage.PackageServices packageServices =
				new com.ansca.corona.storage.PackageServices(com.ansca.corona.CoronaEnvironment.getApplicationContext());
		return packageServices.isPackageNameInstalled(packageName);
	}

	private class init implements NamedJavaFunction {

		@Override
		public String getName() {
			return "init";
		}
		@Override
		public int invoke(LuaState L) {
			if(CoronaLua.isListener(L, 1, "init")){
				initLis = CoronaLua.newRef(L, 1);
				initDis = new CoronaRuntimeTaskDispatcher(L);
			}
			if (!isPackageNameInstalled(StoreServices.SAMSUNG_MARKETPLACE_APP_PACKAGE_NAME)) {
				Log.w("Corona", "Samsung Marketplace is not available or installed");
				//fire init error
				initDis.send(new CoronaRuntimeTask() {
					@Override
					public void executeUsing(CoronaRuntime runtime) {
						LuaState l = runtime.getLuaState();
						CoronaLua.newEvent(l, "init");
						l.newTable();

						l.pushBoolean(true);
						l.setField(-2, "isError");
						l.pushString("Samsung Marketplace Not Found");
						l.setField(-2, "errorType");
						l.pushString("Samsung Marketplace is not available or installed");
						l.setField(-2, "errorString");

						l.setField(-2, "transaction");
						try {
							CoronaLua.dispatchEvent(l, initLis, 0);
						} catch (Exception e) {

						}
					}
				});
				return 0;
			}

			inAppHelper = IapHelper.getInstance(CoronaEnvironment.getCoronaActivity());



			if(L.isString(2) && L.toString(2) == "testMode"){
				inAppHelper.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_TEST);
			}else if (L.isString(2) && L.toString(2) == "testFailureMode"){
				inAppHelper.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_TEST_FAILURE);
			}else{
				inAppHelper.setOperationMode(HelperDefine.OperationMode.OPERATION_MODE_PRODUCTION);
			}
			//change properties
			fDispatcher.send(new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					if (fLibRef == CoronaLua.REFNIL) {
						return;
					}
					LuaState l = runtime.getLuaState();
					// Set the store attributes
					l.rawGet(LuaState.REGISTRYINDEX, fLibRef);

					l.pushBoolean(true);
					l.setField(-2, "isActive");

					l.pushBoolean(true);
					l.setField(-2, "canLoadProducts");

					l.pushBoolean(true);
					l.setField(-2, "canMakePurchases");

					l.pop(1);
				}
			});
			//fire init event
			initDis.send(new CoronaRuntimeTask() {
				@Override
				public void executeUsing(CoronaRuntime runtime) {
					LuaState l = runtime.getLuaState();
					CoronaLua.newEvent(l, "init");

					l.newTable();
					l.pushBoolean(false);
					l.setField(-2, "isError");
					l.pushString("initialized");
					l.setField(-2, "state");
					l.setField(-2, "transaction");


					try {
						CoronaLua.dispatchEvent(l, initLis, 0);
					} catch (Exception e) {

					}
				}
			});
			return 0;
		}
	}

	@SuppressWarnings("unused")
	private class loadProducts implements NamedJavaFunction {
		@Override
		public String getName() {
			return "loadProducts";
		}

		@Override
		public int invoke(LuaState L) {
			if (!isPackageNameInstalled(StoreServices.SAMSUNG_MARKETPLACE_APP_PACKAGE_NAME)) {
				Log.w("Corona", "Samsung Marketplace is not available or installed");
				return 0;
			}
			//@todo figure out a workaround for invalidProducts(could not find a method in docs)
			productIdentifiers.clear();
			String productIdString = "";
			int myRef = -1;
			if (CoronaLua.isListener(L, 2, "") ){
				myRef = CoronaLua.newRef(L, 2);
			}else if(CoronaLua.isListener(L, 3, "")){
				myRef = CoronaLua.newRef(L, 3);
			}
			final int finalMyRef = myRef;
			CoronaRuntimeTaskDispatcher myDis = new CoronaRuntimeTaskDispatcher(L);
			int arrayLength = L.length(1);
			for(int index = 1; index <= arrayLength; index++){
				L.rawGet(1, index);
				if (L.isString(-1)){
					if (index != arrayLength){
						productIdString += L.toString(-1) +", ";
					}else{
						productIdString += L.toString(-1);
					}
					productIdentifiers.add(L.toString(-1));
				}
				L.pop(1);
			}
			if(!productIdString.equals("")){
				inAppHelper.getProductsDetails(productIdString, new OnGetProductsDetailsListener() {
					@Override
					public void onGetProducts(ErrorVo _errorVo, ArrayList<ProductVo> _productList) {
						if (_errorVo != null) {
							if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
								if (_productList != null) {
									myDis.send(new CoronaRuntimeTask() {
										@Override
										public void executeUsing(CoronaRuntime runtime) {
											LuaState l = runtime.getLuaState();
											CoronaLua.newEvent(l, "productList");
											l.pushBoolean(false);
											l.setField(-2, "isError");
											L.newTable();
											int productIndex = 0;
											for (ProductVo item : _productList) {
												l.newTable();
												productIdentifiers.remove(item.getItemId());
												l.pushString(item.getItemId());
												l.setField(-2, "productIdentifier");
												l.pushString(item.getItemName());
												l.setField(-2, "title");
												l.pushString(item.getItemDesc());
												l.setField(-2, "description");
												l.pushString(item.getItemPriceString());
												l.setField(-2, "localizedPrice");
												l.pushString(item.getType());
												l.setField(-2, "purchaseType");
												l.pushBoolean(item.getIsConsumable());
												l.setField(-2, "isConsumable");
												l.pushString(item.getJsonString());
												l.setField(-2, "originalJson");
												l.pushString(item.getItemDownloadUrl());
												l.setField(-2, "downloadUrl");
												l.pushString(item.getItemImageUrl());
												l.setField(-2, "imageUrl");
												l.rawSet(-2, productIndex+1);
												productIndex++;
											}
											l.setField(-2, "products");

											try {
												CoronaLua.dispatchEvent(l, finalMyRef, 0);
											} catch (Exception e) {

											}
										}
									});
								}
							} else {
								myDis.send(new CoronaRuntimeTask() {
									@Override
									public void executeUsing(CoronaRuntime runtime) {
										LuaState l = runtime.getLuaState();
										CoronaLua.newEvent(l, "productList");
										l.pushBoolean(true);
										l.setField(-2, "isError");
										l.pushString(_errorVo.getErrorString());
										l.setField(-2, "error");
										try {
											CoronaLua.dispatchEvent(l, finalMyRef, 0);
										} catch (Exception e) {

										}
									}
								});
							}
						}
					}
				});
			}

			return 0;
		}
	}

	@SuppressWarnings("unused")
	private class purchase implements NamedJavaFunction {

		@Override
		public String getName() {
			return "purchase";
		}

		@Override
		public int invoke(LuaState L) {
			if (!isPackageNameInstalled(StoreServices.SAMSUNG_MARKETPLACE_APP_PACKAGE_NAME)) {
				Log.w("Corona", "Samsung Marketplace is not available or installed");
				return 0;
			}

			String passThroughParm = "";
			if (L.isString(2)){
				passThroughParm = L.toString(2);
			}


			inAppHelper.startPayment(L.toString(1), passThroughParm, new OnPaymentListener() {
				@Override
				public void onPayment(ErrorVo _errorVo, PurchaseVo _purchaseVo) {
					if (_errorVo != null) {
						if (_purchaseVo != null && _errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
							initDis.send(new CoronaRuntimeTask() {
								@Override
								public void executeUsing(CoronaRuntime runtime) {
									LuaState l = runtime.getLuaState();
									CoronaLua.newEvent(l, "storeTransaction");

									l.newTable();

									l.pushString("purchased");
									l.setField(-2, "state");
									l.pushString(_purchaseVo.getPaymentId());
									l.setField(-2, "identifier");
									l.pushString(_purchaseVo.getItemId());
									l.setField(-2, "productIdentifier");
									l.pushString(_purchaseVo.getOrderId());
									l.setField(-2, "orderIdentifier");
									l.pushString(_purchaseVo.getPurchaseDate());
									l.setField(-2, "date");
									l.pushString(_purchaseVo.getItemDownloadUrl());
									l.setField(-2, "downloadUrl");
									l.pushString(_purchaseVo.getItemImageUrl());
									l.setField(-2, "imageUrl");
									l.pushString(_purchaseVo.getJsonString());
									l.setField(-2, "originalJson");
									l.pushBoolean(_purchaseVo.getIsConsumable());
									l.setField(-2, "isConsumable");
									l.pushString(_purchaseVo.getType());
									l.setField(-2, "purchaseType");
									l.pushBoolean(false);
									l.setField(-2, "isError");

									l.setField(-2, "transaction");

									try {
										CoronaLua.dispatchEvent(l, initLis, 0);
									} catch (Exception e) {
									}
								}
							});
						} else {
							initDis.send(new CoronaRuntimeTask() {
								@Override
								public void executeUsing(CoronaRuntime runtime) {
									LuaState l = runtime.getLuaState();
									CoronaLua.newEvent(l, "storeTransaction");

									l.newTable();
									if(_errorVo.getErrorCode() == IapHelper.IAP_PAYMENT_IS_CANCELED){
										l.pushString("cancelled");
										l.setField(-2, "state");
										l.pushBoolean(false);
										l.setField(-2, "isError");
									}else{
										l.pushString("failed");
										l.setField(-2, "state");
										l.pushString(_errorVo.getErrorString());
										l.setField(-2, "errorType");
										l.pushString(_errorVo.getErrorDetailsString());
										l.setField(-2, "errorString");
										l.pushBoolean(true);
										l.setField(-2, "isError");
									}

									l.setField(-2, "transaction");

									try {
										CoronaLua.dispatchEvent(l, initLis, 0);
									} catch (Exception e) {
									}
								}
							});
						}
					}
				}

			});


			return 0;
		}
	}
	private class restore implements NamedJavaFunction {

		@Override
		public String getName() {
			return "restore";
		}

		@Override
		public int invoke(LuaState L) {
			if (!isPackageNameInstalled(StoreServices.SAMSUNG_MARKETPLACE_APP_PACKAGE_NAME)) {
				Log.w("Corona", "Samsung Marketplace is not available or installed");
				return 0;
			}

			inAppHelper.getOwnedList(IapHelper.PRODUCT_TYPE_ALL, new OnGetOwnedListListener() {
				@Override
				public void onGetOwnedProducts(ErrorVo _errorVo, ArrayList<OwnedProductVo> _ownedList) {
					if (_errorVo != null) {
						if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
							if (_ownedList != null) {

								for (OwnedProductVo item : _ownedList) {
									initDis.send(new CoronaRuntimeTask() {
										@Override
										public void executeUsing(CoronaRuntime runtime) {
											LuaState l = runtime.getLuaState();
											CoronaLua.newEvent(l, "storeTransaction");

											l.newTable();

											l.pushString("restoreCompleted");
											l.setField(-2, "state");
											l.pushString(item.getPurchaseDate());
											l.setField(-2, "date");
											l.pushString(item.getPaymentId());
											l.setField(-2, "identifier");
											l.pushString(item.getItemId());
											l.setField(-2, "productIdentifier");
											l.pushString(item.getJsonString());
											l.setField(-2, "originalJson");
											if (item.getType() == "subscription"){
												l.pushString(item.getSubscriptionEndDate());
												l.setField(-2, "subscriptionEndDate");
											}

											l.pushString(item.getType());
											l.setField(-2, "purchaseType");
											l.pushBoolean(item.getIsConsumable());
											l.setField(-2, "isConsumable");
											l.pushBoolean(false);
											l.setField(-2, "isError");

											l.setField(-2, "transaction");

											try {
												CoronaLua.dispatchEvent(l, initLis, 0);
											} catch (Exception e) {
											}
										}
									});
								}
							}
						} else {
							initDis.send(new CoronaRuntimeTask() {
								@Override
								public void executeUsing(CoronaRuntime runtime) {
									LuaState l = runtime.getLuaState();
									CoronaLua.newEvent(l, "storeTransaction");
									l.newTable();

									l.pushString("failed");
									l.setField(-2, "state");
									l.pushString(_errorVo.getErrorString());
									l.setField(-2, "errorType");
									l.pushString(_errorVo.getErrorDetailsString());
									l.setField(-2, "errorString");
									l.pushBoolean(true);
									l.setField(-2, "isError");

									l.setField(-2, "transaction");

									try {
										CoronaLua.dispatchEvent(l, initLis, 0);
									} catch (Exception e) {
									}
								}
							});
						}
					}
				}
			});
			return 0;
		}
	}
	private class consumePurchase implements NamedJavaFunction {
		@Override
		public String getName() {
			return "consumePurchase";
		}

		@Override
		public int invoke(LuaState L) {
			if (!StoreServices.isAppStoreAvailable(StoreServices.SAMSUNG_MARKETPLACE_APP_PACKAGE_NAME)) {
				Log.w("Corona", "Samsung Marketplace is not available or installed");
				return 0;
			}
			inAppHelper.consumePurchasedItems(L.toString(1), new OnConsumePurchasedItemsListener() {
				@Override
				public void onConsumePurchasedItems(ErrorVo _errorVo, ArrayList<ConsumeVo> _consumeList) {
					if (_errorVo != null) {
						if (_errorVo.getErrorCode() == IapHelper.IAP_ERROR_NONE) {
							if (_consumeList != null) {
								for (ConsumeVo item : _consumeList) {
									initDis.send(new CoronaRuntimeTask() {
										@Override
										public void executeUsing(CoronaRuntime runtime) {
											LuaState l = runtime.getLuaState();
											CoronaLua.newEvent(l, "storeTransaction");
											l.newTable();

											l.pushString("consumed");
											l.setField(-2, "state");
											String status = "failedOrder"; // this is 2
											if(item.getStatusCode() == 0){
												status = "success";
											}else if(item.getStatusCode() == 1){
												status = "invalidPurchaseID";
											}else if(item.getStatusCode() == 3){
												status = "nonConsumableItem";
											}else if(item.getStatusCode() == 4){
												status = "alreadyConsumed";
											}else if(item.getStatusCode() == 5){
												status = "unauthorizedUser";
											}else if(item.getStatusCode() == 9){
												status = "serviceError";
											}
											l.pushString(status);
											l.setField(-2, "statusType");
											l.pushString(item.getStatusString());
											l.setField(-2, "status");
											l.pushBoolean(false);
											l.setField(-2, "isError");

											l.setField(-2, "transaction");

											try {
												CoronaLua.dispatchEvent(l, initLis, 0);
											} catch (Exception e) {
											}
										}
									});
								}
							}
						} else {
							initDis.send(new CoronaRuntimeTask() {
								@Override
								public void executeUsing(CoronaRuntime runtime) {
									LuaState l = runtime.getLuaState();
									CoronaLua.newEvent(l, "storeTransaction");
									l.newTable();

									l.pushString("failed");
									l.setField(-2, "state");
									l.pushString(_errorVo.getErrorString());
									l.setField(-2, "errorType");
									l.pushString(_errorVo.getErrorDetailsString());
									l.setField(-2, "errorString");
									l.pushBoolean(false);
									l.setField(-2, "isError");

									l.setField(-2, "transaction");

									try {
										CoronaLua.dispatchEvent(l, initLis, 0);
									} catch (Exception e) {
									}
								}
							});
						}
					}
				}
			});
			return 0;
		}
	}
}
