import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../api/axios';
import {
  CreditCard, Tag, ShoppingBag, AlertCircle, ArrowLeft,
  MapPin, Plus, Check, Lock, Shield, X
} from 'lucide-react';

const Checkout = () => {
  const [cart, setCart] = useState(null);
  const [products, setProducts] = useState({});
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const [addressForm, setAddressForm] = useState({
    fullName: '', phone: '', addressLine1: '', addressLine2: '',
    city: '', state: '', pincode: '', country: 'India', isDefault: true
  });
  const [couponCode, setCouponCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [savingAddress, setSavingAddress] = useState(false);
  const [error, setError] = useState('');
  const [showPayment, setShowPayment] = useState(false);
  const [paymentStep, setPaymentStep] = useState('card');
  const [cardForm, setCardForm] = useState({ number: '', expiry: '', cvv: '', name: '' });
  const [paymentMethod, setPaymentMethod] = useState('card');
  const navigate = useNavigate();

  useEffect(() => {
    fetchCart();
    fetchAddresses();
  }, []);

  const fetchCart = async () => {
    try {
      const res = await API.get('/cart');
      const cartData = res.data.data || res.data;
      setCart(cartData);
      if (cartData.items?.length > 0) {
        const prods = {};
        for (const item of cartData.items) {
          try {
            const pRes = await API.get(`/products/${item.productId}`);
            if (pRes.data.success) prods[item.productId] = pRes.data.data;
          } catch {}
        }
        setProducts(prods);
      }
    } catch (err) {
      setError('Failed to load cart');
    } finally {
      setLoading(false);
    }
  };

  const fetchAddresses = async () => {
    try {
      const res = await API.get('/addresses');
      const addrs = res.data.data || [];
      setAddresses(addrs);
      const defaultAddr = addrs.find(a => a.isDefault);
      if (defaultAddr) setSelectedAddress(defaultAddr.id);
      else if (addrs.length > 0) setSelectedAddress(addrs[0].id);
    } catch (err) {
      console.error('Failed to fetch addresses:', err);
    }
  };

  const saveAddress = async () => {
    setSavingAddress(true);
    setError('');
    try {
      const res = await API.post('/addresses', addressForm);
      if (res.data.success) {
        setShowAddressForm(false);
        setAddressForm({
          fullName: '', phone: '', addressLine1: '', addressLine2: '',
          city: '', state: '', pincode: '', country: 'India', isDefault: true
        });
        fetchAddresses();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save address');
    } finally {
      setSavingAddress(false);
    }
  };

  const getSubtotal = () => {
    if (!cart?.items) return 0;
    return cart.items.reduce((sum, item) => {
      const product = products[item.productId];
      return sum + (product?.price || 0) * item.quantity;
    }, 0);
  };

  const initiatePayment = () => {
    if (!selectedAddress) {
      setError('Please select a delivery address');
      return;
    }
    if (!cart?.items?.length) return;
    setError('');
    setShowPayment(true);
    setPaymentStep('card');
  };

  const processPayment = async () => {
    if (paymentMethod === 'card') {
      if (!cardForm.number || !cardForm.expiry || !cardForm.cvv || !cardForm.name) {
        setError('Please fill all card details');
        return;
      }
    }
    setError('');
    setPaymentStep('processing');

    await new Promise(resolve => setTimeout(resolve, 2500));

    const success = Math.random() > 0.05;
    if (!success) {
      setPaymentStep('card');
      setError('Payment declined. Please try again.');
      return;
    }

    setPaymentStep('success');
    await new Promise(resolve => setTimeout(resolve, 1500));

    setPlacing(true);
    try {
      const orderRequest = {
        items: cart.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        })),
        couponCode: couponCode.trim() || null
      };
      const res = await API.post('/orders', orderRequest);
      if (res.data.success) {
        try { await API.delete('/cart/clear'); } catch {}
        navigate('/orders', { state: { orderSuccess: true, order: res.data.data } });
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order');
      setPaymentStep('card');
    } finally {
      setPlacing(false);
    }
  };

  const formatCardNumber = (val) => {
    const cleaned = val.replace(/\D/g, '').slice(0, 16);
    return cleaned.replace(/(.{4})/g, '$1 ').trim();
  };

  const formatExpiry = (val) => {
    const cleaned = val.replace(/\D/g, '').slice(0, 4);
    if (cleaned.length >= 3) return cleaned.slice(0, 2) + '/' + cleaned.slice(2);
    return cleaned;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  const items = cart?.items || [];
  const subtotal = getSubtotal();

  if (items.length === 0) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-20 text-center">
        <ShoppingBag size={48} className="mx-auto mb-4 text-zinc-700" />
        <p className="text-zinc-500 mb-4">Your cart is empty</p>
        <button onClick={() => navigate('/')} className="px-6 py-2.5 bg-emerald-500 text-zinc-900 rounded-xl font-semibold">
          Browse Products
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
      <button onClick={() => navigate('/cart')} className="flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-6">
        <ArrowLeft size={16} /> Back to Cart
      </button>

      <h1 className="text-2xl font-bold text-white mb-8">Checkout</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">

          {/* Delivery Address */}
          <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-white flex items-center gap-2">
                <MapPin size={18} className="text-emerald-400" />
                Delivery Address
              </h2>
              <button onClick={() => setShowAddressForm(!showAddressForm)}
                className="flex items-center gap-1.5 px-3 py-1.5 text-sm bg-white/5 border border-white/10 rounded-lg text-zinc-300 hover:text-white transition-all">
                <Plus size={14} /> Add New
              </button>
            </div>

            {showAddressForm && (
              <div className="bg-white/[0.02] border border-white/10 rounded-xl p-4 mb-4 space-y-3">
                <div className="grid grid-cols-2 gap-3">
                  <input type="text" placeholder="Full Name *" value={addressForm.fullName}
                    onChange={(e) => setAddressForm({ ...addressForm, fullName: e.target.value })}
                    className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                  <input type="text" placeholder="Phone (10 digits) *" value={addressForm.phone}
                    onChange={(e) => setAddressForm({ ...addressForm, phone: e.target.value })}
                    className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                </div>
                <input type="text" placeholder="Address Line 1 *" value={addressForm.addressLine1}
                  onChange={(e) => setAddressForm({ ...addressForm, addressLine1: e.target.value })}
                  className="w-full px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                <input type="text" placeholder="Address Line 2 (optional)" value={addressForm.addressLine2}
                  onChange={(e) => setAddressForm({ ...addressForm, addressLine2: e.target.value })}
                  className="w-full px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                <div className="grid grid-cols-3 gap-3">
                  <input type="text" placeholder="City *" value={addressForm.city}
                    onChange={(e) => setAddressForm({ ...addressForm, city: e.target.value })}
                    className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                  <input type="text" placeholder="State *" value={addressForm.state}
                    onChange={(e) => setAddressForm({ ...addressForm, state: e.target.value })}
                    className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                  <input type="text" placeholder="Pincode *" value={addressForm.pincode}
                    onChange={(e) => setAddressForm({ ...addressForm, pincode: e.target.value })}
                    className="px-3 py-2.5 bg-white/5 border border-white/10 rounded-lg text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
                </div>
                <div className="flex items-center gap-3">
                  <button onClick={saveAddress} disabled={savingAddress}
                    className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-lg text-sm disabled:opacity-50">
                    {savingAddress ? 'Saving...' : 'Save Address'}
                  </button>
                  <button onClick={() => setShowAddressForm(false)}
                    className="px-4 py-2 border border-white/10 rounded-lg text-zinc-400 text-sm hover:text-white">Cancel</button>
                </div>
              </div>
            )}

            {addresses.length === 0 && !showAddressForm ? (
              <p className="text-zinc-500 text-sm">No saved addresses. Add one to continue.</p>
            ) : (
              <div className="space-y-2">
                {addresses.map((addr) => (
                  <div key={addr.id} onClick={() => setSelectedAddress(addr.id)}
                    className={`flex items-start gap-3 p-3 rounded-xl cursor-pointer border transition-all ${
                      selectedAddress === addr.id ? 'border-emerald-500/50 bg-emerald-500/5' : 'border-white/5 hover:border-white/10'
                    }`}>
                    <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 mt-0.5 ${
                      selectedAddress === addr.id ? 'border-emerald-500 bg-emerald-500' : 'border-zinc-600'
                    }`}>
                      {selectedAddress === addr.id && <Check size={12} className="text-zinc-900" />}
                    </div>
                    <div>
                      <p className="text-white text-sm font-medium">
                        {addr.fullName}
                        {addr.isDefault && <span className="ml-2 text-[10px] bg-emerald-500/20 text-emerald-400 px-1.5 py-0.5 rounded-full font-bold">DEFAULT</span>}
                      </p>
                      <p className="text-zinc-400 text-xs mt-0.5">{addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}</p>
                      <p className="text-zinc-500 text-xs">{addr.city}, {addr.state} - {addr.pincode}</p>
                      <p className="text-zinc-600 text-xs">{addr.phone}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Order Items */}
          <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6">
            <h2 className="text-lg font-semibold text-white mb-4">Order Items ({items.length})</h2>
            <div className="space-y-3">
              {items.map((item) => {
                const product = products[item.productId];
                return (
                  <div key={item.id} className="flex items-center gap-4 py-2">
                    <div className="w-14 h-14 rounded-lg bg-zinc-900 overflow-hidden flex-shrink-0">
                      {product?.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-zinc-700"><ShoppingBag size={18} /></div>
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="text-white text-sm font-medium truncate">{product?.name || 'Product'}</h3>
                      <p className="text-zinc-500 text-xs">Qty: {item.quantity}</p>
                    </div>
                    <span className="text-white font-bold text-sm">${((product?.price || 0) * item.quantity).toFixed(2)}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div>
          <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6 sticky top-20">
            <h2 className="text-lg font-semibold text-white mb-4">Order Summary</h2>

            <div className="mb-4">
              <label className="block text-sm text-zinc-400 mb-1.5">Coupon Code</label>
              <div className="relative">
                <Tag size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input type="text" value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                  placeholder="SAVE20"
                  className="w-full pl-9 pr-3 py-2.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50" />
              </div>
            </div>

            <div className="space-y-2 mb-4 pt-4 border-t border-white/5">
              <div className="flex justify-between text-sm">
                <span className="text-zinc-400">Subtotal</span>
                <span className="text-white">${subtotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-zinc-400">Delivery</span>
                <span className="text-emerald-400">FREE</span>
              </div>
            </div>

            <div className="flex justify-between items-center pt-4 border-t border-white/5 mb-6">
              <span className="text-zinc-400 font-medium">Total</span>
              <span className="text-2xl font-black text-white">${subtotal.toFixed(2)}</span>
            </div>

            {selectedAddress && (
              <div className="bg-emerald-500/5 border border-emerald-500/20 rounded-xl p-3 mb-4">
                <p className="text-xs text-emerald-400 font-medium mb-1">Delivering to:</p>
                {(() => {
                  const addr = addresses.find(a => a.id === selectedAddress);
                  return addr ? (
                    <p className="text-zinc-300 text-xs">{addr.fullName}, {addr.addressLine1}, {addr.city} - {addr.pincode}</p>
                  ) : null;
                })()}
              </div>
            )}

            {error && !showPayment && (
              <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/20 text-red-400 px-3 py-2.5 rounded-xl text-sm mb-4">
                <AlertCircle size={14} /> {error}
              </div>
            )}

            <button onClick={initiatePayment} disabled={!selectedAddress}
              className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-bold rounded-xl transition-all flex items-center justify-center gap-2 disabled:opacity-50">
              <Lock size={18} /> Pay ${subtotal.toFixed(2)}
            </button>

            <div className="flex items-center justify-center gap-2 mt-3">
              <Shield size={12} className="text-zinc-600" />
              <span className="text-zinc-600 text-xs">Secured with 256-bit SSL encryption</span>
            </div>

            {!selectedAddress && addresses.length === 0 && (
              <p className="text-amber-400 text-xs text-center mt-2">Add a delivery address to continue</p>
            )}
          </div>
        </div>
      </div>

      {/* Payment Modal */}
      {showPayment && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-zinc-900 border border-white/10 rounded-2xl w-full max-w-md overflow-hidden">

            {/* Header */}
            <div className="bg-gradient-to-r from-emerald-500/20 to-cyan-500/20 border-b border-white/5 px-6 py-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-emerald-500/20 flex items-center justify-center">
                  <CreditCard size={20} className="text-emerald-400" />
                </div>
                <div>
                  <p className="text-white font-bold">ShopAI Pay</p>
                  <p className="text-zinc-400 text-xs">Secure Payment Gateway</p>
                </div>
              </div>
              {paymentStep === 'card' && (
                <button onClick={() => setShowPayment(false)} className="text-zinc-500 hover:text-white">
                  <X size={20} />
                </button>
              )}
            </div>

            {/* Card Form */}
            {paymentStep === 'card' && (
              <div className="p-6 space-y-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-zinc-400 text-sm">Amount to pay</span>
                  <span className="text-2xl font-black text-white">${subtotal.toFixed(2)}</span>
                </div>

                {/* Payment Methods */}
                <div className="flex gap-2 mb-4">
                  {[
                    { id: 'card', label: 'Card', icon: '💳' },
                    { id: 'upi', label: 'UPI', icon: '📱' },
                    { id: 'netbanking', label: 'Net Banking', icon: '🏦' }
                  ].map(m => (
                    <button key={m.id} onClick={() => setPaymentMethod(m.id)}
                      className={`flex-1 py-2.5 rounded-xl text-xs font-semibold border transition-all flex flex-col items-center gap-1 ${
                        paymentMethod === m.id
                          ? 'border-emerald-500/50 bg-emerald-500/10 text-emerald-400'
                          : 'border-white/5 bg-white/[0.02] text-zinc-500 hover:text-white'
                      }`}>
                      <span className="text-lg">{m.icon}</span>
                      {m.label}
                    </button>
                  ))}
                </div>

                {paymentMethod === 'card' && (
                  <div className="space-y-3">
                    <div>
                      <label className="text-xs text-zinc-500 mb-1 block">Card Number</label>
                      <input type="text" placeholder="4242 4242 4242 4242"
                        value={cardForm.number}
                        onChange={(e) => setCardForm({ ...cardForm, number: formatCardNumber(e.target.value) })}
                        maxLength={19}
                        className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-700 focus:outline-none focus:border-emerald-500/50 text-sm tracking-widest" />
                    </div>
                    <div>
                      <label className="text-xs text-zinc-500 mb-1 block">Cardholder Name</label>
                      <input type="text" placeholder="MANOJ KUMAR"
                        value={cardForm.name}
                        onChange={(e) => setCardForm({ ...cardForm, name: e.target.value.toUpperCase() })}
                        className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-700 focus:outline-none focus:border-emerald-500/50 text-sm tracking-wider" />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="text-xs text-zinc-500 mb-1 block">Expiry</label>
                        <input type="text" placeholder="MM/YY"
                          value={cardForm.expiry}
                          onChange={(e) => setCardForm({ ...cardForm, expiry: formatExpiry(e.target.value) })}
                          maxLength={5}
                          className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-700 focus:outline-none focus:border-emerald-500/50 text-sm tracking-widest" />
                      </div>
                      <div>
                        <label className="text-xs text-zinc-500 mb-1 block">CVV</label>
                        <input type="password" placeholder="•••"
                          value={cardForm.cvv}
                          onChange={(e) => setCardForm({ ...cardForm, cvv: e.target.value.replace(/\D/g, '').slice(0, 3) })}
                          maxLength={3}
                          className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-700 focus:outline-none focus:border-emerald-500/50 text-sm tracking-widest" />
                      </div>
                    </div>
                  </div>
                )}

                {paymentMethod === 'upi' && (
                  <div>
                    <label className="text-xs text-zinc-500 mb-1 block">UPI ID</label>
                    <input type="text" placeholder="yourname@upi"
                      className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-700 focus:outline-none focus:border-emerald-500/50 text-sm" />
                  </div>
                )}

                {paymentMethod === 'netbanking' && (
                  <div className="grid grid-cols-2 gap-2">
                    {['SBI', 'HDFC', 'ICICI', 'Axis', 'Kotak', 'PNB'].map(bank => (
                      <button key={bank}
                        className="py-3 px-4 bg-white/[0.03] border border-white/5 rounded-xl text-zinc-400 text-sm hover:text-white hover:border-white/10 transition-all">
                        {bank}
                      </button>
                    ))}
                  </div>
                )}

                {error && (
                  <div className="flex items-center gap-2 bg-red-500/10 border border-red-500/20 text-red-400 px-3 py-2 rounded-xl text-xs">
                    <AlertCircle size={12} /> {error}
                  </div>
                )}

                <button onClick={processPayment}
                  className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-bold rounded-xl transition-all flex items-center justify-center gap-2">
                  <Lock size={16} /> Pay ${subtotal.toFixed(2)}
                </button>

                <div className="flex items-center justify-center gap-4 pt-2">
                  <div className="flex items-center gap-1.5">
                    <Shield size={10} className="text-zinc-600" />
                    <span className="text-zinc-600 text-[10px]">SSL Secured</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Lock size={10} className="text-zinc-600" />
                    <span className="text-zinc-600 text-[10px]">PCI Compliant</span>
                  </div>
                </div>
              </div>
            )}

            {/* Processing */}
            {paymentStep === 'processing' && (
              <div className="p-12 flex flex-col items-center justify-center">
                <div className="w-16 h-16 border-4 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin mb-6" />
                <p className="text-white font-bold text-lg mb-1">Processing Payment</p>
                <p className="text-zinc-500 text-sm">Please wait, do not close this window...</p>
                <div className="flex items-center gap-2 mt-4">
                  <div className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse" />
                  <span className="text-emerald-400 text-xs">Connecting to payment network</span>
                </div>
              </div>
            )}

            {/* Success */}
            {paymentStep === 'success' && (
              <div className="p-12 flex flex-col items-center justify-center">
                <div className="w-20 h-20 rounded-full bg-emerald-500/20 flex items-center justify-center mb-6">
                  <Check size={40} className="text-emerald-400" />
                </div>
                <p className="text-white font-bold text-lg mb-1">Payment Successful!</p>
                <p className="text-zinc-500 text-sm mb-2">${subtotal.toFixed(2)} paid successfully</p>
                <p className="text-zinc-600 text-xs">Placing your order...</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Checkout;