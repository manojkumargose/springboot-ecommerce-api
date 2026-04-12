import React, { useState, useEffect, useRef } from 'react';
import API from '../api/axios';
import { Plus, Pencil, Trash2, Package, X, Save, Upload, ImageIcon } from 'lucide-react';

const AdminProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({ name: '', price: '', description: '', categoryId: 1, stockQuantity: '' });
  const [editId, setEditId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(null);
  const fileInputRef = useRef(null);
  const [uploadTargetId, setUploadTargetId] = useState(null);

  useEffect(() => { fetchProducts(); }, []);

  const fetchProducts = async () => {
    try {
      const res = await API.get('/products', { params: { page: 0, size: 100 } });
      if (res.data.success) setProducts(res.data.data?.content || []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openAdd = () => {
    setForm({ name: '', price: '', description: '', categoryId: 1, stockQuantity: '' });
    setEditId(null);
    setError('');
    setModal('add');
  };

  const openEdit = (product) => {
    setForm({
      name: product.name,
      price: product.price,
      description: product.description || '',
      categoryId: 1,
      stockQuantity: product.stockQuantity
    });
    setEditId(product.id);
    setError('');
    setModal('edit');
  };

  const saveProduct = async () => {
    setSaving(true);
    setError('');
    try {
      const payload = {
        name: form.name,
        price: parseFloat(form.price),
        description: form.description,
        categoryId: parseInt(form.categoryId),
        stockQuantity: parseInt(form.stockQuantity)
      };
      if (modal === 'add') {
        await API.post('/products', payload);
      } else {
        await API.put(`/products/${editId}`, payload);
      }
      setModal(null);
      fetchProducts();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save product');
    } finally { setSaving(false); }
  };

  const deleteProduct = async (id) => {
    if (!window.confirm('This will mark the product as out of stock. Continue?')) return;
    try {
      await API.delete(`/products/${id}`);
      fetchProducts();
    } catch (err) {
      try {
        const product = products.find(p => p.id === id);
        if (product) {
          await API.put(`/products/${id}`, { ...product, stockQuantity: 0, categoryId: 1 });
          fetchProducts();
        }
      } catch (e) { console.error(e); }
    }
  };

  const triggerImageUpload = (productId) => {
    setUploadTargetId(productId);
    fileInputRef.current?.click();
  };

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file || !uploadTargetId) return;

    setUploading(uploadTargetId);
    try {
      const formData = new FormData();
      formData.append('file', file);
      await API.post(`/products/${uploadTargetId}/image`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      fetchProducts();
    } catch (err) {
      console.error('Image upload failed:', err);
      alert('Failed to upload image. Check Cloudinary config.');
    } finally {
      setUploading(null);
      setUploadTargetId(null);
      e.target.value = '';
    }
  };

  const deleteImage = async (productId) => {
    if (!window.confirm('Remove this product image?')) return;
    try {
      await API.delete(`/products/${productId}/image`);
      fetchProducts();
    } catch (err) {
      console.error('Delete image failed:', err);
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
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
      {/* Hidden file input */}
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleImageUpload}
        accept="image/*"
        className="hidden"
      />

      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-3">
            <Package className="text-emerald-400" size={28} />
            Product Management
          </h1>
          <p className="text-zinc-500 text-sm mt-1">{products.length} products</p>
        </div>
        <button onClick={openAdd} className="flex items-center gap-2 px-4 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl transition-all">
          <Plus size={18} /> Add Product
        </button>
      </div>

      <div className="bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/5">
              <th className="text-left text-xs text-zinc-500 font-medium uppercase px-6 py-3">Product</th>
              <th className="text-right text-xs text-zinc-500 font-medium uppercase px-6 py-3">Price</th>
              <th className="text-right text-xs text-zinc-500 font-medium uppercase px-6 py-3">Stock</th>
              <th className="text-center text-xs text-zinc-500 font-medium uppercase px-6 py-3">Rating</th>
              <th className="text-center text-xs text-zinc-500 font-medium uppercase px-6 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id} className="border-b border-white/[0.03] hover:bg-white/[0.02]">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div
                      className="w-12 h-12 rounded-lg bg-zinc-900 overflow-hidden flex-shrink-0 relative group cursor-pointer border border-white/5"
                      onClick={() => triggerImageUpload(p.id)}
                    >
                      {uploading === p.id ? (
                        <div className="w-full h-full flex items-center justify-center">
                          <div className="w-5 h-5 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
                        </div>
                      ) : p.imageUrl ? (
                        <>
                          <img src={p.imageUrl} alt="" className="w-full h-full object-cover" />
                          <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-all">
                            <Upload size={14} className="text-white" />
                          </div>
                        </>
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-zinc-700 group-hover:text-emerald-400 transition-colors">
                          <ImageIcon size={18} />
                        </div>
                      )}
                    </div>
                    <div>
                      <p className="text-white text-sm font-medium">{p.name}</p>
                      <p className="text-zinc-600 text-xs">{p.categoryName} • ID: {p.id}</p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 text-right text-emerald-400 font-bold text-sm">${p.price?.toFixed(2)}</td>
                <td className="px-6 py-4 text-right">
                  <span className={`text-sm font-medium ${p.stockQuantity <= 5 ? 'text-red-400' : 'text-zinc-400'}`}>
                    {p.stockQuantity}
                  </span>
                </td>
                <td className="px-6 py-4 text-center text-zinc-400 text-sm">
                  {p.averageRating > 0 ? `${p.averageRating.toFixed(1)} ⭐` : '—'}
                </td>
                <td className="px-6 py-4 text-center">
                  <div className="flex items-center justify-center gap-1">
                    <button onClick={() => triggerImageUpload(p.id)} title="Upload Image"
                      className="p-2 rounded-lg hover:bg-emerald-500/10 text-zinc-500 hover:text-emerald-400 transition-all">
                      <Upload size={14} />
                    </button>
                    {p.imageUrl && (
                      <button onClick={() => deleteImage(p.id)} title="Remove Image"
                        className="p-2 rounded-lg hover:bg-orange-500/10 text-zinc-500 hover:text-orange-400 transition-all">
                        <ImageIcon size={14} />
                      </button>
                    )}
                    <button onClick={() => openEdit(p)} title="Edit"
                      className="p-2 rounded-lg hover:bg-blue-500/10 text-zinc-500 hover:text-blue-400 transition-all">
                      <Pencil size={14} />
                    </button>
                    <button onClick={() => deleteProduct(p.id)} title="Delete"
                      className="p-2 rounded-lg hover:bg-red-500/10 text-zinc-500 hover:text-red-400 transition-all">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal */}
      {modal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-zinc-900 border border-white/10 rounded-2xl p-6 w-full max-w-lg">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white">{modal === 'add' ? 'Add Product' : 'Edit Product'}</h3>
              <button onClick={() => setModal(null)} className="text-zinc-500 hover:text-white"><X size={20} /></button>
            </div>

            <div className="space-y-3">
              <input type="text" placeholder="Product Name" value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
              <div className="grid grid-cols-2 gap-3">
                <input type="number" placeholder="Price" value={form.price}
                  onChange={(e) => setForm({ ...form, price: e.target.value })}
                  className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
                <input type="number" placeholder="Stock Quantity" value={form.stockQuantity}
                  onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })}
                  className="px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
              </div>
              <input type="number" placeholder="Category ID" value={form.categoryId}
                onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
                className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm" />
              <textarea placeholder="Description" value={form.description} rows={3}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 text-sm resize-none" />
            </div>

            {error && <p className="text-red-400 text-sm mt-3">{error}</p>}

            <div className="flex items-center gap-3 mt-5">
              <button onClick={() => setModal(null)} className="flex-1 py-2.5 border border-white/10 rounded-xl text-zinc-400 text-sm">Cancel</button>
              <button onClick={saveProduct} disabled={saving}
                className="flex-1 py-2.5 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl text-sm flex items-center justify-center gap-2 disabled:opacity-50">
                {saving ? 'Saving...' : <><Save size={14} /> {modal === 'add' ? 'Add Product' : 'Update'}</>}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminProducts;