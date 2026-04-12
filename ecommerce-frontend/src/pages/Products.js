import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import API from '../api/axios';
import { ShoppingCart, Heart, Star, TrendingUp, TrendingDown, Search, SlidersHorizontal, Flame } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const AI_BASE = 'http://localhost:8083';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [trending, setTrending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [adding, setAdding] = useState(null);
  const { user } = useAuth();

  useEffect(() => {
    fetchProducts();
    fetchTrending();
    const interval = setInterval(fetchProducts, 30000);
    return () => clearInterval(interval);
  }, [search]);

  const fetchProducts = async () => {
    try {
      const params = search ? { search } : {};
      const res = await API.get('/products', { params: { ...params, page: 0, size: 20 } });
      if (res.data.success) {
        setProducts(res.data.data.content || []);
      }
    } catch (err) {
      console.error('Failed to fetch products:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchTrending = async () => {
    try {
      const res = await fetch(`${AI_BASE}/api/recommendations/trending?hours=24`);
      const data = await res.json();
      if (data.success) setTrending(data.data || []);
    } catch (err) {
      console.error('Failed to fetch trending:', err);
    }
  };

  const addToCart = async (productId) => {
    if (!user) return;
    setAdding(productId);
    try {
      await API.post(`/cart/add?productId=${productId}&quantity=1`);
    } catch (err) {
      console.error('Add to cart failed:', err);
    } finally {
      setTimeout(() => setAdding(null), 800);
    }
  };

  const addToWishlist = async (productId) => {
    if (!user) return;
    try {
      await API.post(`/wishlist/${productId}`);
    } catch (err) {
      console.error('Add to wishlist failed:', err);
    }
  };

  const getPriceColor = (product) => {
    if (product.priceChangePercent > 0) return 'text-red-400';
    if (product.priceChangePercent < 0) return 'text-emerald-400';
    return 'text-white';
  };

  // Match trending product IDs with actual products
  const trendingProducts = trending
    .map(t => {
      const product = products.find(p => p.id === (t.productId || t.id));
      return product ? { ...product, demandScore: t.demandScore || t.score || 0 } : null;
    })
    .filter(Boolean)
    .slice(0, 4);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-white">Products</h1>
          <p className="text-zinc-500 text-sm mt-1">
            Prices update in real-time based on AI demand analysis
          </p>
        </div>
        <div className="relative w-full sm:w-72">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search products..."
            className="w-full pl-10 pr-4 py-2.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 transition-all"
          />
        </div>
      </div>

      {/* Trending Section */}
      {trendingProducts.length > 0 && !search && (
        <div className="mb-8">
          <div className="flex items-center gap-2 mb-4">
            <Flame size={20} className="text-orange-400" />
            <h2 className="text-lg font-bold text-white">Trending Now</h2>
            <span className="text-xs text-orange-400 bg-orange-500/10 border border-orange-500/20 px-2 py-0.5 rounded-full font-medium">AI Recommended</span>
          </div>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {trendingProducts.map((product) => (
              <Link
                key={`trending-${product.id}`}
                to={`/products/${product.id}`}
                className="group relative bg-gradient-to-br from-orange-500/5 to-red-500/5 border border-orange-500/20 rounded-2xl overflow-hidden hover:border-orange-500/40 transition-all"
              >
                <div className="absolute top-3 left-3 flex items-center gap-1 px-2 py-1 rounded-lg bg-orange-500/20 border border-orange-500/30 backdrop-blur-md z-10">
                  <Flame size={10} className="text-orange-400" />
                  <span className="text-[10px] text-orange-400 font-bold">HOT</span>
                </div>
                <div className="aspect-[4/3] bg-zinc-900 overflow-hidden">
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-zinc-700">
                      <SlidersHorizontal size={32} />
                    </div>
                  )}
                </div>
                <div className="p-3">
                  <p className="text-white font-semibold text-sm truncate group-hover:text-orange-400 transition-colors">{product.name}</p>
                  <div className="flex items-center justify-between mt-1.5">
                    <span className="text-orange-400 font-bold">${product.price?.toFixed(2)}</span>
                    <span className="text-[10px] text-zinc-500">Demand: {product.demandScore}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Live indicator */}
      <div className="flex items-center gap-2 mb-6">
        <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
        <span className="text-xs text-zinc-500 font-medium tracking-wide uppercase">
          Live AI pricing active — {products.length} products
        </span>
      </div>

      {/* Product Grid */}
      {products.length === 0 ? (
        <div className="text-center py-20 text-zinc-500">
          <PackageIcon size={48} className="mx-auto mb-4 opacity-30" />
          <p>No products found</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {products.map((product) => (
            <div
              key={product.id}
              className="group bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden hover:border-emerald-500/20 hover:bg-white/[0.04] transition-all duration-300"
            >
              <Link to={`/products/${product.id}`}>
                <div className="aspect-square bg-zinc-900 relative overflow-hidden">
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-zinc-700">
                      <SlidersHorizontal size={48} />
                    </div>
                  )}
                  {product.priceChangePercent != null && product.priceChangePercent !== 0 && (
                    <div className={`absolute top-3 right-3 flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-bold backdrop-blur-md
                      ${product.priceChangePercent > 0
                        ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                        : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                      }`}>
                      {product.priceChangePercent > 0 ? <TrendingUp size={12} /> : <TrendingDown size={12} />}
                      {product.priceChangePercent > 0 ? '+' : ''}{product.priceChangePercent?.toFixed(1)}%
                    </div>
                  )}
                  {!product.inStock && (
                    <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                      <span className="text-sm font-bold text-red-400 bg-red-500/20 px-3 py-1.5 rounded-lg border border-red-500/30">
                        Out of Stock
                      </span>
                    </div>
                  )}
                </div>
              </Link>
              <div className="p-4">
                <Link to={`/products/${product.id}`}>
                  <p className="text-xs text-zinc-600 font-medium uppercase tracking-wider mb-1">
                    {product.categoryName || 'General'}
                  </p>
                  <h3 className="text-white font-semibold group-hover:text-emerald-400 transition-colors line-clamp-1">
                    {product.name}
                  </h3>
                </Link>
                {product.averageRating > 0 && (
                  <div className="flex items-center gap-1 mt-2">
                    <Star size={12} className="text-amber-400 fill-amber-400" />
                    <span className="text-xs text-zinc-400">
                      {product.averageRating.toFixed(1)} ({product.reviewCount})
                    </span>
                  </div>
                )}
                <div className="flex items-end justify-between mt-3">
                  <span className={`text-xl font-bold ${getPriceColor(product)}`}>
                    ${product.price?.toFixed(2)}
                  </span>
                  {user && product.inStock && (
                    <div className="flex items-center gap-1.5">
                      <button onClick={() => addToWishlist(product.id)}
                        className="p-2 rounded-lg bg-white/5 hover:bg-pink-500/10 hover:text-pink-400 text-zinc-500 transition-all" title="Add to wishlist">
                        <Heart size={16} />
                      </button>
                      <button onClick={() => addToCart(product.id)} disabled={adding === product.id}
                        className={`p-2 rounded-lg transition-all ${
                          adding === product.id
                            ? 'bg-emerald-500 text-zinc-900'
                            : 'bg-white/5 hover:bg-emerald-500/10 hover:text-emerald-400 text-zinc-500'
                        }`} title="Add to cart">
                        <ShoppingCart size={16} />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const PackageIcon = ({ size, className }) => (
  <svg width={size} height={size} className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M16.5 9.4l-9-5.19M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 003 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z" />
    <polyline points="3.27 6.96 12 12.01 20.73 6.96" />
    <line x1="12" y1="22.08" x2="12" y2="12" />
  </svg>
);

export default Products;