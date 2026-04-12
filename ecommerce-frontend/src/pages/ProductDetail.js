import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import API from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  ShoppingCart, Heart, Star, ArrowLeft, TrendingUp, TrendingDown,
  Package, Minus, Plus, Check, Send, Sparkles
} from 'lucide-react';

const ProductDetail = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [qty, setQty] = useState(1);
  const [added, setAdded] = useState(false);
  const [reviews, setReviews] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });
  const [submitting, setSubmitting] = useState(false);
  const [showReviewForm, setShowReviewForm] = useState(false);

  useEffect(() => {
    fetchProduct();
    fetchReviews();
    fetchRecommendations();
    const interval = setInterval(fetchProduct, 15000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchProduct = async () => {
    try {
      const res = await API.get(`/products/${id}`);
      if (res.data.success) setProduct(res.data.data);
    } catch (err) {
      console.error('Failed to fetch product:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async () => {
    try {
      const res = await API.get(`/products/${id}/reviews`);
      if (res.data.success) setReviews(res.data.data || []);
    } catch (err) {
      console.error('Failed to fetch reviews:', err);
    }
  };

  const fetchRecommendations = async () => {
    try {
      const res = await API.get(`/products/${id}/recommendations?limit=4`);
      if (res.data.success) setRecommendations(res.data.data || []);
    } catch (err) {
      console.error('Failed to fetch recommendations:', err);
    }
  };

  const addToCart = async () => {
    if (!user) return navigate('/login');
    try {
      await API.post(`/cart/add?productId=${id}&quantity=${qty}`);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } catch (err) {
      console.error('Add to cart failed:', err);
    }
  };

  const addToWishlist = async () => {
    if (!user) return navigate('/login');
    try {
      await API.post(`/wishlist/${id}`);
    } catch (err) {
      console.error('Wishlist failed:', err);
    }
  };

  const submitReview = async () => {
    if (!user) return navigate('/login');
    if (!reviewForm.comment.trim()) return;
    setSubmitting(true);
    try {
      await API.post(`/products/${id}/reviews`, {
        rating: reviewForm.rating,
        comment: reviewForm.comment
      });
      setReviewForm({ rating: 5, comment: '' });
      setShowReviewForm(false);
      fetchReviews();
      fetchProduct();
    } catch (err) {
      console.error('Review failed:', err);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-8 h-8 border-2 border-emerald-500/30 border-t-emerald-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (!product) {
    return <div className="text-center py-20 text-zinc-500">Product not found</div>;
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">
      <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-zinc-500 hover:text-white text-sm mb-6 transition-colors">
        <ArrowLeft size={16} /> Back to products
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 mb-12">
        {/* Image */}
        <div className="aspect-square rounded-2xl bg-zinc-900 border border-white/5 overflow-hidden">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-zinc-700">
              <Package size={80} />
            </div>
          )}
        </div>

        {/* Info */}
        <div className="flex flex-col justify-center">
          <p className="text-xs text-emerald-500 font-bold uppercase tracking-widest mb-2">
            {product.categoryName || 'General'}
          </p>
          <h1 className="text-3xl font-bold text-white mb-3">{product.name}</h1>

          {product.averageRating > 0 && (
            <div className="flex items-center gap-2 mb-4">
              <div className="flex items-center gap-0.5">
                {[...Array(5)].map((_, i) => (
                  <Star key={i} size={16} className={i < Math.round(product.averageRating) ? 'text-amber-400 fill-amber-400' : 'text-zinc-700'} />
                ))}
              </div>
              <span className="text-sm text-zinc-400">
                {product.averageRating.toFixed(1)} ({product.reviewCount} reviews)
              </span>
            </div>
          )}

          <div className="flex items-baseline gap-3 mb-2">
            <span className="text-4xl font-black text-white">${product.price?.toFixed(2)}</span>
            {product.priceChangePercent != null && product.priceChangePercent !== 0 && (
              <span className={`flex items-center gap-1 text-sm font-bold ${product.priceChangePercent > 0 ? 'text-red-400' : 'text-emerald-400'}`}>
                {product.priceChangePercent > 0 ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                {product.priceChangePercent > 0 ? '+' : ''}{product.priceChangePercent?.toFixed(1)}%
              </span>
            )}
          </div>

          <div className="flex items-center gap-2 mb-6">
            <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xs text-zinc-500">AI dynamic pricing — price updates with demand</span>
          </div>

          <p className="text-zinc-400 text-sm leading-relaxed mb-6">
            {product.description || 'No description available.'}
          </p>

          <div className="flex items-center gap-2 mb-6">
            {product.inStock ? (
              <>
                <div className="w-2 h-2 rounded-full bg-emerald-500" />
                <span className="text-sm text-emerald-400">In stock ({product.stockQuantity} available)</span>
              </>
            ) : (
              <>
                <div className="w-2 h-2 rounded-full bg-red-500" />
                <span className="text-sm text-red-400">Out of stock</span>
              </>
            )}
          </div>

          {product.inStock && (
            <div className="flex items-center gap-3 mb-4">
              <div className="flex items-center border border-white/10 rounded-xl overflow-hidden">
                <button onClick={() => setQty(Math.max(1, qty - 1))} className="px-3 py-2.5 text-zinc-400 hover:text-white hover:bg-white/5">
                  <Minus size={16} />
                </button>
                <span className="px-4 py-2.5 text-white font-medium min-w-[3rem] text-center">{qty}</span>
                <button onClick={() => setQty(Math.min(product.stockQuantity, qty + 1))} className="px-3 py-2.5 text-zinc-400 hover:text-white hover:bg-white/5">
                  <Plus size={16} />
                </button>
              </div>
              <button
                onClick={addToCart}
                className={`flex-1 py-3 rounded-xl font-semibold flex items-center justify-center gap-2 transition-all
                  ${added ? 'bg-emerald-500 text-zinc-900' : 'bg-emerald-500 hover:bg-emerald-400 text-zinc-900'}`}
              >
                {added ? <><Check size={18} /> Added!</> : <><ShoppingCart size={18} /> Add to Cart</>}
              </button>
            </div>
          )}

          <button onClick={addToWishlist} className="w-full py-3 border border-white/10 rounded-xl text-zinc-400 hover:text-pink-400 hover:border-pink-500/30 hover:bg-pink-500/5 flex items-center justify-center gap-2 transition-all">
            <Heart size={18} /> Add to Wishlist
          </button>
        </div>
      </div>

      {/* Recommendations Section */}
      {recommendations.length > 0 && (
        <div className="border-t border-white/5 pt-8 mb-8">
          <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <Sparkles size={20} className="text-purple-400" />
            Customers Also Bought
          </h2>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {recommendations.map((rec) => (
              <Link
                key={rec.id}
                to={`/products/${rec.id}`}
                className="group bg-white/[0.02] border border-white/5 rounded-2xl overflow-hidden hover:border-purple-500/30 hover:bg-white/[0.04] transition-all"
              >
                <div className="aspect-square bg-zinc-900 overflow-hidden">
                  {rec.imageUrl ? (
                    <img src={rec.imageUrl} alt={rec.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-zinc-700">
                      <Package size={32} />
                    </div>
                  )}
                </div>
                <div className="p-3">
                  <p className="text-xs text-zinc-600 uppercase tracking-wider">{rec.categoryName || 'General'}</p>
                  <h3 className="text-white text-sm font-semibold truncate group-hover:text-purple-400 transition-colors">{rec.name}</h3>
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-white font-bold">${rec.price?.toFixed(2)}</span>
                    {rec.averageRating > 0 && (
                      <div className="flex items-center gap-1">
                        <Star size={10} className="text-amber-400 fill-amber-400" />
                        <span className="text-xs text-zinc-500">{rec.averageRating.toFixed(1)}</span>
                      </div>
                    )}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Reviews Section */}
      <div className="border-t border-white/5 pt-8">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-white">Reviews ({reviews.length})</h2>
          {user && (
            <button
              onClick={() => setShowReviewForm(!showReviewForm)}
              className="px-4 py-2 text-sm bg-white/5 border border-white/10 rounded-xl text-zinc-300 hover:text-white hover:border-white/20 transition-all"
            >
              Write a Review
            </button>
          )}
        </div>

        {showReviewForm && (
          <div className="bg-white/[0.02] border border-white/5 rounded-2xl p-5 mb-6">
            <div className="flex items-center gap-1 mb-3">
              {[1, 2, 3, 4, 5].map((star) => (
                <button key={star} onClick={() => setReviewForm({ ...reviewForm, rating: star })}>
                  <Star size={24} className={star <= reviewForm.rating ? 'text-amber-400 fill-amber-400' : 'text-zinc-700'} />
                </button>
              ))}
              <span className="text-zinc-400 text-sm ml-2">{reviewForm.rating}/5</span>
            </div>
            <textarea
              value={reviewForm.comment}
              onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
              placeholder="Share your experience with this product..."
              rows={3}
              className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-white placeholder-zinc-600 focus:outline-none focus:border-emerald-500/50 resize-none text-sm mb-3"
            />
            <button
              onClick={submitReview}
              disabled={submitting || !reviewForm.comment.trim()}
              className="px-5 py-2 bg-emerald-500 hover:bg-emerald-400 text-zinc-900 font-semibold rounded-xl text-sm flex items-center gap-2 disabled:opacity-50"
            >
              {submitting ? 'Submitting...' : <><Send size={14} /> Submit Review</>}
            </button>
          </div>
        )}

        {reviews.length === 0 ? (
          <p className="text-zinc-600 text-sm">No reviews yet. Be the first to review!</p>
        ) : (
          <div className="space-y-4">
            {reviews.map((review) => (
              <div key={review.id} className="bg-white/[0.02] border border-white/5 rounded-xl p-4">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-full bg-emerald-500/20 flex items-center justify-center text-emerald-400 text-sm font-bold">
                      {review.username?.charAt(0)?.toUpperCase() || 'U'}
                    </div>
                    <span className="text-white text-sm font-medium">{review.username || 'User'}</span>
                  </div>
                  <div className="flex items-center gap-0.5">
                    {[...Array(5)].map((_, i) => (
                      <Star key={i} size={12} className={i < review.rating ? 'text-amber-400 fill-amber-400' : 'text-zinc-700'} />
                    ))}
                  </div>
                </div>
                <p className="text-zinc-400 text-sm">{review.comment}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductDetail;