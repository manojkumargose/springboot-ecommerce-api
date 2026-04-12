import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import API from '../api/axios';
import {
  Package, Clock, CheckCircle, XCircle, Truck, AlertCircle,
  ChevronDown, ChevronUp, FileText, Star
} from 'lucide-react';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const [cancelling, setCancelling] = useState(null);
  const [reviewModal, setReviewModal] = useState(null);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });
  const [submittingReview, setSubmittingReview] = useState(false);
  const location = useLocation();
  const orderSuccess = location.state?.orderSuccess;

  useEffect(() => { fetchOrders(); }, []);

  const fetchOrders = async () => {
    try {
      const res = await API.get('/orders/my');
      if (res.data.success) {
        setOrders(res.data.data || []);
      }
    } catch (err) {
      console.error('Failed to fetch orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const cancelOrder = async (orderId) => {
    setCancelling(orderId);
    try {
      const res = await API.put(`/orders/${orderId}/cancel`);
      if (res.data.success) {
        fetchOrders();
      }
    } catch (err) {
      console.error('Cancel failed:', err);
    } finally {
      setCancelling(null);
    }
  };

  const downloadInvoice = (orderId) => {
    const token = localStorage.getItem('token');
    fetch(`http://localhost:8080/api/v1/invoices/${orderId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.blob())
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `invoice-order-${orderId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      })
      .catch(err => console.error('Download failed:', err));
  };

  const submitReview = async () => {
    if (!reviewModal) return;
    setSubmittingReview(true);
    try {
      await API.post(`/products/${reviewModal.productId}/reviews`, {
        rating: reviewForm.rating,
        comment: reviewForm.comment
      });
      setReviewModal(null);
      setReviewForm({ rating: 5, comment: '' });
    } catch (err) {
      console.error('Review failed:', err);
    } finally {
      setSubmittingReview(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING': return <Clock size={16} className="text-amber-400" />;
      case 'CONFIRMED': return <CheckCircle size={16} className="text-blue-400" />;
      case 'SHIPPED': return <Truck size={16} className="text-cyan-400" />;
      case 'DELIVERED': return <CheckCircle size={16} className="text-emerald-400" />;
      case 'CANCELLED': return <XCircle size={16} className="text-red-400" />;
      default: return <AlertCircle size={16} className="text-zinc-400" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'CONFIRMED': return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'SHIPPED': return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30';
      case 'DELIVERED': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'CANCELLED': return 'bg-red-500/10 text-red-400 border-red-500/30';
      default: return 'bg-zinc-500/10 text-zinc-400 border-zinc-500/30';
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
      {orderSuccess && (
        <div className="flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 px-4 py-3 rounded-xl mb-6">
          <CheckCircle size={20} />
          <div>
            <p className="font-semibold">Order placed successfully!</p>
            <p className="text-sm text-emerald-400/70">You'll receive a confirmation email shortly.</p>
          </div>
        </div>
      )}

      <h1 className="text-2xl font-bold text-white mb-2">My Orders</h1>
      <p className="text-zinc-500 text-sm mb-8">{orders.length} order{orders.length !== 1 ? 's' : ''}</p>

      {orders.length === 0 ? (
        <div className="text-center py-20">
          <Package size={48} className="mx-auto mb-4 text-zinc-700" />
          <p className="text-zinc-500">No orders yet</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden">
              <div
                className="flex items-center justify-between p-4 cursor-pointer hover:bg-white/[0.02] transition-colors"
                onClick={() => setExpanded(expanded === order.id ? null : order.id)}
              >
                <div className="flex items-center gap-4">
                  <div>
                    <p className="text-white font-semibold">Order #{order.id}</p>
                    <p className="text-zinc-500 text-xs mt-0.5">
                      {new Date(order.createdAt).toLocaleDateString('en-US', {
                        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                      })}
                    </p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-bold border ${getStatusColor(order.status)}`}>
                    {getStatusIcon(order.status)}
                    {order.status}
                  </span>
                  <span className="text-white font-bold">${order.finalAmount?.toFixed(2)}</span>
                  {expanded === order.id ? <ChevronUp size={16} className="text-zinc-500" /> : <ChevronDown size={16} className="text-zinc-500" />}
                </div>
              </div>

              {expanded === order.id && (
                <div className="border-t border-white/5 p-4 space-y-3">
                  {order.items?.map((item, idx) => (
                    <div key={idx} className="flex items-center justify-between py-2">
                      <div>
                        <p className="text-white text-sm">{item.productName}</p>
                        <p className="text-zinc-500 text-xs">Qty: {item.quantity} × ${item.price?.toFixed(2)}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-white text-sm font-medium">
                          ${(item.quantity * item.price).toFixed(2)}
                        </span>
                        {order.status === 'DELIVERED' && (
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setReviewModal({ productId: item.productId, productName: item.productName });
                            }}
                            className="px-2 py-1 text-xs bg-amber-500/10 text-amber-400 border border-amber-500/30 rounded-lg hover:bg-amber-500/20 transition-all"
                          >
                            <Star size={12} className="inline mr-1" />
                            Review
                          </button>
                        )}
                      </div>
                    </div>
                  ))}

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
                    <div className="flex justify-between text-sm font-bold pt-1">
                      <span className="text-zinc-300">Total</span>
                      <span className="text-white">${order.finalAmount?.toFixed(2)}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-3 border-t border-white/5">
                    <button
                      onClick={(e) => { e.stopPropagation(); downloadInvoice(order.id); }}
                      className="px-4 py-2 text-sm bg-blue-500/10 text-blue-400 border border-blue-500/30 rounded-xl hover:bg-blue-500/20 transition-all flex items-center gap-1.5"
                    >
                      <FileText size={14} /> Download Invoice
                    </button>
                    {(order.status === 'PENDING' || order.status === 'CONFIRMED') && (
                      <button
                        onClick={(e) => { e.stopPropagation(); cancelOrder(order.id); }}
                        disabled={cancelling === order.id}
                        className="px-4 py-2 text-sm bg-red-500/10 text-red-400 border border-red-500/30 rounded-xl hover:bg-red-500/20 transition-all disabled:opacity-50"
                      >
                        {cancelling === order.id ? 'Cancelling...' : 'Cancel Order'}
                      </button>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {reviewModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-zinc-900 border border-white/10 rounded-2xl p-6 w-full max-w-md">
            <h3 className="text-lg font-bold text-white mb-1">Write a Review</h3>
            <p className="text-zinc-500 text-sm mb-4">{reviewModal.productName}</p>

            <div className="flex items-center gap-1 mb-4">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setReviewForm({ ...reviewForm, rating: star })}
                  className="p-0.5"
                >
                  <Star
                    size={28}
                    className={star <= reviewForm.rating
                      ? 'text-amber-400 fill-amber-400'
                      : 'text-zinc-700'
                    }
                  />
                </button>
              ))}
              <span className="text-zinc-400 text-sm ml-2">{reviewForm.rating}/5</span>
            </div>

            <textarea
              value={reviewForm.comment}
              onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
              placeholder="Share your experience..."
              rows={4}
              className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 resize-none text-sm"
            />

            <div className="flex items-center gap-3 mt-4">
              <button
                onClick={() => { setReviewModal(null); setReviewForm({ rating: 5, comment: '' }); }}
                className="flex-1 py-2.5 border border-white/10 rounded-xl text-zinc-400 hover:text-white transition-all text-sm"
              >
                Cancel
              </button>
              <button
                onClick={submitReview}
                disabled={submittingReview || !reviewForm.comment.trim()}
                className="flex-1 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl transition-all text-sm disabled:opacity-50"
              >
                {submittingReview ? 'Submitting...' : 'Submit Review'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Orders;