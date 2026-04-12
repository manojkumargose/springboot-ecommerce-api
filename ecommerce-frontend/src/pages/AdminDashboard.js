import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import {
  BarChart3, DollarSign, ShoppingBag, Users, Package,
  TrendingUp, XCircle, Clock, CheckCircle, Truck
} from 'lucide-react';

const AdminDashboard = () => {
  const [stats, setStats] = useState({ orders: [], products: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => { fetchStats(); }, []);

  const fetchStats = async () => {
    try {
      const [ordersRes, productsRes] = await Promise.all([
        API.get('/orders'),
        API.get('/products', { params: { page: 0, size: 100 } })
      ]);
      setStats({
        orders: ordersRes.data.data || [],
        products: productsRes.data.data?.content || []
      });
    } catch (err) {
      console.error('Failed to fetch stats:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  const orders = stats.orders;
  const products = stats.products;
  const totalRevenue = orders.filter(o => o.status !== 'CANCELLED').reduce((s, o) => s + (o.finalAmount || 0), 0);
  const pendingOrders = orders.filter(o => o.status === 'PENDING').length;
  const cancelledOrders = orders.filter(o => o.status === 'CANCELLED').length;
  const deliveredOrders = orders.filter(o => o.status === 'DELIVERED').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white flex items-center gap-3">
          <BarChart3 className="text-emerald-400" size={28} />
          Admin Dashboard
        </h1>
        <p className="text-zinc-500 text-sm mt-1">Overview of your store performance</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard icon={DollarSign} label="Total Revenue" value={`$${totalRevenue.toFixed(2)}`} color="emerald" />
        <StatCard icon={ShoppingBag} label="Total Orders" value={orders.length} color="blue" />
        <StatCard icon={Package} label="Products" value={products.length} color="amber" />
        <StatCard icon={XCircle} label="Cancellations" value={cancelledOrders} color="red" />
      </div>

      {/* Order Status Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
        <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6">
          <h2 className="text-lg font-bold text-white mb-4">Order Status</h2>
          <div className="space-y-3">
            <StatusRow icon={Clock} label="Pending" count={pendingOrders} color="amber" total={orders.length} />
            <StatusRow icon={CheckCircle} label="Confirmed" count={orders.filter(o => o.status === 'CONFIRMED').length} color="blue" total={orders.length} />
            <StatusRow icon={Truck} label="Shipped" count={orders.filter(o => o.status === 'SHIPPED').length} color="cyan" total={orders.length} />
            <StatusRow icon={CheckCircle} label="Delivered" count={deliveredOrders} color="emerald" total={orders.length} />
            <StatusRow icon={XCircle} label="Cancelled" count={cancelledOrders} color="red" total={orders.length} />
          </div>
        </div>

        {/* Recent Orders */}
        <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6">
          <h2 className="text-lg font-bold text-white mb-4">Recent Orders</h2>
          <div className="space-y-2">
            {orders.slice(0, 8).map((order) => (
              <div key={order.id} className="flex items-center justify-between py-2 border-b border-white/5 last:border-0">
                <div>
                  <p className="text-white text-sm font-medium">Order #{order.id}</p>
                  <p className="text-zinc-600 text-xs">{order.username}</p>
                </div>
                <div className="text-right">
                  <p className="text-white text-sm font-bold">${order.finalAmount?.toFixed(2)}</p>
                  <p className={`text-xs font-medium ${
                    order.status === 'CANCELLED' ? 'text-red-400' :
                    order.status === 'DELIVERED' ? 'text-emerald-400' :
                    'text-amber-400'
                  }`}>{order.status}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Top Products */}
      <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6">
        <h2 className="text-lg font-bold text-white mb-4">Product Inventory</h2>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-white/5">
                <th className="text-left text-xs text-zinc-500 font-medium uppercase px-4 py-2">Product</th>
                <th className="text-right text-xs text-zinc-500 font-medium uppercase px-4 py-2">Price</th>
                <th className="text-right text-xs text-zinc-500 font-medium uppercase px-4 py-2">Stock</th>
                <th className="text-center text-xs text-zinc-500 font-medium uppercase px-4 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id} className="border-b border-white/[0.03]">
                  <td className="px-4 py-3 text-white text-sm">{p.name}</td>
                  <td className="px-4 py-3 text-right text-emerald-400 text-sm font-bold">${p.price?.toFixed(2)}</td>
                  <td className="px-4 py-3 text-right text-zinc-400 text-sm">{p.stockQuantity}</td>
                  <td className="px-4 py-3 text-center">
                    <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                      p.inStock ? 'bg-emerald-500/10 text-emerald-400' : 'bg-red-500/10 text-red-400'
                    }`}>{p.inStock ? 'In Stock' : 'Out'}</span>
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
    emerald: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
    blue: 'text-blue-400 bg-blue-500/10 border-blue-500/20',
    amber: 'text-amber-400 bg-amber-500/10 border-amber-500/20',
    red: 'text-red-400 bg-red-500/10 border-red-500/20',
  };
  return (
    <div className="bg-white/[0.02] border border-white/5 rounded-xl p-5">
      <div className={`w-10 h-10 rounded-lg flex items-center justify-center mb-3 border ${colors[color]}`}>
        <Icon size={18} />
      </div>
      <p className="text-2xl font-black text-white">{value}</p>
      <p className="text-xs text-zinc-500 mt-0.5">{label}</p>
    </div>
  );
};

const StatusRow = ({ icon: Icon, label, count, color, total }) => {
  const pct = total > 0 ? (count / total) * 100 : 0;
  const colors = { amber: 'bg-amber-500', blue: 'bg-blue-500', cyan: 'bg-cyan-500', emerald: 'bg-emerald-500', red: 'bg-red-500' };
  return (
    <div className="flex items-center gap-3">
      <Icon size={16} className={`text-${color}-400`} />
      <span className="text-zinc-400 text-sm w-24">{label}</span>
      <div className="flex-1 h-2 bg-white/5 rounded-full overflow-hidden">
        <div className={`h-full rounded-full ${colors[color]}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-white text-sm font-bold w-8 text-right">{count}</span>
    </div>
  );
};

export default AdminDashboard;