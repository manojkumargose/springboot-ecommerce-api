import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import {
  BarChart3, TrendingUp, TrendingDown, Activity, DollarSign,
  Eye, ShoppingCart, Heart, Package, Zap, RefreshCw
} from 'lucide-react';

const AIDashboard = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);

  useEffect(() => {
    fetchProducts();
    const interval = setInterval(fetchProducts, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await API.get('/products', { params: { page: 0, size: 50 } });
      if (res.data.success) {
        setProducts(res.data.data.content || []);
        setLastUpdate(new Date());
      }
    } catch (err) {
      console.error('Failed to fetch:', err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const manualRefresh = () => {
    setRefreshing(true);
    fetchProducts();
  };

  const stats = {
    total: products.length,
    avgPrice: products.length > 0
      ? (products.reduce((s, p) => s + (p.price || 0), 0) / products.length)
      : 0,
    priceUp: products.filter(p => p.priceChangePercent > 0).length,
    priceDown: products.filter(p => p.priceChangePercent < 0).length,
  };

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
      <div className="flex items-start justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <BarChart3 className="text-emerald-400" size={28} />
            AI Pricing Dashboard
          </h1>
          <p className="text-zinc-500 text-sm mt-1">
            Real-time demand-based dynamic pricing engine
          </p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
            <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xs text-emerald-400 font-medium">AI Engine Active</span>
          </div>
          <button
            onClick={manualRefresh}
            className="p-2 rounded-lg bg-white/5 hover:bg-white/10 text-zinc-400 hover:text-white transition-all"
          >
            <RefreshCw size={16} className={refreshing ? 'animate-spin' : ''} />
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard icon={Package} label="Total Products" value={stats.total} color="blue" />
        <StatCard icon={DollarSign} label="Avg Price" value={`$${stats.avgPrice.toFixed(2)}`} color="emerald" />
        <StatCard icon={TrendingUp} label="Price Surges" value={stats.priceUp} color="red" />
        <StatCard icon={TrendingDown} label="Price Drops" value={stats.priceDown} color="cyan" />
      </div>

      {/* Demand Weights */}
      <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6 mb-8">
        <h2 className="text-lg font-bold text-white mb-4 flex items-center gap-2">
          <Zap size={18} className="text-amber-400" />
          AI Demand Scoring Weights
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
          <WeightBadge icon={Eye} label="View" weight="1x" color="zinc" />
          <WeightBadge icon={Heart} label="Wishlist" weight="2x" color="pink" />
          <WeightBadge icon={ShoppingCart} label="Cart Add" weight="3x" color="amber" />
          <WeightBadge icon={DollarSign} label="Purchase" weight="5x" color="emerald" />
          <WeightBadge icon={Activity} label="Cancel" weight="-1x" color="red" />
        </div>
      </div>

      {/* Product Pricing Table */}
      <div className="bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden">
        <div className="px-6 py-4 border-b border-white/5 flex items-center justify-between">
          <h2 className="text-lg font-bold text-white">Live Product Prices</h2>
          {lastUpdate && (
            <span className="text-xs text-zinc-600">
              Updated {lastUpdate.toLocaleTimeString()}
            </span>
          )}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                <th className="text-left text-xs text-zinc-500 font-medium uppercase tracking-wider px-6 py-3">Product</th>
                <th className="text-right text-xs text-zinc-500 font-medium uppercase tracking-wider px-6 py-3">Current Price</th>
                <th className="text-right text-xs text-zinc-500 font-medium uppercase tracking-wider px-6 py-3">Change</th>
                <th className="text-center text-xs text-zinc-500 font-medium uppercase tracking-wider px-6 py-3">Demand</th>
                <th className="text-right text-xs text-zinc-500 font-medium uppercase tracking-wider px-6 py-3">Stock</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product, i) => (
                <tr
                  key={product.id}
                  className="border-b border-white/[0.03] hover:bg-white/[0.02] transition-colors"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-zinc-900 overflow-hidden flex-shrink-0">
                        {product.imageUrl ? (
                          <img src={product.imageUrl} alt="" className="w-full h-full object-cover" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-zinc-700">
                            <Package size={16} />
                          </div>
                        )}
                      </div>
                      <div>
                        <p className="text-white font-medium text-sm">{product.name}</p>
                        <p className="text-zinc-600 text-xs">{product.categoryName}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <span className="text-white font-bold">${product.price?.toFixed(2)}</span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {product.priceChangePercent != null && product.priceChangePercent !== 0 ? (
                      <span className={`inline-flex items-center gap-1 text-sm font-semibold
                        ${product.priceChangePercent > 0 ? 'text-red-400' : 'text-emerald-400'}`}
                      >
                        {product.priceChangePercent > 0 ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                        {product.priceChangePercent > 0 ? '+' : ''}{product.priceChangePercent?.toFixed(1)}%
                      </span>
                    ) : (
                      <span className="text-zinc-600 text-sm">—</span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-center">
                    <DemandBadge level={product.demandLevel} score={product.demandScore} />
                  </td>
                  <td className="px-6 py-4 text-right">
                    <span className={`text-sm font-medium ${product.stockQuantity <= 5 ? 'text-red-400' : 'text-zinc-400'}`}>
                      {product.stockQuantity}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ icon: Icon, label, value, color }) => {
  const colors = {
    blue: 'text-blue-400 bg-blue-500/10 border-blue-500/20',
    emerald: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
    red: 'text-red-400 bg-red-500/10 border-red-500/20',
    cyan: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
  };

  return (
    <div className="bg-white/[0.02] border border-white/5 rounded-xl p-4">
      <div className={`w-9 h-9 rounded-lg flex items-center justify-center mb-3 border ${colors[color]}`}>
        <Icon size={16} />
      </div>
      <p className="text-2xl font-bold text-white">{value}</p>
      <p className="text-xs text-zinc-500 mt-0.5">{label}</p>
    </div>
  );
};

const WeightBadge = ({ icon: Icon, label, weight, color }) => {
  const colors = {
    zinc: 'border-zinc-700 text-zinc-400',
    pink: 'border-pink-500/30 text-pink-400',
    amber: 'border-amber-500/30 text-amber-400',
    emerald: 'border-emerald-500/30 text-emerald-400',
    red: 'border-red-500/30 text-red-400',
  };

  return (
    <div className={`flex items-center gap-2 p-3 rounded-xl border bg-white/[0.02] ${colors[color]}`}>
      <Icon size={16} />
      <div>
        <p className="text-xs font-medium">{label}</p>
        <p className="text-lg font-black">{weight}</p>
      </div>
    </div>
  );
};

const DemandBadge = ({ level, score }) => {
  if (!level) return <span className="text-zinc-600 text-sm">—</span>;

  const styles = {
    HIGH: 'bg-red-500/10 text-red-400 border-red-500/30',
    MEDIUM: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
    LOW: 'bg-zinc-500/10 text-zinc-400 border-zinc-500/30',
  };

  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold border ${styles[level] || styles.LOW}`}>
      {level} {score > 0 && <span className="opacity-60">({score})</span>}
    </span>
  );
};

export default AIDashboard;