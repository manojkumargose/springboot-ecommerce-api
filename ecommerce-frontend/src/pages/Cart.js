import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../api/axios';
import { Trash2, Minus, Plus, ShoppingBag, ArrowRight } from 'lucide-react';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [products, setProducts] = useState({});
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => { fetchCart(); }, []);

  const fetchCart = async () => {
    try {
      const res = await API.get('/cart');
      const cartData = res.data.data || res.data;
      console.log('Cart data:', JSON.stringify(cartData));
      setCart(cartData);
      if (cartData.items?.length > 0) {
        const prods = {};
        for (const item of cartData.items) {
          const pid = item.productId || item.product_id;
          try {
            const pRes = await API.get(`/products/${pid}`);
            if (pRes.data.success) prods[pid] = pRes.data.data;
          } catch {}
        }
        setProducts(prods);
      }
    } catch (err) {
      console.error('Failed to fetch cart:', err);
    } finally {
      setLoading(false);
    }
  };

  const getProductId = (item) => item.productId || item.product_id;

  const updateQty = async (productId, quantity) => {
    if (quantity <= 0) { removeItem(productId); return; }
    try {
      await API.put('/cart/update', null, { params: { productId, quantity } });
      fetchCart();
    } catch (err) { console.error('Update failed:', err); }
  };

  const removeItem = async (productId) => {
    console.log('Removing product:', productId);
    try {
      const res = await API.delete('/cart/remove', { params: { productId } });
      console.log('Remove response:', res.data);
      fetchCart();
    } catch (err) { console.error('Remove failed:', err); }
  };

  const getTotal = () => {
    if (!cart?.items) return 0;
    return cart.items.reduce((sum, item) => {
      const product = products[getProductId(item)];
      return sum + (product?.price || 0) * item.quantity;
    }, 0);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  const items = cart?.items || [];

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-white mb-2">Shopping Cart</h1>
      <p className="text-zinc-500 text-sm mb-8">{items.length} item{items.length !== 1 ? 's' : ''}</p>

      {items.length === 0 ? (
        <div className="text-center py-20">
          <ShoppingBag size={48} className="mx-auto mb-4 text-zinc-700" />
          <p className="text-zinc-500 mb-4">Your cart is empty</p>
          <Link to="/" className="inline-flex items-center gap-2 px-6 py-2.5 bg-emerald-500 text-zinc-900 rounded-xl font-semibold hover:bg-emerald-400 transition-all">
            Browse Products <ArrowRight size={16} />
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => {
            const pid = getProductId(item);
            const product = products[pid];
            return (
              <div key={item.id || pid} className="flex items-center gap-4 p-4 bg-white/[0.02] border border-white/5 rounded-xl hover:border-white/10 transition-all">
                <div className="w-20 h-20 rounded-lg bg-zinc-900 overflow-hidden flex-shrink-0">
                  {product?.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-zinc-700">
                      <ShoppingBag size={24} />
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="text-white font-medium truncate">{product?.name || `Product #${pid}`}</h3>
                  <p className="text-emerald-400 font-bold mt-1">${product?.price?.toFixed(2) || '—'}</p>
                </div>
                <div className="flex items-center border border-white/10 rounded-lg overflow-hidden">
                  <button onClick={() => updateQty(pid, item.quantity - 1)} className="px-2.5 py-1.5 text-zinc-400 hover:text-white hover:bg-white/5">
                    <Minus size={14} />
                  </button>
                  <span className="px-3 py-1.5 text-white text-sm font-medium">{item.quantity}</span>
                  <button onClick={() => updateQty(pid, item.quantity + 1)} className="px-2.5 py-1.5 text-zinc-400 hover:text-white hover:bg-white/5">
                    <Plus size={14} />
                  </button>
                </div>
                <div className="text-right w-24 hidden sm:block">
                  <span className="text-white font-bold">${((product?.price || 0) * item.quantity).toFixed(2)}</span>
                </div>
                <button onClick={() => removeItem(pid)} className="p-2 text-zinc-600 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all">
                  <Trash2 size={16} />
                </button>
              </div>
            );
          })}

          <div className="border-t border-white/10 pt-6 mt-6">
            <div className="flex items-center justify-between mb-6">
              <span className="text-zinc-400 text-lg">Total</span>
              <span className="text-3xl font-black text-white">${getTotal().toFixed(2)}</span>
            </div>
            <button
              onClick={() => navigate('/checkout')}
              className="w-full py-3.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-bold rounded-xl transition-all flex items-center justify-center gap-2 text-lg"
            >
              Proceed to Checkout <ArrowRight size={20} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;