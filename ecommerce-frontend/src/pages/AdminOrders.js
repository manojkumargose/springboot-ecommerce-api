import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import {
  ClipboardList, Clock, CheckCircle, Truck, XCircle, Package,
  ChevronDown, ChevronUp
} from 'lucide-react';

const AdminOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const [updating, setUpdating] = useState(null);
  const [filter, setFilter] = useState('ALL');

  useEffect(() => { fetchOrders(); }, []);

  const fetchOrders = async () => {
    try {
      const res = await API.get('/orders');
      if (res.data.success) setOrders(res.data.data || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const updateStatus = async (orderId, status) => {
    setUpdating(orderId);
    try {
      await API.put(`/orders/${orderId}/status`, null, { params: { status } });
      fetchOrders();
    } catch (err) { console.error(err); }
    finally { setUpdating(null); }
  };

  const statusFlow = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];

  const getNextStatus = (current) => {
    const idx = statusFlow.indexOf(current);
    return idx >= 0 && idx < statusFlow.length - 1 ? statusFlow[idx + 1] : null;
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
      CONFIRMED: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
      SHIPPED: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30',
      DELIVERED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
      CANCELLED: 'bg-red-500/10 text-red-400 border-red-500/30'
    };
    return colors[status] || 'bg-zinc-500/10 text-zinc-400 border-zinc-500/30';
  };

  const getStatusIcon = (status) => {
    const icons = {
      PENDING: <Clock size={14} />, CONFIRMED: <CheckCircle size={14} />,
      SHIPPED: <Truck size={14} />, DELIVERED: <CheckCircle size={14} />,
      CANCELLED: <XCircle size={14} />
    };
    return icons[status] || <Package size={14} />;
  };

  const filtered = filter === 'ALL' ? orders : orders.filter(o => o.status === filter);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white flex items-center gap-3">
          <ClipboardList className="text-emerald-400" size={28} />
          Order Management
        </h1>
        <p className="text-zinc-500 text-sm mt-1">{orders.length} total orders</p>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-2 mb-6 flex-wrap">
        {['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map((s) => (
          <button
            key={s}
            onClick={() => setFilter(s)}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold border transition-all ${
              filter === s
                ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                : 'bg-white/[0.02] text-zinc-500 border-white/5 hover:text-white'
            }`}
          >
            {s} {s !== 'ALL' && `(${orders.filter(o => s === 'ALL' || o.status === s).length})`}
          </button>
        ))}
      </div>

      {/* Orders List */}
      <div className="space-y-3">
        {filtered.map((order) => (
          <div key={order.id} className="bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden">
            <div
              className="flex items-center justify-between p-4 cursor-pointer hover:bg-white/[0.02]"
              onClick={() => setExpanded(expanded === order.id ? null : order.id)}
            >
              <div className="flex items-center gap-4">
                <div>
                  <p className="text-white font-semibold">Order #{order.id}</p>
                  <p className="text-zinc-500 text-xs">{order.username} • {new Date(order.createdAt).toLocaleDateString()}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold border ${getStatusColor(order.status)}`}>
                  {getStatusIcon(order.status)} {order.status}
                </span>
                <span className="text-white font-bold">${order.finalAmount?.toFixed(2)}</span>
                {expanded === order.id ? <ChevronUp size={16} className="text-zinc-500" /> : <ChevronDown size={16} className="text-zinc-500" />}
              </div>
            </div>

            {expanded === order.id && (
              <div className="border-t border-white/5 p-4 space-y-3">
                {/* Items */}
                {order.items?.map((item, idx) => (
                  <div key={idx} className="flex items-center justify-between py-1.5">
                    <div>
                      <p className="text-white text-sm">{item.productName}</p>
                      <p className="text-zinc-500 text-xs">Qty: {item.quantity} × ${item.price?.toFixed(2)}</p>
                    </div>
                    <span className="text-white text-sm font-medium">${(item.quantity * item.price).toFixed(2)}</span>
                  </div>
                ))}

                {/* Pricing */}
                <div className="border-t border-white/5 pt-3 space-y-1">
                  <div className="flex justify-between text-sm">
                    <span className="text-zinc-500">Subtotal</span>
                    <span className="text-zinc-300">${order.totalAmount?.toFixed(2)}</span>
                  </div>
                  {order.discountAmount > 0 && (
                    <div className="flex justify-between text-sm">
                      <span className="text-zinc-500">Discount {order.couponCode && `(${order.couponCode})`}</span>
                      <span className="text-emerald-400">-${order.discountAmount?.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-sm font-bold">
                    <span className="text-zinc-300">Total</span>
                    <span className="text-white">${order.finalAmount?.toFixed(2)}</span>
                  </div>
                </div>

                {/* Status Update */}
                {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                  <div className="flex items-center gap-2 pt-3 border-t border-white/5">
                    <span className="text-zinc-500 text-sm">Update status:</span>
                    {getNextStatus(order.status) && (
                      <button
                        onClick={(e) => { e.stopPropagation(); updateStatus(order.id, getNextStatus(order.status)); }}
                        disabled={updating === order.id}
                        className="px-3 py-1.5 text-sm bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded-lg hover:bg-emerald-500/20 disabled:opacity-50"
                      >
                        {updating === order.id ? 'Updating...' : `→ ${getNextStatus(order.status)}`}
                      </button>
                    )}
                    <button
                      onClick={(e) => { e.stopPropagation(); updateStatus(order.id, 'CANCELLED'); }}
                      disabled={updating === order.id}
                      className="px-3 py-1.5 text-sm bg-red-500/10 text-red-400 border border-red-500/30 rounded-lg hover:bg-red-500/20 disabled:opacity-50"
                    >
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminOrders;