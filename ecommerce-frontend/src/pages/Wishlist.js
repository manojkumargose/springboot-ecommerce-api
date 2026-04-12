import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import { Heart, Trash2, ShoppingCart, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

const Wishlist = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchWishlist(); }, []);

  const fetchWishlist = async () => {
    try {
      const res = await API.get('/wishlist');
      setItems(res.data.data || []);
    } catch (err) {
      console.error('Failed to fetch wishlist:', err);
    } finally {
      setLoading(false);
    }
  };

  const removeItem = async (productId) => {
    try {
      await API.delete(`/wishlist/${productId}`);
      fetchWishlist();
    } catch (err) {
      console.error('Remove failed:', err);
    }
  };

  const addToCart = async (productId) => {
    try {
      await API.post(`/cart/add?productId=${productId}&quantity=1`);
    } catch (err) {
      console.error('Add to cart failed:', err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <h1 className="text-2xl font-bold text-white mb-2">Wishlist</h1>
      <p className="text-zinc-500 text-sm mb-8">{items.length} item{items.length !== 1 ? 's' : ''} saved</p>

      {items.length === 0 ? (
        <div className="text-center py-20">
          <Heart size={48} className="mx-auto mb-4 text-zinc-700" />
          <p className="text-zinc-500 mb-4">Your wishlist is empty</p>
          <Link
            to="/"
            className="inline-flex items-center gap-2 px-6 py-2.5 bg-emerald-500 text-zinc-900 rounded-xl font-semibold hover:bg-emerald-400 transition-all"
          >
            Browse Products <ArrowRight size={16} />
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {items.map((item) => (
            <div
              key={item.id}
              className="flex items-center gap-4 p-4 bg-white/[0.02] border border-white/5 rounded-xl hover:border-pink-500/20 transition-all"
            >
              <Link to={`/products/${item.product?.id}`} className="w-20 h-20 rounded-lg bg-zinc-900 overflow-hidden flex-shrink-0">
                {item.product?.imageUrl ? (
                  <img src={item.product.imageUrl} alt={item.product.name} className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-zinc-700">
                    <Heart size={24} />
                  </div>
                )}
              </Link>

              <div className="flex-1 min-w-0">
                <Link to={`/products/${item.product?.id}`}>
                  <h3 className="text-white font-medium truncate hover:text-emerald-400 transition-colors">
                    {item.product?.name || 'Product'}
                  </h3>
                </Link>
                <p className="text-emerald-400 font-bold mt-1">
                  ${item.product?.price?.toFixed(2) || '—'}
                </p>
              </div>

              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => addToCart(item.product?.id)}
                  className="p-2 rounded-lg bg-white/5 hover:bg-emerald-500/10 hover:text-emerald-400 text-zinc-500 transition-all"
                  title="Move to cart"
                >
                  <ShoppingCart size={16} />
                </button>
                <button
                  onClick={() => removeItem(item.product?.id)}
                  className="p-2 rounded-lg bg-white/5 hover:bg-red-500/10 hover:text-red-400 text-zinc-500 transition-all"
                  title="Remove"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Wishlist;