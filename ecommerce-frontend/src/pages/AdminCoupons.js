import React, { useState, useEffect } from 'react';
import API from '../api/axios';
import { Tag, Plus, Trash2, X, Save, ToggleLeft, ToggleRight } from 'lucide-react';

const AdminCoupons = () => {
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ code: '', discountPercent: '', minOrderAmount: '', maxDiscount: '', expiryDate: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { fetchCoupons(); }, []);

  const fetchCoupons = async () => {
    try {
      const res = await API.get('/coupons');
      if (res.data.success) setCoupons(res.data.data || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const createCoupon = async () => {
    setSaving(true);
    setError('');
    try {
      await API.post('/coupons', {
        code: form.code.toUpperCase(),
        discountPercent: parseFloat(form.discountPercent),
        minOrderAmount: parseFloat(form.minOrderAmount) || 0,
        maxDiscount: parseFloat(form.maxDiscount) || null,
        expiryDate: form.expiryDate || null
      });
      setShowForm(false);
      setForm({ code: '', discountPercent: '', minOrderAmount: '', maxDiscount: '', expiryDate: '' });
      fetchCoupons();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create coupon');
    } finally { setSaving(false); }
  };

  const deactivateCoupon = async (id) => {
    try {
      await API.patch(`/coupons/${id}/deactivate`);
      fetchCoupons();
    } catch (err) { console.error(err); }
  };

  const deleteCoupon = async (id) => {
    if (!window.confirm('Delete this coupon?')) return;
    try {
      await API.delete(`/coupons/${id}`);
      fetchCoupons();
    } catch (err) { console.error(err); }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <Tag className="text-emerald-400" size={28} />
            Coupon Management
          </h1>
          <p className="text-zinc-500 text-sm mt-1">{coupons.length} coupons</p>
        </div>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl transition-all">
          <Plus size={18} /> Create Coupon
        </button>
      </div>

      {/* Create Form */}
      {showForm && (
        <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-6 mb-6">
          <h2 className="text-lg font-bold text-white mb-4">New Coupon</h2>
          <div className="grid grid-cols-2 gap-3">
            <input type="text" placeholder="Coupon Code (e.g. SAVE20)" value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value })}
              className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm uppercase" />
            <input type="number" placeholder="Discount % (e.g. 20)" value={form.discountPercent}
              onChange={(e) => setForm({ ...form, discountPercent: e.target.value })}
              className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
            <input type="number" placeholder="Min Order Amount" value={form.minOrderAmount}
              onChange={(e) => setForm({ ...form, minOrderAmount: e.target.value })}
              className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
            <input type="number" placeholder="Max Discount Cap" value={form.maxDiscount}
              onChange={(e) => setForm({ ...form, maxDiscount: e.target.value })}
              className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
            <input type="date" placeholder="Expiry Date" value={form.expiryDate}
              onChange={(e) => setForm({ ...form, expiryDate: e.target.value })}
              className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
          </div>
          {error && <p className="text-red-400 text-sm mt-3">{error}</p>}
          <div className="flex items-center gap-3 mt-4">
            <button onClick={createCoupon} disabled={saving}
              className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl text-sm flex items-center gap-2 disabled:opacity-50">
              {saving ? 'Creating...' : <><Save size={14} /> Create</>}
            </button>
            <button onClick={() => setShowForm(false)} className="px-5 py-2.5 border border-white/10 rounded-xl text-zinc-400 text-sm">Cancel</button>
          </div>
        </div>
      )}

      {/* Coupons List */}
      {coupons.length === 0 ? (
        <div className="text-center py-20">
          <Tag size={48} className="mx-auto mb-4 text-zinc-700" />
          <p className="text-zinc-500">No coupons yet</p>
        </div>
      ) : (
        <div className="space-y-3">
          {coupons.map((coupon) => (
            <div key={coupon.id} className="flex items-center justify-between p-4 bg-white/[0.02] border border-white/5 rounded-xl hover:border-white/10 transition-all">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
                  <Tag size={20} className="text-emerald-400" />
                </div>
                <div>
                  <p className="text-white font-bold text-lg tracking-wider">{coupon.code}</p>
                  <p className="text-zinc-500 text-xs">
                    {coupon.discountPercent}% off
                    {coupon.minOrderAmount > 0 && ` • Min order: $${coupon.minOrderAmount}`}
                    {coupon.maxDiscount && ` • Max: $${coupon.maxDiscount}`}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className={`text-xs font-bold px-2.5 py-1 rounded-full border ${
                  coupon.active !== false
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                    : 'bg-red-500/10 text-red-400 border-red-500/30'
                }`}>
                  {coupon.active !== false ? 'ACTIVE' : 'INACTIVE'}
                </span>
                {coupon.active !== false && (
                  <button onClick={() => deactivateCoupon(coupon.id)}
                    className="p-2 rounded-lg hover:bg-amber-500/10 text-zinc-500 hover:text-amber-400 transition-all" title="Deactivate">
                    <ToggleRight size={16} />
                  </button>
                )}
                <button onClick={() => deleteCoupon(coupon.id)}
                  className="p-2 rounded-lg hover:bg-red-500/10 text-zinc-500 hover:text-red-400 transition-all" title="Delete">
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

export default AdminCoupons;